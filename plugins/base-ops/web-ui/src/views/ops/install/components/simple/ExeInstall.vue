<template>
  <div class="exe-install-c">
    <div
      class="flex-col full-w full-h"
      v-if="exeResult === exeResultEnum.SUCESS"
    >
      <svg-icon
        icon-class="ops-install-success"
        class="icon-size mb"
      ></svg-icon>
      <div class="label-color mb-lg">{{ $t('simple.ExeInstall.5mpmsp16nuo0') }}</div>
      <div class="install-connect-c flex-col mb-xlg">
        <div class="ft-b mb">{{ $t('simple.ExeInstall.5mpmsp16oy40') }}</div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('simple.InstallConfig.5mpmu0laqc80') }}</div>
          <div class="label-value">{{ installStore.getInstallConfig.clusterId }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('simple.InstallConfig.5mpmu0larj40') }}</div>
          <div class="label-value">{{ installStore.getMiniConfig.port }}</div>
        </div>
        <div class="flex-row mb-s">
          <div class="label-w">{{ $t('simple.ExeInstall.5mpmsp16pb40') }}</div>
          <div class="label-value">gaussdb</div>
        </div>
        <div
          class="flex-row mb-s"
          v-for="(nodeData, index) in installStore.getMiniConfig.nodeConfigList"
          :key="index"
        >
          <div class="label-w">{{ nodeData.clusterRole === ClusterRoleEnum.MASTER ? $t('enterprise.ClusterConfig.else3') :
            ($t('enterprise.ClusterConfig.else4') + index) }}</div>
          <div class="label-value">{{ nodeData.publicIp }}</div>
        </div>
      </div>
      <div class="flex-row">
        <a-button
          type="outline"
          class="mr"
          @click="goHome"
        >{{
            $t('simple.ExeInstall.5mpmsp16pp80')
          }}
        </a-button>
        <a-button
          type="primary"
          @click="goOps"
        >{{
            $t('simple.ExeInstall.5mpmsp16pxs0')
          }}
        </a-button>
      </div>
    </div>
    <div
      class="flex-col-start full-h full-w"
      v-else
    >
      <a-steps
        small
        type="arrow"
        style="width: 100%;"
        class="mb"
        :current="data.installStepNum"
        :status="data.currentStatus"
      >
        <a-step>{{ $t('simple.ExeInstall.else1') }}</a-step>
        <a-step>{{ $t('simple.ExeInstall.else2') }}</a-step>
        <a-step>{{ $t('simple.ExeInstall.else3') }}</a-step>
        <a-step>{{ $t('simple.ExeInstall.else4') }}</a-step>
      </a-steps>
      <div class="flex-row full-w teminal-h">
        <div
          :class="`flex-col-start mr panel-w`"
          :style="exeResult === exeResultEnum.FAIL ? '' : 'width: 100%'"
        >
          <a-alert
            class="mb"
            style="padding: 15px 12px;width: fit-content;"
            type="error"
            v-if="exeResult === exeResultEnum.FAIL"
          >
            {{ $t('simple.ExeInstall.5mpmsp16q5o0') }}
          </a-alert>
          <a-alert
            type="warning"
            class="mb"
            style="padding: 15px 12px;width: fit-content;"
            v-if="exeResult === exeResultEnum.UN_INSTALL"
          >{{ $t('simple.ExeInstall.5mpmsp16qhc0') }}
            {{ $t('simple.ExeInstall.5mpmsp16qr80') }}
          </a-alert>
          <div
            id="xtermLog"
            class="xterm"
          ></div>
        </div>
        <div
          :class="`flex-col-start panel-w`"
          v-if="exeResult === exeResultEnum.FAIL"
        >
          <div class="full-w flex-between mb">
            <a-select
              style="width: 300px"
              v-model="hostId"
            >
              <a-option
                v-for="(item, index) in hosts"
                :key="index"
                :value="item.hostId"
                :label="item.privateIp"
              >
              </a-option>
            </a-select>
            <div>
              <a-button
                type="primary"
                @click="retryInstall"
                class="mr-s"
              >{{ $t('simple.ExeInstall.5mpmsp16qzc0')
                }}</a-button>
              <a-button
                type="primary"
                @click="handleDownloadLog"
              >{{
                  $t('components.openLooKeng.5mpiji1qpcc65')
                }}
              </a-button>
            </div>
          </div>
          <div
            id="xterm"
            class="xterm"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, computed, inject, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import { FitAddon } from '@xterm/addon-fit'
import { AttachAddon } from '@xterm/addon-attach'
import { openSSH, installOpenGauss } from '@/api/ops'
import { ClusterRoleEnum, WsConnectType } from '@/types/ops/install'
import { useOpsStore } from '@/store'
import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { encryptPassword } from '@/utils/jsencrypt'
import dayjs from "dayjs";

const installStore = useOpsStore()
const hostId = ref('')
const hosts = ref<any[]>([])
const hostObj = ref<KeyValue>({})
const dataPassword = ref('')

const data = reactive<KeyValue>({
  installStepNum: 1,
  currentStatus: 'process'
})

enum exeResultEnum {
  UN_INSTALL = Number(-1),
  SUCESS = Number(1),
  FAIL = Number(0)
}

const exeResult = ref<number>(exeResultEnum.UN_INSTALL)

// websocket
const terminalLogWs = ref<Socket<any, any> | undefined>()
const terminalWs = ref<Socket<any, any> | undefined>()

const termLog = ref<Terminal>()
const termTerminal = ref<Terminal>()

const loadingFunc = inject<any>('loading')
const logs = ref<string>('')
const fitAddonTerm = ref<any>(null)
const fitAddonTermLog = ref<any>(null)
onMounted(() => {
  loadingFunc.setNextBtnShow(false)
  hosts.value = []
  if (Object.keys(installStore.getMiniConfig).length) {
    dataPassword.value = installStore.getMiniConfig.databasePassword
    installStore.getMiniConfig.nodeConfigList.forEach(item => {
      hostObj.value[item.hostId] = item
      hosts.value.push({
        hostId: item.hostId,
        privateIp: item.privateIp + '(' + item.publicIp + ')'
      })
    })
    hostId.value = hosts.value[0].hostId
  }
  openLogSocket()
})

onBeforeUnmount(() => {
  terminalLogWs.value?.destroy()
  terminalWs.value?.destroy()
  termLog.value?.dispose()
  termTerminal.value?.dispose()
  window.removeEventListener('resize', resizeScreenTermLog)
  window.removeEventListener('resize', resizeScreenTerm)
})

const retryInstall = () => {
  if (terminalWs.value) {
    terminalWs.value?.destroy()
  }
  if (termLog.value) {
    termLog.value.dispose()
  }
  data.installStepNum = 1
  data.currentStatus = 'process'
  exeResult.value = exeResultEnum.UN_INSTALL
  openLogSocket()
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

const openSocket = () => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const terminalSocket = new Socket({ url: `terminal_${socketKey}` })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      hostId: hostId.value,
      wsConnectType: WsConnectType.SSH,
      businessId: `terminal_${socketKey}`
    }
    if (hostObj.value[hostId.value]) {
      openSSH(param)
      initTerm(term, terminalSocket.ws)
    }
  })
}

const openLogSocket = () => {
  let temp = false
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `installLog_${socketKey}` })
  terminalLogWs.value = logSocket
  logSocket.onopen(async () => {
    loadingFunc.toLoading()
    const param = JSON.parse(JSON.stringify(installParam.value))
    param.businessId = `installLog_${socketKey}`
    installOpenGauss(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        term.writeln(res.msg)
        logSocket.destroy()
      }
    }).catch((error) => {
      term.writeln(error.toString())
      loadingFunc.cancelLoading()
      logSocket.destroy()
    })
    initTermLog(term, logSocket.ws)
    localStorage.setItem('Static-pluginBase-opsOpsInstall', '1')
    logs.value = ''
  })
  logSocket.onclose(() => {
    localStorage.removeItem('Static-pluginBase-opsOpsInstall')
  })
  logSocket.onmessage((messageData: any) => {
    console.log('show term message', messageData)

    if (temp) {
      term.write('\x1b[2K\r')
      if (messageData === '100%') {
        term.writeln(messageData)
      } else {
        term.write(messageData)
      }
    } else {
      term.writeln(messageData)
    }
    if (messageData === 'START_SCP_INSTALL_PACKAGE') {
      temp = true
    }
    if (messageData === 'END_SCP_INSTALL_PACKAGE') {
      temp = false
    }
    logs.value += messageData + '\r\n'
    syncStepNumber(messageData)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        data.installStepNum = 8
        loadingFunc.setBackBtnShow(false)
        loadingFunc.setNextBtnShow(false)
        exeResult.value = exeResultEnum.SUCESS
        if (termLog.value) {
          termLog.value?.dispose()
        }
      } else {
        data.currentStatus = 'error'
        loadingFunc.cancelLoading()
        exeResult.value = exeResultEnum.FAIL

        if (termTerminal.value) {
          termTerminal.value.dispose()
        }
        nextTick(() => {
          let fitAddon = new FitAddon()
          term.loadAddon(fitAddon)
          fitAddon.fit()
        })
        openSocket()
      }
      logSocket.destroy()
    }
  })
}

const syncStepNumber = (messageData: string) => {
  switch (messageData) {
    case 'CREATE_INSTALL_USER':
      data.installStepNum = 1
      break
    case 'START_SCP_INSTALL_PACKAGE':
      data.installStepNum = 2
      break
    case 'START_EXE_INSTALL_COMMAND':
      data.installStepNum = 3
      break
    case 'SAVE_INSTALL_CONTEXT':
      data.installStepNum = 4
      break
  }
}

const resizeScreenTermLog = () => {
  try { // When the window size changes, trigger the xterm resize method to make it adaptive.
    fitAddonTermLog.value?.fit()
  } catch (e) {
    console.log("e", e)
  }
}

const initTermLog = (term: Terminal, ws: WebSocket | undefined) => {
  if (ws) {
    const fitAddon = new FitAddon()
    fitAddonTermLog.value = fitAddon;
    term.loadAddon(fitAddon)
    term.open(document.getElementById('xtermLog') as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    termLog.value = term
    window.addEventListener("resize", resizeScreenTermLog)
  }
}

const resizeScreenTerm = () => {
  try { // When the window size changes, trigger the xterm resize method to make it adaptive.
    fitAddonTerm.value?.fit()
  } catch (e) {
    console.log("e", e)
  }
}

const initTerm = (term: Terminal, ws: WebSocket | undefined) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    const fitAddon = new FitAddon()
    fitAddonTerm.value = fitAddon
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon)
    term.open(document.getElementById('xterm') as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
    window.addEventListener("resize", resizeScreenTerm)
  }
}

const goHome = () => {
  window.$wujie?.props.methods.jump({
    name: 'Dashboard'
  })
  loadingFunc.initData()
}
const goOps = () => {
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsMonitorDailyOps'
  })
  loadingFunc.initData()
}

const beforeConfirm = async (): Promise<boolean> => {
  return true
}

const installParam = computed(() => installStore.getInstallParam)
defineExpose({
  beforeConfirm
})

const handleDownloadLog = () => {
  const time = dayjs().format('YYYY-MM-DD_HH:mm:ss')
  const filename = `ops_${time}.log`

  const blob = new Blob([logs.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

</script>

<style lang="less" scoped>
.exe-install-c {
  width: 100%;
  height: calc(100% - 28px - 50px);
  overflow-y: auto;

  .install-connect-c {
    padding: 20px 100px;
    background-color: #f2f3f5;
    border-radius: 8px;

    .content {
      color: rgb(var(--arcoblue-6));
    }

    .label-w {
      width: 80px;
      text-align: left;
      margin-right: 15px;
    }

    .label-value {
      width: 100px;
      text-align: left;
    }
  }

  .panel-w {
    width: 50%;
    height: 100%;
  }

  .teminal-h {
    height: calc(100% - 60px - 60px);
  }

  .xterm {
    width: 100%;
    height: 100%;
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }
}
</style>
