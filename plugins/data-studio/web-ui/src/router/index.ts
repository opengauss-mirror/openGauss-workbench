import { createRouter, RouteRecordRaw, createWebHashHistory } from 'vue-router';
import type { LocationQuery } from 'vue-router';
import pinia from '@/store/index';
import { useTagsViewStore } from '@/store/modules/tagsView';
import Layout from '@/layout/index.vue';

declare module 'vue-router' {
  interface Router {
    myPush: (to: MyRouteLocationRaw) => void;
  }
}
interface MyRouteLocationRaw {
  path: string;
  query?: LocationQuery;
  exParams?: LocationQuery;
}

import errorRouter from './modules/error';

export const constantRoutes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'layout',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: '/home',
        component: () => import('@/views/home/index.vue'),
        name: 'home',
        meta: { title: '', icon: 'home2', affix: true, role: ['other'], keepAlive: true },
      },
      {
        path: '/createUserRole/:id(\\d+)?',
        component: () => import('@/views/userRole/CreateUserRole.vue'),
        name: 'createUserRole',
        meta: { title: '', icon: 'user', keepAlive: true },
      },
      {
        path: '/editUserRole/:id',
        component: () => import('@/views/userRole/EditUserRole.vue'),
        name: 'editUserRole',
        meta: { title: '', icon: 'user', keepAlive: true },
      },
      {
        path: '/createTablespace/:id(\\d+)?',
        component: () => import('@/views/tablespace/CreateTablespace.vue'),
        name: 'createTablespace',
        meta: { title: '', icon: 'tablespace', keepAlive: true },
      },
      {
        path: '/editTablespace/:id',
        component: () => import('@/views/tablespace/EditTablespace.vue'),
        name: 'editTablespace',
        meta: { title: '', icon: 'tablespace', keepAlive: true },
      },
      {
        path: '/createTable/:id(\\d+)?',
        component: () => import('@/views/createTable/index.vue'),
        name: 'createTable',
        meta: { title: '', icon: 'table', keepAlive: true },
      },
      {
        path: '/table/:id',
        component: () => import('@/views/table/index.vue'),
        name: 'table',
        meta: { title: '', icon: 'table', keepAlive: true },
      },
      {
        path: '/tableRelatedSequence/:id',
        component: () => import('@/views/table/tableRelatedSequence.vue'),
        name: 'tableRelatedSequence',
        meta: { title: '', icon: 'table', keepAlive: true },
      },
      {
        path: '/createForeignTable/:id(\\d+)?',
        component: () => import('@/views/createForeignTable/index.vue'),
        name: 'createForeignTable',
        meta: { title: '', icon: 'table', keepAlive: true },
      },
      {
        path: '/foreignTable/:id',
        component: () => import('@/views/foreignTable/EditForeignTable.vue'),
        name: 'foreignTable',
        meta: { title: '', icon: 'table', keepAlive: true },
      },
      {
        path: '/createTrigger/:id(\\d+)?',
        component: () => import('@/views/trigger/CreateTrigger.vue'),
        name: 'createTrigger',
        meta: { title: '', icon: 'trigger', keepAlive: true },
      },
      {
        path: '/trigger/:id',
        component: () => import('@/views/trigger/EditTrigger.vue'),
        name: 'trigger',
        meta: { title: '', icon: 'trigger', keepAlive: true },
      },
      {
        path: '/createTerminal/:id(\\d+)?',
        component: () => import('@/views/terminal/index.vue'),
        name: 'createTerminal',
        meta: { title: '', icon: 'terminal', keepAlive: true },
      },
      {
        path: '/terminal/:id',
        component: () => import('@/views/terminal/index.vue'),
        name: 'terminal',
        meta: { title: '', icon: 'terminal', keepAlive: true },
      },
      {
        path: '/createDebug/:id(\\d+)?',
        component: () => import('@/views/debug/index.vue'),
        name: 'createDebug',
        meta: { title: '', icon: 'terminal', keepAlive: true },
      },
      {
        path: '/debug/:id',
        component: () => import('@/views/debug/index.vue'),
        name: 'debug',
        meta: { title: '', icon: 'terminal', keepAlive: true },
      },
      {
        path: '/debugChild/:id',
        component: () => import('@/views/debugChild/index.vue'),
        name: 'debugChild',
        meta: { title: '', icon: 'terminal', keepAlive: true },
      },
      {
        path: '/view/:id',
        component: () => import('@/views/view/index.vue'),
        name: 'view',
        meta: { title: '', icon: 'table', keepAlive: true },
      },
      {
        path: '/synonym/:id',
        component: () => import('@/views/synonym/index.vue'),
        name: 'synonym',
        meta: { title: '', icon: 'table', keepAlive: true },
      },
      {
        path: '/sequence/:id',
        component: () => import('@/views/sequence/index.vue'),
        name: 'sequence',
        meta: { title: '', icon: 'terminal', keepAlive: true },
      },
      {
        path: '/job/:rootId',
        component: () => import('@/views/jobs/index.vue'),
        name: 'jobs',
        meta: { title: '', icon: 'task', keepAlive: true },
      },
    ],
  },
  errorRouter,
  {
    path: '/:pathMatch(.*)',
    redirect: '/error/404',
  },
];

export const asyncRoutes = [];

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
});

router.myPush = (to: MyRouteLocationRaw) => {
  const TagsViewStore = useTagsViewStore(pinia);
  router.push({
    path: to.path,
    query: to.query,
  });
};

export default router;
