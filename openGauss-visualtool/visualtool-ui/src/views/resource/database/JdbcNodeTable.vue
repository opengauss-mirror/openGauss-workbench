<template>
  <div class="jdbc-instance-table">
    <div v-for="(node, index) in data.nodeList" :key="index">
      <div class="node-detail-c">
        <div class="flex-col-start mr">
          <a-tag color="green" bordered v-if="node.os">{{ node.os }}</a-tag>
          <a-tag class="cursor-c mb-s" bordered @click="handleGetOs(node.ip)">{{ $t('database.JdbcNodeTable.5oxhv6qcm6w0')
          }}</a-tag>
          <div class="flex-row mb-s">
            <div class="mr-s" style="width: 160px;">{{ $t('database.JdbcNodeTable.else1') }}: {{ node.ip }}</div>
            <icon-code-square :size="25" style="cursor: pointer;" @click="showTerminal(node.ip)" />
          </div>
          <div>{{ $t('database.JdbcNodeTable.else2') }}: {{ node.port }}</div>
        </div>
        <div class="flex-row mr">
          <div class="node-role mr">{{ node.role === 'MASTER' ? $t('database.JdbcNodeTable.5oxhv6qcnak0') :
            $t('database.JdbcNodeTable.5oxhv6qcnnk0') }}</div>
          <div :class="'node-state-c ' + getNodeStateColor(node.state)"></div>
        </div>
        <div class="flex-col mr">
          <div class="monitor-data">{{ node.connNum ? node.connNum : '--' }}</div>
          <div>{{ $t('database.JdbcNodeTable.5oxhv6qcnuk0') }}</div>
        </div>
        <div class="flex-col mr">
          <div class="monitor-data">{{ node.qps ? node.qps : '--' }}</div>
          <div>qps</div>
        </div>
        <div class="flex-col mr">
          <div class="monitor-data">{{ node.tps ? node.tps : '--' }}</div>
          <div>tps</div>
        </div>
        <div class="flex-col mr" style="width: 130px;">
          <div class="monitor-data">{{ node.tableSpaceUsed ? node.tableSpaceUsed : '--' }}MB</div>
          <div>{{ $t('database.JdbcNodeTable.5oxhv6qco4c0') }}</div>
        </div>
        <div class="flex-col mr" style="width: 120px;">
          <div class="monitor-data">{{ node.memoryUsed ? node.memoryUsed : '--' }}GB</div>
          <div>{{ $t('database.JdbcNodeTable.5oxhv6qcobk0') }}</div>
        </div>
      </div>
      <a-divider v-if="index !== props.nodes.length - 1"></a-divider>
    </div>
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
const data = reactive<KeyValue>({
  socketArr: [],
  nodeList: []
})

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
        data.nodeList[index].state = 1
        // websocket push socketArr
        data.socketArr.push(websocket)
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

.node-detail-c {
  display: flex;
  justify-content: space-around;

  .cursor-c {
    cursor: pointer;
  }

  .node-role {
    width: 50px;
    height: 40px;
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
}
</style>