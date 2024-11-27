<template>
  <div class="custom-control-container" id="customControl">
    <div
      class="flex-col-start"
      v-if="data.hosts.length"
    >
      <a-tabs
        class="mb-s"
        type="card-gutter"
        :editable="true"
        @add="handleAdd"
        @delete="handleDelete"
        v-model:active-key="data.hostId"
        show-add-button
        auto-switch
      >
        <a-tab-pane
          v-for="(item, index) in data.hosts"
          :key="item.hostId"
          :title="item.publicIp"
          :closable="data.hosts.length > 1"
        >
          <div
            :id="`xterm_${index}`"
            class="xterm"
          ></div>
        </a-tab-pane>
      </a-tabs>
      <div
        class="mb-s"
        style="color: red; font-weight: bold;"
      >
        {{ $t('components.HostTerminal.else2') }}
      </div>
    </div>
    <div
      v-else
      style="margin-top: 15%;"
    >
      <div
        class="flex-col"
        v-if="data.noHost"
      >
        <a-empty class="mb">{{ $t('customControl.index.else1') }}</a-empty>
        <div class="flex-row">
          <a-button
            type="primary"
            class="mr"
            @click="goHostManage"
          >{{ $t('customControl.index.else2') }}</a-button>
          <a-button
            type="outline"
            @click="getHostList"
          >{{ $t('customControl.index.else3') }}</a-button>
        </div>
      </div>
      <div
        class="flex-col"
        v-else
      >
        <a-empty class="mb">{{ $t('customControl.index.else4') }}</a-empty>
        <a-button
          type="outline"
          @click="getHostList"
        >{{ $t('customControl.index.else5') }}</a-button>
      </div>
    </div>

    <host-pwd-dlg
      ref="hostPwdRef"
      @finish="handleAddHost($event)"
    ></host-pwd-dlg>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { reactive, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { hostListAll, openSSH } from '@/api/ops'
import { WsConnectType } from '@/types/ops/install'
import { Terminal } from 'xterm'
import { FitAddon } from '@xterm/addon-fit'
import { AttachAddon } from '@xterm/addon-attach'
import 'xterm/css/xterm.css'
import HostPwdDlg from './HostPwdDlg.vue'
import { debounce } from '@antv/x6/lib/util/function/function'

const data = reactive<KeyValue>({
  noHost: false,
  hosts: [],
  loading: false,
  hostId: '',
  rootPassword: '',
  hostListLoading: false,
  hostList: [],
  hostObj: {},
  isSuccess: true,
  socketMap: {}
})

const termTerminal = ref<Terminal>()

onMounted(() => {
  getHostList()
  onTerminalResize()
})

onBeforeUnmount(() => {
  if (Object.keys(data.socketMap).length) {
    for (let key in data.socketMap) {
      data.socketMap[key].destroy()
    }
  }
})

const goHostManage = () => {
  window.$wujie?.props.methods.jump({
    name: 'ResourcePhysical'
  })
}

const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)
const openDlgToValid = (hostId: string) => {
  if (data.hostObj[hostId]) {
    hostPwdRef.value?.open([])
  }
}
const handleAdd = () => {
  hostPwdRef.value?.open(data.hosts)
}

const handleAddHost = (hostData: KeyValue) => {
  if (hostData.hostId) {
    const currentHostObj = data.hostObj[hostData.hostId]
    currentHostObj.username = hostData.username
    data.hosts.push(data.hostObj[hostData.hostId])
    handleConnect(hostData, data.hosts.length - 1)
  }
}

const handleDelete = (val: any) => {
  data.hosts = data.hosts.filter((item: KeyValue) => {
    return item.hostId !== val
  })
  nextTick(() => {
    data.hostId = data.hosts[0].hostId
  })
  if (data.socketMap[val]) {
    data.socketMap[val].destroy()
  }
}

const onTerminalResize = () => {
  window.addEventListener('resize', onResize())
}

const onResize = debounce(function () {
  setTimeout(() => {
    fitAddon.value?.fit()
  }, 500)
})

const getHostList = () => {
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
      if (data.hostList.length) {
        data.noHost = false
        data.hostId = data.hostList[0].value
        if (!data.hostObj[data.hostId].isRemember) {
          openDlgToValid(data.hostId)
        } else {
          handleAdd()
        }
      } else {
        data.noHost = true
      }
    }
  }).finally(() => {
    data.hostListLoading = false
  })
}

const handleConnect = (hostData: any, index: number) => {
  data.hostId = hostData.hostId
  data.rootPassword = hostData.password
  data.username = hostData.username
  openSocket(index)
}

const openSocket = (index: number) => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const terminalSocket = new Socket({ url: `custom_terminal_${socketKey}` })
  terminalSocket.onopen(() => {
    const param = {
      hostId: data.hostId,
      rootPassword: data.rootPassword,
      sshUsername: data.username,
      wsConnectType: WsConnectType.SSH,
      businessId: `custom_terminal_${socketKey}`
    }
    data.loading = true
    initTerm(term, terminalSocket.ws, index)
    openSSH(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        data.isSuccess = false
        terminalSocket.destroy()
      } else {
        data.socketMap[data.hostId] = terminalSocket
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

const initTerm = (term: Terminal, ws: WebSocket | undefined, index: number) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    fitAddon.value = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon.value)
    term.open(document.getElementById(`xterm_${index}`) as HTMLElement)
    fitAddon.value.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
  }
}

const getTermObj = (): Terminal => {
  const termConfig: any = {
    // rendererType: 'dom',
    fontSize: 14,
    rows: 40,
    cols: 200,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    // lineHeight: 2,
    // letterSpacing: 5,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  }
  if (window.screen.width >= 2560 && window.screen.width < 3840) {
    termConfig.rows = 60
    termConfig.lineHeight = 1
    termConfig.letterSpacing = 4
  } else if (window.screen.width >= 3840) {
    termConfig.rows = 80
    termConfig.lineHeight = 2
    termConfig.letterSpacing = 8
  }
  return new Terminal(termConfig)
}

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
