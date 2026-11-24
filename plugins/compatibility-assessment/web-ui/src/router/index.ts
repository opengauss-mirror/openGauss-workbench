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
