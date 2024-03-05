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
        path: '/vem/log/track',
        component: () => import('@/pages/datasource/track/Index.vue'),
        meta: {
          title: 'datasource.track',
          keepAlive: true,
          breadcrumb: ['app.menuName'],
        },
      },
      {
        path: '/vem/historyDiagnosis',
        component: () => import('@/pages/historyDiagnosis/Index.vue'),
        meta: {
          title: 'histroyDiagnosis.menuName',
          keepAlive: true,
          breadcrumb: ['app.menuName'],
        },
        props: {
          diagnosisType: 'history',
        },
      },
      {
        path: '/vem/diagnosisTaskDetail/:id',
        meta: {
          icon: 'vite',
          title: 'datasource.trackDetail',
          breadcrumb: ['app.menuName', 'datasource.track'],
          hidden: true,
        },
        component: () => import('@/pages/diagnosisTask/detail/Index.vue'),
      },
      {
        path: '/vem/diagnosisTaskDetail',
        meta: {
          icon: 'vite',
          title: 'datasource.trackDetail',
          breadcrumb: ['app.menuName', 'datasource.track'],
          hidden: true,
        },
        component: () => import('@/pages/diagnosisTask/detail/Index.vue'),
      },
      {
        path: '/vem/track_detail/:id',
        meta: {
          icon: 'vite',
          title: 'datasource.trackDetail',
          breadcrumb: ['app.menuName', 'datasource.track'],
          hidden: true,
        },
        component: () => import('@/pages/diagnosisTask/sqlDetail/Index.vue'),
      },
      {
        path: '/vem/track_detail',
        meta: {
          icon: 'vite',
          title: 'datasource.trackDetail',
          breadcrumb: ['app.menuName', 'datasource.track'],
          hidden: true,
        },
        component: () => import('@/pages/diagnosisTask/sqlDetail/Index.vue'),
      },
      {
        path: '/vem/track_large/:id/:type',
        meta: {
          icon: 'vite',
          title: 'datasource.trackDetail',
          breadcrumb: ['app.menuName', 'datasource.track'],
          hidden: true,
        },
        component: () => import('@/pages/datasource/task_detail/detail_large.vue'),
      },
      {
        path: '/vem/log/instance_config',
        component: () => import('@/pages/datasource/InstanceConfig.vue'),
        meta: {
          title: 'datasource.InstanceConfig',
          keepAlive: true,
          breadcrumb: ['app.menuName'],
        },
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
