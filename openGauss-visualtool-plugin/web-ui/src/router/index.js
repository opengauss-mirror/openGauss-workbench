import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
import { PLUGIN_ID } from '@/utils/const'

const router = createRouter({
  history: createWebHistory('/static-plugin/' + PLUGIN_ID + '/'),
  routes,
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router
