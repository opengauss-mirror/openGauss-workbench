<template>
    <div class="top-sql">
        <div class="tab-wrapper-container">
            <div class="search-form-multirow" style="margin-bottom: 10px;">
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
                            <span class="custom-tree-node show-hide">
                                <span>{{ node.label }}</span>
                                <span style="display: none" v-if="node.isLeaf">
                                    <el-link type="primary" @click="showUninstallAgent(node)">{{ $t('install.uninstall') }}</el-link>
                                </span>
                            </span>
                        </el-tree>
                    </el-tab-pane>
                    <el-tab-pane :label="t('install.installedProxy')" name="proxy" v-loading="loadingProxies">
                        <el-tree :data="proxyList" :props="defaultProps" #default="{ node }">
                            <span class="custom-tree-node show-hide">
                                <span>{{ node.label }}</span>
                                <span style="display: none">
                                    <el-link type="primary" @click="showUninstallProxy(node)">{{ $t('install.uninstall') }}</el-link>
                                </span>
                            </span>
                        </el-tree>
                    </el-tab-pane>
                </el-tabs>
            </div>
        </div>
        <InstallAgent v-if="installCollectorShown" :show="installCollectorShown" @changeModal="changeModalInstallCollector" @installed="agentInstalled()" />
        <InstallProxy v-if="installProxyShown" :show="installProxyShown" @changeModal="changeModalInstallProxy" @installed="proxyInstalled()" />
        <UninstallProxy :node="uninstallProxyNode" v-if="uninstallProxyShown" :show="uninstallProxyShown" @changeModal="changeModalUninstallProxy" @installed="proxyUninstalled()" />
        <UninstallAgent :node="uninstallAgentNode" v-if="uninstallAgentShown" :show="uninstallAgentShown" @changeModal="changeModalUninstallAgent" @installed="agentUninstalled()" />

        <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
    </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request';
import ogRequest from '../../../request';
import { useI18n } from 'vue-i18n';
import InstallAgent from './installAgent.vue';
import InstallProxy from './installProxy.vue';
import UninstallAgent from './uninstallAgent.vue';
import UninstallProxy from './uninstallProxy.vue';

const { t } = useI18n();

const activeName = ref('collector');
const errorInfo = ref<string | Error>();

onMounted(() => {
    refreshCollectors();
});

// install collector
const installCollectorShown = ref(false);
const showInstallCollector = () => {
    installCollectorShown.value = true;
};
const changeModalInstallCollector = (val: boolean) => {
    installCollectorShown.value = val;
};
const agentInstalled = () => {
    activeName.value = 'collector';
    refreshCollectors();
};

// install proxy
const installProxyShown = ref(false);
const showInstallProxy = () => {
    installProxyShown.value = true;
};
const changeModalInstallProxy = (val: boolean) => {
    installProxyShown.value = val;
};
const proxyInstalled = () => {
    activeName.value = 'proxy';
    refreshProxies();
};

// uinstallAgent
const uninstallAgentShown = ref(false);
const uninstallAgentNode = ref<any>();
const showUninstallAgent = (node: any) => {
    uninstallAgentNode.value = node;
    uninstallAgentShown.value = true;
};
const changeModalUninstallAgent = (val: boolean) => {
    uninstallAgentShown.value = val;
};
const agentUninstalled = () => {
    activeName.value = 'collector';
    refreshCollectors();
};

// uinstallProxy
const uninstallProxyShown = ref(false);
const uninstallProxyNode = ref<any>();
const showUninstallProxy = (node: any) => {
    uninstallProxyNode.value = node;
    uninstallProxyShown.value = true;
};
const changeModalUninstallProxy = (val: boolean) => {
    uninstallProxyShown.value = val;
};
const proxyUninstalled = () => {
    activeName.value = 'proxy';
    refreshProxies();
};

// Collector list
const collectorList = ref<Array<any>>([]);
const collectorProps = {
    children: 'clusterNodes',
    label: 'label',
};
const {
    data: resCollectors,
    run: refreshCollectors,
    loading: loadingCollector,
} = useRequest(
    () => {
        return ogRequest
            .get('/observability/v1/environment/filebeat', {})
            .then(function (res) {
                return res;
            })
            .catch(function (res) {});
    },
    { manual: true }
);
watch(resCollectors, (res: any) => {
    if (res.length) {
        collectorList.value = res;
        for (let index = 0; index < collectorList.value.length; index++) {
            const element = collectorList.value[index];
            element.label = element.clusterId;
            for (let index2 = 0; index2 < element.clusterNodes.length; index2++) {
                const node = element.clusterNodes[index2];
                // node.label = node.privateIp + '(' + node.publicIp + ')';
                node.label = (node.azName ? node.azName + '_' : '') + node.publicIp + ':' + node.dbPort + (node.clusterRole ?  '(' + node.clusterRole + ')' : '');
            }
        }
    } else collectorList.value = [];
});

// Proxy list
const defaultProps = {
    children: 'children',
    label: 'label',
};
const proxyList = ref<Array<any>>([]);
const {
    data: res,
    run: refreshProxies,
    loading: loadingProxies,
} = useRequest(
    () => {
        return ogRequest
            .get('/observability/v1/environment/elasticsearch', {})
            .then(function (res) {
                return res;
            })
            .catch(function (res) {});
    },
    { manual: true }
);
watch(res, (res: any) => {
    if (res.length) {
        proxyList.value = res;
        for (let index = 0; index < proxyList.value.length; index++) {
            const element = proxyList.value[index];
            // element.label = element.hostid;
            element.label = element.host ? element.host.name + '(' + element.host.publicIp + ':' + element.port  + ')' : element.hostid;
        }
    } else proxyList.value = [];
});

const handleClick = (tab: any, event: Event) => {
    if (tab.paneName === 'collector') refreshCollectors();
    else if (tab.paneName === 'proxy') refreshProxies();
};
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
