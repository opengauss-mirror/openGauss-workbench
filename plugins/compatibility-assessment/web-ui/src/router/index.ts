import { createRouter, createWebHistory } from 'vue-router'

import { appRoutes } from './routes'

const router = createRouter({
  history: createWebHistory('/static-plugin/compatibility-assessment/'),
  routes: [
    {
      path: '/',
      name: 'index',
      redirect: '/task',
      meta: {
        title: '',
        requiresAuth: false,
        keepAlive: false
      }
    },
    {
      path: '/assess',
      name: 'assess',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: 'sql评估',
        requiresAuth: true,
        roles: ['*']
      }
    },
    // web share
    {
      path: '/task',
      name: 'task Manage',
      component: () => import('@/views/task/index.vue'),
      meta: {
        title: '采集任务管理',
        requiresAuth: true,
        roles: ['*']
      }
    },
    ...appRoutes
  ],
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router
