/**
 Copyright(c) vue-admin-perfect(zouzhibin).
*/
import { defineStore } from 'pinia';
import { PRIMARY_COLOR } from '../../config';

export const useSettingStore = defineStore({
  id: 'settingState',
  state: () => ({
    // menu isCollapse
    isCollapse: true,
    withoutAnimation: false,
    device: 'desktop',
    isReload: true,
    themeConfig: {
      // Menu display mode:  horizontal / vertical(default)
      mode: 'vertical',
      // tagsView is show? default: true
      showTag: true,
      primary: PRIMARY_COLOR,
    },
  }),
  getters: {},
  actions: {
    setThemeConfig({ key, val }) {
      this.themeConfig[key] = val;
    },
    setCollapse(value) {
      this.isCollapse = value;
      this.withoutAnimation = false;
    },
    closeSideBar({ withoutAnimation }) {
      this.isCollapse = false;
      this.withoutAnimation = withoutAnimation;
    },
    toggleDevice(device) {
      this.device = device;
    },
    setReload() {
      this.isReload = false;
      setTimeout(() => {
        this.isReload = true;
      }, 50);
    },
  },
});
