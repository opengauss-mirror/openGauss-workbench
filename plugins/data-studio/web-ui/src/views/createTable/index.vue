<template>
  <div class="table-page">
    <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('createTable.tabs[0]')" name="GeneralTab" />
      <el-tab-pane :label="$t('createTable.tabs[1]')" name="ColumnTab" />
      <el-tab-pane :label="$t('createTable.tabs[2]')" name="ConstraintTab" />
      <el-tab-pane :label="$t('createTable.tabs[3]')" name="IndexesTab" />
      <el-tab-pane
        :label="$t('createTable.tabs[4]')"
        name="PartitionTab"
        :disabled="!dataMap.GeneralTab.isPartition"
      />
      <el-tab-pane :label="$t('createTable.tabs[5]')" name="DDL" />
    </el-tabs>
    <div class="tabs-container">
      <GeneralTab
        ref="generalref"
        v-show="currentTabName == 'GeneralTab'"
        v-model:isPartition="dataMap.GeneralTab.isPartition"
        :commonParams="commonParams"
      />
      <ColumnTab
        ref="columnRef"
        v-show="currentTabName == 'ColumnTab'"
        :commonParams="commonParams"
        v-model:data="dataMap.ColumnTab.data"
      />
      <ConstraintTab
        ref="constraintRef"
        v-show="currentTabName == 'ConstraintTab'"
        v-model:data="dataMap.ConstraintTab.data"
        :commonParams="commonParams"
        :columns="availColumns"
      />
      <IndexesTab
        ref="indexesRef"
        v-show="currentTabName == 'IndexesTab'"
        v-model:data="dataMap.IndexesTab.data"
        :commonParams="commonParams"
        :columns="availColumns"
      />
      <PartitionTab
        ref="partitionRef"
        v-show="currentTabName == 'PartitionTab'"
        :columns="availColumns"
      />
      <DDL v-show="currentTabName == 'DDL'" :data="dataMap.DDL.data" />
    </div>
    <div class="page-button">
      <el-button type="primary" @click="handleSave">{{ $t('button.create') }}</el-button>
      <el-button @click="handleReset">{{ $t('button.reset') }}</el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ElMessage } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import { useRoute, useRouter } from 'vue-router';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import GeneralTab from './components/GeneralTab.vue';
  import PartitionTab from './components/PartitionTab.vue';
  import ColumnTab from './components/ColumnTab.vue';
  import ConstraintTab from './components/ConstraintTab.vue';
  import IndexesTab from './components/IndexesTab.vue';
  import DDL from './components/DDL.vue';
  import { useI18n } from 'vue-i18n';
  import { getTablespaceList } from '@/api/metaData';
  import { getCreateTableDdl, createTable } from '@/api/table';
  import { loadingInstance } from '@/utils';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { eventQueue } from '@/hooks/saveData';

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const tagId = TagsViewStore.getViewByRoute(route)?.id;
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('GeneralTab');
  const commonParams = reactive({
    schema: '',
    uuid: '',
  });
  const refreshParams = reactive({
    rootId: '',
    databaseId: '',
    schemaId: '',
  });
  const tablespaceList = ref<string[]>([]);
  provide('tablespaceList', tablespaceList);
  const generalref = ref<InstanceType<typeof GeneralTab>>(null);
  const columnRef = ref<InstanceType<typeof ColumnTab>>(null);
  const constraintRef = ref<InstanceType<typeof ConstraintTab>>(null);
  const indexesRef = ref<InstanceType<typeof IndexesTab>>(null);
  const partitionRef = ref<InstanceType<typeof PartitionTab>>(null);
  const dataMap = reactive({
    GeneralTab: {
      isPartition: false,
    },
    ColumnTab: {
      data: [],
    },
    ConstraintTab: {
      data: [],
    },
    IndexesTab: {
      data: [],
    },
    PartitionTab: {},
    DDL: {
      data: '',
    },
  });
  const availColumns = computed(() => {
    return dataMap.ColumnTab.data
      .filter((item) => !!item.columnName?.trim())
      .map((item) => {
        return {
          label: item.columnName,
          value: item.columnName,
          dataType: item.dataType,
        };
      });
  });

  const handleTabClick = (tab: TabsPaneContext) => {
    if (tab.paneName == 'DDL') {
      handlePreviewDdl();
    }
  };

  const getFinallyParams = () => {
    const generalValue = generalref.value.formValue;
    const partitionValue = partitionRef.value.formValue;
    const params = {
      ...commonParams,
      tableInfo: {
        tableName: generalValue.tableName,
        exists: generalValue.ifNotExists,
        tableType: generalValue.tableType,
        oids: generalValue.oids,
        tableSpace: generalValue.tableSpace,
        fillingFactor: generalValue.fillingFactor,
        storage: generalValue.storage,
        comment: generalValue.comment,
      },
      column: dataMap.ColumnTab.data
        .filter((item) => item.columnName)
        .map((item) => {
          return {
            operationType: 1,
            newColumnName: item.columnName,
            type: item.dataType,
            empty: item.canBeNull,
            defaultValue: item.defaultValue,
            only: item.isUnique,
            precision: item.precisionSize,
            scope: item.range,
            comment: item.description,
          };
        }),
      constraints: dataMap.ConstraintTab.data.map((item) => {
        return {
          type: 1,
          conname: item.constrainName,
          attname: Array.isArray(item.columnName) ? item.columnName.join(',') : '',
          contype: Array.isArray(item.constrainType) ? item.constrainType[0] : null,
          nspname:
            item.constrainType?.[0] == 'f' && item.constrainType[1] ? item.constrainType[1] : null, //fKey namespace
          tbname:
            item.constrainType?.[0] == 'f' && item.constrainType[2] ? item.constrainType[2] : null, //fKey tablename
          colname:
            item.constrainType?.[0] == 'f' && item.constrainType[3] ? item.constrainType[3] : null, //fKey colname
          constraintdef: item.expression,
          condeferrable: item.isDeffered,
          description: item.description,
        };
      }),
      indexs: dataMap.IndexesTab.data.map((item) => {
        return {
          type: 1,
          indexName: item.indexName,
          unique: item.isUnique,
          amname: item.accessMethod,
          attname: Array.isArray(item.columnName) ? item.columnName.join(',') : '',
          expression: item.expression,
          description: item.description,
        };
      }),
      partitionInfo: dataMap.GeneralTab.isPartition
        ? {
            type: partitionValue.partitionType,
            valueInterval: partitionValue.valueInterval,
            partitionName: partitionValue.partitionName,
            tableSpace: partitionValue.tableSpace,
            partitionColumn: partitionValue.partitionColumn?.join(),
            partitionValue: partitionValue.partitionValue,
          }
        : undefined,
    };
    return params;
  };
  const handlePreviewDdl = async () => {
    const params = getFinallyParams();
    const res = (await getCreateTableDdl(params)) as unknown as string;
    dataMap.DDL.data = res;
  };

  const handleSave = async () => {
    const generalValid = generalref.value.validateForm;
    const partitionValid = partitionRef.value.validateForm;
    try {
      await Promise.all([generalValid(), dataMap.GeneralTab.isPartition ? partitionValid() : true]);
    } catch (error) {
      currentTabName.value = error;
      return Promise.reject();
    }
    const actualColumns = dataMap.ColumnTab.data.filter((item) => item.columnName?.trim());
    if (actualColumns.length == 0) {
      currentTabName.value = 'ColumnTab';
      ElMessage.error(t('message.leastOneColumn'));
      return Promise.reject();
    }
    const params = getFinallyParams();
    loading.value = loadingInstance();
    try {
      await createTable(params);
      ElMessage.success(t('message.createSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'mode', refreshParams);
      TagsViewStore.delCurrentView(route);
      const visitedViews = TagsViewStore.visitedViews;
      router.push(visitedViews.slice(-1)[0].fullPath);
    } catch {
      return Promise.reject();
    } finally {
      loading.value.close();
    }
  };

  const handleReset = () => {
    generalref.value.resetFields();
    partitionRef.value.resetFields();
    dataMap.ColumnTab.data = [];
    dataMap.ConstraintTab.data = [];
    dataMap.IndexesTab.data = [];
    dataMap.DDL.data = '';
  };
  const fetchTablespaceList = async () => {
    const res = (await getTablespaceList(commonParams.uuid)) as unknown as string[];
    tablespaceList.value = res;
  };

  onMounted(() => {
    const { schema, uuid, rootId, databaseId, schemaId } = route.query;
    Object.assign(commonParams, {
      schema,
      uuid,
    });
    Object.assign(refreshParams, {
      rootId,
      databaseId,
      schemaId,
    });
    nextTick(fetchTablespaceList);
    eventQueue[tagId] = () => handleSave();
  });
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
  .tabs-container {
    flex: 1;
    display: flex;
    flex-basis: auto;
    overflow: auto;
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
