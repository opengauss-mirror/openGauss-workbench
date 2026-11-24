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

import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    roles?: string[]; // Controls roles that have access to the page
    requiresAuth: boolean; // Whether login is required to access the current page (every route must declare)
    icon?: string; // The icon show in the side menu
    title?: string; // The locale name show in side menu and breadcrumb
    hideInMenu?: boolean; // If true, it is not displayed in the side menu
    hideChildrenInMenu?: boolean; // if set true, the children are not displayed in the side menu
    activeMenu?: string; // if set name, the menu will be highlighted according to the name you set
    order?: number; // Sort routing menu items. If set key, the higher the value, the more forward it is
    noAffix?: boolean; // if set true, the tag will not affix in the tab-bar
    ignoreCache?: boolean; // if set true, the page will not be cached
  }
}
