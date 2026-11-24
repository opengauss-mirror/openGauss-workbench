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
  history: createWebHistory('/static-plugin/base-ops/'),
  routes: [
    {
      path: '/',
      name: 'index',
      redirect: '/modeling/dataflow',
      meta: {
        title: '',
        requiresAuth: false,
        keepAlive: false
      }
    },
    // web share
    {
      path: '/modeling/visualization/report/share/:id',
      name: 'ModelingPreview',
      component: () => import('@/views/modeling/dataflow/child/share/index.vue'),
      meta: {
        requiresAuth: false
      }
    },
    ...appRoutes
  ],
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router
