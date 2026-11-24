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

import { createRouter, createWebHashHistory, RouteRecordRaw } from "vue-router";

declare module "vue-router" {
    // eslint-disable-next-line no-unused-vars
    interface RouteMeta {
        icon?: string;
        title: string;
        hidden?: boolean;
        breadcrumb?: string[]; // Bread crumbs parent path title
    }
}
/**
 * 1、When adding hidden in the meta is true, the route will not appear in the left menu bar
 */
export const routes: RouteRecordRaw[] = [
    {
        path: "/vem",
        meta: {
            icon: "vite",
            title: "app.menuName",
        },
        children: [
            {
                path: "/vem/log",
                component: () => import("@/pages/datasource/LogSearch.vue"),
                meta: {
                    title: "datasource.name",
                },
            },
        ],
    },
];

const router = createRouter({
    history: createWebHashHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    next();
});
export default router;
