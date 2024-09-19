<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :ok-loading="data.loading"
    :modal-style="{ width: '650px' }"
    @submit="submit"
    @cancel="close"
    @finish="close"
  >
    <template #footer>
      <div class="flex-between">
        <div>
          <a-button :loading="data.loading" type="primary" @click="submit">确定</a-button>
          <a-button class="mr" @click="close">取消</a-button>
        </div>
      </div>
    </template>
    <a-form
      :model="data.formData"
      :rules="formRules"
      ref="formRef"
      auto-label-width
      labelAlign="left"
    >
      <a-form-item field="sysTem" :label="$t('操作系统')" validate-trigger="blur" @change="updateOsData" name="sysTem">
        <a-radio-group v-model="tempOs.value" button-style="solid" :disabled="editDisabledFlag">
          <a-radio :value="OS.OPEN_EULER">openEuler</a-radio>
          <a-radio :value="OS.CENTOS">centOs</a-radio>
          <!--          <a-radio :value="kyLin">麒麟系统</a-radio>-->
        </a-radio-group>
      </a-form-item>
      <a-form-item field="arch" :label="$t('系统架构')" validate-trigger="blur" @change="updateArchData" name="arch">
        <a-radio-group v-model="tempArch.value" :disabled="editDisabledFlag">
          <a-radio :value="CpuArch.X86_64">x86_64</a-radio>
          <a-radio :disabled="OS.CENTOS === tempOs.value" :value="CpuArch.AARCH64">aarch64</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item field="version" :label="$t('版本类型')" validate-trigger="blur" @change="updateVersionData" name="version">
        <a-radio-group v-model="tempVersion.value" :disabled="editDisabledFlag">
          <a-radio :value="OpenGaussVersionEnum.MINIMAL_LIST">极简版</a-radio>
          <a-radio :value="OpenGaussVersionEnum.LITE">轻量版</a-radio>
          <a-radio :value="OpenGaussVersionEnum.ENTERPRISE">企业版</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item field="packageVersionNum" :label="$t('版本号')"  name="packageVersionNum">
        <div class="item" >
          <a-select
            v-if="!editDisabledFlag"
            v-model:value="tempVersionNum"
            :ref="tempVersionNum.value"
            allow-create
            placeholder="请输入版本号"
            :options="packageVersionNum"
            :default-value="tempVersionNum"
            :inputmode="true"
            @blur="insertVersionNum"
            @search="searchVersionNum"
            @change="checkContains"
          >
          </a-select><br>
          <a-input v-if="editDisabledFlag" disabled :default-value="tempVersionNum.value"></a-input>
          <br><p class="ant-upload-hint" v-if="!contains">
          {{$t(' 当前版本为新输入版本号，可') }}
          <a-button type="text" href="https://opengauss.org/zh/download/archive/">前往官网</a-button>
          {{ $t('查看版本号是否存在，如不存在则会下载失败') }} </p>
        </div>
      </a-form-item>
      <a-form-item field="name" :label="$t('安装包名称')" name="name">
        <a-input
          v-model="data.formData.name"
          :placeholder="$t('安装包名称')"
          @input="data.isNameDirty = true"
          :rules="[{ required: true }]"
          :disabled="editDisabledFlag"
        />
      </a-form-item>
      <a-form-item field="path" :label="$t('在线下载地址')" name="path">
        <div class="item" style="width:100%" >
          <a-textarea v-model="tempPackageUrl" show-count :maxlength="1000" :default-value="data.formData.packageUrl"  readonly></a-textarea>
          <p class="ant-upload-hint" v-if="data.formData.packageUrl && netStatus">
            {{$t(' 根据上述配置信息匹配安装包，显示该安装包的在线下载地址进行下载安装，如未匹配到相应的安装包或不是目标安装包， 您可以进行 ') }}
            <a-button type="text" @click="uploadStatusChange">离线上传</a-button>
          </p>
          <p v-if="!netStatus" class="ant-upload-hint">{{$t(' 根据上述配置信息匹配安装包。由于网络未连接，信息仅供显示无法下载。请上传离线安装包。 ') }}</p>
        </div>
      </a-form-item>
      <a-form-item
        v-if="uploadStatusTag.value === true || !netStatus"
        field="packagePath"
        :label="$t('离线安装包')"
        name="packagePath"
      >
        <a-upload
          v-model:file-list="fileListPPPP"
          :limit="1"

          :auto-upload="false"
          draggable
          @before-remove="handleBeforeRemove"
          @change="handleFileUpload"
        >
          <template>
            <div class="upload-info flex-col">
              <div class="flex-col">
                <div >
                  <svg-icon icon-class="clouduploadsharp" class="icon-size image mb"></svg-icon>
                </div>
                <div class="tips-1">
                  <span>{{
                      $t('可将文件拖至此处 或 选择文件')
                    }}
                    <a-button  #upload-button >选择文件</a-button>
                  </span>
                </div>
              </div>
            </div>
          </template>
        </a-upload>
      </a-form-item>
      <a-form-item
        v-if="(uploadStatusTag || !netStatus) && progressPercent.valueOf() > 0"
      >
        <a-progress :percent="progressPercent">{{progressPercent.valueOf()}}</a-progress>
      </a-form-item>
    </a-form>
  </a-modal>

</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, toRaw, computed, isReadonly, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { CpuArch, OS } from '../../../../../../plugins/base-ops/web-ui/src/types/os'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import {
  batchPackageOnline,
  batchPackageUpload,
  getVersionNum,
  checkNetStatus,
  checkVersionNumber,
  packageUploadUpdate,
  delPkgTar, getSysUploadPath, hasPkgName, download
} from '@/api/ops'
import dayjs from 'dayjs'
import { FileItem, Message, Modal } from '@arco-design/web-vue'
import message from '@arco-design/web-vue/es/message'
import Socket from '@/utils/websocket'
import axios from "axios";
import {Path} from "@antv/x6";
import isValid = Path.isValid;
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
  fileList: [],
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

const tempVersionNum = reactive({ value: '' })

const contains = ref(true)
const checkContains = (inputValue:any) => {
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
            content: data.formData.packageVersionNum + '不存在，请重新选择'
          })
          data.formData.packageVersionNum = ''
          tempVersionNum.value = null
        } else {
          getPackageUrl()
        }
      }) .catch(error => {
        message.error({
          content: '版本号' + data.formData.packageVersionNum + '不存在，请重新选择'
        })
        data.formData.packageVersionNum = '5.0.0'
        tempVersionNum.value = '5.0.0'
      })
    } else {
      getPackageUrl()
    }
  }
}
const insertVersionNum = (value:any) => {
  tempVersionNum.value = value.target.value
}
const searchVersionNum = (value:any) => {
  data.formData.packageVersionNum = (value != null && value !== '') ? value : undefined
}

const formRules = reactive({
  packageVersionNum: [
    { required: true, message: t('请选择版本号')}
  ],
  name: [
    { required: true, message: t('请输入安装包名称') },
    {
      validator: (value: any, cb: any) => {
        return new Promise((resolve) => {
          hasPkgName(value).then((res: KeyValue) => {
            if (res.data) {
              cb(t('请勿输入重复包名'))
              isValid.value = false
              resolve(false)
            } else {
              resolve(true)
            }
          })
        })
      }
    }
  ],
  packagePath: [{
    validator: (value: any, cb: any) => {
      return new Promise((resolve, reject) => {
        if (data.fileList.length <= 0 && uploadStatusTag.value === true) {
          cb(t('请选择一个安装包进行上传'))
          isValid.value = false
          resolve(false)
          return
        } else {
          resolve(true)
        }
      })
    }
  }]
})

const fileListPPPP = ref([])
const uploadStatusTag = reactive({ value: false })
const uploadStatusChange = () => {
  uploadStatusTag.value = true
}
const uploadFileTag = reactive({ value: true })
const handleFileUpload = (file:any) => {
  data.fileList = file[0]
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

// web socket
const downloadWs = ref<Socket<any, any> | undefined>()
const processVisible = ref(false)
const percentLoading = ref(false)
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

// init
const getSystemSetting = () => {
  getSysUploadPath().then((res) => {
    data.systemUploadPath = res.data
  }) .catch(error => {
    console.error('Error fetching 285:', error)
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
        content: '获取版本号失败'
      })
    }
  }) .catch(error => {
    console.error(error)
  })
}

const tempPackageUrl = ref('')
const getPackageUrl = () => {
  const params = {
    os: data.formData.os,
    cpuArch: data.formData.cpuArch,
    packageVersion: data.formData.packageVersion,
    packageVersionNum: data.formData.packageVersionNum
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
  }) .catch(error => {
    console.error({
      content: error + data.formData.packageVersionNum
    })
  })
}

const netStatus = ref(false)
const getConnectStatus = () => {
  checkNetStatus().then((res) => {
    netStatus.value = res.code === 200
  }) .catch(error => {
    console.error('net connect error:', error)
  })
}

const wsBusinessId = ref('')
const editDisabledFlag = ref(false)
const open = (
  type: string,
  packageData?: KeyValue,
  addOptionFlag?: number,
  wsBusiness?: string,
) => {
  getUploadPath()
  fetchVersionNum()
  getSystemSetting()
  data.type = type
  if (type === 'create') {
    data.title = t('添加安装包')
    uploadStatusTag.value = false
    editDisabledFlag.value = false
  } else {
    data.title = t('更新安装包')
    editDisabledFlag.value = true
  }
  wsBusinessId.value = wsBusiness
  let versionnum = '5.0.2'
  getConnectStatus()
  if (!packageData) {
    Object.assign(data.formData, {
      packageId: '',
      os: OS.CENTOS,
      cpuArch: CpuArch.X86_64,
      packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
      packageVersionNum: versionnum,
      packageUrl: '',
      type: 'openGauss'
    })
    data.fileList = []
    data.formData.packagePath = {}
    fileListPPPP.value = []
    tempVersionNum.value = data.formData.packageVersionNum
    getPackageUrl()
  } else {
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
      data.fileList = [{
        uid: '-1',
        name: data.formData.packagePath.name,
        url: data.formData.packagePath.realPath
      }]
      uploadStatusTag.value = false
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
const updateOsData = (value:string) => {
  tempOs.value = value.target.value
  if (tempOs.value === OS.CENTOS && data.formData.cpuArch ===  CpuArch.AARCH64) {
    tempArch.value = CpuArch.X86_64
    data.formData.cpuArch = CpuArch.X86_64
  }
  data.formData.os = OS.OPEN_EULER
  getPackageUrl()
}
const tempArch = reactive({ value: CpuArch.X86_64 })
const updateArchData = (value:string) => {
  if (data.formData.os === OS.CENTOS && value.target.value ===  CpuArch.AARCH64) {
    tempArch.value = CpuArch.X86_64
    data.formData.cpuArch = CpuArch.X86_64
  } else {
    tempArch.value = value.target.value
    data.formData.cpuArch = tempArch.value
  }
  getPackageUrl()
}
const tempVersion = reactive({ 'value': OpenGaussVersionEnum.MINIMAL_LIST })
const updateVersionData = (value:string) => {
  tempVersion.value = value.target.value
  data.formData.packageVersion = tempVersion.value
  getPackageUrl()
}

const formRef = ref<null | FormInstance>(null)
const emits = defineEmits([`finish`])
const submitLoading = ref(false)
const progressPercent = ref(0)
const submit = async () => {
  let isisvalid = await formRef.value?.validate()
  if (isisvalid && editDisabledFlag.value !== true) {
    console.log('isValild false')
  } else {
    if (uploadStatusTag.value) {
      if (data.formData.packageId && data.formData.packageId != '') {
        if (progressPercent.value === 0) {
          const formData = new FormData
          formData.append('packageId', data.formData.packageId)
          formData.append('uploadFile', data.fileList.file)
          axios({
            url: `/plugins/base-ops/installPackageManager/v2/update/upload/`,
            method: 'POST',
            headers: {
              'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (event) => {
              let percent
              if (event.lengthComputable) {
                percent = Math.round((event.loaded * 100) / event.total);
              }
              progressPercent.value = percent?Number((percent * 0.01).toFixed(2)) :0
            },
            data: formData
          }).then((res) => {
            if(res.code === 200) {
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
        formData.append('packageUrl', '')
        formData.append('packageVersion', data.formData.packageVersion)
        formData.append('uploadFile', data.fileList.file)
        if (progressPercent.value === 0) {
        axios({
          url: `/plugins/base-ops/installPackageManager/v2/save/upload/`,
          method: 'POST',
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          onUploadProgress: (event) => {
            let percent
            if (event.lengthComputable) {
              percent = Math.round((event.loaded * 100) / event.total);
            }
            progressPercent.value = percent?Number((percent * 0.01).toFixed(2)):0
          },
          data: formData
        }).then((res) => {
          if(res.code === 200) {
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
          cpuArch: data.formData.cpuArch,
          openGaussVersion: data.formData.packageVersion,
          openGaussVersionNum: data.formData.packageVersionNum,
          downloadUrl: data.formData.packageUrl,
          wsBusinessId: wsBusinessId.value
        }
        batchPackageOnline(params).then((res) => {
          if (res.code === 200) {
            data.show = false
          }
        }).catch((error) => {
          console.log(error)
        })
      }
    }
  }
}
const close = () => {
  try {
    if (downloadWs.value && downloadWs.value instanceof WebSocket && typeof downloadWs.value.close === 'function') {
      downloadWs.value.close(1000, 'Normal closure')
    }
  } catch (error) {
    console.error('Error closing WebSocket:', error)
  } finally {
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
    nextTick(() => {
      formRef.value?.clearValidate()
      formRef.value?.resetFields()
      data.fileList = []
      data.formData.packagePath = {}
      fileListPPPP.value = []
    })
  }

}
</script>

<style scoped>
:deep(.ant-upload-hint) {
  color:lightgrey;
}

:deep(.arco-radio-checked.arco-radio-disabled .arco-radio-label) {
  color: var(--color-hw-text-2);
}

:deep(.arco-radio-disabled .arco-radio-label) {
  color: var(--color-text-3);
}

:deep(.arco-input-wrapper .arco-input[disabled]){
  -webkit-text-fill-color: var(--color-text-3);
}
:deep(.arco-progress-type-circle) {
  display: table-column;
}

:deep(.arco-upload-list-item-name-link .arco-upload-list-item-file-icon .arco-upload-list-item-file-icon) {
  color: black;
}
</style>
