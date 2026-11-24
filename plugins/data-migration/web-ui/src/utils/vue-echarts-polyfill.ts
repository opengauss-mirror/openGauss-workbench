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

if (typeof document !== 'undefined') {
  try {
    const descriptor = Object.getOwnPropertyDescriptor(
      Document.prototype,
      'adoptedStyleSheets'
    )
    if (descriptor?.set) {
      const originalSetter = descriptor.set
      Object.defineProperty(Document.prototype, 'adoptedStyleSheets', {
        ...descriptor,
        set(sheets: CSSStyleSheet[]) {
          try {
            originalSetter.call(this, sheets)
          } catch {
            console.warn('[vue-echarts-polyfill] adoptedStyleSheets not supported in sandbox environment')
          }
        }
      })
    }
  } catch {
    console.warn('[vue-echarts-polyfill] Document.prototype is not accessible')
  }
}
