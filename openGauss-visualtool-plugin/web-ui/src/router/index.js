import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
const { PLUGIN_ID } = require('@/utils/const')

const router = createRouter({
  history: createWebHistory('/static-plugin/' + PLUGIN_ID + '/'),
  routes,
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router
