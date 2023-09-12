<template>
  <div class="table-page">
    <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
      <el-tab-pane :label="$t('userRole.create.tabs[0]')" name="GeneralTab" />
      <el-tab-pane :label="$t('userRole.create.tabs[1]')" name="MemberTab" />
      <el-tab-pane :label="$t('userRole.create.tabs[2]')" name="DDL" />
    </el-tabs>
    <div class="tabs-container">
      <GeneralTab
        ref="generalRef"
        v-show="currentTabName == 'GeneralTab'"
        :uuid="commonParams.uuid"
        :type="isCreate ? 'create' : 'edit'"
        v-model:data="dataMap.GeneralTab.data"
      />
      <MemberTab
        ref="memberRef"
        v-show="currentTabName == 'MemberTab'"
        :uuid="commonParams.uuid"
        :type="isCreate ? 'create' : 'edit'"
        v-model:data="dataMap.MemberTab.data"
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
  import MemberTab from './components/MemberTab.vue';
  import DDL from './components/DDL.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { previewUserRole, createUserRole } from '@/api/userRole';

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const isCreate = TagsViewStore.getViewByRoute(route)?.name == 'createUserRole';
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('GeneralTab');
  const commonParams = reactive({
    uuid: route.query.uuid as string,
  });
  const refreshParams = reactive({
    rootId: '',
    uuid: '',
  });
  const generalRef = ref<InstanceType<typeof GeneralTab>>(null);
  const memberRef = ref<InstanceType<typeof MemberTab>>(null);
  const dataMap = reactive({
    GeneralTab: {
      data: {
        name: '',
        oid: '',
        type: 'user',
        password: '',
        confirmPassword: '',
        beginDate: '',
        endDate: '',
        connectionLimit: -1,
        resourcePool: '',
        power: [],
        comment: '',
      },
    },
    MemberTab: {
      data: {
        role: [],
      },
    },
    DDL: {
      data: '',
    },
  });
  const handleTabClick = (tab: TabsPaneContext) => {
    if (tab.paneName == 'DDL') {
      handlePreviewDdl();
    }
  };

  const getFinallyParams = async () => {
    try {
      await generalRef.value.validateForm();
      return {
        uuid: commonParams.uuid,
        name: dataMap.GeneralTab.data.name,
        type: dataMap.GeneralTab.data.type,
        password: dataMap.GeneralTab.data.password,
        beginDate: dataMap.GeneralTab.data.beginDate,
        endDate: dataMap.GeneralTab.data.endDate,
        connectionLimit: dataMap.GeneralTab.data.connectionLimit,
        resourcePool: dataMap.GeneralTab.data.resourcePool,
        power: dataMap.GeneralTab.data.power,
        comment: dataMap.GeneralTab.data.comment,
        role: dataMap.MemberTab.data.role,
      };
    } catch (tab) {
      currentTabName.value = tab;
      return Promise.reject();
    }
  };
  const handlePreviewDdl = async () => {
    const params = await getFinallyParams();
    const sql = await previewUserRole(params);
    dataMap.DDL.data = sql as unknown as string;
  };

  const handleSave = async () => {
    const params = await getFinallyParams();
    loading.value = loadingInstance();
    try {
      await createUserRole(params);
      ElMessage.success(t('message.createSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'userRoleCollect', refreshParams);
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
    generalRef.value.resetFields();
    memberRef.value.resetFields();
    dataMap.DDL.data = '';
    currentTabName.value = 'GeneralTab';
  };

  onMounted(() => {
    const { rootId, uuid } = route.query;
    Object.assign(refreshParams, {
      rootId,
      uuid,
    });
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
