<template>
  <div class="detail-container" id="migrationDetail">
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
        <a-button v-if="task.execStatus === TaskStatus.Progress" type="primary" @click="stopTask">{{
          $t('detail.index.5q09asiwffw0') }}</a-button>
      </div>
    </div>
    <a-divider />
    <div class="desc-con">
      <a-descriptions :data="descData" layout="inline-horizontal" table-layout="fixed" bordered />
    </div>
    <div class="progress-con">
      <span class="progress-info">{{ $t('detail.index.5q09asiwg0g0') }}</span>
      <a-progress size="large" :percent="task.execStatus === TaskStatus.Completed
          ? 1
          : Number(task.execProgress) || 0
        " />
      <a-button type="text" @click="loopSubTaskStatus">
        <template #icon>
          <icon-refresh />
        </template>
        <template #default>{{ $t('detail.index.5q09asiwg4g0') }}</template>
      </a-button>
    </div>
    <div class="table-con">
      <a-table :data="tableData" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme"
        :pagination="pagination" @page-change="pageChange">
        <template #columns>
          <a-table-column :title="$t('detail.index.5q09asiwg7s0')" data-index="id" :width="90"></a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwgb40')" :width="160" ellipsis tooltip>
            <template #cell="{ record }">
              {{ record.sourceDbHost + ':' + record.sourceDbPort }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwifk0')" data-index="sourceDb" :width="120" ellipsis
            tooltip></a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwijw0')" :width="160" ellipsis tooltip>
            <template #cell="{ record }">
              {{ record.targetDbHost + ':' + record.targetDbPort }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwing0')" data-index="targetDb" :width="120" ellipsis
            tooltip></a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwiqk0')" :width="120" ellipsis tooltip>
            <template #cell="{ record }">
              {{
                record.migrationModelId === TaskMode.Offline
                ? $t('detail.index.5q09asiwiyc0')
                : $t('detail.index.5q09asiwj1o0')
              }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwj4g0')" :width="300" ellipsis tooltip>
            <template #cell="{ record }">
              <span class="mac-txt" @click="handleTerminal(record)"><icon-code-square /> {{ record.runHost }}（{{
                record.runHostname
              }}）</span>
            </template>
          </a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwjvg0')" :width="150" ellipsis tooltip>
            <template #cell="{ record }">
              <span>{{ execSubStatusMap(record.execStatus) }}</span>
              <a-popover :title="titleMap(record.execStatus)">
                <icon-close-circle-fill
                  v-if="record.execStatus === SUB_TASK_STATUS.MIGRATION_ERROR || record.execStatus === SUB_TASK_STATUS.CHECK_FAILED"
                  size="14" style="color: #ff7d01; margin-left: 3px; cursor: pointer" />
                <template #content>
                  <div v-if="record.execStatus === SUB_TASK_STATUS.MIGRATION_ERROR" class="error-tips">{{
                    record.statusDesc }}</div>
                  <div v-if="record.execStatus === SUB_TASK_STATUS.CHECK_FAILED" class="error-tips">
                    <p v-if="judgeKeyExist(record.statusDesc, 'service_availability') === true">{{
                      parseServiceAvailability(record.statusDesc) }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'database_connect') === true">{{
                      parseDatabaseConnect(record.statusDesc) }}</p>
                    <p>{{ parseDatabasePermission(record.statusDesc) }}</p>
                    <p
                      v-if="judgeKeyExist(record.statusDesc, 'increment_param') === true || judgeKeyExist(record.statusDesc, 'reverse_param') === true">
                      {{ parselogParameter(record.statusDesc) }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'lower_param') === true">{{
                      parseLowerParameter(record.statusDesc) }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'disk_space') === true">{{ parseDiskSpace(record.statusDesc)
                    }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'mysql_encryption') === true">{{
                      parseMysqlEncryption(record.statusDesc) }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'sql_compatibility') === true">{{
                      parseOpenGaussBDB(record.statusDesc) }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'replication_slots') === true">{{
                      parseReplicationNumber(record.statusDesc) }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'enable_slot_log') === true">{{
                      parseEnableSlotLog(record.statusDesc) }}</p>
                    <p v-if="judgeKeyExist(record.statusDesc, 'hba_conf') === true">{{ parseHbaConf(record.statusDesc) }}
                    </p>
                  </div>
                </template>
              </a-popover>
            </template>
          </a-table-column>
          <a-table-column :title="$t('detail.index.5q09asiwka80')" align="center" :width="340" fixed="right">
            <template #cell="{ record }">
              <a-button size="mini" type="text" @click="handleDetail(record)">
                <template #icon>
                  <icon-eye />
                </template>
                <template #default>{{
                  $t('detail.index.5q09asiwkds0')
                }}</template>
              </a-button>
              <a-popconfirm :content="tooltipMap(record.checkDataLevelingAndIncrementFinish)"
                @ok="stopSubIncrease(record)">
                <a-button v-if="(record.migrationModelId === TaskMode.Online &&
                    record.execStatus ===
                    SUB_TASK_STATUS.INCREMENTAL_RUNNING) ||
                  record.execStatus === SUB_TASK_STATUS.INCREMENTAL_FINISHED
                  " size="mini" type="text" :loading="record.execStatus === SUB_TASK_STATUS.INCREMENTAL_FINISHED
    ">
                  <template #icon>
                    <icon-pause />
                  </template>
                  <template #default>{{
                    $t('detail.index.5q09asiwkkw0')
                  }}</template>
                </a-button>
              </a-popconfirm>
              <a-button v-if="record.migrationModelId === TaskMode.Online &&
                  record.execStatus === SUB_TASK_STATUS.INCREMENTAL_STOPPED
                  " size="mini" type="text" @click="startSubReverse(record)">
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>{{
                  $t('detail.index.5q09asiwkq40')
                }}</template>
              </a-button>
              <a-button v-if="record.execStatus !== SUB_TASK_STATUS.MIGRATION_FINISH" size="mini" type="text"
                @click="stopSubTask(record)">
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
    <sub-task-detail v-model:open="subTaskDetailVisible" :task-info="task" :sub-task-id="subTaskId" :tab="tabIndex" />

    <!-- machine terminal -->
    <mac-terminal v-model:open="terminalVisible" :host="macHost" />

    <!-- reverse error dialog -->
    <a-modal v-model:visible="reverseVisible" width="800px" :title="$t('detail.index.5qofnua1tf40')" title-align="start"
      :footer="false">
      <div class="config-con">
        <a-row :gutter="20">
          <a-col :span="12">
            <div class="cur-config">
              <div class="config-title">
                {{ $t('detail.index.5qofnua40ig0') }}
              </div>
              <a-alert class="config-item" :type="reverseConfig.replacationPermise ? 'success' : 'error'">
                <template #title>
                  {{ $t('detail.index.5qofnua4a4g0') }}
                </template>
                {{
                  reverseConfig.replacationPermise
                  ? $t('detail.index.5qofnua4abs0')
                  : $t('detail.index.5qofnua4ag40')
                }}
              </a-alert>
              <a-alert class="config-item" :type="reverseConfig.sslValue === 'NULL' ||
                    reverseConfig.walLevelValue === 'NULL'
                    ? 'error'
                    : 'success'
                  ">
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
              <a-alert class="config-item" :type="reverseConfig.rolcanlogin === 'false' ||
                    reverseConfig.rolreplication === 'false'
                    ? 'error'
                    : 'success'
                  ">
                <template #title>
                  {{ $t('detail.index.5qofpdnmtkk0') }}
                </template>
                <a-descriptions :data="replicationData" size="medium" layout="vertical" bordered />
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
                <a-descriptions :data="[
                  {
                    label: 'rolcanlogin',
                    value: 't'
                  },
                  {
                    label: 'rolreplication',
                    value: 't'
                  }
                ]" size="medium" layout="vertical" bordered />
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
    2: t('detail.index.5q09asiwltg0'),
    3000: t('list.index.5q08sf2dha81')
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
    1000: t('detail.index.5q09asiwnik0'),
    3000: t('detail.index.5q09asiwlca0')
  }
  return maps[status]
}

const titleMap = (status) => {
  const maps = {
    500: t('detail.index.5q09asiwk6k0'),
    3000: t('detail.index.5q09asiwk6a0')
  }
  return maps[status]
}

const tooltipMap = (checkDataLevelingAndIncrementFinish) => {
  const maps = {
    0: t('detail.index.5qtkk88a2eo0'),
    1: t('detail.index.5qtkk98a2eo0'),
    2: t('detail.index.5qtkk97a2eo0')
  }
  return maps[checkDataLevelingAndIncrementFinish]
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

const judgeKeyExist = (content, key) => {
  console.log('key', key)
  var result = ref(false)
  var obj = JSON.parse(content)
  if (key in obj) {
    result = true
  }
  return result
}

const parseServiceAvailability = (content) => {
  var result = t('detail.index.5qtkk97a2eo1')
  console.log('content', content)
  var obj = JSON.parse(content)
  console.log('obj.service_availability', obj.service_availability)
  if (obj.service_availability === 0) {
    result = result + t('detail.index.5qtkk97a2eo2')
  } else {
    result = result + t('detail.index.5qtkk97a2eo3')
  }
  return result
}

const parseDatabaseConnect = (content) => {
  var result = t('detail.index.5qtkk97a2eo4')
  var obj = JSON.parse(content)
  console.log('obj.database_connect', obj.database_connect)
  if (obj.database_connect.mysql === 0) {
    result = result + t('detail.index.5qtkk97a2eo5')
  } else {
    result = result + t('detail.index.5qtkk97a2eo6')
  }
  if (obj.database_connect.opengauss === 0) {
    result = result + t('detail.index.5qtkk97a2eo7')
  } else {
    result = result + t('detail.index.5qtkk97a2eo8')
  }
  return result
}

const parseDatabasePermission = (content) => {
  var result = t('detail.index.5qtkk97a2eo9')
  var obj = JSON.parse(content)
  if ("full_permission" in obj) {
    console.log('obj.full_permission', obj.full_permission)
    if (obj.full_permission.mysql === 0) {
      result = result + t('detail.index.5qtkk97a2e10')
    } else if (obj.full_permission.mysql === 1) {
      result = result + t('detail.index.5qtkk97a2e11')
    } else {
      result = result + t('detail.index.5qtkk97a2e12')
    }
    if (obj.full_permission.opengauss === 0) {
      result = result + t('detail.index.5qtkk97a2e13')
    } else if (obj.full_permission.opengauss === 1) {
      result = result + t('detail.index.5qtkk97a2e14')
    } else {
      result = result + t('detail.index.5qtkk97a2e15')
    }
  }

  if ("increment_permission" in obj) {
    console.log('obj.increment_permission', obj.increment_permission)
    if (obj.increment_permission.mysql === 0) {
      result = result + t('detail.index.5qtkk97a2e16')
    } else if (obj.increment_permission.mysql === 1) {
      result = result + t('detail.index.5qtkk97a2e17')
    } else {
      result = result + t('detail.index.5qtkk97a2e19')
    }
    if (obj.increment_permission.opengauss === 0) {
      result = result + t('detail.index.5qtkk97a2e19')
    } else if (obj.increment_permission.opengauss === 1) {
      result = result + t('detail.index.5qtkk97a2e20')
    } else {
      result = result + t('detail.index.5qtkk97a2e21')
    }
  }

  if ("reverse_permission" in obj) {
    console.log('content.reverse_permission', obj.reverse_permission)
    if (obj.reverse_permission.mysql === 0) {
      result = result + t('detail.index.5qtkk97a2e22')
    } else if (obj.reverse_permission.mysql === 1) {
      result = result + t('detail.index.5qtkk97a2e23')
    } else {
      result = result + t('detail.index.5qtkk97a2e24')
    }
    if (obj.reverse_permission.opengauss === 0) {
      result = result + t('detail.index.5qtkk97a2e25')
    } else if (obj.reverse_permission.opengauss === 1) {
      result = result + t('detail.index.5qtkk97a2e26')
    } else {
      result = result + t('detail.index.5qtkk97a2e27')
    }
  }
  return result
}

const parselogParameter = (content) => {
  var obj = JSON.parse(content)
  var result = t('detail.index.5qtkk97a2e28')
  if ("increment_param" in obj) {
    if (obj.increment_param.mysql.result === 0) {
      result = result + t('detail.index.5qtkk97a2e29')
    } else if (obj.increment_param.mysql.result === 1) {
      result = result + t('detail.index.5qtkk97a2e30') + obj.increment_param.mysql.actualParam + t('detail.index.5qtkk97a2e57') + obj.increment_param.mysql.expectedParam + t('detail.index.5qtkk97a2e35')
      if (obj.increment_param.mysql.SQLException) {
        result = result + obj.increment_param.mysql.SQLException
      }
    } else {
      result = result + t('detail.index.5qtkk97a2e31')
    }
  }
  if ("reverse_param" in obj) {
    if (obj.reverse_param.opengauss.result === 0) {
      result = result + t('detail.index.5qtkk97a2e32')
    } else if (obj.reverse_param.opengauss.result === 1) {
      result = result + t('detail.index.5qtkk97a2e33') + obj.reverse_param.opengauss.binlog_error + t('detail.index.5qtkk97a2e57') + obj.reverse_param.opengauss.binlog + t('detail.index.5qtkk97a2e35')
      if (obj.reverse_param.opengauss.SQLException) {
        result = result + obj.reverse_param.opengauss.SQLException
      }
    } else {
      result = result + t('detail.index.5qtkk97a2e34')
    }
  }

  return result
}

const parseLowerParameter = (content) => {
  var obj = JSON.parse(content)
  var result = t('detail.index.5qtkk97a2e41')
  console.log('obj.disk_space', obj.lower_param)
  if (obj.lower_param.result === 0) {
    result = result + t('detail.index.5qtkk97a2e42')
  } else if (obj.lower_param.result === 1) {
    result = result + t('detail.index.5qtkk97a2e43') + obj.lower_param.mysql + t('detail.index.5qtkk97a2e45') + t('detail.index.5qtkk97a2e44') + obj.lower_param.opengauss + t('detail.index.5qtkk97a2e35')
    if (obj.lower_param.SQLException) {
      result = result + obj.lower_param.SQLException
    }
  }
  return result
}

const parseDiskSpace = (content) => {
  var obj = JSON.parse(content)
  var result = t('detail.index.5qtkk97a2e36')
  console.log('obj.disk_space', obj.disk_space)
  if (obj.disk_space.result === 0) {
    result = result + t('detail.index.5qtkk97a2e37')
  } else if (obj.disk_space.result === 1) {
    result = result + t('detail.index.5qtkk97a2e38') + obj.disk_space.disk_error.need + t('detail.index.5qtkk97a2e39') + obj.disk_space.disk_error.remain + t('detail.index.5qtkk97a2e35')
  } else {
    result = result + t('detail.index.5qtkk97a2e40')
  }
  return result
}

const parseMysqlEncryption = (content) => {
  var obj = JSON.parse(content)
  var result = t('detail.index.5qtkk97a2e46')
  console.log('obj.mysql_encryption', obj.mysql_encryption)
  if (obj.mysql_encryption.result === 0) {
    result = result + t('detail.index.5qtkk97a2e47')
  } else if (obj.mysql_encryption.result === 1) {
    result = result + t('detail.index.5qtkk97a2e48') + obj.mysql_encryption.encryption + t('detail.index.5qtkk97a2e49') + obj.mysql_encryption.valid_encryption + t('detail.index.5qtkk97a2e35')
  }
  return result
}

const parseOpenGaussBDB = (content) => {
  var obj = JSON.parse(content)
  var result = t('detail.index.5qtkk97a2e50')
  console.log('obj.sql_compatibility', obj.sql_compatibility)
  if (obj.sql_compatibility.result === 0) {
    result = result + t('detail.index.5qtkk97a2e51')
  } else if (obj.sql_compatibility.result === 1) {
    result = result + t('detail.index.5qtkk97a2e52') + obj.sql_compatibility.sql_compatibility + t('detail.index.5qtkk97a2e53') + obj.sql_compatibility.valid_sql_compatibility + t('detail.index.5qtkk97a2e35')
  }
  return result
}

const parseReplicationNumber = (content) => {
  var obj = JSON.parse(content)
  var result = t('detail.index.5qtkk97a2e54')
  console.log('obj.replication_slots', obj.replication_slots)
  if (obj.replication_slots.result === 0) {
    result = result + t('detail.index.5qtkk97a2e55')
  } else if (obj.replication_slots.result === 1) {
    result = result + obj.replication_slots.replication_number + t('detail.index.5qtkk97a2e56')
  }
  return result
}

const parseEnableSlotLog = (content) => {
  var obj = JSON.parse(content)
  var result = t('detail.index.5qtkk97a2e58')
  console.log('obj.enable_slot_log', obj.enable_slot_log)
  if (obj.enable_slot_log.result === 0) {
    result = result + t('detail.index.5qtkk97a2e59')
  } else if (obj.enable_slot_log.result === 1) {
    result = result + t('detail.index.5qtkk97a2e60') + obj.enable_slot_log.expected_value + t('detail.index.5qtkk97a2e35')
  } else if (obj.enable_slot_log.result === 2) {
    result = result + obj.enable_slot_log.error_message + t('detail.index.5qtkk97a2e35')
  } else {
    result = result + t('detail.index.5qtkk97a2e40') + t('detail.index.5qtkk97a2e35')
  }
  return result
}

const parseHbaConf = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e61')

  switch (obj.hba_conf.result) {
    case 0:
      return result + t('detail.index.5qtkk97a2e51')
    case 1:
      result = result + t('detail.index.5qtkk97a2e62')

      switch (obj.hba_conf.detail_code) {
        case 2:
          return result + t('detail.index.5qtkk97a2e64')
        case 3:
          return result + t('detail.index.5qtkk97a2e65') + obj.hba_conf.kafka_ip + t('detail.index.5qtkk97a2e35')
        case 4:
          return result + t('detail.index.5qtkk97a2e66')
        default:
          return result + t('detail.index.5qtkk97a2e63')
      }
    case 2:
      return result + t('detail.index.5qtkk97a2e67') + obj.hba_conf.error_message + t('detail.index.5qtkk97a2e35')
    default:
      return result + t('detail.index.5qtkk97a2e40') + t('detail.index.5qtkk97a2e35')
  }
}


const handleLog = (row) => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 3
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
              errorCount: offlineCounts['errorCount'],
              checkErrorCount: offlineCounts['checkErrorCount']
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
              errorCount: onlineCounts['errorCount'],
              checkErrorCount: onlineCounts['checkErrorCount']
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
