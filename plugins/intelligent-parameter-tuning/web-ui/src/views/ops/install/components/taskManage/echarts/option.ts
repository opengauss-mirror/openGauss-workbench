import * as echarts from 'echarts/core'

export const sessionOption = {
  grid: {
    x: 50,
    x2:0,
    y: 30,
    y2: 30,
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true,
      symbolSize: 4,
      itemStyle: {
        normal: {
          lineStyle: {
            width: 1
          }
        }
      }
    }
  ]
}

export const clearData = () => {
  sessionOption.xAxis.data = []
  sessionOption.series[0].data = []
}
