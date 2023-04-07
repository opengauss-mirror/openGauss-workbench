import { EChartsOption } from 'echarts'
import * as echarts from 'echarts'
let chart: echarts.ECharts
export const initChart = (dom: any, cb: any) => {
  chart = echarts.init(dom)
  chart.on('georoam', () => {
    if (chart && chart.getOption()) {
      const nowOption: any = chart.getOption()
      console.log(nowOption)
      if (nowOption && nowOption.geo && nowOption.geo[0] && nowOption.geo[0].center && nowOption.geo[0].center[0] && nowOption.geo[0].center[1] && nowOption.geo[0].zoom) {
        const center = nowOption.geo[0].center
        center[0] = center[0].toFixed(5)
        center[1] = center[1].toFixed(5)
        const zoom = nowOption.geo[0].zoom.toFixed(5)
        console.log(cb)
        cb('georoam', { center, zoom })
      } else if (nowOption.series[0].center && nowOption.series[0].center[0] && nowOption.series[0].center[1] && nowOption.series[0].zoom) {
        const center = nowOption.series[0].center
        center[0] = center[0].toFixed(5)
        center[1] = center[1].toFixed(5)
        const zoom = nowOption.series[0].zoom.toFixed(5)
        cb('georoam', { center, zoom })
      } else {
        console.log(123)
      }
    }
  })
}
export const renderChart = (option: EChartsOption, mapData?: any) => {
  if (mapData) echarts.registerMap(mapData.name, mapData.data)
  chart && chart.setOption(option, true)
  console.log(option)
}
export const clearChart = () => {
  chart && chart.clear()
}
export const showLoading = () => {
  chart && chart.showLoading({
    maskColor: 'rgba(255, 255, 255, 0.2)'
  })
}
export const hideLoading = () => {
  chart && chart.hideLoading()
}
export const getBase64 = () => {
  if (chart) return chart.getDataURL()
}
