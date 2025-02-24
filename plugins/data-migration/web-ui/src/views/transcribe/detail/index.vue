
<template>
  <div class="common-layout" style="background-color: #F4F6FA">
    <div class="top-left">
      <div class="grid-content">
        <div class="icon-text-container">
          <div class="icon-container">
            <img src="@/assets/images/list.png" width="70" alt="">
          </div>
          <div class="text-label-container">
            <el-tooltip :content="list.name" placement="top">
              <div class="text-content">
                {{ list.name }}
              </div>
            </el-tooltip>
            <div class="tag-container">
              <el-tag :type="getTagType(list.status)" effect="dark" size="large">
                {{ getStatusText(list.status) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="top-right">
      <div>
        <div class="icon-text-container" style="height: 100%; margin-right: 10px">
          <div class="text-label-container text-same-line">
            <span class="list-title">{{ list.parsedNum }}</span>
            <span style="font-size: 16px;"> {{ $t('transcribe.detail.parsedData') }}</span>
          </div>
          <div class="text-label-container text-same-line">
            <span class="list-title">{{ list.replayedNum }}</span>
            <span style="font-size: 16px;"> {{ $t('transcribe.detail.replayedData') }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom-left">
      <div class="grid-content">
        <div class="icon-text-container" style=" min-width: 250px;max-width: 300px;">
          <div class="text-label-container basic-info">
            <div class="info-title">
              <span>{{ $t('transcribe.detail.baskInfo') }}</span>
            </div>
            <el-form :data="list" label-position="left" label-width="110">
              <el-form-item :label="$t('transcribe.index.taskname')"><span>{{ list.name || '--' }}</span></el-form-item>
              <el-form-item :label="$t('transcribe.detail.taskId')"><span>{{ pageId || '--' }}</span></el-form-item>
              <el-form-item :label="$t('transcribe.index.tasktype')"><span>{{ getTypeText(list.taskType)
              }}</span></el-form-item>
              <el-form-item :label="$t('transcribe.index.taskDuration')"><span>{{ formattedTime(list.taskDuration)
              }}</span></el-form-item>
              <el-form-item :label="$t('transcribe.index.taskStartTime')"><span>{{ list.startTime || '--'
              }}</span></el-form-item>
              <el-form-item :label="$t('transcribe.detail.taskEndTime')"><span>{{ list.endTime || '--'
              }}</span></el-form-item>
              <el-form-item :label="t('transcribe.detail.sourceIp')"><span>{{ list.sourceIp || '--'
              }}</span></el-form-item>
              <el-form-item :label="t('transcribe.detail.targetIp')"><span>{{ list.targetIp || '--'
              }}</span></el-form-item>
              <el-table :data="dbMapData" border class="dbMapTable">
                <el-table-column :label="$t('transcribe.create.dbrelationship')" align="center">
                  <el-table-column prop="source" :label="$t('transcribe.detail.source')"></el-table-column>
                  <el-table-column prop="target" :label="$t('transcribe.detail.target')"></el-table-column>
                </el-table-column>
              </el-table>
            </el-form>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom-right">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick" :stretch="true"
        style="background-color: #F4F6FA; padding-bottom: 20px">
        <el-tab-pane :label="$t('transcribe.index.slowsql')" name="slowquery" />
        <el-tab-pane :label="$t('transcribe.detail.failSql')" name="failingquery" />
        <el-tab-pane :label="$t('transcribe.detail.durationComparison')" name="durationCompare"
          v-if="list.status === TASKSTATE.FINISH_NUMERIC" />
      </el-tabs>
      <div class="tab-content" v-if="activeName === 'durationCompare'">
        <sql-compare :id="pageId"></sql-compare>
      </div>
      <div class="tab-content" v-if="activeName !== 'durationCompare'">
        <div class=" table-container">
          <div class="control-wrap">
            <div class="search-wrap">
              <el-input type="text" prefix-icon="el-icon-search" v-model="searchText" :placeholder=defaultPlaceholder
                class="search-box" clearable @enter="handleSearch" />
              <el-button :icon="IconSearch" type="primary" @click="handleSearch">{{ $t('transcribe.detail.search')
              }}</el-button>
            </div>
            <div class="switchside">
              <el-switch v-model="autoRefreshFlag" class="ml-2" inline-prompt size="large"
                :active-text="$t('transcribe.index.autorefresh')" :inactive-text="$t('transcribe.index.stoprefresh')"
                @change="autoRefreshList" />
            </div>
          </div>

          <el-table row-key="sqlId" :data="list.data" :style="{ width: tableWidth }">
            <template v-for="(item, index) in tableHead">
              <el-table-column v-if="item.columnname === 'sqlStr'" :prop="item.columnname" :label="item.columncomment"
                :style="div1Width" :show-overflow-tooltip="{ 'popper-class': 'sql-tooltip' }">
              </el-table-column>
              <el-table-column v-else :prop="item.columnname" :label="item.columncomment" :style="div1Width" />
            </template>
          </el-table>
        </div>
        <div class="pagination-container">
          <el-pagination @size-change="pageSizeChange" @current-change="currentPage" :current-page="filter.pageNum"
            :page-sizes="[10, 15, 20, 25]" :page-size="filter.pageSize" :total="list.totalNum"
            layout="total, sizes, prev, pager, next, jumper" />
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { computed, nextTick, ref, onBeforeUnmount } from "vue";
import { getFailSql, getSlowSql, transcribeReplayList, getHostInfo } from "@/api/playback";
import SqlCompare from "./sqlCompare.vue"
import { IconSearch } from '@computing/opendesign-icons'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

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
      return t('transcribe.index.downloading')
    case TASKSTATE.DOWNLOADINGFAIL_NUMERIC:
      return t('transcribe.index.downloadfailed')
    case TASKSTATE.RUNNINGFAIL_NUMERIC:
      return t('transcribe.index.executionfailed')
    case TASKSTATE.RUNNING_NUMERIC:
      return t('transcribe.index.executing')
    case TASKSTATE.FINISH_NUMERIC:
      return t('transcribe.index.complete')
    default:
      return t('transcribe.index.noexecuted')
  }
}

const dbMapData = ref([])
const getTypeText = (executionStatus) => {
  switch (executionStatus) {
    case 'transcribe_replay':
      return t('components.SubTaskDetail.recordPlayback')
    case 'transcribe':
      return t('transcribe.index.transcribe')
    default:
      return t('transcribe.index.replay')
  }
}

const activeName = ref('slowquery')
const searchText = ref()
const defaultPlaceholder = computed(() => {
  if (activeName.value === 'slowquery') {
    return t('transcribe.detail.slowSqlPlaceholder')
  } else {
    return t('transcribe.detail.failSqlPlaceholder')
  }
})
const tableHead = computed(() => {
  if (activeName.value === 'slowquery') {
    return [
      { columnname: 'sqlStr', columncomment: 'SQL' },
      { columnname: 'sourceDuration', columncomment: t('transcribe.detail.sourceDuration') },
      { columnname: 'targetDuration', columncomment: t('transcribe.detail.targetDuration') },
      { columnname: 'countStr', columncomment: t('transcribe.detail.count') }
    ]
  } else {
    return [
      { columnname: 'sql', columncomment: 'SQL' },
      { columnname: 'replayedNum', columncomment: t('components.SubTaskDetail.failReason') }
    ]
  }
})
const tableWidth = ref('100%')
const tableRef = ref(null)

const handleClick = async () => {
  await nextTick(() => {
    getListData()
  })
}

const list = ref({
  id: 0,
  name: '',
  dbMap: '',
  targetDB: '',
  status: '',
  taskType: '',
  slowsqlQuery: 0,
  failingsqlQuery: 0,
  taskDuration: 0,
  sourceDuration: 0,
  targetDuration: 0,
  startTime: '',
  endTime: '',
  parsedNum: '',
  replayedNum: '',
  data: [],
  totalNum: 0,
  sourceIp: '',
  targetIp: '',
})
const filter = {
  pageNum: 1,
  pageSize: 10,
}

const currentPage = (e) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e) => {
  filter.pageSize = e
  getListData()
}

const div1Width = computed(() => {
  let arrLength = tableHead.value.length;
  return arrLength ? `${(100 / arrLength).toFixed(2)}%` : 'auto';
});

const getListData = async () => {
  let tempFilter = { ...filter }
  if (searchText.value) {
    tempFilter['sql'] = searchText.value
  }
  if (activeName.value === 'slowquery') {
    getSlowSql(list.value.id, tempFilter).then(res => {
      if (Number(res.code) === 200) {
        list.value.data = []
        res.rows.forEach(item => {
          const tempPackage = {
            sqlStr: item.sqlStr,
            sourceDuration: item.sourceDuration,
            targetDuration: item.targetDuration,
            explainStr: item.explainStr,
            countStr: item.countStr
          }
          list.value.data.push({ ...tempPackage })
        })
        list.value.totalNum = res.total
      }
    }).catch(error => {
    })
  } else {
    getFailSql(list.value.id, filter).then(res => {
      if (Number(res.code) === 200) {
        list.value.data = []
        res.rows.forEach(item => {
          const tempPackage = {
            sql: item.sql,
            replayedNum: item.message,
          }
          list.value.data.push({ ...tempPackage })
        })
        list.value.totalNum = res.total
      }
    }).catch(error => {
    })
  }
  await nextTick(() => {
    const table = tableRef.value;
    if (table) {
      tableWidth.value = `${table.$el.offsetWidth}px`;
      table.doLayout();
    }
  });
}

const handleSearch = () => {
  getListData()
}
const autoRefreshFlag = ref(false)
let intervalId = null
const autoRefreshList = (value) => {
  if (value) {
    if (intervalId === null) {
      intervalId = setInterval(() => {
        getTaskInfo();
      }, 6000)
    }
  } else {
    if (intervalId !== null) {
      clearInterval(intervalId);
      intervalId = null;
    }
  }
}

onBeforeUnmount(() => {
  if (intervalId !== null) {
    clearInterval(intervalId);
    intervalId = null;
  }
})

const getTaskInfo = () => {
  let tempFilter = { ...filter }
  tempFilter['taskId'] = list.value.id
  transcribeReplayList(tempFilter).then(res => {
    if (Number(res.code) === 200 && res.total === 1) {
      list.value.name = res.rows[0].taskName
      list.value.status = res.rows[0].executionStatus
      list.value.slowsqlQuery = res.rows[0].slowSqlCount
      list.value.failingsqlQuery = res.rows[0].failedSqlCount
      list.value.taskDuration = res.rows[0].taskDuration
      list.value.sourceDuration = res.rows[0].sourceDuration
      list.value.targetDuration = res.rows[0].targetDuration
      list.value.startTime = res.rows[0].taskStartTime?.replaceAll('-', '/')
      list.value.endTime = res.rows[0].taskEndTime?.replaceAll('-', '/')
      list.value.parsedNum = res.rows[0].parseNum
      list.value.replayedNum = res.rows[0].replayNum
      list.value.taskType = res.rows[0].taskType
      dbMapData.value = res.rows[0].dbMap?.map(e => {
        const mapItem = e.split(':')
        return {
          source: mapItem[0],
          target: mapItem[1]
        }
      })
      if (list.value.status === TASKSTATE.FINISH_NUMERIC || list.value.status === TASKSTATE.RUNNING_NUMERIC) {
        getListData()
      }
    }

  }).catch(error => {
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
  return t('transcribe.index.daytime', [days, hours, minutes, seconds])
}

const getHostIp = async () => {
  try {
    const res = await getHostInfo(pageId.value);
    if (res.code === 200) {
      res.data?.forEach(e => {
        if (e.dbType === 'MySQL') {
          list.value.sourceIp = e.ip
        } else {
          list.value.targetIp = e.ip
        }
      })
    }
  } catch (error) {

  }
}
const pageId = ref()
let queryId = 0
const init = () => {
  const queryString = window.location.search
  const params = new URLSearchParams(queryString)
  queryId = params.get('id')
  pageId.value = window.$wujie?.props.data.id ?? queryId
  list.value.id = pageId
  getTaskInfo()
  getHostIp()
}
init()
</script>
<style scoped lang="less">
@import '@/assets/style/openGlobal.less';

html,
body {
  height: 100%;
  margin: 0;
  padding: 0;
}


.common-layout {
  ::v-deep(.el-form-item__content) {
    word-break: break-word;
  }

  display: grid;
  grid-template-columns: 300px 1fr;
  grid-template-rows: 120px 1fr;
  height: 91vh;
  width: 100%;
  overflow: auto;
  gap: 25px;
  padding: 10px;
  grid-template-areas: "top-left top-right"
  "bottom-left bottom-right";
  background-color: #F4F6FA
}

.icon-text-container {
  display: flex;
  align-items: flex-start;
}

.icon-container {
  margin-right: 30px;
}

.text-label-container {
  display: flex;
  flex-direction: column;
}

.basic-info {
  .el-form-item {
    margin-bottom: 0px;
  }

  .info-title {
    margin-bottom: 14px;
    font-weight: bold;
  }

  .dbMapTable {
    margin-top: 8px;
  }
}

.text-same-line {
  background-color: white;
  margin-right: 10px;
  width: 200px;
  height: 100%;
  justify-content: center;
  align-items: center;
}

.list-title {
  font-size: 25px;
  font-weight: bold;
  padding-bottom: 10px
}

.text-content {
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: bold;
  text-overflow: ellipsis;
  overflow: hidden;
  width: 150px;
  white-space: nowrap;
}

.tag-container {
  display: flex;
  justify-content: flex-start;
  width: auto;
}



.top-left,
.top-right,
.bottom-left,
.bottom-right {
  font-size: 20px;
  box-sizing: border-box;
}

.top-left {
  grid-area: top-left;
  background-color: white;
  align-items: center;
  font-weight: bold;
  padding: 20px;
}

.bottom-left {
  grid-area: bottom-left;
  background-color: white;
  align-items: flex-start;
  padding: 22px;
}

.top-right {
  grid-area: top-right;
  display: flex;
  justify-content: flex-start;
}

.bottom-right {
  grid-area: bottom-right;
  flex-direction: column;
  display: grid;
  gap: 10px;
  grid-template-rows: auto 1fr auto;
  height: 100%;
  position: relative;
  background-color: white;
}

.search-box {
  grid-row: 1;
  width: 270px;
  // cursor: pointer;
  padding-right: 20px
}

.table-container {
  grid-row: 2;
  width: 100%;
  overflow-x: hidden;
}

.pagination-container {
  grid-row: 3;
  text-align: center;
  background-color: white;
  padding: 10px;
  border-radius: 5px;
}

.cell-content {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.layout-container {
  height: 100vh;
}

.ellipsis-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  /* Ensure it doesn't overflow the container */
}

:deep(.sql-tooltip) {
  max-width: 1500px;
}

.tab-content {
  padding: 22px;
}

.control-wrap {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .switchside {
    display: flex;

    .el-switch {
      --o-switch-width: auto;
    }

    :deep(.el-switch.is-checked .el-switch__core .el-switch__action) {
      left: calc(100% - 21px);
    }
  }

  .search-wrap {
    display: flex;

  }
}</style>
