import { createRouter, createWebHistory } from 'vue-router'

import { appRoutes } from './routes'

const router = createRouter({
  history: createWebHistory('/static-plugin/MetaTune/'),
  routes: [
    {
      path: '/',
      name: 'index',
      redirect: '/ops/tabList',
      meta: {
        title: '',
        requiresAuth: false,
        keepAlive: false
      }
    },
    ...appRoutes
  ],
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router
