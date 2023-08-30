import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const Task: AppRouteRecordRaw = {
  path: '/task',
  name: 'task',
  component: DEFAULT_LAYOUT,
  redirect: '/task/manage',
  meta: {
    title: '任务中心',
    requiresAuth: false,
    icon: 'icon-list',
    order: 7,
    hideChildrenInMenu: true,
    activeMenu: 'task'
  },
  children: [
    {
      path: '/task/manage',
      name: 'TaskManage',
      component: () => import('@/views/task/manage/index.vue'),
      meta: {
        title: '任务中心',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/task/detail',
      name: 'TaskDetail',
      component: () => import('@/views/task/detail/index.vue'),
      meta: {
        title: '任务详情',
        hideInMenu: true,
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default Task
