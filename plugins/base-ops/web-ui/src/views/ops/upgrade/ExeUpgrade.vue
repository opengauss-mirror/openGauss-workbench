<template>
  <div class="exe-upgrade-c">
    <div class="top">
      <a-button
        class="mr"
        v-if="!data.allVisible"
        @click="handleExpandAll"
      >{{ $t('upgrade.ExeUpgrade.5uuo8zf4cfw0') }}</a-button>
      <a-button
        class="mr"
        v-if="data.allVisible"
        @click="handlePackup"
      >{{ $t('upgrade.ExeUpgrade.5uuo8zf4dn40') }}</a-button>
      <a-button
        class="mr"
        v-if="hasToSubmit"
        @click="handleBatchSubmit"
      >{{ $t('upgrade.ExeUpgrade.5uuo8zf4dxo0') }}</a-button>
      <a-button
        v-if="hasToRollback"
        @click="handleBatchCallback"
      >{{ $t('upgrade.ExeUpgrade.5uuo8zf4e4w0') }}</a-button>
    </div>
    <div
      class="upgrade-item"
      v-for="item in data.clusterList"
      :key="item.id"
    >
      <div
        class="item-top label-color"
        @click="item.visible = !item.visible"
      >
        <div class="flex-row">
          <div class="mr">{{ $t('upgrade.ExeUpgrade.else1') }}:{{ item.name }}{{ item.version }}</div>
          <div class="mr">{{ $t('upgrade.ExeUpgrade.5uuo8zf4ebw0') }}</div>
          <div class="mr-s">{{ item.targetVersion }}</div>
          <icon-code-square
            :size="25"
            class="label-color"
            style="cursor: pointer;"
            @click.stop="showTerminal(item)"
          />
        </div>
        <div
          class="flex-row"
          v-if="item.upgradeLoading"
        >
          <div class="mr-s">{{ $t('upgrade.ExeUpgrade.5uuo8zf4ej80') }}</div>
          <div>{{ item.upgradeProgress }}%</div>
        </div>
        <div v-if="item.rollbackLoading">
          {{ $t('upgrade.ExeUpgrade.5uuo8zf4eq40') }}
        </div>
        <div v-if="item.submitLoading">
          {{ $t('upgrade.ExeUpgrade.5uuo8zf4eww0') }}
        </div>
        <div
          class="flex-row"
          v-if="!item.rollbackLoading && !item.submitLoading && item.state !== UPGRADE_STATE.UPGRADING"
        >
          <div class="mr-s">{{ getStateText(item.state) }}</div>
          <icon-check-circle
            v-if="item.state === UPGRADE_STATE.SUCCESS || item.state === UPGRADE_STATE.TO_SUBMIT"
            class="success-icon"
            :size="25"
          />
          <icon-close-circle
            v-if="item.state === UPGRADE_STATE.FAIL"
            class="fail-icon"
            :size="25"
          />
        </div>
        <div class="flex-row top-right">
          <div
            class="mr-s"
            v-if="item.state === UPGRADE_STATE.UPGRADING"
          >{{ $t('upgrade.ExeUpgrade.5uuo8zf4hk40') }}:{{ upgradeStepName[item.upgradeStep] }}
          </div>
          <a-button
            class="mr"
            type="outline"
            @click.stop="handleDownloadLog(item)"
          >{{ $t('upgrade.ExeUpgrade.5uuo8zf4f480') }}</a-button>
          <div
            class="flex-row"
            v-if="item.state !== UPGRADE_STATE.UPGRADING && !item.rollbackLoading && !item.submitLoading"
          >
            <a-button
              class="mr"
              type="outline"
              v-if="item.state === UPGRADE_STATE.TO_UPGRADE"
              @click.stop="handleToUpgrade(item)"
            >{{ $t('upgrade.ExeUpgrade.5uuo8zf4fao0') }}</a-button>
            <a-button
              class="mr"
              type="outline"
              v-if="item.state === UPGRADE_STATE.TO_SUBMIT"
              @click.stop="handleSubmitUpgrade(item)"
            >{{ $t('upgrade.ExeUpgrade.5uuo8zf4fmg0') }}</a-button>
            <a-button
              class="mr"
              type="outline"
              v-if="item.state === UPGRADE_STATE.FAIL"
              @click.stop="handleReset(item)"
            >{{ $t('upgrade.ExeUpgrade.5uuo8zf4ftc0') }}</a-button>
            <a-button
              type="outline"
              v-if="item.state === UPGRADE_STATE.TO_SUBMIT || item.state === UPGRADE_STATE.FAIL"
              @click.stop="handleRollback(item)"
            >{{ $t('upgrade.ExeUpgrade.5uuo8zf4fzg0') }}</a-button>
          </div>
        </div>
      </div>
      <div
        class="item-center"
        v-show="item.visible"
      >
        <a-steps
          class="mb-s"
          type="arrow"
          :current="item.upgradeStep"
          small
          v-if="item.state === UPGRADE_STATE.TO_UPGRADE || item.state === UPGRADE_STATE.UPGRADING || item.state === UPGRADE_STATE.FAIL"
        >
          <a-step>{{ $t('upgrade.ExeUpgrade.5uuo8zf4g600') }}</a-step>
          <a-step>{{ $t('upgrade.ExeUpgrade.5uuo8zf4gc40') }}</a-step>
          <a-step>{{ $t('upgrade.ExeUpgrade.5uuo8zf4gi00') }}</a-step>
          <a-step>{{ $t('upgrade.ExeUpgrade.5uuo8zf4gs80') }}</a-step>
          <a-step>{{ $t('upgrade.ExeUpgrade.5uuo8zf4gz40') }}</a-step>
          <a-step>{{ $t('upgrade.ExeUpgrade.5uuo8zf4h580') }}</a-step>
        </a-steps>
        <div
          class="to-upgrade-c"
          v-if="item.state === UPGRADE_STATE.TO_UPGRADE"
        >
          <svg-icon
            icon-class="developing"
            class="icon-size"
          ></svg-icon>
          <div class="content ft-m mb-lg">{{ $t('upgrade.ExeUpgrade.5uuo8zf4hbk0') }}</div>
        </div>
        <div
          v-show="item.state !== UPGRADE_STATE.TO_UPGRADE"
          :id="`xterm${item.name}`"
          class="xterm"
        >
        </div>
      </div>
    </div>
  </div>
  <cluster-control-dlg ref="clusterControlRef"></cluster-control-dlg>
</template>

<script lang="ts" setup>
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import { FitAddon } from '@xterm/addon-fit'
import { KeyValue } from '@/types/global'
import { reactive, onMounted, ref, nextTick, computed, inject, watch } from 'vue'
import Socket from '@/utils/websocket'
import { upgrade, upgradeCommit, upgradeRollback, getSysUploadPath, download } from '@/api/ops'
import ClusterControlDlg from './ClusterControlDlg.vue';
import dayjs from "dayjs";
import { encryptPassword } from "@/utils/jsencrypt";
import { useUpgradeStore, upClusterInfo, targetVersion } from '@/store/modules/ops/upgrade'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const upgradeStore = useUpgradeStore()
enum UPGRADE_STATE {
  TO_UPGRADE,
  UPGRADING,
  TO_SUBMIT,
  FAIL,
  SUCCESS
}
const loadingFunc = inject<any>('loading')

const hasToSubmit = computed(() => {
  return data.clusterList.some((item: KeyValue) => {
    return item.state === UPGRADE_STATE.TO_SUBMIT && !item.submitLoading && !item.rollbackLoading
  })
})

const hasToRollback = computed(() => {
  return data.clusterList.some((item: KeyValue) => {
    return (item.state === UPGRADE_STATE.FAIL && !item.rollbackLoading) || (item.state === UPGRADE_STATE.TO_SUBMIT && !item.submitLoading && !item.rollbackLoading)
  })
})

const data = reactive<KeyValue>({
  clusterList: [],
  allVisible: true,
  uploadPath: ''
})

const upgradeStepName = computed((): KeyValue => {
  return {
    1: t('upgrade.ExeUpgrade.5uuo8zf4g600'),
    2: t('upgrade.ExeUpgrade.5uuo8zf4gc40'),
    3: t('upgrade.ExeUpgrade.5uuo8zf4gi00'),
    4: t('upgrade.ExeUpgrade.5uuo8zf4gs80'),
    5: t('upgrade.ExeUpgrade.5uuo8zf4gz40'),
    6: t('upgrade.ExeUpgrade.5uuo8zf4h580')
  }
})

onMounted(async () => {
  const res: KeyValue = await getSysUploadPath()
  if (Number(res.code) === 200) {
    data.uploadPath = res.data
  }

  const upClusterList: upClusterInfo[] = upgradeStore.getUpClusterList
  const targetVersion: targetVersion = upgradeStore.getUpTargetVersion
  // get path 
  let targetPath: string
  let upgradePackageType: string
  if (targetVersion.packagePath) {
    // offline
    upgradePackageType = 'offline'
    targetPath = targetVersion.packagePath.realPath
  } else {
    // online
    upgradePackageType = 'online'
    targetPath = targetVersion.packageUrl
  }
  data.clusterList = []
  upClusterList.forEach((item: upClusterInfo) => {
    const temp = {
      name: item.id,
      version: item.currentVersion,
      targetVersion: targetVersion.packageVersionNum,
      targetPackagePath: targetPath,
      upgradePackageType: upgradePackageType,
      rootPassword: item.rootPassword,
      hostList: item.hostList,
      upgradeLoading: false,
      rollbackLoading: false,
      submitLoading: false,
      upgradeStep: 1,
      upgradeProgress: 0,
      state: UPGRADE_STATE.UPGRADING,
      loading: false,
      termLog: null,
      visible: true
    }
    data.clusterList.push(temp)
  })

  nextTick(() => {
    data.clusterList.forEach((item: KeyValue) => {
      openSocket(item)
    })
  })
})

const openSocket = (cluster: KeyValue) => {
  if (cluster.upgradePackageType === 'online') {
    // open upgradePackage websocket
    openPackageSocket(cluster)
  } else {
    // open upgrade websocket
    openLogSocket(cluster)
  }
}

const openPackageSocket = (cluster: KeyValue) => {
  // get url last fileName
  let temp = false
  const fileName =
    cluster.targetPackagePath.split('/')[
    cluster.targetPackagePath.split('/').length - 1
    ]
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const websocket = new Socket({ url: `download_upgrade_Package_${socketKey}` })
  websocket.onopen(() => {
    cluster.upgradeLoading = true
    const param = {
      resourceUrl: cluster.targetPackagePath,
      targetPath: data.uploadPath,
      fileName: fileName,
      connectType: 'DOWNLOAD_UPGRADE_PACKAGE',
      businessId: `download_upgrade_Package_${socketKey}`
    }
    download(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        term.writeln(res.msg)
        cluster.state = UPGRADE_STATE.FAIL
        websocket.destroy()
      }
      term.writeln('downloading the upgrade package \r\n')
    }).catch((error) => {
      cluster.upgradeLoading = false
      term.writeln(error.toString())
      cluster.state = UPGRADE_STATE.FAIL
      websocket.destroy()
    })
    initPackageTerm(term, cluster)
  })
  websocket.onmessage((messageData: any) => {
    if (!isNaN(Number(messageData))) {
      if (temp) {
        term.write('\x1b[2K\r')
        if (Number(messageData) === 1) {
          term.writeln((Number(messageData) * 100).toFixed(2) + '%')
        } else {
          term.write((Number(messageData) * 100).toFixed(2) + '%')
        }
      } else {
        term.write((Number(messageData) * 100).toFixed(2) + '%')
      }
      if (Number(messageData) !== 1) {
        temp = true
      }
      // default 10%
      cluster.upgradeProgress = 10
      if (Number(messageData) === 1) {
        cluster.targetPackagePath = data.uploadPath + fileName
        // cluster.state = UPGRADE_STATE.FAIL
        initClusterTerm(cluster)
        openLogSocket(cluster)
        websocket.destroy()
      }
    } else {
      term.writeln(messageData)
      cluster.logs += messageData + '\r\n'
      cluster.state = UPGRADE_STATE.FAIL
      cluster.upgradeLoading = false
      websocket.destroy()
    }
  })
}

const handleExpandAll = () => {
  data.allVisible = true
  data.clusterList.forEach((item: KeyValue) => {
    item.visible = true
  })
}
const handlePackup = () => {
  data.allVisible = false
  data.clusterList.forEach((item: KeyValue) => {
    item.visible = false
  })
}

const handleBatchSubmit = () => {
  data.clusterList.forEach((item: KeyValue) => {
    if (item.state === UPGRADE_STATE.TO_SUBMIT) {
      handleSubmitUpgrade(item)
    }
  })
}

const handleBatchCallback = () => {
  data.clusterList.forEach((item: KeyValue) => {
    if (item.state === UPGRADE_STATE.FAIL || item.state === UPGRADE_STATE.TO_SUBMIT) {
      handleRollback(item)
    }
  })
}

const openLogSocket = (cluster: KeyValue) => {
  let temp = false
  const term = cluster.termLog ? cluster.termLog : getTermObj()
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `upgrade_${socketKey}` })
  data.terminalWs = logSocket
  logSocket.onopen(async () => {
    // get Store upgrade cluster param
    const pwd = await encryptPassword(cluster.rootPassword)
    cluster.upgradeLoading = true
    const param = {
      clusterId: cluster.name,
      upgradeType: 'GRAY_UPGRADE',
      upgradePackagePath: cluster.targetPackagePath,
      versionNum: cluster.targetVersion,
      hostRootPassword: pwd,
      businessId: `upgrade_${socketKey}`
    }
    upgrade(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        term.writeln(res.msg)
        logSocket.destroy()
      }
    }).catch((error) => {
      term.writeln(error.toString())
      cluster.state = UPGRADE_STATE.FAIL
      cluster.upgradeLoading = false
      logSocket.destroy()
    })
    initTermLog(term, logSocket.ws, cluster)
    localStorage.setItem('Static-pluginBase-opsOpsInstall', '1')
    cluster.logs = ''
  })
  logSocket.onclose(() => {
    localStorage.removeItem('Static-pluginBase-opsOpsInstall')
  })
  logSocket.onmessage((messageData: any) => {
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
    if (messageData === 'BEFORE_UPLOAD_PACKAGE') {
      temp = true
    }
    if (messageData === 'END_UPLOAD_PACKAGE') {
      temp = false
    }
    cluster.logs += messageData + '\r\n'
    syncProgressNumber(messageData, cluster)
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        data.installStepNum = 8
        cluster.state = UPGRADE_STATE.TO_SUBMIT
      } else {
        data.currentStatus = 'error'
        cluster.state = UPGRADE_STATE.FAIL
        nextTick(() => {
          let fitAddon = new FitAddon()
          term.loadAddon(fitAddon)
          fitAddon.fit()
        })
      }
      cluster.upgradeLoading = false
      logSocket.destroy()
    }
  })
}

const syncProgressNumber = (messageData: string, cluster: KeyValue) => {
  if (messageData === 'ENABLE_STREAM_REPLICATION') {
    cluster.upgradeProgress = 20
    cluster.upgradeStep = 2
  }
  if (messageData === 'CHECK_CLUSTER_STATUS') {
    cluster.upgradeProgress = 40
    cluster.upgradeStep = 3
  }
  if (messageData === 'PREINSTALL') {
    cluster.upgradeProgress = 60
    cluster.upgradeStep = 4
  }
  if (messageData === 'DO_UPGRADE') {
    cluster.upgradeProgress = 80
    cluster.upgradeStep = 5
  }
  if (messageData === 'UPGRADE_CHECK_CLUSTER_STATUS') {
    cluster.upgradeProgress = 100
    cluster.upgradeStep = 6
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

const initPackageTerm = (term: Terminal, cluster: KeyValue) => {
  const fitAddon = new FitAddon()
  term.loadAddon(fitAddon)
  term.open(document.getElementById(`xterm${cluster.name}`) as HTMLElement)
  fitAddon.fit()
  term.clear()
  term.focus()
  cluster.termLog = term
}

const initTermLog = (term: Terminal, ws: WebSocket | undefined, cluster: KeyValue) => {
  if (ws) {
    const fitAddon = new FitAddon()
    term.loadAddon(fitAddon)
    term.open(document.getElementById(`xterm${cluster.name}`) as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    cluster.termLog = term
  }
}
const clusterControlRef = ref<null | InstanceType<typeof ClusterControlDlg>>(null)
const showTerminal = (clusterData: KeyValue) => {
  clusterControlRef.value?.open(clusterData.hostList, clusterData.rootPassword)
}

const getStateText = (state: UPGRADE_STATE) => {
  if (state === UPGRADE_STATE.TO_SUBMIT) {
    return t('upgrade.ExeUpgrade.5uuo8zf4hq00')
  } if (state === UPGRADE_STATE.SUCCESS) {
    return t('upgrade.ExeUpgrade.5uuo8zf4hvk0')
  } if (state === UPGRADE_STATE.FAIL) {
    return t('upgrade.ExeUpgrade.5uuo8zf4i0s0')
  }
}

const handleToUpgrade = (cluster: KeyValue) => {
  initClusterTerm(cluster)
  cluster.state = UPGRADE_STATE.UPGRADING
  openSocket(cluster)
}

const handleDownloadLog = (cluster: KeyValue) => {
  const time = dayjs().format('YYYY-MM-DD_HH:mm:ss')
  const filename = `upgrade_${cluster.name}_${time}.log`

  const blob = new Blob([cluster.logs ? cluster.logs : ''], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const handleSubmitUpgrade = (cluster: KeyValue) => {
  initClusterTerm(cluster)
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `submit_upgrade_${socketKey}` })
  logSocket.onopen(async () => {
    cluster.submitLoading = true
    // get Store upgrade cluster param
    const param = {
      clusterId: cluster.name,
      upgradeType: 'GRAY_UPGRADE',
      upgradePackagePath: cluster.targetPackagePath,
      versionNum: cluster.targetVersion,
      hostRootPassword: cluster.rootPassword,
      businessId: `submit_upgrade_${socketKey}`
    }
    upgradeCommit(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        term.writeln(res.msg)
        logSocket.destroy()
      }
    }).catch((error) => {
      term.writeln(error.toString())
      cluster.state = UPGRADE_STATE.TO_SUBMIT
      cluster.submitLoading = false
      logSocket.destroy()
    })
    initTermLog(term, logSocket.ws, cluster)
    localStorage.setItem('Static-pluginBase-opsOpsInstall', '1')
  })
  logSocket.onclose(() => {
    localStorage.removeItem('Static-pluginBase-opsOpsInstall')
  })
  logSocket.onmessage((messageData: any) => {
    term.writeln(messageData)
    cluster.logs += messageData + '\r\n'
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        cluster.state = UPGRADE_STATE.SUCCESS
      } else {
        nextTick(() => {
          let fitAddon = new FitAddon()
          term.loadAddon(fitAddon)
          fitAddon.fit()
        })
      }
      cluster.submitLoading = false
      logSocket.destroy()
    }
  })
}

const initClusterTerm = (cluster: KeyValue) => {
  if (cluster.termLog) {
    cluster.termLog.dispose()
    cluster.termLog = null
  }
}

const handleReset = (cluster: KeyValue) => {
  cluster.state = UPGRADE_STATE.UPGRADING
  initClusterTerm(cluster)
  openSocket(cluster)
}

const handleRollback = (cluster: KeyValue) => {
  initClusterTerm(cluster)
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const logSocket = new Socket({ url: `rollback_upgrade_${socketKey}` })
  logSocket.onopen(async () => {
    cluster.rollbackLoading = true
    // get Store upgrade cluster param
    const param = {
      clusterId: cluster.name,
      upgradeType: 'GRAY_UPGRADE',
      upgradePackagePath: cluster.targetPackagePath,
      versionNum: cluster.targetVersion,
      hostRootPassword: cluster.rootPassword,
      businessId: `rollback_upgrade_${socketKey}`
    }
    upgradeRollback(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        term.writeln(res.msg)
        logSocket.destroy()
      }
    }).catch((error) => {
      term.writeln(error.toString())
      cluster.rollbackLoading = false
      logSocket.destroy()
    })
    initTermLog(term, logSocket.ws, cluster)
    localStorage.setItem('Static-pluginBase-opsOpsInstall', '1')
  })
  logSocket.onclose(() => {
    localStorage.removeItem('Static-pluginBase-opsOpsInstall')
  })
  logSocket.onmessage((messageData: any) => {
    term.writeln(messageData)
    cluster.logs += messageData + '\r\n'
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        cluster.state = UPGRADE_STATE.TO_UPGRADE
      } else {
        nextTick(() => {
          let fitAddon = new FitAddon()
          term.loadAddon(fitAddon)
          fitAddon.fit()
        })
      }
      cluster.rollbackLoading = false
      logSocket.destroy()
    }
  })
}

watch(() => data.clusterList, () => {
  const hasLoadingProc = data.clusterList.some((item:KeyValue) => item.rollbackLoading || item.upgradeLoading || item.submitLoading)
  if (hasLoadingProc) {
    loadingFunc.startLoading()
    localStorage.setItem('Static-pluginBase-opsOpsUpgrade', '1')
  } else {
    loadingFunc.cancelLoading()
    localStorage.removeItem('Static-pluginBase-opsOpsUpgrade')
  }
}, { deep: true })
</script>
<style lang="less" scoped>
.exe-upgrade-c {
  height: 100%;
  width: 100%;
  overflow: auto;

  .top {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-bottom: 16px;
  }

  .upgrade-item {
    border: 1px solid var(--color-text-3);
    border-radius: 4px;
    margin-bottom: 16px;

    .item-top {
      background-color: var(--color-text-4);
      padding: 8px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      cursor: pointer;

      .top-right {
        width: 300px;
        justify-content: flex-end;
      }

      .success-icon {
        color: #08f108;
      }

      .fail-icon {
        color: #FF7D01;
      }
    }

    .item-center {
      padding: 8px;

      .to-upgrade-c {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;

        .icon-size {
          width: 300px;
          height: 300px;
        }

        .content {
          font-weight: bold;
          color: var(--color-neutral-4);
        }
      }

      .xterm {
        width: 100%;
        height: 370px;
      }
    }
  }
}
</style>