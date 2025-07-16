<template>
  <div class="simple-install-c" id="simpleInstall">
    <div class="label-color">{{ $t('simpleInstall.index.5mpn813gtvs0') }}</div>
    <a-divider class="mb-lg" />
    <div class="install-content flex-col" v-if="data.state === -1">
      <svg-icon icon-class="ops-mini-version" class="icon-size mb"></svg-icon>
      <div class="label-color ft-b ft-m mb">openGauss {{ $t('simpleInstall.index.else1') }}</div>
      <div class="mb" v-if="data.validVisible">
        <a-link @click="goInstall">{{ $t('simpleInstall.index.else3') }}，<span style="text-decoration:underline">{{
          $t('simpleInstall.index.else4')
        }}</span></a-link>
      </div>
      <a-form class="mb" :model="data.form" :rules="formRules" :style="{ width: '400px' }" ref="formRef" auto-label-width>
        <a-form-item field="hostId" :label="$t('simpleInstall.index.5mpn813guf00')">
          <div class="flex-row mr-s">
            <a-select style="width: 313px;" class="mr-s" :loading="data.hostLoading" v-model="data.form.hostId"
              :placeholder="$t('simpleInstall.index.5mpn813gukw0')" @change="hostChange"
              @popup-visible-change="hostVisibleChange">
              <a-option v-for="item in data.hostList" :key="item.hostId" :value="item.hostId">{{
                item.privateIp
                + '(' +
                (item.publicIp ? item.publicIp : '--') + ')'
              }}</a-option>
            </a-select>

          </div>
          <template #extra>
            <label class="label-color">{{ data.form.sysArch }}</label>
          </template>
        </a-form-item>
        <a-form-item field="hostUser" :label="$t('simpleInstall.index.user')" validate-trigger="blur">
          <div class="flex-row mr-s">
            <a-select style="width: 313px;" v-model="data.form.hostUser"
              :placeholder="$t('simpleInstall.index.userPlaceholder')" @change="hostUserChange">
              <a-option v-for="item in data.hostUserList" :key="item.hostUserId" :value="item.hostUserId">{{ item.username
              }}</a-option>
            </a-select>
            <icon-code-square :size="25" class="label-color" style="cursor: pointer;" @click="showTerminal" />
          </div>
        </a-form-item>
        <a-form-item field="install" :label="$t('simpleInstall.index.else5')" validate-trigger="change">
          <a-select :loading="data.installLoading" v-model="data.form.install"
            :placeholder="$t('simpleInstall.index.else6')" value-key="packageId">
            <a-option v-for="item in data.installPackageList" :key="item.packageId" :value="item"
              :label="item.packageVersionNum"></a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="installPath" :label="$t('simple.InstallConfig.5mpmu0lar480')" validate-trigger="blur">
          <a-input class="label-color" v-model.trim="data.form.installPath"
              :placeholder="$t('simple.InstallConfig.5mpmu0lar800')"></a-input>
        </a-form-item>
        <a-form-item field="port" :label="$t('simple.InstallConfig.5mpmu0larj40')" validate-trigger="blur">
          <a-input-number class="label-color" v-model="data.form.port" max="65535" min="0"
              :placeholder="$t('simple.InstallConfig.5mpmu0larmo0')"></a-input-number>
        </a-form-item>
        <a-form-item field="password" :label="$t('simple.InstallConfig.5mpmu0larx00')" validate-trigger="blur">
          <a-input-password class="label-color" v-model="data.form.password"
                          :placeholder="$t('simple.InstallConfig.5mpmu0las0k0')" allow-clear></a-input-password>
        </a-form-item>
      </a-form>
      <a-button type="primary" size="large" :loading="data.loading" @click="handleInstall">{{
        $t('simpleInstall.index.5mpn813gut00')
      }}</a-button>
    </div>
    <div class="install-panel" v-if="data.state !== -1">
      <div class="flex-col full-h" v-if="data.state !== 1">
        <div class="install-doing mb">
          <div class="label-color ft-m ft-b mb-lg">{{ $t('simpleInstall.index.5mpn813guxc0') }}</div>
          <div class="flex-col-start full-w">
            <div class="progress-c mb">
              <a-progress :color="data.state === 1 ? 'green' : 'red'" :stroke-width="12" :percent="data.installProgress">
              </a-progress>
            </div>
            <div class="label-color">{{ $t('simpleInstall.index.5mpn813gv880') }} {{
              $t('simpleInstall.index.5mpn813gvc40')
            }} {{ data.privateIp }}</div>
          </div>
        </div>
        <div class="flex-col-start full-h full-w">
          <a-steps small type="arrow" style="width: 100%;" class="mb" :current="data.installStepNum"
            :status="data.currentStatus">
            <a-step>{{ $t('simple.ExeInstall.else1') }}</a-step>
            <a-step>{{ $t('simple.ExeInstall.else2') }}</a-step>
            <a-step>{{ $t('simple.ExeInstall.else3') }}</a-step>
            <a-step>{{ $t('simple.ExeInstall.else4') }}</a-step>
          </a-steps>
          <div class="flex-row full-w full-h">
            <div class="flex-col-start panel-w mr" :style="data.state === 2 ? '' : 'width: 100%'">
              <a-alert class="mb-s" style="padding: 10px 12px;width: fit-content;" type="error" v-if="data.state === 2">
                {{ $t('simpleInstall.index.5mpn813gvfw0') }}
              </a-alert>
              <div id="xtermLog" class="xterm"></div>
            </div>
            <div class="flex-col-start panel-w" v-if="data.state === 2">
              <div class="flex-between full-w mb">
                <div class="flex-row">
                  <div class="label-color mr-s">{{ $t('simpleInstall.index.5mpn813gvjk0') }}</div>
                  <div class="label-color">{{ data.privateIp }}</div>
                </div>
                <div>
                  <a-button type="primary" @click="retryInstall" class="mr-s">{{ $t('simpleInstall.index.5mpn813gvmw0')
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
      <div class="flex-col full-w full-h" v-else>
        <svg-icon class="succ-icon-size mb" icon-class="ops-install-success"></svg-icon>
        <div class="mb-lg">{{ $t('simpleInstall.index.5mpn813gvqo0') }}</div>
        <div class="install-connect-c flex-col mb-xlg">
          <div class="ft-b mb">{{ $t('simpleInstall.index.5mpn813gvu40') }}</div>
          <div class="flex-row mb-s">
            <div class="label-w">{{ $t('simple.InstallConfig.5mpmu0larj40') }}</div>
            <div class="label-value">{{ data.form.port }}</div>
          </div>
          <div class="flex-row mb-s">
            <div class="label-w">{{ $t('simpleInstall.index.5mpn813gvxg0') }}</div>
            <div class="label-value">gaussdb</div>
          </div>
          <div class="flex-row mb-s">
            <div class="label-w">{{ $t('simpleInstall.index.5mpn813guf00') }}</div>
            <div class="label-value">{{ data.publicIp }}</div>
          </div>
        </div>
        <div class="flex-row">
          <a-button type="outline" class="mr" @click="goHome">{{
            $t('simpleInstall.index.5mpn813gw4c0')
          }}</a-button>
          <a-button type="primary" @click="goOps">{{
            $t('simpleInstall.index.5mpn813gw7w0')
          }}</a-button>
        </div>
      </div>
    </div>
    <host-terminal ref="hostTerminalRef"></host-terminal>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { onBeforeUnmount, onMounted, reactive, ref, computed, nextTick, inject } from 'vue'
import { hostListAll, hostUserListWithoutRoot, quickInstall, openSSH, packageListAll, portUsed, pathEmpty, hostPing, getSysUploadPath } from '@/api/ops'
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
import { FitAddon } from '@xterm/addon-fit'
import { AttachAddon } from '@xterm/addon-attach'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { encryptPassword } from '@/utils/jsencrypt'
import { useI18n } from 'vue-i18n'
import dayjs from "dayjs";
import HostTerminal from "@/views/ops/install/components/hostTerminal/HostTerminal.vue";
import {LINUX_PATH} from "@/views/ops/install/components/enterprise/constant";

const { t } = useI18n()
const data = reactive<KeyValue>({
  state: -1, // -1 un install  0 installing  1 success  2 fail
  loading: false,
  form: {
    hostId: '',
    hostUser: '',
    rootPassword: '',
    rootPasswordEncrypt: '',
    os: '',
    cpuArch: '',
    sysArch: '',
    install: null,
    port: 5432,
    installPath: '/opt/openGauss',
    password: ''
  },
  installProgress: 0,
  port: 22,
  privateIp: '',
  publicIp: '',
  installUserId: '',
  installUsername: '',
  installUserPassword: '',
  hostLoading: false,
  isNeedPwd: false,
  hostObj: {},
  hostList: [],
  hostUserList: [],
  installLoading: false,
  installObj: {},
  installPackageList: [],
  validVisible: false,

  installStepNum: 1,
  currentStatus: 'process',

  installContext: {
    installPackagePath: '/ops/files'
  }
})

// websocket
const terminalLogWs = ref<Socket<any, any> | undefined>()
const terminalWs = ref<Socket<any, any> | undefined>()

const termLog = ref<Terminal>()
const termTerminal = ref<Terminal>()
const logs = ref<string>('')
const loadingFunc = inject<any>('loading')

const fitAddonTerm = ref<any>(null)
const fitAddonTermLog = ref<any>(null)
const formRules = computed(() => {
  return {
    hostId: [{ required: true, 'validate-trigger': 'change', message: t('simpleInstall.index.5mpn813gwbk0') }],
    hostUser: [{ required: true, 'validate-trigger': 'change', message: '请选择用户' }],
    rootPassword: [{ required: true, 'validate-trigger': 'blur', message: t('simpleInstall.index.5mpn813gupc0') }],
    installPath: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0lar800') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else if (!LINUX_PATH.test(value)) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jvx0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    port: [{ required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0larmo0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            // 校验数据库端口
            if (value < 1024 || value > 65529) {
              cb(t('enterprise.ClusterConfig.5mpm3ku3jux0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    install: [{
      validator: (value: any, cb: any) => {
        return new Promise(resolve => {
          if (!Object.keys(value).length) {
            cb(t('simpleInstall.index.else6'))
            resolve(false)
          } else {
            resolve(true)
          }
        })
      }
    }]
  }
})

onMounted(() => {
  getHostList()
  getSystemUploadPath()
})

onBeforeUnmount(() => {
  terminalLogWs.value?.destroy()
  terminalWs.value?.destroy()
  termLog.value?.dispose()
  termTerminal.value?.dispose()
  window.removeEventListener('resize', resizeScreenTermLog)
  window.removeEventListener('resize', resizeScreenTerm)
})

const formRef = ref<FormInstance>()
const handleInstall = async () => {
  data.validVisible = false
  const validRes = await formRef.value?.validate()
  if (!validRes) {
    data.loading = true
    const encryptPwd = await encryptPassword(data.form.rootPassword)
    data.form.rootPasswordEncrypt = encryptPwd
    // password is isok
    try {
      const { privateIp, publicIp, installUsername, installUserPassword, port } = data
      const hostParam = { privateIp, publicIp, username: installUsername, password: installUserPassword, port }
      const passwordValid: KeyValue = await hostPing(hostParam)
      if (Number(passwordValid.code) !== 200) {
        formRef.value?.setFields({
          rootPassword: {
            status: 'error',
            message: t('enterprise.NodeConfig.else8')
          }
        })
        data.loading = false
        return
      }
    } catch (err: any) {
      formRef.value?.setFields({
        rootPassword: {
          status: 'error',
          message: t('enterprise.NodeConfig.else8')
        }
      })
      data.loading = false
      return
    }

    try {
      //  port is used
      const portParam = {
        port: data.form.port,
      }
      const portValid: KeyValue = await portUsed(data.form.hostId, portParam)
      if (Number(portValid.code) === 200) {
        if (portValid.data) {
          data.loading = false
          Message.error('Port check exception')
          return
        }
      } else {
        Message.error('Port check exception')
        return
      }
      // installPath is not empty
      const pathParam = {
        path: data.form.installPath + '/data',
      }
      const pathValid: KeyValue = await pathEmpty(data.form.hostId, pathParam)
        .catch((error) => {
          loadingFunc.cancelLoading();
        })
      if (Number(pathValid.code) === 200) {
        if (!pathValid.data) {
          data.validVisible = true
          Message.error('installPath check exception')
          return
        }
      } else {
        Message.error('installPath check exception')
        return
      }
      data.state = 0
      openLogSocket()
    } catch (error: any) {
      console.error('port or installPath check exception', error)
    } finally {
      data.loading = false
    }
  }
}

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

const openLogSocket = () => {
  const term = getTermObj()
  let isProgress = false
  let temp = false
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `simple_installLog_${socketKey}` })
  terminalLogWs.value = logSocket
  logSocket.onopen(() => {
    localStorage.setItem('Static-pluginBase-opsOpsSimpleInstall', '1')
    initTermLog(term, logSocket.ws)
    exeInstall(logSocket, `simple_installLog_${socketKey}`, term)
    logs.value = ''
  })
  logSocket.onclose(() => {
    localStorage.removeItem('Static-pluginBase-opsOpsSimpleInstall')
  })
  logSocket.onmessage((messageData: any) => {
    logs.value += messageData
    syncStepNumber(messageData)
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
          isProgress = false
        } else {
          term.write((progress * 100).toFixed(0) + '%')
        }
      }
    } else {
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
    }
    numDynamic(messageData)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      data.installProgress = 1
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        term.writeln('install success')
        data.state = 1
      } else {
        data.currentStatus = 'error'
        term.writeln('install fail')
        data.state = 2
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

const openSocket = () => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const terminalSocket = new Socket({ url: `simple_terminal_${socketKey}` })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      hostId: data.form.hostId,
      sshUsername: data.installUsername,
      wsConnectType: WsConnectType.SSH,
      businessId: `simple_terminal_${socketKey}`
    }
    openSSH(param)
    initTerm(term, terminalSocket.ws)
  })
}

const exeInstall = async (socket: Socket<any, any>, businessId: string, term: Terminal) => {
  const param = {
    quickInstallResourceUrl: data.form.install.packageUrl,
    installContext: {
      openGaussVersion: OpenGaussVersionEnum.MINIMAL_LIST,
      openGaussVersionNum: data.form.install.packageVersionNum,
      installMode: InstallModeEnum.OFF_LINE,
      installPackagePath: data.installContext.installPackagePath,
      deployType: DeployTypeEnum.SINGLE_NODE,
      clusterId: 'MINI_' + new Date().getTime(),
      clusterName: '',
      minimalistInstallConfig: {
        port: data.form.port,
        installPackagePath: '/opt/software/openGauss',
        databaseUsername: '',
        databasePassword: data.form.password,
        nodeConfigList: [{
          clusterRole: ClusterRoleEnum.MASTER,
          hostId: data.form.hostId,
          installUserId: data.installUserId,
          installPath: data.form.installPath,
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
    fitAddonTermLog.value = fitAddon
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
      data.publicIp = data.hostList[0].publicIp
      data.port = data.hostList[0].port
      data.form.os = data.hostList[0].os
      data.form.cpuArch = data.hostList[0].cpuArch
      data.form.sysArch = data.hostList[0].os + '_' + data.hostList[0].cpuArch
      getInstallPackageList()
      getHostUser()
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    data.hostLoading = false
  })
}

const getSystemUploadPath = () => {
  getSysUploadPath().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.installContext.installPackagePath = res.data
    } else {
      Message.error('Failed to obtain the system upload path')
    }
  })
}

const getInstallPackageList = () => {
  if (data.form.hostId) {
    if (data.hostObj[data.form.hostId]) {
      data.form.os = data.hostObj[data.form.hostId].os
      data.form.osVersion = data.hostObj[data.form.hostId].osVersion
      data.form.cpuArch = data.hostObj[data.form.hostId].cpuArch
      data.form.sysArch = data.hostObj[data.form.hostId].os + '_' + data.hostObj[data.form.hostId].cpuArch
      data.isNeedPwd = !data.hostObj[data.form.hostId].isRemember
    }
    data.installLoading = true
    const param = {
      os: data.form.os,
      osVersion: data.form.osVersion,
      cpuArch: data.form.cpuArch,
      packageVersion: 'MINIMAL_LIST'
    }
    packageListAll(param).then((res: any) => {
      if (Number(res.code) === 200) {
        data.installPackageList = []
        res.data.forEach((item: KeyValue) => {
          if (item.packageUrl) {
            data.installObj[item.packageId] = item
            data.installPackageList.push(item)
          }
        })
        if (res.data.length) {
          data.form.install = data.installPackageList[0]
        } else {
          data.form.install = null
        }
      }
    }).finally(() => {
      data.installLoading = false
    })
  }
}

const hostVisibleChange = (val: boolean) => {
  if (val) {
    getHostList()
  }
}

const hostChange = () => {
  getInstallPackageList()
  getHostUser()
  if (data.form.hostId) {
    data.port = data.hostObj[data.form.hostId].port
  }
}

const getHostUser = () => {
  data.form.hostUser = ''
  data.installUserId = ''
  data.installUsername = ''
  data.installUsername = ''
  data.hostUserList = []
  if (data.form.hostId) {
    if (data.hostObj[data.form.hostId]) {
      data.privateIp = data.hostObj[data.form.hostId].privateIp
      data.publicIp = data.hostObj[data.form.hostId].publicIp
    }
    hostUserListWithoutRoot(data.form.hostId).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        data.hostUserList = res.data
        if (data.hostUserList.length) {
          data.form.hostUser = data.hostUserList[0].hostUserId
          data.installUsername = data.hostUserList[0].username
          data.installUserId = data.hostUserList[0].hostUserId
          data.installUserPassword = data.hostUserList[0].password
        }
      } else {
        Message.error('Failed to obtain user data from the host')
      }
    })
  }
}

const hostUserChange = (userId) => {
  const userInfo = data.hostUserList.find(item => item.hostUserId === userId)
  data.installUsername = userInfo?.username
  data.installUserId = userId
  data.installUserPassword = userInfo?.password
}

const goInstall = () => {
  data.validVisible = false
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsOpsInstall'
  })
}

const goHome = () => {
  initData()
  window.$wujie?.props.methods.jump({
    name: 'Dashboard'
  })
}

const goOps = () => {
  initData()
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsMonitorDailyOps'
  })
}

const initData = () => {
  data.state = -1
  getHostList()
}

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

const hostTerminalRef = ref<null | InstanceType<typeof HostTerminal>>(null)
const showTerminal = () => {
  if (!data.installUsername) {
    Message.error(t('simpleInstall.index.userPlaceholder'))
    return
  }

  if (!data.form.hostId) {
    formRef.value?.setFields({
      hostId: {
        status: 'error',
        message: t('simple.InstallConfig.5mpmu0laqss0')
      }
    })
    return
  }
  // showTerminal
  handleShowTerminal({
    hostId: data.form.hostId,
    port: data.form.port,
    ip: data.publicIp,
    sshUsername: data.installUsername,
  })
}

const handleShowTerminal = (data: KeyValue) => {
  hostTerminalRef.value?.open(data)
}

</script>

<style lang="less" scoped>
.simple-install-c {
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;

  .icon-size {
    width: 80px;
    height: 80px;
  }

  .install-content {}

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

.xterm {
  width: 100%;
  height: 470px;
}

.panel-w {
  width: 50%;
  height: 100%;
}

.succ-icon-size {
  width: 100px;
  height: 100px;
}
</style>
