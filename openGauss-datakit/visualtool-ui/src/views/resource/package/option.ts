export const cpuOption = {
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {d}%'
  },
  color: ['#868F9C', '#E41D1D'],
  series: [
    {
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
        { value: 0, name: 'used', unit: '%' },
        { value: 0, name: 'unused', unit: '%' }
      ]
    }
  ]
}

export const memoryOption = {
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {d}%'
  },
  color: ['#868F9C', '#91cc75'],
  series: [
    {
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
        { value: 0, name: 'used', unit: '%' },
        { value: 0, name: 'unused', unit: '%' }
      ]
    }
  ]
}

export const diskOption = {
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {d}%'
  },
  color: ['#868F9C', '#91cc75'],
  series: [
    {
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
        { value: 0, name: 'used', unit: '%' },
        { value: 0, name: 'unused', unit: '%' }
      ]
    }
  ]
}
