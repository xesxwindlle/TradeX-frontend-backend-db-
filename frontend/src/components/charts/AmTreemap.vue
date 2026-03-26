<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5hierarchy from '@amcharts/amcharts5/hierarchy'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'

const props = defineProps({
  data: { type: Array, required: true },
})
const emit = defineEmits(['symbol-click'])

let root = null

function buildTreemapData(items) {
  return [
    {
      name: 'Market',
      children: items.map(item => ({
        name: item.symbol,
        value: item.regularMarketVolume || item.averageDailyVolume3m || 1,
        change: item.regularMarketChangePercent,
      })),
    },
  ]
}

onMounted(() => {
  am5.ready(() => {
    root = am5.Root.new('treemap-chart')
    root._logo?.dispose()

    const customTheme = am5.Theme.new(root)
    customTheme.rule('RoundedRectangle', ['hierarchy', 'node', 'shape']).setAll({
      strokeWidth: 1,
      strokeOpacity: 0.6,
    })
    customTheme.rule('Label').setAll({
      fill: am5.color(0xffffff),
      fontSize: 11,
      centerX: am5.p50,
      centerY: am5.p50,
      textAlign: 'center',
    })

    root.setThemes([am5themes_Animated.new(root), customTheme])

    const zoomableContainer = root.container.children.push(
      am5.ZoomableContainer.new(root, {
        width: am5.p100,
        height: am5.p100,
        wheelable: true,
        pinchZoom: true,
        maxZoomLevel: 10,
        minZoomLevel: 1,
      })
    )

    zoomableContainer.children.push(
      am5.ZoomTools.new(root, { target: zoomableContainer })
    )

    const series = zoomableContainer.contents.children.push(
      am5hierarchy.Treemap.new(root, {
        maskContent: false,
        singleBranchOnly: false,
        downDepth: 1,
        initialDepth: 1,
        valueField: 'value',
        categoryField: 'name',
        childDataField: 'children',
        nodePaddingInner: 2,
        nodePaddingOuter: 2,
      })
    )

    series.rectangles.template.adapters.add('fill', (fill, target) => {
      const dataItem = target.dataItem
      if (!dataItem) return fill
      const change = dataItem.dataContext?.change
      if (typeof change !== 'number') return fill
      if (change >= 2) return am5.color(0x16a34a)
      if (change >= 0) return am5.color(0x10b981)
      if (change >= -2) return am5.color(0xef4444)
      return am5.color(0xb91c1c)
    })

    series.rectangles.template.adapters.add('stroke', () => am5.color(0xffffff))

    series.labels.template.adapters.add('text', (text, target) => {
      const dataItem = target.dataItem
      if (!dataItem) return text
      const ctx = dataItem.dataContext
      if (typeof ctx?.change === 'number') {
        return `${ctx.name}\n${ctx.change.toFixed(2)}%`
      }
      return ctx?.name || text
    })

    series.rectangles.template.events.on('click', (event) => {
      const dataItem = event.target.dataItem
      if (!dataItem) return
      const ctx = dataItem.dataContext
      if (ctx?.value && typeof ctx?.change === 'number') {
        emit('symbol-click', ctx.name || '')
      }
    })

    series.rectangles.template.setAll({ cursorOverStyle: 'pointer' })

    const treeData = buildTreemapData(props.data)
    series.data.setAll(treeData)
    series.appear(1000, 100)
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
  <div id="treemap-chart" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 420px;
}
</style>
