<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" @tab-click="handleClick">
        <el-tab-pane label="DDL" name="DDL" />
        <el-tab-pane :label="$t('table.general.title')" name="General" />
        <el-tab-pane :label="$t('table.column.title')" name="Column" />
        <el-tab-pane :label="$t('table.data.title')" name="Data" />
      </el-tabs>
      <el-icon class="refresh" @click="handleRefresh"><Refresh /></el-icon>
    </template>
    <template #center-container>
      <KeepAlive>
        <component
          :is="dataMap[currentTabName].component"
          :data="dataMap[currentTabName].data"
          v-model:page="dataMap[currentTabName].page"
          :rowKey="dataMap[currentTabName].rowKey"
          :loading="dataMap[currentTabName].loading"
          @getData="getData(currentTabName)"
        ></component>
      </KeepAlive>
    </template>
  </ThreeSectionTabsPage>
</template>

<script lang="ts" setup>
  import type { TabsPaneContext } from 'element-plus';
  import { useRoute } from 'vue-router';
  import { useUserStore } from '@/store/modules/user';
  import DDL from './components/DDL.vue';
  import General from './components/General.vue';
  import Column from './components/Column.vue';
  import Data from './components/Data.vue';
  import { getViewDdls, getViewDatas, getViewAttribute, getViewColumn } from '@/api/view';
  import { debounce, formatTableDataAndColumns } from '@/utils';

  type TabName = 'DDL' | 'General' | 'Column' | 'Data';
  type MyTabsPaneContext = TabsPaneContext & {
    paneName: TabName;
  };

  const route = useRoute();
  const UserStore = useUserStore();
  const commonParams = reactive({
    connectionName: '',
    schema: '',
    viewName: '',
    webUser: UserStore.userId,
    uuid: '',
  });
  const dataMap = reactive({
    DDL: {
      data: '',
      page: undefined,
      rowKey: '',
      component: markRaw(DDL),
      hasLoad: false,
      loading: false,
    },
    General: {
      data: [],
      page: undefined,
      rowKey: '',
      component: markRaw(General),
      hasLoad: false,
      loading: false,
    },
    Column: {
      data: [],
      page: undefined,
      rowKey: '',
      component: markRaw(Column),
      hasLoad: false,
      loading: false,
    },
    Data: {
      data: {
        columns: [],
        data: [],
      },
      page: {
        pageNum: 1,
        pageSize: 100,
        pageTotal: 0,
      },
      rowKey: '',
      component: markRaw(Data),
      hasLoad: false,
      loading: false,
    },
  });

  const currentTabName = ref<TabName>('DDL');

  const handleClick = (tab: MyTabsPaneContext) => {
    if (dataMap[tab.paneName].hasLoad) return;
    getData(tab.paneName);
  };

  const getData = async (type: TabName) => {
    const api = {
      DDL: getViewDdls,
      General: getViewAttribute,
      Column: getViewColumn,
      Data: getViewDatas,
    };
    if (!Object.keys(api).includes(type)) return;
    dataMap[type].loading = true;
    const params = { ...commonParams };
    if (type == 'Data') {
      Object.assign(params, {
        pageNum: dataMap.Data.page.pageNum,
        pageSize: dataMap.Data.page.pageSize,
      });
    }
    api[type](params)
      .then((res: any) => {
        if (type == 'DDL') {
          dataMap.DDL.data = res;
        } else if (type == 'General') {
          dataMap.General.data = res.map((itemObj) => {
            const attr = Object.keys(itemObj)[0];
            return {
              attr,
              value: itemObj[attr],
            };
          });
        } else if (type == 'Column') {
          const { column, result } = res;
          const rowKey = `_id`;
          dataMap.Column.rowKey = rowKey;
          dataMap.Column.data = formatTableDataAndColumns(column, result, {
            indexName: rowKey,
          }).data;
        } else {
          const { column, result } = res.data as unknown as { column: string[]; result: any[] };
          const rowKey = `${column[0]}_id`;
          dataMap.Data.rowKey = rowKey;
          dataMap.Data.data = shallowReadonly(
            formatTableDataAndColumns(column, result, { indexName: rowKey }),
          );
          Object.assign(dataMap.Data.page, {
            pageNum: res.pageNum || 0,
            pageSize: res.pageSize || 0,
            pageTotal: res.pageTotal || 0,
          });
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
    const { connectInfoName, viewName, schema, uuid } = route.query;
    Object.assign(commonParams, {
      connectionName: connectInfoName,
      viewName,
      schema,
      uuid,
    });
    getData(currentTabName.value);
  });
</script>
<style lang="scss" scoped>
  .refresh {
    position: absolute;
    right: 18px;
    top: 10px;
    cursor: pointer;
    &:hover {
      color: var(--hover-color);
    }
  }
</style>
