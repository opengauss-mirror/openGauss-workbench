<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs">
        <el-tab-pane :label="$t('foreignTable.foreign.title')" name="ForeignTab" />
        <el-tab-pane :label="$t('foreignTable.columns.title')" name="ColumnsTab" />
      </el-tabs>
      <span class="tabs-right-funciton" :title="t('foreignTable.warning')">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-Warning"></use>
        </svg>
      </span>
    </template>
    <template #tabs-container>
      <ForeignCreateTab
        ref="foreignRef"
        v-show="currentTabName == 'ForeignTab'"
        :role="commonParams.role"
        :uuid="commonParams.uuid"
        :databaseName="commonParams.databaseName"
        :schema="commonParams.schema"
        v-model:data="dataMap.ForeignCreateTab.data"
      />
      <ColumnsTab
        v-show="currentTabName == 'ColumnsTab'"
        v-model:data="dataMap.ColumnsTab.data"
        :uuid="commonParams.uuid"
      />
    </template>
    <template #page-bottom-button>
      <el-button type="primary" @click="handleSave">
        {{ $t('button.create') }}
      </el-button>
      <el-button @click="handleReset">
        {{ $t('button.reset') }}
      </el-button>
    </template>
  </ThreeSectionTabsPage>
</template>

<script lang="ts" setup>
  import { ElMessage } from 'element-plus';
  import { useRoute, useRouter } from 'vue-router';
  import { useAppStore } from '@/store/modules/app';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import ForeignCreateTab from './components/ForeignCreateTab.vue';
  import ColumnsTab from './components/ColumnsTab.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { eventQueue } from '@/hooks/saveData';
  import { createForeignTableApi } from '@/api/foreignTable';

  const route = useRoute();
  const router = useRouter();
  const AppStore = useAppStore();
  const TagsViewStore = useTagsViewStore();
  const tagId = TagsViewStore.getViewByRoute(route)?.id;
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('ForeignTab');
  const commonParams = reactive({
    role: '',
    rootId: route.query.rootId as string,
    uuid: route.query.uuid as string,
    databaseName: route.query.databaseName as string,
    schema: route.query.schema as string,
  });
  const refreshParams = reactive({
    rootId: route.query.rootId as string,
    databaseId: route.query.databaseId as string,
    schemaId: route.query.schemaId as string,
  });
  const foreignRef = ref<InstanceType<typeof ForeignCreateTab>>(null);
  const dataMap = reactive({
    ForeignCreateTab: {
      data: {
        foreignTable: '',
        exists: false,
        datasourceType: 'openGauss',
        foreignServer: '',
        foreignDatabase: '',
        schema: '',
        remoteSchema: '',
        remoteDatabase: '',
        remoteTable: '',
        remoteFilePath: '',
        fileType: 'TEXT',
        quote: '',
        escape: '',
        replaceNull: '',
        encoding: 'UTF-8',
        includeHeader: false,
        delimiter: '',
        delimiterOther: '',
        unmatchedEmptyString: false,
        description: '',
      },
    },
    ColumnsTab: {
      data: [],
    },
  });
  const initForm = () => {
    Object.assign(dataMap.ForeignCreateTab.data, {
      foreignDatabase: commonParams.databaseName,
      schema: commonParams.schema,
    });
    commonParams.role = AppStore.getConnectInfoByRootId(commonParams.rootId)?.userName;
  };

  const handleSave = async () => {
    try {
      await foreignRef.value.validateForm();
    } catch {
      currentTabName.value = 'ForeignTab';
      return Promise.reject();
    }
    const actualColumns = dataMap.ColumnsTab.data.filter((item) => item.columnName?.trim());
    if (actualColumns.length == 0) {
      currentTabName.value = 'ColumnsTab';
      ElMessage.error(t('message.leastOneColumn'));
      return Promise.reject();
    }
    const { data } = dataMap.ForeignCreateTab;
    const params = {
      uuid: commonParams.uuid,
      foreignTable: data.foreignTable,
      exists: data.exists,
      datasourceType: data.datasourceType,
      foreignServer: data.foreignServer,
      schema: data.schema,
      description: data.description,
      columnList: dataMap.ColumnsTab.data
        .filter((item) => item.columnName)
        .map((item) => {
          return {
            operationType: 1,
            foreignColumn: item.columnName,
            type: item.dataType,
            isEmpty: item.canBeNull,
            defaultValue: item.defaultValue,
            isOnly: item.isUnique,
            precision: item.precisionSize,
            scope: item.range,
            comment: item.description,
          };
        }),
    };
    if (data.datasourceType == 'openGauss' || data.datasourceType == 'Oracle') {
      Object.assign(params, {
        remoteSchema: data.remoteSchema,
        remoteTable: data.remoteTable,
      });
    }
    if (data.datasourceType == 'MySQL') {
      Object.assign(params, {
        remoteDatabase: data.remoteDatabase,
        remoteTable: data.remoteTable,
      });
    }
    if (data.datasourceType == 'File') {
      const delimiterMap = {
        comma: ',',
        tab: '    ',
        pipe: '|',
        semicolon: ';',
      };
      Object.assign(params, {
        remoteFilePath: data.remoteFilePath,
        fileType: data.fileType,
        quote: data.quote,
        escape: data.escape,
        replaceNull: data.replaceNull,
        encoding: data.encoding,
        includeHeader: data.includeHeader,
        delimiter: data.delimiter == 'other' ? data.delimiterOther : delimiterMap[data.delimiter],
        unmatchedEmptyString: data.unmatchedEmptyString,
      });
    }
    try {
      loading.value = loadingInstance();
      await createForeignTableApi(params);
      ElMessage.success(t('message.createSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'schema', refreshParams);
      handleCancel();
    } catch {
      return Promise.reject();
    } finally {
      loading.value.close();
    }
  };

  const handleReset = () => {
    foreignRef.value.resetFields();
    currentTabName.value = 'ForeignTab';
    initForm();
  };

  const handleCancel = () => {
    TagsViewStore.closeCurrentTabToLatest(router, route);
  };

  onMounted(() => {
    initForm();
    eventQueue[tagId] = () => handleSave();
  });
</script>
<style lang="scss" scoped>
  .tabs-right-funciton {
    width: 24px;
    height: 24px;
    position: absolute;
    right: 5px;
    top: 5px;
    :deep(.icon) {
      width: 100%;
      height: 100%;
    }
  }
</style>
