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

import type { Router, LocationQueryRaw } from 'vue-router'
import NProgress from 'nprogress' // progress bar

import { useUserStore } from '@/store'
import { isLogin } from '@/utils/auth'

const whiteList = ['login']

export default function setupUserLoginInfoGuard (router: Router) {
  let isExpired = false
  router.beforeEach(async (to, from, next) => {
    NProgress.start()
    const userStore = useUserStore()
    if (isLogin()) {
      if (userStore.userId) {
        next()
      } else {
        if (!isExpired) {
          try {
            await userStore.info()
            isExpired = false
            next()
          } catch (error) {
            isExpired = true
            next({
              name: 'login',
              query: {
                redirect: to.name,
                ...to.query
              } as LocationQueryRaw
            })
          }
        }
      }
    } else {
      if (~whiteList.indexOf(to.name as string)) {
        next()
        return
      }
      next({
        name: 'login',
        query: {
          redirect: to.name,
          ...to.query
        } as LocationQueryRaw
      })
    }
  })
}
