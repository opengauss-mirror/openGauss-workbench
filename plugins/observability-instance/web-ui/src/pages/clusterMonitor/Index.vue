<template>
  <cluster ref="clusterRef" v-if="indexs.showingComponent == 'cluster'" @goback="backToHome"></cluster>
  <div class="tab-wrapper" v-show="indexs.showingComponent == 'list'">
    <el-container>
      <el-main style="position: relative; padding-top: 0px" class="padding-fix">
        <div class="page-header" style="padding-left: 20px">
          <div class="icon"></div>
          <div class="title">{{ $t('clusterMonitor.clusterMonitor') }}</div>
          <div class="seperator"></div>
          <el-breadcrumb separator="/" style="flex-grow: 1">
            <el-breadcrumb-item :to="{ path: '/vem/dashboard/clusters' }">{{
              $t('clusterMonitor.clusterMonitor')
            }}</el-breadcrumb-item>
          </el-breadcrumb>

          <el-button
            class="refresh-button"
            type="primary"
            :icon="Refresh"
            style="padding: 8px"
            @click="autoRefreshFn"
          />
        </div>

        <div class="main-container">
          <div class="statistics" v-loading="loadingStatistics">
            <div class="item">
              <div class="title">{{ $t('clusterMonitor.clusterState') }}</div>
              <div class="row">
                <div>
                  <div style="width: 300px; height: 140px" id="pieChart" ref="main"></div>
                </div>
                <div>
                  <div style="width: 200px; height: 140px" id="pieChart2" ref="main2"></div>
                </div>
              </div>
            </div>

            <div class="item">
              <div class="title">{{ $t('clusterMonitor.nodeDelay') }}</div>
              <div class="row">
                <div>
                  <div id="pieChart3" ref="main3" e style="width: 200px; height: 140px"></div>
                </div>
                <div class="top5">
                  <div class="item" v-for="item in statisticsData?.top5" :key="item.nodeName">
                    <div class="name">{{ item.nodeName }}</div>
                    <div class="ratio-row">
                      <div class="ratio-container">
                        <div class="ratio" :style="{ width: item.ratio * 100 + '%' }"></div>
                        <div class="ratio background"></div>
                      </div>
                      <div class="value">{{ item.value }}</div>
                    </div>
                  </div>
                  <div class="bottom-title">{{ $t('clusterMonitor.topDelay') }}</div>
                </div>
              </div>
            </div>
          </div>

          <el-tabs v-model="dashboardTabKey" class="index-tabs" @tab-change="tabChange">
            <el-tab-pane class="min-height" :label="$t('clusterMonitor.clusterList')" name="clusterList">
              <div class="filter-bar">
                <div class="item">
                  <div style="white-space: nowrap">{{ $t('clusterMonitor.list.deploymentMethodN') }}</div>
                  <el-select v-model="selectedArch" style="width: 130px; margin: 0 4px">
                    <el-option :value="''" :label="$t('app.all')" />
                    <el-option v-for="item in archSelections" :key="item" :value="item" :label="item" />
                  </el-select>
                </div>
                <div class="item">
                  <el-input
                    v-model="searchText"
                    :placeholder="$t('clusterMonitor.list.searchClusterName')"
                    :suffix-icon="Search"
                    style="width: 268px"
                  />
                </div>
              </div>
              <el-table
                :table-layout="'auto'"
                :data="filterClusters"
                style="width: 100%"
                border
                :header-cell-class-name="
                  () => {
                    return 'grid-header'
                  }
                "
                v-loading="loading"
              >
                <el-table-column :label="$t('clusterMonitor.list.name')" width="200">
                  <template #default="scope">
                    <el-link size="small" type="primary" @click="gotoCluster(scope.row)">{{
                      scope.row.clusterId
                    }}</el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="clusterState.value" :label="$t('clusterMonitor.list.state')" width="130">
                  <template #default="scope">
                    <div class="state-row">
                      <div class="state" :class="[scope.row.clusterState?.color?.toLowerCase()]"></div>
                      {{ scope.row.clusterState?.value }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="nodeCount" :label="$t('clusterMonitor.list.pointCount')" width="100" />
                <el-table-column prop="arch" :label="$t('clusterMonitor.list.deploymentMethod')" width="100" />
                <el-table-column prop="version" :label="$t('clusterMonitor.list.version')" width="130">
                  <template #default="scope"> {{ scope.row.version }} {{ scope.row.versionNum }} </template>
                </el-table-column>
                <el-table-column prop="desc" :label="$t('clusterMonitor.list.stateDesc')" show-overflow-tooltip />
                <el-table-column :label="$t('app.operate')" align="center" fixed="right" width="80">
                  <template #default="scope">
                    <el-link size="small" type="primary" @click="gotoInstance(scope.row)"
                      >{{ $t('clusterMonitor.instanceMonitor') }}
                    </el-link>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane class="min-height" :label="$t('clusterMonitor.delayList')" name="delayList">
              <div class="filter-bar">
                <div class="item">
                  <div style="white-space: nowrap">{{ $t('clusterMonitor.delay.syncModeN') }}</div>
                  <el-select v-model="selectedSyncMode" style="width: 130px; margin: 0 4px">
                    <el-option :value="''" :label="$t('app.all')" />
                    <el-option v-for="item in syncWaySelections" :key="item" :value="item" :label="item" />
                  </el-select>
                </div>
                <div class="item">
                  <div style="white-space: nowrap">{{ $t('clusterMonitor.delay.syncStatusN') }}</div>
                  <el-select v-model="selectedSyncStatus" style="width: 130px; margin: 0 4px">
                    <el-option :value="''" :label="$t('app.all')" />
                    <el-option v-for="item in syncStateSelections" :key="item" :value="item" :label="item" />
                  </el-select>
                </div>
                <div class="item">
                  <el-input
                    v-model="syncSearchText"
                    :placeholder="$t('clusterMonitor.list.searchInstanceName')"
                    :suffix-icon="Search"
                    style="width: 268px"
                  />
                </div>
              </div>

              <el-table
                :table-layout="'auto'"
                :data="filterNodes"
                style="width: 100%"
                border
                :header-cell-class-name="
                  () => {
                    return 'grid-header'
                  }
                "
                v-loading="loadingNodes"
              >
                <el-table-column :label="$t('clusterMonitor.delay.nodeName')">
                  <template #default="scope">
                    <el-link size="small" type="primary" @click="gotoCluster(scope.row)">{{
                      scope.row.nodeName
                    }}</el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="nodeState.value" :label="$t('clusterMonitor.delay.nodeStatus')" width="80">
                  <template #default="scope">
                    <div class="state-row">
                      <div class="state" :class="[scope.row.nodeState?.color?.toLowerCase()]"></div>
                      {{ scope.row.nodeState?.value }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="primaryAddr" :label="$t('clusterMonitor.delay.primaryIpPort')" width="140" />
                <el-table-column prop="localAddr" :label="$t('clusterMonitor.delay.secondaryIpPort')" width="140" />
                <el-table-column prop="sync" :label="$t('clusterMonitor.delay.syncMode')" width="80" />
                <el-table-column prop="syncState.value" :label="$t('clusterMonitor.delay.syncStatus')" width="80">
                  <template #default="scope">
                    <div class="state-row">
                      <div class="state" :class="[scope.row.syncState?.color?.toLowerCase()]"></div>
                      {{ scope.row.syncState?.value }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="syncPriority" :label="$t('clusterMonitor.delay.syncPriority')" width="80" />
                <el-table-column prop="receivedDelay" :label="$t('clusterMonitor.delay.receiveDelay')" width="100" />
                <el-table-column prop="writeDelay" :label="$t('clusterMonitor.delay.diskDelay')" width="100" />
                <el-table-column prop="replayDelay" :label="$t('clusterMonitor.delay.replayDelay')" width="100" />
                <el-table-column :label="$t('app.operate')" align="center" fixed="right" width="80">
                  <template #default="scope">
                    <el-link size="small" type="primary" @click="gotoInstance(scope.row)"
                      >{{ $t('clusterMonitor.instanceMonitor') }}
                    </el-link>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { cloneDeep } from 'lodash-es'
import { useRequest } from 'vue-request'
import {
  Statistics,
  getStatistics,
  ClusterListItem,
  getAllClusters,
  getAllClustersStates,
  getAllNodes,
  NodeStateItem,
} from '@/api/cluster'
import { Search, Refresh } from '@element-plus/icons-vue'
import Cluster from '@/pages/clusterMonitor/cluster/Index.vue'
import router from '@/router'
import type { TabPanelName } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts/core'
import { LegendComponent, TitleComponent } from 'echarts/components'
import { PieChart, ScatterChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'

const { t } = useI18n()
const clusterRef = ref()
const dashboardTabKey = ref<string>('clusterList')
const selectedArch = ref<string>('')
const selectedSyncMode = ref<string>('')
const selectedSyncStatus = ref<string>('')
const searchText = ref<string>('')
const syncSearchText = ref<string>('')
const indexs = ref<any>({ listOpened: true, clusterOpened: false, showingComponent: 'list' })
const clusterData = ref<void | ClusterListItem[]>([])
const statisticsData = ref<void | Statistics>()
const nodeData = ref<void | NodeStateItem[]>([])
const main = ref()
const main2 = ref()
const main3 = ref()

echarts.use([CanvasRenderer, PieChart, LegendComponent, ScatterChart, TitleComponent])

onMounted(() => {
  loadAllClusters()
  loadStatistics()
})

// statistics info
const theme = localStorage.getItem('theme')
const optionDefault = {
  color: ['#61DDAA', '#FFA53C', '#F6605A', '#B8B8B8'],
  title: {
    text: 'xxx',
    show: true,
    left: '16',
    bottom: 'bottom',
    textStyle: {
      fontSize: 14,
      color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
    },
  },
  legend: {
    orient: 'vertical',
    top: 'middle',
    left: '100',
    textStyle: {
      fontSize: 12,
      color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
    },
    itemWidth: 8,
    itemHeight: 8,
    icon: 'circle',
    formatter: function (name: any) {
      return ''
    },
  },
  series: [
    {
      type: 'pie',
      radius: ['50%', '65%'],
      center: ['50', '50%'],
      label: {
        show: true,
        position: 'center',
        fontSize: 28,
        color: theme === 'dark' ? '#d4d4d4' : '#4E5969',
        formatter: function (name: any) {
          return ``
        },
      },
      labelLine: {
        show: false,
      },
      emphasis: {
        itemStyle: {
          borderColor: 'transparent',
        },
      },
      hoverOffset: 0,
      data: [{ value: 0, name: '' }],
    },
  ],
}
const setClusterState = () => {
  let chart = echarts.init(main.value)
  const option = cloneDeep(optionDefault)
  option.title.text = t('clusterMonitor.clusterState')
  let rows = statisticsData?.value?.cluster?.proportion
  option.series[0].data = [
    {
      value: rows?.find((obj) => obj.color === 'GREEN')?.num || 0,
      name: rows?.find((obj) => obj.color === 'GREEN')?.value || '',
    },
    {
      value: rows?.find((obj) => obj.color === 'YELLOW')?.num || 0,
      name: rows?.find((obj) => obj.color === 'YELLOW')?.value || '',
    },
    {
      value: rows?.find((obj) => obj.color === 'RED')?.num || 0,
      name: rows?.find((obj) => obj.color === 'RED')?.value || '',
    },
    {
      value: rows?.find((obj) => obj.color === 'GREY')?.num || 0,
      name: rows?.find((obj) => obj.color === 'GREY')?.value || '',
    },
  ]
  option.legend.formatter = function (name: any) {
    let rows = statisticsData?.value?.cluster?.proportion || []
    const targetData = rows?.find((obj) => obj.value === name)?.num
    return `${name} ${targetData}`
  }
  option.series[0].label.formatter = function (name: any) {
    const total = statisticsData?.value?.cluster.total
    return `${total}`
  }
  chart.setOption(option)
}
const setNodeState = () => {
  let chart = echarts.init(main2.value)
  const option = cloneDeep(optionDefault)
  option.title.text = t('clusterMonitor.nodeState')
  let rows = statisticsData?.value?.nodeStat?.proportion
  option.color = ['#61DDAA', '#F6605A']
  option.series[0].data = [
    {
      value: rows?.find((obj) => obj.color === 'GREEN')?.num || 0,
      name: rows?.find((obj) => obj.color === 'GREEN')?.value || '',
    },
    {
      value: rows?.find((obj) => obj.color === 'RED')?.num || 0,
      name: rows?.find((obj) => obj.color === 'RED')?.value || '',
    },
  ]
  option.legend.formatter = function (name: any) {
    let rows = statisticsData?.value?.nodeStat?.proportion || []
    const targetData = rows?.find((obj) => obj.value === name)?.num
    return `${name} ${targetData}`
  }
  option.series[0].label.formatter = function (name: any) {
    const total = statisticsData?.value?.nodeStat?.total
    return `${total}`
  }
  chart.setOption(option)
}
const setNodeSyncState = () => {
  let chart = echarts.init(main3.value)
  const option = cloneDeep(optionDefault)
  option.color = ['#61DDAA', '#FFA53C', '#F6605A']
  option.title.text = t('clusterMonitor.nodeSyncState')
  let rows = statisticsData?.value?.nodeSyncStat?.proportion
  option.series[0].data = [
    {
      value: rows?.find((obj) => obj.color === 'GREEN')?.num || 0,
      name: rows?.find((obj) => obj.color === 'GREEN')?.value || '',
    },
    {
      value: rows?.find((obj) => obj.color === 'YELLOW')?.num || 0,
      name: rows?.find((obj) => obj.color === 'YELLOW')?.value || '',
    },
    {
      value: rows?.find((obj) => obj.color === 'RED')?.num || 0,
      name: rows?.find((obj) => obj.color === 'RED')?.value || '',
    },
  ]
  option.legend.formatter = function (name: any) {
    let rows = statisticsData?.value?.nodeSyncStat?.proportion || []
    const targetData = rows?.find((obj) => obj.value === name)?.num
    return `${name} ${targetData}`
  }
  option.series[0].label.formatter = function (name: any) {
    const total = statisticsData?.value?.nodeSyncStat?.total
    return `${total}`
  }
  chart.setOption(option)
}

const gotoCluster = (row: any) => {
  indexs.value.clusterOpened = true
  indexs.value.showingComponent = 'cluster'
  nextTick(() => {
    clusterRef.value!.goInto(row)
  })
}
const backToHome = () => {
  indexs.value.showingComponent = 'list'
}
const autoRefreshFn = () => {
  if (indexs.value.showingComponent === 'list') {
    loadStatistics()
    if (dashboardTabKey.value === 'clusterList') {
      loadAllClusters()
    }
    if (dashboardTabKey.value === 'delayList') {
      loadAllNodes()
    }
  }
}
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

const tabChange = (tab: TabPanelName) => {
  if (tab === 'clusterList' && clusterData.value?.length === 0) {
    loadAllClusters()
  }
  if (tab === 'delayList' && nodeData.value?.length === 0) {
    loadAllNodes()
  }
}

const archSelections = computed(() => {
  const result: string[] = Array.from(new Set(clusterData.value?.map((obj: ClusterListItem) => obj.arch)))
  return result
})
const syncWaySelections = computed(() => {
  const result: string[] = Array.from(new Set(nodeData.value?.map((obj: NodeStateItem) => obj.sync)))
  return result
})
const syncStateSelections = computed(() => {
  const result: string[] = Array.from(new Set(nodeData.value?.map((obj: NodeStateItem) => obj.syncState.value)))
  return result
})

const filterClusters = computed(() => {
  return clusterData.value?.filter((obj: ClusterListItem) => {
    let result = true
    if (selectedArch.value !== '' && obj.arch !== selectedArch.value) result = false
    if (searchText.value !== '' && obj.clusterId.indexOf(searchText.value) < 0) result = false
    return result
  })
})

const filterNodes = computed(() => {
  return nodeData.value?.filter((obj: NodeStateItem) => {
    let result = true
    if (selectedSyncMode.value !== '' && obj.sync !== selectedSyncMode.value) result = false
    if (selectedSyncStatus.value !== '' && obj.syncState.value !== selectedSyncStatus.value) result = false
    if (syncSearchText.value !== '' && obj.nodeName.indexOf(syncSearchText.value) < 0) result = false
    return result
  })
})

const {
  data: allClusters,
  run: loadAllClusters,
  loading,
} = useRequest(
  () => {
    clusterData.value = []
    return getAllClusters()
  },
  { manual: true }
)
watch(allClusters, () => {
  if (!allClusters.value) return
  clusterData.value = allClusters.value
  loadAllClustersStates()
})
const { data: allClusterStates, run: loadAllClustersStates } = useRequest(getAllClustersStates, { manual: true })
watch(
  allClusterStates,
  () => {
    if (!allClusterStates.value) return
    allClusterStates.value.forEach((objA) => {
      const matchedObjB = clusterData.value?.find((objB) => objB.clusterId === objA.clusterId)
      if (matchedObjB) {
        matchedObjB.clusterState = objA.clusterState
        matchedObjB.desc = objA.desc
        matchedObjB.nodeId = objA.primaryNodeId
      }
    })
  },
  { deep: true }
)
const {
  data: allNodes,
  run: loadAllNodes,
  loading: loadingNodes,
} = useRequest(
  () => {
    nodeData.value = []
    return getAllNodes()
  },
  { manual: true }
)
watch(
  allNodes,
  () => {
    if (!allNodes.value) return
    nodeData.value = allNodes.value
  },
  { deep: true }
)

// load statistics
const {
  data: getStatisticsData,
  run: loadStatistics,
  loading: loadingStatistics,
} = useRequest(
  () => {
    clusterData.value = []
    return getStatistics()
  },
  { manual: true }
)
watch(getStatisticsData, () => {
  statisticsData.value = undefined
  if (!getStatisticsData.value) return
  statisticsData.value = getStatisticsData.value
  setClusterState()
  setNodeState()
  setNodeSyncState()
})
</script>

<style scoped lang="scss">
.statistics {
  display: flex;
  flex-direction: row;
  .item {
    width: 50%;
    .title {
      font-size: 14px;
      font-weight: bold;
    }
    .row {
      display: flex;
      flex-direction: row;
      justify-content: space-around;
      align-items: center;
    }
  }
}
.top5 {
  .bottom-title {
    font-size: 14px;
    font-weight: bold;
    line-height: 22px;
    text-align: center;
    margin-top: 2px;
  }
  .item {
    .name {
      font-size: 10px;
      line-height: 14px;
    }
    .ratio-row {
      display: flex;
      flex-direction: row;
      align-items: center;
      .ratio-container {
        flex-shrink: 0;
        width: 160px;
        position: relative;
        .ratio {
          z-index: 10;
          display: flex;
          width: 100%;
          height: 5px;
          align-items: flex-start;
          gap: 7.463px;
          flex-shrink: 0;
          border-radius: 74.627px;
          background: linear-gradient(143deg, #86a7fb 5.36%, #4678f7 94.64%);
          &.background {
            position: absolute;
            top: 0px;
            width: 100%;
            z-index: 9;
            border-radius: 74.627px;
            opacity: 0.2;
            background: linear-gradient(143deg, #86a7fb 5.36%, #4678f7 94.64%),
              linear-gradient(143deg, #ffbf1a 5.36%, #ff4080 94.64%);
          }
        }
      }
      .value {
        font-size: 10px;
        line-height: 14px;
        margin-left: 12px;
        text-wrap: nowrap;
      }
    }
  }
}
</style>
