
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
            <span class="list-title">{{ list.anynum }}</span>
            <span style="font-size: 16px;"> 已解析数据</span>
          </div>
          <div class="text-label-container text-same-line">
            <span class="list-title">{{ list.playbacknum }}</span>
            <span style="font-size: 16px;"> 已回放数据</span>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom-left">
      <div class="grid-content">
        <div class="icon-text-container" style=" min-width: 250px;max-width: 300px;">
          <div class="text-label-container basic-info">
            <div class="info-title">
              <span>基本信息</span>
            </div>
            <el-form :data="list" label-position="left" label-width="110">
              <el-form-item label="任务名称"><span>{{ list.name }}</span></el-form-item>
              <el-form-item label="任务ID"><span>{{ pageId }}</span></el-form-item>

              <el-form-item label="任务类型"><span>{{ getTypeText(list.taskType) }}</span></el-form-item>
              <el-form-item label="任务耗时"><span>{{ formattedTime(list.taskDuration) }}</span></el-form-item>
              <el-form-item label="任务开始时间"><span>{{ list.startTime }}</span></el-form-item>
              <el-form-item label="任务结束时间"><span>{{ list.endTime }}</span></el-form-item>
              <el-table :data="dbMapData" border class="dbMapTable">
                <el-table-column label="数据库映射关系" align="center">
                  <el-table-column prop="source" label="源端"></el-table-column>
                  <el-table-column prop="target" label="目的端"></el-table-column>
                </el-table-column>
              </el-table>
            </el-form>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom-right">
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick"
        style="background-color: #F4F6FA; padding-bottom: 20px">
        <el-tab-pane label="慢SQL" name="slowquery" />
        <el-tab-pane label="失败SQL" name="failingquery" />
        <el-tab-pane label="耗时对比" name="durationCompare" v-if="list.status === TASKSTATE.FINISH_NUMERIC" />
      </el-tabs>
      <div class="tab-content" v-if="activeName === 'durationCompare'">
        <sql-compare :id="pageId"></sql-compare>
      </div>
      <div class="tab-content" v-if="activeName !== 'durationCompare'">
        <div class=" table-container">
          <div class="control-wrap">
            <div class="search-wrap">
              <el-input type="text" prefix-icon="el-icon-search" v-model="searchText" :placeholder=defaulttext
                style="width: 270px; cursor: pointer;padding-right: 20px" class="search-box" clearable
                @enter="handleSearch" />
              <el-button :icon="IconSearch" type="primary" @click="handleSearch">搜索</el-button>
            </div>
            <div class="switchside">
              <el-switch v-model="autoRefreshFlag" class="ml-2" inline-prompt
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949;" size="large" width="auto"
                active-text="自动刷新" inactive-text="停止刷新" @change="autoRefreshList" />
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
import { computed, nextTick, ref } from "vue";
import { getFailSql, getSlowSql, transcribeReplayList } from "@/api/playback";
import SqlCompare from "./sqlCompare.vue"
import { IconSearch } from '@computing/opendesign-icons'

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

const dbMapData = ref([])
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

const activeName = ref('slowquery')
const searchText = ref()
const defaulttext = ref()
const tableHead = ref([])
const tableWidth = ref('100%')
const tableRef = ref(null)
defineExpose({
  tableRef
})
const handleClick = async (tab, event) => {
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
  anynum: '',
  playbacknum: '',
  data: [],
  totalNum: 0,
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
    defaulttext.value = '请输入慢SQL字段进行搜索'
    tableHead.value = []
    tableHead.value.push({ columnname: 'sqlStr', columncomment: 'SQL' })
    tableHead.value.push({ columnname: 'sourceDuration', columncomment: '源端耗时（微秒）' })
    tableHead.value.push({ columnname: 'targetDuration', columncomment: '目的端耗时（微秒）' })
    tableHead.value.push({ columnname: 'countStr', columncomment: '出现次数' })
    list.value.data = []
    getSlowSql(list.value.id, tempFilter).then(res => {
      if (Number(res.code) === 200) {
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
    defaulttext.value = '请输入失败SQL字段进行搜索'
    tableHead.value = []
    tableHead.value.push({ columnname: 'sql', columncomment: 'SQL' })
    tableHead.value.push({ columnname: 'failreason', columncomment: '失败原因' })
    list.value.data = []
    getFailSql(list.value.id, filter).then(res => {
      if (Number(res.code) === 200) {
        res.rows.forEach(item => {
          const tempPackage = {
            sql: item.sql,
            failreason: item.message,
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
      intervalId = setInterval(getListData, 6000)
    }
  } else {
    if (intervalId !== null) {
      clearInterval(intervalId);
      intervalId = null;
    }
  }
}

const getHostInfo = () => {
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
      list.value.anynum = res.rows[0].parseNum
      list.value.playbacknum = res.rows[0].replayNum
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
  return `${days}天${hours}小时${minutes}分${seconds}秒`
}

const pageId = ref()
let queryId = 0
const init = () => {
  const queryString = window.location.search
  const params = new URLSearchParams(queryString)
  queryId = params.get('id')
  pageId.value = window.$wujie?.props.data.id ?? queryId
  list.value.id = pageId
  getHostInfo()
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

  .switchside {
    display: flex;
  }

  .search-wrap {
    display: flex;

  }
}</style>
