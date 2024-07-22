<template>
  <div class="tab-wrapper">
    <el-container>
      <el-aside :width="isCollapse ? '0px' : '310px'">
        <div style="height: 13px"></div>
        <Install ref="installRef" style="margin-right: 10px" @isUninstallAgent="isUninstallAgent" @afterInstallAgent="afterInstallAgent"/>
      </el-aside>
      <el-main style="position: relative; padding-top: 0px" class="padding-fix">
        <div class="page-header" style="padding-left: 20px">
          <div class="icon"></div>
          <div class="title">{{ $t('instanceMonitor.instanceMonitor') }}</div>
          <div class="seperator"></div>
          <div class="cluster-title">{{ $t('instanceMonitor.clusterTitle') }}</div>

          <el-cascader v-model="clusterNodeId" :options="clusterList" style="width: 280px"
           :class="{'cascader-err': selectInstanceTip}" @visible-change="updateClusterList"/>
          <span ref="infoHollow"><svg-icon v-if="clusterNodeId" class="info-hollow" name="info-hollow" style="margin-left: 4px" @click="showInfo" /></span>
          <!-- <span class="msg-err" v-if="selectInstanceTip">{{selectInstanceTip}}</span> -->
          <div style="position: fixed;z-index: 9999" :style='{"left": infoLeft}' v-if="visible">
            <div class="instance-info">
              <div class="title-row">
                <div class="title">{{ $t('instanceMonitor.nodeInfo.instanceInfo') }}</div>
                <svg-icon class="close" name="close" style="margin-left: 4px" @click="visible = false" />
              </div>
              <div class="text" v-loading="loading">
                <div>{{ $t('instanceMonitor.nodeInfo.databaseVersion') }}{{ nodeInfoData?.version }}</div>
                <div>
                  {{ $t('instanceMonitor.nodeInfo.databaseStartTime')
                  }}{{ moment(nodeInfoData?.time).format('YYYY-MM-DD HH:mm:ss') as string }}
                </div>
                <div>{{ $t('instanceMonitor.nodeInfo.databaseDataDirectory') }}{{ nodeInfoData?.dbDataPath }}</div>
                <div>{{ $t('instanceMonitor.nodeInfo.databaseLogDirectory') }}{{ nodeInfoData?.dbLogPath }}</div>
                <div>
                  {{ $t('instanceMonitor.nodeInfo.enableArchiving')
                  }}{{
                    nodeInfoData?.archiveMode === 'on'
                      ? $t('instanceMonitor.nodeInfo.yes')
                      : $t('instanceMonitor.nodeInfo.no')
                  }}
                </div>
                <div>{{ $t('instanceMonitor.nodeInfo.operatingSystemVersion') }}{{ nodeInfoData?.osVersion }}</div>
                <div>{{ $t('instanceMonitor.nodeInfo.serverCPUManufacturer') }}{{ nodeInfoData?.CPUmanufacturer }}</div>
                <div>{{ $t('instanceMonitor.nodeInfo.serverCPUModel') }}{{ nodeInfoData?.CPUmodel }}</div>
                <div>
                  {{ $t('instanceMonitor.nodeInfo.serverCPUCoreCount') }}{{ nodeInfoData?.CPUcores
                  }}{{ $t('instanceMonitor.nodeInfo.cores') }}
                </div>
                <div>{{ $t('instanceMonitor.nodeInfo.totalMemorySize') }}{{ nodeInfoData?.TotalMemory }}</div>
              </div>
            </div>
          </div>
        </div>
        <div style="position: absolute; left: 0px; top: 5px; z-index: 9999" @click="toggleCollapse">
          <el-icon v-if="!isCollapse" size="20px"><Fold /></el-icon>
          <el-icon v-if="isCollapse" size="20px"><Expand /></el-icon>
        </div>
        <el-tabs v-model="dashboardTabKey" class="index-tabs">
          <el-tab-pane class="min-height" :label="$t('instanceMonitor.index')" :name="tabKeys.Home">
            <performance-load
              ref="performanceLoadRef"
              @goto="goto"
              :tabId="tabId"
              v-if="tabKeyLoaded.indexOf(tabKeys.Home) >= 0 || dashboardTabKey === tabKeys.Home"
              :nodeVersion="nodeVersion"
            />
          </el-tab-pane>
          <el-tab-pane
            class="min-height"
            :label="$t('instanceMonitor.resourceMonitor')"
            :name="tabKeys.ResourceMonitor"
          >
            <resource-monitor
              ref="refResourceMonitor"
              @goto="goto"
              @changeCluster="toChangeCluster"
              :tabId="tabId"
              v-if="tabKeyLoaded.indexOf(tabKeys.ResourceMonitor) >= 0 || dashboardTabKey === tabKeys.ResourceMonitor"
            />
          </el-tab-pane>
          <el-tab-pane
            class="min-height"
            :label="$t('instanceIndex.instanceMetrics')"
            :name="tabKeys.InstanceMonitorInstance"
          >
            <InstanceMetrics
              ref="refInstanceMonitorInstance"
              @goto="goto"
              :tabId="tabId"
              v-if="
                tabKeyLoaded.indexOf(tabKeys.InstanceMonitorInstance) >= 0 ||
                dashboardTabKey === tabKeys.InstanceMonitorInstance
              "
            />
          </el-tab-pane>
          <el-tab-pane class="min-height" label="TOP SQL" :name="tabKeys.InstanceMonitorTOPSQL">
            <TOPSQL
              ref="refInstanceMonitorTOPSQL"
              @goto="goto"
              :tabId="tabId"
              v-if="
                tabKeyLoaded.indexOf(tabKeys.InstanceMonitorTOPSQL) >= 0 ||
                dashboardTabKey === tabKeys.InstanceMonitorTOPSQL
              "
            />
          </el-tab-pane>
          <el-tab-pane class="min-height" :label="$t('dashboard.wdrReports.tabName')" :name="tabKeys.WDR">
            <wdr
              :tabId="tabId"
              @goto="goto"
              ref="wdrComponent"
              v-if="tabKeyLoaded.indexOf(tabKeys.WDR) >= 0 || dashboardTabKey === tabKeys.WDR"
              :instanceId="instanceId"
            />
          </el-tab-pane>
          <el-tab-pane class="min-height" :label="'ASP'" :name="tabKeys.ASP">
            <asp
              :tabId="tabId"
              @goto="goto"
              ref="aspComponent"
              v-if="tabKeyLoaded.indexOf(tabKeys.ASP) >= 0 || dashboardTabKey === tabKeys.ASP"
              :instanceId="instanceId"
            />
          </el-tab-pane>
          <el-tab-pane class="min-height" :label="$t('dashboard.systemConfig.tabName')" :name="tabKeys.SystemConfig">
            <systemConfiguration
              :tabId="tabId"
              @goto="goto"
              ref="paramConfigComponent"
              v-if="tabKeyLoaded.indexOf(tabKeys.SystemConfig) >= 0 || dashboardTabKey === tabKeys.SystemConfig"
              :instanceId="instanceId"
            />
          </el-tab-pane>
          <el-tab-pane class="min-height" :label="$t('collectConfig.tabName')" :name="tabKeys.CollectConfig">
            <CollectionConfig
              :tabId="tabId"
              @goto="goto"
              ref="collectConfigComponent"
              v-if="tabKeyLoaded.indexOf(tabKeys.CollectConfig) >= 0 || dashboardTabKey === tabKeys.CollectConfig"
              :instanceId="instanceId"
            />
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
    <div class="el-message" v-if="isShowMsg" style="top: 40px; z-index: 2003;">
      <i class="el-icon el-message__icon" style="color: #E6A23C" v-if="isShowWarn"><WarningFilled /></i>
      <i class="el-icon el-message__icon" style="color: #F56C6C" v-if="isShowErr"><CircleCloseFilled /></i>
      <p class="el-message__content">{{selectInstanceTip}}</p>
      <i class="el-icon msg-closeBtn" v-if="isShowMsgClose" @click="isShowMsg = false"><Close /></i>
      <e-link class="msg-btn" v-if="isShowMsgBtn" style="color: #F56C6C" @click="msgBtnClick">{{msgBtn}}</e-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Fold, Expand, WarningFilled, Close, CircleCloseFilled } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useMonitorStore } from '@/store/monitor'
import PerformanceLoad from '@/pages/dashboardV2/performanceLoad/Index.vue'
import ResourceMonitor from '@/pages/dashboardV2/resourceMonitor/Index.vue'
import InstanceMetrics from '@/pages/dashboardV2/instanceMonitor/instanceMetrics/Index.vue'
import TOPSQL from '@/pages/dashboardV2/instanceMonitor/topSQL/Index.vue'
import Wdr from '@/pages/dashboardV2/wdr/Index.vue'
import Asp from '@/pages/dashboardV2/asp/Index.vue'
import ogRequest from '@/request'
import { useRequest } from 'vue-request'
import Install from '@/pages/dashboard/install/Index.vue'
import SystemConfiguration from '@/pages/dashboardV2/systemConfiguration/Index.vue'
import CollectionConfig from '@/pages/dashboardV2/collectConfig/Index.vue'
import { tabKeys } from '@/pages/dashboardV2/common'
import { uuid } from '@/shared'
import { getNodeInfo, isExistAgentForInstance } from '@/api/observability'
import { ElMessage } from 'element-plus'
import moment from 'moment'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

type Res =
  | [
      {
        [propName: string]: string | number
      }
    ]
  | undefined
const installRef = ref(null)
const clusterNodeId = ref()
const selectInstanceTip=ref<string>('')
const clusterList = ref<Array<any>>([])
const nodeVersion = ref<string>('')
const lastNodeId = ref<string>('')
const wdrComponent = ref<InstanceType<typeof Wdr>>()
const paramConfigComponent = ref(null)
const performanceLoadRef = ref<InstanceType<typeof PerformanceLoad>>()
const refResourceMonitor = ref<InstanceType<typeof ResourceMonitor>>()
const refInstanceMonitorTOPSQL = ref<InstanceType<typeof TOPSQL>>()
const refInstanceMonitorInstance = ref<InstanceType<typeof InstanceMetrics>>()
const dashboardTabKey = ref<string>('')
const tabId = uuid()
const { instanceId } = storeToRefs(useMonitorStore(tabId))

const router = useRouter()
const tabKeyLoaded = ref<Array<string>>([])
const infoHollow = ref(null)
const visible = ref(false)
const infoLeft = ref<string>('')
const elMsg = ref()
const isShowMsg = ref<boolean>(false)
const isShowWarn = ref<boolean>(false)
const isShowErr = ref<boolean>(false)
const isShowMsgClose = ref<boolean>(false)
const isShowMsgBtn = ref<boolean>(false)
const msgBtn = ref<string>('')
const timer = ref<any>(null)
onMounted(() => {
  dashboardTabKey.value = tabKeys.Home
  if (!clusterNodeId.value) {
    selectInstanceTip.value = t('instanceMonitor.selectInstance')
    isShowMsg.value = true
    isShowWarn.value = true
    isShowErr.value = false
    isShowMsgClose.value = true
    isShowMsgBtn.value = false
  }
})
const toChangeCluster = (publicIp: string, port: string) => {
  for (let p1 = 0; p1 < clusterList.value.length; p1++) {
    const clusterTemp = clusterList.value[p1]
    for (let p2 = 0; p2 < clusterTemp.children.length; p2++) {
      const node = clusterTemp.children[p2]
      if (node.obj.publicIp === publicIp && node.obj.dbPort.toString() === port.toString()) {
        clusterNodeId.value = [clusterTemp.value, node.value]
        return
      }
    }
  }
}
const toChangeClusterByNodeId = (nodeId: string) => {
  for (let p1 = 0; p1 < clusterList.value.length; p1++) {
    const clusterTemp = clusterList.value[p1]
    for (let p2 = 0; p2 < clusterTemp.children.length; p2++) {
      const node = clusterTemp.children[p2]
      if (node.obj.nodeId === nodeId) {
        clusterNodeId.value = [clusterTemp.value, node.value]
        return
      }
    }
  }
}

// tab render only once
watch(dashboardTabKey, (v) => {
  if (tabKeyLoaded.value.indexOf(v) < 0) tabKeyLoaded.value.push(v)
  useMonitorStore(tabId).updateTabNow(dashboardTabKey.value)
})

// get cluster data
const { data: opsClusterData, run: getOpsClusterDatas } = useRequest(() => ogRequest.get('/observability/v1/topsql/cluster'), { manual: true })
watch(opsClusterData, (res: Res) => {
  if (res && Object.keys(res).length) {
    clusterList.value = treeTransform(res)

    nextTick(() => {
      let paramsId = router.currentRoute.value.query.nodeId as string
      let nodeId = window.$wujie?.props.data.nodeId as string
      if (nodeId) toChangeClusterByNodeId(nodeId);
      else if (paramsId) toChangeClusterByNodeId(paramsId); 
    })
  }
})

// cluster changed
watch(clusterNodeId, (res, old) => {
  selectInstanceTip.value = ''
  visible.value = false
  // getInstanceId
  let curInstanceId = instanceId.value
  if (typeof res === 'string') {
    curInstanceId = res
    isShowMsg.value = false
  } else if (Array.isArray(res) && res.length > 0) {
    curInstanceId = res[res.length - 1]
    isShowMsg.value = false
  } else {
    selectInstanceTip.value = t('instanceMonitor.selectInstance')
    isShowMsg.value = true
    isShowWarn.value = true
    isShowErr.value = false
    isShowMsgClose.value = true
    isShowMsgBtn.value = false
  }
  if (timer.value) {
    clearTimeout(timer.value)
  }

  // store new InstanceId to a temp
  lastNodeId.value = curInstanceId
  // set instanceId value
  instanceId.value = curInstanceId

  // find clusterId
  let clusterId = ''
  let obj = {}
  for (let p1 = 0; p1 < clusterList.value.length; p1++) {
    const clusterTemp = clusterList.value[p1]
    for (let p2 = 0; p2 < clusterTemp.children.length; p2++) {
      const node = clusterTemp.children[p2]
      if (node.value === curInstanceId) {
        clusterId = clusterTemp.value
        obj = node.obj
        break
      }
    }
  }
  useMonitorStore(tabId).updateInstanceAndClusterId(curInstanceId, clusterId, obj)
  checkAgentInstanceRel()
})

const isCollapse = ref(true)
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
  if (visible.value) {
    nextTick(() => {
      let position = infoHollow.value.getBoundingClientRect()
      infoLeft.value = (position.left + position.width ) + 'px'
    })
  }
}

const goto = (key: string, param: object) => {
  if (key === tabKeys.InstanceMonitorTOPSQL) {
    dashboardTabKey.value = tabKeys.InstanceMonitorTOPSQL
    nextTick(() => {
      refInstanceMonitorTOPSQL.value!.outsideGoto(key, param)
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
  } else if (key === tabKeys.WDR) {
    dashboardTabKey.value = tabKeys.WDR
    nextTick(() => {
      wdrComponent.value!.outsideGoto(param)
    })
  }
}

const treeTransform = (arr: any) => {
  let obj: any = []
  if (arr instanceof Array) {
    arr.forEach((item) => {
      // init current cluster node
      // if (item.nodeId && item.nodeId === instanceId.value) {
      //   clusterNodeId.value = instanceId.value
      // }
      obj.push({
        label: item.clusterId
          ? item.clusterId
          : (item.azName ? item.azName + '_' : '') +
            item.publicIp +
            ':' +
            item.dbPort +
            (item.clusterRole ? '(' + item.clusterRole + ')' : ''),
        value: item.clusterId ? item.clusterId : item.nodeId,
        obj: item,
        children: treeTransform(item.clusterNodes),
      })
    })
  }
  return obj
}
const showInfo = () => {
  if (!clusterNodeId.value) return
  visible.value = !visible.value
  if (!visible.value) return
  let position = infoHollow.value.getBoundingClientRect()
  infoLeft.value = (position.left + position.width ) + 'px'
  loadNodeInfo(tabId)
}
const { data: nodeInfoData, run: loadNodeInfo, loading } = useRequest(getNodeInfo, { manual: true })
watch(nodeInfoData, () => {}, { deep: true })

const isUninstallAgent = () => {
  ElMessage({
      message: t('install.uninstallAlert'),
      type: 'warning',
    })
    isCollapse.value = false
}
const afterInstallAgent = () => {
  checkAgentInstanceRel()
}
const checkAgentInstanceRel = () => {
  if (!clusterNodeId.value) {
    return;
  }
  isExistAgentForInstance(tabId).then(res => {
    if (res === 0) {
      selectInstanceTip.value = ''
      if (timer.value) {
        clearTimeout(timer.value)
      }
    } else if (res === 1){
      selectInstanceTip.value = t('instanceMonitor.selectInstanceWithoutAgent')
      msgBtn.value = t('instanceMonitor.toInstall')
      isShowMsg.value = true
      isShowWarn.value = false
      isShowErr.value = true
      isShowMsgClose.value = false
      isShowMsgBtn.value = true
      if (timer.value) {
        clearTimeout(timer.value)
      }
    } else if (res === 2) {
      selectInstanceTip.value = t('instanceMonitor.selectInstanceInErrAgent')
      isShowMsg.value = true
      isShowWarn.value = false
      isShowErr.value = true
      isShowMsgClose.value = false
      isShowMsgBtn.value = false
      timer.value = setTimeout(() => {
        isShowMsg.value = false
      }, 5000)
    }
  })
}
const msgBtnClick = () => {
  isShowMsg.value = false
  isCollapse.value = false
  installRef.value.showInstallCollector(instanceId.value)
}
const updateClusterList = (isShow: boolean) => {
  if (isShow) {
    getOpsClusterDatas()
  }
}
</script>

<style scoped lang="scss">
// in DataKit,page has padding,this to fix the padding,to make main-container to border to be 16px
.padding-fix {
  padding-left: 0px;
  padding-right: 9px;
  padding-bottom: 0px;
}

:deep .cascader-err .el-input__wrapper {
  box-shadow: 0 0 1px 1px var(--el-color-danger) inset !important;
}

.msg-err {
    color: var(--el-color-danger);
    font-size: 12px;
    line-height: 1;
    margin-left: 4px;
  }

.min-height {
  min-height: 500px !important;
}

.instance-info {
  position: absolute;
  z-index: 900;
  top: -18px;
  left: 0px;
  width: 406px;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0px 8px 20px 0px rgba(0, 0, 0, 0.2);
  .title-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    width: 406px;
    height: 36px;
    flex-shrink: 0;
    border-radius: 2px 2px 0px 0px;
    border-bottom: 1px solid var(--unnamed, #d9d9d9);
    background: var(--background-color-2);
    .title {
      font-size: 14px;
      font-weight: 500;
      line-height: 24px;
      flex-grow: 1;
      text-align: left;
      padding-left: 24px;
    }
    .close {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
      margin-right: 10px;
    }
  }

  .text {
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 29px;
    padding: 12px 24px;
    background: var(--background-color-1);
  }
}
:deep .el-message {
  background-color: var(--background-color-1)
}
.el-message {
  .msg-closeBtn {
    color: var(--el-message-close-icon-color);
    font-size: var(--el-message-close-size);
    cursor: pointer;
    margin-left: 15px;
  }
  .msg-btn {
    font-size: 14px;
    cursor: pointer;
    margin-left: 15px;
  }
}
</style>
