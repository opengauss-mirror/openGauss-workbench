<template>
  <div class="history-table">
    <el-table
      :data="tableData"
      border
      tooltip-effect="light"
      :tooltip-options="{ rawContent: true }"
      style="width: 100%; height: calc(100% - 36px)"
      v-loading="loading"
    >
      <el-table-column align="center" :label="t('historyTable.column.no')" width="60" type="index">
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
        prop="success"
        :label="t('historyTable.column.status')"
        width="90"
      >
        <template #default="{ row }">
          <div v-if="row.success" class="single-line-omission">
            <span :class="['status-dot success']"></span>
            <span>{{ $t('message.success') }}</span>
          </div>
          <EllipsisWithElTooltip v-else :toolTipProps="{ placement: 'top' }">
            <span :class="['status-dot fail']"></span>
            <span>{{ $t('message.fail') }},</span>
            {{ row.errMes }}
            <template #content>
              <div class="ellipsis-with-tooltip-content">{{ row.errMes }}</div>
            </template>
          </EllipsisWithElTooltip>
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
            {{ row.sql.slice(0, 100) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column align="center" :label="t('historyTable.column.operation')" width="80">
        <template #default="{ row }">
          <span
            class="iconfont icon-yulan opetation-button"
            @click="handlePreview(row)"
            :title="$t('privilege.sqlPreview')"
          ></span>
          <span
            class="iconfont icon-backfill opetation-button"
            @click="handleBackfill(row)"
            :title="$t('privilege.backfill')"
          ></span>
        </template>
      </el-table-column>
    </el-table>
    <Toolbar v-model:page="page" @getData="(params) => onlyGetData(params)" />
  </div>
</template>

<script lang="ts" setup>
  import { ElMessageBox } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import FilterTableDataHeaderSlot from '@/components/FilterTableDataHeaderSlot.vue';
  import EllipsisWithElTooltip from '@/components/EllipsisWithEltooltip/index.vue';
  import Toolbar from './Toolbar.vue';
  import { watchDebounced } from '@vueuse/core';
  import { getPrivilegehistoryApi } from '@/api/privilege';

  interface SqlTableRow {
    id: number;
    startTime: string;
    sql: string;
    privilegeSetQuery: string;
    success: boolean;
    errMes: string;
  }

  const myEmit = defineEmits<{
    (event: 'detail', obj: any): void;
    (event: 'backfill', privilegeSetQuery: any): void;
  }>();

  const { t } = useI18n();
  const loading = ref(false);
  const tableData = ref<SqlTableRow[]>([]);
  const showSqlFilter = ref(false);
  const sqlFilterInput = ref('');

  const page = reactive({
    pageNum: 1,
    pageSize: 100,
    pageTotal: 1,
    dataSize: 0,
  });

  watchDebounced(
    sqlFilterInput,
    () => {
      onlyGetData();
    },
    { debounce: 1000, maxWait: 10000 },
  );

  const onlyGetData = async (pageParams = {}) => {
    const res = await getPrivilegehistoryApi({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      like: sqlFilterInput.value,
      ...pageParams, // Optional parameters, if so, override
    });
    tableData.value = res.data;
    Object.assign(page, {
      pageNum: res.pageNum,
      pageSize: res.pageSize,
      pageTotal: res.pageTotal,
      dataSize: res.dataSize,
    });
  };

  const getData = async () => {
    hideNamefilter();
    onlyGetData();
  };

  const hideNamefilter = () => {
    showSqlFilter.value = false;
  };

  const handlePreview = (row) => {
    myEmit('detail', {
      sqlDetail: row.sql,
      sqlDetailParams: JSON.parse(row.privilegeSetQuery),
    });
  };
  const handleBackfill = (row: SqlTableRow) => {
    ElMessageBox.confirm(t('message.backfillRecord'), t('common.warning'), {
      type: 'warning',
    })
      .then(() => {
        myEmit('backfill', JSON.parse(row.privilegeSetQuery));
      })
      .catch(() => ({}));
  };

  defineExpose({
    getData,
  });
</script>

<style lang="scss" scoped>
  .history-table {
    font-size: 12px;
    width: 100%;
    height: 30%;
    overflow: hidden;
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
    margin-right: 2px;
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
