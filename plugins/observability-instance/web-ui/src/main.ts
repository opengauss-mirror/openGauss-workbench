///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import { createApp } from 'vue'
import 'element-plus/theme-chalk/src/message.scss'
import 'element-plus/theme-chalk/src/table.scss'
import 'element-plus/theme-chalk/el-loading.css'
import 'element-plus/theme-chalk/el-message.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/assets/style/dark.scss'
import '@/assets/style/reset.scss'
import '@/assets/style/common.scss'
import App from '@/App.vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import 'virtual:svg-icons-register'
import router from '@/router'
import { vLoading } from 'element-plus'
import { i18n } from '@/i18n'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
const app = createApp(App)
app.directive('loading', vLoading)
app.use(i18n).use(router).use(pinia).mount('#app')
