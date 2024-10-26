<template>
  <div class="cluster-monitor-container" id="monitor">
    <div class="top full-w mb">
      <div class="label-color ft-b ft-m">{{ $t('monitor.index.5mplmn5z06k0') }}
        <span style="color: grey; font-size: 12px" v-if="data.tip !==''">
           {{ $t('monitor.index.5mplmn5z41w0') }}{{data.tip}}
        </span>
      </div>
      <div class="flex-row">
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">{{ $t('monitor.index.5mplmn5z19w0') }}</div>
          <a-select
            class="select-w"
            :loading="data.clusterListLoading"
            v-model="data.clusterId"
            :placeholder="$t('monitor.index.5mplmn5z1lo0')"
            @change="getHostList"
          >
            <a-option
              v-for="(item, index) in data.clusterList"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </a-select>
        </div>
        <div class="flex-row">
          <div class="label-color top-label mr-s">{{ $t('monitor.index.5mplmn5z1qs0') }}</div>
          <a-select
            class="select-w"
            :loading="data.hostListLoading"
            v-model="data.hostId"
            :placeholder="$t('monitor.index.5mplmn5z1uc0')"
            @change="hostChange"
          >
            <a-option
              v-for="(item, index) in data.hostList"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </a-select>
        </div>
      </div>
    </div>
    <div class="flex-row mb">
      <div class="echart-container cpu-bg mr">
        <div class="label-color ft-b ft-lg mb">{{ $t('monitor.index.else1') }}</div>
        <v-chart
          class="echart"
          :option="data.cpu"
        />
      </div>
      <div class="echart-container memory-bg mr">
        <div class="label-color ft-b ft-lg mb">{{ $t('monitor.index.5mplmn5z1y80') }}</div>
        <v-chart
          class="echart"
          :option="data.memory"
        />
      </div>
      <div class="echart-container net-bg">
        <div class="label-color ft-b ft-lg mb">{{ $t('monitor.index.5mplmn5z2300') }}</div>
        <v-chart
          class="echart"
          :option="data.net"
        />
      </div>
    </div>
    <div class="flex-row mb">
      <div class="echart-container lock-bg mr">
        <div class="label-color ft-b ft-lg mb">{{ $t('monitor.index.5mplmn5z2a00') }}</div>
        <v-chart
          class="echart"
          :option="data.lock"
        />
      </div>
      <div class="echart-container session-bg mr">
        <div class="label-color ft-b ft-lg mb">{{ $t('monitor.index.5mplmn5z2ms0') }}</div>
        <v-chart
          class="echart"
          :option="data.session"
        />
      </div>
      <div class="echart-container connect-bg">
        <div class="label-color ft-b ft-lg mb">{{ $t('monitor.index.5mplmn5z30k0') }}</div>
        <v-chart
          class="echart"
          :option="data.connect"
        />
      </div>
    </div>
    <div class="session-top-ten-c">
      <div class="label-color ft-lg ft-b mb">{{ $t('monitor.index.5mplmn5z37w0') }}
        <span style="color: grey; font-size: 12px" v-if="data.sessionTop.series[0].data[0].value === 0">
         {{$t('monitor.index.5mplmn5z3e90')}}
        </span>
      </div>
      <v-chart
        class="echart-sesion"
        v-if="data.sessionTop.series[0].data[0].value !== 0"
        :option="data.sessionTop"
      ></v-chart>
    </div>
  </div>
</template>

<script lang="ts" setup>

import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import VChart from 'vue-echarts'
import { cpuOption, lockOption, sessionOption, netOption, connectOption, memoryOption, sessionTopOption } from './echarts/option'
import { clusterMonitor, clusterList, getHostByClusterId } from '@/api/ops'
import Socket from '@/utils/websocket'
import { Message } from '@arco-design/web-vue'

const data = reactive<KeyValue>({
  cpu: cpuOption,
  lock: lockOption,
  session: sessionOption,
  net: netOption,
  connect: connectOption,
  memory: memoryOption,
  sessionTop: sessionTopOption,
  clusterId: '',
  hostId: '',
  clusterListLoading: false,
  clusterList: [],
  hostListLoading: false,
  hostList: [],
  tip: ''
})

const instanceWebSocket = ref<Socket<any, any> | undefined>()

onMounted(() => {
  getClusterList()
})

onBeforeUnmount(() => {
  instanceWebSocket.value?.destroy()
})

const getClusterList = () => new Promise(resolve => {
  data.clusterListLoading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      res.data.forEach((item: KeyValue) => {
        data.clusterList.push({
          label: item.clusterId,
          value: item.clusterId
        })
      })
      data.clusterId = data.clusterList[0].value
      getHostList()
    } else resolve(false)
  }).finally(() => {
    data.clusterListLoading = false
  })
})

const getHostList = () => {
  if (data.clusterId) {
    const param = {
      clusterId: data.clusterId
    }
    data.hostListLoading = true
    getHostByClusterId(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        data.hostList = []
        res.data.forEach((item: KeyValue) => {
          data.hostList.push({
            label: `${item.privateIp}(${item.hostname})`,
            value: item.hostId
          })
        })
        data.hostId = data.hostList[0].value
        openWebSocket()
      } else {
        Message.error('Failed to obtain host information')
      }
    }).finally(() => {
      data.hostListLoading = false
    })
  }
}

const hostChange = () => {
  data.cpu.xAxis.data = []
  data.cpu.series[0].data = []
  data.memory.xAxis.data = []
  data.memory.series[0].data = []
  data.lock.xAxis.data = []
  data.lock.series[0].data = []
  data.session.xAxis.data = []
  data.session.series[0].data = []
  data.connect.xAxis.data = []
  data.connect.series[0].data = []
  data.net.xAxis.data = []
  data.net.series[0].data = []
  data.net.series[1].data = []
  data.sessionTop.yAxis.data = []
  data.sessionTop.series[0].data = []
  openWebSocket()
}

const openWebSocket = () => {
  instanceWebSocket.value?.destroy()
  const socketKey = new Date().getTime()
  instanceWebSocket.value = new Socket({ url: `monitor_instance_data_${socketKey}` })
  instanceWebSocket.value.onopen(() => {
    const param = {
      clusterId: data.clusterId,
      hostId: data.hostId,
      businessId: `monitor_instance_data_${socketKey}`
    }
    clusterMonitor(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        instanceWebSocket.value?.destroy()
      }
      if (res.msg != null) {
        data.tip = res.msg
      }
    }).catch(() => {
      instanceWebSocket.value?.destroy()
    })
  })
  instanceWebSocket.value.onmessage((messageData: any) => {
    const eventData = JSON.parse(messageData)
    console.log('monitor websocket data', eventData)
    if (data.cpu.xAxis.data.length > 10) {
      data.cpu.xAxis.data.splice(0, 1)
      data.cpu.series[0].data.splice(0, 1)
      data.memory.xAxis.data.splice(0, 1)
      data.memory.series[0].data.splice(0, 1)
      data.lock.xAxis.data.splice(0, 1)
      data.lock.series[0].data.splice(0, 1)
      data.session.xAxis.data.splice(0, 1)
      data.session.series[0].data.splice(0, 1)
      data.connect.xAxis.data.splice(0, 1)
      data.connect.series[0].data.splice(0, 1)
      data.net.xAxis.data.splice(0, 1)
      data.net.series[0].data.splice(0, 1)
      data.net.series[1].data.splice(0, 1)
    }
    const xdata = convertToDate(eventData.time)
    data.cpu.xAxis.data.push(xdata)
    data.cpu.series[0].data.push(eventData.cpu)
    data.memory.xAxis.data.push(xdata)
    data.memory.series[0].data.push(eventData.memory)
    data.lock.xAxis.data.push(xdata)
    data.lock.series[0].data.push(eventData.lock)
    data.session.xAxis.data.push(xdata)
    data.session.series[0].data.push(eventData.session)
    data.connect.xAxis.data.push(xdata)
    data.connect.series[0].data.push(eventData.connectNum)
    data.net.xAxis.data.push(xdata)
    data.net.series[0].data.push(eventData.net[0].receive / 1024 / 1024 / 1024)
    data.net.series[1].data.push(eventData.net[0].transmit / 1024 / 1024 / 1024)

    data.sessionTop.yAxis.data = []
    eventData.sessionMemoryTop10.forEach((item: KeyValue, index: number) => {
      for (let key in item) {
        data.sessionTop.yAxis.data.push(key)
        data.sessionTop.series[0].data[9 - index].value = item[key]
      }
    })
  })
}

const convertToDate = (time: number) => {
  const date = new Date(time)
  return date.getMinutes() + ':' + date.getSeconds()
}

</script>

<style lang="less" scoped>
.cluster-monitor-container {
  padding: 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  box-sizing: border-box;

  .top {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .top-label {
      white-space: nowrap;
    }

    .select-w {
      width: 200px;
    }
  }

  .echart-container {
    width: 33.3%;
    border-radius: 8px;
    padding: 20px;
    height: 356px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }

  .echart {
    width: 100%;
    height: 100%;
  }

  .cpu-bg {
    background-color: var(--cpu-bg);
  }

  .memory-bg {
    background-color: var(--memory-bg);
  }

  .net-bg {
    background-color: var(--net-bg);
  }

  .lock-bg {
    background-color: var(--lock-bg);
  }

  .session-bg {
    background-color: var(--session-bg);
  }

  .connect-bg {
    background-color: var(--connect-bg);
  }

  .session-top-ten-c {
    padding: 20px;
    border-radius: 4px;
    background-color: var(--session-top-bg);

    .echart-sesion {
      height: 700px;
      width: 100%;
    }
  }

}
</style>
