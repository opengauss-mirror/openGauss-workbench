<template>
  <div class="toolbar">
    <template v-for="(item, key) in commonButton" :key="key">
      <div class="tool-item-wrapper" v-if="item.show">
        <div
          v-if="item.type == 'button'"
          :class="['tool-item', { disabled: !item.enabled }]"
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
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      status: Record<string, boolean>;
      isGlobalEnable?: boolean;
      pageNum?: number;
      pageSize?: number;
      pageTotal?: number;
    }>(),
    {
      isGlobalEnable: true,
    },
  );
  const emit = defineEmits<{
    (e: 'addLine'): void;
    (e: 'copyLine'): void;
    (e: 'removeLine'): void;
  }>();

  const commonButton = computed(() => {
    return [
      {
        name: '',
        type: 'button',
        show: true,
        enabled: props.status.addLine,
        on: () => emit('addLine'),
        icon: 'icon-jia1',
        color: '#4fa643',
        disabledColor: '#4fa64340',
      },
      {
        name: '',
        type: 'button',
        show: false,
        enabled: props.status.copyLine,
        on: () => emit('copyLine'),
        icon: 'icon-copy-add',
        color: '#1afa29',
        disabledColor: '#1afa2940',
      },
      {
        name: '',
        type: 'button',
        show: true,
        enabled: props.status.removeLine,
        on: () => emit('removeLine'),
        icon: 'icon-jian1',
        color: '#d81e06',
        disabledColor: '#d81e0640',
      },
    ];
  });
</script>

<style lang="scss" scoped>
  .toolbar {
    flex-shrink: 0;
    display: flex;
    flex-wrap: wrap;
    padding: 5px 0;
    background: var(--el-bg-color-bar);
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
