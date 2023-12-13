/**
 Copyright(c) vue-admin-perfect(zouzhibin).
*/
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import pinia from './store';

import './permission';
// svg-icons
import 'virtual:svg-icons-register';
import SvgIcon from '@/components/SvgIcon/index.vue'; // svg component
import FontIcon from '@/components/FontIcon.vue';
import { Splitpanes, Pane } from 'splitpanes';
import 'splitpanes/dist/splitpanes.css';
import AdvancedTable from '@/components/AdvancedTable/index.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
// dark mode: element-plus 2.2 has dark mode
import 'element-plus/theme-chalk/dark/css-vars.css';
// diy dark mode
import '@/styles/element-dark.scss';

import '@/styles/index.scss';

import '@/assets/iconfont/iconfont.css';
import '@/assets/iconfont/iconfont.js';

// globle layout component
import UContainerLayout from '@/components/u-container-layout/index.vue';
import ThreeSectionTabsPage from '@/components/ThreeSectionTabsPage.vue';

import { i18n } from '@/i18n/index';
import { dispatchEventStorage } from '@/utils';
import { prevTokenPersist, userPersist } from '@/config';
import { sidebarForage } from '@/utils/localforage';

const prevToken = prevTokenPersist.storage.getItem(prevTokenPersist.key);
const token = localStorage.getItem('opengauss-token');
if (prevToken !== token) {
  sessionStorage.clear();
  sidebarForage.clear();
  userPersist.storage.removeItem(userPersist.key);
  prevTokenPersist.storage.setItem(prevTokenPersist.key, token);
}

const app = createApp(App);

app.component('svg-icon', SvgIcon);
app.component('font-icon', FontIcon);
app.component('u-container-layout', UContainerLayout);
app.component('ThreeSectionTabsPage', ThreeSectionTabsPage);
app.component('Splitpanes', Splitpanes);
app.component('Pane', Pane);
app.component('AdvancedTable', AdvancedTable);

// icon
import * as ElIconsModules from '@element-plus/icons-vue';
Object.keys(ElIconsModules).forEach((key) => {
  // Loop traverse component name
  if ('Menu' !== key) {
    // if not 'Menu', skip. else, add 'Icon' suffix
    app.component(key, ElIconsModules[key]);
  } else {
    app.component(key + 'Icon', ElIconsModules[key]);
  }
});
app.use(pinia);
app.use(router);
app.use(i18n);
app.use(dispatchEventStorage);

app.use(ElementPlus).mount('#app');
