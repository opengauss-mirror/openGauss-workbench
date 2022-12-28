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
