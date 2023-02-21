import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'

const router = createRouter({
  history: createWebHistory('/static-plugin/data-migration/'),
  routes,
  scrollBehavior () {
    return { top: 0 }
  }
})

export default router
