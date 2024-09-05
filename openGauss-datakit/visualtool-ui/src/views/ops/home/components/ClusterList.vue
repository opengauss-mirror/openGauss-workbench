<template>
  <div class="cluster-list-c">
    <a-spin
      class="full-w"
      :loading="data.loading"
      :tip="$t('components.ClusterList.5mpinxl8f0w0')"
    >
      <div v-if="data.clusterList.length">
        <div
          class="mb"
          v-for="(clusterData, index) in data.clusterList"
          :key="index"
        >
          <div class="cluster-item flex-col-start">
            <div class="flex-between flex-row full-w">
              <div class="flex-row">
                <div class="mr cluster-item-title">{{ getVersionName(clusterData.version) }}</div>
                <div class="mr cluster-item-title">{{ clusterData.versionNum }}</div>
                <div class="cluster-item-title">{{ clusterData.clusterId ? clusterData.clusterId : '--' }}</div>
              </div>
              <icon-loading v-if="clusterData.isConnected === -1" />
              <a-tag
                color="green"
                v-else-if="clusterData.isConnected === 1"
              >{{
                $t('components.ClusterList.else14')
              }}</a-tag>
              <a-link
                v-else
                @click="retryOpenWebSocket(clusterData, index)"
              >{{
                $t('components.ClusterList.else15')
              }}</a-link>
              <!-- <a-tag v-else color="red">{{ $t('components.ClusterList.5mpinxl8frs0') }}</a-tag> -->
            </div>
            <a-divider />
            <div
              v-if="clusterData.version === 'MINIMAL_LIST' && clusterData.deployType === 'CLUSTER'"
              class="flex-row full-w"
            >
              <!-- There is only a minimal version and it is one master and one backup -->
              <div
                class="flex-col-start"
                style="width: 50%"
              >
                <div
                  class="full-w mb-lg"
                  style="display: flex;"
                >
                  <div
                    class="mr-lg"
                    style="width: 50%"
                    v-for="(instance, index) in clusterData.clusterNodes"
                    :key="index"
                  >
                    <div
                      class="instance-c"
                      @click="goPlugin(instance)"
                    >
                      <div class="flex-row mb">
                        <a-tag
                          class="mr-s"
                          color="#E41D1D"
                        >{{ getRoleName(instance.clusterRole) }}</a-tag>
                        <div class="mr-s instance-c-title">{{ $t('components.ClusterList.else1') }}{{ index + 1 }}</div>
                        <a-tag :color="getInstanceStateColor(clusterData, clusterData.clusterNodes[0])">{{
                          getInstanceState(clusterData, clusterData.clusterNodes[0])
                        }}</a-tag>
                      </div>
                      <div class="flex-row-center">
                        <svg-icon
                          icon-class="ops-instance"
                          class="mr"
                          style="width: 50px; height: 50px;"
                        ></svg-icon>
                        <div class="flex-col-start">
                          <div class="mb-s txt">{{ $t('components.ClusterList.else2') }}: {{
                            clusterData.clusterNodes[0].session ?
                            clusterData.clusterNodes[0].session : '--'
                          }}{{ $t('components.ClusterList.else6') }}</div>
                          <div class="mb-s txt">{{ $t('components.ClusterList.else3') }}: {{
                            clusterData.clusterNodes[0].connectNum ?
                            clusterData.clusterNodes[0].connectNum : '--'
                          }}{{ $t('components.ClusterList.else6') }}</div>
                          <div class="mb-s txt">{{ $t('components.ClusterList.else4') }}: {{
                            clusterData.clusterNodes[0].lock
                            ? clusterData.clusterNodes[0].lock :
                            '--'
                          }}{{ $t('components.ClusterList.else6') }}</div>
                          <div class="mb-s txt">{{ $t('components.ClusterList.else5') }}: {{
                            clusterData.clusterNodes[0].azName ?
                            clusterData.clusterNodes[0].azName : '--'
                          }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div
                  class="full-w"
                  style="display: flex; justify-content: center"
                >
                  <svg-icon
                    icon-class="ops-host"
                    class="host-icon-size mr"
                  ></svg-icon>
                  <div class="flex-row">
                    <div class="flex-col-start mr">
                      <div class="mb-s">{{ $t('components.ClusterList.else7') }}:</div>
                      <div class="mb">{{
                        clusterData.clusterNodes[0].privateIp ? clusterData.clusterNodes[0].privateIp
                        :
                        '--'
                      }}</div>
                      <div class="mb-s">{{ $t('components.ClusterList.else8') }}:</div>
                      <div class="mb-s">{{
                        clusterData.clusterNodes[0].publicIp ? clusterData.clusterNodes[0].publicIp :
                        '--'
                      }}</div>
                    </div>
                    <div class="flex-col-start">
                      <div class="mb-s">{{ $t('components.ClusterList.else9') }}:</div>
                      <div class="mb">{{
                        clusterData.clusterNodes[0].hostname ? clusterData.clusterNodes[0].hostname :
                        '--'
                      }}</div>
                      <div class="mb-s">{{ $t('components.ClusterList.else10') }}:</div>
                      <div class="mb-s">{{
                        clusterData.clusterNodes[0].kernel ? clusterData.clusterNodes[0].kernel : '-'
                      }}{{ $t('components.ClusterList.else11') }}{{
  clusterData.clusterNodes[0].memorySize ?
  clusterData.clusterNodes[0].memorySize : '-'
}}G
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div
                class="flex-col-start chart-con"
                style="width: 50%; padding: 12px 17px;"
              >
                <div class="flex-between full-w mb-s">
                  <div class="flex-row chart-con-title">{{ $t('components.ClusterList.else9') }}: {{
                    clusterData.clusterNodes[0].hostname
                  }}</div>
                  <div class="flex-row chart-con-title">{{ $t('components.ClusterList.else12') }}: {{
                    clusterData.clusterNodes[0].azAddress ?
                    clusterData.clusterNodes[0].azAddress : '--'
                  }}</div>
                </div>
                <div class="flex-between full-w">
                  <div class="host-info-c mr">
                    <v-chart
                      class="echart"
                      :option="clusterData.clusterNodes[0].cpuOption"
                    />
                    <div class="txt">{{ $t('components.ClusterList.else13') }}</div>
                  </div>
                  <div class="host-info-c">
                    <v-chart
                      class="echart"
                      :option="clusterData.clusterNodes[0].memoryOption"
                    />
                    <div class="txt">{{ $t('components.ClusterList.5mpinxl8g2c0') }}</div>
                  </div>
                </div>
                <div class="flex-between full-w">
                  <div class="host-info-c mr">
                    <v-chart
                      class="echart"
                      :option="clusterData.clusterNodes[0].netOption"
                    />
                    <div class="txt">{{ $t('components.ClusterList.5mpinxl8gaw0') }}</div>
                  </div>
                  <div class="host-info-c">
                    <v-chart
                      class="echart"
                      :option="clusterData.clusterNodes[0].connectOption"
                    />
                    <div class="txt">{{ $t('components.ClusterList.5mpinxl8gi40') }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div
              v-else
              class="flex-col-start full-w"
            >
              <div
                class="flex-col-start full-w"
                v-for="(instance, index) in clusterData.clusterNodes"
                :key="index"
              >
                <div class="flex-between full-w">
                  <div
                    class="host-instance-c mr"
                    style="width: 50%;"
                  >
                    <div
                      class=" mb-s"
                      style="height: 50%;display: flex; justify-content: space-around; align-items: center;"
                    >
                      <svg-icon
                        icon-class="ops-host"
                        class="host-icon-size mr"
                      ></svg-icon>
                      <div class="flex-row">
                        <div class="flex-col-start mr">
                          <div class="flex-row mb-s">
                            <div class="text-nowrap mr-s in-title">{{ $t('components.ClusterList.else7') }}:</div>
                            <div class="txt">{{ instance.privateIp ? instance.privateIp : '--' }}</div>
                          </div>
                          <div class="flex-row mb-s">
                            <div class="text-nowrap mr-s in-title">{{ $t('components.ClusterList.else8') }}:</div>
                            <div class="txt">{{ instance.publicIp ? instance.publicIp : '--' }}</div>
                          </div>
                          <div class="flex-row mb-s">
                            <div class="text-nowrap mr-s in-title">{{ $t('components.ClusterList.else9') }}:</div>
                            <div class="txt">{{ instance.hostname ? instance.hostname : '--' }}</div>
                          </div>
                          <div class="flex-row">
                            <div class="text-nowrap mr-s in-title">{{ $t('components.ClusterList.else10') }}:</div>
                            <div class="txt">{{ instance.kernel ? instance.kernel : '-' }}{{
                              $t('components.ClusterList.else11')
                            }}{{ instance.memorySize ? instance.memorySize : '-' }}G</div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div
                      class="instance-c full-w"
                      style="height: 50%;"
                      @click="goPlugin(instance)"
                    >
                      <div class="flex-row mb-s">
                        <a-tag
                          class="mr-s"
                          color="#E41D1D"
                        >{{ getRoleName(instance.clusterRole) }}</a-tag>
                        <div class="mr-s instance-c-title">{{ $t('components.ClusterList.else1') }}{{ index + 1 }}</div>
                        <a-tag :color="getInstanceStateColor(clusterData, instance)">{{
                          getInstanceState(clusterData, instance)
                        }}</a-tag>
                      </div>
                      <div
                        class=""
                        style="display: flex; justify-content: space-around; align-items: center;"
                      >
                        <svg-icon
                          icon-class="ops-instance"
                          class="instance-icon-size mr"
                        ></svg-icon>
                        <div class="flex-row">
                          <div class="flex-col-start mr">
                            <div class="flex-row mb-s">
                              <div class="mr-s in-title">{{ $t('components.ClusterList.else2') }}:</div>
                              <div class="text-nowrap txt">{{ instance.session ? instance.session : '--' }}{{
                                $t('components.ClusterList.else6')
                              }}</div>
                            </div>
                            <div class="flex-row mb-s">
                              <div class="text-nowrap mr-s in-title">{{ $t('components.ClusterList.else3') }}:</div>
                              <div class="text-nowrap txt">{{ instance.connectNum ? instance.connectNum : '--' }}{{
                                $t('components.ClusterList.else6')
                              }}</div>
                            </div>
                            <div class="flex-row mb-s">
                              <div class="mr-s in-title">{{ $t('components.ClusterList.else4') }}:</div>
                              <div class="text-nowrap txt">{{ instance.lock ? instance.lock : '--' }}{{
                                $t('components.ClusterList.else6')
                              }}</div>
                            </div>
                            <div class="flex-row">
                              <div class="text-nowrap mr-s in-title">{{ $t('components.ClusterList.else5') }}:</div>
                              <div class="text-nowrap txt">{{ instance.azName ? instance.azName : '--' }}</div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div
                    class="flex-col-start chart-con"
                    style="width: 45%; padding: 12px 17px;"
                  >
                    <div class="flex-between full-w mb-s">
                      <div class="flex-row chart-con-title">
                        <div class="mr-s">{{ $t('components.ClusterList.else9') }}: {{ instance.hostname }}</div>
                      </div>
                      <div class="flex-row chart-con-title">{{ $t('components.ClusterList.else12') }}: {{
                        instance.azAddress ?
                        instance.azAddress : '--'
                      }}</div>
                    </div>
                    <div class="flex-between full-w">
                      <div class="host-info-c mr">
                        <v-chart
                          class="echart"
                          :option="instance.cpuOption"
                        />
                        <div class="txt">{{ $t('components.ClusterList.else13') }}</div>
                      </div>
                      <div class="host-info-c">
                        <v-chart
                          class="echart"
                          :option="instance.memoryOption"
                        />
                        <div class="txt">{{ $t('components.ClusterList.5mpinxl8g2c0') }}</div>
                      </div>
                    </div>
                    <div class="flex-between full-w">
                      <div class="host-info-c mr">
                        <v-chart
                          class="echart"
                          :option="instance.netOption"
                        />
                        <div class="txt">{{ $t('components.ClusterList.5mpinxl8gaw0') }}</div>
                      </div>
                      <div class="host-info-c">
                        <v-chart
                          class="echart"
                          :option="instance.connectOption"
                        />
                        <div class="txt">{{ $t('components.ClusterList.5mpinxl8gi40') }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <a-divider v-if="index !== (clusterData.clusterNodes.length - 1)" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div
        class="empty-c mb"
        v-else
      >
        <svg-icon
          icon-class="ops-empty"
          class="host-icon-size"
        ></svg-icon>
        <div class="content">{{ $t('components.ClusterList.5mpinxl8gpc0') }}</div>
      </div>
    </a-spin>
    <div
      class="create-new"
      v-if="props.hasPlugin"
    >
      <div class="flex-col">
        <svg-icon
          icon-class="ops-cluster"
          class="host-icon-size mb"
        ></svg-icon>
        <a-button
          type="outline"
          size="large"
          @click="$router.push({ name: 'Static-pluginBase-opsOpsInstall' })"
        >{{
          $t('components.ClusterList.5mpinxl8gwg0')
        }}</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { ClusterRoleEnum, OpenGaussVersionEnum, DeployTypeEnum } from '@/types/ops/install' // eslint-disable-line
import { clusterList, clusterMonitor } from '@/api/ops'
import instanceImg from '@/assets/images/ops/instance.png'
import hostImg from '@/assets/images/ops/host.png'
import emptyImg from '@/assets/images/ops/empty.png'
import clusterImg from '@/assets/images/ops/cluster.png'
import { instanceRoute } from '@/api/dashboard'
import { filterAsyncRouter, getQuery } from '@/utils/route'
import router from '@/router'
import Socket from '@/utils/websocket'
import { cpuOption, netOption, connectOption, memoryOption, clearData } from './echarts/option'
import VChart from 'vue-echarts'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
reactive([instanceImg, hostImg, emptyImg, clusterImg])

const echartData: {
  cpu: KeyValue,
  net: KeyValue,
  connect: KeyValue,
  memory: KeyValue
} = reactive({
  cpu: cpuOption,
  net: netOption,
  connect: connectOption,
  memory: memoryOption
})

const data: {
  loading: boolean,
  clusterList: KeyValue[],
  socketArr: Socket<any, any>[]
} = reactive({
  clusterList: [],
  loading: false,
  socketArr: []
})

const props = withDefaults(defineProps<{
  hasPlugin: boolean
}>(), {
  hasPlugin: true
})

onMounted(() => {
  getList()
  getInstanceRoute()
})

onBeforeUnmount(() => {
  if (data.socketArr.length) {
    data.socketArr.forEach((item: Socket<any, any>) => {
      if (item) {
        item.destroy()
      }
    })
  }
})

const insRoute = ref<any>({})

const getInstanceRoute = () => {
  instanceRoute().then((res: any) => {
    insRoute.value = res.data
  })
}

const getList = () => new Promise(resolve => {
  data.loading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      data.clusterList = []
      res.data.forEach((item: KeyValue) => {
        item.isShow = true
        item.isConnected = -1
        if (item.version === 'MINIMAL_LIST' && item.deployType === 'CLUSTER' && item.clusterNodes.length < 2) {
          const slaveNode = JSON.parse(JSON.stringify(item.clusterNodes[0]))
          slaveNode.clusterRole = ClusterRoleEnum.SLAVE
          item.clusterNodes.push(slaveNode)
        }
        // Call the backend and open websocket. The background pushes data for the front end
        data.clusterList.push(item)
      })
      openClusterWebsocket()
    } else resolve(false)
  }).finally(() => {
    data.loading = false
  })
})

const openClusterWebsocket = () => {
  data.clusterList.forEach((item: KeyValue, index: number) => {
    openWebSocket(item, index)
  })
}

const getVersionName = (version: string) => {
  switch (version) {
    case 'MINIMAL_LIST':
      return t('components.ClusterList.5mpinxl8hbk0')
    case 'LITE':
      return t('components.ClusterList.5mpinxl8his0')
    default:
      return t('components.ClusterList.5mpinxl8hqc0')
  }
}

const goPlugin = (instance: KeyValue) => {
  const _route: any = {
    path: insRoute.value?.path,
    name: insRoute.value?.pluginId,
    component: 'DEFAULT_LAYOUT',
    redirect: insRoute.value?.path,
    meta: {
      title: insRoute.value?.menuName,
      requiresAuth: false,
      icon: 'icon-apps',
      order: 3,
      hideChildrenInMenu: true
    },
    children: [
      {
        path: insRoute.value?.path,
        name: insRoute.value?.pluginId,
        component: insRoute.value?.component,
        meta: {
          title: insRoute.value?.menuName,
          requiresAuth: true,
          roles: ['*']
        }
      }
    ]
  }

  const routes = filterAsyncRouter([_route])
  router.addRoute(routes[0])
  const obj: any = {
    id: instance.nodeId
  }
  const query = getQuery(insRoute.value?.queryTemplate)

  console.log(query)
  const queryObj: any = {}
  for (let k in query) {
    const replaceStr = query[k].replace(/[\${}]/g, '') // eslint-disable-line
    queryObj[k] = obj[replaceStr]
  }
  router.push({
    path: insRoute.value?.path,
    query: { ...queryObj, ...insRoute.value?.query }
  })
}
const getRoleName = (type: ClusterRoleEnum) => {
  switch (type) {
    case ClusterRoleEnum.MASTER:
      return t('components.ClusterList.5mpinxl8i0g0')
    case ClusterRoleEnum.SLAVE:
      return t('components.ClusterList.5mpinxl8i7k0')
    default:
      return t('components.ClusterList.5mpinxl8if40')
  }
}

const retryOpenWebSocket = (data: KeyValue, clusterIndex: number) => {
  data.isConnected = -1
  openWebSocket(data, clusterIndex)
}

// Open webSocket
const openWebSocket = (data: KeyValue, clusterIndex: number) => {
  if (data.version === 'MINIMAL_LIST' && data.deployType === 'CLUSTER') {
    // Minimalist version One master and one backup are on the same machine, only need to get the first one of the nodes
    data.clusterNodes[0].cpuOption = JSON.parse(JSON.stringify(echartData.cpu))
    data.clusterNodes[0].memoryOption = JSON.parse(JSON.stringify(echartData.memory))
    data.clusterNodes[0].netOption = JSON.parse(JSON.stringify(echartData.net))
    data.clusterNodes[0].connectOption = JSON.parse(JSON.stringify(echartData.connect))
    openHostWebSocket(data, data.clusterNodes[0], clusterIndex, 0)
  } else {
    data.clusterNodes.forEach((item: KeyValue, index: number) => {
      clearData()
      item.state = -1
      item.cpuOption = JSON.parse(JSON.stringify(echartData.cpu))
      item.memoryOption = JSON.parse(JSON.stringify(echartData.memory))
      item.netOption = JSON.parse(JSON.stringify(echartData.net))
      item.connectOption = JSON.parse(JSON.stringify(echartData.connect))
      // Open websocket connection for each host
      openHostWebSocket(data, item, clusterIndex, index)
    })
  }
}

const openHostWebSocket = (clusterData: KeyValue, nodeData: KeyValue, clusterIndex: number, index: number) => {
  const socketKey = new Date().getTime()
  const param = {
    clusterId: clusterData.clusterId,
    hostId: nodeData.hostId,
    privateIp: nodeData.privateIp,
    businessId: 'monitor_' + clusterData.clusterId + '_' + nodeData.hostId + '_' + socketKey
  }
  const websocket = new Socket({ url: `COMMAND_EXEC/${param.businessId}` })
  websocket.onopen(() => {
    // cluster monitoring
    clusterMonitor(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        data.clusterList[clusterIndex].isConnected = 0
        websocket.destroy()
      } else {
        data.clusterList[clusterIndex].isConnected = 1
        // Store this websocket as socketArr
        data.socketArr.push(websocket)
      }
    }).catch(() => {
      data.clusterList[clusterIndex].isConnected = 0
      websocket.destroy()
    })
  })
  websocket.onclose(() => {
    console.log(`on close cluster: ${data.clusterList[clusterIndex].clusterId}, host: ${data.clusterList[clusterIndex].clusterNodes[index].privateIp}`)
    data.clusterList[clusterIndex].isConnected = 0
  })
  websocket.onmessage((messageData: any) => {
    // console.log(`home page cluster: ${data.clusterList[clusterIndex].clusterId}, host: ${data.clusterList[clusterIndex].clusterNodes[index].privateIp}`, messageData)

    const eventData = JSON.parse(messageData)
    data.clusterList[clusterIndex].clusterNodes[index].state = eventData.state
    // reset instance nodeState and nodeRole
    data.clusterList[clusterIndex].clusterNodes[index].lock = eventData.lock
    data.clusterList[clusterIndex].clusterNodes[index].session = eventData.session
    data.clusterList[clusterIndex].clusterNodes[index].connectNum = eventData.connectNum
    data.clusterList[clusterIndex].clusterNodes[index].kernel = eventData.kernel
    if (eventData.memorySize) {
      if (!isNaN(Number(eventData.memorySize))) {
        data.clusterList[clusterIndex].clusterNodes[index].memorySize = (Number(eventData.memorySize) / 1024 / 1024).toFixed(2)
      }
    }
    assemblyEchartsData(data.clusterList[clusterIndex].clusterNodes[index], eventData)
  })
}

const assemblyEchartsData = (nodeData: KeyValue, eventData: KeyValue) => {
  if (nodeData.cpuOption.series[0].data.length > 10) {
    // nodeData.cpuOption.xAxis.data.splice(0, 1)
    nodeData.cpuOption.series[0].data.splice(0, 1)
    // nodeData.memoryOption.xAxis.data.splice(0, 1)
    nodeData.memoryOption.series[0].data.splice(0, 1)
    // nodeData.connectOption.xAxis.data.splice(0, 1)
    nodeData.connectOption.series[0].data.splice(0, 1)
    // nodeData.netOption.xAxis.data.splice(0, 1)
    nodeData.netOption.series[0].data.splice(0, 1)
    nodeData.netOption.series[1].data.splice(0, 1)
  }
  nodeData.state = eventData.state
  // const xdata = convertToDate(eventData.time)
  // nodeData.cpuOption.xAxis.data.push(xdata)
  nodeData.cpuOption.series[0].data.push(eventData.cpu)
  // nodeData.memoryOption.xAxis.data.push(xdata)
  nodeData.memoryOption.series[0].data.push(eventData.memory)
  // nodeData.connectOption.xAxis.data.push(xdata)
  nodeData.connectOption.series[0].data.push(eventData.connectNum)
  // nodeData.netOption.xAxis.data.push(xdata)
  if (eventData.net[0].receive && eventData.net[0].transmit) {
    nodeData.netOption.series[0].data.push(eventData.net[0].receive / 1024 / 1024 / 1024)
    nodeData.netOption.series[1].data.push(eventData.net[0].transmit / 1024 / 1024 / 1024)
  }
}

const getInstanceState = (clusterData: KeyValue, instanceData: KeyValue) => {
  if (clusterData.version === 'MINIMAL_LIST' || clusterData.version === 'LITE') {
    if (instanceData.state === -1) {
      return t('components.ClusterList.5mpinxl8j000')
    } else {
      if (instanceData.state === 'true') {
        return t('components.ClusterList.5mpinxl8img0')
      } else {
        return t('components.ClusterList.5mpinxl8isw0')
      }
    }
  } else {
    if (instanceData.state === -1) {
      return t('components.ClusterList.5mpinxl8j000')
    } else if (instanceData.state && instanceData.state !== 'false') {
      const stateObj = JSON.parse(instanceData.state)
      return getCurrentInstanceState(stateObj, instanceData)
    } else {
      return t('components.ClusterList.5mpinxl8isw0')
    }
  }
}

const getCurrentInstanceState = (stateObj: KeyValue, instanceData: KeyValue) => {
  const currentHostname = instanceData.hostname
  if (stateObj && currentHostname && stateObj.nodeState) {
    switch (stateObj.nodeState[currentHostname]) {
      case 'Normal':
        return t('components.ClusterList.nodeState1')
      case 'Need repair':
        return t('components.ClusterList.nodeState2')
      case 'Starting':
        return t('components.ClusterList.nodeState3')
      case 'Wait promoting':
        return t('components.ClusterList.nodeState4')
      case 'Promoting':
        return t('components.ClusterList.nodeState5')
      case 'Demoting':
        return t('components.ClusterList.nodeState6')
      case 'Building':
        return t('components.ClusterList.nodeState7')
      case 'Catchup':
        return t('components.ClusterList.nodeState8')
      case 'Coredump':
        return t('components.ClusterList.nodeState9')
      case 'Unknown':
        return t('components.ClusterList.nodeState10')
    }
  }
  return t('components.ClusterList.5mpinxl8j000')
}

const getInstanceStateColor = (clusterData: KeyValue, instanceData: KeyValue) => {
  if (clusterData.version === 'MINIMAL_LIST' || clusterData.version === 'LITE') {
    // true or false
    if (instanceData.state === 'true') {
      return 'green'
    } else {
      return 'red'
    }
  } else {
    if (instanceData.state === -1) {
      return 'gray'
    } else if (instanceData.state && instanceData.state !== 'false') {
      return 'green'
    } else {
      return 'red'
    }
  }
}

</script>

<style lang="less" scoped>
.cluster-list-c {
  .instance-icon-size {
    width: 60px;
    height: 60px;
  }

  .host-icon-size {
    width: 60px;
    height: 60px;
  }

  .cluster-item {
    padding: 15px;
    background-color: var(--color-bg-2);
    border-radius: 2px;

    .cluster-item-title {
      font-size: 18px;
      font-weight: bold;
      color: var(--color-text-1);
    }

    .chart-con {
      background-color: var(--color-fill-2);

      .chart-con-title {
        font-size: 14px;
        color: var(--color-text-1);
      }
    }

    .host-instance-c {
      .in-title {
        font-size: 14px;
        color: var(--color-text-2);
      }

      .txt {
        font-size: 14px;
        color: var(--color-text-1);
      }
    }

    @media screen and (max-width: 1800px) {
      .host-instance-c {
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
      }
    }

    @media screen and (min-width: 1800px) {
      .host-instance-c {
        display: flex;
        align-items: center;
      }
    }

    .instance-c {
      border: 1px dashed rgb(var(--primary-6));
      border-radius: 4px;
      padding: 10px;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      cursor: pointer;

      .instance-c-title {
        font-size: 14px;
        color: var(--color-text-1);
      }
    }

    .host-info-c {
      height: 130px;
      width: 200px;
      display: flex;
      flex-direction: column;
      align-items: center;

      .txt {
        font-size: 14px;
        color: var(--color-text-1);
      }
    }
  }

  .empty-c {
    width: 100%;
    display: flex;
    justify-content: center;
    background-color: var(--color-bg-2);
    border-radius: 2px;
    padding: 15px;

    .content {
      font-weight: bold;
      color: var(--color-neutral-4);
    }
  }

  .create-new {
    width: 100%;
    display: flex;
    justify-content: center;
    background-color: var(--color-bg-2);
    border-radius: 2px;
    padding: 15px;

    .content {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
    }
  }
}

.text-nowrap {
  white-space: nowrap;
}
</style>
