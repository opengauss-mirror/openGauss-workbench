import { createRouter, createWebHistory } from 'vue-router'

import { appRoutes } from './routes'

const router = createRouter({
  history: createWebHistory('/static-plugin/base-ops/'),
  routes: [
    {
      path: '/',
      name: 'index',
      redirect: '/modeling/dataflow',
      meta: {
        title: '',
        requiresAuth: false,
        keepAlive: false
      }
    },
    // web share
    {
      path: '/modeling/visualization/report/share/:id',
      name: 'ModelingPreview',
      component: () => import('@/views/modeling/dataflow/child/share/index.vue'),
      meta: {
        requiresAuth: false
      }
    },
    ...appRoutes
  ],
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router
