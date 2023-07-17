<!-- Copyright(c) vue-admin-perfect(zouzhibin). -->
<template>
  <div class="tags-wrap-container">
    <div class="tags-view" ref="scrollContainer">
      <better-scroll :options="{ scrollX: true, scrollY: false }" ref="bsScroll">
        <div class="tags-scroll-inner" ref="tabsWrap">
          <div
            v-for="tag in visitedViews"
            :ref="setTagRef"
            :path="tag.path"
            :data-id="tag.path"
            :fullPath="tag.fullPath"
            :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
            :key="tag.path"
            @click="routerGo(tag)"
          >
            <div
              v-if="tag.path == '/home'"
              class="item-tag-wrap home"
              :class="isActive(tag) ? 'active' : ''"
              @contextmenu.prevent="contextMenu.visible = false"
            >
              <svg-icon :icon-class="tag.meta.icon" class-name="pre-icon" />
              <div class="tags-view-item">{{ $t('windows.home') }}</div>
            </div>
            <div
              v-else
              class="item-tag-wrap"
              :class="isActive(tag) ? 'active' : ''"
              @contextmenu.prevent="handleContextmenu($event, tag)"
            >
              <svg-icon :icon-class="tag.meta.icon" class-name="pre-icon" />
              <div class="tags-view-item" :title="tag.title">{{ tag.query.fileName }}</div>
              <el-icon
                v-if="!isAffix(tag)"
                @click.prevent.stop="closeSelectedTagToLastView(tag)"
                class="tag-icon"
              >
                <circle-close-filled />
              </el-icon>
            </div>
          </div>
        </div>
      </better-scroll>
    </div>
    <el-button class="create" @click="createTerminal" plain>
      <el-icon><Plus /></el-icon>
      {{ $t('create.terminal') }}
    </el-button>
    <LangButton v-if="!isInFrame" />
    <SwitchDark v-if="!isInFrame" />
    <div class="context-menu" :style="contextMenu.menuStyles">
      <ul v-if="contextMenu.visible" v-click-outside="handleClickContextMenuOutside">
        <li @click="refresh(contextTag)" v-if="isActive(contextTag) && contextTag.name == 'table'">
          {{ $t('windows.refresh') }}
        </li>
        <li @click="closeCurrentTab">{{ $t('windows.closeCurrentTab') }}</li>
        <li @click="closeOtherTab">{{ $t('windows.closeOtherTab') }}</li>
        <li @click="closeAllTabToLast">{{ $t('windows.closeAllTab') }}</li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts" setup name="TagsView">
  import { ElMessage } from 'element-plus';
  import betterScroll from './betterScroll.vue';
  import LangButton from '@/components/LangButton.vue';
  import { useRoute, useRouter } from 'vue-router';
  import SwitchDark from '@/components/SwitchTheme.vue';

  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { usePermissionStore } from '@/store/modules/permission';
  import { useAppStore } from '@/store/modules/app';
  import { useI18n } from 'vue-i18n';
  import vClickOutside from '@/directives/clickOutside';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import path from 'path-browserify';
  import { handleEventQueueProcedure, updateEventQueue } from '@/hooks/saveData';
  import { refreshRunningTags, deleteRunningTag } from '@/hooks/tagRunning';

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const PermissionStore = usePermissionStore();
  const AppStore = useAppStore();
  const { t } = useI18n();

  const isInFrame = ref(self !== parent);

  const refresh = (contextTag) => {
    TagsViewStore.reloadView(contextTag.fullPath);
    contextMenu.visible = false;
  };

  PermissionStore.generateRoutes();
  const routes = computed(() => PermissionStore.routes);

  const visitedViews = computed(() => TagsViewStore.visitedViews);

  const bsScroll = ref();
  let obj = new WeakMap();

  let affixTags = ref([]);
  const tags = ref(new Map());

  const setTagRef = (el) => {
    if (el) {
      if (!obj.get(el)) {
        tags.value.set(el.dataset['id'], el);
      }
      obj.set(el, el);
    }
  };

  const scrollContainer = ref();

  function filterAffixTags(routes, basePath = '/') {
    let tags = [];
    routes.forEach((route) => {
      if (route.meta && route.meta.affix) {
        const tagPath = path.resolve(basePath, route.path);
        tags.push({
          fullPath: tagPath,
          path: tagPath,
          name: route.name,
          meta: { ...route.meta },
        });
      }
      if (route.children) {
        const tempTags = filterAffixTags(route.children, route.path);
        if (tempTags.length >= 1) {
          tags = [...tags, ...tempTags];
        }
      }
    });
    return tags;
  }

  const initTags = () => {
    let routesNew = routes.value;
    let affixTag = (affixTags.value = filterAffixTags(routesNew));
    for (const tag of affixTag) {
      if (tag.name) {
        TagsViewStore.addVisitedView(tag);
      }
    }
  };

  const isActive = (view) => {
    return view.path === route.path;
  };
  const isAffix = (tag) => {
    return tag.meta && tag.meta.affix;
  };
  const addTags = () => {
    const { name } = route;
    if (name) {
      TagsViewStore.addVisitedView(route);
    }
    return false;
  };

  function toLastView(visitedViews, view?) {
    const latestView = visitedViews.slice(-1)[0];
    if (latestView) {
      router.push(latestView.fullPath);
    } else {
      if (view && view.name === 'home') {
        router.replace({ path: '/redirect' + view.fullPath });
      } else {
        router.push('/');
      }
    }
  }

  function toLastFilterView(visitedViews, excludeOptions: { path?: string; name?: string }) {
    const latestView = visitedViews
      .filter((item) => item.path !== excludeOptions.path)
      .filter((item) => item.name !== excludeOptions.name)
      .slice(-1)[0];
    if (latestView) {
      router.push(latestView.fullPath);
    } else {
      router.push('/');
    }
  }

  const closeSelectedTag = async (view) => {
    const viewId = view.id;
    const childViews = TagsViewStore.getDebugChildViews(view.id);
    const needToClose = [view].concat(childViews);
    // Judge whether the label (parent function, child function) that needs to be closed is in the current route. If so, select the last unrelated route and close all parent and child windows
    if (needToClose.findIndex((item) => item.id == TagsViewStore.getViewByRoute(route)?.id) > -1) {
      const myVisitedViews = visitedViews.value.map((item) => {
        const { matched, ...others } = item;
        return others;
      });
      const vice = JSON.parse(JSON.stringify(myVisitedViews));
      const lastOtherView = vice.reverse().find((item) => {
        return needToClose.findIndex((n) => n.id == item.id) == -1;
      });
      lastOtherView && routerGo(lastOtherView);
    }
    childViews.forEach((item) => {
      TagsViewStore.delView(item);
    });
    TagsViewStore.delViewById(viewId);
  };

  const closeSelectedTagToLastView = async (view) => {
    contextMenu.visible = false;
    if (view.name == 'debugChild') {
      return ElMessage.error(t('message.debugCannotFinish'));
    }
    if (deleteRunningTag(view.id)) {
      await handleEventQueueProcedure(view);
      closeSelectedTag(view);
      updateEventQueue();
      if (isActive(view)) {
        toLastView(visitedViews.value, view);
      }
    }
  };

  const closeCurrentTab = () => {
    contextMenu.visible = false;
    if (contextTag.value.name == 'debugChild') {
      return ElMessage.error(t('message.debugCannotFinish'));
    }
    if (deleteRunningTag(contextTag.value.id)) {
      closeSelectedTagToLastView(contextTag.value);
    }
  };

  const closeOtherTab = async () => {
    contextMenu.visible = false;
    const { path, name } = contextTag.value;
    for (let item of visitedViews.value) {
      if (item.path !== path && deleteRunningTag(item.id) && item.name !== 'debugChild') {
        await handleEventQueueProcedure(item);
        TagsViewStore.delView(item);
      }
    }
    updateEventQueue();
    if (path !== route.path || name == 'debugChild') {
      toLastFilterView(visitedViews.value, { name: 'debugChild' });
    }
  };

  const closeAllTab = async () => {
    contextMenu.visible = false;
    for (let item of visitedViews.value) {
      if (item.name !== 'debugChild' && deleteRunningTag(item.id)) {
        await handleEventQueueProcedure(item);
        TagsViewStore.delView(item);
      }
    }
    updateEventQueue();
  };
  const closeAllTabToLast = async () => {
    await closeAllTab();
    toLastView(TagsViewStore.visitedViews, route);
  };

  const routerGo = (tag) => {
    router.push({
      path: tag.path,
      query: tag.query,
    });
  };

  function handleScrollAction(currentTag) {
    const scrollContainerRect = scrollContainer.value.getBoundingClientRect();
    let { left: currx, width: currentWidth } = currentTag.getBoundingClientRect();
    const clientX = currx + currentWidth / 2;
    const currentX = clientX - scrollContainerRect.left;
    const deltaX = currentX - scrollContainerRect.width / 2;
    if (bsScroll.value) {
      const { maxScrollX, x: leftX } = bsScroll.value.instance;
      const rightX = maxScrollX - leftX;
      const update = deltaX > 0 ? Math.max(-deltaX, rightX) : Math.min(-deltaX, -leftX);
      bsScroll.value?.instance.scrollBy(update, 0, 300);
    }
  }

  function moveToCurrentTag() {
    nextTick(() => {
      for (const [key, tag] of tags.value) {
        let path = tag.attributes.path.value;
        if (path === route.path) {
          let fullPath = tag.attributes.fullPath.value;
          handleScrollAction(tag);
          if (fullPath !== route.fullPath) {
            TagsViewStore.updateVisitedView(route);
          }
          break;
        }
      }
    });
  }

  const contextMenu = reactive({
    menuStyles: {
      top: '0',
      left: '0',
    },
    visible: false,
  });

  function handleClickContextMenuOutside(): void {
    contextMenu.visible = false;
  }

  const tabsWrap = ref();
  // Right click the selected 'tag'
  const contextTag = ref<any>({});
  function handleContextmenu(event, tag): void {
    if (contextMenu.visible) handleClickContextMenuOutside();
    nextTick(() => {
      if (!tag) return;
      contextTag.value = tag;
      event.preventDefault();
      const position = {
        top: '30px',
        left: event.x + 2 + 'px',
      };
      contextMenu.menuStyles = position;
      contextMenu.visible = true;
    });
  }

  const createTerminal = () => {
    const { name: dbname, rootId, uuid } = AppStore.lastestConnectDatabase;
    const connectInfoName = AppStore.connectListMap.find((item) => item.id == rootId)?.info.name;
    if (!(uuid && connectInfoName)) return ElMessage.warning(t('message.noConnectionAvailable'));

    const terminalNum = TagsViewStore.maxTerminalNum + 1;
    const title = `${dbname}@${connectInfoName}(${terminalNum})`;
    const time = Date.now();
    router.push({
      path: '/createTerminal/' + time,
      query: {
        title,
        fileName: title,
        rootId,
        connectInfoName,
        uuid,
        dbname,
        terminalNum,
        time,
      },
    });
  };

  onMounted(() => {
    initTags();
    addTags();
    nextTick(() => {
      setTimeout(() => {
        moveToCurrentTag();
      }, 50);
    });

    watch(route, () => {
      addTags();
      nextTick(() => {
        setTimeout(() => {
          moveToCurrentTag();
          refreshRunningTags();
        }, 100);
      });
    });

    router.beforeEach(async (to, from, next) => {
      if (to.fullPath === '/home') {
        let whiteList = ['/error/404'];
        await TagsViewStore.removeView(whiteList);
      }
      next();
    });
    EventBus.listen(EventTypeName.CLOSE_SELECTED_TAB, async (view) => {
      await closeSelectedTagToLastView(view);
    });
    EventBus.listen(EventTypeName.CLOSE_ALL_TAB_TO_LAST, async () => {
      await closeAllTabToLast();
    });
    EventBus.listen(EventTypeName.CLOSE_ALL_TAB, async () => {
      await closeAllTab();
    });
  });
  onUnmounted(() => {
    EventBus.unListen(EventTypeName.CLOSE_SELECTED_TAB);
    EventBus.unListen(EventTypeName.CLOSE_ALL_TAB_TO_LAST);
    EventBus.unListen(EventTypeName.CLOSE_ALL_TAB);
  });
</script>

<style lang="scss" scoped>
  .tags-wrap-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    .tags-view {
      padding-left: 10px;
      height: 30px;
      background: white;
      display: flex;
      align-items: center;
      flex: 1;
      box-sizing: border-box;
      overflow: hidden;
    }
    .create {
      margin-right: 5px;
      :deep(.el-icon) {
        margin-right: 3px;
      }
    }
    .refresh {
      cursor: pointer;
      font-size: 22px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      .refresh-inner {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 50px;
        height: 100%;
      }
    }
  }
  .item-tag-wrap {
    position: relative;
    display: inline-flex;
    align-items: center;
    padding: 4px 6px;
    font-size: 14px;
    cursor: pointer;
    margin-right: 5px;
    border: 1px solid #d8dce5;
    user-select: none;
    &.home {
      color: var(--el-color-primary);
    }
    &.active .tag-icon {
      display: block;
    }
    &.active {
      background-color: var(--normal-color);
      color: var(--color-bg-2);
      border-color: var(--normal-color);
    }
  }
  .item-tag-wrap:hover {
    border-color: var(--normal-color);
  }
  .tags-scroll-inner {
    display: flex;
    flex-wrap: nowrap;
  }
  .pre-icon {
    margin-right: 4px;
  }
  .tag-icon {
    margin-left: 6px;
  }
  .tags-view-item {
    position: relative;
    z-index: 2;
    white-space: nowrap;
    .tags-inner {
      display: flex;
      align-items: center;
      white-space: nowrap;
    }
  }
  .more {
    background-color: $primaryColor;
    color: white;
    .tags-view-item {
      display: flex;
      align-items: center;
    }
  }
  .context-menu {
    position: fixed;
    z-index: 100;
    box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
    ul {
      padding: 3px 0;
      margin: 0;
      list-style-type: none;
      border-radius: 4px;
      background-color: var(--el-bg-color-overlay);
    }
    li {
      padding: 2px 12px;
      line-height: 20px;
      display: flex;
      align-items: center;
      white-space: nowrap;
      list-style: none;
      margin: 0;
      cursor: pointer;
      outline: 0;
      &.disabled {
        color: var(--color-text-4);
        cursor: not-allowed;
      }
      &:not(.disabled):hover {
        background-color: var(--color-fill-2);
      }
    }
  }
</style>
