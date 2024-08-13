<template>
  <div class="table-page">
    <div class="tabs">
      <slot name="tabs" />
    </div>
    <div :class="['center-container', { 'center-container-flex': props.isCenterFlex }]">
      <slot name="center-container" />
    </div>
    <div class="page-button" v-if="slots['page-bottom-button']">
      <slot name="page-bottom-button" />
    </div>
  </div>
</template>

<script lang="ts" setup>
  const slots = useSlots();
  const props = withDefaults(
    defineProps<{
      isCenterFlex?: boolean;
    }>(),
    {
      isCenterFlex: true,
    },
  );
</script>

<style lang="scss" scoped>
  .table-page {
    height: 100%;
    padding: 10px 20px;
    position: relative;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .tabs {
    position: relative;
    :deep(.el-tabs__content) {
      padding: 0;
      color: #6b778c;
      font-size: 32px;
      font-weight: normal;
    }
  }
  .center-container {
    flex: 1;
    overflow: auto;
  }
  .center-container-flex {
    display: flex;
    flex-basis: auto;
  }
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  .page-button {
    text-align: center;
    margin-top: 10px;
  }
</style>
