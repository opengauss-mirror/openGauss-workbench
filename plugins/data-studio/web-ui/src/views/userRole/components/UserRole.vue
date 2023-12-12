<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('userRole.create.tabs[0]')" name="GeneralTab" />
        <el-tab-pane :label="$t('userRole.create.tabs[1]')" name="MemberTab" />
        <el-tab-pane
          :label="props.type == 'create' ? $t('userRole.create.tabs[2]') : 'DDL'"
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
      <MemberTab
        ref="memberRef"
        v-show="currentTabName == 'MemberTab'"
        :uuid="commonParams.uuid"
        :type="props.type"
        v-model:data="dataMap.MemberTab.data"
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
  import { useUserStore } from '@/store/modules/user';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import GeneralTab from './GeneralTab.vue';
  import MemberTab from './MemberTab.vue';
  import DDL from './DDL.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { previewUserRole, createUserRole } from '@/api/userRole';
  import { getUserRoleInfo, getUserRoleDdl, updateUserRoleInfo } from '@/api/userRole';

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
  const UserStore = useUserStore();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('GeneralTab');
  const commonParams = reactive({
    uuid: route.query.uuid as string,
    webUser: UserStore.userId,
    userName: route.query.name,
    type: route.query.type,
  });
  const refreshParams = reactive({
    rootId: route.query.rootId as string,
    uuid: route.query.uuid as string,
  });
  const generalRef = ref<InstanceType<typeof GeneralTab>>(null);
  const memberRef = ref<InstanceType<typeof MemberTab>>(null);
  const dataMap = reactive({
    GeneralTab: {
      originData: {} as any,
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
      originData: {} as any,
      data: {
        role: [],
        administrator: [],
        belong: [],
      },
    },
    DDL: {
      data: '',
    },
  });
  const handleTabClick = (tab: TabsPaneContext) => {
    if (props.type == 'create' && tab.paneName == 'DDL') {
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
        administrator: dataMap.MemberTab.data.administrator,
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
      handleCancel();
    } catch {
      return Promise.reject();
    } finally {
      loading.value.close();
    }
  };

  /*
    such as: oldArray: [a,b,c], newArray: [a,b,d],
    so: changeAuth: {c: false, d: true}
  */
  const getChangeArrayMap = (oldArray: string[], newArray: string[]) => {
    const changeArray = {};
    oldArray.forEach((item) => {
      if (!newArray.includes(item)) {
        changeArray[item] = false;
      }
    });
    newArray.forEach((item) => {
      if (!oldArray.includes(item)) {
        changeArray[item] = true;
      }
    });
    return changeArray;
  };

  const handleModify = async () => {
    try {
      await generalRef.value.validateForm();
      const { data, originData } = dataMap.GeneralTab;
      if (!data.beginDate && originData.beginDate) {
        return ElMessage.error(t('rules.empty', [t('common.startDate')]));
      }
      if (!data.endDate && originData.endDate) {
        return ElMessage.error(t('rules.empty', [t('common.endDate')]));
      }
      const params = {
        uuid: commonParams.uuid,
        webUser: commonParams.webUser,
        oldName: dataMap.GeneralTab.originData.name,
        newName:
          dataMap.GeneralTab.originData.name == dataMap.GeneralTab.data.name
            ? undefined
            : dataMap.GeneralTab.data.name,
        type: dataMap.GeneralTab.data.type,
        beginDate: dataMap.GeneralTab.data.beginDate,
        endDate: dataMap.GeneralTab.data.endDate,
        connectionLimit: dataMap.GeneralTab.data.connectionLimit,
        resourcePool: dataMap.GeneralTab.data.resourcePool,
        changePower: getChangeArrayMap(
          dataMap.GeneralTab.originData.power,
          dataMap.GeneralTab.data.power,
        ),
        changeBelong: getChangeArrayMap(
          dataMap.MemberTab.originData.belong,
          dataMap.MemberTab.data.belong,
        ),
        comment: dataMap.GeneralTab.data.comment,
      };
      await updateUserRoleInfo(params);
      ElMessage.success(t('message.editSuccess'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'userRoleCollect', refreshParams);
      handleCancel();
    } catch {
      currentTabName.value = 'GeneralTab';
      return Promise.reject();
    }
  };

  const handleReset = () => {
    generalRef.value.resetFields();
    memberRef.value.resetFields();
    dataMap.DDL.data = '';
    currentTabName.value = 'GeneralTab';
  };

  const handleCancel = () => {
    TagsViewStore.closeCurrentTabToLatest(router, route);
  };

  const getEditInfo = async () => {
    const res = await getUserRoleInfo(commonParams);
    Object.assign(dataMap.GeneralTab.data, {
      name: res.name,
      oid: res.oid,
      type: res.type,
      beginDate: res.beginDate,
      endDate: res.endDate,
      connectionLimit: res.connectionLimit,
      resourcePool: res.resourcePool,
      power: res.power.filter((item) => item),
      comment: res.comment,
    });
    Object.assign(dataMap.MemberTab.data, {
      role: res.role,
      administrator: res.administrator,
      belong: res.belong,
    });
    dataMap.GeneralTab.originData = JSON.parse(JSON.stringify(dataMap.GeneralTab.data));
    dataMap.MemberTab.originData = JSON.parse(JSON.stringify(dataMap.MemberTab.data));
  };

  const getEditDdl = async () => {
    const res = await getUserRoleDdl(commonParams);
    dataMap.DDL.data = res as unknown as string;
  };

  onMounted(async () => {
    if (props.type === 'edit') {
      getEditInfo();
      getEditDdl();
    }
  });
</script>
