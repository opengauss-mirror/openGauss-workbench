<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :ok-loading="data.loading"
    :modal-style="{ width: '650px' }"
    @submit="submit"
    @close="close"
    @cancel="close"
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
          <a-radio :value="OS.OPEN_EULER">openEuler20.03</a-radio>
          <a-radio :value="OS.CENTOS">centOs</a-radio>
          <a-radio :value="OS.All">openEuler22.03</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item field="arch" :label="$t('系统架构')" validate-trigger="blur" @change="updateArchData" name="arch">
        <a-radio-group v-model="tempArch.value" :disabled="editDisabledFlag">
          <a-radio :value="CpuArch.X86_64">x86_64</a-radio>
          <a-radio :value="CpuArch.AARCH64">aarch64</a-radio>
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
          <a-input
            v-model="data.formData.packageVersionNum"
            :rules="[{ required: true }]"
            :disabled="editDisabledFlag"
          />
        </div>
      </a-form-item>
      <a-form-item field="name" :label="$t('安装包名称')" name="name">
        <a-input
          v-model="data.formData.name"
          :placeholder="$t('安装包名称')"
          :max-length="255"
          @input="data.isNameDirty = true"
          :rules="[{ required: true }]"
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
        v-if="uploadStatusTag || !netStatus"
        field="packagePath"
        :label="$t('离线安装包')"
        name="packagePath"
      >
        <a-upload
          v-model:file-list="fileListPPPP"
          :limit="1"
          :show-file-list="true"
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
      <!--      <a-progress size="large" :percent="currPercent" />-->
    </a-modal>
  </div>

</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import {nextTick, reactive, ref, toRaw, computed, isReadonly, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {CpuArch, OS} from '@/types/os'
import {OpenGaussVersionEnum} from "@/types/ops/install"
import {
  batchPackageOnline,
  batchPackageUpload,
  getVersionNum,
  checkNetStatus,
  checkVersionNumber,
  packageUploadUpdate,
  delPkgTar, getSysUploadPath,hasPkgName,download
} from "@/api/ops"
import dayjs from "dayjs"
import {FileItem, Message, Modal} from "@arco-design/web-vue"
import message from "@arco-design/web-vue/es/message"
import Socket from "@/utils/websocket"
import axios from "axios";
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
    osVersion: '',
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

const tempVersionNum = reactive({value:''})

const formRules = reactive<FormRules>({
  packageVersionNum: [
    { required: true, message: t('请选择版本号'), trigger: 'blur' },
  ],
  name: [
    { required: true, message: t('请输入安装包名称'), trigger: 'blur' },
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
  packagePath: [{
    validator: (value: any, cb: any) => {
      return new Promise((resolve, reject) => {
        if (data.fileList.length <= 0) {
          cb(t('请选择一个安装包进行上传'))
          resolve(false)
          return
        } else {
          cb()
          resolve(true)
        }
      })
    }
  }],
})


const fileListPPPP = ref([])
const uploadStatusTag = ref(false)
const uploadStatusChange = () => {
  uploadStatusTag.value = true
}
const uploadFileTag = reactive({ value: true })
const handleFileUpload = (file:file) => {
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


const wsBusinessId = ref('')

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
const packageVersionNum = ref([])
const tempPackageUrl = ref('')
const getPackageUrl = () => {
  const params = {
    os : data.formData.os,
    osVersion: data.formData.osVersion,
    cpuArch: data.formData.cpuArch,
    openGaussVersion : data.formData.packageVersion,
    openGaussVersionNum : data.formData.packageVersionNum
  }
  checkVersionNumber(params).then((res) => {
    if (res.code === 200){
      if (res.data.length) {
        data.formData.packageUrl = res.data[0].packageUrl
      } else {
        data.formData.packageUrl = res.data.packageUrl
      }
      tempPackageUrl.value = data.formData.packageUrl
    } else {
      console.log('error')
    }
    let noExtensionName = ''
    let packageNameList = data.formData.packageUrl.split('/').pop();
    let namelist = packageNameList?.split('.') || [];
    namelist.length && namelist.pop()
    namelist.length && namelist.pop()
    const now = dayjs();
    const curTime = now.format('YYMMDD')
    noExtensionName = namelist.join('.') + curTime;
    data.formData.name = noExtensionName;
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

const editDisabledFlag = ref(true)
const open = (
  type: string,
  packageData: KeyValue,
  addOptionFlag: string
) => {
  getUploadPath()
  data.type = type
  data.title = t('添加安装包')
  uploadStatusTag.value = false
  getConnectStatus()
  data.formData.packageId = ''
  data.formData.name = ''
  data.formData.os = packageData.os
  data.formData.cpuArch = packageData.cpuArch
  data.formData.packageVersion = packageData.packageVersion
  data.formData.packageVersionNum = packageData.packageVersionNum
  data.formData.osVersion = packageData.osVersion
  uploadStatusTag.value = addOptionFlag === '1'
  wsBusinessId.value = addOptionFlag !== '1'?addOptionFlag:''
  getPackageUrl()
  fileListPPPP.value = []
  if (data.formData.osVersion === '20.03' && data.formData.os === OS.OPEN_EULER) {
    tempOs.value = OS.OPEN_EULER
  } else if (data.formData.osVersion === '22.03' && data.formData.os === OS.OPEN_EULER) {
    tempOs.value = OS.All
  } else {
    tempOs.value = OS.CENTOS
  }
  tempArch.value = data.formData.cpuArch
  tempVersion.value = data.formData.packageVersion
  tempVersionNum.value = data.formData.packageVersionNum
  data.show = true
  progressPercent.value = 0
}

defineExpose({
  open
})
const tempOs = reactive({value: OS.CENTOS})
const updateOsData = (value:string) => {
  tempOs.value = value.target.value
  if (tempOs.value === "kyin") {
    data.formData.os = OS.OPEN_EULER
  } else {
    data.formData.os = tempOs.value
  }
  getPackageUrl()
}
const tempArch = reactive({value: CpuArch.X86_64})
const updateArchData = (value:string) => {
  tempArch.value = value.target.value
  data.formData.cpuArch = tempArch.value
  getPackageUrl()
}
const tempVersion = reactive({'value': OpenGaussVersionEnum.MINIMAL_LIST})
const updateVersionData = (value:string) => {
  tempVersion.value = value.target.value
  data.formData.packageVersion = tempVersion.value
  getPackageUrl()
}

const formRef = ref<null | FormInstance>(null)
const emits = defineEmits([`finish`, `downloadStart`])
const submitLoading = ref(false)
const progressPercent = ref(0)
const submit = async () => {
  let isvalid = await formRef.value?.validate()
  if (!isvalid && progressPercent.value === 0) {
    if (uploadStatusTag.value ) {
      const formData = new FormData
      formData.append('name', data.formData.name)
      formData.append('os', data.formData.os)
      formData.append('osVersion', data.formData.osVersion)
      formData.append('cpuArch', data.formData.cpuArch)
      formData.append('packageVersionNum', data.formData.packageVersionNum)
      formData.append('packageUrl', '')
      formData.append('packageVersion', data.formData.packageVersion)
      formData.append('uploadFile', data.fileList.file)
      axios({
        url: `/installPackageManager/v2/save/upload`,
        method: 'POST',
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        data: formData,
        onUploadProgress: (event) => {
          let percent
          if (event.lengthComputable) {
            percent = Math.round((event.loaded * 100) / event.total);
          }
          progressPercent.value = percent?percent * 0.01 :0
        },
      }).then((res) => {
        if (res.code === 200) {
          Message.success(res.msg || '上传成功')
          close()
        } else {
          Message.error(res.msg || '上传失败')
        }
      }).catch((error) => {
        console.log(error)
      })
    } else {
      const params = {
        name: data.formData.name,
        os: data.formData.os,
        osVersion:data.formData.osVersion,
        cpuArch: data.formData.cpuArch,
        openGaussVersion: data.formData.packageVersion,
        openGaussVersionNum: data.formData.packageVersionNum,
        downloadUrl: data.formData.packageUrl,
        wsBusinessId: wsBusinessId.value
      }
      emits('downloadStart', data.formData.name)
      batchPackageOnline(params).then((res) => {
        console.log(' add success online', res)
        close()
      }).catch((error) => {
        console.log(error)
      })
    }
  }

}
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
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
    data.fileList = []
    data.formData.packagePath = {}
  })
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

:deep(.arco-input-wrapper .arco-input[disabled]) {
  -webkit-text-fill-color: var(--color-text-3);
}

:deep(.arco-progress-type-circle) {
  display: table-column;
}

:deep(.arco-upload-list-item-name-link .arco-upload-list-item-file-icon .arco-upload-list-item-file-icon) {
  color: black;
}

</style>
