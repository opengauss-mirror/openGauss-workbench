<template>
  <div class="search-form">
    <div class="filter">
      <ClusterCascader :title="$t('datasource.cluterTitle')" @getCluster="handleClusterValue" />
    </div>
    <div class="filter">
      <span>{{ $t('datasource.database') }}&nbsp;</span>
      <el-select
        v-model="formData.dbName"
        :placeholder="$t('datasource.selectDatabaseType')"
        style="max-width: 150px"
        clearable
      >
        <el-option v-for="item in dbList" :key="item" :value="item" :label="item" />
      </el-select>
    </div>
    <div class="filter">
      <span>{{ $t('datasource.sqlTrackTaskStartTimeSelect') }}&nbsp;</span>
      <MyDatePicker v-model="formData.dateValue" type="datetimerange" :valueFormatToUTC="true" style="width: 300px" />
    </div>
    <el-button type="primary" class="search-button" @click="handleQuery" :icon="Search">{{
      $t('app.query')
    }}</el-button>
    <el-button @click="handleReset" :title="$t('app.refresh')" :icon="Refresh">{{ $t('app.reset') }}</el-button>
  </div>
  <div class="slow-log">
    <div class="slow-log-table">
      <el-table
        size="small"
        :data="tableDatas"
        style="width: 100%"
        :default-sort="{ prop: 'date', order: 'descending' }"
        :header-cell-class-name="
          () => {
            return 'grid-header'
          }
        "
      >
        <el-table-column prop="uniqueQueryId" :label="$t('datasource.sqlID')" width="90" />
        <el-table-column :label="$t('datasource.slowLogTable[0]')" width="140" align="center">
          <template #default="scope">
            <span>{{ dayjs.utc(scope.row.startTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('datasource.slowLogTable[1]')" width="140" align="center">
          <template #default="scope">
            <span>{{ dayjs.utc(scope.row.finishTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sqlTemplate" :label="$t('datasource.slowLogTable[2]')" min-width="250">
          <template #default="scope">
            <span v-if="scope.row.sqlTemplate && scope.row.sqlTemplate.length > 35">
              <el-popover width="300" trigger="hover" :content="scope.row.sqlTemplate" popper-class="sql-popover-tip">
                <template #reference>
                  <span>{{ scope.row.sqlTemplate.substr(0, 35) + '...' }}</span>
                </template>
              </el-popover>
            </span>
            <span v-else>{{ scope.row.sqlTemplate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dbName" :label="$t('datasource.slowLogTable[3]')" width="100" />
        <el-table-column prop="clientAddr" :label="$t('datasource.slowLogTable[4]')" width="110" />
        <el-table-column prop="schemaName" :label="$t('datasource.slowLogTable[5]')" width="100" />
        <el-table-column prop="dbTime" :label="$t('datasource.slowLogTable[6]')" width="120" />
        <el-table-column prop="cpuTime" :label="$t('datasource.slowLogTable[7]')" width="120" />
        <el-table-column prop="dataIoTime" :label="$t('datasource.slowLogTable[8]')" width="120" />
        <el-table-column prop="parseTime" :label="$t('datasource.slowLogTable[9]')" width="120" />
        <el-table-column prop="plExecutionTime" :label="$t('datasource.slowLogTable[10]')" width="130" />
        <el-table-column prop="lockWaitTime" :label="$t('datasource.slowLogTable[11]')" width="120" />
        <el-table-column prop="nreturnedRows" :label="$t('datasource.slowLogTable[12]')" width="100" />
        <el-table-column prop="nreturnedRows" :label="$t('datasource.slowLogTable[13]')" width="100" />
        <el-table-column :label="$t('datasource.trackTable[9]')" align="center" fixed="right" width="80">
          <template #default="scope">
            <el-link type="primary" @click="handleModal(scope.row)">{{ $t('datasource.diagnosis') }}</el-link>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:currentPage="page.currentPage"
        v-model:pageSize="page.pageSize"
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
</template>

<script lang="ts" setup>
import ogRequest from '@/request'
import { useRequest } from 'vue-request'
import { cloneDeep } from 'lodash-es'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

dayjs.extend(utc)
dayjs.extend(timezone)

type Res =
  | {
      records: string[]
      pageNum: number
      total: number
    }
  | undefined

type AddTaskParam = {
  sqlText: string
  clusterId: string
  nodeId: string
  dbName: string
}
const emit = defineEmits(['addTask'])
const tableDatas = ref<Array<any>>([])
const dbList = ref<Array<any>>([])
const searched = ref(false)
const cluster = ref<Array<any>>([])
const addTaskParam = ref<AddTaskParam>({
  sqlText: '',
  clusterId: '',
  nodeId: '',
  dbName: '',
})

const handleModal = (row: any) => {
  addTaskParam.value = {
    sqlText: row.sqlTemplate,
    clusterId: row.clusterId,
    nodeId: row.nodeId,
    dbName: row.dbName,
  }
  emit('addTask', addTaskParam.value)
}
const initFormData = {
  dbName: '',
  dateValue: [],
  type: 'countlist',
}
const formData = reactive(cloneDeep(initFormData))
const page = reactive({
  pageSize: 10,
  currentPage: 1,
  total: 0,
})
const queryData = computed(() => {
  const { dbName, dateValue } = formData
  const { pageSize: row, currentPage: current } = page
  const queryObj = {
    startTime: dateValue.length ? dateValue[0] : null,
    finishTime: dateValue.length ? dateValue[1] : null,
    dbName,
    pageNum: current,
    pageSize: row,
    nodeId: cluster.value.length ? cluster.value[1] : '',
    queryCount: true,
  }
  return queryObj
})
const handleQuery = () => {
  if (!searched.value) searched.value = true
  if (!queryData.value.nodeId) {
    ElMessage.warning(t('datasource.pleaseSelectInstance'))
    return
  }
  page.currentPage = 1
  requestData()
}
const handleReset = () => {
  page.currentPage = 1
  formData.dateValue = []
  if (!queryData.value.nodeId) {
    ElMessage.warning(t('datasource.pleaseSelectInstance'))
    return
  }
  requestData()
}
const changePageCurrent = (data: number) => {
  Object.assign(page, data)
  requestData()
}
const handleClusterValue = (val: any) => {
  cluster.value = val
  dbList.value = []
  formData.dbName = ''
  if (cluster.value.length > 0) dbData(val[1])
}

const { data: ret, run: dbData } = useRequest(
  (nodeId: string) => {
    return ogRequest.get('/sqlDiagnosis/api/v1/clusters/' + nodeId + '/instances', '')
  },
  { manual: true }
)

const { data: res, run: requestData } = useRequest(
  () => {
    const clusterId = cluster.value.length ? cluster.value[0] : ''
    const nodeId = queryData.value.nodeId
    return ogRequest
      .get('/sqlDiagnosis/api/v1/slowSqls', { ...queryData.value })
      .then(function (res) {
        res.records.forEach((element: { clusterId: any; nodeId: any }) => {
          element.nodeId = nodeId
          element.clusterId = clusterId
        })
        return res
      })
      .catch(function (res) {
        tableDatas.value = []
        Object.assign(page, { pageSize: page.pageSize, total: 0, currentPage: 1 })
      })
  },
  { manual: true }
)
watch(ret, (ret: any[]) => {
  if (ret && Object.keys(ret).length) {
    dbList.value = ret
  } else {
    dbList.value = []
  }
})
watch(res, (res: Res) => {
  if (res && Object.keys(res).length) {
    tableDatas.value = res.records
    Object.assign(page, { pageSize: page.pageSize, total: res.total })
  } else {
    tableDatas.value = []
    Object.assign(page, { pageSize: page.pageSize, total: 0, currentPage: 1 })
  }
})

const props = defineProps<{
  logData: string[]
  pages: {
    pageSize: number
    currentPage: number
    total: number
  }
}>()
const tableData = ref<Array<any>>([])
watch(
  () => props.pages,
  (newVal) => {
    Object.assign(page, newVal)
  },
  { deep: true }
)
watch(
  () => props.logData,
  (newVal) => {
    tableData.value = newVal
  }
)

const handleSizeChange = (val: number) => {
  page.currentPage = 1
  page.pageSize = val
  changePageCurrent(page.currentPage)
}
const handleCurrentChange = (val: number) => {
  page.currentPage = val
  changePageCurrent(page.currentPage)
}
</script>

<style lang="scss" scoped>
.slow-log {
  &-chart {
    height: 380px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    .chart-wrap {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      text-align: center;
      .chart-content {
        width: 100%;
        height: 100%;
      }
    }
    .noresult-wrap {
      width: 100%;
      height: 500px;
      .noresult-img {
        width: 200px;
        display: block;
        margin: 20px auto;
      }
      .noresult-text {
        text-align: center;
        color: #707070;
      }
    }
    .noResult .barLine-chart {
      display: none;
    }
  }
}
</style>
