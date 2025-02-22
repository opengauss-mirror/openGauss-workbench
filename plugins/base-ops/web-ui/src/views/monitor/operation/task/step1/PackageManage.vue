<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :modal-style="{ width: '1000px' }"
    @cancel="close"
    @ok="submit"
  >
    <div class="app-container">
      <div>
        <a-cascader
          v-model="selectedOptionsValue.value"
          labelInValue
          placeholder="请输入或者选择表"
          :options="parentTags"
          :field-names="{label:'value', value:'name', children:'children' }"
          multiple
          mode="tags"
          size="large"
          expand-trigger="click"
          :show-search="true"
          :show-all-levels="true"
          :selected-row-keys="selectedRowKeys"
          @change="searchTag"
          disabled
        >
        </a-cascader>
      </div>
      <div>
        <a-table
          row-key="packageId"
          :data="list.data"
          :row-selection="rowSelection"
          :loading="list.loading"
          @selection-change="handleSelected"
          :scroll="{ y: autoplay }"
          v-model:selected-keys="selectedRowKeys"
          @submit="submit"
        >
          <template #columns>
            <a-table-column title="安装包名称" data-index="name" :width="200">
              <template #cell="{record}">{{record.name}}</template>
            </a-table-column>
            <a-table-column title="安装包类型" data-index="type" :width="100">
              <template #cell="{ record }">openGauss</template>
            </a-table-column>
            <a-table-column title="所属系统" data-index="os" :width="100">
              <template #cell="{ record }">{{record.os}}</template>
            </a-table-column>
            <a-table-column title="CPU架构" data-index="cpuArch" :width="100">
              <template #cell="{ record }">{{record.cpuArch}}</template>
            </a-table-column>
            <a-table-column title="版本名称/版本号" data-index="packageVersion" :width="150">
              <template #cell="{ record }" >
                <div v-if="record.packageVersion === OpenGaussVersionEnum.MINIMAL_LIST">极简版/{{record.packageVersionNum}}</div>
                <div v-else-if="record.packageVersion === OpenGaussVersionEnum.LITE">轻量版/{{record.packageVersionNum}}</div>
                <div  v-else>企业版/{{record.packageVersionNum}}</div>
              </template>
            </a-table-column>
            <a-table-column title="操作" data-index="operation" :width="150">
              <template #cell="{ record }">
                <div class="flex-row-start">
                  <a-link class="mr" @click="checkPack(record)">检查</a-link>
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
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">

import { Message } from '@arco-design/web-vue/es/index'
import { reactive, ref, onUnmounted, defineEmits, toRaw} from 'vue'
import {checkPkg, delPackage, delPackageV2, getPackageList} from '@/api/ops'
import { useI18n } from 'vue-i18n'
import {CpuArch, OS} from "@/types/os"
import {OpenGaussVersionEnum} from "@/types/ops/install"
import {KeyValue} from "@antv/x6/lib/types";

const { t } = useI18n()
const rowSelection = {type: 'radio'}
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
const data = reactive({
  selectedData: {},
  show: false,
  title: '安装包管理',
  formData: {
    packageId:'',
    os:'',
    osVersion:'',
    cpuArch: '',
    packageVersion: '',
    packageVersionNum: ''
  },
  selectedData: {}
})


const searchFormData = new FormData

const handleSelected = (keys: (string | number)[]) => {
  selectedRowKeys.value = [keys]
  list.selectedpackageIds = keys
  const findOne = list.data.find((item: KeyValue) => {
    return item.packageId === keys
  })
  if (findOne) {
    data.selectedData[keys] = findOne
  }
}

const checkPack = (record: KeyValue) => {
  checkPkg(record.packageId).then((res: KeyValue) => {
    if (res.data) {
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
  init()
}

const deleteRows = (record: KeyValue) => {
  let templist = []
  templist.push(record.packageId)
  delPackageV2(templist).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: '删除成功'
      })
      getListData()
    }
  }) .catch(error => {
    console.error('310' + error)
  })
  init()
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

// init
const selectedRowKeys = ref([])
const open = (
  packageData: KeyValue,
  packageId: string
) => {
  data.formData.packageId = ''
  data.formData.os = packageData.os
  data.formData.osVersion = packageData.osVersion
  data.formData.cpuArch = packageData.cpuArch
  data.formData.packageVersion = packageData.packageVersion
  data.formData.packageVersionNum = packageData.packageVersionNum
  handleSelected(packageId)
  init()
  data.show = true
}

defineExpose({
  open
})
const init = () => {
  fetchSearchTag()
  getListData()
}

const selectedOptionsValue = reactive({value:''})
const fetchSearchTag = () => {
  selectedOptionsValue.value = []
  selectedOptionsValue.value.push('操作系统/' + data.formData.os)
  selectedOptionsValue.value.push('系统架构/' + data.formData.cpuArch)
  selectedOptionsValue.value.push('版本名称/' + data.formData.packageVersion)
  selectedOptionsValue.value.push('版本号/' + data.formData.packageVersionNum)
}

const emits = defineEmits(['packageIDSelected'])
const submit = () => {
  if (data.selectedData.length < 1) {
    Message.error('请选择安装包后再确认')
  } else {
    const tempselectedRowKeys = selectedRowKeys.value? selectedRowKeys.value: '1'
    emits('packageIDSelected', tempselectedRowKeys)
    data.show = false
  }


}
const close = () => {
  console.log('关闭')
  data.show = false
}

const getListData = () => new Promise(resolve => {
  const name = ''
  const os = data.formData.os.toString()
  const cpuArch = data.formData.cpuArch.toString()
  const openGaussVersion = data.formData.packageVersion
  const openGaussVersionNum = data.formData.packageVersionNum
  list.loading = true
  getPackageList({
    name:name,
    os: os,
    osVersion: data.formData.osVersion,
    cpuArch: cpuArch,
    openGaussVersion: openGaussVersion,
    openGaussVersionNum: openGaussVersionNum,
  }).then((res) => {
    list.data = []
    const tempPackage = {
      name: '',
      type: '',
      packageId: '',
      os: '',
      cpuArch: '',
      packageVersion: '',
      packageVersionNum: '',
      packageUrl: '',
      packagePath: {},
      remark: '',
      hostLabel: ''
    }
    res.forEach(item => {
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
    })
  }) .finally(() => {
    list.loading = false
  }) .catch(error => {
    console.error('522' + error)
  })
})

</script>

<style>
.arco-select-view-multiple.arco-select-view-disabled .arco-select-view-tag {
  color: var(--color-text-1);
  background-color: var(--color-fill-3);
}
</style>
