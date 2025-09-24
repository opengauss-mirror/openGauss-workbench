<template>
  <el-dialog v-model="data.show" :title="data.title"
    :ok-loading="data.loading" @submit="submit" @cancel="close" @finish="close" class="o-dialog--large">
    <template #footer>
      <div class="flex-between dialog-footer justify-center">
        <div>
          <el-button :loading="data.loading" type="primary" @click="debounceSubmit">{{ $t('components.Package.5mtcyb0rty17')
          }}</el-button>
          <el-button class="mr" @click="close">{{ $t('components.Package.5mtcyb0rty18') }}</el-button>
        </div>
      </div>
    </template>
    <div class="dlg-body" id="addPack">
      <el-form :model="data.formData" :rules="formRules" ref="formRef" auto-label-width label-width="120px" label-position="left" class="addPackForm">
        <el-form-item prop="sysTem" :label="$t('components.Package.5mtcyb0rty07')" validate-trigger="blur"
          @change="updateOsData" name="sysTem">
          <el-radio-group v-model="tempOs.value" button-style="solid" :disabled="editDisabledFlag">
            <el-radio-button :value="OS.OPEN_EULER">openEuler20.03</el-radio-button>
            <el-radio-button :value="OS.CENTOS">CentOS</el-radio-button>
            <el-radio-button :value="OS.All">openEuler22.03</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="arch" :label="$t('components.Package.5mtcyb0rty32')" validate-trigger="blur"
          @change="updateArchData" name="arch">
          <el-radio-group v-model="tempArch.value" :disabled="editDisabledFlag">
            <el-radio-button :value="CpuArch.X86_64">x86_64</el-radio-button>
            <el-radio-button :disabled="OS.CENTOS === tempOs.value" :value="CpuArch.AARCH64">aarch64</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="version" :label="$t('components.Package.5mtcyb0rty33')" validate-trigger="blur"
          @change="updateVersionData" name="version">
          <el-radio-group v-model="tempVersion.value" :disabled="editDisabledFlag">
            <el-radio-button :value="OpenGaussVersionEnum.MINIMAL_LIST">{{ $t('components.Package.MINIMAL_LIST')
            }}</el-radio-button>
            <el-radio-button :value="OpenGaussVersionEnum.LITE">{{ $t('components.Package.LITE') }}</el-radio-button>
            <el-radio-button :value="OpenGaussVersionEnum.ENTERPRISE">{{ $t('components.Package.ENTERPRISE')
            }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="packageVersionNum" :label="$t('components.Package.5mtcyb0rty10')" name="packageVersionNum">
          <div class="item">
            <el-select :disabled="editDisabledFlag" v-model="tempVersionNum" :suffix-icon="IconCaretDown" class="w440"
              :placeholder="$t('components.Package.5mtcyb0rty34')" :options="packageVersionNum"
              @search="searchVersionNum" @change="checkContains" filterable>
              <el-option v-for="item in packageVersionNum" :key="item" :label="item" :value="item"></el-option>
            </el-select>
            <br>
            <div class="ant-upload-hint" v-if="!contains">
              {{ $t('components.Package.5mtcyb0rty35') }}
              <el-button type="text" @click="toGaussDownloadPage">{{
                $t('components.Package.5mtcyb0rty36') }}</el-button>
              {{ $t('components.Package.5mtcyb0rty37') }}
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="name" :label="$t('components.Package.5mtcyb0rty06')" name="name">
          <el-input v-model="data.formData.name" :placeholder="$t('components.Package.5mtcyb0rty06')" class="w440 nameInput"
            @input="data.isNameDirty = true" :disabled="editDisabledFlag" :maxlength="255" />
        </el-form-item>
        <el-form-item prop="path" :label="$t('components.Package.5mtcyb0rty38')" name="path">
          <div class="item" style="width:100%">
            <el-input type="textarea" v-model="tempPackageUrl" show-count :maxlength="1000" class="textareaInput"
              :default-value="data.formData.packageUrl" disabled></el-input>
            <div class="ant-upload-hint" v-if="data.formData.packageUrl && netStatus">
              {{ $t('components.Package.5mtcyb0rty39') }}
              <el-button type="text" @click="uploadStatusChange">{{ $t('components.Package.5mtcyb0rty22') }}</el-button>
            </div>
            <div v-if="!netStatus" class="ant-upload-hint">{{ $t('components.Package.5mtcyb0rty40') }}</div>
          </div>
        </el-form-item>
        <el-form-item v-if="uploadStatusTag === true || !netStatus" prop="packagePath"
          :label="$t('components.Package.5mtcyb0rty41')" name="packagePath">
<!--          :tips="t('components.Package.5mtcyb0rty56')"-->
          <file-upload style="width: 100%;" :percentage="progressPercent" @changeFile="changeFile" ></file-upload>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, computed, watch, h, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { CpuArch, OS } from '../../../../../../plugins/base-ops/web-ui/src/types/os'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import {
  batchPackageOnline,
  getVersionNum,
  checkNetStatus,
  checkVersionNumber,
  delPkgTar, getSysUploadPath, hasPkgName, packageOnlineUpdate
} from '@/api/ops'
import { FileItem, Message, Modal } from '@arco-design/web-vue'
import message from '@arco-design/web-vue/es/message'
import axios from 'axios'
import { Path } from '@antv/x6'
import FileUpload from '@/components/file-upload'
import isValid = Path.isValid;
import { IconCaretDown } from '@computing/opendesign-icons'
import { debounce } from '@/utils/debounce'
const { t } = useI18n()

const data = reactive<KeyValue>({
  show: false,
  title: '',
  isViewVersion: false,
  formData: {
    name: '',
    type: 'openGauss',
    packageId: '',
    os: OS.CENTOS,
    osVersion: 7,
    cpuArch: CpuArch.AARCH64,
    packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
    packageVersionNum: '',
    packageUrl: '',
    packagePath: {},
    remark: ''
  },
  cpuArchList: [],
  packageVersionList: [],
  typeList: [],
  file: {},
  type: '',
  systemUploadPath: '',
  isNameDirty: false
})
interface UploadInfo {
  realPath: string,
  realName: string,
  name: string,
  file: File
}

// The first validation of the page is skipped during initialization
const initNameValidateSkipFlag = ref(true)

const tempVersionNum = ref('5.0.2')

const contains = ref(true)
const checkContains = (inputValue: any) => {
  tempVersionNum.value = inputValue
  if (inputValue && inputValue !== '') {
    data.formData.packageVersionNum = inputValue
    contains.value = packageVersionNum.value.some(item => item === data.formData.packageVersionNum)
    if (contains.value === false) {
      const params = {
        os: data.formData.os,
        cpuArch: data.formData.cpuArch,
        packageVersion: data.formData.packageVersion,
        packageVersionNum: data.formData.packageVersionNum
      }
      checkVersionNumber(params).then((res) => {
        if (res.code !== 200) {
          Message.error({
            content: t('components.Package.5mtcyb0rty44', { versionNum: data.formData.packageVersionNum })
          })
          data.formData.packageVersionNum = ''
          tempVersionNum.value = null
        } else {
          getPackageUrl()
        }
      }).catch(error => {
        message.error({
          content: t('components.Package.5mtcyb0rty44', { versionNum: data.formData.packageVersionNum })
        })
        data.formData.packageVersionNum = '5.0.0'
        tempVersionNum.value = '5.0.0'
      })
    } else {
      getPackageUrl()
    }
  }
}

const searchVersionNum = (value: any) => {
  data.formData.packageVersionNum = (value != null && value !== '') ? value : undefined
}

const toGaussDownloadPage = () => {
  // Jump to the download page
  window.open(t('openGaussDownloadPage'))
}

const formRules = computed(() => {
  return {
    packageVersionNum: [
      { required: true, message: t('components.Package.5mtcyb0rty45') }
    ],
    name: [
      { required: true, message: t('components.Package.5mtcyb0rty46') },
      {
        validator: (rule: any, value: any, cb: any) => {
          hasPkgName(value).then((res: KeyValue) => {
            if (initNameValidateSkipFlag.value) {
              initNameValidateSkipFlag.value = false
              cb()
              return
            }
            if (res.data && !editDisabledFlag.value) {
              return cb(new Error(t('components.Package.5mtcyb0rty47')))
            } else {
              cb()
            }
          })
        }
      }
    ],
    packagePath: [{
      validator: (rule: any, value: any, cb: any) => {
        if (!data.file.raw && uploadStatusTag.value === true) {
          return cb(new Error(t('components.Package.5mtcyb0rty48')))
        } else {
          cb()
        }
      }
    }]
  }
})

const uploadStatusTag = ref(false)
const uploadStatusChange = () => {
  uploadStatusTag.value = true
}

const handleBeforeRemove = (file: FileItem) => {
  return new Promise((resolve, reject) => {
    if (file.status === 'done') {
      Modal.confirm({
        title: t('packageManage.AddPackageDlg.5myq6nnecc45'),
        content: `${t('packageManage.AddPackageDlg.5myq6nnecc45')}${file.name
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

// web socket
const processVisible = ref(false)
const currPercent = ref<number>(0)

watch(currPercent, (newValue) => {
  if (newValue === 100) {
    processVisible.value = false
  }
})

const getUploadPath = () => {
  data.uploadPathLoading = true
  getSysUploadPath()
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        data.systemUploadPath = res.data
      }
    })
}

const packageVersionNum = ref([])
const fetchVersionNum = () => {
  getVersionNum().then((res: KeyValue) => {
    packageVersionNum.value = []
    if (Number(res.code) === 200) {
      res.data.forEach(item => { packageVersionNum.value.push(item) })
    } else {
      Message.error({
        content: t('components.Package.5mtcyb0rty49')
      })
    }
  }).catch(error => {
    console.error(error)
  })
}

const tempPackageUrl = ref('')
const getPackageUrl = () => {
  const params = {
    os: data.formData.os,
    osVersion: data.formData.osVersion,
    cpuArch: data.formData.cpuArch,
    openGaussVersion: data.formData.packageVersion,
    openGaussVersionNum: data.formData.packageVersionNum
  }
  checkVersionNumber(params).then((res) => {
    if (res.code === 200) {
      if (res.data.length) {
        data.formData.packageUrl = res.data[0].packageUrl
      } else {
        data.formData.packageUrl = res.data.packageUrl
      }
      tempPackageUrl.value = data.formData.packageUrl
    } else {
      console.log('error')
    }
    let name = data.formData.packageUrl.split('/')
    data.formData.name = name.pop()
  }).catch(error => {
    console.error({
      content: error + data.formData.packageVersionNum
    })
  })
}

const netStatus = ref(true)
const getConnectStatus = () => {
  checkNetStatus().then((res) => {
    netStatus.value = res.code === 200
  }).catch(error => {
    console.error('net connect error:', error)
  })
}

const wsBusinessId = ref('')
const editDisabledFlag = ref(false)
const open = (
  type: string,
  packageData?: KeyValue,
  addOptionFlag?: number,
  wsBusiness?: string
) => {
  getUploadPath()
  fetchVersionNum()
  data.type = type
  if (type === 'create') {
    data.title = t('components.Package.5mtcyb0rty50')
    uploadStatusTag.value = false
    editDisabledFlag.value = false
  } else {
    data.title = t('components.Package.5mtcyb0rty51')
    editDisabledFlag.value = true
  }
  wsBusinessId.value = wsBusiness
  let versionnum = '5.0.2'
  getConnectStatus()
  if (!packageData) {
    Object.assign(data.formData, {
      packageId: '',
      os: OS.CENTOS,
      osVersion: '7',
      cpuArch: CpuArch.X86_64,
      packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
      packageVersionNum: versionnum,
      packageUrl: '',
      type: 'openGauss'
    })
    data.file = {}
    data.formData.packagePath = {}
    tempVersionNum.value = data.formData.packageVersionNum
    getPackageUrl()
  } else {
    Object.assign(data.formData, {
      packageId: packageData.packageId || '',
      name: packageData.name || '',
      os: packageData.os || OS.CENTOS,
      osVersion: packageData.osVersion || '7',
      cpuArch: packageData.cpuArch || CpuArch.X86_64,
      packageVersion: packageData.packageVersion || OpenGaussVersionEnum.MINIMAL_LIST,
      packageVersionNum: packageData.packageVersionNum || versionnum,
      packageUrl: packageData.packageUrl || '',
      packagePath: packageData.packagePath as UploadInfo,
      type: packageData.type || 'openGauss',
      remark: packageData.remark
    })
    if (packageData.cpuArch == CpuArch.AARCH64 && packageData.os == '') {
      data.formData.os = OS.OPEN_EULER
      data.formData.osVersion = '20.03'
    }
    if (packageData.cpuArch == CpuArch.AARCH64 && packageData.os == OS.CENTOS) {
      data.formData.cpuArch = CpuArch.X86_64
    }
    if (data.formData.packagePath?.realPath) {
      data.file = {
        uid: '-1',
        name: data.formData.packagePath.name,
        url: data.formData.packagePath.realPath
      }
      uploadStatusTag.value = false
    }
    if (data.formData.name == '') {
      getPackageUrl()
    }
  }
  if (addOptionFlag === 0) {
    getPackageUrl()
    uploadStatusTag.value = false
  } else if (addOptionFlag === 1) {
    data.formData.name = packageData.name ? packageData.name : ''
    uploadStatusTag.value = true
  }
  tempOs.value = data.formData.os
  if (data.formData.osVersion == '22.03' && data.formData.os == OS.OPEN_EULER) {
    tempOs.value = OS.All
  }
  tempArch.value = data.formData.cpuArch
  tempVersion.value = data.formData.packageVersion
  tempVersionNum.value = data.formData.packageVersionNum
  tempPackageUrl.value = data.formData.packageUrl
  data.show = true
  progressPercent.value = 0
}

defineExpose({
  open
})
const tempOs = reactive({ value: OS.CENTOS })
const updateOsData = (value: string) => {
  tempOs.value = value.target.value
  if (tempOs.value === OS.CENTOS) {
    if (data.formData.cpuArch === CpuArch.AARCH64) {
      tempArch.value = CpuArch.X86_64
      data.formData.cpuArch = CpuArch.X86_64
    }
    data.formData.os = OS.CENTOS
    data.formData.osVersion = '7'
  } else if (tempOs.value === OS.All) {
    data.formData.os = OS.OPEN_EULER
    data.formData.osVersion = '22.03'
  } else if (tempOs.value === OS.OPEN_EULER) {
    data.formData.os = OS.OPEN_EULER
    data.formData.osVersion = '20.03'
  }
  getPackageUrl()
}
const tempArch = reactive({ value: CpuArch.X86_64 })
const updateArchData = (value: string) => {
  if (data.formData.os === OS.CENTOS && value.target.value === CpuArch.AARCH64) {
    tempArch.value = CpuArch.X86_64
    data.formData.cpuArch = CpuArch.X86_64
  } else {
    tempArch.value = value.target.value
    data.formData.cpuArch = tempArch.value
  }
  getPackageUrl()
}
const tempVersion = reactive({ 'value': OpenGaussVersionEnum.MINIMAL_LIST })
const updateVersionData = (value: string) => {
  tempVersion.value = value.target.value
  data.formData.packageVersion = tempVersion.value
  getPackageUrl()
}

const changeFile = (file) => {
  data.file = file
}
const formRef = ref<null | FormInstance>(null)
const emits = defineEmits([`finish`, 'downloadStart'])
const progressPercent = ref(0)
const submit = async () => {
  let isisvalid = !await formRef.value?.validate()
  if (isisvalid && editDisabledFlag.value !== true) {
    console.log('isValild false')
  } else {
    if (uploadStatusTag.value) {
      if (data.formData.packageId && data.formData.packageId != '') {
        if (progressPercent.value === 0) {
          const formData = new FormData
          formData.append('packageId', data.formData.packageId)
          formData.append('uploadFile', data.file.raw)
          axios({
            url: `/plugins/base-ops/installPackageManager/v2/update/upload`,
            method: 'POST',
            headers: {
              'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (event) => {
              let percent
              if (event.lengthComputable) {
                percent = Math.round((event.loaded * 100) / event.total)
              }
              progressPercent.value = percent ? Number(percent.toFixed(2)) : 0
            },
            data: formData
          }).then((res) => {
            if (res.code === 200) {
              close()
            }
          }).catch((error) => {
            console.log(error)
          })
        }
      } else {
        const formData = new FormData
        formData.append('name', data.formData.name)
        formData.append('os', data.formData.os)
        formData.append('cpuArch', data.formData.cpuArch)
        formData.append('packageVersionNum', data.formData.packageVersionNum)
        formData.append('packageUrl', data.formData.packageUrl)
        formData.append('packageVersion', data.formData.packageVersion)
        formData.append('uploadFile', data.file.raw)
        formData.append('osVersion', data.formData.osVersion)
        if (progressPercent.value === 0) {
          axios({
            url: `/plugins/base-ops/installPackageManager/v2/save/upload`,
            method: 'POST',
            headers: {
              'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (event) => {
              if (event.lengthComputable) {
                progressPercent.value = Math.round((event.loaded * 100) / event.total)
              }
            },
            data: formData
          }).then((res) => {
            if (res.code === 200) {
              close()
            }
          }).catch((error) => {
            console.log(error)
          })
        }
      }
    } else {
      if (!editDisabledFlag.value) {
        const params = {
          name: data.formData.name,
          os: data.formData.os,
          osVersion: data.formData.osVersion,
          cpuArch: data.formData.cpuArch,
          openGaussVersion: data.formData.packageVersion,
          openGaussVersionNum: data.formData.packageVersionNum,
          downloadUrl: data.formData.packageUrl,
          wsBusinessId: wsBusinessId.value
        }
        batchPackageOnline(params).then((res) => {
          if (res.code === 200) {
            data.show = false
            initNameValidateSkipFlag.value = true
            emits('downloadStart', data.formData.name)
          }
        }).catch((error) => {
          console.log(error)
        })
      } else if (data.formData.packageId) {
        const params = new URLSearchParams()
        params.append('packageId', data.formData.packageId)
        params.append('wsBusinessId', wsBusinessId.value)
        const config = {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
        packageOnlineUpdate(params, config)
          .then(res => {
            if (res.code === 200) {
              data.show = false
              initNameValidateSkipFlag.value = true
            }
          })
          .catch(error => {
            console.error('online update error:', error)
          })
      } else {
        console.log('download error')
      }
    }
  }
}

// Submit button to add stabilization
const debounceSubmit = debounce(submit)

const close = () => {
  data.show = false
  data.oldPwd = ''
  Object.assign(data.formData, {
    name: '',
    type: 'openGauss',
    packageId: '',
    os: '',
    cpuArch: '',
    packageVersion: '',
    packageVersionNum: '',
    packageUrl: '',
    packagePath: {},
    remark: ''
  })
  tempOs.value = OS.CENTOS
  tempArch.value = CpuArch.X86_64
  tempVersion.value = OpenGaussVersionEnum.MINIMAL_LIST
  tempVersionNum.value = ''
  uploadStatusTag.value = false
  initNameValidateSkipFlag.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
    data.file = {}
    data.formData.packagePath = {}
  })
}

</script>

<style lang="less" scoped>
.justify-center {
  justify-content: center;
}
.w440 {
  width: 440px;
}
.nameInput {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

::v-deep(.addPackForm) {
  // Set styles such as spacing for forms
  .el-form-item {
    .el-form-item__label-wrap {
      margin-left: 0 !important;
      width: 108px;
    }
    .el-form-item__content {
      max-width: 708px;
      .ant-upload-hint {
        margin: 12px 0 0;
        min-height: 22px;
        line-height: 22px;
      }
    }
  }
  .el-form-item.is-required {
    .el-form-item__label {
      margin-left: -8px !important;
    }
  }
  // Sets the selection style of the selection button
  .el-radio-button__original-radio:checked+.el-radio-button__inner {
    background-color: var(--o-radio-button-bg-color_checked) !important;
    border-color: var(--o-radio-button-border-color_checked) !important;
  }
  .el-radio-button__original-radio:disabled:checked +.el-radio-button__inner {
    background-color: var(--o-radio-button-bg-color_disabled_checked) !important;
    border-color: var(--o-radio-button-border-color_disabled_checked) !important;
  }
  .textareaInput {
    .el-textarea__inner {
      min-height: 74px !important;
    }
  }
}
</style>
