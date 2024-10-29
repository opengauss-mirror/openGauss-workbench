<template>
  <div class="log-center-container" id="logCenter">
    <div class="flex-row">
      <div class="label-color query-label mr-s">{{ $t('logCenter.index.5mpllm8hjao0') }}</div>
      <a-select class="select-w mr-lg" style="width: 200px" :loading="data.clusterListLoading" v-model="data.clusterId"
        :placeholder="$t('logCenter.index.5mpllm8hk7o0')" @change="getHostList">
        <a-option v-for="(item, index) in data.clusterList" :key="index" :label="item.label" :value="item.value" />
      </a-select>
      <div class="label-color query-label mr-s">{{ $t('logCenter.index.else1') }}</div>
      <a-select class="select-w mr-lg" style="width: 200px" :loading="data.hostListLoading" v-model="data.hostId"
        :placeholder="$t('logCenter.index.5mpllm8hkk00')">
        <a-option v-for="(item, index) in data.hostList" :key="index" :label="item.label" :value="item.value" />
      </a-select>
      <a-button type="outline" @click="getLogPath">
        <template #icon>
          <icon-search />
        </template>
        <template #default>{{ $t('logCenter.index.5mpllm8hkog0') }}</template>
      </a-button>
    </div>
    <a-divider />
    <a-tabs class="log-content" type="card-gutter">
      <a-tab-pane key="1" :title="$t('logCenter.index.5mpllm8hks40')">
        <file-log :root-path="data.pathData.systemLogPath" :host-id="data.hostId" />
      </a-tab-pane>
      <a-tab-pane key="2" :title="$t('logCenter.index.5mpllm8hkwk0')">
        <file-log :root-path="data.pathData.opLogPath" :host-id="data.hostId" />
      </a-tab-pane>
      <a-tab-pane key="3" :title="$t('logCenter.index.5mpllm8hl000')">
        <audit-log :cluster-id="data.clusterId" :host-id="data.hostId" />
      </a-tab-pane>
      <a-tab-pane key="4" :title="$t('logCenter.index.5mpllm8hl3w0')">
        <file-log :root-path="data.pathData.performanceLogPath" :host-id="data.hostId" />
      </a-tab-pane>
      <a-tab-pane key="5" :title="$t('logCenter.index.5mpllm8hl7w0')">
        <file-log :root-path="data.pathData.dumpLogPath" :host-id="data.hostId" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script lang="ts" setup>

import { KeyValue } from '@/types/global'
import { onMounted, reactive } from 'vue'
import FileLog from './components/FileLog.vue'
import { clusterList, getHostByClusterId, logPath } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import AuditLog from './components/AuditLog.vue'

const data: {
  clusterId: string,
  hostId: string,
  clusterListLoading: boolean,
  clusterList: KeyValue[],
  hostListLoading: boolean,
  hostList: KeyValue[],
  pathData: KeyValue
} = reactive({
  clusterId: '',
  hostId: '',
  clusterListLoading: false,
  clusterList: [],
  hostListLoading: false,
  hostList: [],
  pathData: {
    dumpLogPath: '',
    opLogPath: '',
    performanceLogPath: '',
    systemLogPath: ''
  }
})

onMounted(async () => {
  await getClusterList()
  await getHostList()
  getLogPath()
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
    } else resolve(false)
  }).finally(() => {
    data.clusterListLoading = false
  })
})

const getHostList = () => new Promise(resolve => {
  if (data.clusterId) {
    const param = {
      clusterId: data.clusterId
    }
    data.hostListLoading = true
    getHostByClusterId(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        resolve(true)
        data.hostList = []
        res.data.forEach((item: KeyValue) => {
          data.hostList.push({
            label: `${item.privateIp}(${item.hostname})`,
            value: item.hostId
          })
        })
        data.hostId = data.hostList[0].value
      } else resolve(false)
    }).finally(() => {
      data.hostListLoading = false
    })
  }
})

const getLogPath = () => {
  if (data.clusterId && data.hostId) {
    const param = {
      clusterId: data.clusterId,
      hostId: data.hostId
    }
    data.hostId = param.hostId
    logPath(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Object.assign(data.pathData, res.data)
      } else {
        Message.error('Error obtaining log path')
      }
    }).catch(() => {
      Message.error('Error obtaining log path')
    })
  }
}

</script>

<style lang="less" scoped>
.log-center-container {
  padding: 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;

  .query-label {
    white-space: nowrap;
  }

  .select-w {
    width: 200px;
  }

  .log-content {
    height: 100%;
  }
}

:deep(.arco-tabs-content) {
  height: calc(100% - 32px);
  overflow: auto;
}
</style>
