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
