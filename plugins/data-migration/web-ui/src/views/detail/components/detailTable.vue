<template>
  <div class="ommon-layout" id="migrationDetail">
    <div class="subTableTitle">{{ t('detail.index.subTaskTotal') }}</div>
    <div class="main-table openDesignTableArea minWid912">
      <el-table :data="tableData" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme">
        <el-table-column :label="$t('detail.index.5q09asiwg7s0')" prop="id">
          <template #default="scope">
            <el-button size="small"
              :disabled="scope.row.execStatus === SUB_TASK_STATUS.NOT_RUN || scope.row.execStatus === SUB_TASK_STATUS.CHECK_FAILED"
              type="text" @click="handleDetail(scope.row)">
              {{ scope.row.id }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="$t('detail.index.sourceType')" ellipsis tooltip prop="sourceDbType">
          <template #default="scope">
            {{ scope.row.sourceDbType || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('detail.index.5q09asiwgb40')" ellipsis tooltip>
          <template #default="scope">
            {{ `${scope.row.sourceDbHost}:${scope.row.sourceDbPort}` }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('detail.index.5q09asiwifk0')" prop="sourceDb" ellipsis tooltip></el-table-column>
        <el-table-column :label="$t('detail.index.5q09asiwijw0')" ellipsis tooltip>
          <template #default="scope">
            {{ `${scope.row.targetDbHost}:${scope.row.targetDbPort}` }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('detail.index.5q09asiwing0')" prop="targetDb" ellipsis tooltip></el-table-column>
        <el-table-column :label="$t('detail.index.5q09asiwiqk0')" ellipsis tooltip>
          <template #default="scope">
            {{
              scope.row.migrationModelId === TaskMode.Offline
                ? $t('detail.index.5q09asiwiyc0')
                : $t('detail.index.5q09asiwj1o0')
            }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('detail.index.5q09asiwj4g0')" :min-width="150" ellipsis tooltip>
          <template #default="scope">
            <span class="mac-txt" @click="handleTerminal(scope.row)"><icon-code-square />
              {{ `${scope.row.runHost}（${scope.row.runHostname}）` }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('detail.index.5q09asiwjvg0')" :min-width="140" ellipsis tooltip>
          <template #default="scope">
            <el-tag style="min-width: 88px;" :type="statusColor(scope.row.execStatus, scope.row.migrationModelId) || '--'"
            > {{
              execSubStatusMap(scope.row.execStatus, scope.row.migrationModelId)
            }}</el-tag>
            <el-tooltip :title="titleMap(scope.row.execStatus)" :key="scope.row.id">
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
        <el-table-column :label="$t('detail.index.5q09asiwka80')" :min-width="112" fixed="right">
          <template #default="scope">
            <a-popconfirm :content="tooltipMap(scope.row.checkDataLevelingAndIncrementFinish)" type="warning"
              :ok-text="$t('list.index.confirm')" :cancel-text="$t('list.index.cancel')" @ok="stopSubIncrease(scope.row)"
              class="aPopConfirmStyle">
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
            </a-popconfirm>
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
    </div>
    <div class="terminaStyle">
      <mac-terminal v-model:open="terminalVisible" :host="macHost" />
    </div>

    <ReverseDetailDia v-if="reverseVisible" @closeDialog="closeDialog" :replicationData="replicationData"
      :reverseConfig="reverseConfig"></ReverseDetailDia>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, computed, provide, watch } from 'vue'
import showMessage from '@/utils/showMessage';
import MacTerminal from './MacTerminal.vue'
import {
  subTaskList,
  subTaskFinish,
  subTaskStartReverse,
  subTaskStopIncremental
} from '@/api/detail'
import useTheme from '@/hooks/theme'
import { useI18n } from 'vue-i18n'
import { SUB_TASK_STATUS } from '@/utils/constants'
import ReverseDetailDia from "@/views/subTaskDetail/migrationProcess/reverseDetail.vue";
const { t } = useI18n()

// This is where the corresponding taskId is received
const props = defineProps({
  taskId: {
    type: [String, Number],
    default: '',
  },
  listRefresh: {
    type: Boolean,
    default: true,
  }
})

const emits = defineEmits(['openTmerminal']);
const reverseVisible = ref(false);
const { currentTheme } = useTheme()
const task = ref({})
let timerDown = null

const tableData = ref([])

const reverseConfig = ref({})
const replicationData = ref([])

const TaskMode = reactive({
  Offline: 1,
  Online: 2
})

// sub task status map
const execSubStatusMap = (status, migrationModelId) => {
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
    100: migrationModelId === 2 ? t('list.index.5q08sf2dhj00') : t('detail.index.5q09asiwne80'), // 100 online is the stop, and 100 offline is the end
    500: t('detail.index.5q09asiwngg0'),
    1000: t('detail.index.5q09asiwnik0'),
    3000: t('detail.index.5q09asiwlca0')
  }
  return maps[status]
}

const statusColor = (execStatus, migrationModelId) => {
  const statuColorMap = {
    0: 'info',
    1: 'primary',
    2: 'primary',
    3: 'info',
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
    100: migrationModelId === 2 ? 'info' : 'success', // 100 online is the stop, and 100 offline is the end
    500: 'danger',
    1000: 'primary',
    3000: 'danger'
  }
  console.log(statuColorMap[execStatus], 'statuColorMap[execStatus]')
  return statuColorMap[execStatus]
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

const handleTerminal = (row) => {
  emits("openTmerminal", row)
}
const handleDetail = (row) => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 1
  sessionStorage.setItem('sourceIpPort', row.sourceDbHost + ":" + row.sourceDbPort)
  sessionStorage.setItem('sinkIpPort', row.targetDbHost + ":" + row.targetDbPort)
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
  return result
}

const parseServiceAvailability = (content) => {
  let result = t('detail.index.5qtkk97a2eo1')
  const obj = JSON.parse(content)
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
  if (obj.database_connect?.mysql === 0) {
    result = result + t('detail.index.5qtkk97a2eo5')
  } else {
    result = result + t('detail.index.5qtkk97a2eo6')
  }
  if (obj.database_connect?.opengauss === 0) {
    result = result + t('detail.index.5qtkk97a2eo7')
  } else {
    result = result + t('detail.index.5qtkk97a2eo8')
  }
  return result
}

const parseDatabasePermission = (content) => {
  const obj = JSON.parse(content)
  let result = t('detail.index.5qtkk97a2eo9')
  if (obj?.full_permission) {
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

  if (obj?.increment_permission) {
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

  if (obj?.reverse_permission) {
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

// stop sub task full
const stopSubTask = (row) => {
  subTaskFinish(row.id).then(() => {
    showMessage('success', t('detail.index.subTaskFullStopSuccess'))
    getSubTaskList()
  })
}

// stop sub task increase
const stopSubIncrease = (row) => {
  subTaskStopIncremental(row.id).then(() => {
    getSubTaskList()
  })
}

const closeDialog = () => {
  reverseVisible.value = false;
}
// start sub task reverse
const startSubReverse = (row) => {
  subTaskStartReverse(row.id)
    .then(() => {
      showMessage('success', t('detail.index.subTaskReverseStartSuccess'))
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

const getSubTaskList = (loopQuery) => {
  timerDown && clearTimeout(timerDown)
  const id = props.taskId || 74
  if (loopQuery === 'loopQuery' && !props.listRefresh) {
    // If it's a scheduled query but auto-refresh is turned off, the request is no longer made
    return
  }
  subTaskList(id, {})
    .then((res) => {
      tableData.value = res.rows.map(item => {
        return item
      })
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

onMounted(() => {
  getSubTaskList();
})

// Query the corresponding status and the corresponding one
onBeforeUnmount(() => {
  timerDown && clearTimeout(timerDown)
})
</script>
<style lang="less" scoped>
.ommon-layout {
  width: 100%;
  
  .main-table {
    width: 100%;
  }

  .infoTag {
    color: red;
  }

  .terminaStyle {
    z-index: 1111;
  }
}

.detail-container {
  background-color: var(--o-bg-color-light);
  padding: 20px 24px 28px 20px;
  overflow-x: auto;

  .mainDetail {
    display: flex;
    gap: 24px;
    height: 100%;
    min-width: 900px;

    .rightContent {
      flex: 1;
      display: flex;
      flex-direction: column;
      width: 0;
      gap: 16px;

      .bottom-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 24px;
        background-color: var(--o-bg-color-light2);

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
