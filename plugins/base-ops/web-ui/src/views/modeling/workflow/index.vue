
<template>
  <div class="is-waiting" id="workflow">
    <div class="dd-icon">
      <svg-icon icon-class="modeling-waiting"></svg-icon>
      <div class="text">{{ $t('modeling.dy_common.page_empty') }}</div>
    </div>
  </div>
</template>
<script setup lang="ts">
import {} from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'
</script>
<style scoped lang="less">
  .is-waiting {
    width: 100%;
    height: 100%;
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    .dd-icon {
      width: 300px;
      .text {
        font-size: 20px;
        font-weight: bold;
        color: #c9cdd4;
        text-align: center;
      }
    }
    .arco-icon {
      width: 300px;
      height: 300px;
    }
  }
</style>
