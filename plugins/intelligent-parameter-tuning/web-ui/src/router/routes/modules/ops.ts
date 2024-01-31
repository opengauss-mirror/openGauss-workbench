import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const OPS: AppRouteRecordRaw = {
  path: '/ops',
  name: 'ops',
  component: DEFAULT_LAYOUT,
  redirect: '/ops/simpleInstall',
  meta: {
    title: '安装部署',
    requiresAuth: false,
    icon: 'icon-storage',
    order: 1
  },
  children: [
    {
      path: '/ops/tabList',
      name: 'OpsTabList',
      component: () => import('@/views/ops/install/tabList.vue'),
      meta: {
        title: '参数调优',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default OPS
