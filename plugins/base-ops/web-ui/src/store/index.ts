import { createPinia } from 'pinia'
import useOpsStore from './modules/ops'
import useAppStore from './modules/app'
import useUserStore from './modules/user'

const pinia = createPinia()

export { useOpsStore, useAppStore, useUserStore }
export default pinia
