<template>
  <div class="exe-install-c">
    <div class="flex-col full-w full-h" v-if="exeResult === exeResultEnum.SUCESS">
      <svg-icon icon-class="ops-install-success" class="icon-size mb"></svg-icon>
      <div class="label-color mb-lg">{{ $t('enterprise.ExeInstall.5mpm9j359hw0') }}</div>
      <div class="install-connect-c flex-col mb-xlg">
        <div class="ft-b mb">{{ $t('enterprise.ExeInstall.5mpm9j35a4o0') }}</div>
        <div class="mb">{{ $t('enterprise.ExeInstall.else1') }}: gaussdb</div>
        <!-- <div>{{ $t('enterprise.ExeInstall.else2') }}: {{ dataPassword }}</div> -->
      </div>
      <div class="flex-row">
        <a-button type="outline" class="mr" @click="goHome">{{
          $t('enterprise.ExeInstall.5mpm9j35aa80')
        }}</a-button>
        <a-button type="primary" @click="goOps">{{
          $t('enterprise.ExeInstall.5mpm9j35aec0')
        }}</a-button>
      </div>
    </div>
    <div class="flex-col-start full-h full-w" v-else>
      <a-steps small type="arrow" style="width: 100%;" class="mb" :current="data.installStepNum"
        :status="data.currentStatus">
        <a-step>环境准备</a-step>
        <a-step>安装包准备</a-step>
        <a-step>执行安装</a-step>
        <a-step>安装后处理</a-step>
      </a-steps>
      <div class="flex-row full-w teminal-h">
        <div class="panel-w flex-col-start mr" :style="exeResult === exeResultEnum.FAIL ? '' : 'width: 100%'">
          <a-alert class="mb" style="padding: 14px 12px;width: fit-content;" type="error"
            v-if="exeResult === exeResultEnum.FAIL">
            {{ $t('enterprise.ExeInstall.5mpm9j35ahw0') }}
          </a-alert>
          <a-alert class="mb" type="warning" style="padding: 14px 12px;width: fit-content;"
            v-if="exeResult === exeResultEnum.UN_INSTALL">{{ $t('enterprise.ExeInstall.5mpm9j35alg0') }}
            {{ $t('enterprise.ExeInstall.5mpm9j35ap00') }}</a-alert>
          <div id="xtermLog" class="xterm"></div>
        </div>
        <div class="panel-w flex-col-start" v-if="exeResult === exeResultEnum.FAIL">
          <div class="full-w flex-between mb">
            <a-select style="width: 300px" v-model="hostId" @change="hostChange">
              <a-option v-for="(item, index) in hosts" :key="index" :value="item.hostId" :label="item.privateIp">
              </a-option>
            </a-select>
            <div>
              <a-button type="primary" @click="retryInstall" class="mr-s">{{ $t('enterprise.ExeInstall.5mpm9j35ats0')
              }}</a-button>
              <a-button type="primary" @click="handleDownloadLog">{{
                $t('components.openLooKeng.5mpiji1qpcc65')
              }}
              </a-button>
            </div>
          </div>
          <div id="xterm" class="xterm"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, inject, nextTick, onBeforeUnmount, onMounted, ref, reactive } from 'vue'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import { AttachAddon } from 'xterm-addon-attach'
import { openSSH, installOpenGauss } from '@/api/ops'
import { WsConnectType } from '@/types/ops/install'
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

onMounted(() => {
  loadingFunc.setNextBtnShow(false)
  hosts.value = []
  if (Object.keys(installStore.getEnterpriseConfig).length) {
    dataPassword.value = installStore.getEnterpriseConfig.databasePassword
    installStore.getEnterpriseConfig.nodeConfigList.forEach(item => {
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
  openLogSocket()
}

const hostChange = () => {
  if (terminalWs.value) {
    terminalWs.value?.destroy()
  }
  if (termTerminal.value) {
    termTerminal.value.dispose()
  }
  openSocket()
}

const getTermObj = (): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows: 40,
    cols: 100,
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
  terminalSocket.onopen(async () => {
    const param = {
      hostId: hostId.value,
      rootPassword: '',
      wsConnectType: WsConnectType.SSH,
      businessId: `terminal_${socketKey}`
    }
    if (hostObj.value[hostId.value]) {
      const encryptPwd = await encryptPassword(hostObj.value[hostId.value].rootPassword)
      param.rootPassword = encryptPwd
    }
    openSSH(param).then(() => {
      initTerm(term, terminalSocket.ws)
    })
  })
}

const openLogSocket = () => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `installLog_${socketKey}` })
  terminalLogWs.value = logSocket
  logSocket.onopen(async () => {
    loadingFunc.toLoading()
    const param = JSON.parse(JSON.stringify(installParam.value))
    const nodeList = param.installContext.enterpriseInstallConfig.nodeConfigList
    // root password encrypt
    for (let i = 0; i < nodeList.length; i++) {
      const nodeItemData = nodeList[i]
      const encryptPwd = await encryptPassword(nodeItemData.rootPassword)
      nodeItemData.rootPassword = encryptPwd
    }
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
    term.writeln(messageData)
    logs.value += messageData + '\r\n'
    syncStepNumber(messageData)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        loadingFunc.setBackBtnShow(false)
        loadingFunc.setNextBtnShow(false)
        exeResult.value = exeResultEnum.SUCESS
      } else {
        data.currentStatus = 'error'
        loadingFunc.cancelLoading()
        // fail
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
    case 'START_GEN_XML_CONFIG':
      data.installStepNum = 1
      break
    case 'START_SCP_INSTALL_PACKAGE':
      data.installStepNum = 2
      break
    case 'START_EXE_PREINSTALL_COMMAND':
      data.installStepNum = 3
      break
    case 'SAVE_INSTALL_CONTEXT':
      data.installStepNum = 4
      break
  }
}

const initTermLog = (term: Terminal, ws: WebSocket | undefined) => {
  if (ws) {
    const fitAddon = new FitAddon()
    term.loadAddon(fitAddon)
    term.open(document.getElementById('xtermLog') as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    termLog.value = term
  }
}

const initTerm = (term: Terminal, ws: WebSocket | undefined) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    const fitAddon = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon)
    term.open(document.getElementById('xterm') as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
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
  const filename = `enterprise_ops_${time}.log`

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
  height: calc(100% - 28px - 42px);
  overflow-y: auto;

  .install-connect-c {
    padding: 20px 60px;
    background-color: #f2f3f5;
    border-radius: 8px;
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
