<template>
  <div class="exe-install-c">
    <div class="flex-col full-w full-h" v-if="exeResult === exeResultEnum.SUCESS">
      <svg-icon icon-class="ops-install-success" class="icon-size mb"></svg-icon>
      <div class="mb-lg">{{ $t('simple.ExeInstall.5mpmsp16nuo0') }}</div>
      <div class="install-connect-c flex-col mb-xlg">
        <div class="ft-b mb">{{ $t('simple.ExeInstall.5mpmsp16oy40') }}</div>
        <div class="mb">{{ $t('simple.ExeInstall.5mpmsp16pb40') }}: <span class="content">gaussdb</span></div>
        <!-- <div>{{ $t('simple.ExeInstall.5mpmsp16pig0') }}: <span class="content">{{ dataPassword }}</span></div> -->
      </div>
      <div class="flex-row">
        <a-button type="outline" class="mr" @click="goHome">{{
          $t('simple.ExeInstall.5mpmsp16pp80')
        }}</a-button>
        <a-button type="primary" @click="goOps">{{
          $t('simple.ExeInstall.5mpmsp16pxs0')
        }}</a-button>
      </div>
    </div>
    <div class="flex-row full-w teminal-h" v-else>
      <div class="panel-w flex-col-start mr">
        <a-alert class="mb" style="padding: 14px 12px;width: fit-content;" type="error"
          v-if="exeResult === exeResultEnum.FAIL">
          {{ $t('simple.ExeInstall.5mpmsp16q5o0') }}
        </a-alert>
        <a-alert type="warning" class="mb" style="padding: 14px 12px;width: fit-content;"
          v-if="exeResult === exeResultEnum.UN_INSTALL">{{ $t('simple.ExeInstall.5mpmsp16qhc0') }}
          {{ $t('simple.ExeInstall.5mpmsp16qr80') }}</a-alert>
        <div id="xtermLog" class="xterm"></div>
      </div>
      <div class="panel-w flex-col-start" v-if="exeResult === exeResultEnum.FAIL">
        <div class="full-w flex-between mb">
          <a-select style="width: 300px" v-model="hostId">
            <a-option v-for="(item, index) in hosts" :key="index" :value="item.hostId" :label="item.privateIp">
            </a-option>
          </a-select>
          <a-button type="primary" @click="retryInstall">{{ $t('simple.ExeInstall.5mpmsp16qzc0') }}</a-button>
        </div>
        <div id="xterm" class="xterm"></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, inject, onBeforeUnmount, onMounted, ref } from 'vue'
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
const installStore = useOpsStore()
const hostId = ref('')
const hosts = ref<any[]>([])
const hostObj = ref<KeyValue>({})
const dataPassword = ref('')

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
})

const retryInstall = () => {
  if (terminalWs.value) {
    terminalWs.value?.destroy()
  }
  if (termLog.value) {
    termLog.value.dispose()
  }
  // exeResult.value = exeResultEnum.UN_INSTALL
  openLogSocket()
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
  terminalSocket.onopen(() => {
    const param = {
      hostId: hostId.value,
      rootPassword: '',
      wsConnectType: WsConnectType.SSH,
      businessId: `terminal_${socketKey}`
    }
    if (hostObj.value[hostId.value]) {
      encryptPassword(hostObj.value[hostId.value].rootPassword).then((encryptPwd) => {
        param.rootPassword = encryptPwd
        openSSH(param)
        initTerm(term, terminalSocket.ws)
      }).catch(() => {
        console.warn('encrypt error')
      })
    }
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
    const nodeList = param.installContext.minimalistInstallConfig.nodeConfigList
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
  })
  logSocket.onmessage((messageData: any) => {
    term.writeln(messageData)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        loadingFunc.setBackBtnShow(false)
        loadingFunc.setNextBtnShow(false)
        exeResult.value = exeResultEnum.SUCESS
        if (termLog.value) {
          termLog.value?.dispose()
        }
      } else {
        loadingFunc.cancelLoading()
        exeResult.value = exeResultEnum.FAIL
        if (termTerminal.value) {
          termTerminal.value.dispose()
        }
        openSocket()
      }
      logSocket.destroy()
    }
  })
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
}
const goOps = () => {
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsMonitorDailyOps'
  })
}

const beforeConfirm = async (): Promise<boolean> => {
  return true
}

const installParam = computed(() => installStore.getInstallParam)
defineExpose({
  beforeConfirm
})

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
  }

  .panel-w {
    width: 50%;
    height: 100%;
  }

  .teminal-h {
    height: calc(100% - 60px);
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
