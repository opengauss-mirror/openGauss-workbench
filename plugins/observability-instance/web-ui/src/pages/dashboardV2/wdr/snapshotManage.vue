<template>
  <div class="task-dialog">
    <el-dialog
      width="800px"
      :title="$t('dashboard.wdrReports.snapshotManageDialog.dialogName')"
      v-model="visible"
      :close-on-click-modal="false"
      draggable
      @close="taskClose"
    >
      <div class="dialog-content" v-loading="creatingSnapshot || reading">
        <div class="search-form">
          <div class="filter" style="margin-right: auto">
            <el-button type="primary" @click="handelBuild">{{
              $t('dashboard.wdrReports.snapshotManageDialog.createSnapshot')
            }}</el-button>
          </div>
          <div class="filter">
            <div class="cluster-container-title">{{ $t('datasource.cluterTitle') }}&nbsp;</div>
            <ClusterCascader
              ref="clusterComponent"
              instanceValueKey="hostId"
              @loaded="loaded"
              @getCluster="handleClusterValue"
              notClearable
            />
          </div>
          <div class="filter">
            <el-button type="primary" @click="handleQuery">{{ $t('app.query') }}</el-button>
          </div>
        </div>

        <div class="table-wrapper">
          <el-table
            :data="tableData"
            :header-cell-style="{ 'text-align': 'left' }"
            style="width: 100%"
            :default-sort="{ prop: 'date', order: 'descending' }"
          >
            <el-table-column
              prop="snapshotId"
              width="300"
              :label="$t('dashboard.wdrReports.snapshotManageDialog.snapshotID')"
              :align="'left'"
            />
            <el-table-column
              prop="endTs"
              :label="$t('dashboard.wdrReports.snapshotManageDialog.captureTime')"
              :align="'left'"
            />
          </el-table>
          <el-pagination
            :currentPage="page.currentPage"
            :pageSize="page.pageSize"
            :total="page.total"
            :page-sizes="[10, 20, 30, 40]"
            class="pagination"
            layout="total,sizes,prev,pager,next"
            background
            small
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { useRequest } from 'vue-request'
import restRequest from '@/request/restful'
import { ElMessage } from 'element-plus'
import { useMonitorStore } from '@/store/monitor'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const visible = ref(true)
const props = withDefaults(defineProps<{ tabId: string }>(), {})
const page = reactive({
  pageSize: 10,
  currentPage: 1,
  total: 0,
})
const handleSizeChange = (val: number) => {
  page.currentPage = 1
  page.pageSize = val
  changePageCurrent(page.currentPage)
}
const handleCurrentChange = (val: number) => {
  page.currentPage = val
  changePageCurrent(page.currentPage)
}
const changePageCurrent = (data: number) => {
  Object.assign(page, data)
  requestData()
}
const clusterComponent = ref<any>(null)
const cluster = ref<Array<any>>([])

const loaded = () => {
  let instanceId = useMonitorStore(props.tabId).instanceId
  if (instanceId && clusterComponent.value != null) {
    clusterComponent.value.setNodeId(instanceId)
    nextTick(() => {
      requestData()
    })
  }
}
const handleClusterValue = (val: any) => {
  cluster.value = val
}

const handleQuery = () => {
  requestData()
}

const handelBuild = () => {
  createSnapshot()
}
const { run: createSnapshot, loading: creatingSnapshot } = useRequest(
  () => {
    return restRequest
      .get('/wdr/createSnapshot', {
        clusterId: cluster.value.length ? cluster.value[0] : '',
        hostId: cluster.value.length > 1 ? cluster.value[1] : '',
      })
      .then(function (res) {
        return res
      })
      .catch(function (res) {})
  },
  {
    manual: true,
    onSuccess: (res) => {
      if (res && res.code === 200) {
        ElMessage({
          showClose: true,
          message: t('dashboard.wdrReports.snapshotManageDialog.createSnapshot'),
          type: 'success',
        })
      }
      reading.value = false
    },
  }
)
// list Data
const tableData = ref<Array<any>>([])
const {
  data: res,
  run: requestData,
  loading: reading,
} = useRequest(
  () => {
    return restRequest
      .get('/wdr/listSnapshot', {
        clusterId: cluster.value.length ? cluster.value[0] : '',
        hostId: cluster.value.length > 1 ? cluster.value[1] : '',
        orderby: 'snapshot_id desc',
        pageSize: page.pageSize,
        pageNum: page.currentPage,
      })
      .then(function (res) {
        return res
      })
      .catch(function (res) {
        tableData.value = []
        Object.assign(page, { pageSize: page.pageSize, total: 0, currentPage: 1 })
      })
  },
  { manual: true }
)
type Res =
  | {
      records: string[]
      pageNum: number
      total: number
    }
  | undefined
watch(res, (res: Res) => {
  if (res && res.records && res.records.length) {
    const { total } = res
    tableData.value = res.records
    Object.assign(page, { pageSize: page.pageSize, total })
  } else {
    tableData.value = []
    Object.assign(page, { pageSize: page.pageSize, total: 0 })
  }
})

const emits = defineEmits(['changeModal'])
const taskClose = () => {
  emits('changeModal', false)
}
</script>
