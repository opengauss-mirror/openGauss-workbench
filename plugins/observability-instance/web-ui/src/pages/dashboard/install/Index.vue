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
            <el-tree :data="collectorList" :props="collectorProps" #default="{ node }">
              <div class="custom-tree-node show-hide">
                <svg-icon v-if="node.level === 1" class="icon" name="tree-folder" />
                <svg-icon v-if="node.level === 2" class="icon" name="tree-file" />
                <svg-icon v-if="node.level === 3" class="icon" name="tree-drive-file" />
                <div class="item">
                  <div class="label">{{ node.label }}</div>
                  <div class="btns-placer"></div>
                  <div class="btns" v-if="node.level === 1">
                    <el-link style="margin-right: 5px" type="primary" @click="showEditCollector(node)">{{
                      $t('app.edit')
                    }}</el-link>
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
            <el-tree :data="proxyList" :props="defaultProps" #default="{ node }">
              <span class="custom-tree-node show-hide">
                <div class="item">
                  <div class="label">{{ node.label }}</div>
                  <div class="btns-placer"></div>
                  <div class="btns">
                    <el-link type="primary" @click="showUninstallProxy(node)">{{ $t('install.uninstall') }}</el-link>
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
import { useI18n } from 'vue-i18n'
import InstallAgent from '@/pages/dashboard/install/installAgent.vue'
import InstallProxy from '@/pages/dashboard/install/installProxy.vue'
import UninstallAgent from '@/pages/dashboard/install/uninstallAgent.vue'
import UninstallProxy from '@/pages/dashboard/install/uninstallProxy.vue'

const { t } = useI18n()

const activeName = ref('collector')
const errorInfo = ref<string | Error>()

onMounted(() => {
  refreshCollectors()
})

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

// edit collector
const editCollectorShown = ref(false)
const editNode = ref({})
const deleteNodeId = ref('')
const showEditCollector = (node: any, delNodeId?: string) => {
  console.log('node', node)
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
const showInstallProxy = () => {
  installProxyShown.value = true
}
const changeModalInstallProxy = (val: boolean) => {
  installProxyShown.value = val
}
const proxyInstalled = () => {
  activeName.value = 'proxy'
  refreshProxies()
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
        return res
      })
      .catch(function (res) {})
  },
  { manual: true }
)
watch(resCollectors, (res: any) => {
  if (res.length) {
    collectorList.value = res
    for (let index = 0; index < collectorList.value.length; index++) {
      const element = collectorList.value[index]
      element.label = element.hostName + '(' + element.hostPublicIp + ':' + element.exporterPort + ')'
      element.children = element.clusters
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
  } else collectorList.value = []
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
    proxyList.value = res
    for (let index = 0; index < proxyList.value.length; index++) {
      const element = proxyList.value[index]
      // element.label = element.hostid;
      element.label = element.host
        ? element.host.name + '(' + element.host.publicIp + ':' + element.port + ')'
        : element.hostid
    }
  } else proxyList.value = []
})

const handleClick = (tab: any, event: Event) => {
  if (tab.paneName === 'collector') refreshCollectors()
  else if (tab.paneName === 'proxy') refreshProxies()
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
  .icon {
    flex-shrink: 0;
    width: 16px;
    height: 16px;
    margin-right: 4px;
  }
  .item {
    display: flex;
    align-items: center;
    flex-direction: row;
    overflow: hidden;

    .label {
      flex-shrink: 0;
      overflow: hidden;
    }
    .btns {
      align-items: center;
      display: none;
      padding-left: 6px;
      padding-right: 4px;
    }
    .btns-placer {
      width: 200px;
    }
  }
}
.show-hide:hover :nth-child(2) .btns {
  display: flex !important;
}
.show-hide:hover :nth-child(1) .btns {
  display: flex !important;
}
.show-hide:hover :nth-child(2) .label {
  flex-shrink: 1;
}
.show-hide:hover :nth-child(2) .btns-placer {
  display: none;
}
</style>
