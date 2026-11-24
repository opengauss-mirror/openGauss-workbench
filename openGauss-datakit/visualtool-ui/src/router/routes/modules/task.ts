/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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
