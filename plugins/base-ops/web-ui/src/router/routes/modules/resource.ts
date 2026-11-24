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

import { DEFAULT_LAYOUT } from "@/router/constants";
import { AppRouteRecordRaw } from "../types";

const OPS: AppRouteRecordRaw = {
  path: "/resource",
  name: "resource",
  component: DEFAULT_LAYOUT,
  redirect: "/resource/disasterClusterManager",
  meta: {
    title: "资源中心",
    requiresAuth: false,
    icon: "icon-storage",
    order: 1,
  },
  children: [
    {
      path: "/resource/deviceManager",
      name: "DeviceManager",
      component: () => import("@/views/resource/diskArray/index.vue"),
      meta: {
        title: "磁阵管理",
        requiresAuth: true,
        roles: ["*"],
      },
    },
    {
      path: "/resource/disasterClusterManager",
      name: "DisasterClusterManager",
      component: () => import("@/views/resource/dtCluster/index.vue"),
      meta: {
        title: "容灾集群管理",
        requiresAuth: true,
        roles: ["*"],
      },
    },
  ],
};

export default OPS;
