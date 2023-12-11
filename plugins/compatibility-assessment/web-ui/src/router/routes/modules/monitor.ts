import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const MONITOR: AppRouteRecordRaw = {
  path: '/monitor',
  name: 'monitor',
  component: DEFAULT_LAYOUT,
  redirect: '/monitor/basic',
  meta: {
    title: '监控运维',
    requiresAuth: false,
    icon: 'icon-robot',
    order: 3
  },
  children: [
    {
      path: '/monitor/dailyOps',
      name: 'DailyOps',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '集群运维',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/monitor/basic',
      name: 'MonitorBasic',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '集群监控',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/monitor/logCenter',
      name: 'LogCenter',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '日志分析',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/monitor/slowSql',
      name: 'SlowSql',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: 'SQL诊断',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/monitor/wdr',
      name: 'Wdr',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: 'WDR报告',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/monitor/customControl',
      name: 'CustomControl',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '自定义控制台',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/monitor/packageManage',
      name: 'PackageManage',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '安装包管理',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default MONITOR
