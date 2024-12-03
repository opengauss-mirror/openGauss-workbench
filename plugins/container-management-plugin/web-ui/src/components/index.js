import radioItem from './radio-item.vue';
import cmPagination from './cm-pagination.vue';
import refreshBtn from './refresh-btn.vue';
import descItem from './desc-item.vue';
export default {
  install(Vue) {
    Vue.component('radio-item', radioItem)
    Vue.component('cm-pagination', cmPagination)
    Vue.component('refresh-btn', refreshBtn)
    Vue.component('desc-item', descItem)
  }
}
