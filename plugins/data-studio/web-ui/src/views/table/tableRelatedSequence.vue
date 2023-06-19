<template>
  <div class="table-page">
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column v-for="item in columns" :key="item" :property="item" :label="item" />
    </el-table>
  </div>
</template>

<script lang="ts" setup>
  import { useRoute } from 'vue-router';
  import { tableRelatedSequence } from '@/api/table';
  import { formatTableData } from '@/utils';

  const route = useRoute();
  const commonParams = reactive({
    uuid: '',
    schema: '',
    oid: '',
    tableName: '',
  });
  const loading = ref(false);
  const columns = ref([]);
  const tableData = ref([]);

  const getData = async () => {
    loading.value = true;
    tableRelatedSequence(commonParams)
      .then((res) => {
        columns.value = res.column || [];
        tableData.value = formatTableData(res.column, res.result);
      })
      .finally(() => {
        loading.value = false;
      });
  };

  onMounted(() => {
    const { uuid, schema, oid, tableName } = route.query;
    Object.assign(commonParams, {
      uuid,
      schema,
      oid,
      tableName,
    });
    getData();
  });
</script>

<style lang="scss" scoped>
  .table-page {
    height: 100%;
    padding: 10px 20px;
    position: relative;
  }
</style>
