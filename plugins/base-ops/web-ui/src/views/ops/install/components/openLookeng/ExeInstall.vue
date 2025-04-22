<template>
  <div class="exe-install-c">
    <div class="flex-col">
      <a-steps :current="2" class="mb full-w" type="arrow">
        <a-step :description="$t('components.openLooKeng.5mpiji1qpcc60')" :status="data.uploadStatus">
          {{ $t('components.openLooKeng.5mpiji1qpcc60') }}
        </a-step>
        <a-step :description="$t('components.openLooKeng.5mpiji1qpcc61')" :status="data.zkStatus">
          {{ $t('components.openLooKeng.5mpiji1qpcc61') }}
        </a-step>
        <a-step :description="$t('components.openLooKeng.5mpiji1qpcc62')" :status="data.ssStatus">
          {{ $t('components.openLooKeng.5mpiji1qpcc62') }}
        </a-step>
        <a-step :description="$t('components.openLooKeng.5mpiji1qpcc63')" :status="data.olkStatus">
          {{ $t('components.openLooKeng.5mpiji1qpcc63') }}
        </a-step>
        <a-step :description="$t('components.openLooKeng.5mpiji1qpcc64')" :status="data.serviceStatus">
          {{ $t('components.openLooKeng.5mpiji1qpcc64') }}
        </a-step>
      </a-steps>
      <div class="install-doing mb">
        <div class="progress-top full-w">
          <div class="progress-c mr-s">
            <a-progress color="red" :stroke-width="12" :percent="data.installProgress">
            </a-progress>
          </div>
          <a-spin v-if="status === statusEnum.RUNNING" class="mr"/>
          <a-space class="flex-row">
            <a-button type="primary" v-if="status === statusEnum.FAIL" @click="retryInstall">
              {{ $t('components.openLooKeng.5mpiji1qpcc67') }}
            </a-button>
            <a-button type="primary" @click="downloadLog">{{ $t('components.openLooKeng.5mpiji1qpcc65') }}</a-button>
            <a-button type="primary" @click="showCustomShell">{{ $t('components.openLooKeng.5mpiji1qpcc66') }}</a-button>
          </a-space>
        </div>
      </div>
      <div v-if="status !== statusEnum.SUCCESS" id="xterm" class="xterm"></div>
      <div class="flex-col full-w full-h" v-else>
        <svg-icon class="succ-icon-size mb" icon-class="ops-install-success"></svg-icon>
        <div class="mb-lg ft-b label-color">{{ $t('simpleInstall.index.5mpn813gvqo0') }}</div>
        <div class="install-connect-c flex-col mb-xlg label-color">
          <div class="mb">{{ $t('components.openLooKeng.5mpiji1qpcc68') }}</div>
        </div>
        <div class="flex-row">
          <a-button type="outline" class="mr" @click="goHome">{{
              $t('simpleInstall.index.5mpn813gw4c0')
            }}</a-button>
          <a-button type="primary" @click="goManage">{{
              $t('components.openLooKeng.5mpiji1qpcc69')
            }}</a-button>
        </div>
      </div>
    </div>
    <custom-shell ref="shell"></custom-shell>
  </div>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { ref, reactive, onMounted, inject, onBeforeUnmount } from 'vue'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import Socket from '@/utils/websocket'
import { installOlk } from '@/api/ops'
import { useOpsStore } from '@/store'
import { FitAddon } from '@xterm/addon-fit'
import { dataSourceDbList } from '@/api/modeling'
import { ShardingDsConfig } from '@/types/ops/install'
import { Message } from '@arco-design/web-vue'
import { encryptPassword } from '@/utils/jsencrypt'
import { ProcessFlag } from './ProcessFlag'
import router from '@/router'
import CustomShell from '@/views/ops/install/components/openLookeng/CustomShell.vue'

const installStore = useOpsStore()

enum statusEnum {
  RUNNING = -1,
  SUCCESS = 1,
  FAIL = 0
}

const status = ref<number>(statusEnum.RUNNING)
const loadingFunc = inject<any>('loading')
const shell = ref<null | InstanceType<typeof CustomShell>>(null)

const data = reactive<KeyValue>({
  state: -1, // -1 un install  0 installing  1 success  2 fail
  installProgress: 0,
  logs: '',
  uploadStatus: 'wait',
  zkStatus: 'wait',
  ssStatus: 'wait',
  olkStatus: 'wait',
  serviceStatus: 'wait'
})

const fitAddonTerm = ref<any>(null)

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

const resizeScreenTerm = () => {
  try { // When the window size changes, trigger the xterm resize method to make it adaptive.
    fitAddonTerm.value?.fit()
  } catch (e) {
    console.log("e", e)
  }
}

const initTerm = (term: Terminal, ws: WebSocket | undefined) => {
  const fitAddon = new FitAddon()
  fitAddonTerm.value = fitAddon
  term.loadAddon(fitAddon)
  term.open(document.getElementById('xterm') as HTMLElement)
  fitAddon.fit()
  term.clear()
  term.focus()
  termTerminal.value = term
  window.addEventListener("resize", resizeScreenTerm)
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

const terminalLogWs = ref<Socket<any, any> | undefined>()
const termTerminal = ref<Terminal>()

const openLogSocket = () => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `olk_install_log_${socketKey}` })
  terminalLogWs.value = logSocket
  loadingFunc.toLoading()
  logSocket.onopen(() => {
    localStorage.setItem('Static-pluginBase-opsOpsSimpleInstall', '1')
    initTerm(term, logSocket.ws)
    exeInstall(logSocket, `olk_install_log_${socketKey}`, term)
  })
  logSocket.onclose(() => {
    localStorage.removeItem('Static-pluginBase-opsOpsSimpleInstall')
  })
  logSocket.onmessage((messageData: any) => {
    let isProgress = /^(\d+|\d*\.\d+)%$/.test(messageData)
    if (isProgress) {
      const number = Number(messageData.slice(0, -1))
      term.write(('\x1b[2K\r'))
      if (!Number.isNaN(number) && number === 100) {
        term.writeln(messageData)
        data.logs += messageData + '\r\n'
      } else {
        term.write(messageData)
      }
    } else {
      term.writeln(messageData)
      data.logs += messageData + '\r\n'
    }
    numDynamic(messageData)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      data.installProgress = 1
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        status.value = statusEnum.SUCCESS
        loadingFunc.setBackBtnShow(false)
        loadingFunc.setNextBtnShow(false)
      } else {
        status.value = statusEnum.FAIL
        loadingFunc.cancelLoading()
      }
      logSocket.destroy()
    }
  })
}

const numDynamic = (messageData: string) => {
  if (messageData.indexOf(ProcessFlag.END_UPLOAD) >= 0) {
    data.installProgress = 0.25
    data.uploadStatus = 'finish'
  } else if (new RegExp(ProcessFlag.END_INSTALL_ZOOKEEPER).test(messageData)) {
    data.installProgress = 0.35
    data.zkStatus = 'finish'
  } else if (new RegExp(ProcessFlag.END_INSTALL_SHARDING_PROXY).test(messageData)) {
    data.installProgress = 0.45
    data.ssStatus = 'finish'
  } else if (new RegExp(ProcessFlag.END_INSTALL_OLK).test(messageData)) {
    data.installProgress = 0.55
    data.olkStatus = 'finish'
  } else if (messageData.indexOf(ProcessFlag.END_INSTALL) >= 0) {
    data.installProgress = 0.75
  } else if (messageData.indexOf(ProcessFlag.END_SERVICE) >= 0) {
    data.installProgress = 1
    data.serviceStatus = 'finish'
  }
}

const downloadLog = () => {
  const filename = 'install.log'

  const blob = new Blob([data.logs], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

onMounted(() => {
  loadingFunc.setNextBtnShow(false)
  openLogSocket()
})

onBeforeUnmount(() => {
  terminalLogWs.value?.destroy()
  termTerminal.value?.dispose()
  window.removeEventListener('resize', resizeScreenTerm)
})

const goManage = () => {
  router.push('/ops/olk')
}

const exeInstall = async (socket: Socket<any, any>, businessId: string, term: Terminal) => {
  const reqBody = JSON.parse(JSON.stringify(installStore.openLookengInstallConfig)) as KeyValue
  reqBody.dsConfig = await buildReqData()
  await encryptBodyPassword(reqBody)
  const param = {
    installContext: {
      olkConfig: reqBody
    },
    businessId: businessId
  }
  status.value = statusEnum.RUNNING
  installOlk(param).then((res: KeyValue) => {
    if (Number(res.code) !== 200) {
      socket.destroy()
    }
  }).catch((error) => {
    term.writeln(error.toString())
    loadingFunc.cancelLoading()
    socket.destroy()
  })
}

const encryptBodyPassword = async (reqBody: KeyValue) => {
  if (reqBody.dadNeedEncrypt && reqBody.dadInstallUsername === 'root') {
    reqBody.dadInstallPassword = await encryptPassword(reqBody.dadRootPassword)
  }
  if (reqBody.ssNeedEncrypt && reqBody.ssInstallUsername === 'root') {
    reqBody.ssInstallPassword = await encryptPassword(reqBody.ssRootPassword)
  }
  if (reqBody.olkNeedEncrypt && reqBody.olkInstallUsername === 'root') {
    reqBody.olkInstallPassword = await encryptPassword(reqBody.olkRootPassword)
  }
}

const retryInstall = () => {
  if (terminalLogWs.value) {
    terminalLogWs.value?.destroy()
  }
  if (termTerminal.value) {
    termTerminal.value.dispose()
  }
  data.installProgress = 0
  data.uploadStatus = 'wait'
  data.zkStatus = 'wait'
  data.ssStatus = 'wait'
  data.olkStatus = 'wait'
  data.serviceStatus = 'wait'
  openLogSocket()
}

const buildReqData = async () => {
  const ds = installStore.openLookengInstallConfig.dsConfig
  const res = await dataSourceDbList()
  const dbList: Array<ShardingDsConfig> = []
  if (ds.length <= 0) {
    Message.error('No datasource is selected, please select datasource and try again')
    return
  }
  if (res.data.length <= 0) {
    Message.error('No openGauss cluster is installed, please install a openGauss cluster and try again')
    return
  }
  ds.map((arr: Array<string>) => {
    const clusterId = arr[0]
    const nodeId = arr[1]
    const databaseName = arr[2]
    const cluster = res.data.find((item: KeyValue) => item.clusterId === clusterId)
    if (cluster) {
      const node = cluster.clusterNodes.find((item: KeyValue) => item.nodeId === nodeId)
      if (node && node.dbName.indexOf(databaseName) >= 0) {
        dbList.push({
          dbName: databaseName,
          port: node.dbPort,
          host: node.publicIp,
          username: node.dbUser,
          password: node.dbUserPassword
        } as ShardingDsConfig)
      }
    }
  })
  return dbList
}

const showCustomShell = () => {
  const allHostId = new Set()
  allHostId.add(installStore.openLookengInstallConfig.dadInstallHostId)
  allHostId.add(installStore.openLookengInstallConfig.ssInstallHostId)
  allHostId.add(installStore.openLookengInstallConfig.olkInstallHostId)
  shell.value?.open([...allHostId])
}

</script>

<style lang="less" scoped>
.exe-install-c {
  padding: 20px;
  height: 100%;
  overflow-y: auto;

  .install-doing {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    .progress-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .progress-c {
      width: 100%;
      display: flex;
      border-radius: 3px;
      padding: 5px;
      border: 1px solid rgb(var(--primary-6));
    }
  }

  .xterm {
    width: 100%;
    height: 80%;
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }

  .succ-icon-size {
    width: 120px;
    height: 120px;
  }
}
</style>
