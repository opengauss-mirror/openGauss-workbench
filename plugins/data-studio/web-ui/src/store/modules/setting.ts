/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
