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

import type { Router } from 'vue-router'
import NProgress from 'nprogress'

import usePermission from '@/hooks/permission'
import { useUserStore, useAppStore } from '@/store'
import { appRoutes } from '../routes'
import { WHITE_LIST, NOT_FOUND } from '../constants'

export default function setupPermissionGuard (router: Router) {
  router.beforeEach(async (to, from, next) => {
    const appStore = useAppStore()
    const userStore = useUserStore()
    const Permission = usePermission()
    const permissionsAllow = Permission.accessRouter(to)
    if (appStore.menuFromServer) {
      // Handle routing configuration from the server

      // Refine the permission logic from the server's menu configuration as needed
      if (
        !appStore.appAsyncMenus.length &&
        !WHITE_LIST.find((el) => el.name === to.name)
      ) {
        await appStore.fetchServerMenuConfig()
      }
      // const serverMenuConfig = [...appStore.appAsyncMenus, ...WHITE_LIST]
      // The route obtained from the backend is appended to the routing system
      appStore.appAsyncMenus.forEach(route => router.addRoute(route))
      let exist = false
      const routes = router.getRoutes()
      while (routes.length && !exist) {
        const element = routes.shift()
        if (element?.name === to.name) exist = true
      }
      if (exist && permissionsAllow) {
        next()
      } else next(NOT_FOUND)
    } else {
      // eslint-disable-next-line no-lonely-if
      if (permissionsAllow) next()
      else {
        const destination =
          Permission.findFirstPermissionRoute(appRoutes, userStore.role) ||
          NOT_FOUND
        next(destination)
      }
    }
    NProgress.done()
  })
}
