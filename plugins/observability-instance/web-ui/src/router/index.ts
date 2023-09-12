///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'

declare module 'vue-router' {
  // eslint-disable-next-line no-unused-vars
  interface RouteMeta {
    icon?: string
    title: string
    hidden?: boolean
    breadcrumb?: string[]
  }
}

export const routes: RouteRecordRaw[] = [
  {
    path: '/vem',
    meta: {
      icon: 'vite',
      title: 'app.menuName',
    },
    children: [
      {
        path: '/vem/dashboard/clusters',
        component: () => import('@/pages/clusterMonitor/Index.vue'),
        meta: {
          title: 'dashboard.clusters',
          breadcrumb: [],
        },
      },
      {
        path: '/vem/clusterDetail/:dbid',
        meta: {
          icon: 'vite',
          title: 'session.tabTitle',
          breadcrumb: ['app.menuName', 'session.tabTitle'],
          hidden: true,
        },
        name: 'ClusterDetailLocal',
        component: () => import('@/pages/clusterMonitor/cluster/Index.vue'),
      },
      {
        path: '/vem/clusterDetail',
        meta: {
          icon: 'vite',
          title: 'session.tabTitle',
          breadcrumb: ['app.menuName', 'session.tabTitle'],
        },
        name: 'ClusterDetail',
        component: () => import('@/pages/clusterMonitor/cluster/Index.vue'),
      },
      {
        path: '/vem/dashboard/instance',
        component: () => import('@/pages/dashboardV2/Index.vue'),
        meta: {
          title: 'dashboard.instance',
          breadcrumb: [],
        },
      },
      {
        path: '/vem/sessionDetail/:dbid/:id',
        meta: {
          icon: 'vite',
          title: 'session.tabTitle',
          breadcrumb: ['app.menuName', 'session.tabTitle'],
          hidden: true,
        },
        name: 'SessionDetailLocal',
        component: () => import('@/pages/dashboardV2/instanceMonitor/sessionMonitor/Detail.vue'),
      },
      {
        path: '/vem/sessionDetail',
        meta: {
          icon: 'vite',
          title: 'session.tabTitle',
          breadcrumb: ['app.menuName', 'session.tabTitle'],
        },
        name: 'SessionDetail',
        component: () => import('@/pages/dashboardV2/instanceMonitor/sessionMonitor/Detail.vue'),
      },
      {
        path: '/vem/sql_detail/:dbid/:sqlId',
        meta: {
          icon: 'vite',
          title: 'sql.sqlDetail',
          breadcrumb: ['app.menuName', 'dashboard.instance'],
          hidden: true,
        },
        name: 'SqlDetailLocal',
        component: () => import('@/pages/sql_detail/Index.vue'),
      },
      {
        path: '/vem/sql_detail',
        meta: {
          icon: 'vite',
          title: 'sql.sqlDetail',
          breadcrumb: ['app.menuName', 'dashboard.instance'],
          hidden: true,
        },
        name: 'SqlDetail',
        component: () => import('@/pages/sql_detail/Index.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  if (from.path.includes(`/vem/track_detail/`) && to.path.includes(`/vem/log/track`)) {
    sessionStorage.removeItem('nodes')
  }
  next()
})
export default router
