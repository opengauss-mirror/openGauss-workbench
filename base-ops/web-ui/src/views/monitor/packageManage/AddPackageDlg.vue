<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :ok-loading="submitLoading" :visible="data.show"
    :title="data.title" :modal-style="{ width: '550px' }" @ok="handleBeforeOk" @cancel="close">
    <a-form :model="data.formData" :rules="data.rules" ref="formRef" auto-label-width>
      <a-form-item field="os" :label="$t('packageManage.AddPackageDlg.5myq6nnea2w0')" validate-trigger="change">
        <a-select v-model="data.formData.os" :placeholder="$t('packageManage.AddPackageDlg.5myq6nneap40')"
          @change="osChange">
          <a-option v-for="(item, index) of data.osList" :key="index" :value="item.value" :label="item.label" />
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('packageManage.AddPackageDlg.else1')" validate-trigger="change">
        <a-select v-model="data.formData.cpuArch" :placeholder="$t('packageManage.AddPackageDlg.5myq6nneaw40')">
          <a-option v-for="(item, index) of data.cpuArchList" :key="index" :value="item.value" :label="item.label" />
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('packageManage.AddPackageDlg.5myq6nneb180')" validate-trigger="change">
        <a-select v-model="data.formData.packageVersion" :placeholder="$t('packageManage.AddPackageDlg.5myq6nneb5w0')"
          :disabled="data.isViewVersion">
          <a-option v-for="(item, index) of data.packageVersionList" :key="index" :value="item.value"
            :label="item.label" />
        </a-select>
      </a-form-item>
      <a-form-item field="packageVersionNum" :label="$t('packageManage.AddPackageDlg.5myq6nnebag0')"
        validate-trigger="blur">
        <a-input v-model="data.formData.packageVersionNum"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebew0')"></a-input>
      </a-form-item>
      <a-form-item field="packageUrl" :label="$t('packageManage.AddPackageDlg.5myq6nnebis0')" validate-trigger="blur">
        <a-input v-model="data.formData.packageUrl"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebn40')"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { reactive, ref, nextTick } from 'vue'
import { addPackage, editPackage } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: '',
  isViewVersion: false,
  formData: {
    packageId: '',
    os: 'centos',
    cpuArch: 'x86_64',
    packageVersion: 'MINIMAL_LIST',
    packageVersionNum: '3.0.0',
    packageUrl: ''
  },
  rules: {},
  osList: [],
  cpuArchList: [],
  packageVersionList: []
})

const emits = defineEmits([`finish`])

const submitLoading = ref<boolean>(false)
const formRef = ref<null | FormInstance>(null)
const handleBeforeOk = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      if (data.formData.packageId) {
        // edit
        editPackage(data.formData.packageId, data.formData).then(() => {
          Message.success({ content: `Modified success` })
          emits(`finish`)
          close()
        }).finally(() => {
          submitLoading.value = false
        })
      } else {
        addPackage(data.formData).then(() => {
          Message.success({ content: `Create success` })
          emits(`finish`)
          close()
        }).finally(() => {
          submitLoading.value = false
        })
      }
    }
  })
}

const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const osChange = () => {
  if (data.formData.os === 'centos') {
    data.cpuArchList = [
      { label: 'x86_64', value: 'x86_64' }
    ]
    data.formData.cpuArch = 'x86_64'
  } else if (data.formData.os === 'openEuler') {
    data.cpuArchList = [
      { label: 'x86_64', value: 'x86_64' },
      { label: 'aarch64', value: 'aarch64' }
    ]
  }
}


const open = (type: string, packageData?: KeyValue, defaultVersion?: string) => {
  data.show = true
  if (type === 'create') {
    data.title = t('packageManage.AddPackageDlg.5myq6nnebrc0')
    // init formData
    Object.assign(data.formData, {
      packageId: '',
      os: 'centos',
      cpuArch: 'x86_64',
      packageVersion: 'MINIMAL_LIST',
      packageVersionNum: '3.0.0',
      packageUrl: ''
    })
    if (defaultVersion) {
      data.isViewVersion = true
      data.formData.packageVersion = defaultVersion
    }
  } else {
    data.title = t('packageManage.AddPackageDlg.5myq6nnebwo0')
    if (packageData) {
      Object.assign(data.formData, {
        packageId: packageData.packageId,
        os: packageData.os,
        cpuArch: packageData.cpuArch,
        packageVersion: packageData.packageVersion,
        packageVersionNum: packageData.packageVersionNum,
        packageUrl: packageData.packageUrl
      })
    }
  }
  data.osList = [
    { label: 'centos', value: 'centos' },
    { label: 'openEuler', value: 'openEuler' }
  ]
  data.cpuArchList = [
    { label: 'x86_64', value: 'x86_64' }
  ]
  data.packageVersionList = [
    { label: t('packageManage.AddPackageDlg.5myq6nnec400'), value: 'ENTERPRISE' },
    { label: t('packageManage.AddPackageDlg.5myq6nnec8c0'), value: 'MINIMAL_LIST' },
    { label: t('packageManage.AddPackageDlg.5myq6nnecc40'), value: 'LITE' }
  ]
  data.rules = {
    packageVersionNum: [{ required: true, 'validate-trigger': 'blur', message: t('packageManage.AddPackageDlg.5myq6nnebew0') }],
    packageUrl: [{ required: true, 'validate-trigger': 'blur', message: t('packageManage.AddPackageDlg.5myq6nnebn40') }]
  }
  if (packageData) {
    Object.assign(data.formData, packageData)
  }
}

defineExpose({
  open
})

</script>
