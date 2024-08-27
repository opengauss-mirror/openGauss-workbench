<template>
  <ThreeSectionTabsPage :isCenterFlex="false" class="three-section-tabs-page">
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" :before-leave="beforeTabChange">
        <el-tab-pane :label="$t('privilege.objectSelection')" name="tab1" />
        <el-tab-pane :label="$t('privilege.privilegeSelection')" name="tab2" />
      </el-tabs>
    </template>
    <template #center-container>
      <div class="privileage-center-top">
        <Tab1
          ref="tab1Ref"
          v-show="currentTabName == 'tab1'"
          :data="dataMap.Tab1.data"
          v-model:selectedObjectList="dataMap.Tab1.selectedObjectList"
          v-model:selectedUserRoleRows="dataMap.Tab1.selectedUserRoleRows"
          :uuid="uuid"
          :dbname="dbname"
        />
        <Tab2
          ref="tab2Ref"
          v-show="currentTabName == 'tab2'"
          :type="dataMap.Tab1.data.type"
          v-model:grantOrRevoke="dataMap.Tab2.data.grantOrRevoke"
          :uuid="uuid"
          :dbname="dbname"
        />
      </div>
      <HistoryTable ref="historyTableRef" @detail="handleDetail" @backfill="handleBackfill" />
      <ShowSqlDetailDialog
        v-model="visibleSqlDetailDialog"
        :sql="sqlDetail"
        :params="sqlDetailParams"
        @success="excuteSuccess"
      />
    </template>
    <template #page-bottom-button>
      <el-button type="primary" @click="handleSave">
        {{ $t('button.save') }}
      </el-button>
      <el-button @click="handleReset">
        {{ $t('button.reset') }}
      </el-button>
    </template>
  </ThreeSectionTabsPage>
</template>
<script lang="ts" setup>
  import Tab1 from './components/Tab1.vue';
  import Tab2 from './components/Tab2.vue';
  import HistoryTable from './components/HistoryTable.vue';
  import ShowSqlDetailDialog from './components/ShowSqlDetailDialog.vue';
  import { ElMessage } from 'element-plus';
  import { useRoute } from 'vue-router';
  import { useI18n } from 'vue-i18n';
  import { OperateType, GrantOrRevoke, SaveParams } from './types';
  import { getPrivilegeSqlApi } from '@/api/privilege';

  const route = useRoute();
  const { t } = useI18n();
  const uuid = route.query.uuid as string;
  const dbname = route.query.dbname as string;
  const currentTabName = ref('tab1');
  const tab1Ref = ref<InstanceType<typeof Tab1>>(null);
  const tab2Ref = ref<InstanceType<typeof Tab2>>(null);
  const historyTableRef = ref<InstanceType<typeof HistoryTable>>(null);
  const visibleSqlDetailDialog = ref(false);
  const sqlDetail = ref('');
  const sqlDetailParams = ref<any>({});
  const dataMap = reactive({
    Tab1: {
      data: {
        type: 'schema' as OperateType,
        schema: '',
      },
      selectedObjectList: [],
      selectedUserRoleRows: [],
    },
    Tab2: {
      data: {
        grantOrRevoke: 'GRANT' as GrantOrRevoke,
      },
    },
  });

  const beforeTabChange = (activeName) => {
    if (activeName == 'tab2') {
      const selectedObjectList = dataMap.Tab1.selectedObjectList;
      if (selectedObjectList.length == 0) {
        ElMessage.warning(`${t('rules.empty', [t('privilege.selectedObjects')])}`);
        return Promise.reject();
      }
      const userRoleList = dataMap.Tab1.selectedUserRoleRows;
      if (userRoleList.length == 0) {
        ElMessage.warning(`${t('rules.empty', [t('privilege.authorizedUsersRoles')])}`);
        return Promise.reject();
      }
    }
  };

  const handleDetail = (obj) => {
    sqlDetail.value = obj.sqlDetail;
    sqlDetailParams.value = obj.sqlDetailParams;
    visibleSqlDetailDialog.value = true;
  };

  const handleBackfill = (privilegeSetQuery: SaveParams) => {
    currentTabName.value = 'tab1';
    tab1Ref.value.setBackfill(privilegeSetQuery);
    tab2Ref.value.setBackfill(privilegeSetQuery);
  };

  const handleSave = async () => {
    const privilegeList = tab2Ref.value.getPrivilegeList();
    if (privilegeList.length) {
      const params: SaveParams = {
        uuid,
        grantOrRevoke: dataMap.Tab2.data.grantOrRevoke,
        type: dataMap.Tab1.data.type,
        obj: dataMap.Tab1.selectedObjectList.map((item) => {
          return dataMap.Tab1.data.type == 'schema'
            ? { name: item.name }
            : { name: item.name, schema: item.schema };
        }),
        user: dataMap.Tab1.selectedUserRoleRows.map((item) => item.name),
        privilegeOption: privilegeList,
      };
      const res = await getPrivilegeSqlApi(params);
      sqlDetail.value = res as unknown as string;
      sqlDetailParams.value = params;
      visibleSqlDetailDialog.value = true;
    } else {
      ElMessage.warning(`${t('rules.empty', [t('privilege.name')])}`);
    }
  };
  const handleReset = () => {
    currentTabName.value = 'tab1';
    tab1Ref.value.resetFields();
    tab2Ref.value.resetFields();
  };
  const excuteSuccess = () => {
    handleReset();
    historyTableRef.value.getData();
  };

  onMounted(() => {
    historyTableRef.value.getData();
  });
</script>
<style lang="scss" scoped>
  .privileage-center-top {
    height: 70%;
    overflow: hidden;
  }
</style>
