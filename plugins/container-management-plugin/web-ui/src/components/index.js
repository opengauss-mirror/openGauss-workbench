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

import radioItem from './radio-item.vue';
import cmPagination from './cm-pagination.vue';
import refreshBtn from './refresh-btn.vue';
import descItem from './desc-item.vue';
export default {
  install(Vue) {
    Vue.component('radio-item', radioItem)
    Vue.component('cm-pagination', cmPagination)
    Vue.component('refresh-btn', refreshBtn)
    Vue.component('desc-item', descItem)
  }
}
