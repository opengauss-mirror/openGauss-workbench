<template>
  <div class="app-container" id="physicalMange">
    <div class="main-bd">
      <div class="physical-list">
        <div class="mb-s physicalTitle" id="physicalPageTitle">
          <div style="display:flex; flex-wrap: nowrap;">
            <el-button type="primary" class="mr mr8" @click="handleAddServer()">
              {{ $t('physical.index.createServer') }}
            </el-button>
            <el-button type="primary" class="mr" @click="addHostbulk('create')">
              {{ $t('physical.index.5mphf11rr090') }}
            </el-button>
            <el-button type="primary" class="mr" @click="handleBatchTag()">
              {{ $t('physical.index.labelBatch') }}
            </el-button>
            <el-button type="primary" class="mr" @click="handleLabelManage">
              {{ $t('physical.index.labelManage') }}
            </el-button>
            <el-popconfirm :visible="isMultiDelConfirm" :title="$t('physical.index.5mphf11szws0')" type="warning"
              @confirm="deleteMultiRows">
              <template #reference>
                <el-button type="primary" class="mr" @click="deleteSelectedHosts">
                  {{ $t('physical.index.5mphf11rr590') }}
                </el-button>
              </template>
            </el-popconfirm>
          </div>
          <div>
            <fusionSearch @clickSearch="clickSearch" :labelOptions="labelOptions" />
          </div>
        </div>
        <el-table ref="tableRef" class="openDesignTable" row-key="hostId" :data="list.data" :pagination="list.page"
          :row-selection="list.rowSelection" @page-change="currentPage" @page-size-change="pageSizeChange"
          :loading="list.loading" @selection-change="handleSelectedChange">
          <template #empty>
            <div style="text-align: center;">
              <div class="o-table-empty">
                <el-icon class="o-empty-icon">
                  <IconEmpty></IconEmpty>
                </el-icon>
              </div>
              <p>{{ t('physical.index.noData') || '--' }}</p>
            </div>
          </template>
          <el-table-column type="selection" width="32"></el-table-column>
          <el-table-column :label="$t('physical.index.serverName')" prop="name">
            <template #default="{ row }">
              <div class="flex-col-start">
                <div class="serverName" :title="row.name || ''"
                     style=" overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%;"
                >{{ row.name || '--' }}</div>
                <div class="flex-row" style="margin-top: 8px;" v-if="row.tags">
                  <div class="flex-row" style="flex-wrap: wrap;">
                    <el-tag v-for="(item, index) in row.tags" :key="index" class="mr-s mb-s">
                      <div :title="item"
                        style="max-width: 125px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap">
                        {{ item }}</div>
                    </el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('physical.index.privateAndPublicIP')" prop="privateAndPublicIP" :width="192" show-overflow-tooltip>
            <template #default="{ row }">
              <div style="white-space: nowrap; display: inline-block; width: 100%;">
                <div class="flex-row" style="flex-wrap: wrap;">
                  <div class="mr-s oneline">{{ $t('physical.index.privateIp') }}: {{ row.privateIp || '--' }}</div>
                </div>
                <div class="flex-row" style="flex-wrap: wrap;">
                  <div class="mr-s oneline">{{ $t('physical.index.publicIp') }}: {{ row.publicIp || '--' }}</div>
                  <icon-code-square :size="25" style="cursor: pointer;" @click="checkUser(row)" />
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('physical.index.os')" prop="operatingSystem">
            <template #default="{ row }">
              {{ row.os || '--' }} - {{ row.cpuArch || '--' }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('physical.index.hostName')" prop="hostname"></el-table-column>
          <el-table-column :label="$t('physical.index.realNetRate')" prop="Real-time network rate">
            <template #default="{ row }">
              <div class="netWorkSpeed">
                <div class="upSpeed">{{ t('physical.index.upLink') }}: {{ row.upSpeed && Number(row.upSpeed) > 0? row.upSpeed : '--'  }}</div>
                <div class="downSpeed">{{ t('physical.index.downlink') }}: {{ row.downSpeed && Number(row.downSpeed) > 0? row.downSpeed : '--'}}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('physical.index.cpu')" prop="cpuRate">
            <template #default="{ row }">
              <div>{{ row.cpuRate || '--' }}%</div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('physical.index.memory')" prop="memoryRate">
            <template #default="{ row }">
              <div>{{ row.memoryRate || '--' }}%</div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('physical.index.disk')" prop="diskRate">
            <template #default="{ row }">
              <div>{{ row.diskRate || '--' }}%</div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('physical.index.agentStatus')" prop="state">
            <template #default="{ row }">
              <div v-if="row.agentStatus === AgentStatus.RUNNING"><span class="statusDot runingColor"></span>{{
                t('physical.index.running') }}</div>
              <div v-else-if="row.agentStatus === AgentStatus.UNINSTALL"><span
                  class="statusDot uninstallColor"></span>{{ t('physical.index.uninstalled') }}</div>
              <div v-else-if="row.agentStatus === AgentStatus.STOP"><span class="statusDot offLineColor"></span>{{
                t('physical.index.offline') }}</div>
              <div v-else>--</div>
            </template>
          </el-table-column>
          <el-table-column :label="t('physical.index.operateColumn')" prop="operation" :width="312">
            <template #default="{ row }">
              <div class="flex-row-start">
                <el-button v-if="row.agentStatus === AgentStatus.UNINSTALL" text class="mr"
                  @click="handleAddAgent(row)">{{ t('physical.index.agentInstall') }}</el-button>
                <el-button v-else text class="mr" @click="openAgentManagerDrawer(row)">{{
                  t('physical.index.agentManager') }}</el-button>
                <el-button text class="mr" @click="showHostUserMng(row)">{{ t('physical.index.userManage')
                  }}</el-button>
                <el-popover popper-class="operatePopper" :show-arrow="false" placement="bottom-end" :width="100"
                  trigger="click">
                  <template #reference>
                    <el-button text>{{ t('physical.index.more') }}</el-button>
                  </template>
                  <div class="operateItems">
                    <el-button text @click="handleAddHost('update', row)">{{ t('physical.index.5mphf11szqo0')
                      }}</el-button>
                    <el-popconfirm :title="$t('components.Package.5mtcyb0rty16')" type="warning"
                      popper-class="o-popper-confirm" :confirm-button-text="$t('components.Package.5mtcyb0rty17')"
                      :cancel-button-text="$t('components.Package.5mtcyb0rty18')" @confirm="deleteRows(row)">
                      <template #reference>
                        <el-button text>{{ t('physical.index.5mphf11t0hc0') }}</el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </el-popover>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination :currentPage="filter.pageNum" :page-size="filter.pageSize" :page-sizes="[5, 10, 15, 20]"
          layout="total, sizes, prev, pager, next, jumper" @current-change="currentPage" @size-change="pageSizeChange"
          :total="list.page.total"></el-pagination>
        <add-host ref="addHostRef" @finish="labelClose"></add-host>

        <add-agent ref="addAgentRef" @finish="labelClose" :hostId="rowHostId" :publicIp="rowPublicIp"></add-agent>
        <agent-drawer v-if="showAgentManagerDrawer" :rowInfo="rowInfo" @closeDrawer="closeDrawer"
          @updateTableData="updateTableData"></agent-drawer>
        <host-pwd-dlg ref="hostPwdRef" @finish="handleShowTerminal($event)"></host-pwd-dlg>
        <host-user-mng ref="hostUserRef" class="elDialog"></host-user-mng>
        <host-terminal ref="hostTerminalRef"></host-terminal>
        <label-manage-dlg ref="labelManageDlgRef" @finish="labelClose"  class="elDialog"></label-manage-dlg>
        <bulk-import v-if="showBulkImport" ref="addHostBulkRef" @finish="bulkClose"></bulk-import>
        <batch-label-dlg ref="batchLabelDlgRef" @finish="batchFinish"></batch-label-dlg>
        <choose-terminal-user ref="chooseTerminalUserRef" @getUser="showTerminal"></choose-terminal-user>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message, Table } from '@arco-design/web-vue'
import { onMounted, reactive, ref, onUnmounted, computed, toRaw } from 'vue'
import {
  hostPage, pageMonitor, delHost, hostMonitor, hostTagListAll, hostUserPage, uninstallAgent,
  listAgent, stopHostAgent, startHostAgent
} from '@/api/ops' // eslint-disable-line
import AddHost from './components/AddHost.vue'
import BulkImport from './components/BulkImport.vue'
import HostPwdDlg from './components/HostPwdDlg.vue'
import HostTerminal from './components/HostTerminal.vue'
import HostUserMng from './components/HostUserMng.vue'
import AgentDrawer from './components/AgentDrawer.vue'
import Socket from '@/utils/websocket'
import LabelManageDlg from './label/LabelManageDlg.vue'
import BatchLabelDlg from './components/BatchLabelDlg.vue'
import ChooseTerminalUser from './components/ChooseTerminalUser.vue'
import WujieVue from 'wujie-vue3'
import { useI18n } from 'vue-i18n'
import { IconEmpty } from '@computing/opendesign-icons'
import fusionSearch from '@/components/fusion-search/index.vue'
import { searchType } from '@/types/searchType'
import { useRoute, useRouter, RouteRecordRaw } from 'vue-router'
import { AgentStatus } from './physicalType/index.ts'
import AddAgent from "@/views/resource/physical/components/AddAgent.vue";
import showMessage from '@/hooks/showMessage'
const { t } = useI18n()
const { bus } = WujieVue
const filter = reactive({
  name: '',
  tagIds: [],
  os: '',
  pageNum: 1,
  pageSize: 10
})

// Specifies whether the agent management drawer is displayed
const showAgentManagerDrawer = ref(false)
const rowInfo = ref({})
// Whether to display the batch delete secondary display pop-up window
const isMultiDelConfirm = ref(false)

// Parameters passed to the edit pop-up
const rowHostId = ref('')
const rowPublicIp = ref('')

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
  socketArr: [],
  newSocket: {},
})

const data = reactive<KeyValue>({
  selectedData: {},
  colorYellow: '#FCEF92',
  colorRed: '#E41D1D',
  tagsList: []
})
const tempList = ref<KeyValue[]>([{
  label: '111',
  value: '111'
}])
const initList = computed(() => {
  return [...tempList.value];
})
const labelOptions = computed(() => {
  return {
    tagIds: {
      label: t('physical.index.hostLabel') || '11',
      value: 'tagIds',
      placeholder: t('physical.index.hostLabelPlaceholder'),
      selectType: searchType.MULTIPLESELECT,
      options: tempList.value
    },
    os: {
      label: t('physical.index.os') || '12',
      value: 'os',
      placeholder: t('physical.index.osPlaceholder'),
      selectType: searchType.SELECT,
      options: [
        {
          value: 'openEuler',
          label: 'openEuler'
        },
        {
          value: 'centos',
          label: 'centos'
        }
      ]
    },
    name: {
      label: t('physical.index.ipAddress') || '13',
      value: 'name',
      placeholder: t('physical.index.ipAddressPlaceholder'),
      selectType: searchType.INPUT
    }
  }
})

const timerValue = ref(0)
onMounted(() => {
  timerValue.value = new Date().getTime()
  getListData()
  getAllTag()
  openHostsMonitor()
})

const updateTableData = () => {
  webSocketNew.value && webSocketNew.value.destroy();
  setTimeout(() => {
    timerValue.value = new Date().getTime()
    openHostsMonitor()
    getListData()
  })
}

const hostIdList = computed(() => list.data.map((item: KeyValue) => {
  return item.hostId;
}))
const webSocketNew = ref<any>(null);
const openHostsMonitor = () => {
  const param = {
    businessId: 'monitor_ops_host_' + timerValue.value
  }
  const websocket = new Socket({ url: `COMMAND_EXEC/${param.businessId}` })
  webSocketNew.value = websocket
  websocket.onopen(() => {
  })
  websocket.onmessage((messageData: any) => {
    const eventData = JSON.parse(messageData)
    // Here iterate through hostId--to the corresponding attribute
    list.data.forEach((row, index) => {
      if (row.hostId === eventData?.hostId) {
        list.data[index].downSpeed = eventData?.downSpeed || 0
        list.data[index].upSpeed = eventData?.upSpeed || 0
        list.data[index].isCpu = true
        list.data[index].cpuRate = eventData?.cpu || 0
        list.data[index].isDisk = true
        list.data[index].diskRate = eventData?.disk || 0
        list.data[index].isMemory = true
        list.data[index].memoryRate = eventData?.memory || 0
      }
    })
  })
  websocket.onclose((event) => {
    console.log(event)
  })
}

// Clear and destroy the current websocket
const clearWSList = () => {
  if (webSocketNew.value) {
    webSocketNew.value.destroy()
  }
}

onUnmounted(() => {
  clearWSList()
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
      tempList.value = res.data.map((item: KeyValue) => {
        return {
          label: item.name,
          value: item.hostTagId
        }
      })
      labelOptions.value.tagIds.options = [...tempList.value]
    }
  }).finally(() => {
    data.tagsLoading = false
  })
}

const labelClose = () => {
  getAllTag()
  // getListData()
  updateTableData()
}

const tableRef = ref<null | InstanceType<typeof Table>>(null)
const batchFinish = () => {
  // 这里是将对应选择的项取消
  list.selectedHostIds = []
  getListData()
  getAllTag()
}

const bulkClose = () => {
  showBulkImport.value = false
  getListData()
  getAllTag()
}

// 这里测试下
const clickSearch = (params) => {
  filter.tagIds = params.tagIds;
  filter.os = params.os || '';
  filter.name = params.name || '';
  updateTableData();
}
const getListData = () =>  {
  queryDataList()
  // queryDataList();
}

const queryDataList = () => new Promise(resolve => {
  list.loading = true
  const param = JSON.parse(JSON.stringify(filter))
  param.tagIds = filter.tagIds?.toString() || '';

  hostPage(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      list.data = res.rows?.map((item: KeyValue) => {
        return item;
      })
      list.page.total = res?.total
      setTimeout(() => {
        pageMonitor(hostIdList.value, 'monitor_ops_host_' + timerValue.value).then((res: KeyValue) => {
          if (res?.data?.res) {
          } else {
          }
        }).catch((err) => {
          console.error(err)
        })
      })
      if (rowInfo.value) {
        // If the agent management drawer is open, updating the column data requires updating the open row data
        list.data.forEach((row: KeyValue) => {
          if (row.hostId === rowInfo.value?.hostId) {
            rowInfo.value = row;
          }
        })
      }
    } else resolve(false)
  }).finally(() => {
    // list.loading = false
  })
})

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const handleSelectedChange = (rows: (any)[]) => {
  list.selectedHostIds = rows.map(row => row.hostId)
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

const router = useRouter()

const handleAddServer = () => {
  router.push({ path: '/resource/physical/createserver' })
}

const addAgentRef = ref<null | InstanceType<typeof AddAgent>>(null)
const handleAddAgent = (record?: KeyValue) => {
  addAgentRef.value?.open(record)
}

const addHostRef = ref<null | InstanceType<typeof AddHost>>(null)
const handleAddHost = (type: string, data?: KeyValue) => {
  const title = document.getElementById('physicalPageTitle');
  title?.click();
  rowHostId.value = data.hostId;
  rowPublicIp.value = data.rowPublicIp;
  addHostRef.value?.open(type, data)
}

const batchLabelDlgRef = ref<null | InstanceType<typeof BatchLabelDlg>>(null)
const handleBatchTag = () => {
  if (list.selectedHostIds?.length) {
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
        if (maxObj.tags?.length < item.tags?.length) {
          maxObj = item
        }
      }
    } else {
      maxObj = item
    }
  })

  // get common label
  if (maxObj.tags && maxObj.tags?.length) {
    for (let i = 0; i < maxObj.tags?.length; i++) {
      const tag = maxObj.tags[i]
      let allhas = true
      for (let j = 0; j < selectedArr?.length; j++) {
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

const deleteRows = (record: KeyValue) => {
  // Judge the current state - not installed
  if (record.agentStatus === AgentStatus.UNINSTALL) {
    delHostFunc(record.hostId)
  } else {
    // Invoke uninstall and then delete
    uninstallAgent(record.hostId).then((res: KeyValue) => {
      if (res.code === 200) {
        delHostFunc(record.hostId)
      }
    })
  }
}

const delHostFunc = (hostId: string) => {
  delHost(hostId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'delete success'
      })
      getListData()
    }
  })
}

const addHostBulkRef = ref<null | InstanceType<typeof BulkImport>>(null)
const showBulkImport = ref(false)
const addHostbulk = (type: string, data?: KeyValue) => {
  showBulkImport.value = true
  addHostBulkRef.value?.open()
}

// Check whether to display the popConfirm overlay window for secondary confirmation and deletion
const deleteSelectedHosts = () => {
  // Determine whether this option is selected
  if (list.selectedHostIds?.length > 0) {
    judgeUninstall()
  } else {
    showMessage('warning', t('physical.index.else1'))
    isMultiDelConfirm.value = false
  }
}

const judgeUninstall = () => {
  let allUninstallFlag = true;
  for (let i=0; i<list.data.length; i++) {
    if (list.selectedHostIds.includes(list.data[i].hostId) &&
      list.data[i].agentStatus !== AgentStatus.UNINSTALL) {
        allUninstallFlag = false
    }
  }
  if (!allUninstallFlag) {
    showMessage('warning', t('physical.index.unInstalledAgent'))
  }
  isMultiDelConfirm.value = allUninstallFlag
}

const deleteMultiRows = () => {
  const selectedRecords = list.selectedHostIds.map((hostId: string | number) => {
    return data.selectedData[hostId]
  })
  const tempselectedRecords = selectedRecords.filter(hostId => hostId != null && hostId !== undefined)
  deleteMultipleRows(tempselectedRecords)
  isMultiDelConfirm.value = false
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

const openAgentManagerDrawer = (row: KeyValue) => {
  showAgentManagerDrawer.value = true
  rowInfo.value = row
}

const closeDrawer = () => {
  showAgentManagerDrawer.value = false
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
  position: relative;

  .main-bd {
    height: 100%;

    .physical-list {
      padding: 20px;

      .physicalTitle {
        display: flex;
        flex-direction: column;
        gap: 16px;
        height: 80px;

        .el-button {
          margin-right: 0;
        }

        .mr8 {
          margin-right: 8px;
        }

        .el-table {
          font-size: 14px;
        }
      }
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
