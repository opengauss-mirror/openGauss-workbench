<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('tablespace.create.tabs[0]')" name="GeneralTab" />
        <el-tab-pane
          :label="props.type == 'create' ? $t('tablespace.create.tabs[1]') : 'DDL'"
          name="DDL"
        />
      </el-tabs>
    </template>
    <template #tabs-container>
      <GeneralTab
        ref="generalRef"
        v-show="currentTabName == 'GeneralTab'"
        :uuid="commonParams.uuid"
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
      <el-button v-if="props.type != 'create'" type="primary" @click="handleModify">
        {{ $t('button.saveChanges') }}
      </el-button>
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
    createTablespaceApi,
    getTablespacePreviewDdlApi,
    getTablespaceAttributeApi,
    updateTablespaceApi,
    updateTablespacePreviewDdlApi,
  } from '@/api/tablespace';

  const props = withDefaults(
    defineProps<{
      type: 'create' | 'edit';
    }>(),
    {
      type: 'create',
    },
  );

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('GeneralTab');
  const commonParams = reactive({
    uuid: route.query.uuid as string,
    oid: route.query.oid as string,
    tablespaceName: route.query.name,
  });
  const refreshParams = reactive({
    rootId: route.query.rootId as string,
    uuid: route.query.uuid as string,
  });
  const generalRef = ref<InstanceType<typeof GeneralTab>>(null);
  const dataMap = reactive({
    GeneralTab: {
      originData: {} as any,
      data: {
        tablespaceName: '',
        owner: '',
        sequentialOverhead: '',
        nonSequentialOverhead: '',
        path: '',
        relativePath: false,
        maxStorage: null,
        maxSizeUnit: 'K',
        comment: '',
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
    if (props.type == 'edit' && tab.paneName == 'DDL') {
      getEditDdl();
    }
  };

  const getCreateParams = async () => {
    try {
      await generalRef.value.validateForm();
      const { data } = dataMap.GeneralTab;
      return {
        uuid: commonParams.uuid,
        tablespaceName: data.tablespaceName,
        owner: data.owner,
        sequentialOverhead: data.sequentialOverhead,
        nonSequentialOverhead: data.nonSequentialOverhead,
        path: data.path,
        relativePath: data.relativePath,
        maxStorage: data.maxStorage ? data.maxStorage + data.maxSizeUnit : null,
        comment: data.comment,
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
        newTablespaceName: data.tablespaceName,
        oldTablespaceName: originData.tablespaceName,
        owner: data.owner,
        sequentialOverhead: data.sequentialOverhead,
        nonSequentialOverhead: data.nonSequentialOverhead,
        maxStorage: data.maxStorage ? data.maxStorage + data.maxSizeUnit : null,
        comment: dataMap.GeneralTab.data.comment,
      };
    } catch {
      currentTabName.value = 'GeneralTab';
      return Promise.reject();
    }
  };
  const getCreateDdl = async () => {
    const params = await getCreateParams();
    const sql = await getTablespacePreviewDdlApi(params);
    dataMap.DDL.data = sql as unknown as string;
  };

  const getEditDdl = async () => {
    const { originData } = dataMap.GeneralTab;
    const sql = await getTablespacePreviewDdlApi({
      uuid: commonParams.uuid,
      tablespaceName: originData.tablespaceName,
      owner: originData.owner,
      sequentialOverhead: originData.sequentialOverhead,
      nonSequentialOverhead: originData.nonSequentialOverhead,
      path: originData.path,
      relativePath: originData.relativePath,
      maxStorage: originData.maxStorage ? originData.maxStorage + originData.maxSizeUnit : null,
      comment: originData.comment,
    });
    dataMap.DDL.data = sql as unknown as string;
  };

  const handleSave = async () => {
    const params = await getCreateParams();
    loading.value = loadingInstance();
    try {
      await createTablespaceApi(params);
      ElMessage.success(t('message.createSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'tablespaceCollect', refreshParams);
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
      await updateTablespaceApi(params);
      ElMessage.success(t('message.editSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'tablespaceCollect', refreshParams);
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
    const res = await getTablespaceAttributeApi({
      uuid: commonParams.uuid,
      oid: commonParams.oid,
      tablespaceName: commonParams.tablespaceName,
    });
    const maxStorageArray = res.maxStorage?.split(' ') || [null, 'K'];
    Object.assign(dataMap.GeneralTab.data, {
      tablespaceName: res.tablespaceName,
      owner: res.owner,
      sequentialOverhead: res.sequentialOverhead,
      nonSequentialOverhead: res.nonSequentialOverhead,
      path: res.path,
      relativePath: res.relativePath,
      maxStorage: typeof maxStorageArray[0] == 'string' ? Number(maxStorageArray[0]) : null,
      maxSizeUnit: maxStorageArray[1],
      comment: res.comment,
    });
    dataMap.GeneralTab.originData = JSON.parse(JSON.stringify(dataMap.GeneralTab.data));
  };

  onMounted(async () => {
    if (props.type === 'edit') {
      getEditInfo();
    }
  });
</script>
