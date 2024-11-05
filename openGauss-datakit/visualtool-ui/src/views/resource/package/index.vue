<template>
  <div class="app-container" id="packageList">
    <div class="main-bd">
      <div class="physical-list">
        <div class="flex-between mb-s">
          <div style="display:flex; flex-wrap: nowrap;">
            <a-button type="primary" class="mr" @click="addPackInstall('create')">{{
                $t('components.Package.5mtcyb0rty02')
              }}</a-button>
            <a-button type="primary" class="mr" @click="checkSelectedPack">{{
                $t('components.Package.5mtcyb0rty03')
              }}</a-button>
            <a-button type="primary" class="mr" @click="deleteSelectedHosts">{{
                $t('components.Package.5mtcyb0rty04')
              }}</a-button>
          </div>
        </div>
      </div>
    </div>
    <div>
      <a-cascader
        v-model="selectedOptionsValue.value"
        labelInValue
        :placeholder="$t('components.Package.5mtcyb0rty05')"
        :options="parentTags"
        :field-names="{label:'value', value:'name', children:'children' }"
        multiple
        allow-create
        allow-clear
        mode="tags"
        expand-trigger="click"
        :show-search="true"
        :show-all-levels="true"
        :inputmode="true"
        @change="searchTag"
      >
      </a-cascader>
    </div>
    <div>
      <a-table
        row-key="packageId"
        :data="list.data"
        :pagination="list.page"
        :row-selection="list.rowSelection"
        @page-change="currentPage"
        @page-size-change="pageSizeChange"
        :loading="list.loading"
        @selection-change="handleSelected"
      >
        <template #columns>
          <a-table-column :title="$t('components.Package.5mtcyb0rty06')" data-index="name" :width="200">
            <template #cell="{record}">{{record.name}}</template>
          </a-table-column>
          <!--              physical.index.hostLabel为标签-->
          <a-table-column :title="$t('components.Package.5mtcyb0rty07')" data-index="os" :width="100">
            <template #cell="{ record }">{{record.os}}</template>
          </a-table-column>
          <a-table-column :title="$t('components.Package.5mtcyb0rty08')" data-index="cpuArch" :width="100">
            <template #cell="{ record }">{{record.cpuArch}}</template>
          </a-table-column>
          <a-table-column :title="$t('components.Package.5mtcyb0rty09')" data-index="packageVersion" :width="150">
            <template #cell="{ record }">{{ $t(`components.Package.${record.packageVersion}`) }}</template>

          </a-table-column>
          <a-table-column :title="$t('components.Package.5mtcyb0rty10')" data-index="packageVersionNum" :width="100">
            <template #cell="{ record }">{{record.packageVersionNum}}</template>
          </a-table-column>
          <a-table-column :title="$t('components.Package.5mtcyb0rty11')" data-index="hostLabel" :width="100">
            <template #cell="{ record }">{{ record.hostLabel? $t('components.Package.5mtcyb0rty23'): $t('components.Package.5mtcyb0rty22') }}</template>
          </a-table-column>
          <a-table-column :title="$t('components.Package.5mtcyb0rty12')" data-index="packageUrl" :width="200">
            <template #cell="{ record }">{{record.packageUrl}}</template>
          </a-table-column>
          <a-table-column :title="$t('components.Package.5mtcyb0rty13')" data-index="operation" :width="280">
            <template #cell="{ record }">
              <div class="flex-row-start">
                <a-link class="mr" @click="checkPack(record)">{{$t('components.Package.5mtcyb0rty14')}}</a-link>
                <a-link class="mr" @click="addPackInstall('update', record)">{{$t('components.Package.5mtcyb0rty15')}}</a-link>
                <a-popconfirm
                  :content="$t('components.Package.5mtcyb0rty16')"
                  type="warning"
                  :ok-text="$t('components.Package.5mtcyb0rty17')"
                  :cancel-text="$t('components.Package.5mtcyb0rty18')"
                  @ok="deleteRows(record)"
                >
                  <a-link status="danger">{{$t('components.Package.5mtcyb0rty19')}}</a-link>
                </a-popconfirm>
              </div>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <div v-if="tableEmptyFlag"  style="text-align: center;">
        <p style="font-weight: bold;">{{$t('components.Package.5mtcyb0rty20')}}</p><br>
        <p>{{$t('components.Package.5mtcyb0rty21')}}</p>
        <a-button class="-primary" type="text" @click="addPackInstall('create',searchInfoPackage,1)">{{$t('components.Package.5mtcyb0rty22')}}</a-button>
        <a-button class="-primary" type="text" @click="addPackInstall('create',searchInfoPackage,0)">{{$t('components.Package.5mtcyb0rty23')}}</a-button>
      </div>
    </div>
    <addPack
      ref="addPackRef"
      @finish="addPackClose()"
      @close="addPackClose"
      @submit="addPackSubmit()"
    ></addPack>
    <div style="bottom: 20px; right: 20px;">
      <a-modal
        :mask-closable="false"
        :esc-to-close="false"
        v-model:visible="processVisible"
        :ok-text="$t('components.Package.5mtcyb0rty24')"
        @ok="handleOk"
        @close="close"
      >
        <template #title>
          {{ $t('components.Package.5mtcyb0rty23') }}
        </template>
        <a-progress size="large" :percent="currPercent" />
      </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue/es/index'
import { onMounted, reactive, ref, onUnmounted, watch, toRaw, computed } from 'vue'
import { cpuOption, memoryOption, diskOption } from './option'
import {
  hostPage,
  checkPackage,
  delPackage,
  getVersionNum,
  getPackageList,
  getPackagePage,
  download,
  packageOnlineUpdate,
  delPackageV2
} from '@/api/ops'
import Socket from '@/utils/websocket'
import WujieVue from 'wujie-vue3'
import { useI18n } from 'vue-i18n'
import AddPack from './AddPack.vue'
import { CpuArch, OS } from '../../../../../../plugins/base-ops/web-ui/src/types/os'
import { OpenGaussVersionEnum } from '@/types/ops/install'

const { t } = useI18n()
const { bus } = WujieVue

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
  socketArr: []
})
const data = ref({
  selectedData: {}
})

const addPackRef = ref<null | InstanceType<typeof AddPack>>(null)
const addPackInstall = (type: string, data?: KeyValue, addOptionFlag?:number) => {
  webSocketOpen(type, data, addOptionFlag)
}
const showDownloadPopup = ref(false)
const addPackClose = () => {
  parentTags.value.forEach(e => e.children = [])
  fetchOs()
  fetchArch()
  fetchVersion()
  fetchVersionNum()
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const addPackSubmit = () => {
  processVisible.value = true
  if (wsBusinessId.value && wsBusinessId.value != '') {
    downloadPackage()
  }
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}
const osValue = computed(() => t('components.Package.5mtcyb0rty07'))
const cpuValue = computed(() => t('components.Package.5mtcyb0rty08'))
const versionValue = computed(() => t('components.Package.5mtcyb0rty09'))
const versionNumValue = computed(() => t('components.Package.5mtcyb0rty10'))
const parentTags = ref([
  { name: 'os',
    value: osValue,
    children: [],
    disabled: false
  },
  { name: 'cpuArch',
    value: cpuValue,
    children: [],
    disabled: false
  },
  { name: 'packageVersion',
    value: versionValue,
    children: [],
    disabled: false
  },
  { name: 'packageVersionNum',
    value: versionNumValue,
    children: [],
    disabled: false
  }
])


const selectedOptionsValue = reactive({ value: '' })
const searchFormData = new FormData
const searchTag = (inputValue: any) => {
  const entries = searchFormData.entries()
  for (const [key, value] of entries) {
    searchFormData.delete(key)
  }
  const selectedTagGroups = ref<string[]>([])
  const alertFlag = new Map()
  alertFlag.set('packageName', 3)
  alertFlag.set('os', 2)
  alertFlag.set('cpuArch', 2)
  alertFlag.set('packageVersion', 3)
  alertFlag.set('packageVersionNum', countVersionNum)
  const alertNum = new Map([...alertFlag])
  inputValue.forEach(item => {
    parentTags.value.forEach(tag => {
      if (tag.children.some(piece => piece.name.includes(item))) {
        let tempname = alertFlag.get(tag.name) - 1
        alertFlag.set(tag.name, tempname)
        searchFormData.append(tag.name, item)
        tag.disabled = true
        selectedTagGroups.value.push(tag.name)
      }
    })
  })
  parentTags.value.forEach(tag => {
    if (alertFlag.get(tag.name) == 0) {
      Message.error(t('components.Package.5mtcyb0rty25', { tagName: tag.name }))
      let tempnum = alertNum.get(tag.name) - 1
      selectedOptionsValue.value.splice(-tempnum)
    }
    if (selectedTagGroups.value.includes(tag.name)) {
      tag.disabled = true
      tag.children.forEach(item => {
        if (item.name !== searchFormData.get(tag.name)) {
          item.disabled = true
        }
      })
    } else {
      tag.disabled = false
      tag.children.forEach(item => item.disabled = false)
    }
  })
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const handleSelected = (keys: (string | number)[]) => {
  list.selectedpackageIds = keys
  data.value.selectedData = {}
  list.selectedpackageIds.forEach((packageId: string | number) => {
    const findOne = list.data.find((item: KeyValue) => {
      return item.packageId === packageId
    })
    if (findOne) {
      data.value.selectedData[packageId] = findOne
    }
  })
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
  }) .catch(error => {
    console.error('260' + error)
  }) .finally(() => {
    getListData(filter.pageSize, filter.pageNum, searchFormData)
  })
}

const checkSelectedPack = () => {
  let selectedRecord = []
  for (let item in toRaw(data.value.selectedData)) {
    selectedRecord.push(item)
  }
  if (selectedRecord.length > 0) {
    checkPackMul(selectedRecord)
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
  }) .catch(error => {
    console.error('260' + error)
  }) .finally(() => {
    getListData(filter.pageSize, filter.pageNum, searchFormData)
  })
}

const deleteRows = (record: KeyValue) => {
  let templist = []
  templist.push(record.packageId)
  delPackageV2(templist).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: t('components.Package.5mtcyb0rty29')
      })
    }
  }) .catch(error => {
    console.error(error)
  }) .finally(() => {
    getListData(filter.pageSize, filter.pageNum, searchFormData)
  })
}

const deleteSelectedHosts = () => {
  let selectedRecord = []
  for (let item in toRaw(data.value.selectedData)) {
    selectedRecord.push(item)
  }
  if (selectedRecord.length > 0) {
    deleteMultipleRows(selectedRecord)
  } else {
    Message.warning(t('components.Package.5mtcyb0rty28'))
  }
}

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
    data.value.selectedData = {}
  }) .catch(error => {
    console.error('260' + error)
  }) .finally(() => {
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
const addSearchPackage = (total:number) => {
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
  }) .catch(error => {
    console.error(error)
  })
}

const dataColor = reactive<KeyValue>({
  selectedData: {},
  hasTestObj: {},
  colorYellow: '#FCEF92',
  colorRed: '#E41D1D'
})

onMounted(() => {
  dataColor.hasTestObj = {}
  bus.$on('opengauss-theme-change', (val: string) => {
    if (val === 'dark') {
      changeEchartsColor('dark')
    } else {
      changeEchartsColor('light')
    }
  })
})

const changeEchartsColor = (type: string) => {
  list.dataColor.forEach((item: KeyValue) => {
    if (type === 'dark') {
      item.cpuOption.color[1] = dataColor.colorYellow
      item.diskOption.color[1] = dataColor.colorYellow
      item.memoryOption.color[1] = dataColor.colorYellow
    } else {
      item.cpuOption.color[1] = dataColor.colorRed
      item.diskOption.color[1] = dataColor.colorRed
      item.memoryOption.color[1] = dataColor.colorRed
    }
  })
}

onUnmounted(() => {
  if (list.socketArr.length) {
    list.socketArr.forEach((item: Socket<any, any>) => {
      if (item) {
        item.destroy()
      }
    })
  }
})

const filter = {
  pageNum: 1,
  pageSize: 10
}
const currentPage = (e: number) => {
  filter.pageNum = e
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const getListData = (pageSize?:number, pageNum?:number, formData?: FormData) => new Promise(resolve => {
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
      hostLabel: false
    }
    res.rows.forEach(item => {
      tempPackage.name = item.name
      tempPackage.type = item.type
      tempPackage.packageId = item.packageId
      tempPackage.os = item.os
      tempPackage.cpuArch = item.cpuArch
      tempPackage.packageVersion = item.packageVersion
      tempPackage.packageVersionNum = item.packageVersionNum
      if (item.packageUrl) {
        tempPackage.packageUrl = item.packageUrl
      } else if (item.packagePath && item.packagePath.realPath) {
        tempPackage.packageUrl = item.packagePath.realPath
      } else {
        tempPackage.packageUrl = ''
      }
      tempPackage.packagePath = item.packagePath
      if (item.packageUrl && item.packagePath) {
        let name = item.packageUrl.split('/')
        let tempname = name.pop()
        if (item.packagePath.name == tempname) {
          tempPackage.hostLabel = true
        }
      }
      list.data.push({ ...tempPackage })
    })
    list.page.total = res.total
  }) .finally(() => {
    list.loading = false
    addSearchPackage(list.page.total)
    handleSelected(selectedRowKeys.value)
  }) .catch(error => {
    console.error('522' + error)
  })
})

// web socket
const downloadWs = ref<Socket<any, any> | undefined>()
const processVisible = ref(false)
const percentLoading = ref(false)
const currPercent = ref<number>(0)
const wsBusinessId = ref('')

watch(currPercent, (newValue) => {
  if (newValue === 100) {
    processVisible.value = false
  }
})

const webSocketOpen = (type: string, data?: KeyValue, addOptionFlag?:number) => {
  currPercent.value = 0
  const socketKey = new Date().getTime()
  const wsPrefix = window.location.protocol.includes('https') ? 'wss' : 'ws'
  const socketUrl = `${wsPrefix}://${window.location.host}/ws/base-ops/downloadPackage_${socketKey}`
  const websocket = new WebSocket(socketUrl)
  wsBusinessId.value = `downloadPackage_${socketKey}`
  websocket.onopen = function (event) {
    wsBusinessId.value = `downloadPackage_${socketKey}`
  }
  addPackRef.value?.open(type, data, addOptionFlag, wsBusinessId.value)
  downloadWs.value = websocket
  websocket.onmessage = function (event) {
    processVisible.value = true
    const messageData = event.data
    if (!isNaN(Number(messageData))) {
      const percent = Number(messageData)
      currPercent.value = percent
      if (percent === 100) {
        percentLoading.value = false
        websocket.close()
      }
    }
  }
  websocket.onerror = function (error) {
    console.error('WebSocket error:', error)
  }
}
const uploadPackageId = ref('')

const downloadPackage = () => {
  packageOnlineUpdate(uploadPackageId.value, wsBusinessId.value).then(() => {
    websocket.onmessage = function (event) {
      const messageData = event.data
      if (!isNaN(Number(messageData))) {
        const percent = Number(messageData)
        currPercent.value = percent
        if (percent === 100) {
          percentLoading.value = false
          downloadWs.value?.destroy()
        }
      }
    }
    simulateDownload()
    processVisible.value = true
    percentLoading.value = true
  }) .catch((error) => {
    console.error(error)
  })
}
const simulateDownload = async () => {
  try {
    while (currPercent.value < 100) {
      await new Promise(resolve => setTimeout(resolve, 1000))
    }
    downloadWs.value?.destroy()
  } catch (error) {
    console.error('Download failed:', error)
  }
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
const close = () => {
  processVisible.value = false
}

init()
</script>

<style lang="less" scoped>
:deep(.arco-form-layout-inline) {
  flex-wrap: nowrap;
}
.app-container .main-bd {
  min-height: auto;
}
</style>
