import { EChartsOption } from 'echarts'
import * as echarts from 'echarts'
let chart: echarts.ECharts
export const initChart = (dom: any) => {
  chart = echarts.init(dom)
}
export const renderChart = (option: EChartsOption, mapData?: any) => {
  if (mapData) echarts.registerMap(mapData.name, mapData.data)
  chart && chart.setOption(option, true)
}
export const clearChart = () => {
  chart && chart.clear()
}
export const showLoading = () => {
  chart && chart.showLoading()
}
export const hideLoading = () => {
  chart && chart.hideLoading()
}
export const getBase64 = () => {
  if (chart) return chart.getDataURL()
}
