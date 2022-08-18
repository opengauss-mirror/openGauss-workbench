import * as echarts from 'echarts/core'

export const cpuOption = {
  grid: {
    x: 40,
    y: 30,
    y2: 10
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    name: '%'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true,
      symbol: 'none',
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(195, 200, 251, 1)'
          },
          {
            offset: 1,
            color: '#FFFFFF'
          }
        ])
      },
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

export const memoryOption = {
  grid: {
    x: 40,
    y: 30,
    y2: 10
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    name: '%'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true,
      symbol: 'none',
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(155, 221, 166, 1)'
          },
          {
            offset: 1,
            color: '#FFFFFF'
          }
        ])
      },
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

export const netOption = {
  grid: {
    x: 40,
    y: 30,
    y2: 10
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    name: 'G'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true,
      symbol: 'none',
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(226, 232, 249, 1)'
          },
          {
            offset: 1,
            color: '#FFFFFF'
          }
        ])
      },
      itemStyle: {
        normal: {
          lineStyle: {
            width: 1
          }
        }
      }
    },
    {
      data: [],
      type: 'line',
      smooth: true,
      symbol: 'none',
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(249, 236, 231, 1)'
          },
          {
            offset: 1,
            color: '#FFFFFF'
          }
        ])
      },
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

export const connectOption = {
  grid: {
    x: 40,
    y: 30,
    y2: 10
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    name: 'pcs'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true,
      symbol: 'none',
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(157, 207, 251, 1)'
          },
          {
            offset: 1,
            color: '#FFFFFF'
          }
        ])
      },
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
  cpuOption.xAxis.data = []
  cpuOption.series[0].data = []
  memoryOption.xAxis.data = []
  memoryOption.series[0].data = []
  connectOption.xAxis.data = []
  connectOption.series[0].data = []
  netOption.xAxis.data = []
  netOption.series[0].data = []
  netOption.series[1].data = []
}
