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

import { useEventListener } from "@vueuse/core";
import { Ref } from "vue";

/**
 * is dom element overflow
 * @param targetRef Ref<dom>
 * @param dropdownRef element-plus dropdownRef
 * @returns is overflow
 */
export const useWidthOverflow = (targetRef: Ref<HTMLElement>, dropdownRef: Ref<any>) => {
    const overflow = ref(false)
    const isFinish = ref(false)
    const stopWatch = watch(targetRef, t => {
        if (t) {
            overflow.value = t.scrollWidth > t.offsetWidth
            isFinish.value = true
            useEventListener(t, 'mouseenter', (e: MouseEvent) => {
                if (dropdownRef && dropdownRef.value) {
                    dropdownRef.value.$el.style.left = '300px'
                    if (overflow.value) {
                        dropdownRef.value.handleOpen()
                    }
                }
            })
            useEventListener(t, 'mouseleave', (e: MouseEvent) => {
                if (dropdownRef && dropdownRef.value) {
                    dropdownRef.value.handleClose()
                }
            })
            stopWatch()
        }
    }, { immediate: true })
    const trigger = () => {
        isFinish.value = false
        overflow.value = false
        if (targetRef && targetRef.value) {
            targetRef.value.style.justifyContent = 'flex-start';
            overflow.value = targetRef.value.scrollWidth > targetRef.value.offsetWidth
            isFinish.value = true
        }
    }
    useEventListener(window, 'resize', trigger)
    return { overflow, isFinish, trigger }
}
