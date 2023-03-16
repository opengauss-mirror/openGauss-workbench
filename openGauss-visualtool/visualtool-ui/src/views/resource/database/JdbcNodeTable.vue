<template>
  <div class="jdbc-instance-table">
    <a-table :data="data.nodeList" :columns="columns" :show-header="false" :pagination="false">
      <template #baseInfo="{ record }">
        <div class="flex-col-start mr">
          <!-- <a-tag class="mb-s" color="green" bordered v-if="record.os">{{ record.os }}</a-tag>
          <a-tag class="cursor-c mb-s" bordered v-else @click="handleGetOs(record.ip)">{{
            $t('database.JdbcNodeTable.5oxhv6qcm6w0')
          }}</a-tag> -->
          <div class="flex-row mb-s">
            <div class="mr-s">{{ $t('database.JdbcNodeTable.else5') }}:</div>
            <div v-if="record.os">{{ record.os }}</div>
            <a-button v-else type="outline" size="mini" @click="handleGetOs(record.ip)">{{
              $t('database.JdbcNodeTable.5oxhv6qcm6w0') }}
            </a-button>
          </div>
          <div class="flex-row mb-s">
            <div class="mr-s" style="max-width: 140px;">{{ $t('database.JdbcNodeTable.else1') }}: {{ record.ip }}</div>
            <icon-code-square :size="25" style="cursor: pointer;" @click="showTerminal(record.ip)" />
          </div>
          <div>{{ $t('database.JdbcNodeTable.else2') }}: {{ record.port }}</div>
        </div>
      </template>
      <template #status="{ record }">
        <div class="flex-row mr">
          <div class="node-role mr-s">{{ getNodeRole(record.role) }}</div>
          <div :class="'node-state-c ' + getNodeStateColor(record.state)"></div>
        </div>
      </template>
      <template #connectNum="{ record }">
        <div class="flex-col mr">
          <div class="monitor-data">{{ record.connNum ? record.connNum : '--' }}</div>
          <div>{{ $t('database.JdbcNodeTable.5oxhv6qcnuk0') }}</div>
        </div>
      </template>
      <template #qps="{ record }">
        <div class="flex-col mr">
          <div class="monitor-data">{{ record.qps ? record.qps : '--' }}</div>
          <div>qps</div>
        </div>
      </template>
      <template #tps="{ record }">
        <div class="flex-col mr">
          <div class="monitor-data">{{ record.tps ? record.tps : '--' }}</div>
          <div>tps</div>
        </div>
      </template>
      <template #tableSpaceUsed="{ record }">
        <div class="flex-col mr" style="width: 130px;">
          <div class="monitor-data">{{ record.tableSpaceUsed ? record.tableSpaceUsed : '--' }}MB</div>
          <div>{{ $t('database.JdbcNodeTable.5oxhv6qco4c0') }}</div>
        </div>
      </template>
      <template #memoryUsed="{ record }">
        <div class="flex-col mr" style="width: 120px;">
          <div class="monitor-data">{{ record.memoryUsed ? record.memoryUsed : '--' }}GB</div>
          <div>{{ $t('database.JdbcNodeTable.5oxhv6qcobk0') }}</div>
        </div>
      </template>
    </a-table>
    <host-pwd-dlg ref="hostPwdRef" @finish="handleShowTerminal($event)"></host-pwd-dlg>
    <host-terminal ref="hostTerminalRef" @finish="handleFinish()"></host-terminal>
  </div>
</template>
<script setup lang="ts">
import { defineProps, PropType, onMounted, reactive, ref, computed, watch } from 'vue'
import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { jdbcNodeMonitor } from '@/api/ops'
import HostPwdDlg from './HostPwdDlg.vue'
import HostTerminal from './HostTerminal.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data = reactive<KeyValue>({
  socketArr: [],
  nodeList: []
})

const columns = computed(() => [
  { slotName: 'baseInfo', width: 370 },
  { slotName: 'status', width: 290 },
  { slotName: 'connectNum', width: 180 },
  { slotName: 'qps', width: 180 },
  { slotName: 'tps', width: 180 },
  { slotName: 'tableSpaceUsed', width: 180 },
  { slotName: 'memoryUsed', width: 180 }
])

const props = defineProps({
  nodes: {
    type: Array as PropType<KeyValue[]>,
    required: true
  }
})

onMounted(() => {
  data.nodeList = JSON.parse(JSON.stringify(props.nodes))
  openMonitor()
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

const getNodeRole = (role?: string) => {
  if (!role) {
    return t('database.JdbcNodeTable.else4')
  } else if (role === 'MASTER') {
    return t('database.JdbcNodeTable.5oxhv6qcnak0')
  } else {
    return t('database.JdbcNodeTable.5oxhv6qcnnk0')
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
    businessId: 'monitor_ops_jdbc_' + nodeData.clusterNodeId + '_' + socketKey
  }
  const websocket = new Socket({ url: `COMMAND_EXEC/${param.businessId}` })
  websocket.onopen(() => {
    data.nodeList[index].loading = true
    jdbcNodeMonitor(nodeData.clusterNodeId, param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        data.nodeList[index].state = 0
        websocket.destroy()
      } else {
        if (res.data.res) {
          data.nodeList[index].state = 1
          // websocket push socketArr
          data.socketArr.push(websocket)
        } else {
          data.nodeList[index].state = 0
          websocket.destroy()
        }
      }
    }).catch(() => {
      data.nodeList[index].state = 0
      websocket.destroy()
    }).finally(() => {
      data.nodeList[index].loading = false
    })
  })
  websocket.onclose(() => {
    data.nodeList[index].state = 0
  })
  websocket.onmessage((messageData: any) => {
    const eventData = JSON.parse(messageData)
    data.nodeList[index].tableSpaceUsed = Number(eventData.tableSpaceUsed / 1024 / 1024).toFixed(2)
    data.nodeList[index].memoryUsed = Number(eventData.memoryUsed / 1024 / 1024 / 1024).toFixed(2)
    data.nodeList[index].connNum = eventData.connNum
    data.nodeList[index].qps = eventData.qps
    data.nodeList[index].tps = eventData.tps
    data.nodeList[index].role = eventData.role
  })
}

const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)

const handleGetOs = (ip: string) => {
  console.log('show terminal')
  if (ip) {
    hostPwdRef.value?.open(ip, 'getOs')
  }
}

const showTerminal = (ip: string) => {
  console.log('show terminal')
  if (ip) {
    hostPwdRef.value?.open(ip, 'terminal')
  }
}

const hostTerminalRef = ref<null | InstanceType<typeof HostTerminal>>(null)
const handleShowTerminal = (sshData: KeyValue) => {
  console.log('show password', sshData)
  hostTerminalRef.value?.open(sshData)
}

const handleFinish = () => {
  console.log('finish')
}

</script>
<style lang="less" scoped>
.jdbc-instance-table {
  padding: 10px;
}

.cursor-c {
  cursor: pointer;
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

.monitor-data {
  font-size: 22px;
  margin-bottom: 10px;
}
</style>