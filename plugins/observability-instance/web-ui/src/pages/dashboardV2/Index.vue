<template>
    <div class="tab-wrapper">
        <el-container>
            <el-aside :width="isCollapse ? '0px' : '300px'">
                <div style="height: 13px"></div>
                <Install />
            </el-aside>
            <el-main style="position: relative; padding-top: 0px">
                <div class="page-header" style="padding-left: 16px">
                    <div class="icon"></div>
                    <div class="title">{{ $t('instanceMonitor.instanceMonitor') }}</div>
                    <div class="seperator"></div>
                    <div class="cluster-title">{{ $t('instanceMonitor.clusterTitle') }}</div>

                    <el-cascader v-model="clusterNodeId" :options="clusterList" />
                </div>
                <div style="position: absolute; left: 10px; top: 3px; z-index: 9999" @click="toggleCollapse">
                    <el-icon v-if="!isCollapse" size="20px"><Fold /></el-icon>
                    <el-icon v-if="isCollapse" size="20px"><Expand /></el-icon>
                </div>
                <el-tabs v-model="dashboardTabKey">
                    <el-tab-pane :label="$t('instanceMonitor.index')" :name="tabKeys.Home">
                        <performance-load
                            ref="performanceLoadRef"
                            @goto="goto"
                            :tabId="tabId"
                            v-if="tabKeyLoaded.indexOf(tabKeys.Home) >= 0 || dashboardTabKey === tabKeys.Home"
                            :nodeVersion="nodeVersion"
                        />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('instanceMonitor.resourceMonitor')" :name="tabKeys.ResourceMonitor">
                        <resource-monitor
                            ref="refResourceMonitor"
                            @goto="goto"
                            :tabId="tabId"
                            v-if="
                                tabKeyLoaded.indexOf(tabKeys.ResourceMonitor) >= 0 ||
                                dashboardTabKey === tabKeys.ResourceMonitor
                            "
                        />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('instanceMonitor.instanceMonitor')" :name="tabKeys.InstanceMonitor">
                        <instance-monitor
                            ref="refInstanceMonitor"
                            @goto="goto"
                            :tabId="tabId"
                            v-if="
                                tabKeyLoaded.indexOf(tabKeys.InstanceMonitor) >= 0 ||
                                dashboardTabKey === tabKeys.InstanceMonitor
                            "
                        />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('dashboard.wdrReports.tabName')" :name="tabKeys.WDR">
                        <wdr
                            :tabId="tabId"
                            @goto="goto"
                            ref="wdrComponent"
                            v-if="tabKeyLoaded.indexOf(tabKeys.WDR) >= 0 || dashboardTabKey === tabKeys.WDR"
                            :instanceId="instanceId"
                        />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('dashboard.systemConfig.tabName')" :name="tabKeys.SystemConfig">
                        <systemConfiguration
                            :tabId="tabId"
                            @goto="goto"
                            ref="paramConfigComponent"
                            v-if="
                                tabKeyLoaded.indexOf(tabKeys.SystemConfig) >= 0 ||
                                dashboardTabKey === tabKeys.SystemConfig
                            "
                            :instanceId="instanceId"
                        />
                    </el-tab-pane>
                </el-tabs>
            </el-main>
        </el-container>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Fold, Expand } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useMonitorStore } from '@/store/monitor'
import PerformanceLoad from '@/pages/dashboardV2/performanceLoad/Index.vue'
import ResourceMonitor from '@/pages/dashboardV2/resourceMonitor/Index.vue'
import InstanceMonitor from '@/pages/dashboardV2/instanceMonitor/Index.vue'
import Wdr from '@/pages/dashboardV2/wdr/Index.vue'
import ogRequest from '@/request'
import { useRequest } from 'vue-request'
import Install from '@/pages/dashboard/install/Index.vue'
import SystemConfiguration from '@/pages/dashboardV2/systemConfiguration/Index.vue'
import { tabKeys } from '@/pages/dashboardV2/common'
import { uuid } from '@/shared'

type Res =
    | [
          {
              [propName: string]: string | number
          }
      ]
    | undefined

const clusterNodeId = ref()
const clusterList = ref<Array<any>>([])
const nodeVersion = ref<string>('')
const lastNodeId = ref<string>('')
const wdrComponent = ref(null)
const paramConfigComponent = ref(null)
const performanceLoadRef = ref<InstanceType<typeof PerformanceLoad>>()
const refResourceMonitor = ref<InstanceType<typeof ResourceMonitor>>()
const refInstanceMonitor = ref<InstanceType<typeof InstanceMonitor>>()
const dashboardTabKey = ref<string>('')
const tabId = uuid()
const { instanceId } = storeToRefs(useMonitorStore(tabId))

const tabKeyLoaded = ref<Array<string>>([])

onMounted(() => {
    dashboardTabKey.value = tabKeys.Home
})
// tab render only once
watch(dashboardTabKey, (v) => {
    if (tabKeyLoaded.value.indexOf(v) < 0) tabKeyLoaded.value.push(v)
    useMonitorStore(tabId).updateTabNow(dashboardTabKey.value)
})

// get cluster data
const { data: opsClusterData } = useRequest(() => ogRequest.get('/observability/v1/topsql/cluster'), { manual: false })
watch(opsClusterData, (res: Res) => {
    if (res && Object.keys(res).length) {
        clusterList.value = treeTransform(res)
    }
})

// cluster changed
watch(clusterNodeId, (res) => {
    // getInstanceId
    let curInstanceId = instanceId.value
    if (typeof res === 'string') {
        curInstanceId = res
    } else if (Array.isArray(res) && res.length > 0) {
        curInstanceId = res[res.length - 1]
    }

    // store new InstanceId to a temp
    lastNodeId.value = curInstanceId
    // set instanceId value
    instanceId.value = curInstanceId

    // find clusterId
    let clusterId = ''
    for (let p1 = 0; p1 < clusterList.value.length; p1++) {
        const clusterTemp = clusterList.value[p1]
        for (let p2 = 0; p2 < clusterTemp.children.length; p2++) {
            const node = clusterTemp.children[p2]
            if (node.value === curInstanceId) {
                clusterId = clusterTemp.value
                break
            }
        }
    }
    useMonitorStore(tabId).updateInstanceAndClusterId(curInstanceId, clusterId)
})

const isCollapse = ref(true)
const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value
}

const goto = (key: string, param: object) => {
    console.log('DEBUG: dashboard key', key)
    if (key === tabKeys.InstanceMonitorTOPSQL) {
        dashboardTabKey.value = tabKeys.InstanceMonitor
        nextTick(() => {
            refInstanceMonitor.value!.outsideGoto(key, param)
        })
    } else if (
        key === tabKeys.ResourceMonitorCPU ||
        key === tabKeys.ResourceMonitorIO ||
        key === tabKeys.ResourceMonitorMemory ||
        key === tabKeys.ResourceMonitorNetwork
    ) {
        dashboardTabKey.value = tabKeys.ResourceMonitor
        nextTick(() => {
            refResourceMonitor.value!.outsideGoto(key)
        })
    }
}

const treeTransform = (arr: any) => {
    let obj: any = []
    if (arr instanceof Array) {
        arr.forEach((item) => {
            // init current cluster node
            if (item.nodeId && item.nodeId === instanceId.value) {
                clusterNodeId.value = instanceId.value
            }
            obj.push({
                label: item.clusterId
                    ? item.clusterId
                    : (item.azName ? item.azName + '_' : '') +
                      item.publicIp +
                      ':' +
                      item.dbPort +
                      (item.clusterRole ? '(' + item.clusterRole + ')' : ''),
                value: item.clusterId ? item.clusterId : item.nodeId,
                children: treeTransform(item.clusterNodes),
            })
        })
    }
    return obj
}
</script>
