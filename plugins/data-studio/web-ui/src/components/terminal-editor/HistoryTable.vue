<template>
  <div class="history-table-wrapper">
    <el-table
      :data="showList"
      border
      tooltip-effect="light"
      :tooltip-options="{ rawContent: true }"
      style="width: 100%"
      v-loading="loading"
      class="history-table"
    >
      <el-table-column align="center" :label="t('historyTable.column.no')" width="60">
        <template #default="{ row }">
          <div>
            <span v-if="row.lock" class="iconfont icon-lock num-icon"></span>
            {{ row.index }}
          </div>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        prop="startTime"
        :label="t('historyTable.column.startTime')"
        width="160"
      ></el-table-column>
      <el-table-column
        header-align="center"
        align="center"
        :label="t('historyTable.column.status')"
        width="75"
      >
        <template #default="{ row }">
          <span :class="['status-dot', row.success ? 'success' : 'fail']"></span>
          {{ row.success ? $t('message.success') : $t('message.fail') }}
        </template>
      </el-table-column>
      <el-table-column align="center" :label="t('historyTable.column.sql')">
        <template #header>
          <FilterTableDataHeaderSlot v-model="sqlFilterInput" v-model:show="showSqlFilter">
            {{ $t('historyTable.column.sql') }}
          </FilterTableDataHeaderSlot>
        </template>
        <template #default="{ row }">
          <div class="single-line-omission">
            {{ row.sql.slice(0, 200) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        prop="updateCount"
        :label="t('historyTable.column.effectLineNumber')"
        width="75"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="executeTime"
        :label="t('historyTable.column.executeTime')"
        width="100"
      ></el-table-column>
      <el-table-column
        header-align="center"
        align="left"
        :label="t('historyTable.column.operation')"
        width="100"
      >
        <template #default="{ row }">
          <span
            class="iconfont icon-yulan opetation-button"
            @click="handlePreviewSql(row)"
            :title="$t('historyTable.dialog.title')"
          ></span>
          <span
            v-if="!row.lock"
            class="iconfont icon-lock opetation-button"
            @click="handleLock(row, true)"
            :title="$t('button.lock')"
          ></span>
          <span
            v-if="row.lock"
            class="iconfont icon-unlock opetation-button"
            @click="handleLock(row, false)"
            :title="$t('button.unlock2')"
          ></span>
          <span
            v-if="!row.lock"
            class="iconfont icon-shanchu opetation-button"
            @click="handleDelelte(row)"
            :title="$t('button.delete')"
          ></span>
          <el-icon
            v-if="!row.success && row.errMes"
            class="opetation-button"
            style="vertical-align: text-bottom; font-size: 17px"
            @click="handlePreviewError(row)"
            :title="$t('historyTable.column.errorMessage')"
            ><Warning
          /></el-icon>
        </template>
      </el-table-column>
    </el-table>
    <ShowHistorySqlDetailDialog v-model="visibleSqlDetailDialog" :sql="sqlDetail" />
    <ErrorMessageDetailDialog v-model="visibleErrorDetailDialog" :sql="errorDetail" />
  </div>
</template>

<script lang="ts" setup>
  import { Tooltip as VxeTooltip } from 'vxe-table';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import { useUserStore } from '@/store/modules/user';
  import { useI18n } from 'vue-i18n';
  import ShowHistorySqlDetailDialog from './ShowHistorySqlDetailDialog.vue';
  import ErrorMessageDetailDialog from './ErrorMessageDetailDialog.vue';
  import FilterTableDataHeaderSlot from '@/components/FilterTableDataHeaderSlot.vue';
  import { querySqlHistory, updateSqlHistory, deleteSqlHistory } from '@/api/sqlHistory';

  interface SqlTableRow {
    id: number;
    startTime: string;
    sql: string;
    executeTime: string;
    webUser: string;
    success: boolean;
    errMes: string;
    lock: boolean;
    updateCount: number;
    index: number;
  }

  const UserStore = useUserStore();
  const { t } = useI18n();
  const loading = ref(false);
  const visibleSqlDetailDialog = ref(false);
  const visibleErrorDetailDialog = ref(false);
  const sqlDetail = ref('');
  const errorDetail = ref('');
  const tableData = ref<SqlTableRow[]>([]);
  const showSqlFilter = ref(false);
  const sqlFilterInput = ref('');

  const showList = computed(() => {
    return tableData.value.filter(
      (item) => item.lock || item.sql.indexOf(sqlFilterInput.value) > -1,
    );
  });

  const getData = async () => {
    loading.value = true;
    hideNamefilter();
    querySqlHistory({
      webUser: UserStore.userId,
    })
      .then((res) => {
        tableData.value = res.map((item, index) => ({
          ...item,
          index: index + 1,
        }));
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const hideNamefilter = () => {
    showSqlFilter.value = false;
    sqlFilterInput.value = '';
  };

  const handlePreviewSql = (row) => {
    sqlDetail.value = row.sql;
    visibleSqlDetailDialog.value = true;
  };
  const handlePreviewError = (row) => {
    errorDetail.value = row.errMes;
    visibleErrorDetailDialog.value = true;
  };
  const handleLock = async (row: SqlTableRow, status: boolean) => {
    await updateSqlHistory({
      id: row.id,
      lock: status,
      webUser: UserStore.userId,
    });
    ElMessage.success(t('message.setSuccess'));
    getData();
  };
  const handleDelelte = (row: SqlTableRow) => {
    ElMessageBox.confirm(t('message.deleteRecord'), t('common.warning'), {
      type: 'warning',
    })
      .then(async () => {
        await deleteSqlHistory({ id: row.id });
        ElMessage({
          type: 'success',
          message: t('message.deleteSuccess'),
        });
        getData();
      })
      .catch(() => ({}));
  };

  defineExpose({
    getData,
  });
</script>

<style lang="scss" scoped>
  .history-table-wrapper {
    height: 100%;
    overflow: hidden;
  }
  .history-table {
    font-size: 12px;
    height: 100%;
    :deep(.el-table__cell) {
      padding: 3px 0;
    }

    :deep(.cell) {
      padding: 0 8px;
    }
  }
  .status-dot {
    display: inline-block;
    width: 9px;
    height: 9px;
    border-radius: 50%;
    &.success {
      background: green;
    }
    &.fail {
      background: red;
    }
  }
  .num-icon {
    margin-right: 5px;
    user-select: none;
    font-size: 16px;
  }
  .opetation-button {
    color: var(--el-color-primary);
    cursor: pointer;
    &:nth-child(n + 2) {
      margin-left: 5px;
    }
  }
</style>
