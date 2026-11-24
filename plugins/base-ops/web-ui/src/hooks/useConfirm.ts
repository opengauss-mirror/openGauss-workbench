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

import { ref } from 'vue'


const useConfirm = () => {
  const nextConfirmVisible = ref(false)
  const confirmMessage = ref('')

  let confirmPromiseResolve: ((value: boolean) => void) | null = null
  const waitConfirm = async () => {
    const confirmPromise = new Promise<boolean>(resolve => {
      confirmPromiseResolve = resolve
    })
    try {
      const confirmed = await confirmPromise
      return confirmed
    } catch (error) {
      return false
    }
  }

  const resolveAction = (confirmed: boolean) => {
    nextConfirmVisible.value = confirmed
    if (confirmPromiseResolve) {
      confirmPromiseResolve(confirmed)
    }
  }

  return {
    nextConfirmVisible,
    confirmMessage,
    waitConfirm,
    resolveAction
  }
}

export default useConfirm
