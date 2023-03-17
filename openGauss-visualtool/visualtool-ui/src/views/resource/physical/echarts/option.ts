export const cpuOption = {
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      name: 'CPU使用率',
      type: 'pie',
      radius: '50%',
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }
  ]
}

export const memoryOption = {
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      name: '内存使用率',
      type: 'pie',
      radius: '50%',
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }
  ]
}

export const diskOption = {
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      name: '硬盘使用率',
      type: 'pie',
      radius: '50%',
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }
  ]
}