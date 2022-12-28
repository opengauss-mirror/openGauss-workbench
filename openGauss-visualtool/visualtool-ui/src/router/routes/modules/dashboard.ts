import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const DASHBOARD: AppRouteRecordRaw = {
  path: '/',
  name: 'Home',
  component: DEFAULT_LAYOUT,
  redirect: '/dashboard',
  meta: {
    title: '首页',
    requiresAuth: false,
    icon: 'home',
    order: 0,
    hideChildrenInMenu: true
  },
  children: [
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/ops/home/Index.vue'),
      meta: {
        title: '首页',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default DASHBOARD
