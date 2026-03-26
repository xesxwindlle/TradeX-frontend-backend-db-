<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5hierarchy from '@amcharts/amcharts5/hierarchy'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'
import * as marketApi from '../../api/market'

const emit = defineEmits(['symbol-click'])

let root = null

onMounted(async () => {
  let sectors = {}
  try {
    const r = await marketApi.getSectors()
    if (r.data.data) sectors = r.data.data
  } catch { /* noop */ }

  const treeData = {
    name: 'Market',
    children: Object.entries(sectors).map(([sector, industries]) => ({
      name: sector,
      children: Object.entries(industries).map(([industry, symbols]) => ({
        name: industry,
        children: symbols.map(sym => ({ name: sym, value: 1 })),
      })),
    })),
  }

  am5.ready(() => {
    root = am5.Root.new('sector-tree')
    root._logo?.dispose()
    root.setThemes([am5themes_Animated.new(root)])

    const zoomableContainer = root.container.children.push(
      am5.ZoomableContainer.new(root, {
        width: am5.p100,
        height: am5.p100,
        wheelable: true,
        pinchZoom: true,
      })
    )

    zoomableContainer.children.push(
      am5.ZoomTools.new(root, { target: zoomableContainer })
    )

    const series = zoomableContainer.contents.children.push(
      am5hierarchy.ForceDirected.new(root, {
        maskContent: false,
        singleBranchOnly: false,
        downDepth: 2,
        topDepth: 1,
        initialDepth: 3,
        valueField: 'value',
        categoryField: 'name',
        childDataField: 'children',
        idField: 'name',
        manyBodyStrength: -10,
        centerStrength: 0.8,
      })
    )

    series.get('colors')?.setAll({ step: 2 })
    series.links.template.set('strength', 0.5)
    series.labels.template.set('minScale', 0)
    series.labels.template.setAll({ fill: am5.color(0xffffff), fontSize: 11 })

    series.nodes.template.events.on('click', event => {
      const dataItem = event.target.dataItem
      if (!dataItem) return
      const ctx = dataItem.dataContext
      if (ctx?.value && !ctx?.children) {
        emit('symbol-click', ctx.name || '')
      }
    })

    series.nodes.template.adapters.add('cursorOverStyle', (_cursor, target) => {
      const dataItem = target.dataItem
      if (dataItem) {
        const ctx = dataItem.dataContext
        if (ctx?.value && !ctx?.children) return 'pointer'
      }
      return 'default'
    })

    series.data.setAll([treeData])
    series.set('selectedDataItem', series.dataItems[0])
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
  <div id="sector-tree" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 460px;
}
</style>
