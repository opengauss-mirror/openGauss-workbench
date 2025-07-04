<template>
  <div class="table-con">
    <div class="statistic">
      <div class="success-color">{{ $t('components.BigDataList.5q09o79wvtw0') }}
        <el-tag type="round" size="small">{{ subTaskStore?.subTaskData?.[transFiled[tableType] + 'Counts']?.successCount ||
          0 }}</el-tag>
      </div>
      <div class="danger-color"> {{ $t('components.BigDataList.5q09o79ww2g0') }}
        <el-tag type="round" size="small">{{ subTaskStore?.subTaskData?.[transFiled[tableType] + 'Counts']?.errorCount ||
          0 }}</el-tag>
      </div>
      <div class="primary-color">{{ $t('components.BigDataList.5q09o79wsew0') }}
        <el-tag type="round" size="small">{{ subTaskStore?.subTaskData?.[transFiled[tableType] + 'Counts']?.runningCount ||
          0 }}</el-tag>
      </div>
      <div class="wait-color"> {{ $t('components.BigDataList.5q09nizckiw0') }}
        <el-tag type="round" size="small">{{ subTaskStore?.subTaskData?.[transFiled[tableType] + 'Counts']?.waitCount ||
          0 }}</el-tag>
      </div>
    </div>
    <div class="main-table openDesignTableArea minWid680">
      <!-- This is the search -->
      <div class="mb16">
        <fusion-search :label-options="labelOptions" @click-search="clickSearch" />
      </div>
      <el-table v-loading="tableLoading" :data="tableData" :bordered="false" @filter-change="filterChange">
        <template #empty>
          <div>
            <empty-page></empty-page>
          </div>
        </template>
        <el-table-column :label="$t('components.SubTaskDetail.source_db')" prop="name" ellipsis
          tooltip></el-table-column>
        <el-table-column :label="$t('components.SubTaskDetail.target_db')" prop="name" ellipsis
          tooltip></el-table-column>
        <el-table-column label="" prop="status" :width="300">
          <template #header>
            <el-popover popper-class="popFilterClass" trigger="click">
              <template #default>
                <div class="filterArea">
                  <div class="popperText">{{ $t('components.BigDataList.5q09jzwfppw0') }}</div>
                  <el-switch v-model="onlyError" size="small" @change="filterTableData" />
                </div>
              </template>
              <template #reference>
                <div style="display: flex; align-items: center;">
                  {{ $t('components.BigDataList.5q09jzwfo340') }}
                  <IconFilter class="openDesignIcon" />
                </div>
              </template>
            </el-popover>
          </template>
          <template #default="{ row }">
            <span v-if="row.status === SUB_TASK_STATUS.FULL_START || row.status === SUB_TASK_STATUS.FULL_RUNNING">
              {{ row.percent ? (row.percent * 100).toFixed(2) : '0' }}%
            </span>
            <el-popover v-if="row.status === SUB_TASK_STATUS.FULL_CHECK_FINISH"
              :content="row.errorMsg" position="top">
              <!-- This represents failure. -->
              <template #reference>
                <div class="errorInfo">
                  <IconError class="openDesignIcon" /> <span>{{ row.errorMsg }}</span>
                </div>
              </template>
            </el-popover>
            <IconSuccess v-if="
              row.status === SUB_TASK_STATUS.FULL_FINISH ||
              row.status === SUB_TASK_STATUS.FULL_CHECK_START ||
              row.status === SUB_TASK_STATUS.FULL_CHECKING ||
              row.status === SUB_TASK_STATUS.INCREMENTAL_START" class="openDesignIcon" />
          </template>
        </el-table-column>
      </el-table>
      <el-pagination :total="total" :layout="layout" v-model:page-size="filter.pageSize"
        v-model:current-page="filter.pageNum" @change="searchTable"></el-pagination>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watchEffect, computed, onBeforeUnmount, inject, watch, defineExpose, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { SUB_TASK_STATUS } from '@/utils/constants';
import EmptyPage from '@/components/emptyPage';
import usePagination from '@/utils/usePagination';
import {
  fullMigInfo,
} from '@/api/detail'
import { IconSuccess, IconError, IconFilter } from '@computing/opendesign-icons';
import { useSubTaskStore } from '@/store'
import FusionSearch from "@/components/fusion-search/index.vue";
import { searchType } from "@/types/searchType";
type tabType = 'table' | 'view' | 'function' | 'trigger' | 'procedure'
// Here, because the table interface passes parameters to the function and procedure fields, and the statistics are not func and produce, it is escaped here
const transFiled = {
  table: 'table',
  view: 'view',
  function: 'func',
  trigger: 'trigger',
  procedure: 'produce',
}
const subTaskStore = useSubTaskStore()
const { t } = useI18n();
const { layout } = usePagination();
const subTaskId = inject('subTaskId')
const autoRefresh = inject('autoRefresh');
const tableType = ref<tabType>('table')
const total = ref(0);
const loading = ref(false);
const filter = reactive({
  pageSize: 10,
  pageNum: 1,
  schemaName: '',
  tableName: '',
})

const labelOptions = computed(() => {
  return {
    tableName: {
      label: t('components.SubTaskDetail.databaseName'),
      value: 'tableName',
      placeholder: t('transcribe.index.inputtaskname'),
      selectType: searchType.INPUT
    },
    // The pgSQL adaptation will be shown in the future
    // schemaName: {
    //   label: t('components.SubTaskDetail.schemaName'),
    //   value: 'schemaName',
    //   placeholder: t('transcribe.index.inputtaskname'),
    //   selectType: searchType.INPUT
    // }
  }
})
// Here is the displayed table data.
const tableData = ref([]);
// This is the table data returned by the storage interface, used for filtering.
const totalTable = ref([])
const onlyError = ref(false);
const intervalTimer = ref(null)
const interQueryTable = () => {
  cancelInterval()
  intervalTimer.value = setInterval(() => {
    searchTable('loopQuery');
  }, 6000)
}

const cancelInterval = () => {
  if (intervalTimer.value) {
    clearInterval(intervalTimer.value)
    intervalTimer.value = null
  }
}

watch(() => autoRefresh.value, (newVal, oldValue) => {
  if (autoRefresh.value) {
    interQueryTable();
  } else {
    cancelInterval();
  }
}, { immediate: true })

onBeforeUnmount(() => {
  // Cancel timer
  cancelInterval();
})

const clickSearch = (params) => {
  const { tableName, schemaName } = params
  filter.tableName = tableName || ''
  filter.schemaName = schemaName || ''
  searchTable()
}

const tableLoading = computed(() =>{
  if (subTaskStore.subTaskData?.execStatus && tableLoading.value) {
    searchTable();
  }
  return !subTaskStore.subTaskData?.execStatus;
});

const searchTable = (loopQuery?) => {
  // Need to judge that it's a poll call and that auto-refresh here is turned off
  if (loopQuery === 'loopQuery' && !autoRefresh.value) {
    return;
  }
  // Check whether the subtask data in PINIA exists
  if (!subTaskStore.subTaskData?.execStatus) {
    return;
  }
  // Call the query interface, and later need to add the corresponding parameters.
  try {
    fullMigInfo(subTaskId.value, tableType.value, filter).then((res) => {
      if (tableType.value) {
        tableData.value = res?.data?.[tableType.value] || [];
        totalTable.value = res?.data?.[tableType.value] || [];
        total.value = res.data.total;
      }
    }, (err) => {
      // If an error is reported, the polling will be stopped
      cancelInterval()
    })
  } catch (error) {
    console.error(error)
  }
}

// Get the current tab page table and its corresponding
const getTableType = (type: string) => {
  tableType.value = type;
  // The query should not exceed the websocket of the group page, so the query time needs to be delayed
  let timer = null
  loading.value = true;
  timer = setTimeout(() => {
    searchTable();
    clearTimeout(timer)
    loading.value = false;
    timer = null;
  }, 500)

}

defineExpose({
  getTableType,
})


const filterTableData = () => {
  // Here we judge various types.
  if (onlyError.value) {
    tableData.value = tableData.value.filter(
      (item) => (item.status === SUB_TASK_STATUS.FULL_CHECK_FINISH)
    )
    total.value = tableData.value.length;
  } else {
    tableData.value = totalTable.value;
    total.value = tableData.value.length;
  }
}
</script>

<style lang="less" scoped>
.table-con {
  background-color: var(--o-bg-color-base);
  height: 100%;
  display: flex;
  flex-direction: column;

  .statistic {
    padding: 24px 0px 15px 24px;
    display: flex;
    gap: 16px;
  }

  .main-table {
    :deep(.el-table__empty-block) {
      margin-top: 0;
    }

    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 0 24px 24px;

    .el-table {
      flex: 1;
    }

    .errorInfo {
      display: flex;
      min-height: 16px;
      align-items: center;
    }

    .openDesignIcon {
      width: 16px;
      height: 16px;
    }
  }
}
</style>
<style lang="less">
.popFilterClass {
  width: fit-content !important;

  .filterArea {
    display: flex;
  }
}
</style>
