<template>
  <div class="table-page">
    <el-table :data="gridData" stripe v-loading="loading">
      <el-table-column property="attr" :label="$t('synonym.attribute.serialNo')" />
      <el-table-column property="value" :label="$t('synonym.attribute.attr')" />
    </el-table>
  </div>
</template>

<script lang="ts" setup>
  import { useRoute } from 'vue-router';
  import { useUserStore } from '@/store/modules/user';
  import { getSynonyms } from '@/api/synonym';

  const route = useRoute();
  const UserStore = useUserStore();

  const gridData = ref([]);
  const commonParams = reactive({
    connectionName: '',
    synonymName: '',
    webUser: UserStore.userId,
    uuid: '',
  });
  const loading = ref(false);

  const getData = async () => {
    loading.value = true;
    getSynonyms(commonParams)
      .then((res: any) => {
        const { column, result } = res;
        gridData.value = column.map((row, rowIndex) => {
          return {
            attr: row,
            value: result[0]?.[rowIndex],
          };
        });
      })
      .finally(() => {
        loading.value = false;
      });
  };
  onMounted(() => {
    const { connectInfoName, synonymName, uuid } = route.query;
    Object.assign(commonParams, {
      connectionName: connectInfoName,
      synonymName,
      uuid,
    });
    getData();
  });
</script>

<style scoped lang="scss">
  .table-page {
    height: 100%;
    padding: 10px 20px;
    position: relative;
  }
</style>
