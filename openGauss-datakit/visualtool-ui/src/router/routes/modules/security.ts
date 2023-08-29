import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const SECURITY: AppRouteRecordRaw = {
  path: '/security',
  name: 'security',
  component: DEFAULT_LAYOUT,
  redirect: '/security/user',
  meta: {
    title: '安全中心',
    requiresAuth: false,
    icon: 'icon-settings',
    order: 6
  },
  children: [
    {
      path: '/security/user',
      name: 'SecurityUser',
      component: () => import('@/views/security/user/index.vue'),
      meta: {
        title: '账号与权限',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/security/role',
      name: 'SecurityRole',
      component: () => import('@/views/security/role/index.vue'),
      meta: {
        title: '角色管理',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/security/whitelist',
      name: 'SecurityWhitelist',
      component: () => import('@/views/security/whitelist/index.vue'),
      meta: {
        title: '访问白名单',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/security/usercenter',
      name: 'SecurityUsercenter',
      component: () => import('@/views/security/user/ucenter/index.vue'),
      meta: {
        title: '用户中心',
        hideInMenu: true,
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default SECURITY
