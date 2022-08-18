import { createPinia } from 'pinia'
import useAppStore from './modules/app'
import useUserStore from './modules/user'
import useTabBarStore from './modules/tab-bar'
import useOpsStore from './modules/ops'

const pinia = createPinia()

export { useAppStore, useUserStore, useTabBarStore, useOpsStore }
export default pinia
