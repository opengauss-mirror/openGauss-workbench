
<template>
  <a-modal
    class="cu-snapshot-container"
    :visible="visible"
    :title="$t('modeling.components.CUSnapshot.5m7iplopyjo0')"
    :footer="false"
    @cancel="close"
    @before-close="beforeClose"
    body-style="padding: 0; width: 100%;"
    :modal-style="{ minWidth: '1480px' }"
  >
    <div class="chart-container">
      <div class="chart-dom" ref="chartDomRef"></div>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { EChartsOption } from 'echarts'
import * as echarts from 'echarts'
import { KeyValue } from '@/types/global'
import { getMapGeo } from '@/api/modeling'
const visible = ref<boolean>(false)
const chartDomRef = ref<any>()
const open = (data: EChartsOption) => {
  visible.value = true
  nextTick(() => {
    initChart(data)
  })
}
const close = () => {
  visible.value = false
}
let chart: echarts.ECharts
const initChart = (option: EChartsOption) => {
  chart = echarts.init(chartDomRef.value)
  chart.showLoading()
  if (option.series && Array.isArray(option.series) && option.series.length > 0 && option.series[0] && option.series[0].type === 'map' && option.series[0].map) {
    getMapGeo(option.series[0].map).then((res: KeyValue) => {
      echarts.registerMap(res.mapData.name, res.mapData.data)
      chart.hideLoading()
      chart && chart.setOption(option, true)
    })
  } else {
    chart.hideLoading()
    chart && chart.setOption(option, true)
  }
}
const clearChart = () => {
  chart && chart.clear()
}
const beforeClose = () => {
  clearChart()
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .cu-snapshot-container{
    .chart-container {
      width: 100%;
      height: calc(100vh - 48px - 24px - 24px - 80px);
      .chart-dom {
        width: 100%;
        height: 100%;
      }
    }
  }
</style>
