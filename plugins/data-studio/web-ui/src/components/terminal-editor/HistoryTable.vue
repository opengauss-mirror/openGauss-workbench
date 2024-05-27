<template>
  <div class="history-table">
    <el-table
      :data="showList"
      border
      tooltip-effect="light"
      :tooltip-options="{ rawContent: true }"
      style="width: 100%"
      v-loading="loading"
      class="history-table"
    >
      <el-table-column align="center" :label="t('historyTable.column.no')" width="75">
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
        align="center"
        prop="success"
        :label="t('historyTable.column.status')"
        width="75"
      >
        <template #default="{ row }">
          <div>
            <span :class="['status-dot', row.success ? 'success' : 'fail']"></span>
            {{ row.success == '1' ? $t('message.success') : $t('message.fail') }}
          </div>
        </template>
      </el-table-column>
      <el-table-column align="center" :label="t('historyTable.column.sql')">
        <template #header>
          <div v-if="showSqlFilter" class="flex-header-start">
            <div style="word-break: keep-all; margin-right: 5px">
              {{ $t('historyTable.column.sql') }}
            </div>
            <div class="filter-wrapper">
              <el-icon @click="hideNamefilter" class="icon-pointer">
                <Search />
              </el-icon>
              <el-input class="border-bottom-input" v-model="sqlFilterInput" clearable />
            </div>
          </div>
          <div v-else class="flex-header-between">
            <div style="width: 12px"></div>
            <span>{{ $t('historyTable.column.sql') }}</span>
            <el-icon @click="showSqlFilter = true" class="icon-pointer">
              <Search />
            </el-icon>
          </div>
        </template>
        <template #default="{ row }">
          <div class="single-line-omission">
            {{ row.sql.slice(0, 200) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        prop="errMes"
        :label="t('historyTable.column.errorMessage')"
        show-overflow-tooltip
        width="100"
      ></el-table-column>
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
      <el-table-column align="center" :label="t('historyTable.column.operation')" width="100">
        <template #default="{ row }">
          <span class="iconfont icon-yulan opetation-button" @click="handlePreview(row)"></span>
          <span
            v-if="!row.lock"
            class="iconfont icon-lock opetation-button"
            @click="handleLock(row, true)"
          ></span>
          <span
            v-if="row.lock"
            class="iconfont icon-unlock opetation-button"
            @click="handleLock(row, false)"
          ></span>
          <span
            v-if="!row.lock"
            class="iconfont icon-shanchu opetation-button"
            @click="handleDelelte(row)"
          ></span>
        </template>
      </el-table-column>
    </el-table>
    <ShowHistorySqlDetailDialog v-model="visibleSqlDetailDialog" :sql="sqlDetail" />
  </div>
</template>

<script lang="ts" setup>
  import { ElMessage, ElMessageBox } from 'element-plus';
  import { useUserStore } from '@/store/modules/user';
  import { useI18n } from 'vue-i18n';
  import ShowHistorySqlDetailDialog from './ShowHistorySqlDetailDialog.vue';
  import { querySqlHistory, updateSqlHistory, deleteSqlHistory } from '@/api/sqlHistory';

  interface SqlTableRow {
    id: number;
    startTime: string;
    sql: string;
    executeTime: string;
    webUser: string;
    success: boolean;
    lock: boolean;
  }

  const UserStore = useUserStore();
  const { t } = useI18n();
  const loading = ref(false);
  const visibleSqlDetailDialog = ref(false);
  const sqlDetail = ref('');
  const tableData = ref<SqlTableRow[]>([]);
  const showSqlFilter = ref(false);
  const sqlFilterInput = ref('');

  const showList = computed(() => {
    return tableData.value.filter((item) => item.sql.indexOf(sqlFilterInput.value) > -1);
  });

  // getHistory
  const getHistory = async () => {
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

  const handlePreview = (row) => {
    sqlDetail.value = row.sql;
    visibleSqlDetailDialog.value = true;
  };
  const handleLock = async (row: SqlTableRow, status: boolean) => {
    await updateSqlHistory({
      id: row.id,
      lock: status,
      webUser: UserStore.userId,
    });
    ElMessage.success(t('message.setSuccess'));
    getHistory();
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
        getHistory();
      })
      .catch(() => ({}));
  };

  defineExpose({
    getHistory,
  });
</script>

<style lang="scss" scoped>
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
  .flex-header-start {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    .filter-wrapper {
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  .flex-header-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .icon-pointer {
    cursor: pointer;
    :hover {
      color: var(--normal-color);
    }
  }
  .border-bottom-input {
    box-shadow: none;
    height: auto;
    :deep(.el-input__wrapper) {
      box-shadow: none;
      .el-input__inner {
        box-shadow: 0 1px 0 0 var(--el-input-border-color);
      }
    }
  }
</style>
