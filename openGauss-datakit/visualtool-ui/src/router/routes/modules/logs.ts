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

const SysLog: AppRouteRecordRaw = {
  path: '/logs',
  name: 'Logs',
  component: DEFAULT_LAYOUT,
  redirect: '/logs/sys-log',
  meta: {
    title: '日志管理',
    requiresAuth: false,
    icon: 'home',
    order: 0,
    hideChildrenInMenu: true
  },
  children: [
    {
      path: '/logs/sys-log',
      name: 'SysLog',
      component: () => import('@/views/logs/sys-log/index.vue'),
      meta: {
        title: '系统日志',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/logs/oper-log',
      name: 'OperLog',
      component: () => import('@/views/logs/oper-log/index.vue'),
      meta: {
        title: '操作日志',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default SysLog
