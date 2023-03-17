<template>
  <div class="exe-install-c">
    <div class="flex-col full-w full-h" v-if="status === statusEnum.SUCESS">
      <svg-icon icon-class="ops-install-success" class="icon-size mb"></svg-icon>
      <div class="mb-lg">安装成功</div>
      <div class="flex-row">
        <a-button type="outline" class="mr" @click="goHome">
          返回首页
        </a-button>
        <a-button type="primary" @click="goOps">
          集群运维
        </a-button>
      </div>
    </div>
    <div class="flex-col" v-else>
      <a-steps :current="2" status="wait" class="mb full-w" type="arrow">
        <a-step description="上传安装包">
          上传
          <template #icon>
            <icon-loading/>
          </template>
        </a-step>
        <a-step description="安装Zookeeper">安装Zookeeper</a-step>
        <a-step description="安装ShardingProxy">安装ShardingProxy</a-step>
        <a-step description="安装OpenLookEng">安装OpenLookEng</a-step>
        <a-step description="启动所有服务">启动服务</a-step>
      </a-steps>
      <div class="install-doing mb">
        <div class="progress-top full-w">
          <div class="progress-c mr-s">
            <a-progress :color="status === statusEnum.SUCCESS ? 'green' : 'red'" :stroke-width="12" :percent="data.installProgress">
            </a-progress>
          </div>
          <a-spin v-if="status === statusEnum.RUNNING" class="mr"/>
          <a-space class="flex-row">
            <a-button type="primary" @click="retryInstall">重试</a-button>
            <a-button type="primary">下载日志</a-button>
            <a-button type="primary">自定义控制台</a-button>
          </a-space>
        </div>
      </div>
      <div id="xterm" class="xterm"></div>
    </div>
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
import { FitAddon } from 'xterm-addon-fit'
import { dataSourceDbList } from '@/api/modeling'
import { ShardingDsConfig } from '@/types/ops/install'
import { Message } from '@arco-design/web-vue'
import { encryptPassword } from '@/utils/jsencrypt'
const installStore = useOpsStore()

enum statusEnum {
  RUNNING = -1,
  SUCCESS = 1,
  FAIL = 0
}

const status = ref<number>(statusEnum.RUNNING)

const loadingFunc = inject<any>('loading')

const data = reactive<KeyValue>({
  state: -1, // -1 un install  0 installing  1 success  2 fail
  installProgress: 0
})

const getTermObj = (): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows: 28,
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

const initTerm = (term: Terminal, ws: WebSocket | undefined) => {
  const fitAddon = new FitAddon()
  term.loadAddon(fitAddon)
  term.open(document.getElementById('xterm') as HTMLElement)
  fitAddon.fit()
  term.clear()
  term.focus()
  termTerminal.value = term
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
  let isProgress = false
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `olk_install_log_${socketKey}` })
  terminalLogWs.value = logSocket
  logSocket.onopen(() => {
    localStorage.setItem('Static-pluginBase-opsOpsSimpleInstall', '1')
    initTerm(term, logSocket.ws)
    exeInstall(logSocket, `olk_install_log_${socketKey}`, term)
  })
  logSocket.onclose(() => {
    localStorage.removeItem('Static-pluginBase-opsOpsSimpleInstall')
  })
  logSocket.onmessage((messageData: any) => {
    if (/^(\d+|\d*\.\d+)%$/.test(messageData)) {
      isProgress = true
    }
    term.writeln(messageData)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      data.installProgress = 1
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        status.value = statusEnum.SUCCESS
      } else {
        status.value = statusEnum.FAIL
      }
      logSocket.destroy()
    }
  })
}

onMounted(() => {
  openLogSocket()
})

onBeforeUnmount(() => {
  terminalLogWs.value?.destroy()
  termTerminal.value?.dispose()
})

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
  installOlk(param).then((res: KeyValue) => {
    if (Number(res.code) !== 200) {
      socket.destroy()
    }
    status.value = statusEnum.RUNNING
  }).catch((error) => {
    term.writeln(error.toString())
    socket.destroy()
  })
}

const encryptBodyPassword = async (reqBody:KeyValue) => {
  if (reqBody.dadNeedEncrypt) {
    reqBody.dadInstallPassword = await encryptPassword(reqBody.dadInstallPassword)
  }
  if (reqBody.ssNeedEncrypt) {
    reqBody.ssInstallPassword = await encryptPassword(reqBody.ssInstallPassword)
  }
  if (reqBody.olkNeedEncrypt) {
    reqBody.olkInstallPassword = await encryptPassword(reqBody.olkInstallPassword)
  }
}

const retryInstall = () => {
  if (terminalLogWs.value) {
    terminalLogWs.value?.destroy()
  }
  if (termTerminal.value) {
    termTerminal.value.dispose()
  }
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
}
</style>
