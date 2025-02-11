<template>
  <div class="app-container" id="replayList">
    <div class="main-bd">
      <div class="button-wrap">
        <div class="flex-between mb-s">
          <div class="operate-button">
            <div>
              <el-button type="primary" class="mr" @click="addRecordPlayBack('create')">创建录制回放</el-button>
              <el-popconfirm title="是否确认批量删除?" @confirm="deleteSelectedHosts">
                <template #reference>
                  <el-button type="primary" class="mr">批量删除</el-button>
                </template>
              </el-popconfirm>
              <el-button class="mr" @click="getListData">刷新</el-button>
            </div>
            <div class="switchside">
              <el-switch v-model="autoRefreshFlag" class="ml-2" inline-prompt
                         style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949;" size="large"
                         width="auto"
                         active-text="自动刷新" inactive-text="停止刷新" @change="autoRefreshList"/>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div>
      <fusion-search :label-options="labelOptions" @click-search="clickSearch"/>
    </div>
    <div class="packageList">
      <el-table :row-key="(row) => row.id" :data="list.data" @selection-change="handleSelected" style="width: 100vw">
        <el-table-column type="selection" width="40"/>
        <el-table-column :label="$t('id')" prop="id" :width="50" sortable/>
        <el-table-column :label="$t('任务名称')" prop="taskName" sortable>
          <template #default="{ row }">
            <el-button link type="primary" @click.prevent="goDetail(row)">{{ row.taskName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column :label="$t('数据库')" prop="dbName"/>
        <el-table-column :label="$t('执行状态')" prop="executionStatus" :width="100" sortable>
          <template #default="{ row }">
            <el-popover
              placement="top-start" :width="400" trigger="hover"
              :disabled="![TASKSTATE.DOWNLOADINGFAIL_NUMERIC, TASKSTATE.RUNNINGFAIL_NUMERIC].includes(row.executionStatus)"
              :content="row.errorMsg"
              append-to="#replayList"
              style="max-height: 30vh; max-width: 50vw; width: auto; overflow: auto;">
              <template #reference>
                <el-tag
                  :type="getTagType(row.executionStatus)"
                  effect="dark"
                  size="large"
                  style="width: 90%; font-size: 12px">
                  {{ getStatusText(row.executionStatus) }}
                </el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="$t('任务类型')" prop="taskType" :width="100">
          <template #default="{ row }">
            {{ getTypeText(row.taskType) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('慢sql')" prop="slowSqlCount" :width="70">
        </el-table-column>
        <el-table-column :label="$t('失败sql')" prop="failedSqlCount" :width="70">
        </el-table-column>
        <el-table-column :label="$t('任务耗时')" prop="taskDuration" sortable>
          <template #default="{ row }">
            <div v-if="row.taskDuration === 0">--</div>
            <div v-else> {{ formattedTime(row.taskDuration) }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('任务开始时间')" prop="taskStartTime" sortable/>
        <el-table-column :label="$t('任务完成时间')" prop="taskEndTime" sortable/>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click.prevent="startRows(scope.row)"
              v-if="scope.row.executionStatus !== TASKSTATE.RUNNING_NUMERIC"
              :disabled="isButtonDisabled(scope.row)">
              {{ getButtonText(scope.row.executionStatus) }}
            </el-button>
            <el-popconfirm
              title="是否确认停止执行?"
              @confirm="finishRows(scope.row)"
              v-else>
              <template #reference>
                <el-button link type="primary">停止执行</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="是否确认删除?" @confirm="deleteRows(scope.row)">
              <template #reference>
                <el-button link type="danger" :disabled="scope.row.executionStatus === TASKSTATE.RUNNING_NUMERIC">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination @size-change="pageSizeChange" @current-change="currentPage" :current-page="filter.pageNum"
                     :page-sizes="[10, 15, 20, 25]" :pager-count="11" :page-size="filter.pageSize"
                     layout="total, sizes, prev, pager, next, jumper" :total="list.total">
      </el-pagination>
    </div>
  </div>
</template>

<script setup>
import {Message} from '@arco-design/web-vue/es/index'
import {onMounted, reactive, ref, onUnmounted, watch, toRaw, defineProps, onBeforeUnmount} from 'vue'
import WujieVue from 'wujie-vue3'
import {useI18n} from 'vue-i18n'
import router from "@/router";
import {
  transcribeReplayDelete,
  transcribeReplayFinish,
  transcribeReplayList,
  transcribeReplayStart
} from "@/api/playback";
import showMessage from "@/utils/showMessage";
import FusionSearch from '@/components/fusion-search'
import {searchType} from '@/types/searchType'
import dayjs from 'dayjs'

const {t} = useI18n()
const {bus} = WujieVue

const TASKSTATE = {
  DOWNLOADINGFAIL: 'downloading_fail',
  DOWNLOADING: 'downloading',
  RUNNING: 'running',
  NOSTARTED: 'nostarted',
  FINISH: 'finish',
  RUNNINGFAIL: 'running_fail',
  DOWNLOADINGFAIL_NUMERIC: -2,
  DOWNLOADING_NUMERIC: -1,
  NOSTARTED_NUMERIC: 0,
  RUNNING_NUMERIC: 1,
  FINISH_NUMERIC: 2,
  RUNNINGFAIL_NUMERIC: 3,
}
const getTagType = (executionStatus) => {
  switch (executionStatus) {
    case TASKSTATE.NOSTARTED_NUMERIC:
    case TASKSTATE.DOWNLOADING_NUMERIC:
      return 'info';
    case TASKSTATE.RUNNING_NUMERIC:
      return 'primary';
    case TASKSTATE.FINISH_NUMERIC:
      return 'success';
    default:
      return 'danger';
  }
}

const getStatusText = (executionStatus) => {
  switch (executionStatus) {
    case TASKSTATE.DOWNLOADING_NUMERIC:
      return '正在下载'
    case TASKSTATE.DOWNLOADINGFAIL_NUMERIC:
      return '下载失败'
    case TASKSTATE.RUNNINGFAIL_NUMERIC:
      return '执行失败'
    case TASKSTATE.RUNNING_NUMERIC:
      return '执行中';
    case TASKSTATE.FINISH_NUMERIC:
      return '已完成'
    default:
      return '未执行'
  }
}

const getTypeText = (executionStatus) => {
  switch (executionStatus) {
    case 'transcribe_replay':
      return '录制回放'
    case 'transcribe':
      return '仅录制'
    default:
      return '仅回放'
  }
}

const isButtonDisabled = (row) => {
  return row.executionStatus === TASKSTATE.DOWNLOADING_NUMERIC || row.executionStatus === TASKSTATE.DOWNLOADINGFAIL_NUMERIC;
}

const getButtonText = (executionStatus) => {
  switch (executionStatus) {
    case TASKSTATE.NOSTARTED_NUMERIC:
      return '执行任务'
    case TASKSTATE.DOWNLOADINGFAIL_NUMERIC:
    case TASKSTATE.DOWNLOADING_NUMERIC:
      return '无法执行'
    default:
      return '重新执行'
  }
}

const list = reactive({
  data: [],
  selectedpackageIds: [],
  total: 0,
  loading: false,
  tagsLoading: false,
  tagsList: [],
  socketArr: []
})
const data = ref({
  selectedData: []
})

const addRecordPlayBack = (type) => {
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationCreatetranscribetask`,
  })
}

const handleSelected = (keys) => {
  list.selectedpackageIds = keys
  data.value.selectedData = []
  list.selectedpackageIds.forEach((item) => {
    data.value.selectedData.push((toRaw(item).id))
  })
}

const startRows = (record) => {
  let templist = []
  templist.push(record.id)
  transcribeReplayStart(templist).then((res) => {
    if (Number(res.code) === 200) {
      showMessage.success({
        content: t('components.Package.5mtcyb0rty29')
      })
    }
  }).catch(error => {
    console.log(error)
  }).finally(() => {
    getListData()
  })
  setTimeout(getListData(), 2000)
}

const finishRows = (record) => {
  let templist = []
  templist.push(record.id)
  transcribeReplayFinish(templist).then((res) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: t('中止成功')
      })
    }
  }).catch(error => {
    console.log(error)
  }).finally(() => {
    getListData()
  })
}

const deleteRows = (record) => {
  let templist = []
  templist.push(record.id)
  transcribeReplayDelete(templist).then((res) => {
    if (Number(res.code) === 200) {
      showMessage('success', '删除成功')
    }
  }).catch(error => {
    console.error(error)
  }).finally(() => {
    getListData()
  })
}

const deleteSelectedHosts = () => {
  let selectedRecord = []
  data.value.selectedData.forEach(item => {
    selectedRecord.push(item)
  })
  if (selectedRecord.length > 0) {
    deleteMultipleRows(selectedRecord)
  } else {
    showMessage('warning', '请至少选择一个任务')
  }
}

const deleteMultipleRows = (records) => {
  transcribeReplayDelete(records).then((res) => {
    if (Number(res.code) === 200) {
      showMessage('success', '删除成功')
    } else {
      showMessage('error', '删除失败')
    }
    list.selectedpackageIds = []
    data.value.selectedData = {}
  }).catch(error => {
    console.log('delete error:', error)
  }).finally(() => {
    getListData()
  })
}

const formattedTime = (taskDuration) => {
  let time = taskDuration / 1000
  const days = Math.floor(time / (3600 * 24))
  time %= (3600 * 24)
  const hours = Math.floor(time / 3600)
  time %= 3600
  const minutes = Math.floor(time / 60)
  time %= 60
  const seconds = Math.floor(time)
  return `${days}天${hours}小时${minutes}分${seconds}秒`
}


const labelOptions = ref({
  executionStatus: {
    label: '执行状态',
    value: 'executionStatus',
    placeholder: '请选择执行状态',
    selectType: searchType.SELECT,
    options: [
      {
        value: TASKSTATE.DOWNLOADING_NUMERIC,
        label: '下载中'
      },
      {
        value: TASKSTATE.DOWNLOADINGFAIL_NUMERIC,
        label: '下载失败'
      },
      {
        value: TASKSTATE.FINISH_NUMERIC,
        label: '已完成'
      },
      {
        value: TASKSTATE.NOSTARTED_NUMERIC,
        label: '未执行'
      },
      {
        value: TASKSTATE.RUNNINGFAIL_NUMERIC,
        label: '执行失败'
      },
      {
        value: TASKSTATE.RUNNING_NUMERIC,
        label: '执行中'
      }
    ]
  },
  taskName: {
    label: '任务名称',
    value: 'taskName',
    placeholder: '请输入任务名称',
    selectType: searchType.INPUT
  },
  taskDateRange: {
    label: '任务时间范围',
    value: 'taskDateRange',
    placeholder: '请选择日期范围',
    selectType: searchType.DATERANGE
  },
})

const clickSearch = (params) => {
  const {taskDateRange, executionStatus, taskName} = params;
  filter.executionStatus = executionStatus;
  filter.taskName = taskName;
  filter.taskStartTime = taskDateRange?.length ? dayjs(taskDateRange[0]).format("YYYY-MM-DD HH:MM:ss") : undefined;
  filter.taskEndTime = taskDateRange?.length ? dayjs(taskDateRange[1]).format("YYYY-MM-DD HH:MM:ss") : undefined;
  getListData()
}

const goDetail = row => {
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationTranscribetaskDetail`,
    query: {
      id: row.id
    }
  })
}


const filter = reactive({
  pageNum: 1,
  pageSize: 10,
  executionStatus: undefined,
  taskName: undefined,
  taskStartTime: undefined,
  taskEndTime: undefined,
})
const currentPage = (e) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e) => {
  filter.pageSize = e
  getListData()
}

const autoRefreshFlag = ref(false)
let intervalId = null
const autoRefreshList = (value) => {
  if (value) {
    if (intervalId === null) {
      intervalId = setInterval(getListData, 6000)
    }
  } else {
    if (intervalId !== null) {
      clearInterval(intervalId);
      intervalId = null
    }
  }
}

onBeforeUnmount(() => {
  if (intervalId !== null) {
    clearInterval(intervalId);
  }
})


const getListData = () => {

  list.loading = true
  transcribeReplayList({
    ...filter

  }).then(res => {
    if (Number(res.code) === 200) {
      list.data = []
      const tempTask = {
        id: '',
        taskName: '',
        dbName: '',
        taskType: '',
        executionStatus: '',
        slowSqlCount: '',
        failedSqlCount: '',
        taskDuration: '',
        taskStartTime: '',
        taskEndTime: '',
        errorMsg: '',
      }

      res.rows.forEach(item => {
        tempTask.id = item.id
        tempTask.taskName = item.taskName
        tempTask.dbName = item.dbName
        tempTask.taskType = item.taskType
        tempTask.executionStatus = item.executionStatus
        tempTask.slowSqlCount = item.slowSqlCount
        tempTask.failedSqlCount = item.failedSqlCount
        tempTask.taskDuration = item.taskDuration
        tempTask.taskStartTime = item.taskStartTime
        tempTask.taskEndTime = item.taskEndTime
        tempTask.errorMsg = item.errorMsg
        list.data.push({...tempTask})
      })
      list.total = res.total
    }
  }).catch(error => {
    console.log('get data error:', error)
  }).finally(() => {
    list.loading = false
  })
}

const init = () => {
  getListData()
}

init()

const dataColor = reactive({
  selectedData: {},
  hasTestObj: {},
  colorYellow: '#FCEF92',
  colorRed: '#E41D1D'
})

onMounted(() => {
  dataColor.hasTestObj = {}
  bus.$on('opengauss-theme-change', (val) => {
    if (val === 'dark') {
      changeEchartsColor('dark')
    } else {
      changeEchartsColor('light')
    }
  })
})

const changeEchartsColor = (type) => {
  list.dataColor.forEach((item) => {
    if (type === 'dark') {
      item.cpuOption.color[1] = dataColor.colorYellow
      item.diskOption.color[1] = dataColor.colorYellow
      item.memoryOption.color[1] = dataColor.colorYellow
    } else {
      item.cpuOption.color[1] = dataColor.colorRed
      item.diskOption.color[1] = dataColor.colorRed
      item.memoryOption.color[1] = dataColor.colorRed
    }
  })
}
</script>

<style lang="less" scoped>
@import '@/assets/style/openGlobal.less';

:deep(.arco-form-layout-inline) {
  flex-wrap: nowrap;
}

.button-wrap {
  margin: 24px 0 16px;

  .operate-button {
    display: flex;
    flex-wrap: nowrap;
    justify-content: space-between;

    .switchside {
      display: flex;
      justify-content: flex-end;
      align-items: center;
    }
  }
}

.app-container {
  padding: 0 15px;

  .main-bd {
    min-height: auto;
  }

  .packageList {
    margin-top: 16px
  }
}

.popovermaxheight {
  max-height: 30vh;
  max-width: 50vw;
  width: auto;
  overflow: auto;
}
</style>
