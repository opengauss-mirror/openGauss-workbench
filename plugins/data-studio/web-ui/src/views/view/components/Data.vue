<template>
  <div class="table-container">
    <AdvancedTable
      :data="props.data.data"
      :loading="props.loading"
      :row-key="props.rowKey"
      :columns="props.data.columns"
    >
      <el-table-column
        v-for="item in props.data.columns"
        :key="item.label"
        :prop="item.label"
        :label="item.label"
        :min-width="item.minWidth"
        align="center"
      />
    </AdvancedTable>
    <Toolbar
      type="table"
      :status="barStatus"
      v-model:pageNum="page.pageNum"
      v-model:pageSize="page.pageSize"
      v-model:pageTotal="page.pageTotal"
      @firstPage="handleFirstPage"
      @lastPage="handleLastPage"
      @previousPage="handlePreviousPage"
      @nextPage="handleNextPage"
      @changePageNum="handlePage"
      @update:pageSize="changePageSize"
    />
  </div>
</template>

<script lang="ts" setup>
  import Toolbar from './Toolbar.vue';

  interface Page {
    pageNum: number;
    pageSize: number;
    pageTotal: number;
  }
  const props = withDefaults(
    defineProps<{
      data: any;
      page: Page;
      rowKey: string;
      loading?: boolean;
    }>(),
    {
      data: () => ({
        columns: [],
        data: [],
      }),
      page: () => ({
        pageNum: 1,
        pageSize: 100,
        pageTotal: 0,
      }),
      rowKey: '',
      loading: false,
    },
  );

  const emit = defineEmits<{
    (e: 'getData'): void;
    (e: 'update:page', value: Page): void;
  }>();

  const page = computed({
    get: () => props.page,
    set: (val) => emit('update:page', val),
  });

  const barStatus = {
    save: true,
    cancel: true,
    addLine: true,
    copyLine: true,
    removeLine: true,
    firstPage: true,
    previousPage: true,
    pageNum: true,
    nextPage: true,
    lastPage: true,
    pageSize: true,
  };

  const handleFirstPage = () => {
    page.value.pageNum = 1;
    emit('getData');
  };
  const handlePreviousPage = () => {
    page.value.pageNum--;
    emit('getData');
  };
  const handleNextPage = () => {
    page.value.pageNum++;
    emit('getData');
  };
  const handleLastPage = () => {
    page.value.pageNum = page.value.pageTotal;
    emit('getData');
  };
  const handlePage = (pageNum) => {
    page.value.pageNum = Number(pageNum || 1);
    emit('getData');
  };
  const changePageSize = () => {
    page.value.pageNum = 1;
    emit('getData');
  };
</script>

<style lang="scss" scoped>
  .table-container {
    width: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
  }
</style>
