<template>
  <div class="app-container" id="packageList">
    <div class="main-bd packageManage">
      <div class="control-wrap">
        <div class="flex-between mb-s">
          <div style="display:flex; flex-wrap: nowrap;">
            <el-button type="primary" class="mr" @click="addPackInstall('create')">{{
              $t('components.Package.5mtcyb0rty02')
            }}</el-button>
            <el-button class="mr" @click="checkSelectedPack">{{
              $t('components.Package.5mtcyb0rty03')
            }}</el-button>
            <el-popconfirm :title="$t('components.Package.5mtcyb0rty16')" type="warning" popper-class="o-popper-confirm"
              :confirm-button-text="$t('components.Package.5mtcyb0rty17')" :visible="delPopConfirmVisible"
              :cancel-button-text="$t('components.Package.5mtcyb0rty18')" @confirm="deleteSelectedHosts"
              @cancel="delPopConfirmVisible = false">
              <template #reference>
                <el-button @click="judgeSelectedHosts" class="mr">{{ $t('components.Package.5mtcyb0rty04') }}</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
      <div>
        <fusionSearch @clickSearch="clickSearch" :label-options="labelOptions" />
      </div>
      <div class="package-list">
        <el-table row-key="packageId" :data="list.data" :pagination="list.page" :row-selection="list.rowSelection"
          :loading="list.loading" @selection-change="handleSelected" class="openDesignTable">
          <template #empty>
            <div style="text-align: center;">
              <div class="o-table-empty">
                <el-icon class="o-empty-icon">
                  <IconEmpty></IconEmpty>
                </el-icon>
              </div>
              <p style="font-weight: bold;">{{ $t('components.Package.5mtcyb0rty20') }}</p><br>
              <p>{{ $t('components.Package.5mtcyb0rty21') }}</p>
              <el-button class="-primary" type="text" @click="addPackInstall('create', searchInfoPackage, 1)">{{
                $t('components.Package.5mtcyb0rty22') }}</el-button>
              <el-button class="-primary" type="text" @click="addPackInstall('create', searchInfoPackage, 0)">{{
                $t('components.Package.5mtcyb0rty23') }}</el-button>
            </div>
          </template>
          <el-table-column type="selection" width="32"></el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty06')" prop="name"></el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty07')" prop="os">
          </el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty08')" prop="cpuArch">
          </el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty09')" prop="packageVersion" :width="150">
            <template #default="{ row }">{{ $t(`components.Package.${row.packageVersion}`) }}</template>
          </el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty10')" prop="packageVersionNum" :width="100">
          </el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty11')" prop="hostLabel">
            <template #default="{ row }">{{ row.hostLabel ? $t('components.Package.5mtcyb0rty23') :
              $t('components.Package.5mtcyb0rty22') }}</template>
          </el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty12')" prop="packageUrl">
            <template #default="{ row }">{{ row.hostLabel ? row.packageUrl : row.realPath }}</template>
          </el-table-column>
          <el-table-column :label="$t('components.Package.5mtcyb0rty13')" prop="operation" :width="280">
            <template #default="{ row }">
              <div class="flex-row-start">
                <el-button text class="mr" @click="checkPack(row)">{{ $t('components.Package.5mtcyb0rty14')
                  }}</el-button>
                <el-button text class="mr" @click="addPackInstall('update', row)">{{
                  $t('components.Package.5mtcyb0rty15')
                  }}</el-button>
                <el-popconfirm :title="$t('components.Package.5mtcyb0rty16')" type="warning" popper-class="o-popper-confirm"
                  :confirm-button-text="$t('components.Package.5mtcyb0rty17')"
                  :cancel-button-text="$t('components.Package.5mtcyb0rty18')" @confirm="deleteRows(row)">
                  <template #reference>
                    <el-button status="danger" text>{{ $t('components.Package.5mtcyb0rty19') }}</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination :currentPage="filter.pageNum" :page-size="filter.pageSize" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @current-change="pageNumChange" @size-change="pageSizeChange"
          :total="list.page.total"></el-pagination>
      </div>
      <addPack ref="addPackRef" @finish="addPackClose()" @close="addPackClose" @downloadStart="downloadStart"></addPack>
      <DownloadNotification ref="NotificaionRef" :msg="$t('components.Package.5mtcyb0rty53')" :iconClass="'rar-file'"></DownloadNotification>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue/es/index'
import { h, onMounted, reactive, ref, computed } from 'vue'
import {
  checkPackage,
  getVersionNum,
  getPackagePage,
  delPackageV2
} from '@/api/ops'
import Socket from '@/utils/websocket'
import WujieVue from 'wujie-vue3'
import { useI18n } from 'vue-i18n'
import AddPack from './AddPack.vue'
import { CpuArch, OS } from '../../../../../../plugins/base-ops/web-ui/src/types/os'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import { searchType } from '@/types/searchType'
import DownloadNotification from '@/components/downloadNotification'
import fusionSearch from '@/components/fusion-search/index.vue'
import { IconEmpty } from '@computing/opendesign-icons'
const { t } = useI18n()
const { bus } = WujieVue

const packageVersionNumList = ref([])

const labelOptions = computed(() => {
  return {
    os: {
      label: t('components.Package.5mtcyb0rty07'),
      value: 'os',
      placeholder: t('components.Package.5mtcyb0rty60'),
      selectType: searchType.SELECT,
      options: [
        {
          value: OS.CENTOS,
          label: OS.CENTOS
        },
        {
          value: OS.OPEN_EULER,
          label: OS.OPEN_EULER
        }
      ]
    },
    cpuArch: {
      label: t('components.Package.5mtcyb0rty08'),
      value: 'cpuArch',
      placeholder: t('components.Package.5mtcyb0rty61'),
      selectType: searchType.SELECT,
      options: [
        {
          value: CpuArch.AARCH64,
          label: CpuArch.AARCH64
        },
        {
          value: CpuArch.X86_64,
          label: CpuArch.X86_64
        }
      ]
    },
    packageVersion: {
      label: t('components.Package.5mtcyb0rty09'),
      value: 'packageVersion',
      placeholder: t('components.Package.5mtcyb0rty62'),
      selectType: searchType.SELECT,
      options: [
        {
          value: OpenGaussVersionEnum.MINIMAL_LIST,
          label: OpenGaussVersionEnum.MINIMAL_LIST
        },
        {
          value: OpenGaussVersionEnum.LITE,
          label: OpenGaussVersionEnum.LITE
        },
        {
          value: OpenGaussVersionEnum.ENTERPRISE,
          label: OpenGaussVersionEnum.ENTERPRISE
        }
      ]
    },
    packageVersionNum: {
      label: t('components.Package.5mtcyb0rty10'),
      value: 'packageVersionNum',
      placeholder: t('components.Package.5mtcyb0rty45'),
      selectType: searchType.SELECT,
      options: packageVersionNumList.value
    },
    name: {
      label: t('components.Package.5mtcyb0rty06'),
      value: 'name',
      placeholder: t('components.Package.5mtcyb0rty63'),
      selectType: searchType.INPUT
    }
  }
})

const list = reactive<KeyValue>({
  data: [],
  selectedpackageIds: [],
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
})
const delPopConfirmVisible = ref(false);

const addPackRef = ref<null | InstanceType<typeof AddPack>>(null)
const addPackInstall = (type: string, data?: KeyValue, addOptionFlag?: number) => {
  webSocketOpen(type, data, addOptionFlag)
}
const addPackClose = () => {
  parentTags.value.forEach(e => e.children = [])
  fetchOs()
  fetchArch()
  fetchVersion()
  fetchVersionNum()
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const osValue = computed(() => t('components.Package.5mtcyb0rty07'))
const cpuValue = computed(() => t('components.Package.5mtcyb0rty08'))
const versionValue = computed(() => t('components.Package.5mtcyb0rty09'))
const versionNumValue = computed(() => t('components.Package.5mtcyb0rty10'))
const parentTags = ref([
  {
    name: 'os',
    value: osValue,
    children: [],
    disabled: false
  },
  {
    name: 'cpuArch',
    value: cpuValue,
    children: [],
    disabled: false
  },
  {
    name: 'packageVersion',
    value: versionValue,
    children: [],
    disabled: false
  },
  {
    name: 'packageVersionNum',
    value: versionNumValue,
    children: [],
    disabled: false
  }
])

const clickSearch = (params) => {
  const searchForm = new FormData()
  Object.keys(params).forEach(e => {
    searchForm.append(e, params[e])
  })
  getListData(filter.pageSize, filter.pageNum, searchForm)
}
const searchFormData = new FormData

const handleSelected = (rows: any[]) => {
  list.selectedpackageIds = rows.map(item => item.packageId);
}
const checkPack = (record: KeyValue) => {
  let templist = []
  templist.push(record.packageId)
  checkPackage(templist).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: t('components.Package.5mtcyb0rty26')
      })
    } else {
      Message.error({
        content: t('components.Package.5mtcyb0rty27')
      })
    }
  }).catch(error => {
    console.error('260' + error)
  }).finally(() => {
    getListData(filter.pageSize, filter.pageNum, searchFormData)
  })
}

const checkSelectedPack = () => {
  if (list.selectedpackageIds.length > 0) {
    checkPackMul(list.selectedpackageIds)
  } else {
    Message.warning(t('components.Package.5mtcyb0rty28'))
  }
}

const checkPackMul = (records: any) => {
  checkPackage(records).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: t('components.Package.5mtcyb0rty26')
      })
    } else {
      Message.error({
        content: t('components.Package.5mtcyb0rty27')
      })
    }
  }).catch(error => {
    console.error('260' + error)
  }).finally(() => {
    getListData(filter.pageSize, filter.pageNum, searchFormData)
  })
}

const deleteRows = (record: KeyValue) => {
  delPackageV2([record.packageId]).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: t('components.Package.5mtcyb0rty29')
      })
    }
  }).catch(error => {
    console.error(error)
  }).finally(() => {
    getListData(filter.pageSize, filter.pageNum, searchFormData)
  })
}

const judgeSelectedHosts = () => {
  // Here the length of the choice is judged
  if (list.selectedpackageIds.length > 0) {
    delPopConfirmVisible.value = true;
  } else {
    delPopConfirmVisible.value = false;
    Message.warning(t('components.Package.5mtcyb0rty28'))
  }
}

const deleteSelectedHosts = () => {
  if (list.selectedpackageIds.length > 0) {
    deleteMultipleRows(list.selectedpackageIds)
  } else {
    Message.warning(t('components.Package.5mtcyb0rty28'))
  }
  delPopConfirmVisible.value = false;
}
const NotificaionRef = ref();

const deleteMultipleRows = (records: KeyValue) => {
  delPackageV2(records).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: t('components.Package.5mtcyb0rty30')
      })
    } else {
      Message.error({
        content: t('components.Package.5mtcyb0rty31')
      })
    }
    list.selectedpackageIds = []
  }).catch(error => {
    console.error('260' + error)
  }).finally(() => {
    getListData(filter.pageSize, filter.pageNum, searchFormData)
  })
}

const tableEmptyFlag = ref(false)
const searchInfoPackage = reactive({
  packageId: '',
  name: '',
  os: '',
  cpuArch: '',
  packageVersion: '',
  packageVersionNum: '',
  packageUrl: '',
  packagePath: '',
  type: '',
  remark: ''
})
const addSearchPackage = (total: number) => {
  tableEmptyFlag.value = total === 0
  searchInfoPackage.os = searchFormData.get('os') ? searchFormData.get('os') : ''
  searchInfoPackage.cpuArch = searchFormData.get('cpuArch') ? searchFormData.get('cpuArch') : CpuArch.X86_64
  searchInfoPackage.packageVersion = searchFormData.get('packageVersion') ? searchFormData.get('packageVersion') : OpenGaussVersionEnum.MINIMAL_LIST
  searchInfoPackage.packageVersionNum = searchFormData.get('packageVersionNum') ? searchFormData.get('packageVersionNum') : '5.0.0'
  searchInfoPackage.type = 'openGauss'
}

// init
const initOs = [OS.OPEN_EULER, OS.CENTOS]
const initArch = [CpuArch.X86_64, CpuArch.AARCH64]
const initVersion = [OpenGaussVersionEnum.MINIMAL_LIST, OpenGaussVersionEnum.LITE, OpenGaussVersionEnum.ENTERPRISE]
const init = () => {
  parentTags.value.forEach(e => e.children = [])
  fetchOs()
  fetchArch()
  fetchVersion()
  fetchVersionNum()
  getListData(filter.pageSize, filter.pageNum)
}
const fetchOs = () => {
  parentTags.value.forEach(tag => {
    if (tag.name === 'os') {
      initOs.forEach(child => {
        const newChild = {
          name: child,
          disabled: false
        }
        tag.children.push(newChild)
      })
    }
  })
}
const fetchArch = () => {
  parentTags.value.forEach(tag => {
    if (tag.name === 'cpuArch') {
      initArch.forEach(child => {
        const newChild = {
          name: child,
          disabled: false
        }
        tag.children.push(newChild)
      })
    }
  })
}
const fetchVersion = () => {
  parentTags.value.forEach(tag => {
    if (tag.name === 'packageVersion') {
      initVersion.forEach(child => {
        const newChild = {
          name: child,
          disabled: false
        }
        tag.children.push(newChild)
      })
    }
  })
}
let countVersionNum = 0
const fetchVersionNum = () => {
  getVersionNum().then(res => {
    if (Number(res.code) === 200) {
      packageVersionNumList.value = res.data.map(e => {
        return {
          value: e,
          label: e
        }
      })
      parentTags.value.forEach(tag => {
        if (tag.name === 'packageVersionNum') {
          res.data.forEach(child => {
            const newChild = {
              name: child,
              disabled: false
            }
            tag.children.push(newChild)
            countVersionNum = countVersionNum + 1
          })
        }
      })
    }
  }).catch(error => {
    console.error(error)
  })
}

const filter = {
  pageNum: 1,
  pageSize: 10
}
const pageNumChange = (e: number) => {
  filter.pageNum = e
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const getListData = (pageSize?: number, pageNum?: number, formData?: FormData) => new Promise(resolve => {
  let checkFormValue = formData ? formData.get('name') : null
  const name = checkFormValue !== null ? checkFormValue.toString() : ''
  checkFormValue = formData ? formData.get('os') : null
  const os = checkFormValue !== null ? checkFormValue.toString() : ''
  checkFormValue = formData ? formData.get('cpuArch') : null
  const cpuArch = checkFormValue !== null ? checkFormValue.toString() : ''
  checkFormValue = formData ? formData.get('packageVersion') : null
  const openGaussVersion = checkFormValue
  checkFormValue = formData ? formData.get('packageVersionNum') : null
  const openGaussVersionNum = checkFormValue !== null ? checkFormValue.toString() : ''
  list.loading = true
  getPackagePage(pageSize ? pageSize : 10, pageNum ? pageNum : 1, {
    name: name,
    os: os,
    cpuArch: cpuArch,
    openGaussVersion: openGaussVersion,
    openGaussVersionNum: openGaussVersionNum
  }).then((res) => {
    list.data = []
    const tempPackage = {
      name: '',
      type: '',
      packageId: '',
      os: '',
      cpuArch: CpuArch.AARCH64,
      packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
      packageVersionNum: '',
      packageUrl: '',
      packagePath: {},
      remark: '',
      realPath: '',
      hostLabel: false
    }
    // 这里优化
    list.page.total = res.total
    res.rows.forEach(item => {
      tempPackage.name = item.name
      tempPackage.type = item.type
      tempPackage.packageId = item?.packageId
      tempPackage.os = item.os
      tempPackage.cpuArch = item.cpuArch
      tempPackage.packageVersion = item?.packageVersion || ''
      tempPackage.packageVersionNum = item?.packageVersionNum || ''
      tempPackage.realPath = item.realPath ? item.realPath : item.packagePath?.realPath
      tempPackage.packageUrl = item.packageUrl
      tempPackage.packagePath = item.packagePath
      if (item.source && item.source === 'online') {
        tempPackage.hostLabel = true
      } else if (item.source && item.source === 'offline') {
        tempPackage.hostLabel = false
      } else if (item.source === null) {
        let sysFilNam = item.packageUrl?.split('/')?.pop()
        if (item.fileName && item.fileName === sysFilNam) {
          tempPackage.hostLabel = true
        } else {
          tempPackage.hostLabel = false
        }
      } else {
        console.log('get package info error')
      }
      list.data.push({ ...tempPackage })
    })
  }).finally(() => {
    list.loading = false
    addSearchPackage(list.page.total)
  }).catch(error => {
    console.error('522' + error)
  })
})

// web socket
const processVisible = ref(false)
const percentLoading = ref(false)

const uploadName = ref('')
const downloadStart = (name) => {
  uploadName.value = name
}
const lastProcess = ref(0);
const nextProcess = ref(0);
const timer = ref<any>(null)
const webSocketOpen = (type: string, data?: KeyValue, addOptionFlag?:number) => {
  const socketKey = new Date().getTime()
  const wsPrefix = window.location.protocol.includes('https') ? 'wss' : 'ws'
  const socketUrl = `${wsPrefix}://${window.location.host}/ws/base-ops/downloadPackage_${socketKey}`
  const websocket = new WebSocket(socketUrl)
  const wsBusinessId = `downloadPackage_${socketKey}`
  websocket.onopen = function (event) {
  }
  addPackRef.value?.open(type, data, addOptionFlag, wsBusinessId)
  websocket.onmessage = function (event) {
    const messageData = event.data
    NotificaionRef.value?.createOrUpdateNotification(wsBusinessId, messageData, data?.name || uploadName.value)
    if (messageData === 'File download Failed') {
      Message.error({
        content: t('components.Package.5mtcyb0rty52')
      })
      handleOk()
      websocket.close()
      NotificaionRef.value.closeNotifiCation(wsBusinessId);
      clearInterval(timer.value)
    } else {
      if (!isNaN(Number(messageData))) {
        const percent = Number(messageData)
        nextProcess.value = percent;

        clearInterval(timer.value);
        timer.value = setInterval(() => {
          // The latest value is updated every 10 seconds, and if there is no change from the old value, the dialog and the overlay box are prompted and closed
          if (nextProcess.value === 1) {
            lastProcess.value = 0;
            nextProcess.value = 0;
          } else if (nextProcess.value.toString() === lastProcess.value.toString()) {
            let warnningMsg = setTimeout(() => {
              Message.warning({
                content: t('components.Package.5mtcyb0rty57')
              })
              NotificaionRef.value.closeNotifiCation(wsBusinessId);
              clearTimeout(warnningMsg)
            }, 10000)
            websocket.close();
            clearInterval(timer.value)
          } else {
            // update lastProcess value
            lastProcess.value = nextProcess.value
          }
        }, 10000)
        if (percent === 1) {
          percentLoading.value = false
          closeSocketAndNotification(websocket, wsBusinessId)
        }
      } else if (messageData === 'DOWNLOAD_FINISH') {
        percentLoading.value = false
        closeSocketAndNotification(websocket, wsBusinessId)
      } else {
        console.error('WebSocket error')
        websocket.close()
        closeSocketAndNotification(websocket, wsBusinessId)
      }
    }
  }
  websocket.onerror = function (error) {
    console.error('WebSocket error:', error)
    closeSocketAndNotification(websocket, wsBusinessId)
  }
}

// Extracted events to close the socket and notification overlays
const closeSocketAndNotification = (websocket: any, wsBusinessId: string) => {
  lastProcess.value = 0;
  nextProcess.value = 0;
  websocket?.close();
  NotificaionRef.value.closeNotifiCation(wsBusinessId);
  clearInterval(timer.value);
}

const handleOk = () => {
  processVisible.value = false
  parentTags.value.forEach(e => e.children = [])
  fetchOs()
  fetchArch()
  fetchVersion()
  fetchVersionNum()
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

init()
</script>

<style lang="less" scoped>
// Installation package management style
.main-bd.packageManage {
  padding: 24px;
  display: flex;
  flex-direction: column;

  .control-wrap {
    margin-bottom: 16px;
  }

  .package-list {
    margin-top: 16px;
    flex: 1;
    display: flex;
    flex-direction: column;
    .el-table {
      flex: 1;
    }
    .el-pagination {
      height: 32px;
      margin-top: 16px;
    }
  }
}
</style>
