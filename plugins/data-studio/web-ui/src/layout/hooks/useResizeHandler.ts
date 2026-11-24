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
import { useSettingStore } from '@/store/modules/setting';
import { computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute } from 'vue-router';

const { body } = document;

const WIDTH = 992; // refer to Bootstrap's responsive design

export const useResizeHandler = () => {
  const SettingStore = useSettingStore();
  const route = useRoute();
  const device = computed(() => {
    return SettingStore.device;
  });
  function $_isMobile() {
    const rect = body.getBoundingClientRect();
    return rect.width - 1 < WIDTH;
  }
  function $_resizeHandler() {
    if (!document.hidden) {
      const isMobile = $_isMobile();
      SettingStore.toggleDevice(isMobile ? 'mobile' : 'desktop');

      if (isMobile) {
        SettingStore.closeSideBar({ withoutAnimation: true });
      }
    }
  }
  onMounted(() => {
    const isMobile = $_isMobile();
    if (isMobile) {
      SettingStore.toggleDevice('mobile');
      SettingStore.closeSideBar({ withoutAnimation: true });
    }
    window.addEventListener('resize', $_resizeHandler);

    watch(route, () => {
      if (device.value === 'mobile' && SettingStore.isCollapse) {
        SettingStore.closeSideBar({ withoutAnimation: false });
      }
    });
  });

  onUnmounted(() => {
    window.removeEventListener('resize', $_resizeHandler);
  });

  return { device };
};
