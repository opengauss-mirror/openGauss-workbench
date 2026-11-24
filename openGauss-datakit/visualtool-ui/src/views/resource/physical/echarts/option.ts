/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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