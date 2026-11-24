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

import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import globalComponents from '@/components'
import App from './App.vue'
import router from './router'
import '@/api/interceptor'
import '@/assets/icons'
import i18n from './locale/index'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import opendesign2 from '@computing/opendesign2'
import '@computing/opendesign2/themes/es/css'

import '@arco-design/web-vue/dist/arco.less'
import '@/assets/style/global.less'
import store from '@/store';
import '@/utils/vue-echarts-polyfill'

const app = createApp(App)

app.use(ArcoVue, {})
app.use(ArcoVueIcon)
app.use(ElementPlus)
app.use(opendesign2)

app.use(router)
app.use(globalComponents)
app.use(i18n)
app.use(store)

app.mount('#app')
