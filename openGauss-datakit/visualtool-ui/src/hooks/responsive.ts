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

import { onMounted, onBeforeMount, onBeforeUnmount } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { useAppStore } from '@/store'
import { addEventListen, removeEventListen } from '@/utils/event'

const WIDTH = 992 // https://arco.design/vue/component/grid#responsivevalue

function queryDevice () {
  const rect = document.body.getBoundingClientRect()
  return rect.width - 1 < WIDTH
}

export default function useResponsive (immediate?: boolean) {
  const appStore = useAppStore()
  function resizeHandler () {
    if (!document.hidden) {
      const isMobile = queryDevice()
      appStore.toggleDevice(isMobile ? 'mobile' : 'desktop')
      appStore.toggleMenu(isMobile)
    }
  }
  const debounceFn = useDebounceFn(resizeHandler, 100)
  onMounted(() => {
    if (immediate) debounceFn()
  })
  onBeforeMount(() => {
    addEventListen(window, 'resize', debounceFn)
  })
  onBeforeUnmount(() => {
    removeEventListen(window, 'resize', debounceFn)
  })
}
