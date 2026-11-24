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

const DASHBOARD: AppRouteRecordRaw = {
  path: '/',
  name: 'Home',
  component: DEFAULT_LAYOUT,
  redirect: '/dashboard',
  meta: {
    title: '首页',
    requiresAuth: false,
    icon: 'home',
    order: 0,
    hideChildrenInMenu: true
  },
  children: [
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/ops/home/Index.vue'),
      meta: {
        title: '首页',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default DASHBOARD
