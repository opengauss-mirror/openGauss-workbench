<template>
  <div class="tab-wrapper">
    <el-container>
      <el-main style="position: relative; padding-top: 0px" class="padding-fix">
        <div class="page-header" style="padding-left: 20px">
          <div class="icon"></div>
          <div class="title">{{ $t('clusterMonitor.clusterMonitor') }}</div>
          <div class="seperator"></div>
          <el-breadcrumb separator="/" style="flex-grow: 1">
            <el-breadcrumb-item>
              <div @click="goback">
                <a>{{ $t('clusterMonitor.clusterMonitor') }}</a>
              </div>
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{
              $t('clusterMonitor.detail.title').replace('temp', clusterRow.clusterId)
            }}</el-breadcrumb-item>
          </el-breadcrumb>

          <el-button
            class="refresh-button"
            type="primary"
            :icon="Refresh"
            style="padding: 8px"
            @click="loadClusterNodes(clusterRow.clusterId)"
          />
        </div>

        <div class="main-container">
          <my-card :title="$t('clusterMonitor.detail.info')" :bodyPadding="false" skipBodyHeight>
            <el-table
              :data="clusterNodesData"
              style="width: 100%"
              border
              :header-cell-class-name="
                () => {
                  return 'grid-header'
                }
              "
              @selection-change="selectionChange"
              v-loading="loadingNodes"
            >
              <el-table-column
                type="selection"
                prop="xxxx"
                :label="$t('clusterMonitor.detail.instance.legend')"
                align="center"
                width="40"
              />
              <el-table-column :label="$t('clusterMonitor.detail.instance.legend')" align="center" width="50">
                <template #default="scope">
                  <div style="width: 100%; display: flex; flex-direction: row; justify-content: center">
                    <div
                      style="width: 12px; height: 12px; border-radius: 2px"
                      :style="{ background: scope.row.color }"
                    ></div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="nodeName" :label="$t('clusterMonitor.detail.instance.nodeName')" />
              <el-table-column prop="localAddr" :label="$t('clusterMonitor.detail.instance.ipPort')" width="140" />
              <el-table-column prop="role" :label="$t('clusterMonitor.detail.instance.role')" width="60" />
              <el-table-column
                prop="nodeState.value"
                :label="$t('clusterMonitor.detail.instance.nodeStatus')"
                width="80"
              >
                <template #default="scope">
                  <div class="state-row">
                    <div class="state" :class="[scope.row.nodeState?.color?.toLowerCase()]"></div>
                    {{ scope.row.nodeState?.value }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="cmServerState.value" label="CM_server" width="90">
                <template #default="scope">
                  <div class="state-row">
                    <div class="state" :class="[scope.row.cmServerState?.color?.toLowerCase()]"></div>
                    {{ scope.row.cmServerState?.value }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="omMonitorState.value" label="OM_moniter" width="100">
                <template #default="scope">
                  <div class="state-row">
                    <div class="state" :class="[scope.row.omMonitorState?.color?.toLowerCase()]"></div>
                    {{ scope.row.omMonitorState?.value }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="cmAgentState.value" label="CM_agent" width="80">
                <template #default="scope">
                  <div class="state-row">
                    <div class="state" :class="[scope.row.cmAgentState?.color?.toLowerCase()]"></div>
                    {{ scope.row.cmAgentState?.value }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sync" :label="$t('clusterMonitor.detail.instance.syncMode')" width="80" />
              <el-table-column
                prop="syncPriority"
                :label="$t('clusterMonitor.detail.instance.syncPriority')"
                width="80"
              />

              <el-table-column
                prop="syncState.value"
                :label="$t('clusterMonitor.detail.instance.syncStatus')"
                width="80"
              >
                <template #default="scope">
                  <div class="state-row">
                    <div class="state" :class="[scope.row.syncState?.color?.toLowerCase()]"></div>
                    {{ scope.row.syncState?.value }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="receivedDelay"
                :label="$t('clusterMonitor.detail.instance.receiveDelay')"
                width="80"
              />
              <el-table-column prop="writeDelay" :label="$t('clusterMonitor.detail.instance.diskDelay')" width="80" />
              <el-table-column
                prop="replayDelay"
                :label="$t('clusterMonitor.detail.instance.replayDelay')"
                width="80"
              />
              <el-table-column :label="$t('app.operate')" align="center" fixed="right" width="80">
                <template #default="scope">
                  <el-link size="small" type="primary" @click="gotoInstance(scope.row)">{{
                    $t('clusterMonitor.detail.instance.performanceMonitoring')
                  }}</el-link>
                </template>
              </el-table-column>
            </el-table>

            <div style="padding: 12px" v-loading="loading">
              <el-row :gutter="12">
                <el-col :span="6">
                  <my-card :title="'CPU(%)'" height="200" :bodyPadding="false">
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.cpu"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                      :max="100"
                      :min="0"
                      :interval="25"
                      :unit="'%'"
                    />
                  </my-card>
                </el-col>
                <el-col :span="6">
                  <my-card :title="$t('clusterMonitor.detail.instance.memory') + '(%)'" height="200" :bodyPadding="false">
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.memory"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                      :max="100"
                      :min="0"
                      :interval="25"
                      :unit="'%'"
                    />
                  </my-card>
                </el-col>
                <el-col :span="6">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.networkReceive') + '(Mb/s)'"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.networkIn"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                      :unit="'Mb/S'"
                    />
                  </my-card>
                </el-col>
                <el-col :span="6">
                  <my-card :title="$t('clusterMonitor.detail.instance.networkSend') + '(Mb/s)'" height="200" :bodyPadding="false">
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.networkOut"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                      :unit="'Mb/S'"
                    />
                  </my-card>
                </el-col>
              </el-row>

              <div class="gap-row"></div>
              <el-row :gutter="12">
                <el-col :span="6">
                  <my-card :title="$t('clusterMonitor.detail.instance.diskRead') + '(B)'" height="200" :bodyPadding="false">
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.io"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                      :unit="'B'"
                    />
                  </my-card>
                </el-col>
                <el-col :span="6">
                  <my-card :title="'QPS(%)'" height="200" :bodyPadding="false">
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.qps"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                    />
                  </my-card>
                </el-col>
                <el-col :span="6">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.sqlResponseTime80') + '(ms)'"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.sql80"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                      :unit="'ms'"
                    />
                  </my-card>
                </el-col>
                <el-col :span="6">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.sqlResponseTime95') + '(ms)'"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.sql95"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                      :unit="'ms'"
                    />
                  </my-card>
                </el-col>
              </el-row>

              <div class="gap-row"></div>
              <el-row :gutter="12">
                <el-col :span="8">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.primaryWalAccumulation')"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.writeTotal"
                      :xData="metricsData.writeTotalTime"
                      :xFormater="'MM-DD'"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                    />
                  </my-card>
                </el-col>
                <el-col :span="8">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.primaryWalSendPressure')"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.sendPressure"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                    />
                  </my-card>
                </el-col>
                <el-col :span="8">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.primaryWalWriteRate')"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.writePerSecond"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                    />
                  </my-card>
                </el-col>
              </el-row>

              <div class="gap-row"></div>
              <el-row :gutter="12">
                <el-col :span="8">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.standbyReceiveDelay')"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.receivedDelay"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                    />
                  </my-card>
                </el-col>
                <el-col :span="8">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.standbyDiskDelay')"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.writeDelay"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                    />
                  </my-card>
                </el-col>
                <el-col :span="8">
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.standbyReplayDelay')"
                    height="200"
                    :bodyPadding="false"
                  >
                    <LazyLine
                      :tabId="tabId"
                      :formatter="toFixed"
                      :data="metricsData.replayDelay"
                      :xData="metricsData.time"
                      :legendShown="false"
                      :tool-tips-sort="'desc'"
                      :tool-tips-exclude-zero="true"
                    />
                  </my-card>
                </el-col>
              </el-row>
            </div>
          </my-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRequest } from 'vue-request'
import { getClusterDetails, getClusterNodes, ClusterNode } from '@/api/cluster'
import { toFixed, uuid } from '@/shared'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import colorCharts from '@/assets/style/color.module.scss'
import router from '@/router'
import { Refresh } from '@element-plus/icons-vue'
const { t } = useI18n()
const tabId = ref<string>(uuid())
const clusterRow = ref<any>({ clusterId: '' })
const clusterNodesData = ref<ClusterNode[]>([])

interface LineData {
  name: string
  data: any[]
  [other: string]: any
}
interface MetricsData {
  cpu: LineData[]
  memory: LineData[]
  networkIn: LineData[]
  networkOut: LineData[]
  io: LineData[]
  qps: LineData[]
  sql80: LineData[]
  sql95: LineData[]
  sendPressure: LineData[]
  writePerSecond: LineData[]
  receivedDelay: LineData[]
  writeDelay: LineData[]
  replayDelay: LineData[]
  writeTotal: LineData[]
  writeTotalTime: string[]
  time: string[]
}
const defaultData = {
  cpu: [],
  memory: [],
  networkIn: [],
  networkOut: [],
  io: [],
  qps: [],
  sql80: [],
  sql95: [],
  sendPressure: [],
  writePerSecond: [],
  receivedDelay: [],
  writeDelay: [],
  replayDelay: [],
  writeTotal: [],
  writeTotalTime: [],
  time: [],
}
const metricsData = ref<MetricsData>(defaultData)
const colorArray: Array<string> = colorCharts.chartColors.split(',')

onMounted(() => {})

const myEmit = defineEmits(['goback'])
const goback = () => {
  myEmit('goback')
}
const goInto = (row: any) => {
  clusterRow.value = row
  loadClusterNodes(clusterRow.value.clusterId)
}
defineExpose({ goInto })

const gotoInstance = (row: any) => {
  let nodeId = row.nodeId
  if (!nodeId) {
    ElMessage({
      showClose: true,
      message: t('clusterMonitor.list.noNodeId'),
      type: 'warning',
    })
    return
  }
  const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
  if (curMode === 'wujie') {
    // @ts-ignore plug-in components
    window.$wujie?.props.methods.jump({
      name: `Static-pluginObservability-instanceVemDashboardInstance`,
      query: {
        nodeId,
      },
    })
  } else {
    // local
    router.push({ path: `/vem/dashboard/instance`, query: { nodeId } })
  }
}
const selectionChange = (selection: Array) => {
  if (selection.length <= 0) {
    for (let prop in metricsData.value) {
      if (Object.prototype.hasOwnProperty.call(metricsData.value, prop)) {
        metricsData.value[prop].forEach((element) => {
          element.lineStyle = undefined
        })
      }
    }
  } else {
    for (let prop in metricsData.value) {
      if (Object.prototype.hasOwnProperty.call(metricsData.value, prop)) {
        metricsData.value[prop].forEach((element) => {
          if (
            selection.some((item: any) => {
              return item.nodeName === element.name
            })
          ) {
            element.lineStyle = undefined
          } else {
            element.lineStyle = {
              opacity: 0.1,
            }
          }
        })
      }
    }
  }
}
const { data: allClusters, run: loadAllClusters, loading } = useRequest(getClusterDetails, { manual: true })
watch(
  allClusters,
  () => {
    // clear data
    metricsData.value = JSON.parse(JSON.stringify(defaultData))

    if (!allClusters.value) return

    let responseKeys = [
      ['CPU', 'cpu'], // cpu
      ['MEMORY', 'memory'], // memory
      ['NETWORK_IN_TOTAL', 'networkIn'], // network
      ['NETWORK_OUT_TOTAL', 'networkOut'], // network
      ['IOPS_R_TOTAL', 'io'], // io
      ['INSTANCE_QPS', 'qps'], // qps
      ['INSTANCE_DB_RESPONSETIME_P80', 'sql80'], // 80%
      ['INSTANCE_DB_RESPONSETIME_P95', 'sql95'], // 95%
      ['CLUSTER_PRIMARY_WAL_SEND_PRESSURE', 'sendPressure'], // Send Pressure
      ['CLUSTER_PRIMARY_WAL_WRITE_PER_SEC', 'writePerSecond'], // write per sencend
      ['CLUSTER_WAL_RECEIVED_DELAY', 'receivedDelay'], // received delay
      ['CLUSTER_WAL_WRITE_DELAY', 'writeDelay'], // write delay
      ['CLUSTER_WAL_REPLAY_DELAY', 'replayDelay'], // replay delay
      ['CLUSTER_PRIMARY_WAL_WRITE_TOTAL', 'writeTotal'], // write total daily
    ]
    responseKeys.forEach((responseKeyArray) => {
      for (let node of clusterNodesData.value) {
        let tempData: string[] = []
        if (allClusters.value[responseKeyArray[0]] && allClusters.value[responseKeyArray[0]][node.nodeId]) {
          allClusters.value[responseKeyArray[0]][node.nodeId].forEach((element) => {
            if (responseKeyArray[0] === 'NETWORK_IN_TOTAL' || responseKeyArray[0] === 'NETWORK_OUT_TOTAL') {
              tempData.push(toFixed(element / 1024 / 1024, 1))
            } else if (
              responseKeyArray[0] === 'INSTANCE_DB_RESPONSETIME_P80' ||
              responseKeyArray[0] === 'INSTANCE_DB_RESPONSETIME_P95'
            ) {
              tempData.push(toFixed(element / 1000, 1))
            } else tempData.push(toFixed(element))
          })
          metricsData.value[responseKeyArray[1]].push({ data: tempData, name: node.nodeName, key: node.nodeId })
        } else {
          metricsData.value[responseKeyArray[1]].push({ data: [], name: node.nodeName, key: node.nodeId })
        }
      }
    })

    // time
    metricsData.value.time = allClusters.value.time
    metricsData.value.writeTotalTime = allClusters.value.CLUSTER_PRIMARY_WAL_WRITE_TOTAL_TIME
  },
  { deep: true }
)

const {
  data: clusterNodes,
  run: loadClusterNodes,
  loading: loadingNodes,
} = useRequest(getClusterNodes, { manual: true })
watch(
  clusterNodes,
  () => {
    if (!clusterNodes.value) return

    clusterNodesData.value = clusterNodes.value

    for (let index = 0; index < clusterNodesData.value.length; index++) {
      const element = clusterNodesData.value[index]
      element.color = colorArray[index]
    }
    loadAllClusters(clusterRow.value.clusterId)
  },
  { deep: true }
)
</script>

<style scoped lang="scss"></style>
