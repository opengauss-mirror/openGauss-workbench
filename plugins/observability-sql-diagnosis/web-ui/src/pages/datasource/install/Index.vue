<template>
    <div class="top-sql">
        <div class="tab-wrapper-container">
            <div class="search-form-multirow">
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
                        <el-tree :data="collectorList" :props="collectorProps" #default="{ node }">
                            <span class="custom-tree-node show-hide">
                                <span>{{ node.label }}</span>
                                <span style="display: none" v-if="node.isLeaf">
                                    <el-link type="primary" @click="showUninstallAgent(node)">{{ $t('install.uninstall') }}</el-link>
                                </span>
                            </span>
                        </el-tree>
                    </el-tab-pane>
                </el-tabs>
            </div>
        </div>
        <InstallAgent v-if="installCollectorShown" :show="installCollectorShown" @changeModal="changeModalInstallCollector" />
        <UninstallAgent :node="uninstallAgentNode" v-if="uninstallAgentShown" :show="uninstallAgentShown" @changeModal="changeModalUninstallAgent" @installed="agentUninstalled()" />

        <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
    </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import ogRequest from '../../../request'
import { useI18n } from 'vue-i18n'
import InstallAgent from './installAgent.vue'
import UninstallAgent from './uninstallAgent.vue'

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
const agentUninstalled = (code: number) => {
    activeName.value = 'agent'
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
        for (let index = 0; index < collectorList.value.length; index++) {
            const element = collectorList.value[index]
            element.label = element.clusterId
            for (let index2 = 0; index2 < element.clusterNodes.length; index2++) {
                const node = element.clusterNodes[index2]
                node.label = node.privateIp + '(' + node.publicIp + ')'
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
</script>

<style scoped lang="scss">
@import '../../../assets/style/style1.scss';

.custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
}
.show-hide:hover :nth-child(2) {
    display: inline-block !important;
}
</style>
