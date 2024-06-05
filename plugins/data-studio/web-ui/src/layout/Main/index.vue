<!-- Copyright(c) vue-admin-perfect(zouzhibin). -->
<template>
  <div class="app-main" v-if="isReload">
    <Splitpanes
      class="default-theme"
      :dbl-click-splitter="false"
      @resize="iframePointerEvents = 'none'"
      @resized="iframePointerEvents = 'auto'"
    >
      <Pane min-size="30">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-slide" mode="out-in" appear>
            <keep-alive v-if="route.meta && route.meta.keepAlive" :include="cachedViewName">
              <component
                :is="wrap(route, Component)"
                :key="route.fullPath + `_${loadViewTime(route.fullPath)}`"
              />
            </keep-alive>
            <component :is="wrap(route, Component)" :key="route.fullPath" v-else />
          </transition>
        </router-view>
      </Pane>
      <Pane min-size="30" size="30" v-if="AppStore.isOpenSqlAssistant">
        <div class="iframe-wrapper">
          <div class="iframe-container">
            <div class="iframe-fix" v-show="iframePointerEvents == 'none'"></div>
            <iframe
              name="assistantFrame"
              :src="iframeSrc"
              width="100%"
              height="100%"
              frameborder="0"
              :pointer-events="iframePointerEvents"
              ref="assistantRef"
            >
            </iframe>
          </div>
        </div>
      </Pane>
    </Splitpanes>
  </div>
</template>

<script lang="ts" setup name="mainVue">
  import { useAppStore } from '@/store/modules/app';
  import { useSettingStore } from '@/store/modules/setting';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  const AppStore = useAppStore();
  const SettingStore = useSettingStore();
  const TagsViewStore = useTagsViewStore();

  const baseURL = import.meta.env.BASE_URL;
  const iframeSrc = computed(() => {
    return `${baseURL}statics/db_assistant/index_${
      AppStore.language === 'zh-CN' ? 'zh' : 'en'
    }.html`;
  });
  const iframePointerEvents = ref<'auto' | 'none'>('auto');
  const cachedViewName = computed(() => {
    // attention: `wrapperName` must in `cachedViewName`. Add `loadTime` is to solve problem of keep-alive and mounted More than once
    return TagsViewStore.visitedViews
      .filter((item) => item.meta?.keepAlive)
      .map((item) => item.path + item.loadTime);
  });

  const loadViewTime = (fullPath) => {
    const view = TagsViewStore.visitedViews.find((item) => item.fullPath == fullPath);
    return (view && view.loadTime) || 1;
  };

  const isReload = computed(() => SettingStore.isReload);

  const wrapperMap = new Map();
  const wrap = (route, component) => {
    let wrapper;
    // attention: `wrapperName` must in `cachedViewName`. Add `loadViewTime` is to solve problem of keep-alive and mounted More than once
    const wrapperName = route.path + loadViewTime(route.fullPath);
    if (wrapperMap.has(wrapperName)) {
      wrapper = wrapperMap.get(wrapperName);
    } else {
      wrapper = {
        name: wrapperName,
        render() {
          return h('div', { className: 'keepalive-page' }, component);
        },
      };
      wrapperMap.set(wrapperName, wrapper);
    }
    return h(wrapper);
  };
</script>

<style lang="scss" scoped>
  .app-main {
    flex: 1;
    overflow: hidden;
    flex-direction: column;
    width: 100%;
    box-sizing: border-box;
    background: var(--el-bg-color);
  }
  .keepalive-page {
    height: 100%;
  }
  .iframe-wrapper {
    margin: 10px;
    margin-left: 3px;
    height: calc(100% - 20px);
    border: 1px solid #d8d8d8;
    .iframe-container {
      height: 100%;
      position: relative;
      .iframe-fix {
        position: absolute;
        z-index: 100;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: #fff;
        opacity: 0;
      }
    }
  }
</style>
