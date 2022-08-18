import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const PLUGIN: AppRouteRecordRaw = {
  path: '/plugin',
  name: 'plugin',
  component: DEFAULT_LAYOUT,
  redirect: '/plugin/manage',
  meta: {
    title: '插件功能',
    requiresAuth: false,
    icon: 'icon-apps',
    order: 5,
    hideChildrenInMenu: true,
    activeMenu: 'plugin'
  },
  children: [
    {
      path: '/plugin/manage',
      name: 'PluginManage',
      component: () => import('@/views/plugin/manage/index.vue'),
      meta: {
        title: '插件管理',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default PLUGIN
