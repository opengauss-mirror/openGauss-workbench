<template>
  <div class="page-wrapper">
    <div class="table-page">
      <div class="table-page-top">
        <TopBar
          @add="handleAdd"
          @remove="handleRemove"
          @enable="handleEnable"
          @disable="handleDisable"
          @refresh="handleFirstPage"
        />
      </div>
      <div class="table-page-content">
        <el-table
          ref="multipleTableRef"
          border
          height="100%"
          :data="tableData"
          tooltip-effect="light"
          @selection-change="handleSelectionChange"
        >
          <el-table-column :label="$t('job.jobId')" align="center" width="90">
            <template #default="{ row }">
              <a class="id-text" @click="handleEdit(row)">
                {{ row.jobId }}
              </a>
            </template>
          </el-table-column>
          <el-table-column
            prop="jobContent"
            :label="$t('job.jobContent')"
            align="center"
            width="150"
            show-overflow-tooltip
          />
          <el-table-column
            prop="databaseName"
            :label="$t('job.databaseName')"
            align="center"
            width="130"
          />
          <el-table-column prop="jobStatus" :label="$t('job.jobStatus')" align="center" width="130">
            <template #default="{ row }">
              {{ statusMap[row.jobStatus] }}
            </template>
          </el-table-column>
          <el-table-column prop="interval" :label="$t('job.interval')" align="center" width="130" />
          <el-table-column
            prop="startDate"
            :label="$t('job.startDate')"
            align="center"
            width="150"
            class-name="word-break"
          />
          <el-table-column
            prop="nextRunDate"
            :label="$t('job.nextRunDate')"
            align="center"
            width="150"
            class-name="word-break"
          />
          <el-table-column
            prop="failureCount"
            :label="$t('job.failureCount')"
            align="center"
            width="110"
          />
          <el-table-column
            prop="failureMsg"
            :label="$t('job.failureMsg')"
            align="center"
            width="160"
            show-overflow-tooltip
          />
          <el-table-column
            prop="lastEndDate"
            :label="$t('job.lastEndDate')"
            align="center"
            width="150"
            class-name="word-break"
          />
          <el-table-column
            prop="lastSucDate"
            :label="$t('job.lastSucDate')"
            align="center"
            width="150"
            class-name="word-break"
          />
          <el-table-column prop="creator" :label="$t('job.creator')" align="center" width="130" />
          <el-table-column prop="executor" :label="$t('job.executor')" align="center" width="130" />
          <el-table-column :label="$t('job.operation')" align="center" fixed="right" width="130">
            <template #default="{ row }">
              <el-button text type="primary" @click="handleRemove(row)">{{
                $t('button.delete')
              }}</el-button>
              <el-button
                text
                type="primary"
                v-if="row.jobStatus == 'd'"
                @click="handleEnable(row)"
                >{{ $t('button.enabled') }}</el-button
              >
              <el-button
                text
                type="primary"
                v-if="row.jobStatus != 'd'"
                @click="handleDisable(row)"
                >{{ $t('button.disabled') }}</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="table-page-bottom">
        <BottomBar
          type="table"
          :status="barStatus"
          v-model:pageNum="page.pageNum"
          v-model:pageSize="page.pageSize"
          v-model:pageTotal="page.pageTotal"
          @firstPage="handleFirstPage"
          @lastPage="handleLastPage"
          @previousPage="handlePreviousPage"
          @nextPage="handleNextPage"
          @changePageNum="handlePage"
          @update:pageSize="changePageSize"
        />
      </div>
    </div>
    <CreateJobDialog
      v-model="visibleCreate"
      :type="dialogType"
      :uuid="uuid"
      :jobId="targetJobId"
      @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
  import TopBar from './components/TopBar.vue';
  import BottomBar from './components/BottomBar.vue';
  import CreateJobDialog from './components/CreateJobDialog.vue';
  import { ElMessage, ElMessageBox, ElTable } from 'element-plus';
  import { useRoute } from 'vue-router';
  import { useI18n } from 'vue-i18n';
  import { useAppStore } from '@/store/modules/app';
  import { queryJobListApi, deleteJobApi, setEnableJobApi, setDisableJobApi } from '@/api/job';

  const route = useRoute();
  const { t } = useI18n();
  const AppStore = useAppStore();
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();
  const multipleSelection = ref([]);
  const visibleCreate = ref(false);
  const rootId = route.params.rootId;
  const uuid = computed(() => AppStore.getConnectionOneAvailableUuid(rootId));
  const targetJobId = ref('');
  const dialogType = ref<'create' | 'edit'>('create');
  const statusMap = computed(() => ({
    r: t('job.statusMap.r'),
    s: t('job.statusMap.s'),
    f: t('job.statusMap.f'),
    d: t('job.statusMap.d'),
  }));
  const tableData = ref([]);
  const barStatus = {
    firstPage: true,
    previousPage: true,
    pageNum: true,
    nextPage: true,
    lastPage: true,
    pageSize: true,
  };

  const page = reactive({
    pageNum: 1,
    pageSize: 100,
    pageTotal: 0,
  });

  const getList = async () => {
    const res = await queryJobListApi({
      uuid: uuid.value,
      pageSize: page.pageSize,
      pageNum: page.pageNum,
    });
    tableData.value = res.data;
    Object.assign(page, {
      pageNum: res.pageNum,
      pageSize: res.pageSize,
      pageTotal: res.pageTotal,
    });
  };

  const handleSelectionChange = (val) => {
    multipleSelection.value = val;
  };

  const handleEdit = (row) => {
    targetJobId.value = row.jobId;
    dialogType.value = 'edit';
    visibleCreate.value = true;
  };

  const handleAdd = () => {
    visibleCreate.value = true;
    dialogType.value = 'create';
  };
  const handleRemove = (row) => {
    ElMessageBox.confirm(t('message.willDelete', { name: row.jobId })).then(async () => {
      await deleteJobApi({
        uuid: uuid.value,
        jobId: row.jobId,
      });
      ElMessage.success(t('message.deleteSuccess'));
      getList();
    });
  };
  const handleEnable = (row) => {
    ElMessageBox.confirm(t('message.willEnable', { name: row.jobId })).then(async () => {
      await setEnableJobApi({
        uuid: uuid.value,
        jobId: row.jobId,
      });
      ElMessage.success(t('message.enabledSuccess'));
      getList();
    });
  };
  const handleDisable = (row) => {
    ElMessageBox.confirm(t('message.willDisable', { name: row.jobId })).then(async () => {
      await setDisableJobApi({
        uuid: uuid.value,
        jobId: row.jobId,
      });
      ElMessage.success(t('message.disabledSuccess'));
      getList();
    });
  };

  const handleFirstPage = () => {
    page.pageNum = 1;
    getList();
  };
  const handlePreviousPage = () => {
    page.pageNum--;
    getList();
  };
  const handleNextPage = () => {
    page.pageNum++;
    getList();
  };
  const handleLastPage = () => {
    page.pageNum = page.pageTotal;
    getList();
  };
  const handlePage = (pageNum) => {
    page.pageNum = Number(pageNum || 1);
    getList();
  };
  const changePageSize = () => {
    page.pageNum = 1;
    getList();
  };

  onMounted(() => {
    getList();
  });
</script>

<style lang="scss" scoped>
  .id-text {
    color: #0000ee;
    text-decoration: underline;
    cursor: pointer;
  }
  :deep(.word-break) {
    .cell {
      word-break: auto-phrase;
    }
  }
  .page-wrapper {
    height: 100%;
  }
  .table-page {
    height: 100%;
    padding: 10px 0;
    position: relative;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .table-page-top {
    position: relative;
    :deep(.el-tabs__content) {
      padding: 0;
      color: #6b778c;
      font-size: 32px;
      font-weight: normal;
    }
  }
  .table-page-content {
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
</style>
