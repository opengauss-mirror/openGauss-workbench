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
