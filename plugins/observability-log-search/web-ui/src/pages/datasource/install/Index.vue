<template>
    <div class="top-sql">
        <div class="tab-wrapper-container">
            <div class="search-form-multirow">
                <div class="row" style="justify-content: flex-start">
                    <div class="filter">
                        <el-button type="primary" @click="showInstallCollector">{{ $t("install.installAgent") }}</el-button>
                        <el-button type="primary" @click="showInstallProxy">{{ $t("install.installProxy") }}</el-button>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-container">
            <div class="table-wrapper">
                <el-tabs v-model="activeName" @tab-click="handleClick">
                    <el-tab-pane :label="t('install.installedAgent')" name="collector" v-loading="loadingCollector">
                        <el-tree :data="collectorList" :props="collectorProps" />
                    </el-tab-pane>
                    <el-tab-pane :label="t('install.installedProxy')" name="proxy" v-loading="loadingProxies">
                        <el-tree :data="proxyList" :props="defaultProps" />
                    </el-tab-pane>
                </el-tabs>
            </div>
        </div>
        <InstallAgent v-if="installCollectorShown" :show="installCollectorShown" @changeModal="changeModalInstallCollector" />
        <InstallProxy v-if="installProxyShown" :show="installProxyShown" @changeModal="changeModalInstallProxy" @installed="proxyInstalled()" />

        <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
    </div>
</template>

<script setup lang="ts">
import { useRequest } from "vue-request";
import ogRequest from "../../../request";
import { useI18n } from "vue-i18n";
import InstallAgent from "./installAgent.vue";
import InstallProxy from "./installProxy.vue";

const { t } = useI18n();

const activeName = ref("collector");
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

// install proxy
const installProxyShown = ref(false);
const showInstallProxy = () => {
    installProxyShown.value = true;
};
const changeModalInstallProxy = (val: boolean) => {
    installProxyShown.value = val;
};
const proxyInstalled = (code: number) => {
    activeName.value = "proxy";
    refreshProxies();
};

// Collector list
const collectorList = ref<Array<any>>([]);
const collectorProps = {
    children: "clusterNodes",
    label: "label",
};
const {
    data: resCollectors,
    run: refreshCollectors,
    loading: loadingCollector,
} = useRequest(
    () => {
        return ogRequest
            .get("/observability/v1/environment/filebeat", {})
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
                node.label = node.privateIp + "(" + node.publicIp + ")";
            }
        }
    } else collectorList.value = [];
});

// Proxy list
const defaultProps = {
    children: "children",
    label: "label",
};
const proxyList = ref<Array<any>>([]);
const {
    data: res,
    run: refreshProxies,
    loading: loadingProxies,
} = useRequest(
    () => {
        return ogRequest
            .get("/observability/v1/environment/elasticsearch", {})
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
            element.label = element.hostid;
        }
    } else proxyList.value = [];
});

const handleClick = (tab: any, event: Event) => {
    if (tab.paneName === "collector") refreshCollectors();
    else if (tab.paneName === "proxy") refreshProxies();
};
</script>

<style scoped lang="scss">
@import "../../../assets/style/style1.scss";
</style>
