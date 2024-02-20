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
