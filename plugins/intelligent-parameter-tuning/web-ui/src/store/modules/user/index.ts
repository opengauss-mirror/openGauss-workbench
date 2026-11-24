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

import { defineStore } from 'pinia'
import {
  logout as userLogout,
  getUserInfo
} from '@/api/user'
import { clearToken } from '@/utils/auth'
import { removeRouteListener } from '@/utils/route-listener'
import useAppStore from '../app'

const useUserStore = defineStore('user', {
  state: (): any => ({
    userName: '',
    nickName: '',
    userId: undefined,
    phonenumber: '',
    email: undefined,
    remark: undefined,
    avatar: undefined
  }),

  getters: {
    userInfo (state: any): any {
      return { ...state }
    }
  },

  actions: {
    // Set user's information
    setInfo (partial: any) {
      this.$patch(partial)
    },

    // Reset user's information
    resetInfo () {
      this.$reset()
    },

    // Get user's information
    async info () {
      const res = await getUserInfo()

      this.setInfo(res.data)
    },

    logoutCallBack () {
      const appStore = useAppStore()
      this.resetInfo()
      clearToken()
      removeRouteListener()
      appStore.clearServerMenu()
    },
    // Logout
    async logout () {
      try {
        await userLogout()
      } finally {
        this.logoutCallBack()
      }
    }
  }
})

export default useUserStore
