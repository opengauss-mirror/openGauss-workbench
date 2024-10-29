<!-- Copyright(c) vue-admin-perfect(zouzhibin). -->
<template>
  <div class="main-container-wrapper" id="dataStudio">
    <Splitpanes class="default-theme main-container-split" :dbl-click-splitter="false">
      <Pane min-size="20" max-size="40" size="20">
        <Sidebar class="sidebar-container" />
      </Pane>
      <Pane>
        <div class="main-container">
          <TagViews />
          <div class="m-container-content" :class="{ 'app-main-hide-tag': !showTag }">
            <UMain />
          </div>
        </div>
      </Pane>
    </Splitpanes>
  </div>
</template>

<script lang="ts" setup name="layout">
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
  .sidebar-container-wrapper {
    display: flex;
    height: 100%;
  }
  .main-container {
    height: 100%;
    position: relative;
    overflow: hidden;
    display: flex;
    box-sizing: border-box;
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
  :deep(.el-divider--vertical) {
    height: 100%;
    margin: 0px 0px;
  }
  .main-container-split > :deep(.splitpanes__splitter) {
    border-left: none !important;
  }
</style>
