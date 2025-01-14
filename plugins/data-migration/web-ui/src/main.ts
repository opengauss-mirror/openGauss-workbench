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

const app = createApp(App)

app.use(ArcoVue, {})
app.use(ArcoVueIcon)
app.use(ElementPlus)
app.use(opendesign2)

app.use(router)
app.use(globalComponents)
app.use(i18n)

app.mount('#app')
