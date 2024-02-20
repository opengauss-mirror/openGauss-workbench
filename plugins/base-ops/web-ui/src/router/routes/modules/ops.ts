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
      component: () => import('@/views/ops/simpleInstall/index.vue'),
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
      component: () => import('@/views/ops/install/Install.vue'),
      meta: {
        title: '集群安装',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/batchInstall',
      name: 'batchInstall',
      component: () => import('@/views/ops/batchInstall/index.vue'),
      meta: {
        title: '批量安装',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/upgrade',
      name: 'Upgrade',
      component: () => import('@/views/ops/upgrade/index.vue'),
      meta: {
        title: '批量升级',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/migrate',
      name: 'Migrate',
      component: () => import('@/views/ops/migrate/index.vue'),
      meta: {
        title: '集群迁移',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/backup',
      name: 'Backup',
      component: () => import('@/views/ops/backup/index.vue'),
      meta: {
        title: '备份恢复',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/olk',
      name: 'Olk',
      component: () => import('@/views/ops/resource/openLooKeng/index.vue'),
      meta: {
        title: 'OpenLooKeng管理',
        requiresAuth: true,
        roles: ['*']
      }
    },
    {
      path: '/ops/disasterClusterInstall',
      name: 'dtClusterInstall',
      component: () => import('@/views/ops/dtClusterInstall/index.vue'),
      meta: {
        title: '容灾集群安装',
        requiresAuth: true,
        roles: ['*']
      }
    },
  ]
}

export default OPS
