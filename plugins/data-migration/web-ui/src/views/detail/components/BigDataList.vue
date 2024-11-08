<template>
  <div class="table-con">
    <div class="table-header-con">
      <a-table :bordered="false" :pagination="false">
        <template #columns>
          <a-table-column
            :title="
              $t('components.BigDataList.5q09l2jkfp80', {
                sourceDb: props.subTaskInfo.sourceDb
              })
            "
            data-index="name"
            :width="250"
          ></a-table-column>
          <a-table-column
            :title="
              $t('components.BigDataList.5q09m4g78no0', {
                targetDb: props.subTaskInfo.targetDb
              })
            "
            data-index="name"
            :width="250"
          ></a-table-column>
          <a-table-column data-index="status" align="center">
            <template #title>
              <span>{{ $t('components.BigDataList.5q09jzwfo340') }}</span>
              <a-popover content-class="pop-con">
                <span style="margin-left: 5px; cursor: pointer"
                  ><icon-filter
                /></span>
                <template #content>
                  <div class="filter-con">
                    <span>{{ $t('components.BigDataList.5q09jzwfppw0') }}</span>
                    <a-switch
                      v-model="onlyError"
                      size="small"
                      @change="filterTableData(1)"
                    />
                  </div>
                </template>
              </a-popover>
            </template>
          </a-table-column>
          <a-table-column data-index="status" align="center">
            <template #title>
              <span>{{ $t('components.BigDataList.5q09jzwfq5g0') }}</span>
              <a-popover content-class="pop-con">
                <span style="margin-left: 5px; cursor: pointer"
                  ><icon-filter
                /></span>
                <template #content>
                  <div class="filter-con">
                    <span>{{ $t('components.BigDataList.5q09jzwfppw0') }}</span>
                    <a-switch
                      v-model="onlyCheckError"
                      size="small"
                      @change="filterTableData(2)"
                    />
                  </div>
                </template>
              </a-popover>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
    <div class="big-data-table-body">
      <a-collapse :default-active-key="['table']" :bordered="true">
        <a-collapse-item v-for="item in table_list" :key="item.key">
          <template #header>
            <span>{{ listNampMap(item.key) }}</span>
          </template>
          <template #extra>
            <a-popover>
              <icon-bar-chart class="data-count" size="16" />
              <template #content>
                <p>
                  {{ $t('components.BigDataList.5q09nizckiw0')
                  }}{{ props.recordCounts[item.key].waitCount }}
                </p>
                <p>
                  {{ $t('components.BigDataList.5q09o79wsew0')
                  }}{{ props.recordCounts[item.key].runningCount }}
                </p>
                <p>
                  {{ $t('components.BigDataList.5q09o79wvtw0')
                  }}{{ props.recordCounts[item.key].successCount }}
                </p>
                <p>
                  {{ $t('components.BigDataList.5q09o79ww2g0')
                  }}{{ props.recordCounts[item.key].errorCount }}
                </p>
              </template>
            </a-popover>
          </template>
          <a-table
            :data="tableListData[item.key]"
            :bordered="false"
            :pagination="false"
            :scroll="{ y: 285 }"
            :scrollbar="false"
            :virtual-list-props="
              tableListData[item.key] && tableListData[item.key].length > 50
                ? { height: 300, threshold: 50 }
                : false
            "
          >
            <template #columns>
              <a-table-column
                title=""
                data-index="name"
                :width="250"
                ellipsis
                tooltip
              ></a-table-column>
              <a-table-column
                title=""
                data-index="name"
                :width="250"
                ellipsis
                tooltip
              ></a-table-column>
              <a-table-column title="" data-index="status" align="center">
                <template #cell="{ record }">
                  <span
                    v-if="
                      record.status === SUB_TASK_STATUS.FULL_START ||
                      record.status === SUB_TASK_STATUS.FULL_RUNNING
                    "
                    >{{
                      record.percent ? (record.percent * 100).toFixed(2) : '0'
                    }}%</span
                  >
                  <icon-check-circle-fill
                    v-if="
                      record.status === SUB_TASK_STATUS.FULL_FINISH ||
                      record.status === SUB_TASK_STATUS.FULL_CHECK_START ||
                      record.status === SUB_TASK_STATUS.FULL_CHECKING ||
                      record.status === SUB_TASK_STATUS.INCREMENTAL_START
                    "
                    size="16"
                    style="color: #00b429"
                  />
                  <a-popover
                    :title="$t('components.BigDataList.5q09jzwfqa80')"
                    position="tr"
                  >
                    <icon-close-circle-fill
                      v-if="record.status === SUB_TASK_STATUS.FULL_CHECK_FINISH"
                      size="16"
                      style="color: #ff7d01"
                    />
                    <template #content>
                      <p>{{ record.errorMsg }}</p>
                    </template>
                  </a-popover>
                </template>
              </a-table-column>
              <a-table-column title="" data-index="status" align="center">
                <template #cell="{ record }">
                  <span
                    v-if="record.status === SUB_TASK_STATUS.FULL_CHECK_START"
                    >{{
                      record.percent ? (record.percent * 100).toFixed(2) : '0'
                    }}%</span
                  >
                  <icon-check-circle-fill
                    v-if="record.status === SUB_TASK_STATUS.FULL_CHECKING"
                    size="16"
                    style="color: #00b429"
                  />
                  <a-popover
                    :title="$t('components.BigDataList.5q09jzwfqa80')"
                    position="tr"
                  >
                    <icon-close-circle-fill
                      v-if="record.status === SUB_TASK_STATUS.INCREMENTAL_START"
                      size="16"
                      style="color: #ff7d01"
                    />
                    <template #content>
                      <p>{{ record.errorMsg }}</p>
                    </template>
                  </a-popover>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-collapse-item>
      </a-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import { SUB_TASK_STATUS } from '@/utils/constants'

const { t } = useI18n()

const props = defineProps({
  fullData: Object,
  subTaskInfo: Object,
  recordCounts: {
    type: Object,
    default: () => ({
      table: { waitCount: 0, runningCount: 0, successCount: 0, errorCount: 0 },
      view: { waitCount: 0, runningCount: 0, successCount: 0, errorCount: 0 },
      function: {
        waitCount: 0,
        runningCount: 0,
        successCount: 0,
        errorCount: 0
      },
      trigger: { waitCount: 0, runningCount: 0, successCount: 0, errorCount: 0 },
      procedure: {
        waitCount: 0,
        runningCount: 0,
        successCount: 0,
        errorCount: 0
      }
    })
  }
})

const onlyError = ref(false)
const onlyCheckError = ref(false)

const table_list = ref([
  {
    key: 'table'
  },
  {
    key: 'view'
  },
  {
    key: 'function'
  },
  {
    key: 'trigger'
  },
  {
    key: 'procedure'
  }
])

// list name
const listNampMap = (key) => {
  const maps = {
    table: t('components.BigDataList.5q09jzwfqf40'),
    view: t('components.BigDataList.5q09jzwfqiw0'),
    function: t('components.BigDataList.5q09jzwfqm80'),
    trigger: t('components.BigDataList.5q09jzwfqp80'),
    procedure: t('components.BigDataList.5q09jzwfqs40')
  }
  return maps[key]
}

const tableListData = reactive({
  table: ref([]),
  view: ref([]),
  function: ref([]),
  trigger: ref([]),
  procedure: ref([])
})

watchEffect(
  () => {
    if (props.fullData) {
      tableListData.table = props.fullData['table']
      tableListData.view = props.fullData['view']
      tableListData.function = props.fullData['function']
      tableListData.trigger = props.fullData['trigger']
      tableListData.procedure = props.fullData['procedure']
    } else {
      tableListData.table = []
      tableListData.view = []
      tableListData.function = []
      tableListData.trigger = []
      tableListData.procedure = []
    }
  },
  { deep: true }
)

const filterTableData = (type) => {
  if ((type === 1 && onlyError.value) || (type === 2 && onlyCheckError.value)) {
    tableListData.table = props.fullData['table'].filter(
      (item) => (item.status === SUB_TASK_STATUS.FULL_CHECK_FINISH || item.status === SUB_TASK_STATUS.INCREMENTAL_START)
    )
    tableListData.view = props.fullData['view'].filter(
      (item) => item.status === SUB_TASK_STATUS.FULL_CHECK_FINISH
    )
    tableListData.function = props.fullData['function'].filter(
      (item) => item.status === SUB_TASK_STATUS.FULL_CHECK_FINISH
    )
    tableListData.trigger = props.fullData['trigger'].filter(
      (item) => item.status === SUB_TASK_STATUS.FULL_CHECK_FINISH
    )
    tableListData.procedure = props.fullData['procedure'].filter(
      (item) => item.status === SUB_TASK_STATUS.FULL_CHECK_FINISH
    )
  } else {
    onlyError.value = false
    onlyCheckError.value = false
    tableListData.table = props.fullData['table']
    tableListData.view = props.fullData['view']
    tableListData.function = props.fullData['function']
    tableListData.trigger = props.fullData['trigger']
    tableListData.procedure = props.fullData['procedure']
  }
}
</script>

<style lang="less" scoped>
.table-con {
  .table-header-con {
    height: 40px;
    overflow-y: hidden;
  }
  .big-data-table-body {
    :deep(.arco-table .arco-table-element thead) {
      .arco-table-th {
        .arco-table-cell {
          padding: 0;
        }
      }
    }
  }
  :deep(.arco-collapse-item-content) {
    padding-left: 0;
    padding-right: 0;
    background-color: transparent;
    .arco-collapse-item-content-box {
      padding: 0;
    }
  }
  :deep(.row-changed) {
    .arco-table-td {
      background: var(--color-primary-light-1);
    }
  }
  .data-count {
    cursor: pointer;
    color: var(--color-text-3);
  }
}
</style>
