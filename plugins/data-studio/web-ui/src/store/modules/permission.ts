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
import { defineStore } from 'pinia';
import { constantRoutes } from '@/router/index';

export function filterAsyncRoutes(routes, roles) {
  const res = [];
  routes.forEach((route) => {
    const tmp = { ...route };
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, roles);
      }
      res.push(tmp);
    }
  });
  return res;
}

function hasPermission(roles, route) {
  if (route.meta && route.meta.roles) {
    return roles.some((role) => route.meta.roles.includes(role));
  } else {
    return false;
  }
}

export const usePermissionStore = defineStore({
  id: 'permissionState',
  state: () => ({
    routes: [],
    addRoutes: {},
  }),
  getters: {
    permission_routes: (state) => {
      return state.routes;
    },
  },
  actions: {
    generateRoutes() {
      return new Promise((resolve) => {
        const accessedRoutes = [];
        this.routes = constantRoutes.concat(accessedRoutes);
        this.addRoutes = accessedRoutes;
        resolve(accessedRoutes);
      });
    },
    clearRoutes() {
      this.routes = [];
      this.addRoutes = [];
    },
  },
});
