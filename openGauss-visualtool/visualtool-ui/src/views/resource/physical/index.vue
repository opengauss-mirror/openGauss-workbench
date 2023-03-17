<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="physical-list">
        <div class="flex-between mb">
          <div>
            <a-button type="primary" class="mr" @click="handleAddHost('create')">
              <template #icon>
                <icon-plus />
              </template>
              {{ $t('physical.index.5mphf11rr080') }}
            </a-button>
          </div>
          <div>
            <a-form :model="filter" layout="inline">
              <a-form-item field="publicIp" :label="$t('physical.index.ipAddress')">
                <a-input v-model="filter.name" allow-clear :placeholder="$t('physical.index.ipAddressPlaceholder')"
                  style="width: 180px;"></a-input>
              </a-form-item>
              <a-form-item field="hostLabel" :label="$t('physical.index.hostLabel')">
                <a-select style="width: 180px;" :loading="data.tagsLoading" v-model="filter.tagIds"
                  :placeholder="$t('physical.index.hostLabelPlaceholder')" multiple allow-clear>
                  <a-option v-for="item in data.tagsList" :key="item.value" :value="item.value">{{
                    item.label
                  }}</a-option>
                </a-select>
              </a-form-item>
              <a-form-item field="os" :label="$t('physical.index.os')">
                <a-select v-model="filter.os" allow-clear :placeholder="$t('physical.index.osPlaceholder')"
                  style="width: 150px;">
                  <a-option value="openEuler">openEuler</a-option>
                  <a-option value="centOS">centos</a-option>
                </a-select>
              </a-form-item>
              <a-form-item>
                <a-button type="outline" @click="getListData()">
                  <template #icon>
                    <icon-search />
                  </template>
                  <template #default>{{ $t('physical.index.5mphf11szdk0') }}</template>
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-table class="d-a-table-row" row-key="hostId" :data="list.data" :columns="columns" :pagination="list.page"
          :row-selection="list.rowSelection" @page-change="currentPage" @page-size-change="pageSizeChange"
          :loading="list.loading">
          <template #baseInfo="{ record }">
            <div class="flex-col-start">
              <div class="host-name">{{ record.name ? record.name : '--' }}</div>
              <div class="flex-row">
                <div class="mr-s">{{ $t('physical.index.privateIp') }}:</div>
                <div>{{ record.privateIp }}</div>
              </div>
              <div class="flex-row">
                <div class="mr-s">{{ $t('physical.index.publicIp') }}:</div>
                <div style="max-width: 140px;" class="mr-s">{{ record.publicIp }}</div>
                <icon-code-square :size="25" style="cursor: pointer;" @click="showTerminal(record)" />
              </div>
              <div class="flex-row">
                <div class="mr-s">{{ $t('physical.index.cpuArch') }}:</div>
                <div>{{ record.cpuArch ? record.cpuArch : '--' }}</div>
              </div>
              <div class="flex-row">
                <div class="mr-s">{{ $t('physical.index.os') }}:</div>
                <div>{{ record.os ? record.os : '--' }}</div>
              </div>
              <div class="flex-row">
                <div class="mr-s">{{ $t('physical.index.hostName') }}:</div>
                <div>{{ record.hostname ? record.hostname : '--' }}</div>
              </div>
            </div>
          </template>
          <template #hostLabel="{ record }">
            <div class="flex-row" style="flex-wrap: wrap;">
              <a-tag v-for="(item, index) in record.tags" :key="index" class="mr-s mb-s">
                <div :title="item" style="max-width: 125px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap">
                  {{ item
                  }}</div>
              </a-tag>
            </div>
          </template>
          <template #diskInfo="{ record }">
            <div class="flex-row">
              <div class="flex-col mr" style="width: 160px;">
                <div class="net flex-row-center">
                  <div class="flex-col mr" style="width: 80px;">
                    <icon-arrow-fall :size="25" class="mb" />
                    <div>{{ record.downSpeed ? record.downSpeed : '--' }} byte/s</div>
                  </div>
                  <div class="flex-col" style="width: 80px;">
                    <icon-arrow-rise :size="25" class="mb" />
                    <div>{{ record.upSpeed ? record.upSpeed : '--' }} byte/s</div>
                  </div>
                </div>
                <div>{{ $t('physical.index.net') }}</div>
              </div>
              <div class="flex-col mr">
                <v-chart class="chart" :option="record.cpuOption" />
                <div>{{ $t('physical.index.cpu') }}</div>
              </div>
              <div class="flex-col mr">
                <v-chart class="chart" :option="record.memoryOption" />
                <div>{{ $t('physical.index.memory') }}</div>
              </div>
              <div class="flex-col mr">
                <v-chart class="chart" :option="record.diskOption" />
                <div>{{ $t('physical.index.disk') }}</div>
              </div>
            </div>
          </template>
          <template #operation="{ record }">
            <div class="flex-row-start">
              <a-link class="mr" @click="showHostUserMng(record)">{{ $t('physical.index.5mphf11szks0') }}</a-link>
              <!-- <a-link class="mr" @click="handleTest(record)">{{ $t('physical.index.5mphf11syw80') }}</a-link> -->
              <a-link class="mr" @click="handleShowDetail(record)">{{ $t('physical.index.detail') }}</a-link>
              <a-link class="mr" @click="handleAddHost('update', record)">{{
                $t('physical.index.5mphf11szqo0')
              }}</a-link>
              <a-popconfirm :content="$t('physical.index.5mphf11szws0')" type="warning"
                :ok-text="$t('physical.index.5mphf11t05c0')" :cancel-text="$t('physical.index.5mphf11t0bc0')"
                @ok="deleteRows(record)">
                <a-link status="danger">{{ $t('physical.index.5mphf11t0hc0') }}</a-link>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
        <add-host ref="addHostRef" @finish="getListData"></add-host>
        <host-pwd-dlg ref="hostPwdRef" @finish="handleShowTerminal($event)"></host-pwd-dlg>
        <host-user-mng ref="hostUserRef"></host-user-mng>
        <host-terminal ref="hostTerminalRef"></host-terminal>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { hostPage, delHost, hostMonitor, hostTagListAll } from '@/api/ops' // eslint-disable-line
import AddHost from './components/AddHost.vue'
import HostPwdDlg from './components/HostPwdDlg.vue'
import HostTerminal from './components/HostTerminal.vue'
import HostUserMng from './components/HostUserMng.vue'
import { cpuOption, memoryOption, diskOption } from './echarts/option'
import VChart from 'vue-echarts'
import Socket from '@/utils/websocket'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const filter = reactive({
  name: '',
  tagIds: [],
  os: '',
  pageNum: 1,
  pageSize: 10
})

const columns = computed(() => [
  { title: t('physical.index.baseInfo'), slotName: 'baseInfo', width: 350 },
  { title: t('physical.index.hostLabel'), slotName: 'hostLabel' },
  { title: t('physical.index.diskInfo'), slotName: 'diskInfo' },
  { title: t('physical.index.5mphf11tfjw0'), slotName: 'operation' }
])

const echartsOption = reactive<KeyValue>({
  cpuOption: cpuOption,
  memoryOption: memoryOption,
  diskOption: diskOption
})

const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    'show-total': true,
    'show-jumper': true,
    'show-page-size': true
  },
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true
  },
  loading: false,
  tagsLoading: false,
  tagsList: [],
  socketArr: []
})

const data = reactive<KeyValue>({
  hasTestObj: {}
})

onMounted(() => {
  data.hasTestObj = {}
  getListData()
  getAllTag()
})

const getAllTag = () => {
  data.tagsLoading = true
  hostTagListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.tagsList = []
      res.data.forEach((item: KeyValue) => {
        const temp = {
          label: item.name,
          value: item.hostTagId
        }
        data.tagsList.push(temp)
      })
    }
  }).finally(() => {
    data.tagsLoading = false
  })
}

const getListData = () => new Promise(resolve => {
  list.loading = true
  const param = JSON.parse(JSON.stringify(filter))
  param.tagIds = filter.tagIds.toString()
  hostPage(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      list.data = []
      res.rows.forEach((item: KeyValue, index: number) => {
        item.cpuOption = JSON.parse(JSON.stringify(echartsOption.cpuOption))
        item.memoryOption = JSON.parse(JSON.stringify(echartsOption.memoryOption))
        item.diskOption = JSON.parse(JSON.stringify(echartsOption.diskOption))
        if (item.isRemember) {
          openHostMonitor(item, index)
        }
        list.data.push(item)
      })
      list.page.total = res.total
    } else resolve(false)
  }).finally(() => {
    list.loading = false
  })
})

const openHostMonitor = (hostData: KeyValue, index: number) => {
  const socketKey = new Date().getTime()
  const param = {
    businessId: 'monitor_ops_host_' + hostData.hostId + '_' + socketKey
  }
  const websocket = new Socket({ url: `COMMAND_EXEC/${param.businessId}` })
  websocket.onopen(() => {
    list.data[index].loading = true
    const monitorParam = {
      hostId: hostData.hostId,
      businessId: param.businessId,
      rootPassword: ''
    }
    hostMonitor(monitorParam).then((res: KeyValue) => {
      console.log('show monitor', res)
      if (Number(res.code) !== 200) {
        list.data[index].state = 0
        websocket.destroy()
      } else {
        if (res.data.res) {
          list.data[index].state = 1
          // websocket push socketArr
          list.socketArr.push(websocket)
        } else {
          list.data[index].state = 0
          websocket.destroy()
        }
      }
    }).catch(() => {
      console.log('show monitor error')
      list.data[index].state = 0
      websocket.destroy()
    }).finally(() => {
      list.data[index].loading = false
    })
  })
  websocket.onclose(() => {
    list.data[index].state = 0
  })
  websocket.onmessage((messageData: any) => {
    const eventData = JSON.parse(messageData)
    console.log('get host monitor data', eventData)
    list.data[index].downSpeed = eventData.downSpeed
    list.data[index].upSpeed = eventData.upSpeed
    list.data[index].cpuOption.series[0].data[0] = eventData.cpu / 100
    list.data[index].cpuOption.series[0].data[1] = 1 - eventData.cpu / 100
    list.data[index].diskOption.series[0].data[0] = eventData.disk / 100
    list.data[index].diskOption.series[0].data[1] = 1 - eventData.disk / 100
    list.data[index].memoryOption.series[0].data[0] = eventData.memory / 100
    list.data[index].memoryOption.series[0].data[1] = 1 - eventData.memory / 100
  })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData()
}

const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)
const showTerminal = (hostData: KeyValue) => {
  // isRemember password
  if (hostData.isRemember) {
    // showTerminal
    handleShowTerminal({
      hostId: hostData.hostId,
      port: hostData.port,
      ip: hostData.publicIp
    })
  } else {
    // show pwdDlg
    hostPwdRef.value?.open(hostData)
  }
}

const hostTerminalRef = ref<null | InstanceType<typeof HostTerminal>>(null)
const handleShowTerminal = (hostData: KeyValue) => {
  console.log('show password', hostData)
  hostTerminalRef.value?.open(hostData)
}
const handleShowDetail = (record: KeyValue) => {
  console.log('show detail', record)
}

const addHostRef = ref<null | InstanceType<typeof AddHost>>(null)
const handleAddHost = (type: string, data?: KeyValue) => {
  addHostRef.value?.open(type, data)
}

// const currRecord = ref<KeyValue>({})
const deleteRows = (record: KeyValue) => {
  delHost(record.hostId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'delete success'
      })
      getListData()
    }
  })
}

// const getStateColor = (state: number) => {
//   switch (state) {
//     case -1:
//       return 'gray'
//     case 0:
//       return 'red'
//     case 1:
//       return 'green'
//   }
// }

// const getStateDesc = (state: number) => {
//   switch (state) {
//     case -1:
//       return t('physical.index.5mphf11tfr80')
//     case 0:
//       return t('physical.index.5mphf11tfx80')
//     case 1:
//       return t('physical.index.5mphf11tg780')
//   }
// }

const hostUserRef = ref<null | InstanceType<typeof HostUserMng>>(null)
const showHostUserMng = (record: KeyValue) => {
  hostUserRef.value?.open(record)
}

</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .physical-list {
      padding: 20px;
    }

    .host-name {
      font-size: 18px;
      font-weight: bold;
    }

    .net {
      height: 120px;
    }

    .chart {
      width: 120px;
      height: 120px;
    }
  }
}
</style>
