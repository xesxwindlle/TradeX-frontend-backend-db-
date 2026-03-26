<script setup>
import { onMounted, onBeforeUnmount, watch } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5xy from '@amcharts/amcharts5/xy'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'

const props = defineProps({
  data: { type: Array, required: true },
})

let root = null
let yAxis = null
let series = null

watch(() => props.data, (newData) => {
  if (yAxis && series) {
    yAxis.data.setAll(newData)
    series.data.setAll(newData)
  }
}, { deep: true })

onMounted(() => {
  am5.ready(() => {
    root = am5.Root.new('winners-losers-chart')
    root._logo?.dispose()
    root.setThemes([am5themes_Animated.new(root)])

    const chart = root.container.children.push(
      am5xy.XYChart.new(root, {
        panX: false,
        panY: false,
        wheelX: 'none',
        wheelY: 'none',
        layout: root.verticalLayout,
      })
    )

    yAxis = chart.yAxes.push(
      am5xy.CategoryAxis.new(root, {
        categoryField: 'symbol',
        renderer: am5xy.AxisRendererY.new(root, {
          minGridDistance: 10,
          inversed: true,
        }),
      })
    )

    yAxis.get('renderer').labels.template.setAll({
      fill: am5.color(0xe1e4e8),
      fontSize: 12,
      fontWeight: '600',
    })

    const xAxis = chart.xAxes.push(
      am5xy.ValueAxis.new(root, {
        renderer: am5xy.AxisRendererX.new(root, {}),
      })
    )

    xAxis.get('renderer').labels.template.adapters.add('text', (text, target) => {
      const value = target.dataItem?.get('value')
      return value != null ? Number(value).toFixed(2) + '%' : text
    })

    xAxis.get('renderer').labels.template.setAll({
      fill: am5.color(0x9ca3af),
      fontSize: 11,
    })

    series = chart.series.push(
      am5xy.ColumnSeries.new(root, {
        xAxis,
        yAxis,
        valueXField: 'change',
        categoryYField: 'symbol',
        tooltip: am5.Tooltip.new(root, {
          labelText: '{categoryY}: {valueX.formatNumber("#.##")}%',
        }),
      })
    )

    series.columns.template.setAll({
      height: am5.percent(80),
      strokeOpacity: 0,
    })

    series.columns.template.adapters.add('fill', (fill, target) => {
      const value = target.dataItem?.get('valueX')
      return value != null && value >= 0 ? am5.color(0x10b981) : am5.color(0xef4444)
    })

    yAxis.data.setAll(props.data)
    series.data.setAll(props.data)
  })
})

onBeforeUnmount(() => {
  if (root) {
    root.dispose()
    root = null
  }
})
</script>

<template>
  <div id="winners-losers-chart" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 380px;
}
</style>
