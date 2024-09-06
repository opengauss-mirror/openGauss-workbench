<template>
  <div class="select-comp-container cm-pagination-content">
    <div class="total-page">
      {{ `共${Math.ceil(total / pageSize)}页，${total}条` }}
    </div>
    <a-select :model-value="pageSize" style="width: 110px;margin-right: 16px;" @change="pageSizeChange"
      :trigger-props="{ position: 'top', autoFitPosition: false }" popup-container=".select-comp-container">
      <a-option :value="10">每页10条</a-option>
      <a-option :value="20">每页20条</a-option>
      <a-option :value="30">每页30条</a-option>
      <a-option :value="40">每页40条</a-option>
      <a-option :value="50">每页50条</a-option>
    </a-select>
    <a-pagination :current="current" :page-size="pageSize" :total="total" @change="pageChange" show-jumper>
    </a-pagination>
  </div>
</template>
<script setup>
import { computed } from 'vue';

const props = defineProps(['total', 'pageSize', 'current']);
const emits = defineEmits(['change','page-size-change']);

const current = computed(() => props.current || 1);
const pageSize = computed(() => props.pageSize || 10);
const total = computed(() => props.total || 0);

function pageChange(current) {
  emits('change', current);
}
function pageSizeChange(size) {
  emits('page-size-change', size)
}

</script>
<style lang="less" scoped>
.cm-pagination-content {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;

  .total-page {
    display: inline-block;
    height: 100%;
    margin-right: 8px;
    color: #547781;
    font-size: 14px;
    line-height: 32px;
    white-space: nowrap;
    margin-right: 20px;
  }
}
</style>