<template>
  <div class="search-form">
    <div class="filter">
      <el-button type="primary" @click="handleModal">{{ $t('datasource.addTrBtn') }}</el-button>
    </div>
    <div class="seperator"></div>
    <div class="filter">
      <span>{{ $t('datasource.cluterTitle') }}&nbsp;</span>
      <el-cascader v-model="formData.cluster" clearable :options="clusterList" @change="getClusterValue" />
    </div>
    <div class="filter">
      <span>{{ $t('datasource.database') }}&nbsp;</span>
      <el-select
        v-model="formData.dbName"
        clearable
        style="width: 150px"
        :placeholder="$t('datasource.selectDatabaseType')"
      >
        <el-option v-for="item in dbList" :key="item" :value="item" :label="item" />
      </el-select>
    </div>
    <div class="filter">
      <el-input
        v-model="formData.searchText"
        clearable
        style="width: 180px"
        :prefix-icon="Search"
        :placeholder="$t('datasource.searchPlaceholder')"
      />
    </div>
    <div class="filter">
      <span>{{ $t('datasource.createTime') }}&nbsp;</span>
      <MyDatePicker v-model="formData.dateValue" type="datetimerange" :valueFormatToUTC="true" />
    </div>
    <el-button type="primary" class="search-button" @click="handleQuery" :icon="Search">{{
      $t('app.query')
    }}</el-button>
  </div>
  <div class="page-container">
    <div class="table-wrapper">
      <el-table
        :data="tableData"
        :header-cell-style="{ 'text-align': 'center' }"
        style="width: 100%"
        :default-sort="{ prop: 'date', order: 'descending' }"
      >
        <el-table-column :label="$t('datasource.trackTable[0]')" width="160" align="center">
          <template #default="scope">
            <span v-if="scope.row.name && scope.row.name.length > 18">
              <el-popover width="300" trigger="hover" :content="scope.row.name" popper-class="sql-popover-tip">
                <template #reference>
                  <a class="table-wrapper-table-id" @click="gotoTaskDetail(scope.row.id)">{{
                    scope.row.name.substr(0, 18) + '...'
                  }}</a>
                </template>
              </el-popover>
            </span>
            <a class="table-wrapper-table-id" @click="gotoTaskDetail(scope.row.id)" v-else>{{ scope.row.name }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="tasktype" :label="$t('datasource.trackTable[1]')" width="80" align="center" />
        <el-table-column label="SQL" width="300">
          <template #default="scope">
            <span v-if="scope.row.sql && scope.row.sql.length > 35">
              <el-popover width="300" trigger="hover" :content="scope.row.sql" popper-class="sql-popover-tip">
                <template #reference>
                  <span>{{ scope.row.sql.substr(0, 35) + '...' }}</span>
                </template>
              </el-popover>
            </span>
            <span v-else>{{ scope.row.sql }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="state" :label="$t('datasource.trackTable[2]')" width="100" align="center" />
        <el-table-column :label="$t('datasource.trackTable[3]')" width="140" align="center">
          <template #default="scope">
            {{ scope.row.starttime ? dayjs.utc(scope.row.starttime).local().format('YYYY-MM-DD HH:mm:ss') : '' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('datasource.trackTable[4]')" width="140" align="center">
          <template #default="scope">
            <div>{{ scope.row.endtime ? dayjs.utc(scope.row.endtime).local().format('YYYY-MM-DD HH:mm:ss') : '' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="cost" :label="$t('datasource.trackTable[5]')" width="100" />
        <el-table-column :label="$t('datasource.trackTable[6]')" width="140" align="center">
          <template #default="scope">
            <div>
              {{ scope.row.createtime ? dayjs.utc(scope.row.createtime).local().format('YYYY-MM-DD HH:mm:ss') : '' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="clusterId" :label="$t('datasource.trackTable[7]')" width="100" align="center" />
        <el-table-column prop="nodeId" :label="$t('datasource.trackTable[8]')" width="100" align="center" />
        <el-table-column :label="$t('datasource.trackTable[9]')" align="center" fixed="right" width="80">
          <template #default="scope">
            <el-link size="small" type="primary" @click="handleDelete(scope.row)">{{ $t('app.delete') }}</el-link>
          </template>
        </el-table-column>
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
      <TrackAdd
        :type="1"
        v-if="addModel"
        :show="addModel"
        :dbName="formData.dbName"
        :clusterId="formData.cluster"
        :clusterList="clusterList"
        :dbList="dbList"
        @changeModal="changeModalCurrent"
        @conveyFlag="bandleCovey"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { Search } from '@element-plus/icons-vue'
import 'element-plus/es/components/message-box/style/index'
import { cloneDeep } from 'lodash-es'
import TrackAdd from '@/pages/datasource/track/trackAdd.vue'
import ogRequest from '@/request'
import { useRequest } from 'vue-request'
import { ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

type Res =
  | {
      tableData: string[]
      total: number
      pageNum: number
      records: string[]
    }
  | undefined

type Rer =
  | [
      {
        [propName: string]: string | number
      }
    ]
  | undefined

const initFormData = {
  dbName: '',
  searchText: '',
  dateValue: [],
  cluster: [],
}
const formData = reactive(cloneDeep(initFormData))
const tableData = ref<Array<any>>([])
const dbList = ref<Array<any>>([])
const clusterList = ref<Array<any>[]>([])
const addModel = ref(false)
const page = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 10,
})

const queryData = computed(() => {
  const { dateValue, searchText, dbName, cluster } = formData
  const { pageSize: pagesize, currentPage: current } = page
  const queryObj = {
    startTime: dateValue.length && dateValue[0] ? dateValue[0] : null,
    endTime: dateValue.length && dateValue[1] ? dateValue[1] : null,
    dbName,
    name: searchText,
    pageNum: current,
    pageSize: pagesize,
    clusterId: cluster.length > 0 ? cluster[0] : null,
    nodeId: cluster.length > 0 ? cluster[1] : null,
    sqlId: '',
    queryCount: true,
  }
  return queryObj
})
const handleModal = () => {
  addModel.value = true
}
const changeModalCurrent = (val: boolean) => {
  addModel.value = val
}
const bandleCovey = (code: number) => {
  page.currentPage = 1
  requestData()
}
const handleQuery = () => {
  page.currentPage = 1
  requestData()
}

const handleSizeChange = (val: number) => {
  page.currentPage = 1
  page.pageSize = val
  requestData()
}
const handleCurrentChange = (val: number) => {
  page.currentPage = val
  requestData()
}
const router = useRouter()
const gotoTaskDetail = (id: string) => {
  if (process.env.mode === 'production') {
    window.$wujie?.props.methods.jump({
      name: `Static-pluginObservability-sql-diagnosisVemTrack_detail`,
      query: {
        id,
      },
    })
  } else router.push(`/vem/track_detail/${id}`)
}
const getClusterValue = (val: string[]) => {
  dbList.value = []
  formData.dbName = ''
  if (formData.cluster == null) formData.cluster = []
  if (formData.cluster && formData.cluster.length > 0) dbData(formData.cluster[1])
}
const handleDelete = (val: any) => {
  ElMessageBox.confirm(t('datasource.confirmToDeleteTask'))
    .then(() => {
      hanleDelete(val.id)
    })
    .catch(() => {
      console.log('cancel')
      // catch error
    })
}
onMounted(() => {
  // clear DEBUG_QUERY_ID && SQL_DIAGNOSIS_NODEID
  localStorage.removeItem('DEBUG_QUERY_ID')
  localStorage.removeItem('SQL_DIAGNOSIS_NODEID')
  requestData()
  clusterData()
})

const { data: rer, run: clusterData } = useRequest(
  () => {
    return ogRequest.get('/sqlDiagnosis/api/v1/clusters', '')
  },
  { manual: true }
)
const { data: ret, run: dbData } = useRequest(
  (nodeId: string) => {
    return ogRequest.get('/sqlDiagnosis/api/v1/clusters/' + nodeId + '/instances', '')
  },
  { manual: true }
)
const { data: res, run: requestData } = useRequest(
  () => {
    return ogRequest.get('/sqlDiagnosis/api/v1/diagnosisTasks', queryData.value)
  },
  { manual: true }
)

// Delete one task
const hanleDelete = (id: string) => {
  useRequest(
    () => {
      return ogRequest.delete(`/sqlDiagnosis/api/v1/diagnosisTasks/${id}`)
    },
    {
      onSuccess: (data) => {
        if (JSON.stringify(data)) {
          requestData()
        }
      },
    }
  )
}

const treeTransform = (arr: any) => {
  let obj: any = []
  if (arr instanceof Array) {
    arr.forEach((item) => {
      obj.push({
        label: item.clusterId ? item.clusterId : item.azName + '_' + item.publicIp + '(' + item.nodeId + ')',
        value: item.clusterId ? item.clusterId : item.nodeId,
        children: treeTransform(item.clusterNodes),
      })
    })
  }
  return obj
}
// Cluster and host IP
watch(rer, (rer: Rer) => {
  if (rer && Object.keys(rer).length) {
    clusterList.value = treeTransform(rer)
  }
})
// Database Type
watch(ret, (ret: any[]) => {
  if (ret && Object.keys(ret).length) {
    dbList.value = ret
  } else {
    dbList.value = []
  }
})
watch(res, (res: Res) => {
  if (res && Object.keys(res).length) {
    const { total } = res
    tableData.value = res.records
    Object.assign(page, { pageSize: page.pageSize, total })
  } else {
    tableData.value = []
    Object.assign(page, { pageSize: page.pageSize, total: 0, currentPage: 1 })
  }
})
</script>

<style lang="scss" scoped>
.operator-wrap {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin: 20px 0;
  & > div {
    margin-right: 10px;
  }
}
.table-wrapper {
  height: calc(100% - 50px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  &-table-id {
    color: #0093ff;
    cursor: pointer;
  }
}
.deleteBtn {
  color: #d4d4d4;
  cursor: pointer;
}
.deleteBtn-icon {
  position: relative;
  top: 2px;
  right: 5px;
}
:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
}
:deep(.el-range-editor--small.el-input__wrapper) {
  width: 240px;
}
</style>
