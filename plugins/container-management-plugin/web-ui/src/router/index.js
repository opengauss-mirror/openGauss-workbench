import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
const { PLUGIN_ID } = require('@/utils/const')

const router = createRouter({
  history: createWebHistory('/static-plugin/' + PLUGIN_ID + '/'),
  routes,
  // scrollBehavior () {
  //   return { top: 0 }
  // }
})
// 处理tab数据
function disposeTab(to) {
  const { fullPath, meta } = to;
  // 获取标签数据
  const tabData = JSON.parse(sessionStorage.getItem('cm-page-tab') || '[]');
  const ci = tabData?.findIndex(tag => tag.fullPath === fullPath);
  const nd = { fullPath, meta };
  // 设置当前标签页
  if (ci > -1) {
    tabData[ci] = nd;
  } else {
    tabData.push(nd);
  }
  // 更新数据
  sessionStorage.setItem('cm-page-tab', JSON.stringify(tabData));
}

router.beforeEach((to) => {
  disposeTab(to);
})
// 卸载时清除数据
window.addEventListener('unload', () => {
  sessionStorage.removeItem('cm-page-tab')
})
export default router
