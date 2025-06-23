import { createPinia } from 'pinia'
import useSubTaskStore from './modules/useTaskDetailStore'

const pinia = createPinia()

export { useSubTaskStore }
export default pinia
