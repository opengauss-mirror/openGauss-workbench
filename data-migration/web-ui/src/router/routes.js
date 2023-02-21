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
    component: () => import('@/views/task/index'),
    meta: {
      title: '创建迁移任务',
      keepAlive: false
    }
  },
  {
    path: '/taskDetail',
    name: 'TaskDetail',
    component: () => import('@/views/detail/index'),
    meta: {
      title: '迁移任务详情',
      keepAlive: false
    }
  }
]

export default routes
