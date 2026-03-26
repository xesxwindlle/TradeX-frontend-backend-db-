<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5xy from '@amcharts/amcharts5/xy'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'
import * as marketApi from '../../api/market'

let root = null

const indices = [
  { symbol: '^DJI',  name: 'Dow Jones',   color: 0xef4444 },
  { symbol: '^IXIC', name: 'NASDAQ',       color: 0x007bff },
  { symbol: '^GSPC', name: 'S&P 500',      color: 0xf59e0b },
  { symbol: '^RUT',  name: 'Russell 2000', color: 0x10b981 },
]

onMounted(async () => {
  const results = await Promise.allSettled(
    indices.map(idx => marketApi.getChart(idx.symbol, 'Max'))
  )

  const indexData = indices.map((idx, i) => {
    const result = results[i]
    if (result.status === 'fulfilled' && result.value.data.data) {
      const quotes = result.value.data.data
      return {
        name: idx.name,
        color: idx.color,
        data: quotes.map(q => ({ Date: q.date, Close: q.close })),
      }
    }
    return { name: idx.name, color: idx.color, data: [] }
  })

  am5.ready(() => {
    root = am5.Root.new('indices-chart')
    root._logo?.dispose()

    const myTheme = am5.Theme.new(root)
    myTheme.rule('AxisLabel', ['minor']).setAll({ dy: 1 })
    myTheme.rule('Grid', ['x']).setAll({ strokeOpacity: 0.05 })
    myTheme.rule('Grid', ['x', 'minor']).setAll({ strokeOpacity: 0.05 })
    myTheme.rule('Label').setAll({ fill: am5.color(0xffffff) })

    root.setThemes([am5themes_Animated.new(root), myTheme])

    const chart = root.container.children.push(
      am5xy.XYChart.new(root, {
        panX: true,
        panY: true,
        wheelX: 'panX',
        wheelY: 'zoomX',
        pinchZoomX: true,
      })
    )

    const xAxis = chart.xAxes.push(
      am5xy.DateAxis.new(root, {
        baseInterval: { timeUnit: 'day', count: 1 },
        renderer: am5xy.AxisRendererX.new(root, { minorGridEnabled: true }),
      })
    )

    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(root, {
        renderer: am5xy.AxisRendererY.new(root, {}),
      })
    )

    indexData.forEach(s => {
      if (s.data.length === 0) return
      const series = chart.series.push(
        am5xy.LineSeries.new(root, {
          name: s.name,
          xAxis,
          yAxis,
          valueYField: 'Close',
          valueXField: 'Date',
          stroke: am5.color(s.color),
          fill: am5.color(s.color),
          tooltip: am5.Tooltip.new(root, {
            pointerOrientation: 'horizontal',
            labelText: '[bold]{name}[/]\n{valueY.formatNumber("#,###.00")}',
          }),
        })
      )
      series.data.setAll(s.data)
      series.appear()
    })

    const cursor = chart.set('cursor', am5xy.XYCursor.new(root, { behavior: 'none' }))
    cursor.lineY.set('visible', false)

    chart.set('scrollbarX', am5.Scrollbar.new(root, { orientation: 'horizontal' }))

    const legend = chart.rightAxesContainer.children.push(
      am5.Legend.new(root, {
        width: 140,
        paddingLeft: 15,
        height: am5.percent(100),
      })
    )
    legend.labels.template.setAll({ fill: am5.color(0xffffff), fontSize: 12 })
    legend.data.setAll(chart.series.values)

    chart.appear(1000, 100)
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
  <div id="indices-chart" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 380px;
}
</style>
