<script setup lang="ts">
import { Fold, Expand } from "@element-plus/icons-vue";
import { Refresh } from "@element-plus/icons-vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useMonitorStore } from "../../store/monitor";
import { useWindowStore } from "../../store/window";
import PerformanceLoad from "./performance_load/Index.vue";
import TopSql from "./top_sql/Index.vue";
import Wdr from "./wdr/Index.vue";
import { i18n } from "../../i18n";
import ogRequest from "../../request";
import { useRequest } from "vue-request";
import Install from "./install/Index.vue";
import SystemConfiguration from "./system_configuration/Index.vue";

const { t } = useI18n();

type Res =
    | [
          {
              [propName: string]: string | number;
          }
      ]
    | undefined;

const datePickerRef = ref<HTMLDivElement>();
const clusterNodeId = ref();
const clusterList = ref<Array<any>[]>([]);
const connectStatus = ref<boolean | undefined>(undefined);
const curServerInfoText = ref("");
const nodeVersion = ref<string>("");
const lastNodeId = ref<string>("");
const wdrComponent = ref(null);
const paramConfigComponent = ref(null);

const { serverInfoText } = storeToRefs(useWindowStore());
const { tab, filters, autoRefresh, rangeTime, instanceId } = storeToRefs(useMonitorStore());
// tab render only once
const tabLoaded = reactive([tab.value === 0, tab.value === 1, tab.value === 2, tab.value === 3]);
watch(tab, (v) => {
    if (!tabLoaded[v]) {
        tabLoaded[v] = true;
    }
    if (v === 0 || v === 1) {
        nextTick(() => {
            if (nodeIdChangedByOtherTabs.value !== "") {
                clusterNodeId.value = nodeIdChangedByOtherTabs.value;
                nodeIdChangedByOtherTabs.value = "";
            }
        });
    }
    if (v === 2) {
        nextTick(() => {
            wdrComponent.value.syncNodeId(lastNodeId.value);
        });
    }
    if (v === 3) {
        nextTick(() => {
            paramConfigComponent.value.syncNodeId(lastNodeId.value);
        });
    }
});
const isCollapse = ref(true);
const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value;
};

// nodeId sync
const nodeIdChangedByOtherTabs = ref<string>("");
const nodeIdChanged = (nodeId: any) => {
    nodeIdChangedByOtherTabs.value = nodeId;
    lastNodeId.value = nodeId;
};

const autoRefreshFn = () => {
    autoRefresh.value = !autoRefresh.value;
};

const { data: opsClusterData } = useRequest(() => ogRequest.get("/observability/v1/topsql/cluster"), { manual: false });

const { data: connectStatusData, run: runConnectStatus, loading: connectStatusLoadding } = useRequest((nodeId: String) => ogRequest.get(`/observability/v1/topsql/connect/${nodeId}`), { manual: true });

const treeTransform = (arr: any) => {
    let obj: any = [];
    if (arr instanceof Array) {
        arr.forEach((item) => {
            // init current cluster node
            if (item.nodeId && item.nodeId === instanceId.value) {
                clusterNodeId.value = instanceId.value;
            }
            obj.push({
                label: item.clusterId ? item.clusterId : item.azName + "_" + item.publicIp + "(" + item.nodeId + ")",
                value: item.clusterId ? item.clusterId : item.nodeId,
                children: treeTransform(item.clusterNodes),
            });
        });
    }
    return obj;
};

const showConnectStatus = (status: boolean | undefined) => {
    if (status === undefined) {
        return "";
    }
    return status ? t("dashboard.connectStatus.success") : t("dashboard.connectStatus.error");
};

const onDatePackerVisible = (v: boolean) => {
    if (!v) {
        const docu = document.getElementsByClassName("el-range-input");
        // @ts-ignore
        docu[0]?.blur();
    }
};

const getVersionByNodeId = (curNodeId: string) => {
    if (!Array.isArray(opsClusterData.value)) {
        return "";
    }
    for (let i = 0; i < opsClusterData.value.length; i += 1) {
        const version = opsClusterData.value[i].version || "";
        const children = opsClusterData.value[i].clusterNodes;
        if (!Array.isArray(children)) {
            continue;
        }
        for (let j = 0; j < children.length; j += 1) {
            if (children[j]?.nodeId === curNodeId) {
                return version.toLocaleUpperCase();
            }
        }
    }
    return "";
};

watch(opsClusterData, (res: Res) => {
    if (res && Object.keys(res).length) {
        clusterList.value = treeTransform(res);
    }
});

watch(clusterNodeId, (res) => {
    let curInstanceId = instanceId.value;
    if (typeof res === "string") {
        curInstanceId = res;
    } else if (Array.isArray(res) && res.length > 0) {
        curInstanceId = res[res.length - 1];
    }

    lastNodeId.value = curInstanceId;
    // get connection status
    runConnectStatus(curInstanceId);
    // set instanceId value
    instanceId.value = curInstanceId;
    useMonitorStore().instanceId = curInstanceId;
    // useMonitorStore().tab = 0;
    nodeVersion.value = getVersionByNodeId(curInstanceId);
});

watch(connectStatusData, (res) => {
    connectStatus.value = res;
});

watch(rangeTime, (r) => {
    if (r !== -1) {
        filters.value[tab.value].time = null;
    }
});

watch(serverInfoText, (val) => {
    if (typeof val === "string" && val !== "") {
        curServerInfoText.value = val;
    } else {
        curServerInfoText.value = "";
    }
});
</script>

<template>
    <div class="tab-wrapper" :key="clusterNodeId">
        <el-container>
            <el-aside :width="isCollapse ? '0px' : '300px'">
                <div style="height: 23px"></div>
                <Install />
            </el-aside>
            <el-main style="position: relative">
                <div>
                    <div style="position: absolute; left: 10px; top: 31px; z-index: 9999" @click="toggleCollapse">
                        <el-icon v-if="!isCollapse" size="20px"><Fold /></el-icon>
                        <el-icon v-if="isCollapse" size="20px"><Expand /></el-icon>
                    </div>
                </div>
                <el-tabs v-model="tab">
                    <div class="tab-wrapper-container" v-show="tab === 0 || tab === 1">
                        <div class="cluster-container">
                            <div class="cluster-container-title">{{ $t("datasource.cluterTitle") }}</div>
                            <el-cascader v-model="clusterNodeId" :options="clusterList" />
                            <div v-if="false" class="divider" />
                            <div class="cluster-info-loading" v-if="false && connectStatusLoadding" v-loading="connectStatusLoadding"></div>
                            <div class="cluster-info" v-if="false && connectStatus !== undefined && !connectStatusLoadding">
                                <span class="cluster-info-light" :style="{ backgroundColor: connectStatus ? 'green' : 'red' }" />
                                <span>{{ showConnectStatus(connectStatus) }}</span>
                            </div>
                            <div v-if="false && curServerInfoText !== ''" class="divider" />
                            <div v-if="false && curServerInfoText !== ''">{{ serverInfoText }}</div>
                        </div>
                        <div class="tab-wrapper-filter">
                            <span>{{ $t("app.autoRefresh") }}:</span>
                            <el-select v-model="filters[tab].refreshTime" style="width: 60px; margin: 0 4px">
                                <el-option :value="15" label="15s" />
                                <el-option :value="30" label="30s" />
                                <el-option :value="60" label="60s" />
                            </el-select>
                            <el-button type="primary" :icon="Refresh" style="padding: 8px" @click="autoRefreshFn" />
                            <div class="divider"></div>
                            <span>{{ $t("dashboard.range") }}:</span>
                            <el-select v-model="filters[tab].rangeTime" :style="{ width: i18n.global.locale.value === 'en' ? '115px' : '85px' }">
                                <el-option :value="1" :label="$t('dashboard.last1H')" />
                                <el-option :value="12" :label="$t('dashboard.last12H')" />
                                <el-option :value="24" :label="$t('dashboard.last1D')" />
                                <el-option :value="48" :label="$t('dashboard.last2D')" />
                                <el-option :value="168" :label="$t('dashboard.last7D')" />
                                <el-option :value="-1" :label="$t('app.custom')" />
                            </el-select>
                            <el-date-picker ref="datePickerRef" :disabled="filters[tab].rangeTime !== -1" type="datetimerange" v-model="filters[tab].time" :start-placeholder="$t('app.startDate')" :end-placeholder="$t('app.endDate')" :range-separator="$t('app.to')" @visible-change="onDatePackerVisible" />
                        </div>
                    </div>

                    <el-tab-pane :label="$t('dashboard.load')" :name="0">
                        <performance-load v-if="tabLoaded[0] || tab === 0" :nodeVersion="nodeVersion" />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('dashboard.top')" :name="1">
                        <top-sql v-if="tabLoaded[1] || tab === 1" :instanceId="instanceId" />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('dashboard.wdrReports.tabName')" :name="2">
                        <wdr @nodeIdChanged="nodeIdChanged" ref="wdrComponent" v-if="tabLoaded[2] || tab === 2" :instanceId="instanceId" />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('dashboard.systemConfig.tabName')" :name="3">
                        <system-configuration @nodeIdChanged="nodeIdChanged" ref="paramConfigComponent" v-if="tabLoaded[3] || tab === 3" :instanceId="instanceId" />
                    </el-tab-pane>
                </el-tabs>
            </el-main>
        </el-container>
    </div>
</template>

<style scoped lang="scss">
.cluster-container {
    height: 40px;
    // background-color: var(--el-bg-color-sub);
    padding: 0 16px;
    display: flex;
    align-items: center;

    &-title {
        font-size: 14px;
        margin-right: 10px;
    }

    :deep(.el-cascader) {
        width: 210px;
    }

    :deep(.el-input__wrapper) {
        border-radius: 5px;
        font-size: 12px;
        font-weight: 700;
    }
}

.cluster-info {
    height: inherit;
    display: flex;
    align-items: center;
    font-size: 12px;

    &-light {
        display: inline-block;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background-color: green;
        margin-right: 8px;
    }

    &-loading {
        width: 50px;
    }
}

.tab-wrapper {
    position: relative;

    &-container {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        justify-content: end;
        overflow: hidden;
        font-size: 12px;
    }

    &-filter {
        font-size: 12px;
        width: 600px;
        z-index: 10;
        padding-right: 16px;
        display: flex;
        align-items: center;
        padding: 0 10px;
        height: 40px;
        > div:not(:last-of-type),
        > span,
        > button {
            margin-right: 4px;
        }

        :deep(.el-button .el-icon svg) {
            color: var(--el-color-icon-refresh-color);
        }

        :deep(.el-button--small) {
            background-color: var(--el-color-button-small-bg) !important;
            border: 1px solid var(--el-bg-color-og-hover) !important;
            color: var(--el-bg-color-og-hover) !important;
            padding: 5px !important;
        }

        :deep(.el-select .el-input.is-focus .el-input__wrapper) {
            box-shadow: 0 0 0 1px var(--hw-primary-1, var(--hw-primary-1)) inset !important;
        }
        :deep(.el-select-dropdown__item.selected) {
            color: var(--el-color-tabbar-active) !important;
        }
        :deep(.el-range-editor.is-active) {
            box-shadow: 0 0 0 1px var(--hw-primary-1, var(--hw-primary-1)) inset !important;
        }
    }

    :deep(.el-range-input) {
        background-color: var(--el-bg-color);
    }

    :deep(.el-date-editor--datetimerange) {
        width: 100px;
        background-color: var(--el-bg-color);
    }
}
.divider {
    height: 24px;
    width: 1px;
    margin: 0 8px !important;
    background-color: var(--el-color-divider-border-color);
}
:deep(.el-tabs__header) {
    padding: 0 16px;
    // width: v-bind(tabHeaderW);
}
</style>
