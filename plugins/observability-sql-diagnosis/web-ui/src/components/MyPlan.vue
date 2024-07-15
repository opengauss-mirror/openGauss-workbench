<template>
  <div class="implementation-plan" v-if="props.planData.data">
    <div class="i-p-filter">
      <el-tooltip effect="light" placement="bottom-end" style="color: #fff">
        <template #content
          ><p style="color: #d4d4d4">{{ $t('sql.mostWidthPosi') }}</p></template
        >
        <el-button size="small" @click="getMostValueRow('planWidth')">{{ $t('sql.mostWidth') }}</el-button>
      </el-tooltip>
      <el-tooltip effect="light" placement="bottom">
        <template #content
          ><p style="color: #d4d4d4">{{ $t('sql.mostRowsPosi') }}</p></template
        >
        <el-button size="small" @click="getMostValueRow('planRows')">{{ $t('sql.mostRows') }}</el-button>
      </el-tooltip>
      <el-tooltip effect="light" placement="bottom-start">
        <template #content
          ><p style="color: #d4d4d4">{{ $t('sql.mostCostPosi') }}</p></template
        >
        <el-button size="small" @click="getMostValueRow('singleCost')">{{ $t('sql.mostCost') }}</el-button>
      </el-tooltip>
    </div>
    <div class="i-p-table">
      <el-table
        ref="singleTableRef"
        :data="props.planData.data"
        :style="{ width: '100%', marginBottom: '20px' }"
        row-key="nodeType"
        :row-class-name="tableRowClassName"
        :height="props.height"
        default-expand-all
        border
        :tree-props="{ children: 'planVOS' }"
      >
        <el-table-column type="index" align="center" />
        <el-table-column prop="nodeType" label="operation" show-overflow-tooltip />
        <el-table-column prop="alias" label="object" width="80" show-overflow-tooltip />
        <el-table-column label="cost" width="200">
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
        <el-table-column label="rows" width="120">
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
        <el-table-column label="width" width="120">
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
        <el-table-column prop="strategy" label="strategy" width="80" show-overflow-tooltip />
        <el-table-column prop="groupByKey" label="Group By Key" width="80" show-overflow-tooltip />
        <el-table-column prop="sortKey" label="Sort Key" width="80" show-overflow-tooltip />
        <el-table-column prop="filter" label="Filter" width="80" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useDebounceFn } from '@vueuse/core'
import { ElTable } from 'element-plus'

const props = withDefaults(
  defineProps<{
    planData: any
    height: string
  }>(),
  {
    planData: {
      data: [],
      total: {},
    },
  }
)

const data = reactive({
  planData: [],
  total: {
    totalPlanRows: 1,
    totalPlanWidth: 1,
  },
})

const recordMaxRowData = reactive({
  id: '',
  value: Number.MIN_VALUE,
})

const maxRowData = reactive({
  id: '',
  value: Number.MIN_VALUE,
})

const totalSingleCost = ref(0)
const totalCost = ref(0)
let count = 0
const calc = (nodes: any[]) => {
  nodes.forEach((d) => {
    count++
    d.id = count
    if (d.planVOS && d.planVOS.length) {
      if (d.nodeType !== 'Limit') {
        let c = d.totalCost
        d.planVOS.forEach((child: any) => {
          c = Number.parseFloat((c - child.totalCost).toFixed(2))
        })
        d.singleCost = c
      } else {
        d.singleCost = 0
      }
      calc(d.planVOS)
    } else {
      d.singleCost = d.totalCost
    }
    totalSingleCost.value += d.singleCost
  })
}
watch(props.planData, (res) => {
  if (Object.keys(res).length === 0) return
  data.total = res.total
  if (res.data && res.data.length) {
    calc(res.data)
    totalCost.value = res.data[0].totalCost
  }
  data.planData = res.data
})

onMounted(() => {
  let res = props.planData
  if (Object.keys(res).length === 0) return
  data.total = res.total
  if (res.data && res.data.length) {
    calc(res.data)
    totalCost.value = res.data[0].totalCost
  }
  data.planData = res.data
})

watch(recordMaxRowData, () => {
  if (recordMaxRowData.id !== '') {
    maxRowData.value = recordMaxRowData.value
    maxRowData.id = recordMaxRowData.id
  }
})

const getMostValueRow = (type: string) => {
  maxRowData.value = Number.MIN_VALUE
  maxRowData.id = ''
  recordMaxRowData.value = Number.MIN_VALUE
  recordMaxRowData.id = ''
  TraversalTree(props.planData.data, type)
}

const TraversalTree = useDebounceFn((treeData: Array<any>, type: string) => {
  console.log('DEBUG:treeData', treeData)
  treeData.forEach((item) => {
    console.log('DEBUG:item', item)
    console.log('DEBUG:type', type)
    console.log('DEBUG:item[type]', item[type])
    console.log('DEBUG:recordMaxRowData.value', recordMaxRowData.value)
    if (item[type] > recordMaxRowData.value) {
      recordMaxRowData.value = item[type]
      recordMaxRowData.id = item.id
    }
    if (Array.isArray(item.planVOS) && item.planVOS.length > 0) {
      TraversalTree(item.planVOS, type)
    }
  })
}, 0)

let timer: any
const tableRowClassName = ({ row }: { row: { id: string } }) => {
  console.log('DEBUG:row', row)
  console.log('DEBUG:maxRowData', row)
  console.log('DEBUG:row.id', row.id)
  console.log('DEBUG:maxRowData.id', maxRowData.id)
  if (row.id === maxRowData.id) {
    let observer: IntersectionObserver
    let el: HTMLElement
    let top: number = 0
    // clear high light
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      observer.unobserve(el)
      maxRowData.value = Number.MIN_VALUE
      maxRowData.id = ''
      recordMaxRowData.value = Number.MIN_VALUE
      recordMaxRowData.id = ''
      if (timer) {
        clearTimeout(timer)
      }
    }, 3000)

    nextTick(() => {
      el = document.querySelector('.warning-row') as HTMLElement
      if (el) {
        observer = new IntersectionObserver(
          (entries) => {
            if (!entries[0].isIntersecting) {
              document.querySelector('.i-p-table .el-scrollbar__wrap')?.scrollTo(0, top)
              observer.unobserve(el)
            }
          },
          {
            threshold: 1,
            root: document.querySelector('.i-p-table .el-scrollbar__wrap'),
          }
        )
        observer.observe(el)
        top = el.offsetTop
        let current = el.offsetParent as HTMLElement | null
        while (current !== null && !current.classList.contains('el-scrollbar')) {
          top += current.offsetTop
          current = current.offsetParent as HTMLElement | null
        }
      }
    })
    return 'warning-row'
  }
  return ''
}
</script>

<style scoped lang="scss">
.implementation-plan {
  .i-p-filter {
    display: flex;
    flex-direction: row-reverse;
    margin-bottom: 8px;

    &:deep(.el-button) {
      margin: 0 0 0 8px;
      background-color: var(--el-button-color-small);
      border: 1px solid #353535;
      color: var(--el-text-color-og);
    }

    &:deep(.el-button:hover) {
      color: var(--el-text-color-og);
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
      --el-table-tr-bg-color: var(--el-color-table-row-bg-color);
    }
  }
}
</style>
