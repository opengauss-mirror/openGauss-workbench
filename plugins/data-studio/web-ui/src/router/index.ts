import { createRouter, RouteRecordRaw, createWebHashHistory } from 'vue-router';
import Layout from '@/layout/index.vue';

interface extendRoute {
  hidden?: boolean;
}

import errorRouter from './modules/error';

export const constantRoutes: Array<RouteRecordRaw & extendRoute> = [
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
        path: '/table/:id',
        component: () => import('@/views/table/index.vue'),
        name: 'table',
        meta: { title: '', icon: 'table', keepAlive: true },
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
  history: createWebHashHistory(), // hash
  routes: constantRoutes,
});

export default router;
