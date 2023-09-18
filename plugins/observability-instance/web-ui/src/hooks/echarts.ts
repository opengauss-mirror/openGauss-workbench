///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import { EChartsOption } from 'echarts'
import { ECharts } from 'echarts/core'
import { Ref } from 'vue'
import { useMonitorStore } from '@/store/monitor'
import { ElMessage } from 'element-plus'
import { i18n } from '@/i18n'

const { t } = i18n.global

type XAxisType = EChartsOption['XAXisOption'] & { data: string[] }

let monitorStore: any

export const brushMap = reactive<Record<string, any>>({})
const toggleBrush = (brushType: string | boolean, uuid: string) => {
  brushMap[uuid]['chart']?.dispatchAction({
    type: 'takeGlobalCursor',
    key: 'brush',
    brushOption: {
      brushType,
    },
  })
  brushMap[uuid]['range'].start = ''
  brushMap[uuid]['range'].end = ''
}
const addBrushEvent = (uuid: string) => {
  const ev = (params: any) => {
    if (brushMap[uuid]['chart']?.containPixel('grid', [params.offsetX, params.offsetY])) {
      toggleBrush('lineX', uuid)
      brushMap[uuid]['chart']?.getZr().off('mousedown', ev)
    }
  }
  brushMap[uuid]['chart']?.getZr().off('mousedown', ev)
  brushMap[uuid]['chart']?.getZr().on('mousedown', ev)
}
export const useClearBrushSelect = (uuid: string) => {
  brushMap[uuid]['chart']?.dispatchAction({
    type: 'brush',
    command: 'clear',
    areas: [],
  })
  // toggleBrush(false, uuid)
  addBrushEvent(uuid)
}
export const useRange = (uuid: string) => {
  const range = ref({ start: '', end: '' })
  watch(
    () => brushMap[uuid],
    (m) => {
      if (m) {
        range.value = m['range']
      }
    }
  )
  return range
}

/**
 * brush select range
 * @param myChart echarts instance
 * @param uuid uuid
 * @returns range
 */
export const useRangeXAixsByBrush = (myChart: ECharts, uuid: string) => {
  monitorStore = useMonitorStore(uuid)
  brushMap[uuid] = Object.create(null)
  brushMap[uuid]['range'] = shallowReactive({
    start: '',
    end: '',
  })
  brushMap[uuid]['chart'] = myChart
  addBrushEvent(uuid)

  const brushEnd = (params: any) => {
    const option = myChart.getOption() as EChartsOption
    const xAxis = option.xAxis as XAxisType | XAxisType[]
    const xData = Array.isArray(xAxis) ? xAxis[0].data : xAxis.data

    monitorStore.timeType = 'CUSTOM'
    monitorStore.timeRange = [
      xData[myChart.convertFromPixel({ xAxisIndex: 0 }, params.areas[0].range[0])],
      xData[myChart.convertFromPixel({ xAxisIndex: 0 }, params.areas[0].range[1])],
    ]
  }
  myChart.off('brushEnd', brushEnd)
  myChart.on('brushEnd', brushEnd)
  return brushMap[uuid]['range']
}

const toggleDataZoomBrush = (myChart: ECharts, a: boolean) => {
  myChart.dispatchAction({
    type: 'takeGlobalCursor',
    key: 'dataZoomSelect',
    dataZoomSelectActive: a,
  })
}

const toggleDataZoomSelect = (myChart: ECharts, a: boolean) => {
  toggleDataZoomBrush(myChart, a)
}

const watchBrush = (myChart: ECharts) => {
  const startBrush = (params: { offsetX: number; offsetY: number }) => {
    if (myChart.containPixel('grid', [params.offsetX, params.offsetY])) {
      toggleDataZoomSelect(myChart, true)
      myChart.getZr().off('mousedown', startBrush)
    }
  }
  myChart.getZr().off('mousedown', startBrush)
  myChart.getZr().on('mousedown', startBrush)
}
const addZoomEvent = (myChart: ECharts, inZoom: Ref<boolean>) => {
  myChart.off('datazoom')
  myChart.on('datazoom', (params: any) => {
    toggleDataZoomBrush(myChart, false)
    watchBrush(myChart)
    if (params.startValue !== undefined) {
      return
    }
    if (params.batch && params.batch[0].start === 0 && params.batch[0].end === 0) {
      return
    }
    if (params.start !== undefined && params.start === 0 && params.end === 100) {
      return
    }
    inZoom.value = true
    const start = params.batch[0].startValue
    const end = params.batch[0].endValue
    if (start === end) {
      ElMessage.warning(t('echart.selectDataErrTip'))
      return
    }
    const option = myChart.getOption() as any
    monitorStore.manualRangeSelection([option.xAxis[0].data[start], option.xAxis[0].data[end]])
  })
}
export const useDataZoom = (myChart: ECharts, tabId: string) => {
  const inZoom = ref(false)
  monitorStore = useMonitorStore(tabId)
  watchBrush(myChart)
  addZoomEvent(myChart, inZoom)
  return {
    inZoom,
  }
}
