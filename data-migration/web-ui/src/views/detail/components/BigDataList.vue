<template>
  <div class="table-con">
    <div class="table-header-con">
      <a-table :bordered="false" :pagination="false">
        <template #columns>
          <a-table-column :title="`源库：${props.subTaskInfo.sourceDb}`" data-index="name" :width="250"></a-table-column>
          <a-table-column :title="`目的库：${props.subTaskInfo.targetDb}`" data-index="name" :width="250"></a-table-column>
          <a-table-column data-index="status" align="center">
            <template #title>
              <span>迁移状态</span>
              <a-popover content-class="pop-con">
                <span style="margin-left: 5px;cursor: pointer;"><icon-filter /></span>
                <template #content>
                  <div class="filter-con">
                    <span>仅显示错误数据：</span>
                    <a-switch v-model="onlyError" size="small" @change="filterTableData(1)" />
                  </div>
                </template>
              </a-popover>
            </template>
          </a-table-column>
          <a-table-column data-index="status" align="center">
            <template #title>
              <span>迁移校验</span>
              <a-popover content-class="pop-con">
                <span style="margin-left: 5px;cursor: pointer;"><icon-filter /></span>
                <template #content>
                  <div class="filter-con">
                    <span>仅显示错误数据：</span>
                    <a-switch v-model="onlyCheckError" size="small" @change="filterTableData(2)" />
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
            <span>{{ item.name }}</span>
          </template>
          <template #extra>
            <a-popover>
              <icon-bar-chart class="data-count" size="16" />
              <template #content>
                <p>等待数：{{ props.recordCounts[item.key].waitCount }}</p>
                <p>执行数：{{ props.recordCounts[item.key].runningCount }}</p>
                <p>完成数：{{ props.recordCounts[item.key].finishCount }}</p>
                <p>失败数：{{ props.recordCounts[item.key].errorCount }}</p>
              </template>
            </a-popover>
          </template>
          <a-table :data="tableListData[item.key]" :bordered="false" :pagination="false" :scroll="{ y: 285 }" :scrollbar="false" :virtual-list-props="tableListData[item.key].length > 50 ? { height: 300, threshold: 7 } : false">
            <template #columns>
              <a-table-column title="" data-index="name" :width="250" ellipsis tooltip></a-table-column>
              <a-table-column title="" data-index="name" :width="250" ellipsis tooltip></a-table-column>
              <a-table-column title="" data-index="status" align="center">
                <template #cell="{ record }">
                  <a-popover>
                    <icon-bar-chart v-if="!record.status" class="data-count" size="16" />
                    <template #content>
                      <p>等待数：{{ record.counts.waitCount }}</p>
                      <p>执行数：{{ record.counts.runningCount }}</p>
                      <p>完成数：{{ record.counts.finishCount }}</p>
                      <p>失败数：{{ record.counts.errorCount }}</p>
                    </template>
                  </a-popover>
                  <span v-if="record.status === 1 || record.status === 2">{{ record.percent ? (record.percent * 100).toFixed(2) : '0' }}%</span>
                  <icon-check-circle-fill v-if="record.status === 3 || record.status === 4 || record.status === 5" size="16" style="color: #00B429;" />
                  <a-popover title="错误详情" position="tr">
                    <icon-close-circle-fill v-if="record.status === 6" size="16" style="color: #FF7D01;" />
                    <template #content>
                      <p>{{ record.msg }}</p>
                    </template>
                  </a-popover>
                </template>
              </a-table-column>
              <a-table-column title="" data-index="status" align="center">
                <template #cell="{ record }">
                  <span v-if="record.status === 4">{{ record.percent ? (record.percent * 100).toFixed(2) : '0' }}%</span>
                  <icon-check-circle-fill v-if="record.status === 5" size="16" style="color: #00B429;" />
                  <a-popover title="错误详情" position="tr">
                    <icon-close-circle-fill v-if="record.status === 6" size="16" style="color: #FF7D01;" />
                    <template #content>
                      <p>{{ record.msg }}</p>
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

const props = defineProps({
  fullData: Object,
  subTaskInfo: Object,
  recordCounts: {
    type: Object,
    default: () => ({
      table: { waitCount: 0, runningCount: 0, finishCount: 0, errorCount: 0 },
      view: { waitCount: 0, runningCount: 0, finishCount: 0, errorCount: 0 },
      function: { waitCount: 0, runningCount: 0, finishCount: 0, errorCount: 0 },
      trigger: { waitCount: 0, runningCount: 0, finishCount: 0, errorCount: 0 },
      procedure: { waitCount: 0, runningCount: 0, finishCount: 0, errorCount: 0 }
    })
  }
})

const onlyError = ref(false)
const onlyCheckError = ref(false)

const table_list = ref([
  {
    key: 'table',
    name: '表'
  },
  {
    key: 'view',
    name: '视图'
  },
  {
    key: 'function',
    name: '函数'
  },
  {
    key: 'trigger',
    name: '触发器'
  },
  {
    key: 'procedure',
    name: '存储过程'
  }
])

const tableListData = reactive({
  table: ref([]),
  view: ref([]),
  function: ref([]),
  trigger: ref([]),
  procedure: ref([])
})

watchEffect(() => {
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
}, { deep: true })

const filterTableData = (type) => {
  if ((type === 1 && onlyError.value) || (type === 2 && onlyCheckError.value)) {
    tableListData.table = props.fullData['table'].filter(item => item.status === 6)
    tableListData.view = props.fullData['view'].filter(item => item.status === 6)
    tableListData.function = props.fullData['function'].filter(item => item.status === 6)
    tableListData.trigger = props.fullData['trigger'].filter(item => item.status === 6)
    tableListData.procedure = props.fullData['procedure'].filter(item => item.status === 6)
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
