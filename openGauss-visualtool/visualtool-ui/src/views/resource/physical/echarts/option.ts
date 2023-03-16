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
      data: [
        { value: 0.9, name: '已占用' },
        { value: 0.1, name: '空闲' }
      ]
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
      data: [
        { value: 0.7, name: '已占用' },
        { value: 0.3, name: '空闲' }
      ]
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
      data: [
        { value: 0.3, name: '已占用' },
        { value: 0.5, name: '空闲' }
      ]
    }
  ]
}