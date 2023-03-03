<template>
  <div class="jdbc-instance-table">
    <div v-for="(node, index) in data.nodeList" :key="index">
      <div class="node-detail-c">
        <div class="flex-col-start mr">
          <a-tag color="green" bordered v-if="node.os">{{ node.os }}</a-tag>
          <a-tag class="mb-s" bordered>未检测到系统</a-tag>
          <div class="flex-row mb-s">
            <div class="mr-s" style="width: 160px;">IP地址: {{ node.ip }}</div>
            <icon-code-square :size="25" style="cursor: pointer;" @click="showTerminal(node.ip)" />
          </div>
          <div>端口: {{ node.port }}</div>
        </div>
        <div class="flex-col mr">
          <div class="node-role">{{ node.role === 'MASTER' ? '主' : '备' }}节点</div>
          <a-tag v-if="node.state === -1" bordered>未连接</a-tag>
          <a-tag v-if="node.state === 0" color="red" bordered>连接失败</a-tag>
          <a-tag v-if="node.state === 1" color="green" bordered>连接成功</a-tag>
        </div>
        <div class="flex-col mr">
          <div class="monitor-data">{{ node.connNum ? node.connNum : '--' }}</div>
          <div>连接数</div>
        </div>
        <div class="flex-col mr">
          <div class="monitor-data">{{ node.qps ? node.qps : '--' }}</div>
          <div>qps</div>
        </div>
        <div class="flex-col mr">
          <div class="monitor-data">{{ node.tps ? node.tps : '--' }}</div>
          <div>tps</div>
        </div>
        <div class="flex-col mr" style="width: 120px;">
          <div class="monitor-data">{{ node.tableSpaceUsed ? node.tableSpaceUsed : '--' }}MB</div>
          <div>表空间占用</div>
        </div>
        <div class="flex-col mr" style="width: 120px;">
          <div class="monitor-data">{{ node.memoryUsed ? node.memoryUsed : '--' }}GB</div>
          <div>内存占用</div>
        </div>
        <!-- <div class="flex-col mr">
          <a-link>详情</a-link>
        </div> -->
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
const showTerminal = (ip: string) => {
  console.log('show terminal')
  if (ip) {
    hostPwdRef.value?.open(ip)
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

  .node-role {
    width: 50px;
    height: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: var(--color-text-4);
    margin-bottom: 10px;
  }

  .monitor-data {
    font-size: 22px;
    margin-bottom: 10px;
  }
}
</style>