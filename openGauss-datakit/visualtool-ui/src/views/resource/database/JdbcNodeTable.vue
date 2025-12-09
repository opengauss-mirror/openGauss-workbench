<template>
  <div class="jdbc-instance-table">
    <el-table
      :data="data.nodeList"
      :border="false"
      style="width: 100%"
    >
      <el-table-column prop="ip" :label="$t('database.JdbcNodeTable.else1')">
        <template #default="{ row: record }">
          {{ record.ip || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="port" :label="$t('database.JdbcNodeTable.else2')">
        <template #default="{ row: record }">
          {{ record.port || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="username" :label="$t('database.JdbcInstance.5oxhtcboa240')">
        <template #default="{ row: record }">
          {{ record.username || '--' }}
        </template>
      </el-table-column>

      <el-table-column prop="state" :label="$t('database.JdbcNodeTable.connectionStatus')">
        <template #default="{ row: record }">
          <span class="status-container">
            <span class="status-dot"
                  :class="{ 'error-dot': record.status === 'error', 'success-dot': record.status === 'success', 'info-dot': record.status === 'loading'}" />
            {{record.status}}
          </span>

        </template>
      </el-table-column>
      <template v-for="col in dbSpecificColumns" :key="col.prop">
        <el-table-column
          :prop="col.prop"
          :label="getColumnLabel(col)"
          :min-width="120"
        >
          <template #default="{ row }">
            {{ formatColumnValue(row[col.prop], col.prop) }}
          </template>
        </el-table-column>
      </template>
    </el-table>
  </div>
</template>
<script setup lang="ts">
import { PropType, onMounted, onUnmounted, reactive, ref, computed, watch } from 'vue'
import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import {jdbcNodeMonitor} from '@/api/ops'
import { useI18n } from 'vue-i18n'
import {JDBCType} from "@/types/jdbc";
const { t } = useI18n()
const data = reactive<KeyValue>({
  socketArr: [],
  nodeList: []
})

const props = defineProps({
  jdbcData: {
    type: Array as PropType<KeyValue>,
    required: true
  }
})

onMounted(() => {
  data.nodeList = JSON.parse(JSON.stringify(props.jdbcData.nodes))
  openMonitor()
})

onUnmounted(() => {
  if (data.socketArr.length) {
    data.socketArr.forEach((item: Socket<any, any>) => {
      if (item) {
        item.destroy()
      }
    })
  }
})

const emits = defineEmits(['validRes'])

const stateComp = computed(() => {
  const stateList = data.nodeList.filter((item: KeyValue) => {
    return item.state === 1
  })
  if (stateList.length === data.nodeList.length) {
    return true
  }
  return false
})

watch(stateComp, (val: boolean) => {
  if (val) {
    emits('validRes', 1)
  } else {
    emits('validRes', 0)
  }
})

const getNodeStateColor = (state: number) => {
  switch (state) {
    case -1:
      return 'un-check'
    case 1:
      return 'check-pass'
    case 0:
      return 'check-error'
  }
}

const getNodeState = (state?: number): string => {
  if (state == null) {
    return 'error'
  }
  switch (state) {
    case -1:
      return 'checking'
    case 1:
      return 'pass'
    case 0:
      return 'error'
    default:
      return 'error'
  }
}

const openMonitor = () => {
  data.nodeList.filter((item: KeyValue, index: number) => {
    item.loading = false
    item.state = -1
    openNodeMonitor(item, index)
  })
}

const openNodeMonitor = (nodeData: KeyValue, index: number) => {
  const socketKey = new Date().getTime()
  const param = {
    businessId: 'monitor_ops_jdbc_' + nodeData.clusterNodeId + '_' + socketKey,
    dbType: props.jdbcData.dbType
  }
  const websocket = new Socket({ url: `COMMAND_EXEC/${param.businessId}` })
  websocket.onopen(() => {
    data.nodeList[index].loading = true
    jdbcNodeMonitor(nodeData.clusterNodeId, param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        data.nodeList[index].state = 0
        websocket.destroy()
      } else {
        data.nodeList[index].state = 1
        data.socketArr.push(websocket)
      }
    }).catch((error: any) => {
      console.log('Websocket error:', error)
      data.nodeList[index].state = 0
      websocket.destroy()
    }).finally(() => {
      data.nodeList[index].loading = false
    })
  })
  websocket.onclose(() => {
    data.nodeList[index].state = 0
  })
  websocket.onerror((error) => {
    console.error('WebSocket Error:', error)
    data.nodeList[index].state = 0
  })
  websocket.onmessage((messageData: any) => {
    const eventData = JSON.parse(messageData)
    if (Object.keys(eventData).length) {
      data.nodeList[index].status = eventData.status
      if (props.jdbcData.dbType === JDBCType.openGauss || props.jdbcData.dbType === JDBCType.PostgreSQL) {
        data.nodeList[index].connNum = eventData.connNum
        data.nodeList[index].lockNum = eventData.lockNum
        data.nodeList[index].sessionNum = eventData.sessionNum
      } else if (props.jdbcData.dbType === JDBCType.MySQL) {
        data.nodeList[index].tableSpaceUsed = Number(eventData.tableSpaceUsed / 1024 / 1024).toFixed(2)
        data.nodeList[index].memoryUsed = Number(eventData.memoryUsed / 1024 / 1024 / 1024).toFixed(2)
        data.nodeList[index].connNum = eventData.connNum
        data.nodeList[index].qps = eventData.qps
        data.nodeList[index].tps = eventData.tps
        data.nodeList[index].role = eventData.role
      } else if (props.jdbcData.dbType === JDBCType.Milvus) {
        data.nodeList[index].collectionNum = eventData.collectionNum
        data.nodeList[index].apiResponseDelay = eventData.apiResponseDelay
      } else {
        data.nodeList[index].unassigned_shards = eventData.unassigned_shards
        data.nodeList[index].active_shards_percent_as_number = Number(eventData.active_shards_percent_as_number).toFixed(2)
        data.nodeList[index].clusterStatus = eventData.clusterStatus
      }
    }
  })
}

const dbColumnConfigs = {
  [JDBCType.MySQL]: [
    { prop: 'tps', label: 'TPS' },
    { prop: 'qps', label: 'QPS' },
    { prop: 'connNum', i18nKey: 'database.JdbcNodeTable.5oxhv6qcnuk0'},
    { prop: 'tableSpaceUsed', i18nKey: 'database.JdbcNodeTable.5oxhv6qco4c0'},
    { prop: 'memoryUsed', i18nKey: 'database.JdbcNodeTable.5oxhv6qcobk0'}
  ],
  [JDBCType.openGauss]: [
    { prop: 'connNum', i18nKey: 'database.JdbcNodeTable.5oxhv6qcnuk0'},
    { prop: 'sessionNum', i18nKey: 'database.JdbcNodeTable.else7'},
    { prop: 'lockNum', i18nKey: 'database.JdbcNodeTable.else8'}
  ],
  [JDBCType.PostgreSQL]: [
    { prop: 'connNum', i18nKey: 'database.JdbcNodeTable.5oxhv6qcnuk0'},
    { prop: 'sessionNum', i18nKey: 'database.JdbcNodeTable.else7'},
    { prop: 'lockNum', i18nKey: 'database.JdbcNodeTable.else8' }
  ],
  [JDBCType.Milvus]: [
    { prop: 'collectionNum', i18nKey: 'database.JdbcNodeTable.collectionNum'},
    { prop: 'apiResponseDelay', i18nKey: 'database.JdbcNodeTable.apiResponseDelay', formatter: (v) => v ? `${v}ms` : '--' }
  ],
  [JDBCType.Elasticsearch]: [
    { prop: 'unassigned_shards', i18nKey: 'database.JdbcNodeTable.unassigned_shards'},
    { prop: 'active_shards_percent_as_number', i18nKey: 'database.JdbcNodeTable.active_shards'},
    { prop: 'clusterStatus', i18nKey: 'database.JdbcNodeTable.clusterStatus'},
  ]
}
const dbSpecificColumns = computed(() => {
  return dbColumnConfigs[props.jdbcData.dbType] || []
})
const getColumnLabel = (col) => {
  if (col.label) {
    return col.label
  }
  if (col.labelKey) {
    if (col.labelKey.includes('.')) {
      try {
        const translated = t(col.labelKey)
        return translated
      } catch (error) {
        return col.labelKey
      }
    }
    return col.labelKey
  }
  if (col.i18nKey) {
    try {
      const translated = t(col.i18nKey)
      return translated
    } catch (error) {
      return col.i18nKey
    }
  }
  return col.prop
}

const formatColumnValue = (value, prop) => {
  if (prop === 'active_shards_percent_as_number') {
    if (value == null || value === '') return '--'
    const num = Number(value)
    return isNaN(num) ? '--' : `${num.toFixed(2)}%`
  }else if (prop === 'apiResponseDelay') {
    return value ? `${value}ms` : '--'
  } else if (prop === 'memoryUsed') {
    return value ? `${value}GB` : '--'
  } else if (prop === 'tableSpaceUsed') {
    return value ? `${value}MB` : '--'
  } else {
    return value || '--'
  }
}

</script>
<style lang="less" scoped>
.jdbc-instance-table {
  :deep(.el-table) {
    .el-table__header .el-table__cell {
      padding-top: 0px;
      padding-bottom: 0px;
      padding-left: 8px;
      padding-right: 8px;
    }

    .el-table__body .el-table__cell {
      padding-top: 16px;
      padding-bottom: 16px;
      padding-left: 8px;
      padding-right: 8px;
    }

    .el-table__fixed .el-table__cell,
    .el-table__fixed-right .el-table__cell {
      padding-top: 16px;
      padding-bottom: 16px;
      padding-left: 8px;
      padding-right: 8px;
    }
  }
}

.table-row {
  display: flex;
  align-items: center;
  overflow-x: auto;
  min-width: 100%;
}

.table-cell {
  display: flex;
  align-items: center;

  &.base-info {
    flex: 0 0 350px;
  }

  &.status {
    flex: 0 0 230px;
  }

  &.connect-info {
    flex: 0 0 200px;
  }

  &.table-space {
    flex: 0 0 130px;
  }

  &.memory-used {
    flex: 0 0 130px;
  }
}

.node-role {
  height: 40px;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--color-text-4);
}

.node-state-c {
  width: 15px;
  height: 15px;
  border-radius: 50%;
}

.un-check {
  background-color: gray;
}

.check-pass {
  background-color: green;
}

.check-error {
  background-color: red
}

.status-container {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin: 0 16px 0 0;
}

.info-dot {
  background-color: var(--o-color-info-secondary);
}

.error-dot {
  background-color: var(--o-color-danger);
}

.success-dot {
  background-color: var(--o-color-success);
}

.flex-col-start {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 64px;
  min-width: 70%;
  padding-bottom: 8px;
}

.flex-col {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flex-row {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.mb-s {
  margin-bottom: 8px;
}

.mr {
  margin-right: 16px;
}

.mr-s {
  margin-right: 8px;
}
</style>
