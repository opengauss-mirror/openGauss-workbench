<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :ok-loading="submitLoading"
    :visible="data.show"
    :title="data.title"
    :modal-style="{ width: '50vw' }"
    @ok="handleBeforeOk"
    @on-cancel="close(false, $event)"
    @cancel="close(true, $event)"
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
          v-model="data.formData.packageVersion"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nneb5w0')"
          :disabled="data.isViewVersion"
        >
          <a-option
            v-for="(item, index) of data.packageVersionList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="packageVersionNum"
        :label="$t('packageManage.AddPackageDlg.5myq6nnebag0')"
      >
        <a-input
          v-model.trim="data.formData.packageVersionNum"
          :placeholder="$t('packageManage.AddPackageDlg.5myq6nnebew0')"
        ></a-input>
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
  getSysUploadPath,
  hasPkgName
} from '@/api/ops'
import { FileItem, Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import { DefaultOpenGauss, PackageType } from '@/types/resource/package'
import { CpuArch, OS, SysArch } from '@/types/os'
import { UploadInfo } from '@/types/resource/package'
import { Modal } from '@arco-design/web-vue'
import { getToken } from '@/utils/auth'
import dayjs from 'dayjs'

const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: '',
  isViewVersion: false,
  formData: {
    name: '',
    type: PackageType.OPENGAUSS,
    packageId: '',
    os: OS.CENTOS,
    cpuArch: CpuArch.AARCH64,
    packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
    packageVersionNum: '3.0.0',
    packageUrl: '',
    packagePath: {},
    remark: ''
  },
  rules: {},
  osList: [],
  cpuArchList: [],
  packageVersionList: [],
  fileList: [],
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
          params.append(key, data.formData[key])
        }
      })
      if (data.fileList.length > 0) {
        const file = data.fileList[0]
        // only init file need here
        if (file.status === 'init') {
          params.append('file', file.file)
        }
      }
      addPackage(params)
        .then(() => {
          emits(`finish`)
          close(false)
        })
        .finally(() => {
          submitLoading.value = false
        })
    }
  })
}

const close = (flag: boolean, e?: Event) => {
  if (data.fileList.length > 0 && data.fileList[0].status === 'done' && flag) {
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
  if (data.formData.os === OS.CENTOS) {
    data.cpuArchList = [{ label: CpuArch.X86_64, value: CpuArch.X86_64 }]
    data.formData.cpuArch = CpuArch.X86_64
  } else if (data.formData.os === OS.OPEN_EULER) {
    data.cpuArchList = [
      { label: CpuArch.X86_64, value: CpuArch.X86_64 },
      { label: CpuArch.AARCH64, value: CpuArch.AARCH64 }
    ]
  }
}

const open = (defaultVersion?: string) => {
  data.show = true
  data.title = t('packageManage.AddPackageDlg.5myq6nnebrc0')
  getSystemSetting()
  // init formData
  Object.assign(data.formData, {
    packageId: '',
    os: OS.CENTOS,
    cpuArch: CpuArch.X86_64,
    packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
    packageVersionNum: DefaultOpenGauss.VERSION,
    packageUrl: '',
    type: PackageType.OPENGAUSS
  })
  if (defaultVersion) {
    data.isViewVersion = true
    data.formData.packageVersion = defaultVersion
  }
  data.osList = [
    { label: OS.CENTOS, value: OS.CENTOS },
    { label: OS.OPEN_EULER, value: OS.OPEN_EULER }
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
    packagePath: [
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve, reject) => {
            if (data.fileList.length <= 0) {
              cb(t('packageManage.AddPackageDlg.5myq6nnecc50'))
              resolve(false)
              return
            } else {
              cb()
              resolve(true)
            }
          })
        }
      }
    ]
  }
  generateName()
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
    Message.error(file.response.msg)
  }
}

const getSystemSetting = () => {
  getSysUploadPath().then((res) => {
    data.systemUploadPath = res.data
  })
}

defineExpose({
  open
})

const generateName = () => {
  if (
    data.type === 'create' &&
    !data.isNameDirty &&
    data.formData.packageVersionNum
  ) {
    let name = `${data.formData.type}-${data.formData.os}-${data.formData.cpuArch}`
    if (data.formData.type === PackageType.OPENGAUSS) {
      name += `-${data.formData.packageVersion}`
    }
    name += `-${data.formData.packageVersionNum}-${dayjs().format(
      'YYYYMMDDHHmmss'
    )}`
    data.formData.name = name
  }
}
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
