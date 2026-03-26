<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5hierarchy from '@amcharts/amcharts5/hierarchy'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'
import * as marketApi from '../../api/market'

const emit = defineEmits(['symbol-click'])

let root = null

onMounted(async () => {
  const [treemapRes, sectorsRes] = await Promise.allSettled([
    marketApi.getTreemap(100),
    marketApi.getSectors(),
  ])

  // symbol → { volume, change }
  const symbolMap = new Map()
  if (treemapRes.status === 'fulfilled' && treemapRes.value.data.data) {
    for (const item of treemapRes.value.data.data) {
      symbolMap.set(item.symbol, {
        volume: item.regularMarketVolume ?? item.averageDailyVolume3m ?? 1,
        change: item.regularMarketChangePercent,
      })
    }
  }

  const sectors = sectorsRes.status === 'fulfilled' && sectorsRes.value.data.data
    ? sectorsRes.value.data.data
    : {}

  const children = Object.entries(sectors)
    .map(([sector, industries]) => ({
      name: sector,
      children: Object.values(industries)
        .flat()
        .filter(sym => symbolMap.has(sym))
        .map(sym => ({
          name: sym,
          value: symbolMap.get(sym).volume,
          change: symbolMap.get(sym).change,
        })),
    }))
    .filter(s => s.children.length > 0)

  const treeData = [{ name: 'Market', children }]

  am5.ready(() => {
    root = am5.Root.new('sector-treemap')
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
        downDepth: 2,
        initialDepth: 2,
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

    series.rectangles.template.events.on('click', event => {
      const dataItem = event.target.dataItem
      if (!dataItem) return
      const ctx = dataItem.dataContext
      if (ctx?.value && typeof ctx?.change === 'number') {
        emit('symbol-click', ctx.name || '')
      }
    })

    series.rectangles.template.setAll({ cursorOverStyle: 'pointer' })

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
  <div id="sector-treemap" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 460px;
}
</style>
