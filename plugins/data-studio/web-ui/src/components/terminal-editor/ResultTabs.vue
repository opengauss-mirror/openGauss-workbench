<template>
  <el-tabs type="card" v-model="tabValue" @tab-remove="removeTab" class="result-tabs">
    <el-tab-pane name="home">
      <template #label>
        <span class="custom-tabs-label" @contextmenu.prevent>
          <el-icon><InfoFilled /></el-icon>
          <span>{{ $t('resultTab.message') }}</span>
        </span>
      </template>
      <div class="my-message">
        <el-scrollbar ref="qaScrollbarRef">
          <div class="message-row" v-for="item in props.msgData" :key="item.id">
            <template v-if="item.label"> {{ item.label }}: </template
            ><span v-html="item.text"></span>
          </div>
        </el-scrollbar>
      </div>
    </el-tab-pane>
    <el-tab-pane v-if="props.type == 'sql'" name="history">
      <template #label>
        <span class="custom-tabs-label" @contextmenu.prevent>
          <span class="iconfont icon-lishijilu"></span>
          <span>{{ $t('resultTab.history') }}</span>
        </span>
      </template>
      <HistoryTable ref="historyTableRef" />
    </el-tab-pane>
    <el-tab-pane v-for="item in props.tabList" :name="item.name" :key="item.id">
      <template #label>
        <el-dropdown
          trigger="contextmenu"
          :id="item.name"
          @visible-change="handleChange($event, item.name)"
          ref="dropdownRef"
        >
          <div class="custom-tabs-label" :class="tabValue === item.name ? 'active-label' : ''">
            <el-icon><Grid /></el-icon>
            <span>{{ item.name }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="result-dropdown">
              <el-dropdown-item @click="removeTab(item.name)">
                {{ $t('resultTab.closeCurrentTab') }}
              </el-dropdown-item>
              <el-dropdown-item
                @click="removeTab(item.name, 'other')"
                v-if="props.tabList.length > 1"
              >
                {{ $t('resultTab.closeOtherTab') }}
              </el-dropdown-item>
              <el-dropdown-item @click="removeTab(item.name, 'all')">
                {{ $t('resultTab.closeAllTab') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <!-- tips:
        vxe-table use Virtual Scrolling:
        'height' must be 'auto'. Otherwise, the performance will be extremely poor and the browser will crash!
        'max-height' is not necessary.
       -->
      <div class="my-el-table-v2">
        <vxe-table
          size="mini"
          border
          :empty-text="$t('common.noData')"
          :show-overflow="isInFrame ? 'title' : 'tooltip'"
          :resizable-config="{ minWidth: 30 }"
          height="auto"
          :min-height="50"
          :row-config="{ isHover: true }"
          :column-config="{ resizable: true }"
          :data="item.data"
          :scroll-x="{ enabled: true }"
          :scroll-y="{ enabled: true }"
          :tooltipConfig="{ theme: 'light' }"
        >
          <vxe-column type="seq" title=" " width="60"></vxe-column>
          <vxe-column
            v-for="col in item.columns"
            :key="col.key"
            :field="col.key"
            :title="col.title"
            :min-width="col.width"
          ></vxe-column>
        </vxe-table>
      </div>
    </el-tab-pane>
  </el-tabs>
</template>
<script lang="ts" setup>
  import HistoryTable from './HistoryTable.vue';
  import { watchThrottled } from '@vueuse/core';

  interface messageType {
    id: number | string;
    label: string;
    text: string;
  }
  interface Props {
    msgData: messageType[];
    tabList: any[];
    tabValue: string;
    type: 'sql' | 'debug';
  }
  const props = withDefaults(defineProps<Props>(), {
    msgData: () => [],
    tabList: () => [],
    tabValue: 'home',
    type: 'sql',
  });
  const emit = defineEmits<{
    (e: 'update:tabList', tabList: any[]): void;
    (e: 'update:tabValue', tabValue: string): void;
  }>();

  const historyTableRef = ref<InstanceType<typeof HistoryTable>>(null);
  const tabValue = computed({
    get: () => props.tabValue,
    set: (val) => emit('update:tabValue', val),
  });
  const isInHistory = computed(() => tabValue.value === 'history');
  const isInFrame = ref(self !== parent);
  const isNeedScrollMessage = ref(false);

  watchThrottled(
    isInHistory,
    (value) => {
      if (value) {
        nextTick(historyTableRef.value.getData);
      }
    },
    { throttle: 3000 },
  );

  watch(
    props.msgData,
    () => {
      nextTick(() => {
        if (props.tabValue == 'home') {
          scrollToMessageBottom();
          isNeedScrollMessage.value = false;
        } else {
          isNeedScrollMessage.value = true;
        }
      });
    },
    {
      deep: true,
    },
  );

  watch(
    () => props.tabValue,
    (newTabValue) => {
      if (newTabValue == 'home') {
        isNeedScrollMessage.value && scrollToMessageBottom();
        isNeedScrollMessage.value = false;
      }
    },
  );

  const removeTab = (targetName: string, type?: string) => {
    const index = props.tabList.findIndex((item) => item.name === targetName);

    switch (type) {
      case 'all':
        emit('update:tabList', []);
        tabValue.value = 'home';
        break;
      case 'other':
        emit('update:tabList', [props.tabList[index]]);
        if (tabValue.value !== 'home' && targetName !== tabValue.value) {
          tabValue.value = targetName;
        }
        break;
      default:
        // eslint-disable-next-line no-case-declarations
        let tabLists: any[] = props.tabList.filter((item) => {
          return item.name !== targetName;
        });
        emit('update:tabList', tabLists);
        if (targetName === tabValue.value) {
          tabValue.value = index === 0 ? 'home' : props.tabList[index - 1].name;
        }
        break;
    }
  };

  const dropdownRef = ref();
  const handleChange = (visible: boolean, name: string) => {
    // When right clicking is triggered on multiple tabs consecutively, multiple menus will appear. The solution is: After triggering the right clicking menu, close other right clicking menus
    if (!visible) return;
    dropdownRef.value.forEach((item: { id: string; handleClose: () => void }) => {
      if (item.id === name) return;
      item.handleClose();
    });
  };

  const qaScrollbarRef = ref(null);
  const scrollToMessageBottom = async () => {
    setTimeout(() => {
      const element = qaScrollbarRef.value.$el;
      qaScrollbarRef.value.scrollTo(0, element.querySelector('.el-scrollbar__view').scrollHeight);
    }, 200);
  };
  defineExpose({
    scrollToMessageBottom,
  });
</script>
<style lang="scss" scoped>
  .result-tabs {
    height: 100%;
    :deep(.el-tabs__content) {
      height: calc(100% - 43px);
    }
    :deep(.el-tabs__header) {
      background-color: var(--el-fill-color-light);
      margin: 0;
      .el-tabs__item.is-active {
        background-color: var(--el-bg-color-overlay);
        border-right-color: var(--el-border-color);
        border-left-color: var(--el-border-color);
        .el-icon {
          color: var(--active-color);
        }
      }
    }
    :deep(.el-tabs__item) {
      padding: 0 !important;
      color: #75757e;
      .el-dropdown {
        line-height: inherit;
      }
    }
    :deep(.el-tabs__nav-next) {
      line-height: 31px;
    }
    :deep(.el-tabs__nav-prev) {
      line-height: 31px;
    }
    :deep(.el-tab-pane) {
      height: 100%;
      padding: 10px;
      padding-left: 0;
      padding-bottom: 0;
    }
    .custom-tabs-label {
      padding: 0 8px;
      .el-icon {
        margin-right: 3px;
        font-size: 1.2em;
        vertical-align: middle;
        margin-top: -2px;
      }
      .custom-tabs-label-text {
        vertical-align: middle;
      }
    }
  }
  .active-label {
    color: var(--normal-color);
  }
  .my-message {
    height: 100%;
    .message-row {
      margin: 3px 0;
    }
  }
  .my-el-table-v2 {
    height: 100%;
  }

  :deep(.vxe-table--render-default.border--full .vxe-table--header-wrapper) {
    background: none;
  }
</style>
