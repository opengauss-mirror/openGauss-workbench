<template>
  <div class="app-container" id="physicalMange">
    <div class="main-bd">
      <div class="physical-list">
        <div class="flex-between mb-s">
          <div style="display:flex; flex-wrap: nowrap;">
            <a-button
              type="primary"
              class="mr"
              @click="handleAddHost('create')"
            >
              <template #icon>
                <icon-plus />
              </template>
              {{ $t('physical.index.5mphf11rr080') }}
            </a-button>
            <a-button
              type="primary"
              class="mr"
              @click="addHostbulk('create')"
            >
              {{ $t('physical.index.5mphf11rr090') }}
            </a-button>
            <a-popconfirm
              :popup-visible="isMultiDelConfirm"
              :content="$t('physical.index.5mphf11szws0')"
              type="warning"
              :ok-text="$t('physical.index.5mphf11t05c0')"
              :cancel-text="$t('physical.index.5mphf11t0bc0')"
              @ok="deleteMultiRows()"
              @cancel="isMultiDelConfirm = false"
              >
              <a-button
                type="primary"
                class="mr"
                @click="deleteSelectedHosts"
              >
                {{ $t('physical.index.5mphf11rr590') }}
              </a-button>
            </a-popconfirm>
            <a-button
              type="primary"
              class="mr"
              @click="handleBatchTag()"
            >
              {{ $t('physical.index.labelBatch') }}
            </a-button>
            <a-button
              type="primary"
              class="mr"
              @click="handleLabelManage"
            >
              {{ $t('physical.index.labelManage') }}
            </a-button>
          </div>
          <div>
            <a-form
              :model="filter"
              layout="inline"
            >
              <a-form-item
                field="publicIp"
                :label="$t('physical.index.ipAddress')"
              >
                <a-input
                  v-model.trim="filter.name"
                  allow-clear
                  :placeholder="$t('physical.index.ipAddressPlaceholder')"
                ></a-input>
              </a-form-item>
              <a-form-item
                field="hostLabel"
                :label="$t('physical.index.hostLabel')"
              >
                <a-select
                  :loading="data.tagsLoading"
                  v-model="filter.tagIds"
                  :placeholder="$t('physical.index.hostLabelPlaceholder')"
                  multiple
                  allow-clear
                >
                  <a-option
                    v-for="item in data.tagsList"
                    :key="item.value"
                    :value="item.value"
                  >{{
                    item.label
                  }}</a-option>
                </a-select>
              </a-form-item>
              <a-form-item
                field="os"
                :label="$t('physical.index.os')"
              >
                <a-select
                  v-model="filter.os"
                  allow-clear
                  :placeholder="$t('physical.index.osPlaceholder')"
                >
                  <a-option value="openEuler">openEuler</a-option>
                  <a-option value="centos">centos</a-option>
                </a-select>
              </a-form-item>
              <a-form-item>
                <a-button
                  type="outline"
                  @click="getListData()"
                >
                  <template #icon>
                    <icon-search />
                  </template>
                  <template #default>{{ $t('physical.index.5mphf11szdk0') }}</template>
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-table
          ref="tableRef"
          row-key="hostId"
          :data="list.data"
          :pagination="list.page"
          :row-selection="list.rowSelection"
          @page-change="currentPage"
          @page-size-change="pageSizeChange"
          :loading="list.loading"
          @selection-change="handleSelectedChange"
        >
          <template #columns>
            <a-table-column
              :title="$t('physical.index.baseInfo')"
              data-index="baseInfo"
              :width="300"
            >
              <template #cell="{ record }">
                <div class="flex-col-start">
                  <div class="host-name">{{ record.name ? record.name : '--' }}</div>
                  <div class="flex-row">
                    <div class="mr-s oneline">{{ $t('physical.index.privateIp') }}:</div>
                    <div class="maxlength">{{ record.privateIp }}</div>
                  </div>
                  <div class="flex-row">
                    <div class="mr-s  oneline">{{ $t('physical.index.publicIp') }}:</div>
                    <div
                      class="mr-s maxlength"
                    >{{ record.publicIp }}</div>
                    <icon-code-square
                      :size="25"
                      style="cursor: pointer;"
                      @click="checkUser(record)"
                    />
                  </div>
                  <div class="flex-row">
                    <div class="mr-s">{{ $t('physical.index.os') }}:</div>
                    <div>{{ record.os ? record.os : '--' }} - {{ record.cpuArch ? record.cpuArch : '--' }}</div>
                  </div>
                  <div class="flex-row">
                    <div class="mr-s">{{ $t('physical.index.hostName') }}:</div>
                    <div>{{ record.hostname ? record.hostname : '--' }}</div>
                  </div>
                </div>
              </template>
            </a-table-column>
            <a-table-column
              :title="$t('physical.index.hostLabel')"
              data-index="hostLabel"
              :width="200"
            >
              <template #cell="{ record }">
                <div
                  class="flex-row"
                  style="flex-wrap: wrap;"
                  v-if="record.tags"
                >
                  <a-tag
                    v-for="(item, index) in record.tags"
                    :key="index"
                    class="mr-s mb-s"
                  >
                    <div
                      :title="item"
                      style="max-width: 125px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap"
                    >
                      {{ item
                      }}</div>
                  </a-tag>
                </div>
                <div v-else>
                  --
                </div>
              </template>
            </a-table-column>
            <a-table-column data-index="diskInfo">
              <template #title>
                <div class="flex-row">
                  <div class="mr">{{ $t('physical.index.diskInfo') }}</div>
                  <a-tag
                    color="green"
                    class="mr-s"
                  >{{ $t('physical.index.realTime') }}</a-tag>
                </div>
              </template>
              <template #cell="{ record }">
                <div class="flex-row">
                  <div
                    class="flex-col mr"
                    style="width: 160px;"
                  >
                    <div class="net flex-row-center">
                      <div
                        class="flex-col mr"
                        style="width: 80px;"
                      >
                        <icon-arrow-fall
                          style="color: #a9aeb8"
                          :size="30"
                          class="mb-s"
                        />
                        <div style="white-space: nowrap;">{{ record.downSpeed ? record.downSpeed : '--' }} byte/s</div>
                      </div>
                      <div
                        class="flex-col"
                        style="width: 80px;"
                      >
                        <icon-arrow-rise
                          style="color: #a9aeb8"
                          :size="30"
                          class="mb-s"
                        />
                        <div style="white-space: nowrap;">{{ record.upSpeed ? record.upSpeed : '--' }} byte/s</div>
                      </div>
                    </div>
                    <div>{{ $t('physical.index.net') }}</div>
                  </div>
                  <div class="flex-col mr">
                    <div
                      class="chart chart-empty-c"
                      v-if="!record.isCpu"
                    >
                      <icon-empty
                        :size="70"
                        class="bg-color"
                      />
                    </div>
                    <v-chart
                      v-else
                      class="chart"
                      :option="record.cpuOption"
                    />
                    <div>{{ $t('physical.index.cpu') }}</div>
                  </div>
                  <div class="flex-col mr">
                    <div
                      class="chart chart-empty-c"
                      v-if="!record.isMemory"
                    >
                      <icon-empty
                        :size="70"
                        class="bg-color"
                      />
                    </div>
                    <v-chart
                      v-else
                      class="chart"
                      :option="record.memoryOption"
                    />
                    <div>{{ $t('physical.index.memory') }}</div>
                  </div>
                  <div class="flex-col mr">
                    <div
                      class="chart chart-empty-c"
                      v-if="!record.isDisk"
                    >
                      <icon-empty
                        :size="70"
                        class="bg-color"
                      />
                    </div>
                    <v-chart
                      v-else
                      class="chart"
                      :option="record.diskOption"
                    />
                    <div>{{ $t('physical.index.disk') }}</div>
                  </div>
                </div>
              </template>
            </a-table-column>
            <a-table-column
              :title="$t('physical.index.5mphf11tfjw0')"
              data-index="operation"
              :width="280"
            >
              <template #cell="{ record }">
                <div class="flex-row-start">
                  <a-link
                    class="mr"
                    @click="showHostUserMng(record)"
                  >{{ $t('physical.index.5mphf11szks0') }}</a-link>
                  <a-link
                    class="mr"
                    @click="handleAddHost('update', record)"
                  >{{
                    $t('physical.index.5mphf11szqo0')
                  }}</a-link>
                  <a-popconfirm
                    :content="$t('physical.index.5mphf11szws0')"
                    type="warning"
                    :ok-text="$t('physical.index.5mphf11t05c0')"
                    :cancel-text="$t('physical.index.5mphf11t0bc0')"
                    @ok="deleteRows(record)"
                  >
                    <a-link status="danger">{{ $t('physical.index.5mphf11t0hc0') }}</a-link>
                  </a-popconfirm>
                </div>
              </template>
            </a-table-column>
          </template>
        </a-table>
        <add-host
          ref="addHostRef"
          @finish="labelClose"
        ></add-host>
        <host-pwd-dlg
          ref="hostPwdRef"
          @finish="handleShowTerminal($event)"
        ></host-pwd-dlg>
        <host-user-mng ref="hostUserRef"></host-user-mng>
        <host-terminal ref="hostTerminalRef"></host-terminal>
        <label-manage-dlg
          ref="labelManageDlgRef"
          @finish="labelClose"
        ></label-manage-dlg>
        <bulk-import
          ref="addHostBulkRef"
          @finish="bulkClose"
        ></bulk-import>
        <batch-label-dlg
          ref="batchLabelDlgRef"
          @finish="batchFinish"
        ></batch-label-dlg>
        <choose-terminal-user ref="chooseTerminalUserRef" @getUser="showTerminal"></choose-terminal-user>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message, Table } from '@arco-design/web-vue'
import { onMounted, reactive, ref, onUnmounted } from 'vue'
import { hostPage, delHost, hostMonitor, hostTagListAll, hostUserPage } from '@/api/ops' // eslint-disable-line
import AddHost from './components/AddHost.vue'
import BulkImport from './components/BulkImport.vue'
import HostPwdDlg from './components/HostPwdDlg.vue'
import HostTerminal from './components/HostTerminal.vue'
import HostUserMng from './components/HostUserMng.vue'
import { cpuOption, memoryOption, diskOption } from './echarts/option'
import VChart from 'vue-echarts'
import Socket from '@/utils/websocket'
import LabelManageDlg from './label/LabelManageDlg.vue'
import BatchLabelDlg from './components/BatchLabelDlg.vue'
import ChooseTerminalUser from './components/ChooseTerminalUser.vue'
import WujieVue from 'wujie-vue3'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const { bus } = WujieVue
const filter = reactive({
  name: '',
  tagIds: [],
  os: '',
  pageNum: 1,
  pageSize: 10
})

// 是否显示批量删除二次显示弹窗
const isMultiDelConfirm = ref(false);

const echartsOption = reactive<KeyValue>({
  cpuOption: cpuOption,
  memoryOption: memoryOption,
  diskOption: diskOption
})

const list = reactive<KeyValue>({
  data: [],
  selectedHostIds: [],
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
  selectedData: {},
  hasTestObj: {},
  colorYellow: '#FCEF92',
  colorRed: '#E41D1D'
})

onMounted(() => {
  data.hasTestObj = {}
  getListData()
  getAllTag()
  bus.$on('opengauss-theme-change', (val: string) => {
    if (val === 'dark') {
      changeEchartsColor('dark')
    } else {
      changeEchartsColor('light')
    }
  })
})

onUnmounted(() => {
  if (list.socketArr.length) {
    list.socketArr.forEach((item: Socket<any, any>) => {
      if (item) {
        item.destroy()
      }
    })
  }
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

const labelClose = () => {
  getAllTag()
  getListData()
}

const tableRef = ref<null | InstanceType<typeof Table>>(null)
const batchFinish = () => {
  tableRef.value?.select(list.selectedHostIds, false)
  list.selectedHostIds = []
  getListData()
  getAllTag()
}

const bulkClose = () => {
  getListData()
  getAllTag()
}

const getListData = () => new Promise(resolve => {
  list.loading = true
  const param = JSON.parse(JSON.stringify(filter))
  param.tagIds = filter.tagIds.toString()
  hostPage(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      list.data = []
      list.socketArr.forEach((item: Socket<any, any>) => {
        if (item) {
          item.destroy()
        }
      })

      res.rows.forEach((item: KeyValue, index: number) => {
        item.cpuOption = JSON.parse(JSON.stringify(echartsOption.cpuOption))
        item.memoryOption = JSON.parse(JSON.stringify(echartsOption.memoryOption))
        item.diskOption = JSON.parse(JSON.stringify(echartsOption.diskOption))
        item.isCpu = false
        item.isMemory = false
        item.isDisk = false
        openHostMonitor(item, index)
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
    list.data[index].downSpeed = eventData.downSpeed
    list.data[index].upSpeed = eventData.upSpeed
    list.data[index].isCpu = true

    if (localStorage.getItem('opengauss-theme') === 'dark') {
      list.data[index].cpuOption.color[1] = data.colorYellow
      list.data[index].diskOption.color[1] = data.colorYellow
      list.data[index].memoryOption.color[1] = data.colorYellow
    } else {
      list.data[index].cpuOption.color[1] = data.colorRed
      list.data[index].diskOption.color[1] = data.colorRed
      list.data[index].memoryOption.color[1] = data.colorRed
    }
    list.data[index].cpuOption.series[0].data[0].value = eventData.cpu
    list.data[index].cpuOption.series[0].data[1].value = (100 - eventData.cpu)
    list.data[index].isDisk = true
    list.data[index].diskOption.series[0].data[0].value = eventData.disk
    list.data[index].diskOption.series[0].data[1].value = 100 - eventData.disk
    list.data[index].isMemory = true
    list.data[index].memoryOption.series[0].data[0].value = eventData.memory
    list.data[index].memoryOption.series[0].data[1].value = 100 - eventData.memory
  })
}

const changeEchartsColor = (type: string) => {
  list.data.forEach((item: KeyValue) => {
    if (type === 'dark') {
      item.cpuOption.color[1] = data.colorYellow
      item.diskOption.color[1] = data.colorYellow
      item.memoryOption.color[1] = data.colorYellow
    } else {
      item.cpuOption.color[1] = data.colorRed
      item.diskOption.color[1] = data.colorRed
      item.memoryOption.color[1] = data.colorRed
    }
  })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const handleSelectedChange = (keys: (string | number)[]) => {
  list.selectedHostIds = keys
  list.selectedHostIds.forEach((hostId: string | number) => {
    const findOne = list.data.find((item: KeyValue) => {
      return item.hostId === hostId
    })
    if (findOne) {
      data.selectedData[hostId] = findOne
    }
  })
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData()
}

const chooseTerminalUserRef = ref<null | InstanceType<typeof ChooseTerminalUser>>(null)
let currentTermData = {}
const checkUser = async (hostData) => {
  currentTermData = hostData
  const { code, rows } = await hostUserPage(hostData.hostId)
  if (Number(code) === 200) {
      if (rows?.length === 0) {
        Message.error(t('physical.index.noUserTip'))
        return
      }
      if (rows?.length === 1) {
        showTerminal(rows[0].username)
        return
      }
      const hasRoot = rows.some(item => item.username === 'root')
      if (hasRoot) {
        showTerminal('root')
        return
      }
      chooseTerminalUserRef.value?.openChooseUser(rows)
  }
}

const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)
const showTerminal = async (username: string) => {
  if (!username) {
    Message.error(t('physical.index.noUserTip'))
    return
  }
  const hostData = {
    ...currentTermData,
    username
  }
  handleShowTerminal({
      hostId: hostData.hostId,
      port: hostData.port,
      ip: hostData.publicIp,
      username
    })
}

const hostTerminalRef = ref<null | InstanceType<typeof HostTerminal>>(null)
const handleShowTerminal = (hostData: KeyValue) => {
  hostTerminalRef.value?.open(hostData)
}

const addHostRef = ref<null | InstanceType<typeof AddHost>>(null)
const handleAddHost = (type: string, data?: KeyValue) => {
  addHostRef.value?.open(type, data)
}

const batchLabelDlgRef = ref<null | InstanceType<typeof BatchLabelDlg>>(null)
const handleBatchTag = () => {
  if (list.selectedHostIds.length) {
    // get common labels
    const commonLabels = getCommonLabels()
    batchLabelDlgRef.value?.open(list.selectedHostIds, commonLabels)
  } else {
    Message.warning(t('physical.index.else1'))
  }
}

const getCommonLabels = () => {
  const result: string[] = []
  const selectedArr: any[] = []
  list.selectedHostIds.forEach((hostId: string | number) => {
    selectedArr.push(data.selectedData[hostId])
  })

  // get length max
  let maxObj: any = null
  selectedArr.forEach((item: KeyValue) => {
    if (maxObj) {
      if (maxObj.tags && item.tags) {
        if (maxObj.tags.length < item.tags.length) {
          maxObj = item
        }
      }
    } else {
      maxObj = item
    }
  })

  // get common label
  if (maxObj.tags && maxObj.tags.length) {
    for (let i = 0; i < maxObj.tags.length; i++) {
      const tag = maxObj.tags[i]
      let allhas = true
      for (let j = 0; j < selectedArr.length; j++) {
        const tempData = selectedArr[j]
        if (!tempData.tags || tempData.tags?.indexOf(tag) < 0) {
          allhas = false
          break
        }
      }
      if (allhas) {
        result.push(tag)
      }
    }
  }

  return result
}

const labelManageDlgRef = ref<null | InstanceType<typeof LabelManageDlg>>(null)
const handleLabelManage = () => {
  labelManageDlgRef.value?.open()
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

const addHostBulkRef = ref<null | InstanceType<typeof BulkImport>>(null)
const addHostbulk = (type: string, data?: KeyValue) => {

  addHostBulkRef.value?.open()
}

// 判断是否显示二次确认删除popConfirm悬浮窗
const deleteSelectedHosts = () => {
  // 判断当前是否选中
  if (list.selectedHostIds.length > 0) {
    isMultiDelConfirm.value = true;
  } else {
    Message.warning(t('physical.index.else1'))
    isMultiDelConfirm.value = false;
  }
}

// 批量删除
const deleteMultiRows = () => {
  const selectedRecords = list.selectedHostIds.map((hostId: string | number) => {
    return data.selectedData[hostId]
  })
  const tempselectedRecords = selectedRecords.filter(hostId => hostId != null && hostId !== undefined)
  deleteMultipleRows(tempselectedRecords)
  isMultiDelConfirm.value = false;
}

const deleteMultipleRows = (records: KeyValue) => {
  const deletePromises = records.map((record) => delHost(record.hostId))
    Promise.all(deletePromises).then((responses) => {
      let allSuccess = true
      responses.forEach((res) => {
        if (Number(res.code) !== 200) {
          allSuccess = false
        }
      })
      if (allSuccess) {
        Message.success(t('physical.index.5mphf11rr890'))
      } else {
        Message.error(t('physical.index.5mphf11rr990'))
      }
      list.selectedHostIds = []
      data.selectedData = {}
      getListData()
    }).catch(() => {
      Message.error({
        content: 'An error occurred while deleting hosts'
      })
    })
  getListData()
  getAllTag()

}

const hostUserRef = ref<null | InstanceType<typeof HostUserMng>>(null)
const showHostUserMng = (record: KeyValue) => {
  hostUserRef.value?.open(record)
}

</script>

<style lang="less" scoped>
:deep(.arco-form-layout-inline) {
  flex-wrap: nowrap;
}

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
      height: 110px;
    }

    .chart {
      width: 110px;
      height: 110px;
    }

    .chart-empty-c {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;

      .bg-color {
        color: rgb(var(--gray-5));
      }
    }
  }
}
.oneline {
  white-space: nowrap;
}
.maxlength {
  max-width: 180px;
}
</style>
