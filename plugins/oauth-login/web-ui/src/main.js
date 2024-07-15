import router from '../router/index.js'
import { createApp } from 'vue'
import App from './App.vue'
import authdemo from './authdemo.vue'

createApp(App).use(router).mount('#app');