<template>
  <div class="search-form">
    <div class="form1">
      <div class="filter">
        <span>{{ $t('datasource.peroid') }}&nbsp;</span>
        <el-cascader v-model="sqlPeroid" :options="peroidOptions" :show-all-levels="false" @change="setSqlConfig('peroid')" style="width: 100px;" />
      </div>
      <div class="filter">
        <span>{{ $t('datasource.frequency') }}&nbsp;</span>
        <el-cascader v-model="sqlFrequency" :options="frequencyOptions" :show-all-levels="false" @change="setSqlConfig('frequency')" style="width: 100px;" />
      </div>
    </div>
    <div class="form2">
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
  </div>
  <div>
    <my-card
      :title="$t('dashboard.slowSqlMore') + (metricsData.threshold ? `(${$t('app.moreThan') + metricsData.threshold})` : '')"
      height="200"
      :bodyPadding="false"
      :showBtns="true" @download="title => download(title,slowSQL3s)"
      :info="slowSQL3sInfo"
    >
      <template v-if="placeholders === '{{i18n,slowSql.param.tip}}'">
        <div class="message-error">
          <div class="message-error">
            <div class=""><svg-icon name="info" />{{ $t('datasource.slowSqlChartParamTip') }}</div>
          </div>
        </div>
      </template>
      <template v-else-if="placeholders === '{{i18n,slowSql.agent.tip}}'">
        <div class="message-error">
          <div class=""><svg-icon name="info" />{{ $t('datasource.slowSqlChartAgentTip') }}</div>
        </div>
      </template>
      <template v-else-if="placeholders === '{{i18n,slowSql.metric.tip}}'">
        <div class="message-error">
          <div class=""><svg-icon name="info" />{{ $t('datasource.slowSqlChartMetricTip') }}</div>
        </div>
      </template>
      <template v-else>
        <LazyLine
          ref="slowSQL3s"
          :tabId="1"
          :tips="$t(`datasource.slowSqlChartTip`)"
          :formatter="toFixed"
          :data="metricsData.slowSQL"
          :xData="metricsData.time"
        />
      </template>
    </my-card>
  </div>
  <el-tabs v-model="tab" class="tab2">
    <el-tab-pane :name="2">
      <template #label>
        <span>{{ $t("datasource.statistics") }}
        </span>
      </template>
      <el-table
        size="small"
        :data="tableAggDatas"
        style="width: 100%"
        v-loading="aggLoading"
        :default-sort="{ prop: 'totalExecuteTime', order: 'descending' }"
        @sort-change="handleAggSortChange"
        :header-cell-class-name="
        () => {
          return 'grid-header'
        }"
      >
        <el-table-column prop="uniqueQueryId" :label="$t('datasource.slowStaticTable[0]')" width="120" fixed="left" :sortable="true" />
        <el-table-column prop="sqlTemplate" :label="$t('datasource.slowStaticTable[1]')" min-width="250" fixed="left" :sortable="true">
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
        <el-table-column prop="executeNum" :label="$t('datasource.slowStaticTable[2]')" width="110" :sortable="true" />
        <el-table-column prop="totalExecuteTime" :label="$t('datasource.slowStaticTable[3]')" width="120" :sortable="true" />
        <el-table-column prop="avgExecuteTime" :label="$t('datasource.slowStaticTable[4]')" width="130" :sortable="true" />
        <el-table-column prop="totalScanRows" :label="$t('datasource.slowStaticTable[5]')" width="110" :sortable="true" />
        <el-table-column prop="avgScanRows" :label="$t('datasource.slowStaticTable[6]')" width="120" :sortable="true" />
        <el-table-column prop="totalRandomScanRows" :label="$t('datasource.slowStaticTable[7]')" width="130" :sortable="true" />
        <el-table-column prop="avgRandomScanRows" :label="$t('datasource.slowStaticTable[8]')" width="140" :sortable="true" />
        <el-table-column prop="totalOrderScanRows" :label="$t('datasource.slowStaticTable[9]')" width="130" :sortable="true" />
        <el-table-column prop="avgOrderScanRows" :label="$t('datasource.slowStaticTable[10]')" width="140" :sortable="true" />
        <el-table-column prop="avgReturnRows" :label="$t('datasource.slowStaticTable[11]')" width="120" :sortable="true" />
        <el-table-column prop="avgLockTime" :label="$t('datasource.slowStaticTable[12]')" width="130" :sortable="true" />
        <el-table-column prop="firstExecuteTime" :label="$t('datasource.slowStaticTable[13]')" width="140" align="center" :sortable="true">
          <template #default="scope">
            <span>{{ dayjs.utc(scope.row.firstExecuteTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="finalExecuteTime" :label="$t('datasource.slowStaticTable[14]')" width="140" align="center" :sortable="true">
          <template #default="scope">
            <span>{{ dayjs.utc(scope.row.finalExecuteTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:currentPage="aggPage.currentPage"
        v-model:pageSize="aggPage.pageSize"
        :total="aggPage.total"
        :page-sizes="[10, 20, 30, 40]"
        class="pagination"
        layout="total,sizes,prev,pager,next"
        background
        small
        @size-change="aggHandleSizeChange"
        @current-change="aggHandleCurrentChange"
      />
    </el-tab-pane>
    <el-tab-pane :label="$t('datasource.detail')" :name="3">
      <template #label>
        <span>{{ $t("datasource.detail") }}
        </span>
      </template>
      <el-table
        size="small"
        :data="tableDatas"
        style="width: 100%"
        v-loading="loading"
        :default-sort="{ prop: 'startTime', order: 'descending' }"
        @sort-change="handleSortChange"
        :header-cell-class-name="
          () => {
            return 'grid-header'
          }
        "
      >
        <el-table-column prop="debugQueryId" :label="$t('datasource.sqlID')" width="130" fixed="left" :sortable="true">
        <template #default="scope">
            <a class="table-wrapper-table-id" @click="gotoSqlDetail(scope.row.debugQueryId)">{{ scope.row.debugQueryId }}</a>
        </template>
        </el-table-column>
        <el-table-column prop="sqlTemplate" :label="$t('datasource.slowLogTable[2]')" min-width="250" fixed="left" :sortable="true">
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
        <el-table-column prop="startTime" :label="$t('datasource.slowLogTable[0]')" width="140" align="center" :sortable="true">
          <template #default="scope">
            <span>{{ dayjs.utc(scope.row.startTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" :label="$t('datasource.slowLogTable[1]')" width="140" align="center" :sortable="true">
          <template #default="scope">
            <span>{{ dayjs.utc(scope.row.finishTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dbName" :label="$t('datasource.slowLogTable[3]')" width="80" :sortable="true" />
        <el-table-column prop="clientAddr" :label="$t('datasource.slowLogTable[4]')" width="110" :sortable="true" />
        <el-table-column prop="schemaName" :label="$t('datasource.slowLogTable[5]')" width="100" :sortable="true" />
        <el-table-column prop="dbTime" :label="$t('datasource.slowLogTable[6]')" width="100" :sortable="true" />
        <el-table-column prop="cpuTime" :label="$t('datasource.slowLogTable[7]')" width="80" :sortable="true" />
        <el-table-column prop="dataIoTime" :label="$t('datasource.slowLogTable[8]')" width="70" :sortable="true" />
        <el-table-column prop="parseTime" :label="$t('datasource.slowLogTable[9]')" width="90" :sortable="true" />
        <el-table-column prop="plExecutionTime" :label="$t('datasource.slowLogTable[10]')" width="120" :sortable="true" />
        <el-table-column prop="lockWaitTime" :label="$t('datasource.slowLogTable[11]')" width="90" :sortable="true" />
        <el-table-column prop="nTuplesReturned" :label="$t('datasource.slowLogTable[12]')" width="120" :sortable="true" />
        <el-table-column prop="nreturnedRows" :label="$t('datasource.slowLogTable[13]')" width="100" :sortable="true" />
        <el-table-column prop="uniqueQueryId" :label="$t('datasource.slowLogTable[14]')" width="120" :sortable="true" />
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
    </el-tab-pane>
  </el-tabs>

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
import { i18n } from '@/i18n'
import LazyLine from "@/components/echarts/LazyLine.vue";
import { useMonitorStore } from "@/store/monitor";
import { toFixed } from "@/shared";
import { storeToRefs } from "pinia";
import { getInstanceInfo } from "@/api/observability";
import { useIntervalTime } from "@/hooks/time";
import { tabKeys } from "@/api/common";
const { t } = useI18n()
import moment from "moment"


dayjs.extend(utc)
dayjs.extend(timezone)

const clusterNodeId = ref()
const placeholders = ref<string>("")

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
  schemaName: string
}
const emit = defineEmits(['addTask'])
const tableDatas = ref<Array<any>>([])
const tableAggDatas = ref<Array<any>>([])
const aggLoading = ref(false);
const dbList = ref<Array<any>>([])
const searched = ref(false)
const cluster = ref<Array<any>>([])
const addTaskParam = ref<AddTaskParam>({
  sqlText: '',
  clusterId: '',
  nodeId: '',
  dbName: '',
  schemaName: ''
})

const handleModal = (row: any) => {
  addTaskParam.value = {
    sqlText: row.sqlTemplate,
    clusterId: row.clusterId,
    nodeId: row.nodeId,
    dbName: row.dbName,
    schemaName: row.schemaName
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
  orderByColumn : 'startTime',
  isAsc : 'desc'
})
const aggPage = reactive({
  pageSize: 10,
  currentPage: 1,
  total: 0,
  orderByColumn : 'totalExecuteTime',
  isAsc : 'desc'
})
const queryData = computed(() => {
  const { dbName, dateValue } = formData
  const { pageSize: row, currentPage: current, orderByColumn: column, isAsc : order} = page
  const queryObj = {
    startTime: dateValue.length ? dateValue[0] : null,
    finishTime: dateValue.length ? dateValue[1] : null,
    dbName,
    pageNum: current,
    pageSize: row,
    nodeId: cluster.value.length ? cluster.value[1] : '',
    queryCount: true,
    orderByColumn: column,
    isAsc: order
  }
  return queryObj
})
const queryAggData = computed(() => {
  const { dbName, dateValue } = formData
  const { pageSize: row, currentPage: current, orderByColumn: column, isAsc : order } = aggPage
  const queryObj = {
    startTime: dateValue.length ? dateValue[0] : null,
    finishTime: dateValue.length ? dateValue[1] : null,
    dbName,
    pageNum: current,
    pageSize: row,
    nodeId: cluster.value.length ? cluster.value[1] : '',
    queryCount: true,
    orderByColumn: column,
    isAsc: order
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
  aggPage.currentPage = 1
  requestData()
  requestAggData()
  load();
}
const handleReset = () => {
  page.currentPage = 1
  aggPage.currentPage = 1
  formData.dateValue = []
  if (!queryData.value.nodeId) {
    ElMessage.warning(t('datasource.pleaseSelectInstance'))
    return
  }
  requestData()
  requestAggData()
  requestMetricData()
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

const aggChangePageCurrent = (data: number) => {
  Object.assign(aggPage, data)
  requestAggData()
}

const handleSortChange = ({ column, prop, order }) => {
   const sortedProp = prop;
   const sortOrder = order === 'ascending' ? 'asc' : 'desc';
   page.currentPage = 1
   page.orderByColumn = sortedProp,
   page.isAsc = sortOrder
   tableDatas.value = []
   requestData()
};

const handleAggSortChange = ({ column, prop, order }) => {
   const sortedProp = prop;
   const sortOrder = order === 'ascending' ? 'asc' : 'desc';
   aggPage.currentPage = 1
   aggPage.orderByColumn = sortedProp,
   aggPage.isAsc = sortOrder
   tableAggDatas.value = []
   requestAggData()
};

const { data: ret, run: dbData } = useRequest(
  (nodeId: string) => {
    return ogRequest.get('/sqlDiagnosis/api/v1/clusters/' + nodeId + '/instances', '')
  },
  { manual: true }
)

const { loading, data: res, run: requestData } = useRequest(
  () => {
    const clusterId = cluster.value.length ? cluster.value[0] : ''
    const nodeId = queryData.value.nodeId
    return ogRequest
      .get('/sqlDiagnosis/api/v1/slowSqls', { ...queryData.value })
      .then(function (res) {
        res.data.records.forEach((element: { clusterId: any; nodeId: any }) => {
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
    tableDatas.value = res.data.records
    Object.assign(page, { pageSize: page.pageSize, total: res.data.total })
  } else {
    tableDatas.value = []
    Object.assign(page, { pageSize: page.pageSize, total: 0, currentPage: 1 })
  }
})

const { data: aggResult, run: requestAggData } = useRequest(
  () => {
    aggLoading.value = true;
    const clusterId = cluster.value.length ? cluster.value[0] : ''
    const nodeId = queryAggData.value.nodeId
    return ogRequest
      .get('/sqlDiagnosis/api/v1/slowSqls/aggData', { ...queryAggData.value })
      .then(function (res) {
        res.data.records.forEach((element: { clusterId: any; nodeId: any }) => {
          element.nodeId = nodeId
          element.clusterId = clusterId
        })
        return res
      })
      .catch(function (res) {
        tableAggDatas.value = []
        Object.assign(aggPage, { pageSize: aggPage.pageSize, total: 0, currentPage: 1 })
      })
      .finally(function () {
        aggLoading.value = false;
      });
  },
  { manual: true }
)
type aggRes =
  | {
      records: string[]
      pageNum: number
      total: number
    }
  | undefined
watch(aggResult, (aggResult: aggRes) => {
  if (aggResult && Object.keys(aggResult).length) {
    tableAggDatas.value = aggResult.data.records
    Object.assign(aggPage, { pageSize: aggPage.pageSize, total: aggResult.data.total })
  } else {
    tableAggDatas.value = []
    Object.assign(aggPage, { pageSize: aggPage.pageSize, total: 0, currentPage: 1 })
  }
})

const props = withDefaults(defineProps<{ tabId: string }>(), {});
const tab = 2;

interface LineData {
  name: string;
  data: any[];
  [other: string]: any;
}
interface MetricsData {
  slowSQL: LineData[];
  time: string[];
  message: string;
  threshold: string;
}
const metricsData = ref<MetricsData>({
  slowSQL: [],
  time: [],
  message: '',
  threshold: ''
});
const {
  updateCounter,
  sourceType,
  autoRefreshTime,
  tabNow,
  nodeId,
} = storeToRefs(useMonitorStore(props.tabId));

const sqlPeroid = ref([])
const sqlFrequency = ref([])

const peroidOptions = computed(() => [{
  value: 'day',
  label: t('datasource.byDay'),
  children: Array.from({ length: 29 }).map((item, index) => ({
    value: `${index + 1}day`,
    label: (index + 1) + t('dashboard.day'),
  }))
},
{
  value: 'month',
  label: t('datasource.byMonth'),
  children: Array.from({ length: 12 }).map((item, index) => ({
    value: `${index + 1}month`,
    label: (index + 1) + t('dashboard.month'),
  }))
}])

const frequencyOptions = computed(() => [{
  value: 's',
  label: t('datasource.bySecond'),
  children: Array.from({ length: 30 }).map((item, index) => ({
    value: `${index + 30}s`,
    label: (index + 30) + t('dashboard.second'),
  }))
},
{
  value: 'm',
  label: t('datasource.byMinute'),
  children: Array.from({ length: 59 }).map((item, index) => ({
    value: `${index + 1}m`,
    label: (index + 1) + t('dashboard.minute'),
  }))
},
{
  value: 'h',
  label: t('datasource.byHour'),
  children: Array.from({ length: 24 }).map((item, index) => ({
    value: `${index + 1}h`,
    label: (index + 1) + t('dashboard.hour'),
  }))
}])

const getSqlDefaultConfig = () => {
  ogRequest.get('/sqlDiagnosis/api/v1/getDefault').then((res) => {
    sqlPeroid.value = [res.peroid?.match(/[a-zA-Z]+$/)?.[0], res.peroid]
    sqlFrequency.value = [res.frequency.slice(-1)[0], res.frequency]
  })
}
const setSqlConfig = (type: 'peroid' | 'frequency') => {
  if (type === 'peroid') {
    const splitToArray = () => {
      return sqlPeroid.value.slice(-1)[0].match(/[a-zA-Z]+|\d+/g) || [];
    };
    const periodArray = splitToArray()
    if (periodArray.length === 2) {
      let formattedPeriod = periodArray[0]
      if (periodArray[1] === 'month') {
        formattedPeriod = formattedPeriod + 'm'
      } else {
        formattedPeriod = formattedPeriod + 'd'
      }
      ogRequest.post(`/sqlDiagnosis/api/v1/peroid?peroid=${formattedPeriod}`).then(() => {
        ElMessage.success(t('app.saveSuccess'))
      })
    } else {
      ElMessage.error(t('app.saveFail'))
    }
  }
  if (type === 'frequency') {
    ogRequest.post(`/sqlDiagnosis/api/v1/frequency?frequency=${sqlFrequency.value.slice(-1)[0]}`).then(() => {
      ElMessage.success(t('app.saveSuccess'))
    })
  }
}

// same for every page in index
const timer = ref<number>();
onMounted(() => {
  getSqlDefaultConfig()
  load();
});
watch(
  updateCounter,
  () => {
    clearInterval(timer.value);
    if (tabNow.value === tabKeys.InstanceMonitorInstanceInfo) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) {
        load();
      }
      if (updateCounter.value.source === sourceType.value.MANUALREFRESH) load();
      if (updateCounter.value.source === sourceType.value.TIMETYPE) load();
      if (updateCounter.value.source === sourceType.value.TIMERANGE) load();
      if (updateCounter.value.source === sourceType.value.TABCHANGE) load();
      const time = autoRefreshTime.value;
      timer.value = useIntervalTime(
        () => {
          load();
        },
        computed(() => time * 1000)
      );
    }
  },
  { immediate: false }
);

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
  const nodeId = queryData.value.nodeId
  if (!nodeId) return;
  requestMetricData();
};
const { data: indexData, run: requestMetricData } = useRequest(
  () => {
    const clusterId = cluster.value.length ? cluster.value[0] : '';
    const instanceId = queryData.value.nodeId;
    const sTime = queryAggData.value.startTime;
    const eTime = queryAggData.value.finishTime;
    const dbName = queryData.value.dbName;
    let start = 0
    let end = 0
    let step = 0
    if(sTime === null || eTime === null){
      const _time = moment()
      end = Number.parseInt(`${_time.subtract(60, 'second').toDate().getTime() / 1000}`)
      start = Number.parseInt(`${_time.subtract(24 * 7, 'hour').toDate().getTime() / 1000}`)
    }else{
      start = Number.parseInt(`${new Date(sTime).getTime() / 1000}`)
      end= Number.parseInt(`${new Date(eTime).getTime() / 1000}`)
    }
    step = Math.max(14, Number.parseInt(`${Math.round((end - start) / 260)}`))
    return ogRequest.get('/sqlDiagnosis/api/v1/slowSqls/chart', {
      id: instanceId,
      start: start,
      end: end,
      step: step,
      dbName: dbName
    })
    .then(function (res) {
      if (res.msg != null && res.msg != 'success') {
        placeholders.value = res.msg
      }else{
        placeholders.value = 'success'
        return res
      }
    })
    .catch(function (res) {
      placeholders.value = 'error'
    })
  }, {
  manual: true,
});
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.slowSQL = [];
    metricsData.value.threshold = '';

    const baseData = indexData.value;
    if (!baseData) return;

    // tip info
    metricsData.value.message = baseData.msg

    // slow SQL
    slowSQL3sInfo.value.option = []
    if (baseData.data.INSTANCE_DB_SLOWSQL) {
      let tempData: string[] = [];
      baseData.data.INSTANCE_DB_SLOWSQL.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.slowSQL.push({
        data: tempData,
        name: queryData.value.dbName ? queryData.value.dbName + t("instanceMonitor.instance.dbSlowSQLQty") : t("instanceMonitor.instance.slowSQLQty"),
      });
      slowSQL3sInfo.value.option.push({ name: t("instanceMonitor.instance.slowSQLQty"), value: t("instanceMonitor.instance.slowSQLQtyContent") })
    }

    // time
    metricsData.value.time = baseData.data.time;

    //threshold
    metricsData.value.threshold=baseData.data.slowSqlThreshold
  },
  { deep: true }
);

const slowSQL3s = ref();
const download = (title: string, ref: any) => {
  ref.download(title)
}

const slowSQL3sInfo = ref<any>({
  title: t("app.lineOverview"),
  option: []
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

const aggHandleSizeChange = (val: number) => {
  aggPage.currentPage = 1
  aggPage.pageSize = val
  aggChangePageCurrent(aggPage.currentPage)
}
const aggHandleCurrentChange = (val: number) => {
  aggPage.currentPage = val
  aggChangePageCurrent(aggPage.currentPage)
}

const getInstallInstance = async () => {
  try {
    const res = await ogRequest.get('/sqlDiagnosis/api/v1/plugin/instance');
    if(res.code === 200) {
      return res.data;
    }
  } catch (error) {
    console.error(error);
  }
}

const router = useRouter()
const gotoSqlDetail = async(debugQueryId: string) => {
  const result = await getInstallInstance()
  const instanceId = queryData.value.nodeId;
  if(!result){
     ElMessage.warning(t('datasource.pleaseInstallInstance'))
     return
  }else{
    window.$wujie?.props.methods.jump({
      name: `Static-pluginObservability-instanceVemSql_detail`,
      query: {
        dbid : instanceId,
        id : debugQueryId
      },
    })
  }
}

onMounted(() => {
  // @ts-ignore
  const wujie = window.$wujie
  if (wujie) {
    // Monitoring platform language change
    wujie?.bus.$on('opengauss-locale-change', (val: string) => {
      requestMetricData()
    })
  }
})
</script>

<style lang="scss" scoped>
.search-form {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  column-gap: 15px;
}
.form1, .form2 {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 4px;
}
.slow-log {
  &-chart {
    height: 200px;
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
      height: 200px;
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
.tab-wrapper {
  position: relative;
  &-filter {
    z-index: 10;
    position: absolute;
    padding-right: 16px;
    display: flex;
    align-items: center;
    right: 0;
    top: 0px;
    height: 32px;
    background-color: $og-sub-background-color;
    > div:not(:last-of-type),
    > span,
    > button {
      margin-right: 4px;
    }
  }
}
.message-error {
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  > div {
    font-size: 12px;
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 4px 12px;
    gap: 4px;
    border: 1px solid var(--border-2);
    border-radius: 2px;
  }

  &.center {
    position: unset;
    justify-content: center;
    flex-direction: row;
  }
}
</style>
