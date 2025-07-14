import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const RESOURCE: AppRouteRecordRaw = {
  path: '/resource',
  name: 'resource',
  component: DEFAULT_LAYOUT,
  redirect: '/resource/database',
  meta: {
    title: '资源中心',
    requiresAuth: false,
    icon: 'icon-nav',
    order: 4
  },
  children: [
    {
      path: '/resource/database',
      name: 'ResourceDatabase',
      component: () => import('@/views/resource/database/index.vue'),
      meta: {
        title: '实例管理',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/resource/protocol',
      name: 'ResourceProtocol',
      component: () => import('@/views/resource/protocol/index.vue'),
      meta: {
        title: '协议资源',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/resource/physical',
      name: 'ResourcePhysical',
      component: () => import('@/views/resource/physical/index.vue'),
      meta: {
        title: '设备管理',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/resource/physical/createserver',
      name: 'CreateServer',
      component: () => import('@/views/resource/physical/components/CreateServerPage.vue'),
      meta: {
        title: '创建服务器',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/resource/az',
      name: 'ResourceAz',
      component: () => import('@/views/resource/az/index.vue'),
      meta: {
        title: 'AZ管理',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default RESOURCE
