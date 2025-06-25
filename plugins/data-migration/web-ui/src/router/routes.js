const routes = [
  {
    path: '/',
    redirect: '/index'
  },
  {
    path: '/index',
    name: 'TaskCenter',
    component: () => import('@/views/list/index'),
    meta: {
      title: '迁移任务中心',
      keepAlive: false
    }
  },
  {
    path: '/taskConfig',
    name: 'TaskConfig',
    component: () => import('@/views/migration/index.vue'),
    meta: {
      title: '创建迁移任务',
      keepAlive: false
    }
  },
  {
    path: '/taskDetail',
    name: 'TaskDetail',
    component: () => import('@/views/detail/index.vue'),
    meta: {
      title: '迁移任务详情',
      keepAlive: false
    }
  },
  {
    path: '/subTaskDetail',
    name: 'SubTaskDetail',
    component: () => import('@/views/subTaskDetail/index'),
    meta: {
      title: '迁移子任务详情',
      keepAlive: false
    }
  },
  {
    path: '/thirdPartySoftwareConfig',
    name: 'ThirdPartySoftwareConfig',
    component: () => import('@/views/third/index'),
    meta: {
      title: '第三方软件配置中心',
      keepAlive: false
    }
  },
  {
    path: '/createtranscribetask',
    name: 'CreateTask',
    component: () => import('@/views/transcribe/task/index'),
    meta: {
      title: '创建录制回放任务',
      keepAlive: false
    }
  },
  {
    path: '/transcribe',
    name: 'Transcribe',
    component: () => import('@/views/transcribe/index'),
    meta: {
      title: '录制回放',
      keepAlive: false
    }
  },
  {
    path: '/transcribetaskDetail',
    name: 'TranscribeTaskDetail',
    component: () => import('@/views/transcribe/detail/index'),
    meta: {
      title: '录制回放任务详情',
      keepAlive: false
    }
  },
]

export default routes
