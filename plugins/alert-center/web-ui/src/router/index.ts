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
                path: "/vem/alert/alertRecord",
                meta: {
                    title: 'alertRecord.title',
                },
                component: () => import("@/views/alert/AlertRecord/Index.vue"),
            },
            {
                path: "/vem/alert/recordDetail",
                meta: {
                    title: 'alertRecord.title',
                    hidden: true
                },
                component: () => import("@/views/alert/AlertRecord/RecordDetail.vue"),
            },
            {
                path: "/vem/alert/AlertClusterNodeConf",
                meta: {
                    title: 'AlertClusterNodeConf.title',
                },
                component: () => import("@/views/alert/AlertClusterNodeConf/Index.vue"),
            },
            {
                path: "/vem/alert/alertTemplate",
                meta: {
                    title: 'alertTemplate.title',
                },
                component: () => import("@/views/alert/AlertTemplate/Index.vue"),
            },
            {
                path: "/vem/alert/alertRule",
                component: () => import("@/views/alert/AlertRule/Index.vue"),
                meta: {
                    title: 'alertRule.title',
                    breadcrumb: [],
                },
            },
            {
                path: "/vem/notify/notifyTemplate",
                component: () => import("@/views/notify/NotifyTemplate/Index.vue"),
                meta: {
                    title: 'notifyTemplate.title',
                },
            },
            {
                path: "/vem/notify/notifyWay",
                component: () => import("@/views/notify/NotifyWay/Index.vue"),
                meta: {
                    title: 'notifyWay.title',
                },
            },
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
