import { createRouter, createWebHashHistory, RouteRecordRaw } from "vue-router";

declare module 'vue-router' {
    // eslint-disable-next-line no-unused-vars
    interface RouteMeta {
        icon?: string;
        title: string;
        hidden?: boolean;
        breadcrumb?: string[];
    }
}

export const routes: RouteRecordRaw[] = [
    {
        path: "/vem",
        meta: {
            icon: "vite",
            title: 'app.menuName',
        },
        children: [
            {
                path: "/vem/dashboard/instance",
                component: () => import("@/pages/dashboard/Index.vue"),
                meta: {
                    title: 'dashboard.instance',
                    // breadcrumb: ['app.menuName', 'dashboard.name'],
                    breadcrumb: [],
                    // hidden: true,
                },
            },
            {
                path: "/vem/sql_detail/:dbid/:sqlId",
                meta: {
                    icon: "vite",
                    title: 'sql.sqlDetail',
                    breadcrumb: ['app.menuName', 'dashboard.instance'],
                    hidden: true
                },
                name: "SqlDetailLocal",
                component: () => import("@/pages/sql_detail/Index.vue"),
            },
            {
                path: "/vem/sql_detail",
                meta: {
                    icon: "vite",
                    title: 'sql.sqlDetail',
                    breadcrumb: ['app.menuName', 'dashboard.instance'],
                    hidden: true
                },
                name: "SqlDetail",
                component: () => import("@/pages/sql_detail/Index.vue"),
            }
        ]
    },
];

const router = createRouter({
    history: createWebHashHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    if (from.path.includes(`/vem/track_detail/`) && to.path.includes(`/vem/log/track`)) {
        sessionStorage.removeItem('nodes');
    }
    next();
})
export default router;
