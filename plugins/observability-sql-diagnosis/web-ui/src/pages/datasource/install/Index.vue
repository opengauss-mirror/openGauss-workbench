<template>
  <div class="top-sql">
    <div class="tab-wrapper-container">
      <div class="search-form-multirow" style="margin-bottom: 10px">
        <div class="row" style="justify-content: flex-start">
          <div class="filter">
            <el-button type="primary" @click="showInstallCollector">{{ $t('install.installAgent') }}</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="page-container">
      <div class="table-wrapper">
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane :label="t('install.installedAgent')" name="collector" v-loading="loadingCollector">
            <el-tree :indent="0" :data="collectorList" :props="collectorProps" #default="{ node, data }">
              <div class="custom-tree-node show-hide">
                <span v-if="data.id">
                  <el-tooltip :content="getStatusText(nodeStatusMap[data.id].status)" placement="top" effect="light">
                    <span class="state-row">
                      <span class="state" :class="nodeStatusMap[data.id].status"></span>
                    </span>
                  </el-tooltip>
                </span>
                <span v-if="data.id">
                  <span>
                    <el-popover
                      trigger="hover"
                      v-if="!nodeStatusMap[data.id].loading && nodeStatusMap[data.id].isErr"
                      placement="right"
                      :width="400"
                    >
                      <div>{{ nodeStatusMap[data.id].err }}</div>
                      <template #reference>
                        <el-icon style="margin-top: 5px" color="red"><WarningFilled /></el-icon>
                      </template>
                    </el-popover>
                  </span>
                </span>
                <el-icon v-if="data.id && nodeStatusMap[data.id].loading" class="custom-icon"><Loading /></el-icon>
                <div class="item">
                  <div class="label">{{ node.label }}</div>
                  <div class="btns-placer"></div>
                  <div class="btns" v-if="node.isLeaf">
                    <el-link
                      type="primary"
                      v-if="showStarting(data, nodeStatusMap)"
                      :disabled="nodeStatusMap[data.id].loading || !canStart(data, nodeStatusMap)"
                      style="margin-right: 5px"
                      @click="showDialog(data, 'start')"
                      >{{ t('install.start') }}</el-link
                    >
                    <el-link
                      type="primary"
                      v-if="!showStarting(data, nodeStatusMap)"
                      :disabled="nodeStatusMap[data.id].loading || !canStop(data, nodeStatusMap)"
                      style="margin-right: 5px"
                      @click="showDialog(data, 'stop')"
                      >{{ t('install.stop') }}</el-link
                    >
                    <el-link type="primary" style="margin-right: 5px" @click="showUninstallAgent(node)">{{
                      $t('install.uninstall')
                    }}</el-link>
                  </div>
                </div>
              </div>
            </el-tree>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    <InstallAgent
      v-if="installCollectorShown"
      :show="installCollectorShown"
      @changeModal="changeModalInstallCollector"
      @installed="agentInstalled()"
    />
    <UninstallAgent
      :node="uninstallAgentNode"
      v-if="uninstallAgentShown"
      :show="uninstallAgentShown"
      @changeModal="changeModalUninstallAgent"
      @installed="agentUninstalled()"
    />

    <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />

    <div class="dialog">
      <el-dialog
        width="400px"
        :title="dialogTitle"
        v-model="visible"
        :close-on-click-modal="false"
        draggable
        @close="closeDialog"
      >
        <div class="dialog-content">
          <el-form ref="formRef" :model="formData">
            <el-form-item
              :label="t('install.rootPWD')"
              prop="rootPassword"
              :rules="[{ required: true, message: t('install.collectorRules[1]'), trigger: 'blur' }]"
            >
              <el-input v-model="formData.rootPassword" show-password style="width: 200px; margin: 0 4px" />
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button v-if="dialogType === 'start'" style="padding: 5px 20px" type="primary" @click="start">{{
            $t('install.start')
          }}</el-button>
          <el-button v-if="dialogType === 'stop'" style="padding: 5px 20px" type="primary" @click="stop">{{
            $t('install.stop')
          }}</el-button>
          <el-button style="padding: 5px 20px" @click="closeDialog">{{ $t('app.cancel') }}</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import ogRequest from '@/request'
import { useI18n } from 'vue-i18n'
import InstallAgent from '@/pages/datasource/install/installAgent.vue'
import UninstallAgent from '@/pages/datasource/install/uninstallAgent.vue'
import type { FormInstance } from 'element-plus'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import { encryptPassword } from '@/utils/jsencrypt'

const { t } = useI18n()

const activeName = ref('collector')
const errorInfo = ref<string | Error>()

// start stop restart
const formRef = ref<FormInstance>()
const curNode = ref<any>()
const visible = ref<boolean>(false)
const formData = ref<any>({
  rootPassword: '',
})
const dialogTitle = ref<string>()
const dialogType = ref<string>()
const nodeStatusMap = ref<any>({})
const timer = ref<any>()

onMounted(() => {
  refreshCollectors()
})

const showStarting = (data: any, map: any) => {
  let showStartingStatus = ['unknown', 'starting', 'manualStop', 'errorThreadNotExists']
  return showStartingStatus.some((str) => str === map[data.id].status)
}
const canStart = (data: any, map: any) => {
  let canStartStatus = ['unknown', 'manualStop', 'errorThreadNotExists']
  return canStartStatus.some((str) => str === map[data.id].status)
}
const canStop = (data: any, map: any) => {
  let canStopStatus = ['normal', 'errorProgramUnhealthy']
  return canStopStatus.some((str) => str === map[data.id].status)
}
const getStatusText = (status: string) => {
  return t('install.status.' + status)
}

// install collector
const installCollectorShown = ref(false)
const showInstallCollector = () => {
  installCollectorShown.value = true
}
const changeModalInstallCollector = (val: boolean) => {
  installCollectorShown.value = val
}
const agentInstalled = () => {
  activeName.value = 'collector'
  refreshCollectors()
}

// uinstallAgent
const uninstallAgentShown = ref(false)
const uninstallAgentNode = ref<any>()
const showUninstallAgent = (node: any) => {
  uninstallAgentNode.value = node
  uninstallAgentShown.value = true
}
const changeModalUninstallAgent = (val: boolean) => {
  uninstallAgentShown.value = val
}
const agentUninstalled = () => {
  activeName.value = 'collector'
  refreshCollectors()
}

// Collector list
const collectorList = ref<Array<any>>([])
const collectorProps = {
  children: 'clusterNodes',
  label: 'label',
}
const {
  data: resCollectors,
  run: refreshCollectors,
  loading: loadingCollector,
} = useRequest(
  () => {
    return ogRequest
      .get('/observability/v1/environment/agent', {})
      .then(function (res) {
        return res
      })
      .catch(function (res) {})
  },
  { manual: true }
)
watch(resCollectors, (res: any) => {
  if (res.length) {
    collectorList.value = res
    let idList = []
    for (let index = 0; index < collectorList.value.length; index++) {
      const element = collectorList.value[index]
      element.label = element.clusterId
      for (let index2 = 0; index2 < element.clusterNodes.length; index2++) {
        const node = element.clusterNodes[index2]
        node.label =
          (node.azName ? node.azName + '_' : '') +
          node.publicIp +
          ':' +
          node.dbPort +
          (node.clusterRole ? '(' + node.clusterRole + ')' : '')

        idList.push(node.id)
      }
    }
    initNodeStatusMap(idList)
    updateNodeStatusMapByInterval()
  } else collectorList.value = []
})
const proxyList = ref<Array<any>>([])
const {
  data: res,
  run: refreshProxies,
  loading: loadingProxies,
} = useRequest(
  () => {
    return ogRequest
      .get('/observability/v1/environment/elasticsearch', {})
      .then(function (res) {
        return res
      })
      .catch(function (res) {})
  },
  { manual: true }
)
watch(res, (res: any) => {
  if (res.length) {
    proxyList.value = res
    for (let index = 0; index < proxyList.value.length; index++) {
      const element = proxyList.value[index]
      element.label = element.hostid
    }
  } else proxyList.value = []
})

const handleClick = (tab: any, event: Event) => {
  if (tab.paneName === 'collector') refreshCollectors()
  else if (tab.paneName === 'proxy') refreshProxies()
}

const initNodeStatusMap = (idList: string[]) => {
  if (!idList || idList.length === 0) {
    return
  }
  for (let id of idList) {
    if (!nodeStatusMap.value[id]) {
      nodeStatusMap.value[id] = {
        status: 'grey',
        isErr: false,
        err: '',
        loading: false,
      }
    }
  }
}

const updateNodeStatusMapByInterval = () => {
  if (timer.value) {
    clearInterval(timer.value)
  }
  timer.value = setInterval(updateNodeStatusMap(), 30000)
}

const updateNodeStatusMap = () => {
  ogRequest
    .get(`/observability/v1/agent/status`)
    .then((res) => {
      if (res.code === 200 && res.data) {
        for (let node of res.data) {
          if (!nodeStatusMap.value[node.id]) {
            nodeStatusMap.value[node.id] = {
              status: node.status,
              isErr: false,
              err: '',
              loading: false,
            }
          } else {
            nodeStatusMap.value[node.id].status = node.status
          }
        }
      }
    })
    .catch(() => {})
}

// start stop restart
const showDialog = (data: any, type: string) => {
  visible.value = true
  curNode.value = data
  dialogTitle.value = t('install.' + type)
  dialogType.value = type
}
const start = async () => {
  let id = curNode.value.id
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    nodeStatusMap.value[id].status = 'starting'
    nodeStatusMap.value[id].loading = true
    let rootPwd = await encryptPassword(formData.value.rootPassword)
    ogRequest
      .post(`/observability/v1/agent/start?id=${id}&rootPwd=${encodeURIComponent(rootPwd)}`, null, {}, { notTip: true })
      .then((res) => {
        nodeStatusMap.value[id].loading = false
        nodeStatusMap.value[id].isErr = false
        nodeStatusMap.value[id].err = ''
        updateNodeStatusMapByInterval()
      })
      .catch((msg) => {
        nodeStatusMap.value[id].loading = false
        nodeStatusMap.value[id].isErr = true
        nodeStatusMap.value[id].err = msg
        updateNodeStatusMapByInterval()
      })
    closeDialog()
  })
}
const stop = async () => {
  let id = curNode.value.id
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    nodeStatusMap.value[id].status = 'stopping'
    nodeStatusMap.value[id].loading = true
    let rootPwd = await encryptPassword(formData.value.rootPassword)
    ogRequest
      .post(`/observability/v1/agent/stop?id=${id}&rootPwd=${encodeURIComponent(rootPwd)}`, null, {}, { notTip: true })
      .then((res) => {
        nodeStatusMap.value[id].loading = false
        nodeStatusMap.value[id].isErr = false
        nodeStatusMap.value[id].err = ''
        updateNodeStatusMapByInterval()
      })
      .catch((msg) => {
        nodeStatusMap.value[id].loading = false
        nodeStatusMap.value[id].isErr = true
        nodeStatusMap.value[id].err = msg
        updateNodeStatusMapByInterval()
      })
    closeDialog()
  })
}
const closeDialog = () => {
  curNode.value = null
  formData.value.rootPassword = ''
  visible.value = false
}
</script>

<style scoped lang="scss">
@import '../../../assets/style/style1.scss';

.custom-tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
  flex-grow: 0;
  overflow: hidden;
  width: 100%;
  .state-row {
    display: flex;
    flex-direction: row;
    .state {
      width: 6px;
      height: 6px;
      margin-right: 4px;
      border-radius: 100px;
      &.normal {
        background: var(--green, #52c41a);
        box-shadow: 0px 1px 3px 0px #52c41a;
      }
      &.starting,
      &.manualStop {
        background: var(--green, #ffa53c);
        box-shadow: 0px 1px 3px 0px #ffa53c;
      }
      &.errorThreadNotExists,
      &.errorProgramUnhealthy {
        background: var(--red, #f6605a);
        box-shadow: 0px 1px 3px 0px #f6605a;
      }
      &.starting,
      &.stopping {
        background: var(--blue, #0093ff);
        box-shadow: 0px 1px 3px 0px #b8b8b8;
      }
      &.unknown {
        background: var(--grey, #b8b8b8);
        box-shadow: 0px 1px 3px 0px #b8b8b8;
      }
    }
  }
  .item {
    display: flex;
    align-items: center;
    flex-direction: row;
    overflow: hidden;
    width: 100%;

    .label {
      flex-shrink: 0;
      overflow: hidden;
    }
    .btns {
      align-items: center;
      display: none;
      padding-left: 5px;
      padding-right: 4px;
    }
  }
  .btn-group {
    display: none;
    // align-items: center;
    flex-direction: row;
    position: absolute;
    z-index: 9999;
    background-color: #e4e7ed;
    padding-left: 5px;
    right: 0;
  }
}
.show-hide:hover .btns {
  display: flex !important;
  flex-direction: row;
  justify-content: flex-end;
  flex-grow: 1;
}
.show-hide:hover .label {
  flex-shrink: 1;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.custom-icon {
  margin-top: 2px;
  animation: spin 1s infinite linear;
}
</style>
