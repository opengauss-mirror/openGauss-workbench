<template>
  <a-modal v-if="data.show" :mask-closable="false" :esc-to-close="false" :visible="data.show"
           :title="$t('components.openLooKeng.5mpiji1qpcc66')" :modal-style="{ width: '50vw' }"
           hide-cancel @cancel="handleClose" @ok="handleClose">
    <div class="custom-control-container">
      <a-tabs type="card-gutter" v-model:active-key="data.hostId" @change="handleChangeTab">
        <a-tab-pane v-for="(item, index) in data.hosts" :key="item.hostId" :title="item.publicIp">
          <div :id="`xterm_${item.hostId}`" class="xterm"></div>
        </a-tab-pane>
      </a-tabs>

      <host-pwd-dlg ref="hostPwdRef" @finish="handleAddHostByPassword($event)"></host-pwd-dlg>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import {KeyValue} from '@/types/global'
import Socket from '@/utils/websocket'
import {reactive, ref, onMounted, onBeforeUnmount, nextTick} from 'vue'
import {hostListAll, openSSH} from '@/api/ops'
import {WsConnectType} from '@/types/ops/install'
import {Terminal} from 'xterm'
import { FitAddon } from '@xterm/addon-fit'
import {AttachAddon} from '@xterm/addon-attach'
import 'xterm/css/xterm.css'
import HostPwdDlg from '@/views/monitor/customControl/HostPwdDlg.vue'
import {debounce} from '@antv/x6/lib/util/function/function'

const data = reactive<KeyValue>({
  hosts: [],
  loading: false,
  hostId: '',
  rootPassword: '',
  hostListLoading: false,
  hostList: [],
  hostObj: {},
  isSuccess: true,
  socketMap: {},
  show: false
})

const termTerminal = ref<Terminal>()

onBeforeUnmount(() => {
  if (Object.keys(data.socketMap).length > 0) {
    for (let key of Object.keys(data.socketMap)) {
      data.socketMap[key].destroy()
    }
  }
})

const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)
const openDlgToValid = (hostId: string) => {
  if (data.hostObj[hostId]) {
    const excludeHost: Array<KeyValue> = []
    Object.keys(data.hostObj).forEach((key: string) => {
      if (key !== hostId) {
        excludeHost.push(data.hostObj[key])
      }
    })
    hostPwdRef.value?.open(excludeHost)
  }
}

const handleAddHost = (hostData: KeyValue) => {
  if (hostData.hostId) {
    data.hosts.push(data.hostObj[hostData.hostId])
    handleConnect(hostData)
  }
}

const handleAddHostByPassword = (hostData: KeyValue) => {
  data.hostObj[hostData.hostId].isRemember = true
  handleConnect(hostData)
}

const onTerminalResize = () => {
  window.addEventListener('resize', onResize())
}

const onResize = debounce(function () {
  setTimeout(() => {
    fitAddon.value?.fit()
  }, 500)
})

const getHostList = (hostIdList: Array<string>) => {
  data.hostListLoading = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.hostList = []
      res.data.forEach((item: KeyValue) => {
        data.hostObj[item.hostId] = item
        data.hostList.push({
          label: `${item.privateIp}(${item.publicIp})`,
          value: item.hostId
        })
      })
      if (data.hostList.length > 0) {
        data.hostId = data.hostList[0].value
        if (!data.hostObj[data.hostId].isRemember) {
          openDlgToValid(data.hostId)
        } else {
          handleAddHost(data.hostObj[data.hostId])
        }
      }
      hostIdList.splice(0, 1)
      // create all tab
      hostIdList.forEach((id: string) => {
        data.hosts.push(data.hostObj[id])
      })
    }
  }).finally(() => {
    data.hostListLoading = false
  })
}

const handleConnect = (hostData: any) => {
  data.hostId = hostData.hostId
  data.rootPassword = hostData.password
  openSocket(hostData.hostId)
}

const handleClose = () => {
  data.show = false
  if (Object.keys(data.socketMap).length) {
    for (let key of Object.keys(data.socketMap)) {
      data.socketMap[key].destroy()
    }
  }
  data.hosts = []
  data.hostList = []
  data.socketMap = {}
  return true
}

const handleChangeTab = () => {
  const hostData = data.hostObj[data.hostId]
  if (!hostData.isRemember) {
    openDlgToValid(data.hostId)
  } else if (!data.socketMap[data.hostId]) {
    handleConnect(hostData)
  }
}

const openSocket = (hostId: string) => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const terminalSocket = new Socket({url: `custom_terminal_${socketKey}`})
  terminalSocket.onopen(() => {
    const param = {
      hostId: hostId,
      rootPassword: data.rootPassword,
      wsConnectType: WsConnectType.SSH,
      businessId: `custom_terminal_${socketKey}`
    }
    data.loading = true
    initTerm(term, terminalSocket.ws, hostId)
    openSSH(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        data.isSuccess = false
        terminalSocket.destroy()
      } else {
        data.socketMap[hostId] = terminalSocket
        data.isSuccess = true
      }
    }).catch((error: any) => {
      data.isSuccess = false
      term.writeln(error.toString())
      terminalSocket.destroy()
    }).finally(() => {
      data.loading = false
    })
  })
}

let fitAddon = ref<FitAddon | undefined>()

const initTerm = (term: Terminal, ws: WebSocket | undefined, hostId: string) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    fitAddon.value = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon.value)
    term.open(document.getElementById(`xterm_${hostId}`) as HTMLElement)
    fitAddon.value.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
  }
}

const getTermObj = (): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows: 40,
    cols: 200,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  })
}

const open = (hostIdList: Array<string>) => {
  data.show = true
  getHostList(hostIdList)
  onTerminalResize()
}

defineExpose({
  open
})

</script>

<style lang="less" scoped>
:deep(.arco-tabs-nav-tab) {
  color: var(--color-text-1);
}

.custom-control-container {
  padding: 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;

  .top-label {
    white-space: nowrap;
  }

  .xterm {
    width: 100%;
    height: 100%;
  }
}
</style>
