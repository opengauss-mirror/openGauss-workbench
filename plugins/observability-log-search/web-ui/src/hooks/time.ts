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

import { ComputedGetter, Ref } from "vue"

export const useIntervalTime = (cb: Function, timeFn: ComputedGetter<number> | Ref<number>) => {
    const timer = ref<number>()
    timer.value = setInterval(cb, typeof timeFn === 'function' ? timeFn() : timeFn.value)
    watch(timeFn, t => {
        if (timer.value) {
            clearInterval(timer.value)
            timer.value = setInterval(cb, t)
        }
    })
    onBeforeUnmount(() => {
        if (timer.value) {
            clearInterval(timer.value)
        }
    })
}
