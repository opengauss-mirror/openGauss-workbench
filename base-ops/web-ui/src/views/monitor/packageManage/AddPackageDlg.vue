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
        <a-select v-model="data.formData.cpuArch" :placeholder="$t('packageManage.AddPackageDlg.5myq6nneaw40')"
          @change="getPackageUrl">
          <a-option v-for="(item, index) of data.cpuArchList" :key="index" :value="item.value" :label="item.label" />
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('packageManage.AddPackageDlg.5myq6nneb180')" validate-trigger="change">
        <a-select v-model="data.formData.packageVersion" :placeholder="$t('packageManage.AddPackageDlg.5myq6nneb5w0')"
          :disabled="data.isViewVersion" @change="getPackageUrl">
          <a-option v-for="(item, index) of data.packageVersionList" :key="index" :value="item.value"
            :label="item.label" />
        </a-select>
      </a-form-item>
      <a-form-item field="packageVersionNum" :label="$t('packageManage.AddPackageDlg.5myq6nnebag0')"
        validate-trigger="blur">
        <a-input v-model="data.formData.packageVersionNum" @blur="getPackageUrl"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebew0')"></a-input>
      </a-form-item>
      <a-form-item field="packageUrl" :label="$t('packageManage.AddPackageDlg.5myq6nnebis0')" validate-trigger="blur">
        <a-textarea v-model="data.formData.packageUrl" auto-size
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebn40')" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { reactive, ref, nextTick, computed } from 'vue'
import { addPackage, editPackage } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { OpenGaussVersionEnum } from '@/types/ops/install'
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
    packageUrl: '',
    urlPrefix: 'https://opengauss.obs.cn-south-1.myhuaweicloud.com'
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
  getPackageUrl()
}

const getPackageUrl = () => {
  data.formData.packageUrl = `https://opengauss.obs.cn-south-1.myhuaweicloud.com/${data.formData.packageVersionNum}/${getSysArch()}/${getPackageName()}`
}

const getSysArch = () => {
  if (data.formData.cpuArch === 'x86_64') {
    if (data.formData.os === 'openEuler') {
      return 'x86_openEuler'
    }
    return 'x86'
  }
  return 'arm'
}

const getPackageName = () => {
  let result = 'openGauss-'
  if (data.formData.packageVersion === OpenGaussVersionEnum.LITE) {
    result = result + 'Lite-' + data.formData.packageVersionNum + '-'
    if (data.formData.os === 'centos') {
      result += ('CentOS-' + data.formData.cpuArch + '.tar.gz')
    } else {
      result += ('openEuler-' + data.formData.cpuArch + '.tar.gz')
    }
  } else if (data.formData.packageVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
    result = result + data.formData.packageVersionNum + '-'
    if (data.formData.os === 'centos') {
      result += 'CentOS-64bit.tar.bz2'
    } else {
      result += 'openEuler-64bit.tar.bz2'
    }
  } else {
    result = result + data.formData.packageVersionNum + '-'
    if (data.formData.os === 'centos') {
      result += 'CentOS-64bit-all.tar.gz'
    } else {
      result += 'openEuler-64bit-all.tar.gz'
    }
  }
  return result
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
    getPackageUrl()
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
    urlPrefix: [{ required: true, 'validate-trigger': 'blur', message: t('packageManage.AddPackageDlg.5myq6nnebn40') }],
    packageUrl: [
      { required: true, message: t('packageManage.AddPackageDlg.5myq6nnebn40') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /http(s)?:\/\/([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('packageManage.AddPackageDlg.else2'))
              resolve(false)
            }
          })
        }
      }
    ]
  }
}

defineExpose({
  open
})

</script>
