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

export const edge1 = {
  ports: {
    groups: {
      top: { position: 'top',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      },
      bottom: { position: 'bottom',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      },
      left: { position: 'left',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      },
      right: { position: 'right',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      }
    },
    items: [
      { group: 'top', id: 'port1' },
      { group: 'bottom', id: 'port2' },
      { group: 'left', id: 'port3' },
      { group: 'right', id: 'port4' }
    ]
  }
}
