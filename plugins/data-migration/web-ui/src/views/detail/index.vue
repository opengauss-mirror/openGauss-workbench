<template>
  <div class="detail-container">
    <div class="title-con">
      <div class="title-left">
        <div class="title">
          {{ $t('detail.index.5q09cuenq100', { name: task.taskName }) }}
        </div>
        <div class="task-status-con">
          <span class="task-status-title">{{
            $t('detail.index.5q09asiwekc0')
          }}</span>
          <span class="task-status">{{ execStatusMap(task.execStatus) }}</span>
        </div>
      </div>
      <div class="title-right">
        <a-button
          v-if="task.execStatus === TaskStatus.Progress"
          type="primary"
          @click="stopTask"
          >{{ $t('detail.index.5q09asiwffw0') }}</a-button
        >
      </div>
    </div>
    <a-divider />
    <div class="desc-con">
      <a-descriptions
        :data="descData"
        layout="inline-horizontal"
        table-layout="fixed"
        bordered
      />
    </div>
    <div class="progress-con">
      <span class="progress-info">{{ $t('detail.index.5q09asiwg0g0') }}</span>
      <a-progress
        size="large"
        :percent="
          task.execStatus === TaskStatus.Completed
            ? 1
            : Number(task.execProgress) || 0
        "
      />
      <a-button type="text" @click="loopSubTaskStatus">
        <template #icon>
          <icon-refresh />
        </template>
        <template #default>{{ $t('detail.index.5q09asiwg4g0') }}</template>
      </a-button>
    </div>
    <div class="table-con">
      <a-table
        :data="tableData"
        :bordered="false"
        :stripe="!currentTheme"
        :hoverable="!currentTheme"
        :pagination="pagination"
        @page-change="pageChange"
      >
        <template #columns>
          <a-table-column
            :title="$t('detail.index.5q09asiwg7s0')"
            data-index="id"
            :width="90"
          ></a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwgb40')"
            :width="160"
            ellipsis
            tooltip
          >
            <template #cell="{ record }">
              {{ record.sourceDbHost + ':' + record.sourceDbPort }}
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwifk0')"
            data-index="sourceDb"
            :width="120"
            ellipsis
            tooltip
          ></a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwijw0')"
            :width="160"
            ellipsis
            tooltip
          >
            <template #cell="{ record }">
              {{ record.targetDbHost + ':' + record.targetDbPort }}
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwing0')"
            data-index="targetDb"
            :width="120"
            ellipsis
            tooltip
          ></a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwiqk0')"
            :width="120"
            ellipsis
            tooltip
          >
            <template #cell="{ record }">
              {{
                record.migrationModelId === TaskMode.Offline
                  ? $t('detail.index.5q09asiwiyc0')
                  : $t('detail.index.5q09asiwj1o0')
              }}
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwj4g0')"
            :width="300"
            ellipsis
            tooltip
          >
            <template #cell="{ record }">
              <span class="mac-txt" @click="handleTerminal(record)"
                ><icon-code-square /> {{ record.runHost }}（{{
                  record.runHostname
                }}）</span
              >
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwjvg0')"
            :width="150"
            ellipsis
            tooltip
          >
            <template #cell="{ record }">
              <span>{{ execSubStatusMap(record.execStatus) }}</span>
              <a-popover :title="$t('detail.index.5q09asiwk6k0')">
                <icon-close-circle-fill
                  v-if="record.execStatus === SUB_TASK_STATUS.MIGRATION_ERROR"
                  size="14"
                  style="color: #ff7d01; margin-left: 3px; cursor: pointer"
                />
                <template #content>
                  <div class="error-tips">{{ record.statusDesc }}</div>
                </template>
              </a-popover>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('detail.index.5q09asiwka80')"
            align="center"
            :width="340"
            fixed="right"
          >
            <template #cell="{ record }">
              <a-button size="mini" type="text" @click="handleDetail(record)">
                <template #icon>
                  <icon-eye />
                </template>
                <template #default>{{
                  $t('detail.index.5q09asiwkds0')
                }}</template>
              </a-button>
              <a-button
                v-if="
                  (record.migrationModelId === TaskMode.Online &&
                    record.execStatus ===
                      SUB_TASK_STATUS.INCREMENTAL_RUNNING) ||
                  record.execStatus === SUB_TASK_STATUS.INCREMENTAL_FINISHED
                "
                size="mini"
                type="text"
                :loading="
                  record.execStatus === SUB_TASK_STATUS.INCREMENTAL_FINISHED
                "
                @click="stopSubIncrease(record)"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>{{
                  $t('detail.index.5q09asiwkkw0')
                }}</template>
              </a-button>
              <a-button
                v-if="
                  record.migrationModelId === TaskMode.Online &&
                  record.execStatus === SUB_TASK_STATUS.INCREMENTAL_STOPPED
                "
                size="mini"
                type="text"
                @click="startSubReverse(record)"
              >
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>{{
                  $t('detail.index.5q09asiwkq40')
                }}</template>
              </a-button>
              <a-button
                v-if="record.execStatus !== SUB_TASK_STATUS.MIGRATION_FINISH"
                size="mini"
                type="text"
                @click="stopSubTask(record)"
              >
                <template #icon>
                  <icon-stop />
                </template>
                <template #default>{{
                  $t('detail.index.5q09asiwl5g0')
                }}</template>
              </a-button>
              <a-button size="mini" type="text" @click="handleLog(record)">
                <template #icon>
                  <icon-file />
                </template>
                <template #default>{{
                  $t('detail.index.5q09asiwlac0')
                }}</template>
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- sub task detail -->
    <sub-task-detail
      v-model:open="subTaskDetailVisible"
      :task-info="task"
      :sub-task-id="subTaskId"
      :tab="tabIndex"
    />

    <!-- machine terminal -->
    <mac-terminal v-model:open="terminalVisible" :host="macHost" />

    <!-- reverse error dialog -->
    <a-modal
      v-model:visible="reverseVisible"
      width="800px"
      :title="$t('detail.index.5qofnua1tf40')"
      title-align="start"
      :footer="false"
    >
      <div class="config-con">
        <a-row gutter="20">
          <a-col :span="12">
            <div class="cur-config">
              <div class="config-title">
                {{ $t('detail.index.5qofnua40ig0') }}
              </div>
              <a-alert
                class="config-item"
                :type="reverseConfig.replacationPermise ? 'success' : 'error'"
              >
                <template #title>
                  {{ $t('detail.index.5qofnua4a4g0') }}
                </template>
                {{
                  reverseConfig.replacationPermise
                    ? $t('detail.index.5qofnua4abs0')
                    : $t('detail.index.5qofnua4ag40')
                }}
              </a-alert>
              <a-alert
                class="config-item"
                :type="
                  reverseConfig.sslValue === 'NULL' ||
                  reverseConfig.walLevelValue === 'NULL'
                    ? 'error'
                    : 'success'
                "
              >
                <template #title>
                  {{ $t('detail.index.5qofom5ifnk0') }}
                </template>
                <p>
                  {{
                    reverseConfig.sslValue === 'NULL'
                      ? $t('detail.index.5qofqisf3jc0')
                      : `ssl=${reverseConfig.sslValue};`
                  }}
                </p>
                <p>
                  {{
                    reverseConfig.walLevelValue === 'NULL'
                      ? $t('detail.index.5qofqisf68w0')
                      : `wal_level=${reverseConfig.walLevelValue};`
                  }}
                </p>
              </a-alert>
              <a-alert
                class="config-item"
                :type="
                  reverseConfig.rolcanlogin === 'false' ||
                  reverseConfig.rolreplication === 'false'
                    ? 'error'
                    : 'success'
                "
              >
                <template #title>
                  {{ $t('detail.index.5qofpdnmtkk0') }}
                </template>
                <a-descriptions
                  :data="replicationData"
                  size="medium"
                  layout="vertical"
                  bordered
                />
              </a-alert>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="correct-config">
              <div class="config-title">
                {{ $t('detail.index.5qofnua4akg0') }}
              </div>
              <a-alert class="config-item" type="success">
                <template #title>
                  {{ $t('detail.index.5qofnua4a4g0') }}
                </template>
                host replication {{ reverseConfig.dbUser }} 0.0.0.0/0 sha256
              </a-alert>
              <a-alert class="config-item" type="success">
                <template #title>
                  {{ $t('detail.index.5qofom5ifnk0') }}
                </template>
                <p>ssl=on;</p>
                <p>wal_level=logical;</p>
              </a-alert>
              <a-alert class="config-item" type="success">
                <template #title>
                  {{ $t('detail.index.5qofpdnmtkk0') }}
                </template>
                <a-descriptions
                  :data="[
                    {
                      label: 'rolcanlogin',
                      value: 't'
                    },
                    {
                      label: 'rolreplication',
                      value: 't'
                    }
                  ]"
                  size="medium"
                  layout="vertical"
                  bordered
                />
              </a-alert>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { Message } from '@arco-design/web-vue'
import SubTaskDetail from './components/SubTaskDetail.vue'
import MacTerminal from './components/MacTerminal.vue'
import { stop } from '@/api/list'
import {
  taskDetail,
  refreshStatus,
  subTaskList,
  subTaskFinish,
  subTaskStartReverse,
  subTaskStopIncremental
} from '@/api/detail'
import useTheme from '@/hooks/theme'
import { useI18n } from 'vue-i18n'
import { SUB_TASK_STATUS } from '@/utils/constants'

const { t } = useI18n()

const { currentTheme } = useTheme()

const task = ref({})
const descData = ref([])
let timerTop = null
let timerDown = null
let timerStatus = null

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

const reverseVisible = ref(false)
const reverseConfig = ref({})
const replicationData = ref([])

// status map
const execStatusMap = (status) => {
  const maps = {
    0: t('detail.index.5q09asiwlcg0'),
    1: t('detail.index.5q09asiwlew0'),
    2: t('detail.index.5q09asiwltg0')
  }
  return maps[status]
}

const TaskMode = reactive({
  Offline: 1,
  Online: 2
})

const TaskStatus = reactive({
  Unstart: 0,
  Progress: 1,
  Completed: 2
})

// sub task status map
const execSubStatusMap = (status) => {
  const maps = {
    0: t('detail.index.5q09asiwlcg0'),
    1: t('detail.index.5q09asiwlwc0'),
    2: t('detail.index.5q09asiwmi00'),
    3: t('detail.index.5q09asiwmow0'),
    4: t('detail.index.5q09asiwmr40'),
    5: t('detail.index.5q09asiwmt80'),
    6: t('detail.index.5q09asiwmvg0'),
    7: t('detail.index.5q09asiwmxg0'),
    8: t('detail.index.5q09asiwmzw0'),
    9: t('detail.index.5q09asiwn200'),
    10: t('detail.index.5q09asiwn201'),
    11: t('detail.index.5q09asiwn4k0'),
    12: t('detail.index.5q09asiwna40'),
    13: t('detail.index.5q09asiwncc0'),
    100: t('detail.index.5q09asiwne80'),
    500: t('detail.index.5q09asiwngg0'),
    1000: t('detail.index.5q09asiwnik0')
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

const handleTerminal = (row) => {
  terminalVisible.value = true
  macHost.value = row
}

const handleDetail = (row) => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 1
}

const handleLog = (row) => {
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
const stopSubTask = (row) => {
  subTaskFinish(row.id).then(() => {
    Message.success('Stop success')
    loopSubTaskStatus()
    getSubTaskList()
  })
}

// stop sub task increase
const stopSubIncrease = (row) => {
  subTaskStopIncremental(row.id).then(() => {
    Message.success('Stop success')
    loopSubTaskStatus()
    getSubTaskList()
  })
}

// start sub task reverse
const startSubReverse = (row) => {
  subTaskStartReverse(row.id)
    .then(() => {
      Message.success('Start success')
      loopSubTaskStatus()
      getSubTaskList()
    })
    .catch((e) => {
      if (e.code === 50154) {
        reverseConfig.value = e.data
        replicationData.value = [
          {
            label: 'rolcanlogin',
            value: e.data.rolcanlogin === 'true' ? 't' : 'f'
          },
          {
            label: 'rolreplication',
            value: e.data.rolreplication === 'true' ? 't' : 'f'
          }
        ]
        reverseVisible.value = true
      }
      if (e.code === 50155) {
        Message.error(t('detail.index.5qtkk99a2eo0'))
      }
    })
}

const loopSubTaskStatus = () => {
  timerStatus && clearTimeout(timerStatus)
  const id = window.$wujie?.props.data.id
  refreshStatus(id)
    .then(() => {
      if (task.value.execStatus !== 2) {
        timerStatus = setTimeout(() => {
          loopSubTaskStatus()
        }, 10000)
      }
    })
    .catch(() => {
      timerStatus && clearTimeout(timerStatus)
      timerStatus = null
    })
}

const getSubTaskList = () => {
  timerDown && clearTimeout(timerDown)
  const id = window.$wujie?.props.data.id
  subTaskList(id, {
    ...queryParams
  })
    .then((res) => {
      tableData.value = res.rows
      pagination.total = res.total
      if (task.value.execStatus !== 2) {
        timerDown = setTimeout(() => {
          getSubTaskList()
        }, 5000)
      }
    })
    .catch(() => {
      timerDown && clearTimeout(timerDown)
      timerDown = null
    })
}

const getTaskDetail = () => {
  timerTop && clearTimeout(timerTop)
  const id = window.$wujie?.props.data.id
  taskDetail(id)
    .then((res) => {
      task.value = res.data.task
      const taskInfo = res.data.task
      const offlineCounts = res.data.offlineCounts
      const onlineCounts = res.data.onlineCounts
      const hosts = res.data.hosts
      descData.value = [
        {
          label: () => t('detail.index.5q09asiwnks0'),
          value: taskInfo.createUser
        },
        {
          label: () => t('detail.index.5q09asiwnmw0'),
          value: offlineCounts['total'] + onlineCounts['total']
        },
        {
          label: () => t('detail.index.5q09asiwnow0'),
          value: () =>
            `${t('detail.index.5q09efwo3nc0', {
              num: hosts.length
            })}（${hosts.map((item) => item.hostName)}）`
        },
        {
          label: () => t('detail.index.5q09asiwnrs0'),
          value: taskInfo.createTime
        },
        {
          label: () => t('detail.index.5q09asiwnu40'),
          value: () =>
            t('detail.index.5q09frs8fh00', {
              total: offlineCounts['total'],
              notRunCount: offlineCounts['notRunCount'],
              runningCount: offlineCounts['runningCount'],
              finishCount: offlineCounts['finishCount'],
              errorCount: offlineCounts['errorCount']
            }),
          span: 2
        },
        {
          label: () => t('detail.index.5q09asiwnw00'),
          value: taskInfo.execTime
        },
        {
          label: () => t('detail.index.5q09asiwny00'),
          value: () =>
            t('detail.index.5q09frs8fh00', {
              total: onlineCounts['total'],
              notRunCount: onlineCounts['notRunCount'],
              runningCount: onlineCounts['runningCount'],
              finishCount: onlineCounts['finishCount'],
              errorCount: onlineCounts['errorCount']
            }),
          span: 2
        }
      ]
      if (task.value.execStatus !== 2) {
        timerTop = setTimeout(() => {
          getTaskDetail()
        }, 5000)
      }
    })
    .catch(() => {
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
  timerStatus && clearTimeout(timerStatus)
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

.config-con {
  .config-title {
    font-size: 14px;
  }

  .config-item {
    margin-top: 10px;
  }
}
</style>
