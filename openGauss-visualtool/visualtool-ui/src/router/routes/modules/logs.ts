import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const SysLog: AppRouteRecordRaw = {
  path: '/logs',
  name: 'Logs',
  component: DEFAULT_LAYOUT,
  redirect: '/logs/sys-log',
  meta: {
    title: '日志管理',
    requiresAuth: false,
    icon: 'home',
    order: 0,
    hideChildrenInMenu: true
  },
  children: [
    {
      path: '/logs/sys-log',
      name: 'SysLog',
      component: () => import('@/views/logs/sys-log/index.vue'),
      meta: {
        title: '系统日志',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/logs/oper-log',
      name: 'OperLog',
      component: () => import('@/views/logs/oper-log/index.vue'),
      meta: {
        title: '操作日志',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default SysLog
