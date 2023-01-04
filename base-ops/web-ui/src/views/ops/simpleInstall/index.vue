<template>
  <div class="simple-install-c">
    <div>{{ $t('simpleInstall.index.5mpn813gtvs0') }}</div>
    <a-divider class="mb-lg" />
    <div class="install-content" v-if="data.state === -1">
      <svg-icon icon-class="ops-mini-version" class="icon-size mb"></svg-icon>
      <div class="ft-b ft-m mb">openGauss {{ $t('simpleInstall.index.else1') }}</div>
      <a-form class="mb" :model="data.form" :rules="data.rules" :style="{ width: '400px' }" ref="formRef"
        auto-label-width>
        <a-form-item field="hostId" :label="$t('simpleInstall.index.5mpn813guf00')">
          <a-select :loading="data.hostLoading" v-model="data.form.hostId"
            :placeholder="$t('simpleInstall.index.5mpn813gukw0')" @change="getHostUser">
            <a-option v-for="item in data.hostList" :key="item.hostId" :value="item.hostId">{{ item.privateIp
    + '(' +
    (item.publicIp ? item.publicIp : '--') + ')'
}}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="rootPassword" :label="$t('simpleInstall.index.else2')" validate-trigger="blur"
          :rules="[{ required: true, message: t('simpleInstall.index.5mpn813gupc0') }]">
          <a-input-password v-model="data.form.rootPassword" :placeholder="$t('simpleInstall.index.5mpn813gupc0')"
            allow-clear />
        </a-form-item>
      </a-form>
      <a-button type="primary" size="large" @click="handleInstall">{{ $t('simpleInstall.index.5mpn813gut00')
}}</a-button>
    </div>
    <div class="install-panel" v-if="data.state !== -1">
      <div class="flex-col full-h" v-if="data.state !== 1">
        <div class="install-doing mb">
          <div class="ft-m ft-b mb-lg">{{ $t('simpleInstall.index.5mpn813guxc0') }}</div>
          <div class="flex-col-start full-w">
            <div class="progress-c mb">
              <a-progress :color="data.state === 1 ? 'green' : 'red'" :stroke-width="12"
                :percent="data.installProgress">
              </a-progress>
            </div>
            <div>{{ $t('simpleInstall.index.5mpn813gv880') }} {{ $t('simpleInstall.index.5mpn813gvc40') }} {{
    data.privateIp
}}</div>
          </div>
        </div>
        <div class="flex-row full-w full-h">
          <div class="flex-col-start mr" style="width: 50%;">
            <a-alert class="mb-s" style="padding: 10px 12px;width: fit-content;" type="error" v-if="data.state === 2">
              {{ $t('simpleInstall.index.5mpn813gvfw0') }}
            </a-alert>
            <div id="xtermLog" class="xterm"></div>
          </div>
          <div class="flex-col-start" style="width: 50%;" v-if="data.state === 2">
            <div class="flex-between full-w mb">
              <div class="flex-row">
                <div class="mr-s">{{ $t('simpleInstall.index.5mpn813gvjk0') }}</div>
                <div>{{ data.privateIp }}</div>
              </div>
              <a-button type="primary" @click="retryInstall">{{ $t('simpleInstall.index.5mpn813gvmw0') }}</a-button>
            </div>
            <div id="xterm" class="xterm"></div>
          </div>
        </div>
      </div>
      <div class="flex-col full-w full-h" v-else>
        <svg-icon class="succ-icon-size mb" icon-class="ops-install-success"></svg-icon>
        <div class="mb-lg">{{ $t('simpleInstall.index.5mpn813gvqo0') }}</div>
        <div class="install-connect-c flex-col mb-xlg">
          <div class="ft-b mb">{{ $t('simpleInstall.index.5mpn813gvu40') }}</div>
          <div class="mb">{{ $t('simpleInstall.index.5mpn813gvxg0') }} <span class="content">gaussdb</span></div>
          <div>{{ $t('simpleInstall.index.5mpn813gw100') }} <span class="content">1qaz2wsx#EDC</span></div>
        </div>
        <div class="flex-row">
          <a-button type="outline" class="mr" @click="goHome">{{
    $t('simpleInstall.index.5mpn813gw4c0')
}}</a-button>
          <a-button type="primary" @click="goOps">{{ $t('simpleInstall.index.5mpn813gw7w0')
            }}</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { hostListAll, hostUserListWithoutRoot, quickInstall, openSSH } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import {
  ClusterRoleEnum,
  DeployTypeEnum,
  InstallModeEnum,
  OpenGaussVersionEnum, WsConnectType,
  WsConnectTypeEnum
} from '@/types/ops/install'
import Socket from '@/utils/websocket'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import { AttachAddon } from 'xterm-addon-attach'
import { FitAddon } from 'xterm-addon-fit'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { encryptPassword } from '@/utils/jsencrypt'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data = reactive<KeyValue>({
  state: -1, // -1 un install  0 installing  1 success  2 fail
  form: {
    hostId: '',
    rootPassword: '',
    rootPasswordEncrypt: ''
  },
  installProgress: 0,
  privateIp: '',
  installUserId: '',
  installUsername: '',
  hostLoading: false,
  hostObj: {},
  hostList: [],
  rules: {
    hostId: [{ required: true, 'validate-trigger': 'change', message: t('simpleInstall.index.5mpn813gwbk0') }],
    rootPassword: [{ required: true, 'validate-trigger': 'blur', message: t('simpleInstall.index.5mpn813gupc0') }]
  }
})

// websocket
const terminalLogWs = ref<Socket<any, any> | undefined>()
const terminalWs = ref<Socket<any, any> | undefined>()

const termLog = ref<Terminal>()
const termTerminal = ref<Terminal>()

onMounted(() => {
  getHostList()
})

onBeforeUnmount(() => {
  terminalLogWs.value?.destroy()
  terminalWs.value?.destroy()
  termLog.value?.dispose()
  termTerminal.value?.dispose()
})

const formRef = ref<FormInstance>()
const handleInstall = async () => {
  const validRes = await formRef.value?.validate()
  if (!validRes) {
    const encryptPwd = await encryptPassword(data.form.rootPassword)
    data.form.rootPasswordEncrypt = encryptPwd
    data.state = 0
    openLogSocket()
  }
}

const retryInstall = () => {
  if (terminalWs.value) {
    terminalWs.value?.destroy()
  }
  if (termLog.value) {
    termLog.value.dispose()
  }
  openLogSocket()
}

const openLogSocket = () => {
  const term = getTermObj()
  let isProgress = false
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `simple_installLog_${socketKey}` })
  terminalLogWs.value = logSocket
  logSocket.onopen(() => {
    initTermLog(term, logSocket.ws)
    exeInstall(logSocket, `simple_installLog_${socketKey}`, term)
  })
  logSocket.onmessage((messageData: any) => {
    if (messageData === 'START') {
      isProgress = true
    }
    if (messageData === 'DOWNLOAD_FINISH') {
      isProgress = false
    }
    if (isProgress) {
      const progress = Number(messageData)
      if (isNaN(progress)) {
        term.writeln(messageData)
      } else {
        term.write('\x1b[2K\r')
        if (progress === 1) {
          term.writeln((progress * 100).toFixed(0) + '%')
        } else {
          term.write((progress * 100).toFixed(0) + '%')
        }
      }
    } else {
      term.writeln(messageData)
    }
    numDynamic(messageData)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      data.installProgress = 1
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        term.writeln('install success')
        data.state = 1
      } else {
        term.writeln('install fail')
        data.state = 2
        if (termTerminal.value) {
          termTerminal.value.dispose()
        }
        openSocket()
      }
      logSocket.destroy()
    }
  })
}

const openSocket = () => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const terminalSocket = new Socket({ url: `simple_terminal_${socketKey}` })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      hostId: data.form.hostId,
      rootPassword: data.form.rootPasswordEncrypt,
      wsConnectType: WsConnectType.SSH,
      businessId: `simple_terminal_${socketKey}`
    }
    openSSH(param)
    initTerm(term, terminalSocket.ws)
  })
}

const exeInstall = async (socket: Socket<any, any>, businessId: string, term: Terminal) => {
  const param = {
    installContext: {
      openGaussVersion: OpenGaussVersionEnum.MINIMAL_LIST,
      openGaussVersionNum: '3.0.0',
      installMode: InstallModeEnum.OFF_LINE,
      installPackagePath: '/ops/files',
      // installPackagePath: 'E:/hw/installPackage/download/',
      deployType: DeployTypeEnum.SINGLE_NODE,
      clusterId: 'MINI_' + new Date().getTime(),
      clusterName: '',
      minimalistInstallConfig: {
        port: Number(5432),
        databaseUsername: '',
        databasePassword: '1qaz2wsx#EDC',
        nodeConfigList: [{
          clusterRole: ClusterRoleEnum.MASTER,
          hostId: data.form.hostId,
          rootPassword: data.form.rootPasswordEncrypt,
          installUserId: data.installUserId,
          installPath: '/opt/openGauss',
          dataPath: '/opt/openGauss/data',
          isInstallDemoDatabase: true
        }]
      }
    },
    wsConnectType: WsConnectTypeEnum.COMMAND_EXEC,
    businessId: businessId
  }
  quickInstall(param).then((res: KeyValue) => {
    if (Number(res.code) !== 200) {
      socket.destroy()
    }
  }).catch((error) => {
    term.writeln(error.toString())
    socket.destroy()
  })

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

const numDynamic = (eventData: string) => {
  if (eventData === 'START') {
    data.installProgress = 0.05
  } else if (eventData === 'BEFORE INSTALL') {
    data.installProgress = 0.15
  } else if (eventData === 'INSTALL') {
    data.installProgress = 0.3
  } else if (eventData === 'SAVE CONTEXT') {
    data.installProgress = 0.6
  } else if (eventData === 'CREATE REMOTE USER') {
    data.installProgress = 0.8
  }
}

const getHostList = () => {
  data.hostLoading = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.hostList = []
      data.hostList = res.data
      res.data.forEach((item: KeyValue) => {
        data.hostObj[item.hostId] = item
      })
      data.form.hostId = data.hostList[0].hostId
      data.privateIp = data.hostList[0].privateIp
      getHostUser()
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    data.hostLoading = false
  })
}

const getHostUser = () => {
  if (data.form.hostId) {
    if (data.hostObj[data.form.hostId]) {
      data.privateIp = data.hostObj[data.form.hostId].privateIp
    }
    hostUserListWithoutRoot(data.form.hostId).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        if (res.data.length) {
          data.installUserId = res.data[0].hostUserId
          data.installUsername = res.data[0].username
        } else {
          data.installUserId = ''
          data.installUsername = ''
        }
      } else {
        Message.error('Failed to obtain user data from the host')
      }
    })
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

</script>

<style lang="less" scoped>
.simple-install-c {
  padding: 20px;
  background-color: #FFF;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 136px - 40px);
  box-sizing: border-box;

  .icon-size {
    width: 80px;
    height: 80px;
  }

  .install-content {
    margin-top: 100px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  .install-doing {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    .progress-c {
      width: 100%;
      display: flex;
      border-radius: 3px;
      padding: 5px;
      border: 1px solid rgb(var(--primary-6));
    }
  }

  .install-panel {
    height: calc(100% - 78px);
    box-sizing: border-box;
  }
}

.install-connect-c {
  padding: 20px 100px;
  background-color: #f2f3f5;
  border-radius: 8px;

  .content {
    color: rgb(var(--arcoblue-6));
  }
}

.xterm {
  width: 100%;
  height: 470px;
}

.succ-icon-size {
  width: 100px;
  height: 100px;
}
</style>
