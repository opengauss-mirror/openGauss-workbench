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
