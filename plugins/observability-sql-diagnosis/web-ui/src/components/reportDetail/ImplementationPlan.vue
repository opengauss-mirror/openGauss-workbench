<script setup lang="ts">
import { ElTable } from 'element-plus'
import ogRequest from '../../request'
import { useRequest } from 'vue-request'

const props = withDefaults(
  defineProps<{
    plan: Record<string, any>
    nodeId: string
  }>(),
  {
    nodeId: '',
  }
)

const data = reactive({
  planData: [],
  total: {
    totalPlanRows: 1,
    totalPlanWidth: 1,
  },
})

const { data: res, run: requestData } = useRequest(
  (nodeId: string, sqlId) => {
    return ogRequest.get(`/historyDiagnosis/api/v1/plan/${nodeId}/${sqlId}`)
  },
  { manual: true }
)

onMounted(() => {
  const debugQueryId = localStorage.getItem('DEBUG_QUERY_ID')
  if (typeof debugQueryId === 'string' && debugQueryId !== '') {
    const nodeId = localStorage.getItem('SQL_DIAGNOSIS_NODEID') || ''
    requestData(nodeId, debugQueryId)
  }
})

watch(res, (r) => {
  if (r && Array.isArray(r.data?.data)) {
    calc(r.data.data)
    data.planData = r.data.data
    data.total = r.data.total
    totalCost.value = r.data.data[0].totalCost
  }
})

const totalSingleCost = ref(0)
const totalCost = ref(0)
const calc = (nodes: any[]) => {
  nodes.forEach((d) => {
    if (d.children && d.children.length) {
      if (d.nodeType !== 'Limit') {
        let c = d.totalCost
        d.children.forEach((child: any) => {
          c = Number.parseFloat((c - child.totalCost).toFixed(2))
        })
        d.singleCost = c
      } else {
        d.singleCost = 0
      }
      calc(d.children)
    } else {
      d.singleCost = d.totalCost
    }
    totalSingleCost.value += d.singleCost
  })
}
watch(
  () => props.plan,
  (res) => {
    const debugQueryId = localStorage.getItem('DEBUG_QUERY_ID')
    if (typeof debugQueryId === 'string' && debugQueryId !== '') {
      return
    }
    data.total = res.total
    if (res.data && res.data.length) {
      calc(res.data)
      totalCost.value = res.data[0].totalCost
    }
    data.planData = res.data
  }
)
</script>

<template>
  <div class="implementation-plan">
    <div class="i-p-table">
      <el-table
        ref="singleTableRef"
        :data="data.planData"
        :style="{ width: '100%', marginBottom: '20px' }"
        height="340"
        row-key="id"
        default-expand-all
        border
      >
        <el-table-column type="index" />
        <el-table-column prop="nodeType" label="operation" />
        <el-table-column prop="alias" label="object" width="150" />
        <el-table-column label="cost" width="300">
          <template #default="{ row }">
            <div class="i-p-table-cost">
              <div style="flex: 1; position: relative; height: 10px">
                <my-progress
                  :data="[
                    { label: $t('report.singleStepOperationCost'), value: row.singleCost, color: '#37D4D1' },
                    { label: $t('report.totalCost'), value: totalSingleCost, total: true },
                  ]"
                  :style="{
                    position: 'absolute',
                    zIndex:
                      row.singleCost / totalSingleCost <= row.totalCost / totalCost && row.nodeType !== 'Limit' ? 1 : 0,
                  }"
                  width="100%"
                  height="10px"
                  :fixTotal="row.totalCost"
                  :fixColor="['#37D4D1', '#0093FF']"
                />
                <my-progress
                  :data="[
                    {
                      label: $t('report.singleStepOperationCost'),
                      value: row.totalCost,
                      color: '#0093FF',
                      hide: row.nodeType === 'Limit',
                    },
                    { label: $t('report.totalCost'), value: totalCost, total: true },
                  ]"
                  style="position: absolute"
                  width="100%"
                  height="10px"
                  :fixTotal="row.totalCost"
                  :fixColor="['#37D4D1', '#0093FF']"
                />
              </div>
              <p>{{ row.totalCost }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="rows">
          <template #default="{ row }">
            <div class="i-p-table-cost">
              <my-progress
                :data="[
                  { label: 'rows', value: row.planRows ?? 0, color: '#0093FF' },
                  { label: '', value: data.total.totalPlanRows, total: true },
                ]"
                style="flex: 1"
                width="100%"
                height="10px"
                :onlyOne="true"
              />
              <p>{{ row.planRows }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="width">
          <template #default="{ row }">
            <div class="i-p-table-cost">
              <my-progress
                :data="[
                  { label: 'width', value: row.planWidth ?? 0, color: '#0093FF' },
                  { label: '', value: data.total.totalPlanWidth, total: true },
                ]"
                style="flex: 1"
                width="100%"
                height="10px"
                :onlyOne="true"
              />
              <p>{{ row.planWidth }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="joinType" label="condition" />
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.implementation-plan {
  .i-p-filter {
    display: flex;
    flex-direction: row-reverse;
    margin-bottom: 8px;

    &:deep(.el-button) {
      margin: 0 0 0 8px;
      background-color: #5c5c5c;
      border: 1px solid #353535;
      color: #d4d4d4;
    }

    &:deep(.el-button:hover) {
      color: #d4d4d4;
    }
  }

  .i-p-table {
    overflow-y: auto;

    &-cost {
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      > p {
        margin-left: 8px;
      }
    }

    &:deep(.el-table .warning-row) {
      --el-table-tr-bg-color: #4e575a;
    }

    :deep(.el-table) {
      background-color: var(--el-bg-color-og);
    }

    :deep(.el-table__header-wrapper th) {
      background-color: var(--el-bg-color-og);
      color: var(--el-text-color-og);
    }

    :deep(.el-table__row) {
      background-color: var(--el-bg-color-og);
    }
    :deep(.el-table__row th) {
      color: var(--el-text-color-og);
    }
  }
}
</style>
