<template>
  <div class="detail-container">
    <div class="title-con">
      <div class="title-left">
        <div class="title">数据迁移任务详情（{{ task.taskName }}）</div>
        <div class="task-status-con">
          <span class="task-status-title">任务状态：</span>
          <span class="task-status">{{ execStatusMap(task.execStatus) }}</span>
        </div>
      </div>
      <div class="title-right">
        <a-button v-if="task.execStatus === 1" type="primary" @click="stopTask">停止</a-button>
      </div>
    </div>
    <a-divider />
    <div class="desc-con">
      <a-descriptions :data="descData" layout="inline-horizontal" table-layout="fixed" bordered />
    </div>
    <div class="progress-con">
      <span class="progress-info">总进度</span>
      <a-progress size="large" :percent="task.execStatus === 2 ? 1: (task.execProgress || 0)" />
      <a-button type="text" @click="loopSubTaskStatus">
        <template #icon>
          <icon-refresh />
        </template>
        <template #default>刷新</template>
      </a-button>
    </div>
    <div class="table-con">
      <a-table :data="tableData" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme" :pagination="pagination" @page-change="pageChange">
        <template #columns>
          <a-table-column title="子任务ID" data-index="id" :width="90"></a-table-column>
          <a-table-column title="源实例名" :width="180" ellipsis tooltip>
            <template #cell="{ record }">
              {{ record.sourceDbHost + ':' + record.sourceDbPort }}
            </template>
          </a-table-column>
          <a-table-column title="源库名" data-index="sourceDb" :width="100" ellipsis tooltip></a-table-column>
          <a-table-column title="目的实例名" :width="180" ellipsis tooltip>
            <template #cell="{ record }">
              {{ record.targetDbHost + ':' + record.targetDbPort }}
            </template>
          </a-table-column>
          <a-table-column title="目的库名" data-index="targetDb" :width="100" ellipsis tooltip></a-table-column>
          <a-table-column title="迁移过程模式" :width="120" ellipsis tooltip>
            <template #cell="{ record }">
              {{ record.migrationModelId === 1 ? '离线模式' : '在线模式' }}
            </template>
          </a-table-column>
          <a-table-column title="执行机器" :width="300" ellipsis tooltip>
            <template #cell="{ record }">
              <span class="mac-txt" @click="handleTerminal(record)"><icon-code-square /> {{ record.runHost }}（{{ record.runHostname }}）</span>
            </template>
          </a-table-column>
          <a-table-column title="当前状态" :width="150" ellipsis tooltip>
            <template #cell="{ record }">
              <span>{{ execSubStatusMap(record.execStatus) }}</span>
              <a-popover title="迁移失败详情">
                <icon-close-circle-fill v-if="record.execStatus === 500" size="14" style="color: #FF7D01;margin-left: 3px;cursor: pointer;" />
                <template #content>
                  <div class="error-tips">{{ record.statusDesc }}</div>
                </template>
              </a-popover>
            </template>
          </a-table-column>
          <a-table-column title="操作" align="center" :width="340" fixed="right">
            <template #cell="{ record }">
              <a-button
                size="mini"
                type="text"
                @click="handleDetail(record)"
              >
                <template #icon>
                  <icon-eye />
                </template>
                <template #default>详情</template>
              </a-button>
              <!-- <a-button
                v-if="(record.migrationModelId === 1 && record.execStatus === 2) || (record.migrationModelId === 2 && record.execStatus === 2)"
                size="mini"
                type="text"
                @click="stopSubTask(record)"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>停止全量</template>
              </a-button> -->
              <a-button
                v-if="record.migrationModelId === 2 && record.execStatus === 8"
                size="mini"
                type="text"
                @click="stopSubIncrease(record)"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>停止增量</template>
              </a-button>
              <a-button
                v-if="record.migrationModelId === 2 && (record.execStatus === 3 || record.execStatus === 9)"
                size="mini"
                type="text"
                @click="startSubReverse(record)"
              >
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>启动反向</template>
              </a-button>
              <!-- <a-button
                v-if="record.migrationModelId === 2 && record.execStatus === 11"
                size="mini"
                type="text"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>停止反向</template>
              </a-button> -->
              <a-button
                v-if="record.execStatus !== 100"
                size="mini"
                type="text"
                @click="stopSubTask(record)"
              >
                <template #icon>
                  <icon-stop />
                </template>
                <template #default>结束迁移</template>
              </a-button>
              <a-button
                size="mini"
                type="text"
                @click="handleLog(record)"
              >
                <template #icon>
                  <icon-file />
                </template>
                <template #default>日志</template>
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- sub task detail -->
    <sub-task-detail v-model:open="subTaskDetailVisible" :task-info="task" :sub-task-id="subTaskId" :tab="tabIndex" />

    <!-- machine terminal -->
    <mac-terminal v-model:open="terminalVisible" :host="macHost" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { Message } from '@arco-design/web-vue'
import SubTaskDetail from './components/SubTaskDetail.vue'
import MacTerminal from './components/MacTerminal.vue'
import { stop } from '@/api/list'
import { taskDetail, refreshStatus, subTaskList, subTaskFinish, subTaskStartReverse, subTaskStopIncremental } from '@/api/detail'
import useTheme from '@/hooks/theme'

const { currentTheme } = useTheme()

const task = ref({})
const descData = ref([])
let timerTop = null
let timerDown = null

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})
const tableData = ref([])

// status map
const execStatusMap = (status) => {
  const maps = {
    0: '未启动',
    1: '迁移中',
    2: '已完成'
  }
  return maps[status]
}

// sub task status map
const execSubStatusMap = (status) => {
  const maps = {
    0: '未启动',
    1: '全量迁移开始',
    2: '全量迁移进行中',
    3: '全量迁移完成',
    4: '全量校验开始',
    5: '全量校验中',
    6: '全量检验完成',
    7: '增量迁移开始',
    8: '增量迁移进行中',
    9: '增量迁移已停止',
    10: '反向迁移开始',
    11: '反向迁移进行中',
    12: '反向迁移已停止',
    100: '迁移完成',
    500: '迁移失败',
    1000: '等待资源中'
  }
  return maps[status]
}

const pageChange = (current) => {
  queryParams.pageNum = current
  pagination.current = current
  getSubTaskList()
}

const subTaskDetailVisible = ref(false)
const subTaskId = ref()
const tabIndex = ref(1)

const terminalVisible = ref(false)
const macHost = ref({})

const handleTerminal = row => {
  terminalVisible.value = true
  macHost.value = row
}

const handleDetail = row => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 1
}

const handleLog = row => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 2
}

// stop task
const stopTask = async () => {
  await stop(task.value.id)
  Message.success('Stop success')
  getTaskDetail()
  getSubTaskList()
}

// stop sub task full
const stopSubTask = row => {
  subTaskFinish(row.id).then(() => {
    Message.success('Stop success')
    loopSubTaskStatus()
    getSubTaskList()
  })
}

// stop sub task increase
const stopSubIncrease = row => {
  subTaskStopIncremental(row.id).then(() => {
    Message.success('Stop success')
    loopSubTaskStatus()
    getSubTaskList()
  })
}

// start sub task reverse
const startSubReverse = row => {
  if (row.targetDbVersion.substring(0, 3) === '3.0') {
    Message.error(`${row.targetDbVersion} version of openGauss does not support reverse migration`)
    return
  }
  subTaskStartReverse(row.id).then(() => {
    Message.success('Start success')
    loopSubTaskStatus()
    getSubTaskList()
  })
}

const loopSubTaskStatus = () => {
  const id = window.$wujie?.props.data.id
  refreshStatus(id).then(res => {
    console.log(res)
  })
}

const getSubTaskList = () => {
  timerDown && clearTimeout(timerDown)
  const id = window.$wujie?.props.data.id
  subTaskList(id, {
    ...queryParams
  }).then(res => {
    tableData.value = res.rows
    pagination.total = res.total
    if (task.value.execStatus !== 2) {
      timerDown = setTimeout(() => {
        getSubTaskList()
      }, 5000)
    }
  }).catch(() => {
    timerDown && clearTimeout(timerDown)
    timerDown = null
  })
}

const getTaskDetail = () => {
  timerTop && clearTimeout(timerTop)
  const id = window.$wujie?.props.data.id
  taskDetail(id).then(res => {
    task.value = res.data.task
    const taskInfo = res.data.task
    const offlineCounts = res.data.offlineCounts
    const onlineCounts = res.data.onlineCounts
    const hosts = res.data.hosts
    descData.value = [
      {
        label: '创建人：',
        value: taskInfo.createUser
      },
      {
        label: '子任务数量：',
        value: offlineCounts['total'] + onlineCounts['total']
      },
      {
        label: '分配执行机器：',
        value: `${hosts.length}台（${hosts.map(item => item.hostName)}）`
      },
      {
        label: '创建时间：',
        value: taskInfo.createTime
      },
      {
        label: '子任务数量（离线模式）：',
        value: `总数：${offlineCounts['total']}，未启动：${offlineCounts['notRunCount']}，执行中：${offlineCounts['runningCount']}，迁移完成：${offlineCounts['finishCount']}，迁移失败：${offlineCounts['errorCount']}`,
        span: 2
      },
      {
        label: '执行时间：',
        value: taskInfo.execTime
      },
      {
        label: '子任务数量（在线模式）：',
        value: `总数：${onlineCounts['total']}，未启动：${onlineCounts['notRunCount']}，执行中：${onlineCounts['runningCount']}，迁移完成：${onlineCounts['finishCount']}，迁移失败：${onlineCounts['errorCount']}`,
        span: 2
      }
    ]
    if (task.value.execStatus !== 2) {
      timerTop = setTimeout(() => {
        getTaskDetail()
      }, 5000)
    }
  }).catch(() => {
    timerTop && clearTimeout(timerTop)
    timerTop = null
  })
}

onMounted(() => {
  getTaskDetail()
  getSubTaskList()
  setTimeout(() => {
    loopSubTaskStatus()
  }, 3000)
})

onBeforeUnmount(() => {
  timerTop && clearTimeout(timerTop)
  timerDown && clearTimeout(timerDown)
})
</script>

<style lang="less" scoped>
.detail-container {
  position: relative;
  .title-con {
    padding: 20px 20px 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title-left {
      display: flex;
      align-items: center;
      .title {
        font-size: 20px;
        color: var(--color-text-1);
      }
      .task-status-con {
        margin-left: 40px;
        display: flex;
        align-items: center;
        .task-status-title {
          color: var(--color-text-1);
          white-space: nowrap;
          margin-right: 10px;
          display: flex;
          align-items: center;
        }
        .task-status {
          color: rgb(var(--primary-6));
        }
      }
    }
  }
  .desc-con {
    padding: 0 20px;
  }
  .progress-con {
    margin-top: 20px;
    padding: 0 20px;
    display: flex;
    align-items: center;
    .progress-info {
      white-space: nowrap;
      margin-right: 10px;
      color: var(--color-text-1);
    }
  }
  .table-con {
    margin-top: 20px;
    padding: 0 20px 30px;
    .mac-txt {
      cursor: pointer;
      color: rgb(var(--primary-6));
    }
  }
}

.error-tips {
  max-width: 1200px;
  max-height: 350px;
  overflow-y: auto;
}
</style>
