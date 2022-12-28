import * as echarts from 'echarts/core'

export const cpuOption = {
  grid: {
    x: 40,
    y: 30,
    y2: 30
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
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(115, 33, 212, 1)'
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
    y2: 30
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
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(32, 108, 207, 1)'
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
    y2: 30
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
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(114, 46, 209, 1)'
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
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(255, 125, 0, 1)'
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

export const lockOption = {
  grid: {
    x: 40,
    y: 30,
    y2: 30
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    name: 'pcs',
    interval: 1
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true,
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(25, 81, 255, 0.5)'
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

export const sessionOption = {
  grid: {
    x: 40,
    y: 30,
    y2: 30
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
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(209, 24, 24, 1)'
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
    y2: 30
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
      symbolSize: 4,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(1, 180, 42, 1)'
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

export const sessionTopOption = {
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: 100
  },
  xAxis: {
    type: 'value',
    name: 'Byte',
    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    name: 'SessionID',
    axisLabel: {
      formatter: '{value}'
    },
    data: []
  },
  series: [
    {
      name: 'session used memorySize',
      type: 'bar',
      barWidth: 20,
      data: [{
        value: 0,
        itemStyle: {
          color: '#4EA3FF'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#05B088'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#F5A43F'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#F55857'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#24E9C3'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#EEABA3'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#F5C03E'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#009682'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#B996FC'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#B996FC'
        }
      }]
    }
  ]
}

export const clearData = () => {
  cpuOption.xAxis.data = []
  cpuOption.series[0].data = []
  memoryOption.xAxis.data = []
  memoryOption.series[0].data = []
  lockOption.xAxis.data = []
  lockOption.series[0].data = []
  sessionOption.xAxis.data = []
  sessionOption.series[0].data = []
  connectOption.xAxis.data = []
  connectOption.series[0].data = []
  netOption.xAxis.data = []
  netOption.series[0].data = []
  netOption.series[1].data = []
  sessionTopOption.yAxis.data = []
  sessionTopOption.series[0].data = []
}
