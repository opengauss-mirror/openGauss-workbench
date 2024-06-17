import { createRouter, createWebHistory } from 'vue-router'


const routes = [
  {
    path: '/',
    name: 'index',
    alias: '/index',
    component: () => import('@/App.vue'),
  },
  {
    path: '/login',
    name: 'login',
    alias: '/callback',
    component: () => import('@/authdemo.vue'),
  },
]
const router = createRouter({
  history: createWebHistory('/static-plugin/oauth-login/oauth/'),
  routes: routes,
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router;
