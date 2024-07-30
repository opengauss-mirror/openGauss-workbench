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
            <template #cell="{ record }">{{record.packageVersion}}</template>
          </a-table-column>
          <a-table-column title="版本号" data-index="packageVersionNum" :width="100">
            <template #cell="{ record }">{{record.packageVersionNum}}</template>
          </a-table-column>
          <a-table-column title="安装来源" data-index="hostLabel" :width="100">
            <template #cell="{ record }"></template>
          </a-table-column>
          <a-table-column title="下载地址/离线安装包" data-index="packageUrl" :width="200">
            <template #cell="{ record }">{{record.packageUrl}}</template>
          </a-table-column>
          <a-table-column title="操作" data-index="operation" :width="280">
            <template #cell="{ record }">
              <div class="flex-row-start">
                <a-link class="mr" @click="checkPack(record)">检查</a-link>
                <a-link class="mr" @click="addPackInstall('update', record)">修改</a-link>
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
  </div>
  <addPack
    ref="addPackRef"
    @finish="addPackClose()"
    @submit="addPackSubmit()"
  ></addPack>
</template>


<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue/es/index'
import { onMounted, reactive, ref, onUnmounted } from 'vue'
import { cpuOption, memoryOption, diskOption } from './option'
import {hostPage, checkPackage, delPackage, getVersionNum, getPackageList, getPackagePage} from '@/api/ops'
import Socket from '@/utils/websocket'
import WujieVue from 'wujie-vue3'
import { useI18n } from 'vue-i18n'
import AddPack from "./AddPack.vue"
import {CpuArch, OS} from "../../../../../../plugins/base-ops/web-ui/src/types/os"
import {OpenGaussVersionEnum} from "@/types/ops/install"

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
  console.log('enter addpackage:' + type)
  console.log(data)
  console.log(typeof(data))
  addPackRef.value?.open(type, data, addOptionFlag)
}
const showDownloadPopup = ref(false)
const addPackClose = () => {
  init()
  console.log('addPackClose')
}

const addPackSubmit = () => {
  console.log('addPackSubmit')
  showDownloadPopup.value = true
  init()
}

const parentTags = ref([
  { name: 'os',
    value:'操作系统',
    children: [],
    disabled: false
  },
  { name: 'cpuArch',
    value:'cpu架构',
    children: [],
    disabled: false
  },
  { name: 'packageVersion',
    value:'openGauss版本',
    children: [],
    disabled: false
  },
  { name: 'packageVersionNum',
    value:'版本号',
    children: [],
    disabled: false
  },
])

const selectedOptionsValue = reactive({value:''})
const searchFormData = new FormData
const searchTag = (inputValue: any) => {
  const entries = searchFormData.entries()
  for (const [key, value] of entries) {
    searchFormData.delete(key)
  }
  const selectedTagGroups =  ref<string[]>([])
  const alertFlag = new Map()
  alertFlag.set('packageName', 3)
  alertFlag.set('os', 3)
  alertFlag.set('cpuArch', 2)
  alertFlag.set('packageVersion', 3)
  alertFlag.set('packageVersionNum', countVersionNum)
  const alertNum = new Map([...alertFlag])
  inputValue.forEach(item => {
    parentTags.value.forEach(tag => {
      if (tag.children.some(piece => piece.name.includes(item))){
        let tempname = alertFlag.get(tag.name) - 1
        alertFlag.set(tag.name, tempname)
        console.log(tag.name, item)
        searchFormData.append(tag.name, item)
        tag.disabled = true
        selectedTagGroups.value.push(tag.name)
      }
    })
  })
  console.log(alertFlag)
  parentTags.value.forEach(tag => {
    if (alertFlag.get(tag.name)  == 0) {
      console.log(alertFlag + ' ' + tag.name)
      Message.error('同一个父级标签只能选择一个子级标签,' + tag.name + '标签下有多项，仅保留第一项')
      console.log(selectedOptionsValue)
      let tempnum = alertNum.get(tag.name) -1
      selectedOptionsValue.value.splice( -tempnum)
      console.log(selectedOptionsValue)
    }
    if (selectedTagGroups.value.includes(tag.name)) {
      console.log(tag.name + '已被禁止')
      tag.disabled = true
      tag.children.forEach(item =>{
        if (item.name !== searchFormData.get(tag.name)){
          item.disabled = true
          console.log(item.name)
        }
      })
    } else {
      tag.disabled = false
      tag.children.forEach(item => item.disabled = false)
    }
  })
  getListData(searchFormData)
}

const handleSelected = (keys: (string | number)[]) => {
  list.selectedpackageIds = keys
  list.selectedpackageIds.forEach((packageId: string | number) => {
    const findOne = list.data.find((item: KeyValue) => {
      return item.packageId === packageId
    })
    if (findOne) {
      data.value.selectedData[packageId] = findOne
    }
  })
  console.log(data.value)
}

const checkPack = (record: KeyValue) => {
  let tempPackageId = []
  tempPackageId.push(JSON.stringify(record.packageId))
  checkPackage(tempPackageId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: '检查通过'
      })
    } else {
      Message.error({
        content: '检查未通过'
    })
      }
  }) .catch(error => {
    console.error('260' + error)
  })
}

const checkSelectedPack = () => {
  const selectedRecords = list.selectedpackageIds.map((packageId: string | number) => {
    return data.value.selectedData[packageId]
  })
  if (selectedRecords.length > 0) {
    checkPackMul(selectedRecords)
  } else {
    Message.warning(t('请至少选择一个安装包'))
  }
}

const checkPackMul = (records: KeyValue) => {
  const checkPromises = records.map((record) => {
    let tempPackageId = []
    tempPackageId.push(JSON.stringify(record.packageId))
    return checkPackage(tempPackageId)
  })
  Promise.all(checkPromises).then((responses) => {
    let allSuccess = true
    responses.forEach((res) => {
      if (Number(res.code) !== 200) {
        allSuccess = false
      }
    })
    if (allSuccess) {
      Message.success(t('检查全部通过'))
    } else {
      Message.error(t('检查未全部通过'))
    }
    list.selectedpackageIds = []
    data.value.selectedData = {}
  }).catch(() => {
    console.error({
      content: '293 An error occurred while checking packages'
    })
  })
  getListData()
}

const deleteRows = (record: KeyValue) => {
  delPackage(record.packageId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: '删除成功'
      })
      getListData()
    }
  }) .catch(error => {
    console.error('310' + error)
  })
}

const deleteSelectedHosts = () => {
  const selectedRecords = list.selectedpackageIds.map((packageId: string | number) => {
    return data.value.selectedData[packageId]
  })
  if (selectedRecords.length > 0) {
    deleteMultipleRows(selectedRecords);
  } else {
    Message.warning(t('请至少选择一个安装包'))
  }
}

const deleteMultipleRows = (records: KeyValue) => {
  const deletePromises = records.map((record: { packageId: string, path: string }) => delPackage(record.packageId))
  Promise.all(deletePromises).then((responses) => {
    let allSuccess = true
    responses.forEach((res) => {
      if (Number(res.code) !== 200) {
        allSuccess = false
      }
    })
    if (allSuccess) {
      Message.success(t('已删除全部选择安装包'))
    } else {
      Message.error(t('删除失败'))
    }
    list.selectedpackageIds = []
    data.value.selectedData = []
  }).catch(() => {
    Message.error({
      content: '344 An error occurred while deleting packages'
    })
  })
  getListData()
}

const tableEmptyFlag = ref(false)
const searchInfoPackage = reactive( {
  packageId: '',
  name: '',
  os: '',
  cpuArch: '',
  packageVersion: '',
  packageVersionNum: '',
  packageUrl: '',
  packagePath: '',
  type: '',
  remark: '',
})
const addSearchPackage = (total:number) => {
  tableEmptyFlag.value = total === 0
  searchInfoPackage.os = searchFormData.get('os')?searchFormData.get('os'):OS.CENTOS
  searchInfoPackage.cpuArch = searchFormData.get('cpuArch')?searchFormData.get('cpuArch'):CpuArch.X86_64
  searchInfoPackage.packageVersion = searchFormData.get('packageVersion')?searchFormData.get('packageVersion'):OpenGaussVersionEnum.MINIMAL_LIST
  searchInfoPackage.packageVersionNum = searchFormData.get('packageVersionNum')?searchFormData.get('packageVersionNum'):'5.0.0'
  searchInfoPackage.type = 'openGauss'
}

// init
const initOs = [ OS.OPEN_EULER, OS.CENTOS, '麒麟系统']
const initArch = [CpuArch.X86_64, CpuArch.AARCH64]
const initVersion = [OpenGaussVersionEnum.MINIMAL_LIST, OpenGaussVersionEnum.LITE, OpenGaussVersionEnum.ENTERPRISE]
const init = () => {
  fetchOs()
  fetchArch()
  fetchVersion()
  fetchVersionNum()
  getListData()
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
  pageSize:1
}
const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData()
}

const rawList = ref([])

const getListData = (formData?: FormData) => new Promise(resolve => {
  let checkFormValue  = formData?formData.get('name'):null
  const name = checkFormValue !== null ? checkFormValue.toString() : ''
  checkFormValue =  formData?formData.get('os'):null
  const os = checkFormValue !== null ? checkFormValue.toString() : ''
  checkFormValue = formData?formData.get('cpuArch'):null
  const cpuArch = checkFormValue!== null ?checkFormValue.toString():''
  checkFormValue = formData?formData.get('packageVersion'):null
  const openGaussVersion = checkFormValue
  checkFormValue = formData?formData.get('packageVersionNum'):null
  const openGaussVersionNum = checkFormValue!== null ?checkFormValue.toString():''
  list.loading = true
  console.log('listAll')
  getPackagePage({
    name:name,
    os: os,
    cpuArch: cpuArch,
    openGaussVersion: openGaussVersion,
    openGaussVersionNum: openGaussVersionNum
  }).then((res) => {
    console.log(res)
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
      console.log(item)
      tempPackage.name = item.name
      tempPackage.type = item.type
      tempPackage.packageId =  item.packageId
      tempPackage.os = item.os
      tempPackage.cpuArch = item.cpuArch
      tempPackage.packageVersion = item.packageVersion
      tempPackage.packageVersionNum = item.packageVersionNum
      tempPackage.packageUrl = item.packageUrl
      tempPackage.packagePath = item.packagePath
      tempPackage.hostLabel = item.filename?'离线上传':'在线下载'
      list.data.push({...tempPackage})
      console.log(tempPackage)
    })
    list.page.total = res.total
  }) .finally(() => {
    list.loading = false
    addSearchPackage(list.page.total)
  }) .catch(error => {
    console.error('522' + error)
  })
})

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
