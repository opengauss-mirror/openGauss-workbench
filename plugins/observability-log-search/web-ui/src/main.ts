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

import { createApp } from "vue";
import 'element-plus/theme-chalk/src/message.scss'
import 'element-plus/theme-chalk/src/table.scss'
import 'element-plus/theme-chalk/el-loading.css'
import 'element-plus/theme-chalk/el-message.css'
import 'element-plus/theme-chalk/dark/css-vars.css';
import '@/assets/style/dark.scss'
import '@/assets/style/reset.scss';
import '@/assets/style/common.scss';
import App from "./App.vue";
import { createPinia } from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import "virtual:svg-icons-register";
import router from "./router";
import { vLoading, ElInfiniteScroll } from "element-plus";
import { i18n } from './i18n'

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
const app = createApp(App)
app.directive('loading', vLoading)
app.directive("infiniteScroll", ElInfiniteScroll);
app.use(i18n).use(router).use(pinia).mount("#app");
