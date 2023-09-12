<template>
  <div class="toolbar">
    <div class="toolbar-left">
      <template v-for="(item, key) in commonButton" :key="key">
        <div class="tool-item-wrapper" v-if="item.position == 'left' && item.show">
          <div
            v-if="item.type == 'button'"
            :class="['tool-item', { disabled: !item.enabled }]"
            :title="item.tips"
            @click="item.enabled && item.on()"
          >
            <span
              :class="['iconfont', item.icon]"
              :style="{
                color: item.enabled ? item.color : item.disabledColor,
              }"
            ></span>
            <span class="tool-name">{{ item.name }}</span>
          </div>
          <div v-if="item.type == 'divider'" class="divider"></div>
          <el-input
            v-if="item.type == 'pageNum'"
            :disabled="!item.enabled"
            :model-value="pageNum"
            @input="handlePageNumInput"
            @change="handlePageNumChange"
            size="small"
            style="width: 50px"
            input-style="text-align: center;"
          />
          <el-select
            v-if="item.type == 'pageSize'"
            :model-value="pageSize"
            @change="changePageSize"
            :disabled="!item.enabled"
            size="small"
            style="width: 90px"
          >
            <el-option :label="`100/${t('common.page')}`" :value="100" />
            <el-option :label="`300/${t('common.page')}`" :value="300" />
            <el-option :label="`500/${t('common.page')}`" :value="500" />
          </el-select>
        </div>
      </template>
    </div>
    <div class="toolbar-right">
      <template v-for="(item, key) in commonButton" :key="key">
        <div class="tool-item-wrapper" v-if="item.position == 'right' && item.show">
          <div
            v-if="item.type == 'button'"
            :class="['tool-item', { disabled: !item.enabled }]"
            :title="item.tips"
            @click="item.enabled && item.on()"
          >
            <span
              :class="['iconfont', item.icon]"
              :style="{
                color: item.isActive
                  ? item.activeColor
                  : item.enabled
                  ? item.color
                  : item.disabledColor,
              }"
            ></span>
            <span class="tool-name">{{ item.name }}</span>
          </div>
          <div v-if="item.type == 'divider'" class="divider"></div>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      type: 'common' | 'table';
      status: Record<string, boolean>;
      canEdit?: boolean;
      isGlobalEnable?: boolean;
      pageNum?: number;
      pageSize?: number;
      pageTotal?: number;
      sort?: boolean;
      filter?: boolean;
    }>(),
    {
      isGlobalEnable: true,
      canEdit: true,
    },
  );
  const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'cancel'): void;
    (e: 'addLine'): void;
    (e: 'copyLine'): void;
    (e: 'removeLine'): void;
    (e: 'importData'): void;
    (e: 'exportCurrentPage'): void;
    (e: 'exportAllPage'): void;
    (e: 'export'): void;
    (e: 'firstPage'): void;
    (e: 'lastPage'): void;
    (e: 'previousPage'): void;
    (e: 'nextPage'): void;
    (e: 'page', value: number): void;
    (e: 'update:pageNum', value: number): void;
    (e: 'update:pageSize', value: number): void;
    (e: 'update:pageTotal', value: number): void;
    (e: 'update:sort', value: boolean): void;
    (e: 'update:filter', value: boolean): void;
  }>();

  const commonButton = computed(() => {
    return [
      {
        name: t('button.save'),
        type: 'button',
        position: 'left',
        show: true,
        enabled: props.canEdit && props.status.save,
        on: () => emit('save'),
        icon: 'icon-gou',
        color: '#4fa643',
        disabledColor: '#4fa64340',
      },
      {
        name: t('button.cancel'),
        type: 'button',
        position: 'left',
        show: true,
        enabled: props.canEdit && props.status.cancel,
        on: () => emit('cancel'),
        icon: 'icon-cuo',
        color: '#d81e06',
        disabledColor: '#d81e0640',
      },
      {
        type: 'divider',
        position: 'left',
        show: true,
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['common', 'table'].includes(props.type),
        enabled: props.canEdit && props.status.addLine,
        on: () => emit('addLine'),
        icon: 'icon-jia1',
        color: '#4fa643',
        disabledColor: '#4fa64340',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.canEdit && props.status.copyLine,
        on: () => emit('copyLine'),
        icon: 'icon-copy-add',
        color: '#1afa29',
        disabledColor: '#1afa2940',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['common', 'table'].includes(props.type),
        enabled: props.canEdit && props.status.removeLine,
        on: () => emit('removeLine'),
        icon: 'icon-jian1',
        color: '#d81e06',
        disabledColor: '#d81e0640',
      },
      {
        type: 'divider',
        position: 'left',
        show: true,
      },
      {
        name: '',
        tips: t('table.data.importData'),
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: true,
        on: () => emit('importData'),
        icon: 'icon-import1',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        tips: t('table.data.exportCurrentPage'),
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: true,
        on: () => emit('exportCurrentPage'),
        icon: 'icon-export',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        tips: t('table.data.exportAllPage'),
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: true,
        on: () => emit('exportAllPage'),
        icon: 'icon-daochusuoyou',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        type: 'divider',
        position: 'left',
        show: ['table'].includes(props.type),
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageNum > 1 && props.status.firstPage,
        on: () => emit('firstPage'),
        icon: 'icon-page-first-line',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageNum > 1 && props.status.previousPage,
        on: () => emit('previousPage'),
        icon: 'icon-page-previous',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'pageNum',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.status.pageNum,
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageTotal && props.pageNum < props.pageTotal && props.status.nextPage,
        on: () => emit('nextPage'),
        icon: 'icon-page-next',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'button',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.pageTotal && props.pageNum < props.pageTotal && props.status.lastPage,
        on: () => emit('lastPage'),
        icon: 'icon-page-last-line',
        color: '#409eff',
        disabledColor: '#409eff40',
      },
      {
        name: '',
        type: 'pageSize',
        position: 'left',
        show: ['table'].includes(props.type),
        enabled: props.status.pageSize,
      },
      {
        name: '',
        type: 'button',
        position: 'right',
        show: ['table'].includes(props.type),
        enabled: true,
        on: () => emit('update:sort', !props.sort),
        icon: 'icon-sort',
        color: '#8a8a8a',
        disabledColor: '',
        isActive: props.sort,
        activeColor: 'var(--el-color-primary)',
      },
      {
        name: '',
        type: 'button',
        position: 'right',
        show: ['table'].includes(props.type),
        enabled: true,
        on: () => emit('update:filter', !props.filter),
        icon: 'icon-shaixuan',
        color: '#8a8a8a',
        disabledColor: '',
        isActive: props.filter,
        activeColor: 'var(--el-color-primary)',
      },
    ];
  });

  const handlePageNumInput = (value) => {
    const result = value.replace(/[^0-9]/g, '');
    const page = result == 0 ? props.pageNum : Math.min(props.pageTotal, Number(value));
    emit('update:pageNum', page);
  };
  const handlePageNumChange = (value) => {
    handlePageNumInput(value);
    emit('page', props.pageNum);
  };
  const changePageSize = (value) => {
    emit('update:pageSize', value);
  };
</script>

<style lang="scss" scoped>
  .toolbar {
    display: flex;
    justify-content: space-between;
    flex-shrink: 0;
    flex-wrap: wrap;
    padding: 5px 0;
    background: var(--el-bg-color-bar);
    .toolbar-left,
    .toolbar-right {
      display: flex;
    }
    .tool-item-wrapper {
      display: flex;
      align-items: center;
      padding: 0 5px;
    }
    .tool-item {
      cursor: pointer;
      position: relative;
      user-select: none;
      line-height: 24px;
      .font-icon {
        font-size: 15px;
        margin-right: 4px;
      }
      &:hover {
        .tool-name {
          background: var(--el-fill-color-light);
        }
      }
      &.disabled {
        cursor: not-allowed;
        color: #b5b8bd;
        &:hover {
          .tool-name {
            background: inherit;
          }
        }
      }
    }
  }
  .divider {
    width: 1px;
    height: 19px;
    border-left: 1px solid #d4cfcf;
  }
  :deep(.el-select) {
    .el-input__inner {
      text-align: center;
    }
  }
</style>
