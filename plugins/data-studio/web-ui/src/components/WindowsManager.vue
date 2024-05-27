<template>
  <div class="window-manager">
    <el-popover
      v-model:visible="visible"
      placement="right-start"
      width="auto"
      trigger="click"
      popper-style="max-width: 450px;max-height: calc(100% - 60px);overflow-y: auto;border: 3px solid var(--el-border-color-light);"
      :offset="5"
    >
      <template #reference>
        <el-button :icon="CaretBottom" class="button"></el-button>
      </template>
      <div class="windows-wrapper group-wrapper">
        <div v-if="props.showTitle" class="group-title-contain">
          <div class="left">
            <svg-icon icon-class="window" class-name="icon" /> {{ $t('windows.list') }}
          </div>
        </div>
        <div class="group-search-contain">
          <el-input
            v-model="input2"
            :placeholder="$t('windows.placeholder')"
            :prefix-icon="Search"
          />
        </div>
        <div class="group-main-contain">
          <ul class="windows-list">
            <li
              v-for="tag in visitedViews"
              :key="tag.path"
              @click="routerGo(tag)"
              :class="['windows-item', { home: tag.path == '/home', active: isActive(tag) }]"
            >
              <svg-icon :icon-class="tag.meta.icon" class-name="icon" />
              <span class="name" v-if="tag.path == '/home'">{{ $t('windows.home') }}</span>
              <span class="name" v-else :title="tag.title">
                {{ decodeURIComponent(tag.showName) }}
              </span>
            </li>
          </ul>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script lang="ts" setup name="WindowsManager">
  import { CaretBottom, Search } from '@element-plus/icons-vue';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { useRoute, useRouter } from 'vue-router';

  const props = withDefaults(
    defineProps<{
      showTitle?: boolean;
    }>(),
    {
      showTitle: false,
    },
  );
  const TagsViewStore = useTagsViewStore();
  const route = useRoute();
  const router = useRouter();

  const input2 = ref<string>('');
  const visible = ref(false);

  const visitedViews = computed(() => {
    return input2.value
      ? TagsViewStore.visitedViews.filter((item) =>
          decodeURIComponent(item.showName).includes(input2.value),
        )
      : TagsViewStore.visitedViews;
  });

  const isActive = (rou) => {
    return rou.path === route.path;
  };
  const routerGo = (tag) => {
    visible.value = false;
    router.push({
      path: tag.path,
      query: tag.query,
    });
  };
</script>

<style lang="scss" scoped>
  .window-manager {
    display: inline-block;
    .button {
      padding: 5px 6px;
      height: 27px;
      border-radius: 9px;
      margin-right: 3px;
      background-color: transparent;
      border: 1px solid #d8dce5;
      &:hover {
        background-color: var(--el-button-hover-bg-color);
      }
      &:active {
        background-color: var(--el-button-active-bg-color);
      }
    }
  }
  .group-wrapper {
    display: flex;
    flex-direction: column;
  }
  .icon {
    margin-right: 6px;
    color: #75757e;
  }
  .group-title-contain {
    padding: 3px 5px;
    display: flex;
    justify-content: space-between;
    border-bottom: border-style();
    .left {
      display: flex;
      align-items: center;
      flex-shrink: 0;
    }
    .icon {
      font-size: 19px;
    }
    .el-icon {
      font-size: 18px;
      color: #75757e;
      cursor: pointer;
      :hover {
        color: var(--el-color-primary);
      }
    }
  }
  .group-search-contain {
    margin: 5px;
  }
  .group-main-contain {
    flex: 1;
    overflow: auto;
    margin: 5px;
    margin-top: 0;
    border: border-style();
    border-radius: 4px;
    position: relative;
  }
  .windows-list {
    width: fit-content;
    .windows-item {
      white-space: nowrap;
      cursor: pointer;
      margin: 2px 3px;
      padding: 5px;
      border: 1px solid transparent;
      &:hover {
        background: var(--el-bg-color-bar);
      }
      &.active {
        color: var(--el-color-primary);
        border-color: var(--el-color-primary);
        .icon {
          color: var(--el-color-primary);
        }
        &:hover {
          background: none;
        }
      }
    }
  }
</style>
