import SvgIcon from './svg-icon/index.vue'
import { VAceEditor } from 'vue3-ace-editor'
export default {
  install (Vue) {
    Vue.component('svg-icon', SvgIcon)
    Vue.component('v-ace-editor', VAceEditor)
  }
}
