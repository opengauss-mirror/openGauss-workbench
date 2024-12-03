const routes = [
  {
    path: '/cluster-manage',
    redirect: '/cluster-manage/cluster-list'
  },
  {
    path: "/cluster-manage/cluster-list",
    name: "ClusterList",
    component: () =>
      import(/* webpackChunkName: "ClusterList" */ "../views/ClusterList/index.vue"),
    meta: {
      title: '集群列表',
      keepAlive: false
    }
  },
  {
    path: "/cluster-manage/cluster-list/details",
    name: "ClusterDetails",
    component: () =>
      import(/* webpackChunkName: "ClusterList" */ "../views/ClusterList/details/index.vue"),
    meta: {
      title: '集群详情',
      keepAlive: false
    }
  }
]

export default routes
