/**
 Copyright(c) vue-admin-perfect(zouzhibin).
*/
import Layout from '@/layout/index.vue';

const errorRouter = {
  path: '/error',
  component: Layout,
  redirect: 'noRedirect',
  name: 'error',
  hidden: true,
  meta: {
    title: 'error page',
    icon: 'School',
  },
  children: [
    {
      path: '404',
      component: () => import('@/views/error/404.vue'),
      name: '404',
      meta: { title: '404', icon: 'MenuIcon' },
    },
  ],
};

export default errorRouter;
