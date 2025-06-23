<template>
  <div class="detail-container common-layout" id="migrationDetail">
    <div class="mainDetail">
      <div class="leftContent">
        <div class="top-content basic-title">
          <img src="@/assets/images/list.png" width="40" height="40" />
          <div class="title-right">
            <el-tooltip :content="task.taskName">
              <div class="name-text">{{ task.taskName }}</div>
            </el-tooltip>
            <el-tag :type="statusColorMap[task?.execStatus] || '--'" class="status-tag">{{
              execStatusMap(task.execStatus)
              }}</el-tag>
          </div>
        </div>
        <div class="bottom-content basic-info">
          <div class="info-title">{{ t('detail.index.baseInfo') }}</div>
          <div class="basicItem" :key="item.label" v-for="(item) in descData" style="margin-bottom:16px">
            <TextTooltip  class="basicLable" :content="item?.label"></TextTooltip>
            <TextTooltip class="basicValue" :content="item?.value"></TextTooltip>
          </div>
        </div>
      </div>
      <div class="rightContent">
        <div class="top-content statistic">
          <div class="card-area">
            <statistic-card :count="totalCount.total" :description="$t('detail.index.subTaskTotal')" type="default"
              class="mr-16"></statistic-card>
            <statistic-card :count="totalCount.finishCount" :description="$t('detail.index.migrationComplete')"
              type="success" class="mr-4"></statistic-card>
            <statistic-card :count="totalCount.errorCount" :description="$t('detail.index.migrationFail')" type="danger"
              class="mr-4"></statistic-card>
            <statistic-card :count="totalCount.checkErrorCount" :description="$t('detail.index.preCheckFail')"
              type="danger" class="mr-4"></statistic-card>
            <statistic-card :count="totalCount.runningCount" :description="$t('detail.index.execution')" type="primary"
              class="mr-4"></statistic-card>
            <statistic-card :count="totalCount.notRunCount" :description="$t('detail.index.notStarted')" type="wait"
              class="mr-4"></statistic-card>
          </div>
          <div class="button-area">
            <div class="switchIntervalUpdate">
              <div class="switchDesc">
                {{ switchRefreshText }}
              </div>
              <el-switch v-model="autoRefresh" :active-value="true" :inactive-value="false" size="small"
                @change="switchIntervalQueryList"></el-switch>
            </div>
            <div class="processTask">
              <el-button type="primary" @click="stopTask">{{ t('detail.index.migrationEnd') }}</el-button>
              <el-popconfirm :title="$t('list.index.5q08sf2dk800')" @confirm="deleteTheTask">
                <template #reference>
                  <el-button :disabled="task.execStatus !== 0 && task.execStatus !== 2">{{ t('detail.index.delTask')
                    }}</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
        <div class="bottom-content main-list">
          <div class="list-title">
            {{ t('detail.index.subTaskList') }}
          </div>
          <div class="updateBtn">
            <el-button @click="getSubTaskList">
              <template #icon>
                <icon-sync />
              </template>
              <template #default>{{ $t('list.index.5q08sf2dj240') }}</template>
            </el-button>
          </div>
          <div class="main-table openDesignTableArea">
            <el-table :data="tableData" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme">
              <el-table-column :label="$t('detail.index.5q09asiwg7s0')" prop="id"></el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwgb40')" ellipsis tooltip>
                <template #default="scope">
                  {{ `${scope.row.sourceDbHost}:${scope.row.sourceDbPort}` }}
                </template>
              </el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwifk0')" prop="sourceDb" ellipsis
                tooltip></el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwijw0')" ellipsis tooltip>
                <template #default="scope">
                  {{ `${scope.row.targetDbHost}:${scope.row.targetDbPort}` }}
                </template>
              </el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwing0')" prop="targetDb" ellipsis
                tooltip></el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwiqk0')" ellipsis tooltip>
                <template #default="scope">
                  {{
                    scope.row.migrationModelId === TaskMode.Offline
                      ? $t('detail.index.5q09asiwiyc0')
                      : $t('detail.index.5q09asiwj1o0')
                  }}
                </template>
              </el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwj4g0')" ellipsis tooltip>
                <template #default="scope">
                  <span class="mac-txt" @click="handleTerminal(scope.row)"><icon-code-square />
                    {{ `${scope.row.runHost}（${scope.row.runHostname}）` }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwjvg0')" :min-width="180" ellipsis tooltip>
                <template #default="scope">
                  <el-tag class="minWid88" :type="statusColorMap[scope.row.execStatus] || '--'"> {{
                    execSubStatusMap(scope.row.execStatus)
                  }}</el-tag>
                  <el-tooltip :title="titleMap(scope.row.execStatus)">
                    <template #default>
                      <icon-close-circle-fill
                        v-if="scope.row.execStatus === SUB_TASK_STATUS.MIGRATION_ERROR || scope.row.execStatus === SUB_TASK_STATUS.CHECK_FAILED"
                        size="14" style="color: #ff7d01; margin-left: 3px; cursor: pointer" />
                    </template>
                    <template #content>
                      <div v-if="scope.row.execStatus === SUB_TASK_STATUS.MIGRATION_ERROR" class="error-tips">{{
                        scope.row.statusDesc }}</div>
                      <div v-if="scope.row.execStatus === SUB_TASK_STATUS.CHECK_FAILED" class="error-tips">
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'service_availability')">{{
                          parseServiceAvailability(scope.row.statusDesc) }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'database_connect')">{{
                          parseDatabaseConnect(scope.row.statusDesc) }}</p>
                        <p>{{ parseDatabasePermission(scope.row.statusDesc) }}</p>
                        <p
                          v-if="judgeKeyExist(scope.row.statusDesc, 'increment_param') || judgeKeyExist(scope.row.statusDesc, 'reverse_param')">
                          {{ parselogParameter(scope.row.statusDesc) }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'lower_param')">{{
                          parseLowerParameter(scope.row.statusDesc) }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'disk_space')">{{
                          parseDiskSpace(scope.row.statusDesc)
                        }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'mysql_encryption')">{{
                          parseMysqlEncryption(scope.row.statusDesc) }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'sql_compatibility')">{{
                          parseOpenGaussBDB(scope.row.statusDesc) }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'replication_slots')">{{
                          parseReplicationNumber(scope.row.statusDesc) }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'enable_slot_log')">{{
                          parseEnableSlotLog(scope.row.statusDesc) }}</p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'hba_conf')">{{
                          parseHbaConf(scope.row.statusDesc) }}
                        </p>
                        <p v-if="judgeKeyExist(scope.row.statusDesc, 'gtid_set')">{{
                          parseGtidSet(scope.row.statusDesc) }}
                        </p>
                      </div>
                    </template>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column :label="$t('detail.index.5q09asiwka80')" align="center" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" :disabled="scope.row.execStatus === SUB_TASK_STATUS.NOT_RUN" type="text"
                    @click="handleDetail(scope.row)">
                    {{
                      $t('detail.index.5q09asiwkds0')
                    }}
                  </el-button>
                  <el-popconfirm :title="tooltipMap(scope.row.checkDataLevelingAndIncrementFinish)"
                    @confirm="stopSubIncrease(scope.row)">
                    <template #reference>
                      <el-button v-if="(scope.row.migrationModelId === TaskMode.Online &&
                        scope.row.execStatus ===
                        SUB_TASK_STATUS.INCREMENTAL_RUNNING) ||
                        scope.row.execStatus === SUB_TASK_STATUS.INCREMENTAL_FINISHED
                      " size="small" type="text" :loading="scope.row.execStatus === SUB_TASK_STATUS.INCREMENTAL_FINISHED
                            ">
                        {{
                          $t('detail.index.5q09asiwkkw0')
                        }}
                      </el-button>
                    </template>

                  </el-popconfirm>
                  <el-button v-if="scope.row.migrationModelId === TaskMode.Online &&
                    scope.row.execStatus === SUB_TASK_STATUS.INCREMENTAL_STOPPED
                  " size="small" type="text" @click="startSubReverse(scope.row)">
                    {{
                      $t('detail.index.5q09asiwkq40')
                    }}
                  </el-button>
                  <el-button v-if="scope.row.execStatus !== SUB_TASK_STATUS.MIGRATION_FINISH" size="small" type="text"
                    @click="stopSubTask(scope.row)">
                    {{
                      $t('detail.index.5q09asiwl5g0')
                    }}
                  </el-button>
                  <el-button size="small" type="text" @click="handleLog(scope.row)">
                    {{
                      $t('detail.index.5q09asiwlac0')
                    }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination :total="total" :layout="layout" v-model:page-size="pagination.pageSize"
              v-model:current-page="pagination.pageNum" @change="getSubTaskList"></el-pagination>
          </div>
        </div>
      </div>
    </div>
    <mac-terminal v-model:open="terminalVisible" :host="macHost" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, computed, provide } from 'vue'
import StatisticCard from '@/components/statisticCard'
import MacTerminal from './components/MacTerminal.vue'
import { stop, deleteTask } from '@/api/list'
import usePagination from '@/utils/usePagination'
import showMessage from '@/utils/showMessage'
import TextTooltip from "@/components/textTooltip/index.vue";
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
import { useRouter, useRoute } from 'vue-router'
import { SUB_TASK_STATUS } from '@/utils/constants'
const route = useRoute();
const router = useRouter();
const { t } = useI18n()

const { currentTheme } = useTheme()
const autoRefresh = ref(true)
const switchRefreshText = computed(() => {
  return autoRefresh.value ? t('components.SubTaskDetail.autoRefresh') : t('components.SubTaskDetail.stoprefresh')
})
const task = ref({})
const hosts = ref([]);
const descData = computed(() => [
  {
    label: t('detail.index.taskName'),
    value: task.value?.taskName || '--',
  },
  {
    label: t('detail.index.5q09asiwnow0'),
    value: hosts.value.length ? `${t('detail.index.5q09efwo3nc0', {
      num: hosts.value.length
    })}（${hosts.value?.map((item) => item.hostName)}）` : '--',
  },
  {
    label: t('detail.index.5q09asiwnks0'),
    value: task.value.createUser || '--'
  },

  {
    label: t('detail.index.5q09asiwnrs0'),
    value: task.value.createTime || '--'
  },
  {
    label: t('detail.index.5q09asiwnw00'),
    value: task.value.execTime || '--'
  }
])

let timerTop = null
let timerDown = null
let timerStatus = null

const {
  total,
  pagination,
  layout
} = usePagination()
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
    30: t('components.SubTaskDetail.incrementError'),
    40: t('components.SubTaskDetail.reverseError'),
    100: t('detail.index.5q09asiwne80'),
    500: t('detail.index.5q09asiwngg0'),
    1000: t('detail.index.5q09asiwnik0'),
    3000: t('detail.index.5q09asiwlca0')
  }
  return maps[status]
}

const statusColorMap = {
  0: 'info',
  1: 'primary',
  2: 'primary',
  3: 'primary',
  4: 'primary',
  5: 'primary',
  6: 'primary',
  7: 'primary',
  8: 'primary',
  9: 'primary',
  10: 'primary',
  11: 'primary',
  12: 'primary',
  13: 'primary',
  30: 'danger',
  40: 'danger',
  100: 'success',
  500: 'danger',
  1000: 'primary',
  3000: 'danger'
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

const subTaskDetailVisible = ref(false)
const subTaskId = ref()
const tabIndex = ref(1)

const terminalVisible = ref(false)
const macHost = ref({})

const switchIntervalQueryList = () => {
  // Determine whether it is enabled, and poll if it is enabled
  if (autoRefresh.value) {
    // Query the APIs related to polling
    queryDetailInfo()
  }
}

const handleTerminal = (row) => {
  terminalVisible.value = true
  macHost.value = row
}

const handleDetail = (row) => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 1
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationSubTaskDetail`,
    query: {
      id: row.id
    }
  })
}

provide('subTaskId', subTaskId)
const judgeKeyExist = (content, key) => {
  let result = false;
  const obj = JSON.parse(content);
  if (key in obj) {
    result = true;
  }
  return result;
}

const parseServiceAvailability = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2eo1')
  if (obj.service_availability === 0) {
    result = result + t('detail.index.5qtkk97a2eo2')
  } else {
    result = result + t('detail.index.5qtkk97a2eo3')
  }
  return result
}

const parseDatabaseConnect = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2eo4')
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
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2eo9')
  if ("full_permission" in obj) {
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
    if (obj.increment_permission.mysql === 0) {
      result = result + t('detail.index.5qtkk97a2e16')
    } else if (obj.increment_permission.mysql === 1) {
      result = result + t('detail.index.5qtkk97a2e17')
    } else {
      result = result + t('detail.index.5qtkk97a2e18')
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
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e28')
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
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e41')
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
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e36')
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
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e46')
  if (obj.mysql_encryption.result === 0) {
    result = result + t('detail.index.5qtkk97a2e47')
  } else if (obj.mysql_encryption.result === 1) {
    result = result + t('detail.index.5qtkk97a2e48') + obj.mysql_encryption.encryption + t('detail.index.5qtkk97a2e49') + obj.mysql_encryption.valid_encryption + t('detail.index.5qtkk97a2e35')
  }
  return result
}

const parseOpenGaussBDB = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e50')
  if (obj.sql_compatibility.result === 0) {
    result = result + t('detail.index.5qtkk97a2e51')
  } else if (obj.sql_compatibility.result === 1) {
    result = result + t('detail.index.5qtkk97a2e52') + obj.sql_compatibility.sql_compatibility + t('detail.index.5qtkk97a2e53') + obj.sql_compatibility.valid_sql_compatibility + t('detail.index.5qtkk97a2e35')
  }
  return result
}

const parseReplicationNumber = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e54')
  if (obj.replication_slots.result === 0) {
    result = result + t('detail.index.5qtkk97a2e55')
  } else if (obj.replication_slots.result === 1) {
    result = result + obj.replication_slots.replication_number + t('detail.index.5qtkk97a2e56')
  }
  return result
}

const parseEnableSlotLog = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e58')
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

const parseGtidSet = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2e68')

  switch (obj.gtid_set.result) {
    case 0:
      return result + t('detail.index.5qtkk97a2e70')
    case 1:
      if (obj.gtid_set.error_message !== undefined) {
        return result + t('detail.index.5qtkk97a2e67') + obj.gtid_set.error_message + t('detail.index.5qtkk97a2e35')
      }
      return result = result + t('detail.index.5qtkk97a2e69')
    default:
      return result + t('detail.index.5qtkk97a2e40') + t('detail.index.5qtkk97a2e35')
  }
}

const handleLog = (row) => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 3
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationSubTaskDetail`,
    query: {
      id: row.id,
      tab: 'log'
    }
  })
}

const deleteTheTask = async () => {
  await deleteTask(task.value.id)
  showMessage('success', 'Delete success')
}

// stop task
const stopTask = async () => {
  await stop(task.value.id)
  showMessage('success', 'Stop success')
  getTaskDetail()
  getSubTaskList()
}

// stop sub task full
const stopSubTask = (row) => {
  subTaskFinish(row.id).then(() => {
    showMessage('success', 'Stop success')
    loopSubTaskStatus()
    getSubTaskList()
  })
}

// stop sub task increase
const stopSubIncrease = (row) => {
  subTaskStopIncremental(row.id).then(() => {
    showMessage('success', 'Stop success')
    loopSubTaskStatus()
    getSubTaskList()
  })
}

// start sub task reverse
const startSubReverse = (row) => {
  subTaskStartReverse(row.id)
    .then(() => {
      showMessage('success', 'Start success')
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
        showMessage('error', t('detail.index.5qtkk99a2eo0'))
      }
    })
}

const loopSubTaskStatus = (loopQuery) => {
  timerStatus && clearTimeout(timerStatus)
  const id = window.$wujie?.props.data.id || 74
  if (loopQuery === 'loopQuery' && !autoRefresh.value) {
    return
  }
  refreshStatus(id)
    .then(() => {
      if (task.value.execStatus !== 2 && autoRefresh.value) {
        timerStatus = setTimeout(() => {
          loopSubTaskStatus('loopQuery')
        }, 3000)
      }
    })
    .catch(() => {
      timerStatus && clearTimeout(timerStatus)
      timerStatus = null
    })
}

const getSubTaskList = (loopQuery) => {
  timerDown && clearTimeout(timerDown)
  const id = window.$wujie?.props.data.id || 74
  if (loopQuery === 'loopQuery' && !autoRefresh.value) {
    return
  }
  subTaskList(id, {
    ...pagination.value
  })
    .then((res) => {
      tableData.value = res.rows.map(item => {
        return item
      })

      total.value = res.total
      if (task.value.execStatus !== 2) {
        timerDown = setTimeout(() => {
          getSubTaskList('loopQuery')
        }, 5000)
      }
    })
    .catch((err) => {
      timerDown && clearTimeout(timerDown)
      timerDown = null
    })
}

const totalCount = ref({
  total: 0,
  notRunCount: 0,
  runningCount: 0,
  finishCount: 0,
  errorCount: 0,
  checkErrorCount: 0
})

const getTaskDetail = (loopQuery) => {
  timerTop && clearTimeout(timerTop)
  const id = window.$wujie?.props.data.id || 74

  if (loopQuery === 'loopQuery' && !autoRefresh.value) {
    return
  }
  taskDetail(id).then((res) => {
    task.value = res.data.task
    Object.keys(totalCount.value).forEach(e => {
      totalCount.value[e] = 0 + res.data.offlineCounts[e] + res.data.onlineCounts[e]
    })
    hosts.value = res.data.hosts
    if (task.value.execStatus !== 2) {
      timerTop = setTimeout(() => {
        getTaskDetail('loopQuery')
      }, 5000)
    }
  })
    .catch(() => {
      timerTop && clearTimeout(timerTop)
      timerTop = null
    })
}

onMounted(() => {
  queryDetailInfo();
})

const queryDetailInfo = () => {
  getTaskDetail()
  getSubTaskList()
  setTimeout(() => {
    loopSubTaskStatus()
  }, 3000)
}

onBeforeUnmount(() => {
  timerTop && clearTimeout(timerTop)
  timerDown && clearTimeout(timerDown)
  timerStatus && clearTimeout(timerStatus)
})
</script>
<style lang="less" scoped>
.detail-container {
  height: calc(100vh - 114px);
  min-height: calc(100vh - 114px);
  background-color: var(--o-bg-color-light);
  padding: 20px 24px 28px 20px;
  overflow-x: auto;

  .mainDetail {
    display: flex;
    gap: 24px;
    height: 100%;
    min-width: 1000px;

    .leftContent {
      width: 292px;
      display: flex;
      flex-direction: column;
      gap: 16px;

      .basic-title {
        height: 80px;
        background-color: var(--o-bg-color-light2);
        display: flex;
        padding: 0 24px;
        align-items: center;
        gap: 16px;

        img {
          width: 40px;
          height: 40px;
        }

        .title-right {
          flex: 1;
          width: 0;
          display: flex;
          flex-direction: column;
          gap: 8px;
          color: var(--o-text-color-primary);

          .name-text {
            height: 24px;
            line-height: 24px;
            font-size: 16px;
            font-weight: bolder;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .el-tag {
            width: fit-content;
            padding: 4px 24px;
            font-size: 12px;
          }
        }
      }

      .basic-info {
        flex: 1;
        padding: 20px 24px;
        background-color: var(--o-bg-color-light2);
        color: var(--o-text-color-primary);

        .info-title {
          height: 24px;
          line-height: 24px;
          font-size: 16px;
          font-weight: bolder;
        }

        .basicItem {
          display: flex;
          height: 24px;
          margin-top: 12px;
          align-items: center;

          .basicLable {
            width: 106px;
          }

          .basicValue {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }

    .rightContent {
      flex: 1;
      display: flex;
      flex-direction: column;
      width: 0;
      gap: 16px;

      .top-content {
        height: 80px;
        display: flex;
        width: 100%;

        .card-area {
          flex: 1;
          display: flex;
          gap: 4px;
          max-width: calc(100% - 192px);

          .main-card {
            max-width: 182px;
            background-color: var(--o-bg-color-light2);
            color: var(--o-text-color-primary);

            &:first-child {
              margin-right: 12px;
            }
          }
        }

        .button-area {
          display: flex;
          flex-direction: column;
          justify-content: space-between;

          .switchIntervalUpdate {
            display: flex;
            gap: 4px;
            place-self: flex-end;
          }
        }
      }

      .bottom-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 24px;
        background-color: var(--o-bg-color-light2);
        overflow: auto;

        .list-title {
          height: 24px;
          font-size: 16px;
          font-weight: 600;
          color: var(--o-text-color-primary);
          margin-bottom: 12px;
        }

        .updateBtn {
          height: 32px;
          margin-bottom: 16px;
        }

        .main-table {
          flex: 1;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
        }
      }
    }
  }
}
</style>
