import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const MODELING: AppRouteRecordRaw = {
  path: '/modeling',
  name: 'modeling',
  component: DEFAULT_LAYOUT,
  redirect: '/modeling/basic',
  meta: {
    title: '业务设计',
    requiresAuth: false,
    icon: 'icon-relation',
    order: 2
  },
  children: [
    {
      path: '/modeling/databasedesign',
      name: 'ModelingDatabasedesign',
      component: () => import('@/views/modeling/databasedesign/index.vue'),
      meta: {
        title: '数据建模',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/modeling/workflow',
      name: 'ModelingWorkflow',
      component: () => import('@/views/modeling/workflow/index.vue'),
      meta: {
        title: '业务流设计',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/modeling/dataflow',
      name: 'ModelingDataFlow',
      component: () => import('@/views/modeling/dataflow/index.vue'),
      meta: {
        title: '数据流设计',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/modeling/dataflow/detail',
      name: 'ModelingDataFlowDetail',
      component: () => import('@/views/modeling/dataflow/child/detail/index.vue'),
      meta: {
        title: '数据流详情',
        hideInMenu: true,
        requiresAuth: false,
        roles: ['*']
      }
    },
    {
      path: '/modeling/modelDesign',
      name: 'ModelDesign',
      component: () => import('@/views/modeling/modelDesign/index.vue'),
      meta: {
        title: '模型设计',
        hideInMenu: true,
        requiresAuth: false,
        roles: ['*']
      }
    }
  ]
}

export default MODELING
