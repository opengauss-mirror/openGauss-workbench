<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="physical-list">
        <div class="flex-between mb-s">
          <div style="display:flex; flex-wrap: nowrap;">
            <a-button type="primary" class="mr" @click="addPackInstall('create')">新增安装包</a-button>
            <a-button type="primary" class="mr" @click="checkSelectedPack">批量检查</a-button>
            <a-button type="primary" class="mr" @click="deleteSelectedHosts">批量删除</a-button>
          </div>
        </div>
      </div>
    </div>
    <div>
      <a-cascader
        v-model="selectedOptionsValue.value"
        labelInValue
        placeholder="请输入或者选择表"
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
          <a-table-column title="安装包名称" data-index="name" :width="200">
            <template #cell="{record}">{{record.name}}</template>
          </a-table-column>
          <!--              physical.index.hostLabel为标签-->
          <a-table-column title="操作系统" data-index="os" :width="100">
            <template #cell="{ record }">{{record.os}}</template>
          </a-table-column>
          <a-table-column title="CPU架构" data-index="cpuArch" :width="100">
            <template #cell="{ record }">{{record.cpuArch}}</template>
          </a-table-column>
          <a-table-column title="openGauss版本" data-index="packageVersion" :width="150">
            <template #cell="{ record }">{{translateVersion(record.packageVersion)}}</template>
          </a-table-column>
          <a-table-column title="版本号" data-index="packageVersionNum" :width="100">
            <template #cell="{ record }">{{record.packageVersionNum}}</template>
          </a-table-column>
          <a-table-column title="安装来源" data-index="hostLabel" :width="100">
            <template #cell="{ record }">{{record.hostLabel}}</template>
          </a-table-column>
          <a-table-column title="下载地址/离线安装包" data-index="packageUrl" :width="200">
            <template #cell="{ record }">{{record.packageUrl}}</template>
          </a-table-column>
          <a-table-column title="操作" data-index="operation" :width="280">
            <template #cell="{ record }">
              <div class="flex-row-start">
                <a-link class="mr" @click="checkPack(record)">检查</a-link>
                <a-link class="mr" @click="addPackInstall('update', record)">更新</a-link>
                <a-popconfirm
                  :content="$t('是否确定删除？')"
                  type="warning"
                  :ok-text="$t('确定')"
                  :cancel-text="$t('取消')"
                  @ok="deleteRows(record)"
                >
                  <a-link status="danger">删除</a-link>
                </a-popconfirm>
              </div>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <div v-if="tableEmptyFlag"  style="text-align: center;">
        <p style="font-weight: bold;">未查询到该条件下的安装包</p><br>
        <p>您可以离线上传或者在线下载相应的安装包</p>
        <a-button class="-primary" type="text" @click="addPackInstall('create',searchInfoPackage,1)">离线上传</a-button>
        <a-button class="-primary" type="text" @click="addPackInstall('create',searchInfoPackage,0)">在线下载</a-button>
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
        :ok-text="$t('下载完成')"
        @ok="handleOk"
        @close="close"
      >
        <template #title>
          {{ $t('在线下载') }}
        </template>
        <a-progress size="large" :percent="currPercent" />
      </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue/es/index'
import { onMounted, reactive, ref, onUnmounted, watch, toRaw } from 'vue'
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
import useLocale from '@/hooks/locale'
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
  init()
}

const addPackSubmit = () => {
  processVisible.value = true
  if (wsBusinessId && wsBusinessId.value && wsBusinessId.value != '') {
    downloadPackage()
  }
  getListData(filter.pageSize, filter.pageNum, searchFormData)
}

const parentTags = ref([
  { name: 'os',
    value: '操作系统',
    children: [],
    disabled: false
  },
  { name: 'cpuArch',
    value: 'cpu架构',
    children: [],
    disabled: false
  },
  { name: 'packageVersion',
    value: 'openGauss版本',
    children: [],
    disabled: false
  },
  { name: 'packageVersionNum',
    value: '版本号',
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
  alertFlag.set('os', 3)
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
      Message.error('同一个父级标签只能选择一个子级标签,' + tag.name + '标签下有多项，仅保留第一项')
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

const { currentLocale } = useLocale()
const translateVersion = (version:OpenGaussVersionEnum) => {
  if (currentLocale.value === 'en-US') {
    switch (version) {
      case OpenGaussVersionEnum.ENTERPRISE:
        return 'Enterprise'
      case OpenGaussVersionEnum.MINIMAL_LIST:
        return 'Simplified'
      case OpenGaussVersionEnum.LITE:
        return 'Lite'
    }
  }
  switch (version) {
    case OpenGaussVersionEnum.ENTERPRISE:
      return '企业版'
    case OpenGaussVersionEnum.MINIMAL_LIST:
      return '极简版'
    case OpenGaussVersionEnum.LITE:
      return '轻量版'
  }
}

const checkPack = (record: KeyValue) => {
  let templist = []
  templist.push(record.packageId)
  checkPackage(templist).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: '检查完成'
      })
    } else {
      Message.error({
        content: '检查未完成'
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
    Message.warning(t('请至少选择一个安装包'))
  }
}

const checkPackMul = (records: any) => {
  checkPackage(records).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: '检查完成'
      })
    } else {
      Message.error({
        content: '检查失败'
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
        content: '删除成功'
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
    Message.warning(t('请至少选择一个安装包'))
  }
}

const deleteMultipleRows = (records: KeyValue) => {
  delPackageV2(records).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: '已删除全部选择安装包'
      })
    } else {
      Message.error({
        content: '删除失败'
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
  searchInfoPackage.os = searchFormData.get('os') ? searchFormData.get('os') : OS.CENTOS
  searchInfoPackage.cpuArch = searchFormData.get('cpuArch') ? searchFormData.get('cpuArch') : CpuArch.X86_64
  searchInfoPackage.packageVersion = searchFormData.get('packageVersion') ? searchFormData.get('packageVersion') : OpenGaussVersionEnum.MINIMAL_LIST
  searchInfoPackage.packageVersionNum = searchFormData.get('packageVersionNum') ? searchFormData.get('packageVersionNum') : '5.0.0'
  searchInfoPackage.type = 'openGauss'
}

// init
const initOs = [OS.OPEN_EULER, OS.CENTOS, '麒麟系统']
const initArch = [CpuArch.X86_64, CpuArch.AARCH64]
const initVersion = [OpenGaussVersionEnum.MINIMAL_LIST, OpenGaussVersionEnum.LITE, OpenGaussVersionEnum.ENTERPRISE]
const init = () => {
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
      hostLabel: ''
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
      tempPackage.hostLabel = item.packageUrl? '在线下载' : '离线上传'
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
    websocket.onmessage = function(event) {
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
  init()
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
