<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('trigger.create.tabs[0]')" name="GeneralTab" />
        <el-tab-pane
          :label="props.type == 'create' ? $t('trigger.create.tabs[1]') : 'DDL'"
          name="DDL"
        />
      </el-tabs>
    </template>
    <template #center-container>
      <GeneralTab
        ref="generalRef"
        v-show="currentTabName == 'GeneralTab'"
        :userName="commonParams.userName"
        :uuid="commonParams.uuid"
        :schema="commonParams.schema"
        :type="props.type"
        v-model:data="dataMap.GeneralTab.data"
      />
      <DDL v-show="currentTabName == 'DDL'" :data="dataMap.DDL.data" />
    </template>
    <template #page-bottom-button>
      <el-button v-if="props.type == 'create'" type="primary" @click="handleSave">
        {{ $t('button.create') }}
      </el-button>
      <el-button v-if="props.type == 'create'" @click="handleReset">
        {{ $t('button.reset') }}
      </el-button>
      <!-- <el-button v-if="props.type != 'create'" type="primary" @click="handleModify">
        {{ $t('button.saveChanges') }}
      </el-button> -->
      <el-button v-if="props.type != 'create'" @click="handleCancel">
        {{ $t('button.cancel') }}
      </el-button>
    </template>
  </ThreeSectionTabsPage>
</template>

<script lang="ts" setup>
  import { ElMessage } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import { useRoute, useRouter } from 'vue-router';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import GeneralTab from './GeneralTab.vue';
  import DDL from './DDL.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import {
    getTriggerDdlPreviewApi,
    createTriggerApi,
    editTriggerApi,
    queryTriggerApi,
    showDdlTriggerApi,
  } from '@/api/trigger';

  const props = withDefaults(
    defineProps<{
      type: 'create' | 'edit';
    }>(),
    {
      type: 'create',
    },
  );

  const mapping = {
    r: 'table',
    v: 'view',
    m: 'view',
  };

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('GeneralTab');
  const commonParams = reactive({
    userName: route.query.userName as string,
    uuid: route.query.uuid as string,
    schema: route.query.schema as string,
    oid: route.query.oid as string,
    name: route.query.name,
    tableName: route.query.tableName,
  });
  const refreshParams = reactive({
    rootId: route.query.rootId as string,
    databaseId: route.query.databaseId as string,
    schemaId: route.query.schemaId as string,
  });
  const generalRef = ref<InstanceType<typeof GeneralTab>>(null);
  const dataMap = reactive({
    GeneralTab: {
      originData: {} as any,
      data: {
        name: '',
        status: false,
        type: 'table' as 'table' | 'view',
        tableName: '',
        time: 'BEFORE',
        frequency: 'STATEMENT',
        function: '',
        event: [],
        condition: '',
        columnList: [],
        description: '',
      },
    },
    DDL: {
      data: '',
    },
  });
  const handleTabClick = (tab: TabsPaneContext) => {
    if (props.type == 'create' && tab.paneName == 'DDL') {
      getCreateDdl();
    }
  };

  const getCreateParams = async () => {
    try {
      await generalRef.value.validateForm();
      const { data } = dataMap.GeneralTab;
      return {
        uuid: commonParams.uuid,
        schema: commonParams.schema,
        name: data.name,
        status: data.status,
        type: data.type,
        tableName: data.tableName,
        frequency: data.frequency,
        time: data.time,
        event: data.event,
        columnList: data.columnList,
        function: data.function,
        condition: data.condition,
        description: data.description,
      };
    } catch {
      currentTabName.value = 'GeneralTab';
      return Promise.reject();
    }
  };

  const getEditParams = async () => {
    try {
      await generalRef.value.validateForm();
      const { data, originData } = dataMap.GeneralTab;
      return {
        uuid: commonParams.uuid,
        schema: commonParams.schema,
        name: data.name,
        status: data.status,
        type: data.type,
        tableName: data.tableName,
        newTableName: originData.tableName,
        frequency: data.frequency,
        time: data.time,
        event: data.event,
        columnList: data.columnList,
        function: data.function,
        condition: data.condition,
        description: data.description,
      };
    } catch {
      currentTabName.value = 'GeneralTab';
      return Promise.reject();
    }
  };
  const getCreateDdl = async () => {
    const params = await getCreateParams();
    const sql = await getTriggerDdlPreviewApi(params);
    dataMap.DDL.data = sql as unknown as string;
  };

  const handleSave = async () => {
    const params = await getCreateParams();
    loading.value = loadingInstance();
    try {
      await createTriggerApi(params);
      ElMessage.success(t('message.createSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'schema', refreshParams);
      handleCancel();
    } catch {
      return Promise.reject();
    } finally {
      loading.value.close();
    }
  };

  const handleModify = async () => {
    const params = await getEditParams();
    try {
      await editTriggerApi(params);
      ElMessage.success(t('message.editSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'schema', refreshParams);
      handleCancel();
    } catch {
      currentTabName.value = 'GeneralTab';
      return Promise.reject();
    }
  };

  const handleReset = () => {
    generalRef.value.resetFields();
    dataMap.DDL.data = '';
    currentTabName.value = 'GeneralTab';
  };

  const handleCancel = () => {
    TagsViewStore.closeCurrentTabToLatest(router, route);
  };

  const getEditInfo = async () => {
    const res = await queryTriggerApi({
      uuid: commonParams.uuid,
      name: commonParams.name,
      tableName: commonParams.tableName,
    });
    const { result } = res;
    Object.assign(dataMap.GeneralTab.data, {
      name: result.name,
      status: result.status,
      type: mapping[result.type],
      tableName: result.tableName,
      time: result.time,
      frequency: result.frequency,
      function: result.function,
      event: result.event,
      columnList: result.columnList || [],
      condition: result.condition,
      description: result.description,
    });
    dataMap.GeneralTab.originData = JSON.parse(JSON.stringify(dataMap.GeneralTab.data));
    const res1 = await showDdlTriggerApi({
      uuid: commonParams.uuid,
      oid: commonParams.oid,
    });
    dataMap.DDL.data = res1.result;
  };

  onMounted(async () => {
    if (props.type === 'edit') {
      await getEditInfo();
    }
    await generalRef.value.fetchTableViewList();
    await generalRef.value.fetchUpdateFieldList();
    generalRef.value.setCurrentTableSelect();
  });
</script>
