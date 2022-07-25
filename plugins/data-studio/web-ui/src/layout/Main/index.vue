<!-- Copyright(c) vue-admin-perfect(zouzhibin). -->
<template>
  <div class="app-main" v-if="isReload">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-slide" mode="out-in" appear>
        <keep-alive v-if="route.meta && route.meta.keepAlive" :include="cachedViewName">
          <component
            :is="wrap(route, Component)"
            :key="route.fullPath + `_${loadViewTime(route.fullPath)}`"
          />
        </keep-alive>
        <component :is="wrap(route.path, Component)" :key="route.fullPath" v-else />
      </transition>
    </router-view>
  </div>
</template>

<script lang="ts" setup name="mainVue">
  import { computed, h } from 'vue';
  import { useSettingStore } from '@/store/modules/setting';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  const SettingStore = useSettingStore();
  const TagsViewStore = useTagsViewStore();

  const cachedViewName = computed(() => {
    // attention: `wrapperName` must in `cachedViewName`. Add `loadTime` is to solve problem of keep-alive and mounted More than once
    return TagsViewStore.visitedViews.map((item) => item.path + item.loadTime);
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
    height: 100%;
    overflow: hidden;
    flex-direction: column;
    width: 100%;
    box-sizing: border-box;
    background: var(--el-bg-color-container);
  }
  .keepalive-page {
    height: 100%;
  }
</style>
