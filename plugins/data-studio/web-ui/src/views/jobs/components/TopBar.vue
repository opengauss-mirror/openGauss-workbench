<template>
  <div class="functions-wrapper">
    <template v-for="(item, key) in displayButton" :key="key">
      <div class="function-item-wrapper" v-if="item.show">
        <div
          :class="['function-item', { disabled: !item.enabled }]"
          @click="item.enabled && item.on()"
        >
          <span
            :class="['iconfont', item.icon]"
            :style="{
              color: item.enabled ? item.color : item.disabledColor,
            }"
          ></span>
          <span class="funcion-name">{{ item.name }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const emit = defineEmits<{
    (e: 'add'): void;
    (e: 'remove'): void;
    (e: 'enable'): void;
    (e: 'disable'): void;
    (e: 'refresh'): void;
  }>();

  const displayButton = computed(() => {
    return {
      add: {
        name: t('button.add'),
        show: true,
        enabled: true,
        on: () => emit('add'),
        icon: 'icon-jia1',
        color: '#4fa643',
        disabledColor: '#4fa64340',
      },
      refresh: {
        name: t('button.refresh'),
        show: true,
        enabled: true,
        on: () => emit('refresh'),
        icon: 'icon-refresh',
        color: '#606266',
        disabledColor: '#606266',
      },
    };
  });
</script>

<style lang="scss" scoped>
  .functions-wrapper {
    flex-shrink: 0;
    padding: 5px 0;
    background: var(--el-bg-color-bar);
    .function-item-wrapper {
      display: inline-block;
      padding: 0 8px;
      &:nth-child(n + 2) {
        border-left: 1px solid #d4cfcf;
      }
    }
    .function-item {
      cursor: pointer;
      position: relative;
      user-select: none;
      .iconfont {
        font-size: 15px;
        margin-right: 4px;
      }
      &:hover {
        .funcion-name {
          background: var(--el-fill-color-light);
        }
      }
      &.disabled {
        cursor: not-allowed;
        color: #b5b8bd;
        &:hover {
          .funcion-name {
            background: inherit;
          }
        }
      }
    }
  }
</style>
