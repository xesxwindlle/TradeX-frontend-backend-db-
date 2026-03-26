<script setup>
import { onMounted, onBeforeUnmount, watch, ref } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5xy from '@amcharts/amcharts5/xy'
import * as am5stock from '@amcharts/amcharts5/stock'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'
import { getChart } from '../../api/market'

const props = defineProps({
  symbol: { type: String, required: true },
})

// Unique IDs per instance so multiple charts can coexist
const uid = Math.random().toString(36).slice(2)
const chartId = `chartdiv-stock-${uid}`
const controlsId = `chartcontrols-stock-${uid}`

const period = ref('1Y')
const loading = ref(false)
const PERIODS = ['1M', '3M', '6M', '1Y', 'Max']

let root = null
let valueSeries = null
let volumeSeries = null
let sbSeries = null
let stockChart = null
let mainPanel = null
let valueLegend = null
let chartReady = false

function mapQuotes(quotes) {
  return quotes.map(q => ({
    Date: q.date,
    Open: q.open,
    High: q.high,
    Low: q.low,
    Close: q.close,
    Volume: q.volume,
  }))
}

async function loadData() {
  if (!chartReady || !props.symbol) return
  loading.value = true
  try {
    const r = await getChart(props.symbol, period.value)
    const data = mapQuotes(r.data.data || [])
    if (valueSeries) valueSeries.data.setAll(data)
    if (volumeSeries) volumeSeries.data.setAll(data)
    if (sbSeries) sbSeries.data.setAll(data)
  } catch (e) {
    console.warn('AmStockChart loadData error:', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.symbol, () => { if (chartReady) loadData() })
watch(period, () => { if (chartReady) loadData() })

onMounted(() => {
  am5.ready(async () => {
    root = am5.Root.new(chartId)
    root._logo.dispose()

    const myTheme = am5.Theme.new(root)
    myTheme.rule('Grid', ['scrollbar', 'minor']).setAll({ visible: false })

    root.setThemes([am5themes_Animated.new(root), myTheme])
    root.interfaceColors.set('background', am5.color(0x000000))
    root.interfaceColors.set('text', am5.color(0xffffff))
    root.interfaceColors.set('alternativeText', am5.color(0x000000))
    root.numberFormatter.set('numberFormat', '#,###.00')

    stockChart = root.container.children.push(
      am5stock.StockChart.new(root, { paddingRight: 0 })
    )
    mainPanel = stockChart.panels.push(
      am5stock.StockPanel.new(root, { wheelY: 'zoomX', panX: true, panY: true })
    )

    const valueAxis = mainPanel.yAxes.push(am5xy.ValueAxis.new(root, {
      renderer: am5xy.AxisRendererY.new(root, { pan: 'zoom' }),
      extraMin: 0.1,
      tooltip: am5.Tooltip.new(root, {}),
      numberFormat: '#,###.00',
      extraTooltipPrecision: 2,
    }))

    const dateAxis = mainPanel.xAxes.push(am5xy.GaplessDateAxis.new(root, {
      baseInterval: { timeUnit: 'day', count: 1 },
      renderer: am5xy.AxisRendererX.new(root, { minorGridEnabled: true }),
      tooltip: am5.Tooltip.new(root, {}),
    }))

    valueSeries = mainPanel.series.push(am5xy.CandlestickSeries.new(root, {
      name: props.symbol,
      turboMode: true,
      clustered: false,
      valueXField: 'Date',
      valueYField: 'Close',
      highValueYField: 'High',
      lowValueYField: 'Low',
      openValueYField: 'Open',
      calculateAggregates: true,
      xAxis: dateAxis,
      yAxis: valueAxis,
      legendValueText: 'open: [bold]{openValueY}[/] high: [bold]{highValueY}[/] low: [bold]{lowValueY}[/] close: [bold]{valueY}[/]',
      legendRangeValueText: '',
    }))

    stockChart.set('stockSeries', valueSeries)

    const volumeAxisRenderer = am5xy.AxisRendererY.new(root, { inside: true, pan: 'zoom' })
    volumeAxisRenderer.labels.template.set('forceHidden', true)
    volumeAxisRenderer.grid.template.set('forceHidden', true)

    const volumeValueAxis = mainPanel.yAxes.push(am5xy.ValueAxis.new(root, {
      numberFormat: '#.#a',
      height: am5.percent(20),
      y: am5.percent(100),
      centerY: am5.percent(100),
      renderer: volumeAxisRenderer,
    }))

    volumeSeries = mainPanel.series.push(am5xy.ColumnSeries.new(root, {
      name: 'Volume',
      turboMode: true,
      clustered: false,
      valueXField: 'Date',
      valueYField: 'Volume',
      xAxis: dateAxis,
      yAxis: volumeValueAxis,
      legendValueText: "[bold]{valueY.formatNumber('#,###.0a')}[/]",
    }))

    volumeSeries.columns.template.setAll({ strokeOpacity: 0, fillOpacity: 0.5 })
    volumeSeries.columns.template.adapters.add('fill', (fill, target) => {
      const dataItem = target.dataItem
      return dataItem ? stockChart.getVolumeColor(dataItem) : fill
    })

    stockChart.set('volumeSeries', volumeSeries)

    valueLegend = mainPanel.plotContainer.children.push(
      am5stock.StockLegend.new(root, { stockChart })
    )
    valueLegend.labels.template.setAll({ fill: am5.color(0xffffff) })
    valueLegend.valueLabels.template.setAll({ fill: am5.color(0xffffff) })
    valueLegend.data.setAll([valueSeries, volumeSeries])

    mainPanel.set('cursor', am5xy.XYCursor.new(root, {
      yAxis: valueAxis,
      xAxis: dateAxis,
      snapToSeries: [valueSeries],
      snapToSeriesBy: 'y!',
    }))

    const scrollbar = mainPanel.set('scrollbarX', am5xy.XYChartScrollbar.new(root, {
      orientation: 'horizontal',
      height: 50,
    }))
    stockChart.toolsContainer.children.push(scrollbar)

    const sbDateAxis = scrollbar.chart.xAxes.push(am5xy.GaplessDateAxis.new(root, {
      baseInterval: { timeUnit: 'day', count: 1 },
      renderer: am5xy.AxisRendererX.new(root, { minorGridEnabled: true }),
    }))
    const sbValueAxis = scrollbar.chart.yAxes.push(am5xy.ValueAxis.new(root, {
      renderer: am5xy.AxisRendererY.new(root, {}),
    }))
    sbSeries = scrollbar.chart.series.push(am5xy.LineSeries.new(root, {
      valueYField: 'Close',
      valueXField: 'Date',
      xAxis: sbDateAxis,
      yAxis: sbValueAxis,
    }))
    sbSeries.fills.template.setAll({ visible: true, fillOpacity: 0.3 })

    // Series type switcher
    const seriesSwitcher = am5stock.SeriesTypeControl.new(root, { stockChart })
    seriesSwitcher.events.on('selected', (ev) => setSeriesType(ev.item.id))

    function getNewSettings(series) {
      const settings = {}
      ;['name', 'valueYField', 'highValueYField', 'lowValueYField', 'openValueYField',
        'calculateAggregates', 'valueXField', 'xAxis', 'yAxis',
        'legendValueText', 'legendRangeValueText', 'stroke', 'fill',
      ].forEach(k => { settings[k] = series.get(k) })
      return settings
    }

    function setSeriesType(seriesType) {
      const cur = stockChart.get('stockSeries')
      const settings = getNewSettings(cur)
      const data = cur.data.values
      mainPanel.series.removeValue(cur)

      let series
      switch (seriesType) {
        case 'line':
          series = mainPanel.series.push(am5xy.LineSeries.new(root, settings))
          break
        case 'candlestick':
        case 'procandlestick':
          settings.clustered = false
          series = mainPanel.series.push(am5xy.CandlestickSeries.new(root, settings))
          if (seriesType === 'procandlestick') series.columns.template.get('themeTags')?.push('pro')
          break
        case 'ohlc':
          settings.clustered = false
          series = mainPanel.series.push(am5xy.OHLCSeries.new(root, settings))
          break
      }
      if (series) {
        valueLegend.data.removeValue(cur)
        series.data.setAll(data)
        stockChart.set('stockSeries', series)
        mainPanel.get('cursor')?.set('snapToSeries', [series])
        valueLegend.data.insertIndex(0, series)
        valueSeries = series
      }
    }

    const controlsEl = document.getElementById(controlsId)
    if (controlsEl) {
      am5stock.StockToolbar.new(root, {
        container: controlsEl,
        stockChart,
        controls: [
          am5stock.IndicatorControl.new(root, { stockChart, legend: valueLegend }),
          am5stock.DateRangeSelector.new(root, { stockChart }),
          seriesSwitcher,
          am5stock.DrawingControl.new(root, { stockChart }),
          am5stock.ResetControl.new(root, { stockChart }),
          am5stock.SettingsControl.new(root, { stockChart }),
        ],
      })
    }

    chartReady = true
    await loadData()
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
  <div class="chart-wrapper">
    <!-- Period selector -->
    <div class="period-row">
      <button
        v-for="p in PERIODS"
        :key="p"
        @click="period = p"
        :class="['period-btn', period === p ? 'active' : '']"
      >{{ p }}</button>
      <div v-if="loading" class="chart-spinner"></div>
    </div>

    <div :id="controlsId" class="chartcontrols-stock"></div>
    <div :id="chartId" class="chartdiv-stock"></div>
  </div>
</template>

<style scoped>
.chart-wrapper { width: 100%; }

.period-row {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 8px 14px;
  background: #18181b;
  border-bottom: 1px solid #27272a;
  border-radius: 12px 12px 0 0;
}

.period-btn {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  background: none;
  color: #71717a;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}
.period-btn:hover { background: #27272a; color: #e4e4e7; }
.period-btn.active { background: rgba(139,92,246,0.15); color: #a78bfa; }

.chart-spinner {
  width: 13px; height: 13px;
  border-radius: 50%;
  border: 2px solid #27272a;
  border-top-color: #8b5cf6;
  animation: spin 0.7s linear infinite;
  margin-left: 8px;
}
@keyframes spin { to { transform: rotate(360deg); } }

:global(.am5-modal input) {
  color: #000000 !important;
  background: #ffffff !important;
}

.chartcontrols-stock {
  height: auto;
  padding: 8px 14px;
  background: #18181b;
}
.chartcontrols-stock :deep(*) { color: white !important; }
.chartcontrols-stock :deep(button) {
  background-color: #27272a !important;
  color: white !important;
  border: 1px solid #3f3f46 !important;
  border-radius: 6px !important;
  padding: 5px 10px !important;
  margin: 0 2px !important;
  font-size: 11px !important;
  cursor: pointer !important;
}

.chartdiv-stock {
  width: 100%;
  height: 420px;
  background: #18181b;
  border-radius: 0 0 12px 12px;
}

</style>
