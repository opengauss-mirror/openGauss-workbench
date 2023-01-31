<template>
  <div class="table-page">
    <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleClick">
      <el-tab-pane label="DDL" name="DDL" />
      <el-tab-pane :label="$t('table.column.title')" name="Column" />
      <el-tab-pane :label="$t('table.constraint.title')" name="Constraint" />
      <el-tab-pane :label="$t('table.indexes.title')" name="Indexes" />
      <el-tab-pane :label="$t('table.data.title')" name="Data" />
    </el-tabs>
    <el-icon class="refresh" @click="handleRefresh"><Refresh /></el-icon>
    <div class="table-container">
      <KeepAlive>
        <component
          :is="dataMap[currentTabName].component"
          :data="dataMap[currentTabName].data"
          :loading="dataMap[currentTabName].loading"
        ></component>
      </KeepAlive>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { markRaw, onMounted, reactive, ref } from 'vue';
  import type { TabsPaneContext } from 'element-plus';
  import { useRoute } from 'vue-router';
  import { useUserStore } from '@/store/modules/user';
  import DDL from './components/DDL.vue';
  import Column from './components/Column.vue';
  import Constraint from './components/Constraint.vue';
  import Indexes from './components/Indexes.vue';
  import Data from './components/Data.vue';
  import {
    getTableDdl,
    getTableColumn,
    getTableConstraint,
    getTableIndex,
    getTableData,
  } from '@/api/table';
  import { debounce, formatTableData, formatTableV2Data } from '@/utils';

  const route = useRoute();
  const UserStore = useUserStore();
  const commonParams = reactive({
    webUser: '',
    connName: '',
    tableName: '',
    schema: '',
  });
  const dataMap = reactive({
    DDL: {
      data: '',
      component: markRaw(DDL),
      hasLoad: false,
      loading: false,
    },
    Column: {
      data: [],
      component: markRaw(Column),
      hasLoad: false,
      loading: false,
    },
    Constraint: {
      data: [],
      component: markRaw(Constraint),
      hasLoad: false,
      loading: false,
    },
    Indexes: {
      data: [],
      component: markRaw(Indexes),
      hasLoad: false,
      loading: false,
    },
    Data: {
      data: [],
      component: markRaw(Data),
      hasLoad: false,
      loading: false,
    },
  });

  const currentTabName = ref('DDL');

  const handleClick = (tab: TabsPaneContext) => {
    if (dataMap[tab.paneName].hasLoad) return;
    getData(tab.paneName);
  };

  const getData = async (type) => {
    const api = {
      DDL: getTableDdl,
      Column: getTableColumn,
      Constraint: getTableConstraint,
      Indexes: getTableIndex,
      Data: getTableData,
    };
    if (!Object.keys(api).includes(type)) return;
    dataMap[type].loading = true;
    api[type]({
      ...commonParams,
    })
      .then((res) => {
        if (type == 'DDL') {
          dataMap.DDL.data = res.result;
        } else {
          const { column, result } = res;
          dataMap[type].data =
            type == 'Data' ? formatTableV2Data(column, result) : formatTableData(column, result);
        }
      })
      .finally(() => {
        dataMap[type].hasLoad = true;
        dataMap[type].loading = false;
      });
  };

  const doFreshDebounce = debounce(
    () => {
      getData(currentTabName.value);
    },
    1000,
    true,
  );
  const handleRefresh = () => {
    if (!currentTabName.value) return;
    doFreshDebounce();
  };

  onMounted(() => {
    const { connectInfoName: connName, tableName, schema } = route.query;
    Object.assign(commonParams, {
      webUser: UserStore.userId,
      connName,
      tableName,
      schema,
    });
    getData(currentTabName.value);
  });
</script>
<style lang="scss" scoped>
  .table-page {
    height: 100%;
    padding: 10px 20px;
    position: relative;
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
  .refresh {
    position: absolute;
    right: 18px;
    top: 15px;
    cursor: pointer;
    &:hover {
      color: var(--hover-color);
    }
  }
  .table-container {
    height: calc(100% - 40px);
  }
  :deep(.el-table) {
    height: 100%;
  }
</style>
