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
