<template>
  <div class="operation-icon-wrapper" :title="t('terminal.SQLAssistant')">
    <svg class="operation-icon" aria-hidden="true" @click="toggleSqlAssistant('opengauss')">
      <use xlink:href="#icon-icon_SQL"></use>
    </svg>
  </div>
</template>

<script lang="ts" setup>
  import { useAppStore } from '@/store/modules/app';
  import { useI18n } from 'vue-i18n';

  const AppStore = useAppStore();
  const { t } = useI18n();

  const toggleSqlAssistant = (type: 'opengauss') => {
    // 1. If it is opened, the same 'type' will be closed, and different 'type' will be switched
    // 2. If it's closed, then open it
    if (AppStore.SqlAssistantType == type || !AppStore.isOpenSqlAssistant) {
      AppStore.isOpenSqlAssistant = !AppStore.isOpenSqlAssistant;
    }
    AppStore.SqlAssistantType = type;
  };
</script>
<style lang="scss" scoped>
  .operation-icon-wrapper {
    width: 23px;
    height: 23px;
    margin: 0 2px;
  }
  .operation-icon {
    width: 100%;
    height: 100%;
    cursor: pointer;
  }
  :deep(.el-only-child__content) {
    &:focus-visible {
      outline: none;
    }
  }
</style>
