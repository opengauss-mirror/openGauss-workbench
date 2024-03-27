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
                        <el-tree :indent="0" :data="collectorList" :props="collectorProps" #default="{ node, data }">
                            <div class="custom-tree-node show-hide">
                                <span v-if="data.id">
                                    <el-tooltip :content="getStatusText(agentNodeStatusMap[data.id].status)" placement="top" effect="light">
                                        <span class="state-row">
                                            <span class="state" :class="agentNodeStatusMap[data.id].status"></span>
                                        </span>
                                    </el-tooltip>
                                </span>
                                <span v-if="data.id">
                                    <span>
                                        <el-popover trigger="hover" v-if="!agentNodeStatusMap[data.id].loading && agentNodeStatusMap[data.id].isErr" placement="right" :width="400">
                                            <div>{{ agentNodeStatusMap[data.id].err }}</div>
                                            <template #reference>
                                                <el-icon style="margin-top: 5px" color="red"><WarningFilled /></el-icon>
                                            </template>
                                        </el-popover>
                                    </span>
                                </span>
                                <el-icon class="custom-icon" v-if="data.id && agentNodeStatusMap[data.id].loading"><Loading /></el-icon>
                                <div class="item">
                                    <div class="label">{{ node.label }}</div>
                                    <div class="btns-placer"></div>
                                    <div class="btns" v-if="node.isLeaf">
                                        <el-link type="primary" v-if="showStarting(data, agentNodeStatusMap)" :disabled="agentNodeStatusMap[data.id].loading || !canStart(data, agentNodeStatusMap)" style="margin-right: 5px" @click="startAgent(data.id)">{{ t('install.start') }}</el-link>
                                        <el-link type="primary" v-if="!showStarting(data, agentNodeStatusMap)" :disabled="agentNodeStatusMap[data.id].loading || !canStop(data, agentNodeStatusMap)" style="margin-right: 5px" @click="stopAgent(data.id)">{{ t('install.stop') }}</el-link>
                                        <el-link type="primary" style="margin-right: 5px" @click="showUninstallAgent(node)">{{ $t('install.uninstall') }}</el-link>
                                    </div>
                                </div>
                            </div>
                        </el-tree>
                    </el-tab-pane>
                    <el-tab-pane :label="t('install.installedProxy')" name="proxy" v-loading="loadingProxies">
                        <div>
                            <span class="custom-tree-node show-hide server" v-for="data in proxyList" :key="data.id">
                                <span v-if="data.id">
                                    <el-tooltip :content="getStatusText(proxyNodeStatusMap[data.id].status)" placement="top" effect="light">
                                        <span class="state-row">
                                            <span class="state" :class="proxyNodeStatusMap[data.id].status"></span>
                                        </span>
                                    </el-tooltip>
                                </span>
                                <span v-if="data.id">
                                    <span>
                                        <el-popover trigger="hover" v-if="!proxyNodeStatusMap[data.id].loading && proxyNodeStatusMap[data.id].isErr" placement="right" :width="400">
                                            <div>{{ proxyNodeStatusMap[data.id].err }}</div>
                                            <template #reference>
                                                <el-icon style="margin-top: 5px" color="red"><WarningFilled /></el-icon>
                                            </template>
                                        </el-popover>
                                    </span>
                                </span>
                                <el-icon class="custom-icon" v-if="data.id && proxyNodeStatusMap[data.id].loading"><Loading /></el-icon>
                                <div class="item">
                                    <div class="label">{{ data.label }}</div>
                                    <div class="btns">
                                        <el-link type="primary" v-if="showStarting(data, proxyNodeStatusMap)" :disabled="proxyNodeStatusMap[data.id].loading || !canStart(data, proxyNodeStatusMap)" style="margin-right: 5px" @click="startProxy(data.id)">{{ t('install.start') }}</el-link>
                                        <el-link type="primary" v-if="!showStarting(data, proxyNodeStatusMap)" :disabled="proxyNodeStatusMap[data.id].loading || !canStop(data, proxyNodeStatusMap)" style="margin-right: 5px" @click="stopProxy(data.id)">{{ t('install.stop') }}</el-link>
                                        <el-link
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
                        </div>
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
import { Loading, WarningFilled } from '@element-plus/icons-vue';

const { t } = useI18n();

const activeName = ref('collector');
const errorInfo = ref<string | Error>();
const agentNodeStatusMap = ref<any>({});
const proxyNodeStatusMap = ref<any>({});
const agentTimer = ref<any>();
const proxyTimer = ref<any>();

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
const showStarting = (data: any, map: any) => {
    let showStartingStatus = ['unknown', 'starting', 'manualStop', 'errorThreadNotExists'];
    return showStartingStatus.some((str) => str === map[data.id].status);
};
const canStart = (data: any, map: any) => {
    let canStartStatus = ['unknown', 'manualStop', 'errorThreadNotExists'];
    return canStartStatus.some((str) => str === map[data.id].status);
};
const canStop = (data: any, map: any) => {
    let canStopStatus = ['normal', 'errorProgramUnhealthy'];
    return canStopStatus.some((str) => str === map[data.id].status);
};
const getStatusText = (status: string) => {
    return t('install.status.' + status);
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
        let idList = [];
        for (let index = 0; index < collectorList.value.length; index++) {
            const element = collectorList.value[index];
            element.label = element.clusterId;
            for (let index2 = 0; index2 < element.clusterNodes.length; index2++) {
                const node = element.clusterNodes[index2];
                // node.label = node.privateIp + '(' + node.publicIp + ')';
                node.label = (node.azName ? node.azName + '_' : '') + node.publicIp + ':' + node.dbPort + (node.clusterRole ? '(' + node.clusterRole + ')' : '');

                idList.push(node.id);
            }
        }
        initAgentNodeStatusMap(idList);
        updateAgentNodeStatusMapByInterval();
    } else collectorList.value = [];
});

// Proxy list
const defaultProps = {
    children: 'children',
    label: 'label',
    class: 'six',
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
        let idList = [];
        for (let index = 0; index < proxyList.value.length; index++) {
            const element = proxyList.value[index];
            // element.label = element.hostid;
            element.label = element.host ? element.host.name + '(' + element.host.publicIp + ':' + element.port + ')' : element.hostid;

            idList.push(element.id);
        }
        initProxyNodeStatusMap(idList);
        updateProxyNodeStatusMapByInterval();
    } else proxyList.value = [];
});

const handleClick = (tab: any, event: Event) => {
    if (tab.paneName === 'collector') refreshCollectors();
    else if (tab.paneName === 'proxy') refreshProxies();
};

const initAgentNodeStatusMap = (idList: string[]) => {
    if (!idList || idList.length === 0) {
        return;
    }
    for (let id of idList) {
        if (!agentNodeStatusMap.value[id]) {
            agentNodeStatusMap.value[id] = {
                status: 'grey',
                isErr: false,
                err: '',
                loading: false,
            };
        }
    }
};
const updateAgentNodeStatusMapByInterval = () => {
    if (agentTimer.value) {
        clearInterval(agentTimer.value);
    }
    agentTimer.value = setInterval(updateAgentNodeStatusMap(), 30000);
};
const updateAgentNodeStatusMap = () => {
    ogRequest
        .get(`/logSearch/v1/filebeat/status`)
        .then((res) => {
            if (res.code === 200 && res.data) {
                for (let node of res.data) {
                    if (!agentNodeStatusMap.value[node.id]) {
                        agentNodeStatusMap.value[node.id] = {
                            status: node.status,
                            isErr: false,
                            err: '',
                            loading: false,
                        };
                    } else {
                        agentNodeStatusMap.value[node.id].status = node.status;
                    }
                }
            }
        })
        .catch(() => {});
};
const startAgent = (id: string) => {
    if (!id) {
        return;
    }
    agentNodeStatusMap.value[id].status = 'starting'
    agentNodeStatusMap.value[id].loading = true;
    ogRequest
        .post(`/logSearch/v1/filebeat/start?id=${id}`, null, {}, { notTip: true })
        .then((res) => {
            agentNodeStatusMap.value[id].loading = false;
            agentNodeStatusMap.value[id].isErr = false;
            agentNodeStatusMap.value[id].err = '';
            updateAgentNodeStatusMapByInterval();
        })
        .catch((msg) => {
            agentNodeStatusMap.value[id].loading = false;
            agentNodeStatusMap.value[id].isErr = true;
            agentNodeStatusMap.value[id].err = msg;
            updateAgentNodeStatusMapByInterval();
        });
};
const stopAgent = (id: string) => {
    if (!id) {
        return;
    }
    agentNodeStatusMap.value[id].status = 'stopping'
    agentNodeStatusMap.value[id].loading = true;
    ogRequest
        .post(`/logSearch/v1/filebeat/stop?id=${id}`, null, {}, { notTip: true })
        .then((res) => {
            agentNodeStatusMap.value[id].loading = false;
            agentNodeStatusMap.value[id].isErr = false;
            agentNodeStatusMap.value[id].err = '';
            updateAgentNodeStatusMapByInterval();
        })
        .catch((msg) => {
            agentNodeStatusMap.value[id].loading = false;
            agentNodeStatusMap.value[id].isErr = true;
            agentNodeStatusMap.value[id].err = msg;
            updateAgentNodeStatusMapByInterval();
        });
};

const initProxyNodeStatusMap = (idList: string[]) => {
    if (!idList || idList.length === 0) {
        return;
    }
    for (let id of idList) {
        if (!proxyNodeStatusMap.value[id]) {
            proxyNodeStatusMap.value[id] = {
                status: 'grey',
                isErr: false,
                err: '',
                loading: false,
            };
        }
    }
};
const updateProxyNodeStatusMapByInterval = () => {
    if (proxyTimer.value) {
        clearInterval(proxyTimer.value);
    }
    proxyTimer.value = setInterval(updateProxyNodeStatusMap(), 30000);
};
const updateProxyNodeStatusMap = () => {
    ogRequest
        .get(`/logSearch/v1/elastic/status`)
        .then((res) => {
            if (res.code === 200 && res.data) {
                for (let node of res.data) {
                    if (!proxyNodeStatusMap.value[node.id]) {
                        proxyNodeStatusMap.value[node.id] = {
                            status: node.status,
                            isErr: false,
                            err: '',
                            loading: false,
                        };
                    } else {
                        proxyNodeStatusMap.value[node.id].status = node.status;
                    }
                }
            }
        })
        .catch(() => {});
};
const startProxy = (id: string) => {
    if (!id) {
        return;
    }
    proxyNodeStatusMap.value[id].status = 'starting'
    proxyNodeStatusMap.value[id].loading = true;
    ogRequest
        .post(`/logSearch/v1/elastic/start?id=${id}`, null, {}, { notTip: true })
        .then((res) => {
            proxyNodeStatusMap.value[id].loading = false;
            proxyNodeStatusMap.value[id].isErr = false;
            proxyNodeStatusMap.value[id].err = '';
            updateProxyNodeStatusMapByInterval();
        })
        .catch((msg) => {
            proxyNodeStatusMap.value[id].loading = false;
            proxyNodeStatusMap.value[id].isErr = true;
            proxyNodeStatusMap.value[id].err = msg;
            updateProxyNodeStatusMapByInterval();
        });
};
const stopProxy = (id: string) => {
    if (!id) {
        return;
    }
    proxyNodeStatusMap.value[id].status = 'stopping'
    proxyNodeStatusMap.value[id].loading = true;
    ogRequest
        .post(`/logSearch/v1/elastic/stop?id=${id}`, null, {}, { notTip: true })
        .then((res) => {
            proxyNodeStatusMap.value[id].loading = false;
            proxyNodeStatusMap.value[id].isErr = false;
            proxyNodeStatusMap.value[id].err = '';
            updateProxyNodeStatusMapByInterval();
        })
        .catch((msg) => {
            proxyNodeStatusMap.value[id].loading = false;
            proxyNodeStatusMap.value[id].isErr = true;
            proxyNodeStatusMap.value[id].err = msg;
            updateProxyNodeStatusMapByInterval();
        });
};
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
            position: absolute;
            display: none;
            padding-left: 5px;
            padding-right: 4px;
            right: 0;
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
    background-color: #F2F3F5;
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
