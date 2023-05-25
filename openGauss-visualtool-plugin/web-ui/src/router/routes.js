const routes = [
  {
    path: '/',
    redirect: '/index'
  },
  {
    path: '/index',
    name: 'HelloWorld',
    component: () => import('@/views/HelloWorld.vue'),
    meta: {
      title: 'Sql执行中心',
      keepAlive: false
    }
  },
  {
    path: '/license',
    name: 'License',
    component: () => import('@/views/License.vue'),
    meta: {
      title: 'license界面',
      keepAlive: false
    }
  }
]

export default routes
