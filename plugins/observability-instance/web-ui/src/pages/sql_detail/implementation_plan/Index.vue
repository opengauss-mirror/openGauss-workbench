<template>
  <div class="implementation-plan" v-if="!errorInfo">
    <div class="i-p-filter">
      <el-tooltip effect="light" placement="bottom-end" style="color: #fff">
        <template #content
          ><p :style="{ color: theme === 'dark' ? '#D4D4D4' : '#868F9C' }">{{ $t('sql.mostWidthPosi') }}</p></template
        >
        <el-button size="small" @click="getMostValueRow('planWidth')">{{ $t('sql.mostWidth') }}</el-button>
      </el-tooltip>
      <el-tooltip effect="light" placement="bottom">
        <template #content
          ><p :style="{ color: theme === 'dark' ? '#D4D4D4' : '#868F9C' }">{{ $t('sql.mostRowsPosi') }}</p></template
        >
        <el-button size="small" @click="getMostValueRow('planRows')">{{ $t('sql.mostRows') }}</el-button>
      </el-tooltip>
      <el-tooltip effect="light" placement="bottom-start">
        <template #content
          ><p :style="{ color: theme === 'dark' ? '#D4D4D4' : '#868F9C' }">{{ $t('sql.mostCostPosi') }}</p></template
        >
        <el-button size="small" @click="getMostValueRow('singleCost')">{{ $t('sql.mostCost') }}</el-button>
      </el-tooltip>
    </div>
    <div class="i-p-table" v-loading="planDataLoading">
      <el-table
        ref="singleTableRef"
        :data="data.planData"
        :style="{ width: '100%', marginBottom: '20px' }"
        row-key="id"
        :row-class-name="tableRowClassName"
        height="340"
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
  <my-message
    v-if="errorInfo"
    :type="isInfoTip(errorInfo) ? 'info' : 'error'"
    :tip="$t(`sql.${errorInfo}`)"
    defaultTip=""
    :key="errorInfo"
  />
</template>

<script setup lang="ts">
import { useDebounceFn } from '@vueuse/core'
import { ElTable } from 'element-plus'
import { useRequest } from 'vue-request'
import { storeToRefs } from 'pinia'
import ogRequest from '@/request'
import { useWindowStore } from '@/store/window'

const { theme } = storeToRefs(useWindowStore())

const props = withDefaults(
  defineProps<{
    dbid: string | string[]
    sqlId: string | string[]
  }>(),
  {
    dbid: '',
    sqlId: '',
  }
)

const errorInfo = ref<string | undefined>()

const data = reactive<{
  planData: Array<any>
  total: {
    totalPlanRows: number
    totalPlanWidth: number
  }
}>({
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

const { run: requesPlanDataRes, loading: planDataLoading } = useRequest(
  (sqlId: string | string[], dbid: string | string[]) => {
    ogRequest
      .get(
        `/observability/v1/topsql/plan?id=${dbid}&sqlId=${sqlId}`
      )
      .then((r: any) => {
        const list = r.data
        const total = r.total
        if (Array.isArray(list) && list.length > 0) {
          calc(list)
          totalCost.value = list[0].totalCost
          data.planData = list
          data.total = total
        }
      }).catch((e: any) => {
        if (!e) {
          errorInfo.value = 'failGetExecutionPlan'
          return
        }
        if (typeof(e) === 'string') {
          errorInfo.value = e;
          return
        }
        const code = e?.data.code;
        if (code === 602) {
          errorInfo.value = 'executionParamTip';
        }
      });
  },
  { manual: true }
)

onMounted(() => {
  requesPlanDataRes(props.sqlId, props.dbid)
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
  TraversalTree(data.planData, type)
}

const TraversalTree = useDebounceFn((treeData: Array<any>, type: string) => {
  treeData.forEach((item) => {
    if (item[type] > recordMaxRowData.value) {
      recordMaxRowData.value = item[type]
      recordMaxRowData.id = item.id
    }
    if (Array.isArray(item.children) && item.children.length > 0) {
      TraversalTree(item.children, type)
    }
  })
}, 0)

const isInfoTip = (value: string | undefined) => {
  return value === 'failGetExecutionPlan' || value === 'failResolveExecutionPlan'
}

let timer: any
const tableRowClassName = ({ row }: { row: { id: string } }) => {
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
