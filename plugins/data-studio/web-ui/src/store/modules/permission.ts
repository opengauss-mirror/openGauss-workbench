/**
 Copyright(c) vue-admin-perfect(zouzhibin).
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
