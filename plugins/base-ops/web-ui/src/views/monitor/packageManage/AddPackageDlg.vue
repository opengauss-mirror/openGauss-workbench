<template>
  <a-modal
    unmount-on-close
    :mask-closable="false"
    :esc-to-close="false"
    :ok-loading="submitLoading"
    :visible="data.show"
    :title="data.title"
    :modal-style="{ width: '50vw' }"
    @ok="handleBeforeOk"
    @on-cancel="close(false, $event)"
    @cancel="close(false, $event)"
  >
    <a-form
      :model="data.formData"
      :rules="data.rules"
      ref="formRef"
      auto-label-width
    >
      <a-form-item
        field="name"
        :label="$t('packageManage.AddPackageDlg.5myq5c8zpu93')"
      >
        <a-input
          v-model="data.formData.name"
          :placeholder="$t('packageManage.AddPackageDlg.5myq5c8zpu95')"
          @input="data.isNameDirty = true"
        />
      </a-form-item>
      <a-form-item field="type" :label="$t('packageManage.index.5myq5c8zpu83')">
        <a-select
          v-model="data.formData.type"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nneap41')"
          @change="onTypeChange"
        >
          <a-option
            v-for="(item, index) of data.typeList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="os"
        :label="$t('packageManage.AddPackageDlg.5myq6nnea2w0')"
      >
        <a-select
          v-model="data.formData.os"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nneap40')"
          @change="osChange"
        >
          <a-option
            v-for="(item, index) of data.osList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('packageManage.AddPackageDlg.else1')">
        <a-select
          v-model="data.formData.cpuArch"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nneaw40')"
          @change="getPackageUrl"
        >
          <a-option
            v-for="(item, index) of data.cpuArchList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('packageManage.AddPackageDlg.5myq6nneb180')">
        <a-select
          v-if="data.formData.type === PackageType.OPENGAUSS"
          v-model="data.formData.packageVersion"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nneb5w0')"
          :disabled="data.isViewVersion"
          @change="getPackageUrl"
        >
          <a-option
            v-for="(item, index) of data.packageVersionList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
        <a-input
          v-else
          v-model="data.formData.packageVersion"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nneb5w0')"
        />
      </a-form-item>
      <a-form-item
        field="packageVersionNum"
        :label="$t('packageManage.AddPackageDlg.5myq6nnebag0')"
      >
        <a-input
          v-model="data.formData.packageVersionNum"
          @blur="getPackageUrl"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebew0')"
        ></a-input>
      </a-form-item>
      <a-form-item
        v-if="data.formData.type === PackageType.OPENGAUSS"
        field="packageUrl"
        :label="$t('packageManage.AddPackageDlg.5myq6nnebis0')"
      >
        <a-textarea
          v-model.trim="data.formData.packageUrl"
          auto-size
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebn40')"
        />
      </a-form-item>
      <a-form-item
        field="packagePath"
        :label="$t('packageManage.AddPackageDlg.5myq6nnebn41')"
      >
        <a-upload
          ref="uploadRef"
          v-model:file-list="data.fileList"
          :limit="1"
          :show-file-list="true"
          :auto-upload="false"
          draggable
          @change="handleFileChange"
          @before-remove="handleBeforeRemove"
          @success="handleUploadSuccess"
        >
          <template #upload-button>
            <div class="upload-info flex-col">
              <div class="flex-col">
                <div class="upload-icon">
                  <icon-plus :style="{ fontSize: '48px', color: '#86909C' }" />
                </div>
                <div class="tips-1">
                  <span>{{
                    $t('packageManage.AddPackageDlg.5myq6nnecc47')
                  }}</span>
                  <div v-if="data.systemUploadPath">
                    {{
                      $t('packageManage.AddPackageDlg.5myq6nnecc48') +
                      data.systemUploadPath +
                      $t('packageManage.AddPackageDlg.5myq6nnecc49')
                    }}
                  </div>
                </div>
              </div>
            </div>
          </template>
        </a-upload>
      </a-form-item>
      <a-form-item
        field="remark"
        :label="$t('packageManage.AddPackageDlg.5myq6nnebn44')"
      >
        <a-textarea
          v-model="data.formData.remark"
          :auto-size="{ minRows: 2, maxRows: 2 }"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebn43')"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref } from 'vue'
import {
  addPackage,
  analysisPkg,
  delPkgTar,
  editPackage,
  getSysUploadPath,
  hasPkgName, listSupportOsName
} from '@/api/ops'
import { FileItem, Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import { PackageType, DefaultOpenGauss } from '@/types/resource/package'
import { CpuArch, OS, SysArch } from '@/types/os'
import { UploadInfo } from '@/types/resource/package'
import { Modal } from '@arco-design/web-vue'
import dayjs from 'dayjs'

const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: '',
  isViewVersion: false,
  formData: {
    type: PackageType.OPENGAUSS,
    packageId: '',
    name: '',
    os: OS.CENTOS,
    cpuArch: CpuArch.AARCH64,
    packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
    packageVersionNum: DefaultOpenGauss.VERSION,
    packageUrl: '',
    packagePath: {},
    remark: '',
    urlPrefix: 'https://opengauss.obs.cn-south-1.myhuaweicloud.com'
  },
  rules: {},
  osList: [],
  centosBasedSet: [],
  openEulerBasedSet: [],
  cpuArchList: [],
  packageVersionList: [],
  typeList: [],
  fileList: [],
  type: '',
  systemUploadPath: '',
  isNameDirty: false
})

const emits = defineEmits([`finish`])

const submitLoading = ref<boolean>(false)
const formRef = ref<null | FormInstance>(null)
const handleBeforeOk = () => {
  formRef.value?.validate().then((result) => {
    if (!result) {
      submitLoading.value = true
      const params = new FormData()
      Object.keys(data.formData).map((key) => {
        if (key === 'packagePath') {
          params.append(key, JSON.stringify(data.formData[key]))
        } else {
          if (data.formData[key]) {
            params.append(key, data.formData[key])
          } else {
            params.append(key, '')
          }
        }
      })
      if (data.fileList.length > 0) {
        const file = data.fileList[0]
        // onlu init file need here
        if (file.status === 'init') {
          params.append('file', file.file)
        }
      }
      if (data.formData.packageId) {
        // edit
        editPackage(params)
          .then(() => {
            Message.success({ content: `Modified success` })
            emits(`finish`)
            close(false)
          })
          .finally(() => {
            submitLoading.value = false
          })
      } else {
        addPackage(params)
          .then(() => {
            Message.success({ content: `Create success` })
            emits(`finish`)
            close(false)
          })
          .finally(() => {
            submitLoading.value = false
          })
      }
    }
  })
}

const close = (flag: boolean, e?: Event) => {
  if (
    data.fileList.length > 0 &&
    data.fileList[0].status === 'done' &&
    data.type === 'create' &&
    flag
  ) {
    return new Promise((resolve, reject) => {
      Modal.confirm({
        title: t('packageManage.AddPackageDlg.5myq6nnecc41'),
        content: t('packageManage.AddPackageDlg.5myq6nnecc42'),
        onOk: () => {
          doClose()
        },
        onCancel: () => reject('cancel')
      })
    })
  } else {
    doClose()
  }
}

const doClose = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
    data.fileList = []
    data.formData.packagePath = {}
  })
}

const osChange = () => {
  if (data.formData.os === OS.All) {
    data.cpuArchList = [{ label: CpuArch.NOARCH, value: CpuArch.NOARCH }]
    data.formData.cpuArch = CpuArch.NOARCH
  } else if (data.centosBasedSet.includes(data.formData.os)) {
    data.cpuArchList = [{ label: CpuArch.X86_64, value: CpuArch.X86_64 }]
    data.formData.cpuArch = CpuArch.X86_64
  } else if (data.openEulerBasedSet.includes(data.formData.os)) {
    data.cpuArchList = [
      { label: CpuArch.X86_64, value: CpuArch.X86_64 },
      { label: CpuArch.AARCH64, value: CpuArch.AARCH64 }
    ]
  }
  getPackageUrl()
  generateName()
}

const onTypeChange = () => {
  formRef.value?.clearValidate()
  // will add package version in future
  switch (data.formData.type) {
    case PackageType.OPENGAUSS:
      data.formData.packageVersion = OpenGaussVersionEnum.MINIMAL_LIST
      data.formData.os = OS.CENTOS
      data.formData.cpuArch = CpuArch.X86_64
      data.formData.packageVersionNum = DefaultOpenGauss.VERSION
      getPackageUrl()
      break
    default:
      data.formData.packageVersion = ''
      data.formData.packageVersionNum = ''
      data.formData.os = OS.All
      data.formData.cpuArch = CpuArch.NOARCH
      data.formData.packageUrl = ''
      break
  }
  data.formData.name = ''
  data.isNameDirty = false
  generateName()
}

const getPackageUrl = () => {
  generateName()
  if (data.formData.type === PackageType.OPENGAUSS) {
    data.formData.packageUrl = `https://opengauss.obs.cn-south-1.myhuaweicloud.com/${
      data.formData.packageVersionNum
    }/${getSysArch()}/${getPackageName()}`
  } else {
    return ''
  }
}

const getSysArch = () => {
  if (data.formData.cpuArch === CpuArch.X86_64) {
    if (data.formData.os === OS.OPEN_EULER) {
      return SysArch.X86_OPEN_EULER
    }
    return SysArch.X86
  }
  return SysArch.ARM
}

const getPackageName = () => {
  let result = 'openGauss-'
  if (data.formData.packageVersion === OpenGaussVersionEnum.LITE) {
    result = result + 'Lite-' + data.formData.packageVersionNum + '-'
    if (data.centosBasedSet.includes(data.formData.os)) {
      result += 'CentOS-' + data.formData.cpuArch + '.tar.gz'
    } else {
      result += 'openEuler-' + data.formData.cpuArch + '.tar.gz'
    }
  } else if (
    data.formData.packageVersion === OpenGaussVersionEnum.MINIMAL_LIST
  ) {
    result = result + data.formData.packageVersionNum + '-'
    if (data.centosBasedSet.includes(data.formData.os)) {
      result += 'CentOS-64bit.tar.bz2'
    } else {
      result += 'openEuler-64bit.tar.bz2'
    }
  } else {
    result = result + data.formData.packageVersionNum + '-'
    if (data.centosBasedSet.includes(data.formData.os)) {
      result += 'CentOS-64bit-all.tar.gz'
    } else {
      result += 'openEuler-64bit-all.tar.gz'
    }
  }
  return result
}

const open = (
  type: string,
  packageData?: KeyValue,
  defaultVersion?: string
) => {
  data.show = true
  data.type = type
  getSystemSetting()
  if (type === 'create') {
    data.title = t('packageManage.AddPackageDlg.5myq6nnebrc0')
    // init formData
    Object.assign(data.formData, {
      packageId: '',
      os: OS.CENTOS,
      cpuArch: CpuArch.X86_64,
      packageVersion:
        packageData?.type === PackageType.OPENGAUSS || !packageData
          ? OpenGaussVersionEnum.MINIMAL_LIST
          : '',
      packageVersionNum:
        packageData?.type === PackageType.OPENGAUSS || !packageData
          ? DefaultOpenGauss.VERSION
          : '',
      packageUrl: '',
      type: packageData?.type ? packageData?.type : PackageType.OPENGAUSS
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
        name: packageData.name,
        os: packageData.os,
        cpuArch: packageData.cpuArch,
        packageVersion: packageData.packageVersion,
        packageVersionNum: packageData.packageVersionNum,
        packageUrl: packageData.packageUrl,
        packagePath: packageData.packagePath as UploadInfo,
        type: packageData.type,
        remark: packageData.remark
      })
      if (data.formData.packagePath?.realPath) {
        data.fileList = [
          {
            uid: '-1',
            name: data.formData.packagePath.name,
            url: data.formData.packagePath.realPath
          }
        ]
      }
    }
  }
  initData()
  generateName()
}

const getSystemSetting = () => {
  getSysUploadPath().then((res) => {
    data.systemUploadPath = res.data
  })
}

const initData = () => {
  data.osList = []
  listSupportOsName().then((res) => {
    data.centosBasedSet = res.data.centosBasedSet
    data.openEulerBasedSet = res.data.openEulerBasedSet
    for (let os of data.centosBasedSet) {
      data.osList.push({ label: os, value: os })
    }
    for (let os of data.openEulerBasedSet) {
      data.osList.push({ label: os, value: os })
    }
  })
  data.typeList = [
    { label: 'OpenGauss', value: PackageType.OPENGAUSS },
    { label: 'Zookeeper', value: PackageType.ZOOKEEPER },
    { label: 'ShardingProxy', value: PackageType.SHARDING_PROXY },
    { label: 'OpenLooKeng', value: PackageType.OPENLOOKENG },
    { label: 'DistributeDeploy', value: PackageType.DISTRIBUTE_DEPLOY }
  ]
  data.packageVersionList = [
    {
      label: t('packageManage.AddPackageDlg.5myq6nnec400'),
      value: OpenGaussVersionEnum.ENTERPRISE
    },
    {
      label: t('packageManage.AddPackageDlg.5myq6nnec8c0'),
      value: OpenGaussVersionEnum.MINIMAL_LIST
    },
    {
      label: t('packageManage.AddPackageDlg.5myq6nnecc40'),
      value: OpenGaussVersionEnum.LITE
    }
  ]
  data.rules = {
    packageVersionNum: [
      {
        required: true,
        'validate-trigger': 'blur',
        message: t('packageManage.AddPackageDlg.5myq6nnebew0')
      }
    ],
    name: [
      {
        required: true,
        message: t('packageManage.AddPackageDlg.5myq5c8zpu95')
      },
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve) => {
            if (data.formData.packageId) {
              resolve(true)
            }
            hasPkgName(value).then((res: KeyValue) => {
              if (res.data) {
                cb(t('packageManage.AddPackageDlg.5myq5c8zpu94'))
                resolve(false)
              } else {
                resolve(true)
              }
            })
          })
        }
      }
    ],
    urlPrefix: [
      {
        required: true,
        'validate-trigger': 'blur',
        message: t('packageManage.AddPackageDlg.5myq6nnebn40')
      }
    ],
    packageUrl: [
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve, reject) => {
            if (!value && data.fileList.length <= 0) {
              cb(t('packageManage.AddPackageDlg.5myq6nnecc43'))
              resolve(false)
              return
            }
            if (data.fileList.length <= 0 && !value) {
              cb(t('packageManage.AddPackageDlg.5myq6nnecc44'))
              resolve(false)
              return
            }
            if (data.fileList.length > 0 && !value) {
              cb()
              resolve(true)
              return
            }
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

const handleFileChange = () => {
  if (data.fileList.length > 0) {
    doAnalysisPkg(data.fileList[0].name)
  }
}

const doAnalysisPkg = (pkgName: string) => {
  analysisPkg(pkgName, data.formData.type).then((res) => {
    data.formData.os = res.data.os
    data.formData.cpuArch = res.data.cpuArch
    data.formData.packageVersionNum = res.data.packageVersionNum
    data.formData.packageVersion = res.data.packageVersion
    osChange()
  })
}

const handleBeforeRemove = (file: FileItem) => {
  return new Promise((resolve, reject) => {
    if (file.status === 'done') {
      Modal.confirm({
        title: t('packageManage.AddPackageDlg.5myq6nnecc45'),
        content: `${t('packageManage.AddPackageDlg.5myq6nnecc45')}${
          file.name
        }${t('packageManage.AddPackageDlg.5myq6nnecc46')}`,
        onOk: () => {
          delPkgTar(
            data.formData.packagePath?.realPath,
            data.formData.packageId
          ).then((res) => {
            data.formData.packagePath = {}
            emits(`finish`)
            resolve(true)
          })
        },
        onCancel: () => reject('cancel')
      })
    } else {
      resolve(true)
    }
  })
}

const handleUploadSuccess = (file: FileItem) => {
  if (file.response.code === 200) {
    data.formData.packagePath = file.response.data as UploadInfo
    Message.success('Upload success')
  } else {
    file.status = 'error'
    Message.error(file.response.msg)
  }
}

const generateName = () => {
  let os = data.openEulerBasedSet.includes(data.formData.os) ? 'openEuler' : 'centos'
  if (
    data.type === 'create' &&
    !data.isNameDirty &&
    data.formData.packageVersionNum
  ) {
    let name = `${data.formData.type}-${os}-${data.formData.cpuArch}`
    if (data.formData.type === PackageType.OPENGAUSS) {
      name += `-${data.formData.packageVersion}`
    }
    name += `-${data.formData.packageVersionNum}-${dayjs().format(
      'YYYYMMDDHHmmss'
    )}`
    data.formData.name = name
  }
}

defineExpose({
  open
})
</script>
<style lang="less" scoped>
.upload-info {
  background: var(--color-fill-2);
  border: 1px dotted var(--color-fill-4);
  height: 120px;
  width: 100%;
  border-radius: 2px;

  .tips-1 {
    font-size: 14px;
    color: var(--color-text-2);
    margin-bottom: 12px;

    .highlight {
      color: rgb(var(--primary-6));
    }
  }
}
:deep(.arco-upload-progress) {
  display: none !important;
}
</style>
