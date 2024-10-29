<template>
  <div class="tab-wrapper" id="instanceClusterMonitor">
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
            <template #headerExtend>
              <div class="card-links">
                <el-link type="primary" @click="showTopology(false)">
                  <div class="btn-row" v-if="!showTop">
                    <svg-icon class="table_selected icon" name="table_selected" />
                    {{$t('clusterMonitor.table')}}
                  </div>
                  <div class="btn-row" v-else style="color: black">
                    <svg-icon class="table icon" name="table" />
                    {{$t('clusterMonitor.table')}}
                  </div>
                </el-link>
                <div class="el-link" style="width: 1px; height: 16px; background: #d9d9d9"></div>
                <el-link type="primary" @click="showTopology(true)">
                  <div class="btn-row" v-if="!showTop" style="color: black">
                    <svg-icon class="topology" name="topology" />
                    {{$t('clusterMonitor.topology')}}
                  </div>
                  <div class="btn-row" v-else>
                    <svg-icon class="topology_selected" name="topology_selected" />
                    {{$t('clusterMonitor.topology')}}
                  </div>
                </el-link>
              </div>
            </template>

            <div style="position: relative">
              <div v-show="showTop" style="position: absolute; top: 8px; left: 16px">
                <div class="legends" v-for="item in clusterNodesData" :key="item.nodeId">
                  <div class="icon" :style="{ background: colorArray[clusterNodesData.indexOf(item)] }"></div>
                  <span>{{ item.role }}:{{ item.nodeName }}</span>
                </div>
              </div>

              <div v-show="showTop" class="line-tips" style="position: absolute; top: 12px; left: auto; right: 10px">
                <div class=""><svg-icon name="info" /> {{$t('clusterMonitor.tips')}}</div>
              </div>

              <keep-alive>
                <div
                  v-loading="loadingNodes"
                  v-show="showTop"
                  ref="networkContainer"
                  style="width: 100%; height: 230px"
                ></div>
              </keep-alive>
              <div id="tooltips" class="menu">
                <div>{{$t('clusterMonitor.ipAdr')}}{{ hoverNode?.localAddr }}</div>
                <div>{{$t('clusterMonitor.role')}}{{ hoverNode?.role }}</div>
                <div class="state-row" style="display: flex; flex-direction: row">
                  {{$t('clusterMonitor.state')}}
                  <div class="state" :class="[hoverNode?.nodeState?.color?.toLowerCase()]"></div>
                  {{ hoverNode?.nodeState.value }}
                </div>
                <div>{{$t('clusterMonitor.replayDelay')}}{{ hoverNode?.writeDelay ? hoverNode?.writeDelay : '--' }}</div>
              </div>
            </div>

            <el-table
              ref="myTable"
              v-show="!showTop"
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
              <el-table-column prop="role" :label="$t('clusterMonitor.detail.instance.role')" width="60">
                <template #default="scope">
                  <div align="center">{{scope.row.role ? scope.row.role : $t('clusterMonitor.noneRole')}}</div>
                </template>
              </el-table-column>
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
              <el-table-column prop="cmAgentState.value" label="CM_agent" width="100">
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
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.memory') + '(%)'"
                    height="200"
                    :bodyPadding="false"
                  >
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
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.networkSend') + '(Mb/s)'"
                    height="200"
                    :bodyPadding="false"
                  >
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
                  <my-card
                    :title="$t('clusterMonitor.detail.instance.diskRead') + '(B)'"
                    height="200"
                    :bodyPadding="false"
                  >
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
          <my-card
            style="margin-top: 12px"
            :title="$t('clusterMonitor.switchRecord.switchRecord')"
            height="380"
            :bodyPadding="false"
          >
            <div class="filter-bar" style="padding-top: 6px">
              <div class="item">
                <el-select v-model="timeRange" style="width: 130px; margin: 0 12px" @change="loadSwitchRecords">
                  <el-option :value="'1'" :label="$t('dashboard.last1M')" />
                  <el-option :value="'3'" :label="$t('dashboard.last3M')" />
                  <el-option :value="'6'" :label="$t('dashboard.last6M')" />
                  <el-option :value="'0'" :label="$t('app.all')" />
                </el-select>
              </div>
            </div>
            <el-table
              :data="switchRecordData"
              style="width: 100%;"
              border
              :header-cell-class-name="
                () => {
                  return 'grid-header'
                }
              "
              v-loading="loadingSwitchRecords"
            >
              <el-table-column prop="switchTime" :label="$t('clusterMonitor.switchRecord.switchTime')" width="160" />
              <el-table-column prop="primary" :label="$t('clusterMonitor.switchRecord.mainNode')" width="120" />
              <el-table-column prop="reason" :label="$t('clusterMonitor.switchRecord.reason')" />
            </el-table>
            <el-pagination
              :currentPage="page.currentPage"
              :pageSize="page.pageSize"
              :total="page.total"
              :page-sizes="[10, 20, 30, 40]"
              class="pagination"
              layout="total,sizes,prev,pager,next"
              background
              small
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </my-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import vis from 'vis'
import { useRequest } from 'vue-request'
import {
  getClusterDetails,
  getClusterNodes,
  ClusterNode,
  ClusterRelation,
  getSwitchRecords,
  switchRecord,
} from '@/api/cluster'
import { toFixed, uuid } from '@/shared'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import colorCharts from '@/assets/style/color.module.scss'
import router from '@/router'
import { Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)
const { t } = useI18n()
const tabId = ref<string>(uuid())
const clusterRow = ref<any>({ clusterId: '' })
const clusterNodesData = ref<ClusterNode[]>([])
const showTop = ref<boolean>(false)
const nodes = ref<vis.DataSet>([])
const edges = ref<vis.DataSet>([])
const myTable = ref<any>()
const hoverNode = ref<any>()
const timeRange = ref<string>('1')

const networkContainer = ref<any>()
let network: any = null

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
const switchRecordData = ref<switchRecord[]>([])
const page = ref<any>({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

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
const selectNode = (nodeId: String) => {
  if (!nodeId) return
  let index = clusterNodesData.value.findIndex((obj) => obj.nodeId === nodeId)
  let selected = myTable.value.getSelectionRows()
  if (selected.findIndex((obj) => obj.nodeId === nodeId) >= 0) {
    myTable.value.toggleRowSelection(clusterNodesData.value[index], false)
  } else {
    myTable.value.toggleRowSelection(clusterNodesData.value[index], true)
  }
}
const selectionChange = (selection: Array) => {
  if (selection.length <= 0) {
    for (let prop in metricsData.value) {
      if (Object.prototype.hasOwnProperty.call(metricsData.value, prop)) {
        metricsData.value[prop].forEach((element) => {
          if (typeof element === 'object') {
            element.lineStyle = undefined
          }
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
            if (typeof element === 'object') {
              element.lineStyle = {
                opacity: 0.1,
              }
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

    clusterNodesData.value = clusterNodes?.value.nodeList
    for (let index = 0; index < clusterNodesData.value.length; index++) {
      const element = clusterNodesData.value[index]
      element.color = colorArray[index]
    }
    loadAllClusters(clusterRow.value.clusterId)

    page.value.pageSize = 10
    page.value.currentPage = 1
    loadSwitchRecords()

    // do topology
    let nodesTemp = []
    let edgesTemp = []
    if (clusterNodes?.value.relation?.length > 0) {
      clusterNodes?.value.relation.forEach((element) => {
        buildNode(nodesTemp, edgesTemp, element, 0)
      })
    }
    nodes.value = new vis.DataSet(nodesTemp)
    edges.value = new vis.DataSet(edgesTemp)
    if (showTop.value) {
      drawTopology()
    }
  },
  { deep: true }
)

// topology
const buildNode = (nodes: any[], edges: any[], relation: ClusterRelation, level: number) => {
  nodes.push({ id: relation.nodeId, label: '', image: getUrl(relation.nodeId), shape: 'image', level })
  if (relation.children?.length > 0) {
    relation.children.forEach((element) => {
      edges.push({ from: relation.nodeId, to: element.nodeId })
      buildNode(nodes, edges, element, level + 1)
    })
  }
}
const showTopology = (val) => {
  showTop.value = val
  if (val) {
    drawTopology()
  }
}
const drawTopology = () => {
  const container = networkContainer.value
  const options = {
    nodes: {
      shape: 'text',
      fixed: true,
    },
    interaction: {
      hover: true,
      hoverConnectedEdges: false,
    },
    layout: {
      hierarchical: {
        direction: 'UD',
        sortMethod: 'directed',
        levelSeparation: 80,
        nodeSpacing: 200,
      },
    },

    edges: {
      arrows: {
        to: {
          enabled: true,
          type: 'circle',
          scaleFactor: 0.1,
        },
      },
      arrowStrikethrough: true,
      chosen: {
        edge: false,
      },
      color: {
        color: '#D8D8D8',
      },
      smooth: {
        enabled: true,
        forceDirection: 'vertical',
        type: 'cubicBezier',
        roundness: 0.9,
      },
    },
  }
  const data = { nodes: nodes.value, edges: edges.value }
  network = new vis.Network(container, data, options)

  network.once('stabilizationIterationsDone', function () {
    network.moveTo({ scale: 1.6 })
  })

  let debounceTimeout = null
  let debounceDelay = 200
  let eventHandler = function (event) {
    clearTimeout(debounceTimeout)
    debounceTimeout = setTimeout(function () {
      let nodeId = event.nodes[0]
      selectNode(nodeId)
    }, debounceDelay)
  }
  network.off('click', eventHandler)
  network.on('click', eventHandler)

  network.on('hoverNode', function (properties: any) {
    let nodeId = properties.node
    let node = clusterNodesData.value.find(function (obj) {
      return obj.nodeId === nodeId
    })
    hoverNode.value = node
    if (hoverNode.value && !hoverNode.value.role) {
      hoverNode.value.role = t('clusterMonitor.noneRole')
    }
    let nodePosition = network.getPositions([nodeId])[nodeId]
    let canvasX = nodePosition.x
    let canvasY = nodePosition.y
    let domPosition = network.canvasToDOM({ x: canvasX, y: canvasY })

    let divHoverNode = document.getElementById('tooltips')
    divHoverNode.style.display = 'block'
    divHoverNode.style.left = domPosition.x - 150 + 'px'
    divHoverNode.style.top = domPosition.y - 130 + 'px'
  })

  network.on('blurNode', function () {
    let divHoverNode = document.getElementById('tooltips')
    divHoverNode.style.display = 'none'
  })
}
const getUrl = (nodeId: String) => {
  let node = clusterNodesData.value.find(function (obj) {
    return obj.nodeId === nodeId
  })
  if (!node) return

  let nodeRole = node.role ? node.role : t('clusterMonitor.noneRoleName')
  let cmServerStateColor = node.cmServerState.color.toLowerCase()
  let omMonitorStateColor = node.omMonitorState.color.toLowerCase()
  let cmAgentStateColor = node.cmAgentState.color.toLowerCase()
  let nodeMain =
    'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0NCIgaGVpZ2h0PSI1MCIgdmlld0JveD0iMCAwIDQ0IDUwIiBmaWxsPSJub25lIj4NCiAgPHBhdGggZD0iTTIxLjg3NDggMzQuODczNkMzMS4yNDc4IDM0Ljg3MzYgMzguODQ2MiAzMC41NjY0IDM4Ljg0NjIgMjUuMjUzM0MzOC44NDYyIDE5Ljk0MDIgMzEuMjQ3OCAxNS42MzMxIDIxLjg3NDggMTUuNjMzMUMxMi41MDE3IDE1LjYzMzEgNC45MDMzNSAxOS45NDAyIDQuOTAzMzUgMjUuMjUzM0M0LjkwMzM1IDMwLjU2NjQgMTIuNTAxNyAzNC44NzM2IDIxLjg3NDggMzQuODczNloiIGZpbGw9InVybCgjcGFpbnQwX2xpbmVhcl80NjEwXzQyMTA1KSIvPg0KICA8cGF0aCBkPSJNMzguODQ2MiAyMy44NjFWMzYuNTE5MkMzOC4zNDMzIDQxLjY0NTggMzAuOTI2MiA0NS43NTk3IDIxLjg3NDggNDUuNzU5N0MxMi44MjMzIDQ1Ljc1OTcgNS40NjkwNiA0MS43MDkxIDQuOTAzMzUgMzYuNTE5MlYyMy44NjFDNC45MDMzNSAyNC4wNTA4IDQuOTAzMzUgMjQuMjQwNyA0LjkwMzM1IDI0LjQzMDZDNC45MDMzNSAyOS44MTAzIDEyLjUwOTEgMzQuMjQwNyAyMS44NzQ4IDM0LjI0MDdDMzEuMjQwNSAzNC4yNDA3IDM4Ljg0NjIgMjkuODczNiAzOC44NDYyIDI0LjQzMDZDMzguODQ2MiAyNC4yNDA3IDM4Ljg0NjIgMjQuMDUwOCAzOC44NDYyIDIzLjg2MVoiIGZpbGw9InVybCgjcGFpbnQxX2xpbmVhcl80NjEwXzQyMTA1KSIvPg0KICA8cGF0aCBvcGFjaXR5PSIwLjQiIGQ9Ik01LjQwNjcgMzEuNDU1OEg1LjcyMDk4QzUuNzIwOTggMzYuNTE5MSAxMy4wMTI0IDQwLjY5NjMgMjIuMDYzOCA0MC42OTYzQzMxLjExNTMgNDAuNjk2MyAzOC40MDY3IDM2LjUxOTEgMzguNDA2NyAzMS40NTU4SDM4LjcyMUMzOC43MjEgMzYuNzcyMyAzMS4yNDEgNDEuMDEyOCAyMi4wNjM4IDQxLjAxMjhDMTIuODg2NyA0MS4wMTI4IDUuNDA2NyAzNi43NzIzIDUuNDA2NyAzMS40NTU4WiIgZmlsbD0iIzYxNzRBQSIvPg0KICA8cGF0aCBkPSJNMzguNTMxOSAyNC45MzY4VjI0LjY4MzZDMzguMDI5MSAxOS44NzM1IDMwLjczNzYgMTUuOTQ5NSAyMS44NzQ4IDE1Ljk0OTVDMTMuMDExOSAxNS45NDk1IDUuNzIwNDkgMTkuODEwMiA1LjIxNzYzIDI0LjY4MzZWMjQuOTM2OFYzNi43MDlWMzYuOTYyMUM1LjcyMDQ5IDQxLjc3MjMgMTMuMDExOSA0NS42OTYzIDIxLjg3NDggNDUuNjk2M0MzMC43Mzc2IDQ1LjY5NjMgMzguMDI5MSA0MS44MzU1IDM4LjUzMTkgMzYuOTYyMVYzNi43MDlWMjQuOTM2OFoiIHN0cm9rZT0iIzQzNjNCRCIgc3Ryb2tlLXdpZHRoPSIxLjQiLz4NCiAgPHBhdGggZD0iTTcuNjA2NyAzMS43MDlMOC45MjY3MiAzMi43ODQ5VjM1LjMxNjZMNy42MDY3IDM0LjI0MDZWMzEuNzA5WiIgZmlsbD0iI0E1QURDMSIvPg0KICA8cGF0aCBvcGFjaXR5PSIwLjUwMTIiIGQ9Ik0xMC4yNDYyIDMzLjQ4MUwxMS41NjYyIDM0LjU1NjlWMzcuMDg4NkwxMC4yNDYyIDM2LjAxMjZWMzMuNDgxWiIgZmlsbD0iI0E1QURDMSIvPg0KICA8cGF0aCBkPSJNMjEuODc0OCAyMy42NzA5QzMxLjI0NzggMjMuNjcwOSAzOC44NDYyIDE5LjM2MzggMzguODQ2MiAxNC4wNTA3QzM4Ljg0NjIgOC43Mzc1NiAzMS4yNDc4IDQuNDMwNDIgMjEuODc0OCA0LjQzMDQyQzEyLjUwMTcgNC40MzA0MiA0LjkwMzM1IDguNzM3NTYgNC45MDMzNSAxNC4wNTA3QzQuOTAzMzUgMTkuMzYzOCAxMi41MDE3IDIzLjY3MDkgMjEuODc0OCAyMy42NzA5WiIgZmlsbD0iI0YwRjJGOSIvPg0KICA8cGF0aCBkPSJNMzguODQ2MiAxMi42NTgzVjI1LjMxNjZDMzguMzQzMyAzMC40NDMxIDMwLjkyNjIgMzQuNTU3MSAyMS44NzQ4IDM0LjU1NzFDMTIuODIzMyAzNC41NTcxIDUuNDY5MDYgMzAuNTA2NCA0LjkwMzM1IDI1LjMxNjZWMTIuNjU4M0M0LjkwMzM1IDEyLjg0ODIgNC45MDMzNSAxMy4wMzgxIDQuOTAzMzUgMTMuMjI4QzQuOTAzMzUgMTguNjA3NyAxMi41MDkxIDIzLjAzODEgMjEuODc0OCAyMy4wMzgxQzMxLjI0MDUgMjMuMDM4MSAzOC44NDYyIDE4LjY3MSAzOC44NDYyIDEzLjIyOEMzOC44NDYyIDEzLjAzODEgMzguODQ2MiAxMi44NDgyIDM4Ljg0NjIgMTIuNjU4M1oiIGZpbGw9InVybCgjcGFpbnQyX2xpbmVhcl80NjEwXzQyMTA1KSIvPg0KICA8cGF0aCBvcGFjaXR5PSIwLjQiIGQ9Ik01LjQwNjcgMjAuMjUzMkg1LjcyMDk4QzUuNzIwOTggMjUuMzE2NSAxMy4wMTI0IDI5LjQ5MzcgMjIuMDYzOCAyOS40OTM3QzMxLjExNTMgMjkuNDkzNyAzOC40MDY3IDI1LjMxNjUgMzguNDA2NyAyMC4yNTMySDM4LjcyMUMzOC43MjEgMjUuNTY5NiAzMS4yNDEgMjkuODEwMSAyMi4wNjM4IDI5LjgxMDFDMTIuODg2NyAyOS44MTAxIDUuNDA2NyAyNS41Njk2IDUuNDA2NyAyMC4yNTMyWiIgZmlsbD0iIzYxNzRBQSIvPg0KICA8cGF0aCBkPSJNNS4yMTc2MyAyNy43ODQ4VjI3LjUzMTZWMTMuNzM0MlYxMy40ODFDNS43MjA0OSA4LjYwNzU5IDEzLjAxMTkgNC43NDY4MyAyMS44NzQ4IDQuNzQ2ODNDMzAuNzM3NiA0Ljc0NjgzIDM4LjAyOTEgOC42NzA4OCAzOC41MzE5IDEzLjQ4MVYxMy43MzQyVjI4LjE2NDVWMjguNDE3NyIgc3Ryb2tlPSIjNDM2M0JEIiBzdHJva2Utd2lkdGg9IjEuNCIvPg0KICA8cGF0aCBkPSJNNy42MDY3IDIwLjUwNjNMOC45MjY3MiAyMS41ODIzVjI0LjExMzlMNy42MDY3IDIzLjAzOFYyMC41MDYzWiIgZmlsbD0iIzg2OUFENCIvPg0KICA8cGF0aCBvcGFjaXR5PSIwLjUwMTIiIGQ9Ik0xMC4yNDYyIDIyLjI3ODNMMTEuNTY2MiAyMy4zNTQzVjI1Ljg4NTlMMTAuMjQ2MiAyNC44MVYyMi4yNzgzWiIgZmlsbD0iI0E1QjRERiIvPg0KICA8ZGVmcz4NCiAgICA8bGluZWFyR3JhZGllbnQgaWQ9InBhaW50MF9saW5lYXJfNDYxMF80MjEwNSIgeDE9IjEyLjQxNTIiIHkxPSIxNy44NTEyIiB4Mj0iMjEuMjA2OCIgeTI9IjM5LjIzODgiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj4NCiAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNGRUZFRkYiLz4NCiAgICAgIDxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iI0YxRjZGRiIvPg0KICAgIDwvbGluZWFyR3JhZGllbnQ+DQogICAgPGxpbmVhckdyYWRpZW50IGlkPSJwYWludDFfbGluZWFyXzQ2MTBfNDIxMDUiIHgxPSIzOC44NjY4IiB5MT0iMzQuOTA1IiB4Mj0iNC45NDE3IiB5Mj0iMzMuODg5OCIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPg0KICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0E0QjNERiIvPg0KICAgICAgPHN0b3Agb2Zmc2V0PSIwLjIzNTQiIHN0b3AtY29sb3I9IiM3NjhEQ0YiLz4NCiAgICAgIDxzdG9wIG9mZnNldD0iMC40NjY0IiBzdG9wLWNvbG9yPSIjREFFMkY3Ii8+DQogICAgICA8c3RvcCBvZmZzZXQ9IjAuNjkxNiIgc3RvcC1jb2xvcj0iI0JBQzVFNiIvPg0KICAgICAgPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjRDVEQ0YwIi8+DQogICAgPC9saW5lYXJHcmFkaWVudD4NCiAgICA8bGluZWFyR3JhZGllbnQgaWQ9InBhaW50Ml9saW5lYXJfNDYxMF80MjEwNSIgeDE9IjM4Ljg2NjgiIHkxPSIyMy43MDIzIiB4Mj0iNC45NDE3IiB5Mj0iMjIuNjg3MSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPg0KICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0E0QjNERiIvPg0KICAgICAgPHN0b3Agb2Zmc2V0PSIwLjIzNTQiIHN0b3AtY29sb3I9IiM3NjhEQ0YiLz4NCiAgICAgIDxzdG9wIG9mZnNldD0iMC40NjY0IiBzdG9wLWNvbG9yPSIjREFFMkY3Ii8+DQogICAgICA8c3RvcCBvZmZzZXQ9IjAuNjkxNiIgc3RvcC1jb2xvcj0iI0JBQzVFNiIvPg0KICAgICAgPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjRDVEQ0YwIi8+DQogICAgPC9saW5lYXJHcmFkaWVudD4NCiAgPC9kZWZzPg0KPC9zdmc+'

  let svg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="190" height="88" >' +
    '<rect x="0" y="0" width="100%" height="100%" fill="#ffffff" stroke-width="20" stroke="#ffffff" ></rect>' +
    '<foreignObject x="0" y="0" width="100%" height="100%">' +
    '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size:14px;">' +
    '  <div style="display: flex; flex-direction: row; align-items: center;" class="container">' +
    '    <div style="display: flex; flex-direction: column; align-items: center;margin-right:24px">' +
    '      <img style="height:50px;width:44px;margin:bottom:2px" src="' +
    nodeMain +
    '      "/>' +
    '      <div style="font-size: 14px;font-weight: bold;">[nodeRole]</div>' +
    '    </div>' +
    '    <div class="list">' +
    '      <div class="item"><div class="row">' +
    '        <div class="point [cmServerStateColor]"></div>' +
    '        <div>CM_server</div>' +
    '      </div></div>' +
    '      <div class="item"><div class="row">' +
    '        <div class="point [omMonitorStateColor]"></div>' +
    '        <div>OM_monitor</div>' +
    '      </div></div>' +
    '      <div class="item"><div class="row">' +
    '        <div class="point [cmAgentStateColor]"></div>' +
    '        <div>CM_agent</div>' +
    '      </div></div>' +
    '    </div>' +
    '  </div>' +
    '</div>' +
    '<style>' +
    '  .container{' +
    '    padding:4px 8px;' +
    '    justify-content:space-around;' +
    '    border-radius: 4px;' +
    '    border: 1px solid var(--unnamed, #D9D9D9);' +
    '    box-shadow: 0px 4px 4px 0px rgba(0, 0, 0, 0.04);' +
    '  }' +
    '  .item{font-size: 12px;}' +
    '  .row{' +
    '    display: flex;' +
    '    flex-direction: row;' +
    '    border-radius: 4px;' +
    '    border: 1px solid var(--unnamed, #d9d9d9);' +
    '    padding: 2px 4px;' +
    '    align-items: center;' +
    '    box-shadow: 0px 4px 4px 0px rgba(0, 0, 0, 0.04);' +
    '   margin-bottom: 4px;' +
    '  }' +
    '  .point{' +
    '      width: 6px;' +
    '      height: 6px;' +
    '      border-radius: 100px;' +
    '      margin-right: 4px;' +
    '  }' +
    '  .point.green {' +
    '    background: var(--green, #37c461);' +
    '    box-shadow: 0px 1px 3px 0px #52c41a;' +
    '  }' +
    '  .point.yellow {' +
    '    background: var(--green, #ffa53c);' +
    '    box-shadow: 0px 1px 3px 0px #ffa53c;' +
    '  }' +
    '  .point.red {' +
    '    background: var(--green, #f6605a);' +
    '    box-shadow: 0px 1px 3px 0px #f6605a;' +
    '  }' +
    '  .point.grey {' +
    '    background: var(--green, #b8b8b8);' +
    '    box-shadow: 0px 1px 3px 0px #b8b8b8;' +
    '  }' +
    '</style>' +
    '</foreignObject></svg>'
  svg = svg.replace('[nodeRole]', nodeRole)
  svg = svg.replace('[cmServerStateColor]', cmServerStateColor)
  svg = svg.replace('[omMonitorStateColor]', omMonitorStateColor)
  svg = svg.replace('[cmAgentStateColor]', cmAgentStateColor)

  let url = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg)
  return url
}

// switch record start
const loadSwitchRecords = () => {
  let clusterId = clusterRow.value.clusterId
  if (timeRange.value === '0') {
    loadSwitchRecordsRun(clusterId, page.value.pageSize, page.value.currentPage)
  } else {
    let start = 0
    let end = 0
    let month = Number(timeRange.value)
    let now = new Date()
    let min = 24 * 60 // min for 1 day
    start = Number.parseInt(`${now.getTime() - 1000 * min * 30 * month * 60}`)
    end = Number.parseInt(`${now.getTime()}`)
    loadSwitchRecordsRun(clusterId, page.value.pageSize, page.value.currentPage, start, end)
  }
}
const {
  data: switchRecordSourceData,
  run: loadSwitchRecordsRun,
  loading: loadingSwitchRecords,
} = useRequest(getSwitchRecords, { manual: true })
watch(
  switchRecordSourceData,
  () => {
    // clear data
    switchRecordData.value = []

    if (!switchRecordSourceData.value) return

    switchRecordData.value = switchRecordSourceData.value.records
    page.value.total = switchRecordSourceData.value.total
  },
  { deep: true }
)

const handleSizeChange = (val: number) => {
  page.value.pageSize = val
  page.value.currentPage = 1
  loadSwitchRecords()
}
const handleCurrentChange = (val: number) => {
  page.value.currentPage = val
  loadSwitchRecords()
}

// switch record end
</script>

<style scoped lang="scss">
.btn-row {
  display: flex;
  align-items: flex-end;
}
.legends {
  font-size: 12px;
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 6px;
  .icon {
    border-radius: 2px;
    background: #246cff;
    width: 12px;
    height: 12px;
    flex-shrink: 0;
    margin-right: 4px;
  }
}

.menu {
  position: absolute;
  border-radius: 4px;
  left: -99999px;
  top: -999999px;
  z-index: 99999;
  width: 170px;
  font-size: 12px;
  color: #ffffff;
  background: #000000a3;
  padding: 14px 10px;
  div {
    line-height: 20px;
  }
}
</style>
