/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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
