<!-- Copyright(c) vue-admin-perfect(zouzhibin). -->
<template>
  <div class="main-container-wrapper">
    <Sidebar class="sidebar-container" />
    <div class="main-container">
      <TagViews />
      <div class="m-container-content" :class="{ 'app-main-hide-tag': !showTag }">
        <UMain />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup name="layout">
  import { computed } from 'vue';
  import { useSettingStore } from '@/store/modules/setting';
  import Sidebar from './Sidebar/index.vue';
  import UMain from './Main/index.vue';
  import TagViews from './TagsView/index.vue';

  const SettingStore = useSettingStore();

  const showTag = computed(() => SettingStore.themeConfig.showTag);
</script>

<style lang="scss" scoped>
  .main-container-wrapper {
    height: 100%;
    display: flex;
  }
  .main-container {
    overflow: hidden;
    display: flex;
    flex: 1;
    box-sizing: border-box;
    flex-direction: column;
  }
  .sidebar-container {
    display: flex;
    flex-direction: column;
  }
  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 90;
  }
  .m-container-content {
    overflow: hidden;
    display: flex;
    flex: 1;
    position: relative;
    box-sizing: border-box;
    background-color: #f6f8ff;
  }
</style>
