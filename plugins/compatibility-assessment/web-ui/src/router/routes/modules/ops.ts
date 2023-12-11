import { DEFAULT_LAYOUT } from '@/router/constants'
import { AppRouteRecordRaw } from '../types'

const OPS: AppRouteRecordRaw = {
  path: '/ops',
  name: 'ops',
  component: DEFAULT_LAYOUT,
  redirect: '/ops/simpleInstall',
  meta: {
    title: '安装部署',
    requiresAuth: false,
    icon: 'icon-storage',
    order: 1
  },
  children: [
    {
      path: '/ops/simpleInstall',
      name: 'SimpleInstall',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '一键安装',
        requiresAuth: true,
        roles: ['*'],
        ignoreCache: true
      }
    },
    {
      path: '/ops/install',
      name: 'OpsInstall',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '集群安装',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/batchInstall',
      name: 'batchInstall',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '批量安装',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/upgrade',
      name: 'Upgrade',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '批量升级',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/migrate',
      name: 'Migrate',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '集群迁移',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/backup',
      name: 'Backup',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: '备份恢复',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/olk',
      name: 'Olk',
      component: () => import('@/views/assessment/assessment.vue'),
      meta: {
        title: 'OpenLooKeng管理',
        requiresAuth: true,
        roles: ['*']
      }
    }
  ]
}

export default OPS
