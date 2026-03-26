<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5xy from '@amcharts/amcharts5/xy'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'

const props = defineProps({
  traded:  { type: Array, default: () => [] },
  held:    { type: Array, default: () => [] },
  watched: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})

const chartType = ref('traded')

let root = null
let xAxis = null
let series = null

function getCurrentData() {
  if (chartType.value === 'held') return props.held
  if (chartType.value === 'watched') return props.watched
  return props.traded
}

function updateChartData() {
  if (xAxis && series) {
    const data = getCurrentData()
    xAxis.data.setAll(data)
    series.data.setAll(data)
  }
}

watch(chartType, () => { updateChartData() })
watch(() => [props.traded, props.held, props.watched], () => { updateChartData() }, { deep: true })

onMounted(() => {
  root = am5.Root.new('ranking-chart')
  root._logo.dispose()
  root.setThemes([am5themes_Animated.new(root)])

  const chart = root.container.children.push(
    am5xy.XYChart.new(root, {
      panX: false,
      panY: false,
      wheelX: 'none',
      wheelY: 'none',
      paddingLeft: 0,
    })
  )

  chart.zoomOutButton.set('forceHidden', true)

  const xRenderer = am5xy.AxisRendererX.new(root, {
    minGridDistance: 30,
    minorGridEnabled: true,
  })

  xRenderer.labels.template.setAll({
    rotation: -45,
    centerY: am5.p50,
    centerX: am5.p100,
    paddingRight: 5,
    fill: am5.color(0xa1a1aa),
    fontSize: 12,
  })

  xRenderer.grid.template.set('visible', false)

  xAxis = chart.xAxes.push(
    am5xy.CategoryAxis.new(root, {
      maxDeviation: 0,
      categoryField: 'symbol',
      renderer: xRenderer,
    })
  )

  const yAxis = chart.yAxes.push(
    am5xy.ValueAxis.new(root, {
      maxDeviation: 0,
      min: 0,
      renderer: am5xy.AxisRendererY.new(root, {}),
    })
  )

  yAxis.get('renderer').labels.template.setAll({
    fill: am5.color(0xa1a1aa),
    fontSize: 12,
  })

  series = chart.series.push(
    am5xy.ColumnSeries.new(root, {
      name: 'Series 1',
      xAxis,
      yAxis,
      valueYField: 'count',
      categoryXField: 'symbol',
      tooltip: am5.Tooltip.new(root, {
        labelText: '{categoryX}: {valueY}',
      }),
    })
  )

  series.columns.template.setAll({
    cornerRadiusTL: 5,
    cornerRadiusTR: 5,
    strokeOpacity: 0,
  })

  series.columns.template.adapters.add('fill', (_fill, target) => {
    return chart.get('colors').getIndex(series.columns.indexOf(target))
  })

  series.columns.template.adapters.add('stroke', (_stroke, target) => {
    return chart.get('colors').getIndex(series.columns.indexOf(target))
  })

  series.bullets.push(() => {
    return am5.Bullet.new(root, {
      locationY: 1,
      sprite: am5.Label.new(root, {
        text: '{count}',
        fill: am5.color(0xffffff),
        centerY: 0,
        centerX: am5.p50,
        populateText: true,
        fontSize: 11,
      }),
    })
  })

  updateChartData()
  series.appear(1000)
  chart.appear(1000, 100)
})

onBeforeUnmount(() => {
  if (root) {
    root.dispose()
    root = null
  }
})
</script>

<template>
  <div>
    <div class="flex gap-1 mb-4">
      <button
        :class="['px-3 py-1.5 rounded-lg text-[12px] font-medium transition-all', chartType === 'traded' ? 'bg-accent-dim text-accent' : 'text-secondary hover:text-primary hover:bg-border']"
        @click="chartType = 'traded'"
      >Most Traded</button>
      <button
        :class="['px-3 py-1.5 rounded-lg text-[12px] font-medium transition-all', chartType === 'held' ? 'bg-accent-dim text-accent' : 'text-secondary hover:text-primary hover:bg-border']"
        @click="chartType = 'held'"
      >Most Held</button>
      <button
        :class="['px-3 py-1.5 rounded-lg text-[12px] font-medium transition-all', chartType === 'watched' ? 'bg-accent-dim text-accent' : 'text-secondary hover:text-primary hover:bg-border']"
        @click="chartType = 'watched'"
      >Most Watched</button>
    </div>
    <div v-if="loading" class="flex justify-center py-10 text-muted text-[13px]">Loading...</div>
    <div v-show="!loading" id="ranking-chart" class="chart-container"></div>
  </div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 360px;
}
</style>
