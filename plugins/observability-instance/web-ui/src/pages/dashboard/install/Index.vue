<template>
  <div class="top-sql">
    <div class="tab-wrapper-container">
      <div class="search-form-multirow" style="margin-bottom: 10px">
        <div class="row" style="justify-content: flex-start">
          <div class="filter">
            <el-button type="primary" @click="showInstallCollector">{{ $t('install.installAgent') }}</el-button>
            <el-button type="primary" @click="showInstallProxy">{{ $t('install.installProxy') }}</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="page-container">
      <div class="table-wrapper">
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane :label="t('install.installedAgent')" name="collector" v-loading="loadingCollector">
            <el-tree :indent="10" :data="collectorList" :props="collectorProps" #default="{ node, data }">
              <div class="custom-tree-node show-hide">
                <svg-icon v-if="node.level === 1" class="icon" name="tree-folder" />
                <svg-icon v-if="node.level === 2" class="icon" name="tree-file" />
                <svg-icon v-if="node.level === 3" class="icon" name="tree-drive-file" />
                <span v-if="data.id" style="margin-left: 2px">
                  <el-tooltip
                    :content="getStatusText(agentNodeStatusMap[data.id].status)"
                    placement="top"
                    effect="light"
                  >
                    <span class="state-row">
                      <span class="state" :class="agentNodeStatusMap[data.id].status"></span>
                    </span>
                  </el-tooltip>
                </span>
                <span v-if="data.id">
                  <span v-if="!agentNodeStatusMap[data.id].loading">
                    <el-popover trigger="hover" v-if="agentNodeStatusMap[data.id].isErr" placement="right" :width="400">
                        <div>{{ agentNodeStatusMap[data.id].err }}</div>
                        <template #reference>
                          <el-icon style="margin-top: 5px" color="red"><WarningFilled /></el-icon>
                        </template>
                    </el-popover>
                  </span>
                  <el-icon class="custom-icon" v-if="agentNodeStatusMap[data.id].loading"><Loading /></el-icon>
                </span>
                <div class="item">
                  <div class="label">{{ node.label }}</div>
                  <div class="btns" v-if="node.level === 1">
                    <el-link
                      type="primary"
                      v-if="showStarting(data, agentNodeStatusMap)"
                      :disabled="agentNodeStatusMap[data.id].loading || !canStart(data, agentNodeStatusMap)"
                      style="margin-right: 5px"
                      @click="startAgent(data.id)"
                      >{{ t('install.start') }}</el-link
                    >

                    <el-link
                      :disabled="agentNodeStatusMap[data.id].loading || !canStop(data, agentNodeStatusMap)"
                      type="primary"
                      v-if="!showStarting(data, agentNodeStatusMap)"
                      style="margin-right: 5px"
                      @click="stopAgent(data.id)"
                      >{{ t('install.stop') }}</el-link
                    >
                    <el-link style="margin-right: 5px" type="primary" @click="showEditCollector(node)">
                      {{ $t('app.edit') }}
                    </el-link>
                    <el-link type="primary" @click="showUninstallAgent(node)">{{ $t('install.uninstall') }}</el-link>
                  </div>
                  <div class="btns" v-if="node.isLeaf">
                    <el-link type="primary" @click="showEditCollector(node.parent.parent, node.data.nodeId)">{{
                      $t('app.delete')
                    }}</el-link>
                  </div>
                </div>
              </div>
            </el-tree>
          </el-tab-pane>
          <el-tab-pane :label="t('install.installedProxy')" name="proxy" v-loading="loadingProxies">
            <el-tree :indent="10" :data="proxyList" :props="defaultProps" #default="{ node, data }">
              <span class="custom-tree-node show-hide">
                <span v-if="data.id" style="margin-left: 2px">
                  <el-tooltip
                    :content="getStatusText(proxyNodeStatusMap[data.id].status)"
                    placement="top"
                    effect="light"
                  >
                    <span class="state-row">
                      <span class="state" :class="proxyNodeStatusMap[data.id].status"></span>
                    </span>
                  </el-tooltip>
                </span>
                <span v-if="data.id">
                  <el-icon class="custom-icon" v-if="proxyNodeStatusMap[data.id].loading"><Loading /></el-icon>
                  <span v-if="!proxyNodeStatusMap[data.id].loading">
                    <el-popover trigger="hover" v-if="proxyNodeStatusMap[data.id].isErr" placement="right" :width="400">
                      <div>{{ proxyNodeStatusMap[data.id].err }}</div>
                      <template #reference>
                        <el-icon style="margin-top: 5px" color="red"><WarningFilled /></el-icon>
                      </template>
                    </el-popover>
                  </span>
                </span>
                <div class="item">
                  <div class="label">{{ data.label }}</div>
                  <div class="btns">
                    <el-link
                      v-if="data.type === 'PROMETHEUS_MAIN'"
                      type="primary"
                      style="margin-right: 5px"
                      @click="
                        showReinstallProxy(node)
                      "
                      >{{ $t('install.reinstallBtn') }}
                    </el-link>
                    <el-link type="primary" style="margin-right: 5px" @click="startProxy(data.id)" v-if="showStarting(data, proxyNodeStatusMap)"
                      :disabled="proxyNodeStatusMap[data.id].loading || !canStart(data, proxyNodeStatusMap)">{{
                      t('install.start')
                    }}</el-link>
                    <el-link type="primary" style="margin-right: 5px" @click="stopProxy(data.id)" v-if="!showStarting(data, proxyNodeStatusMap)"
                      :disabled="proxyNodeStatusMap[data.id].loading || !canStop(data, proxyNodeStatusMap)">{{
                      t('install.stop')
                    }}</el-link>
                    <el-link style="margin-right: 5px" type="primary" @click="showEditProxy(node)">
                      {{ $t('app.edit') }}
                    </el-link>
                    <el-link
                      v-if="data.type === 'PROMETHEUS'"
                      type="primary"
                      style="margin-right: 5px"
                      @click="
                        showUninstallProxy({
                          label: data.label,
                          data,
                        })
                      "
                      >{{ $t('install.uninstall') }}</el-link
                    >
                  </div>
                </div>
              </span>
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
    <InstallAgent
      v-if="editCollectorShown"
      :show="editCollectorShown"
      :editing="true"
      :node="editNode"
      :delNodeId="deleteNodeId"
      @changeModal="changeModalEditCollector"
      @installed="agentEdited()"
    />
    <InstallProxy
      v-if="installProxyShown"
      :show="installProxyShown"
      :node="proxyNode"
      :editing="isProxyEdit"
      :isReinstall="isReinstall"
      @changeModal="changeModalInstallProxy"
      @installed="proxyInstalled()"
    />
    <UninstallProxy
      :node="uninstallProxyNode"
      v-if="uninstallProxyShown"
      :show="uninstallProxyShown"
      @changeModal="changeModalUninstallProxy"
      @installed="proxyUninstalled()"
    />
    <UninstallAgent
      :node="uninstallAgentNode"
      v-if="uninstallAgentShown"
      :show="uninstallAgentShown"
      @changeModal="changeModalUninstallAgent"
      @installed="agentUninstalled()"
    />

    <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
  </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import restRequest from '@/request/restful'
import ogRequest from '@/request'
import { useI18n } from 'vue-i18n'
import InstallAgent from '@/pages/dashboard/install/installAgent.vue'
import InstallProxy from '@/pages/dashboard/install/installProxy.vue'
import UninstallAgent from '@/pages/dashboard/install/uninstallAgent.vue'
import UninstallProxy from '@/pages/dashboard/install/uninstallProxy.vue'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const activeName = ref('collector')
const errorInfo = ref<string | Error>()
const agentNodeStatusMap = ref<any>({})
const proxyNodeStatusMap = ref<any>({})
const agentTimer = ref<any>()
const proxyTimer = ref<any>()

const emits = defineEmits(["isUninstallAgent"]);

const showUninstallInfo = ref<boolean>(false)
onMounted(() => {
  showUninstallInfo.value = true
  refreshCollectors()
})

// install collector
const installCollectorShown = ref(false)
const showInstallCollector = () => {
  restRequest
      .get('/observability/v1/environment/prometheus', {})
      .then(function (res) {
        let promeList = res.filter((item) => item.type === 'PROMETHEUS') || []
        if (promeList.length === 0) {
          ElMessage.error({
            message: t('install.installServerAlert'),
            type: 'error',
          })
          return;
        }
        installCollectorShown.value = true
      })
}
const changeModalInstallCollector = (val: boolean) => {
  installCollectorShown.value = val
}
const agentInstalled = () => {
  activeName.value = 'collector'
  refreshCollectors()
}

// edit collector
const editCollectorShown = ref(false)
const editNode = ref({})
const deleteNodeId = ref('')
const showEditCollector = (node: any, delNodeId?: string) => {
  editNode.value = node.data
  deleteNodeId.value = delNodeId !== undefined ? delNodeId : ''
  editCollectorShown.value = true
}
const changeModalEditCollector = (val: boolean) => {
  editCollectorShown.value = val
}
const agentEdited = () => {
  activeName.value = 'collector'
  refreshCollectors()
}

// install proxy
const installProxyShown = ref(false)
const proxyNode = ref('')
const isProxyEdit = ref<boolean>(false)
const isReinstall = ref<boolean>(false)
const showInstallProxy = () => {
  isReinstall.value = false
  restRequest
      .get('/observability/v1/environment/prometheus', {})
      .then(function (res) {
        let promeList = res.filter((item) => item.type === 'PROMETHEUS') || []
        if (promeList.length > 0) {
          ElMessage.error({
            message: t('install.installedServerAlert'),
            type: 'error',
          })
          return;
        }
        installProxyShown.value = true
        isProxyEdit.value = false
      })
}
const showEditProxy = (node: any) => {
  isReinstall.value = false
  proxyNode.value = node.data
  installProxyShown.value = true
  isProxyEdit.value = true
}
const showReinstallProxy = (node: any) => {
  proxyNode.value = node.data
  installProxyShown.value = true
  isProxyEdit.value = true
  isReinstall.value = true
}
const changeModalInstallProxy = (val: boolean) => {
  installProxyShown.value = val
}
const proxyInstalled = () => {
  activeName.value = 'proxy'
  refreshProxies()
}
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

// uinstallProxy
const uninstallProxyShown = ref(false)
const uninstallProxyNode = ref<any>()
const showUninstallProxy = (node: any) => {
  uninstallProxyNode.value = node
  uninstallProxyShown.value = true
}
const changeModalUninstallProxy = (val: boolean) => {
  uninstallProxyShown.value = val
}
const proxyUninstalled = () => {
  activeName.value = 'proxy'
  refreshProxies()
}

// Collector list
const collectorList = ref<Array<any>>([])
const collectorProps = {
  children: 'children',
  label: 'label',
}
const {
  data: resCollectors,
  run: refreshCollectors,
  loading: loadingCollector,
} = useRequest(
  () => {
    return restRequest
      .get('/observability/environment/api/v1/exporters', {})
      .then(function (res) {
        if ((!res || res.length === 0) && showUninstallInfo.value) {
          showUninstallInfo.value = false
          //不能放在watch里面，会触发第二次watch
          emits('isUninstallAgent')  
        }
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
      element.label = element.hostName + '(' + element.hostPublicIp + ':' + element.exporterPort + ')'
      element.children = element.clusters
      idList.push(element.id)
      for (let index2 = 0; index2 < element.children.length; index2++) {
        const cluster = element.children[index2]
        cluster.label = cluster.clusterId
        cluster.children = cluster.clusterNodes
        for (let index3 = 0; index3 < cluster.children.length; index3++) {
          const node = cluster.children[index3]
          node.label =
            (node.azName ? node.azName + '_' : '') +
            node.publicIp +
            ':' +
            node.dbPort +
            (node.clusterRole ? '(' + node.clusterRole + ')' : '')
        }
      }
    }
    initAgentNodeStatusMap(idList)
    updateAgentNodeStatusMapByInterval()
  } else {
    collectorList.value = []
  }
})

// Proxy list
const defaultProps = {
  children: 'children',
  label: 'label',
}
const proxyList = ref<Array<any>>([])
const {
  data: res,
  run: refreshProxies,
  loading: loadingProxies,
} = useRequest(
  () => {
    return restRequest
      .get('/observability/v1/environment/prometheus', {})
      .then(function (res) {
        return res
      })
      .catch(function (res) {})
  },
  { manual: true }
)
watch(res, (res: any) => {
  if (res.length) {
    let idList = []
    let promeMainList = res.filter((item) => item.type === 'PROMETHEUS_MAIN')
    if (promeMainList.length === 0) {
      proxyList.value = []
      return
    }
    idList.push(promeMainList[0].id)
    promeMainList[0]['label'] = 'Prometheus'
    promeMainList[0]['children'] = []
    let promeList = res.filter((item) => item.type === 'PROMETHEUS')
    for (let prom of promeList) {
      prom['label'] = prom.host ? prom.host.name + '(' + prom.host.publicIp + ':' + prom.port + ')' : prom.hostid
      promeMainList[0]['children'].push(prom)
      idList.push(prom.id)
    }
    proxyList.value = promeMainList
    initProxyNodeStatusMap(idList)
    updateProxyNodeStatusMapByInterval()
  } else proxyList.value = []
})

const handleClick = (tab: any, event: Event) => {
  if (tab.paneName === 'collector') refreshCollectors()
  else if (tab.paneName === 'proxy') refreshProxies()
}

const initAgentNodeStatusMap = (idList: string[]) => {
  if (!idList || idList.length === 0) {
    return
  }
  for (let id of idList) {
    if (!agentNodeStatusMap.value[id]) {
      agentNodeStatusMap.value[id] = {
        status: 'unknown',
        isErr: false,
        err: '',
        loading: false,
      }
    }
  }
}
const updateAgentNodeStatusMapByInterval = () => {
  if (agentTimer.value) {
    clearInterval(agentTimer.value)
  }
  agentTimer.value = setInterval(updateAgentNodeStatusMap(), 30000)
}
const updateAgentNodeStatusMap = () => {
  ogRequest
    .get(`/observability/v1/exporters/status`)
    .then((res) => {
      if (res) {
        for (let node of res) {
          if (!agentNodeStatusMap.value[node.id]) {
            agentNodeStatusMap.value[node.id] = {
              status: node.status,
              isErr: false,
              err: '',
              loading: false,
            }
          } else {
            agentNodeStatusMap.value[node.id].status = node.status
          }
        }
      }
    })
    .catch(() => {})
}
const startAgent = (id: string) => {
  if (!id) {
    return
  }
  agentNodeStatusMap.value[id].loading = true
  agentNodeStatusMap.value[id].status = 'starting'
  ogRequest
    .post(`/observability/v1/exporter/start?id=${id}`, null, {}, { notTip: true })
    .then((res) => {
      agentNodeStatusMap.value[id].loading = false
      agentNodeStatusMap.value[id].isErr = false
      agentNodeStatusMap.value[id].err = ''
      updateAgentNodeStatusMapByInterval()
    })
    .catch((msg) => {
      agentNodeStatusMap.value[id].loading = false
      agentNodeStatusMap.value[id].isErr = true
      agentNodeStatusMap.value[id].err = msg
      updateAgentNodeStatusMapByInterval()
    })
}
const stopAgent = (id: string) => {
  if (!id) {
    return
  }
  agentNodeStatusMap.value[id].loading = true
  agentNodeStatusMap.value[id].status = 'stopping'
  ogRequest
    .post(`/observability/v1/exporter/stop?id=${id}`, null, {}, { notTip: true })
    .then((res) => {
      agentNodeStatusMap.value[id].loading = false
      agentNodeStatusMap.value[id].isErr = false
      agentNodeStatusMap.value[id].err = ''
      updateAgentNodeStatusMapByInterval()
    })
    .catch((msg) => {
      agentNodeStatusMap.value[id].loading = false
      agentNodeStatusMap.value[id].isErr = true
      agentNodeStatusMap.value[id].err = msg
      updateAgentNodeStatusMapByInterval()
    })
}

const initProxyNodeStatusMap = (idList: string[]) => {
  if (!idList || idList.length === 0) {
    return
  }
  for (let id of idList) {
    if (!proxyNodeStatusMap.value[id]) {
      proxyNodeStatusMap.value[id] = {
        status: 'unknown',
        isErr: false,
        err: '',
        loading: false,
      }
    }
  }
}
const updateProxyNodeStatusMapByInterval = () => {
  if (proxyTimer.value) {
    clearInterval(proxyTimer.value)
  }
  proxyTimer.value = setInterval(updateProxyNodeStatusMap(), 30000)
}
const updateProxyNodeStatusMap = () => {
  ogRequest
    .get(`/observability/v1/prometheus/status`)
    .then((res) => {
      if (res) {
        for (let node of res) {
          if (!proxyNodeStatusMap.value[node.id]) {
            proxyNodeStatusMap.value[node.id] = {
              status: node.status,
              isErr: false,
              err: '',
              loading: false,
            }
          } else {
            proxyNodeStatusMap.value[node.id].status = node.status
          }
        }
      }
    })
    .catch(() => {})
}
const startProxy = (id: string) => {
  if (!id) {
    return
  }
  proxyNodeStatusMap.value[id].loading = true
  proxyNodeStatusMap.value[id].status = 'starting'
  ogRequest
    .post(`/observability/v1/prometheus/start?id=${id}`, null, {}, { notTip: true })
    .then((res) => {
      proxyNodeStatusMap.value[id].loading = false
      proxyNodeStatusMap.value[id].isErr = false
      proxyNodeStatusMap.value[id].err = ''
      updateProxyNodeStatusMapByInterval()
    })
    .catch((msg) => {
      proxyNodeStatusMap.value[id].loading = false
      proxyNodeStatusMap.value[id].isErr = true
      proxyNodeStatusMap.value[id].err = msg
      updateProxyNodeStatusMapByInterval()
    })
}
const stopProxy = (id: string) => {
  if (!id) {
    return
  }
  proxyNodeStatusMap.value[id].loading = true
  proxyNodeStatusMap.value[id].status = 'stopping'
  ogRequest
    .post(`/observability/v1/prometheus/stop?id=${id}`, null, {}, { notTip: true })
    .then((res) => {
      proxyNodeStatusMap.value[id].loading = false
      proxyNodeStatusMap.value[id].isErr = false
      proxyNodeStatusMap.value[id].err = ''
      updateProxyNodeStatusMapByInterval()
    })
    .catch((msg) => {
      proxyNodeStatusMap.value[id].loading = false
      proxyNodeStatusMap.value[id].isErr = true
      proxyNodeStatusMap.value[id].err = msg
      updateProxyNodeStatusMapByInterval()
    })
}
</script>

<style scoped lang="scss">
@import '../../../assets/style/style1.scss';

.el-tree-node {
  border: 1px solid red !important;
}
.custom-tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
  flex-grow: 0;
  overflow: hidden;
  width: 100%;
  &.server {
    padding-left: 10px;
    margin-right: 10px;
    height: 24px;
    &:hover {
      background-color: #f5f7fa;
    }
    .item {
      .btns {
        margin-right: 8px;
      }
    }
  }

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
      //         NORMAL("normal"),
      // UNKNOWN("unknown"),
      // STARTING("starting"),
      // STOPPING("stopping"),
      // MANUAL_STOP("manualStop"),
      // ERROR_THREAD_NOT_EXISTS("errorThreadNotExists"),
      // ERROR_PROGRAM_UNHEALTHY("errorProgramUnhealthy");
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
  margin-top: 5px;
  animation: spin 1s infinite linear;
}
</style>
