<template>
  <div class="cm-desc-item-content">
    <div class="label">
      {{ props.label }}
    </div>
    <div class="main">
      <slot></slot>
    </div>
  </div>
</template>
<script setup>
const props = defineProps(["label"]);
</script>
<style lang="less" scoped>
.cm-desc-item-content {
  font-size: 14px;
  padding-bottom: 16px;
  width: 50%;
  vertical-align: top;
  .label {
    display: inline-block;
    width: 25%;
    vertical-align: top;
  }

  .main {
    display: inline-block;
    width: 75%;
    word-break: break-all;
    padding-right: 8px;
  }
}
</style>
