<template>
  <el-dialog v-model="data.show" :title="data.title" close-on-press-escape="false" draggable="true" class="upload-dialog"
             @open="open" @close="handleClose" :z-index="1000" :style="{ minHeight: '200px' }"
  >
    <div v-if="data.status === fileStatusEnum.unLoad">
      <el-upload drag accept=".xlsx,.xls" :http-request="uploadchange" class="upload-win"
      >
        <el-icon class="el-icon--upload">
          <UploadFilled/>
        </el-icon>
        <br>
        <div class="el-upload__text">
          {{ $t('bulk.BulkImport.5exv06n8x1kj0') }}
          <em>{{ $t('bulk.BulkImport.5exv06n8x1ki0') }}</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            <el-text class="fontsize"> {{ $t('bulk.BulkImport.5exv06n8x1ky0') }}</el-text>
            <el-link type="primary" @click="downLoadModule" style="float:right; text-align:right ">
              <el-icon color="#409EFF">
                <Download/>
              </el-icon>
              <el-text type="primary">{{ $t('bulk.BulkImport.5exv06n8x1ku0') }}</el-text>
            </el-link>
          </div>
        </template>
      </el-upload>
    </div>
    <div v-if="data.status === fileStatusEnum.formLoading">
      <div v-if="data.formLoaFlag === formLoaFlaEnum.waiting">
        <el-text class="fontsize">{{ $t('bulk.BulkImport.5exv06n8x1kv0') }}</el-text>
        <div style="margin-top: 10px; margin-bottom: 10px">
          <el-text class="fontsize">{{ data.files[0]?.name }}</el-text>
        </div>
        <el-progress :percentage="progress" stroke-width="8"/>
      </div>
    </div>
    <div v-if="data.formLoaFlag === formLoaFlaEnum.formWin">
      <el-button type="primary" @click="reupload" style="margin-bottom: 12px;">
        {{ $t('bulk.BulkImport.5exv06n8x1ko0') }}
      </el-button>
      <br>
      <el-text class="fontsize" type="default">{{ $t('bulk.BulkImport.5exv06n8x1kp0') }}</el-text>
      <el-upload drag accept=".xlsx,.xls" :http-request="uploadchange" class="upload-winauto">
        <div class=" upload_box " style="{ height: 5px; padding-bottom: 0px; }">
          <div class="content_box" ref="dropArea" @dragover.prevent="handleDragOver" @dragenter.prevent="handleDragLeave"
               @drop.prevent="handleDrop" :style="{ backgroundColor: maskColor }">
            <div style="flex: 1; display: flex; align-items: center; gap: 26px;">
              <div>
                <ZipIcon :label-text="fileTypeUpper" :type="fileIconColor" size="32" col/>
              </div>
              <div style="text-align: left">
                <el-text class="fontsize">{{ data.files[0]?.name }}</el-text>
                <br>
                <el-text class="fontsize" type="default">{{ (data.files[0]?.size / 1024).toFixed(2) }}KB</el-text>
              </div>
            </div>
            <el-icon @click="reupload" :size="16">
              <Delete/>
            </el-icon>
            <input ref="uploadFileRef" style="display: none" type="file" name="file" @change="uploadchange"/>
          </div>
        </div>
      </el-upload>
      <div v-if="data.formstatus === 'pass'">
        <el-text class="fontsize" type="success">{{ $t('bulk.BulkImport.5exv06n8x1kq0') }}</el-text>
        <div style="text-align: center;">
          <el-button type="primary" class='mr' @click="anaFilClick">{{
              $t('bulk.BulkImport.5exv06n8x1ka0')
            }}
          </el-button>
          <el-button class='mr' @click="handleClose">{{ $t('bulk.BulkImport.5exv06n8x1kb0') }}</el-button>
        </div>
      </div>
      <div v-else-if="data.formstatus === 'tooBig'">
        <el-text class="fontsize" type="danger">{{ $t('bulk.BulkImport.5exv06n8x1kc0') }}</el-text>
      </div>
      <div v-else-if="data.formstatus === 'mul'">
        <el-text class="fontsize" type="danger">{{ $t('bulk.BulkImport.5exv06n8x1kd0') }}</el-text>
      </div>
      <div v-else-if="data.formstatus === 'formErr'">
        <el-text class="fontsize" type="danger">{{ $t('bulk.BulkImport.5exv06n8x1kw0') }}</el-text>
      </div>
      <div v-else>
        <p>{{ $t('bulk.BulkImport.5exv06n8x1ke0') }} {{ data.formstatus }}</p>
      </div>
    </div>

    <div v-if="data.status === fileStatusEnum.fileParsing" >
      <div step-back>
        <el-steps :space="200" :active="activeStep" finish-status="success"  :process-status="processStatus"
                  class="custom-steps step-back">
          <el-step :description="step1Title" :status="step1Status" />
          <el-step :description="t('bulk.BulkImport.step2Content')" :status="step2Status" />
          <el-step :description="t('bulk.BulkImport.step3Content')" :status="step3Status" />
        </el-steps>
      </div>
      <div >
        <div v-if="data.formLoaFlag === formLoaFlaEnum.anyWin" style="text-align: center">
          <br>
          <svg-icon icon-class="file-search-suc" style="font-size: 100px"></svg-icon>
          <br>
          <el-text type="default">{{ $t('bulk.BulkImport.5exv06n8x1kn0') }}</el-text>
          <br>
          <el-text type="default">{{ $t('bulk.BulkImport.5exv06n8x1kh1') }}</el-text>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadErr" style="text-align: center">
          <br>
          <svg-icon icon-class="file-search-fail" style="font-size: 100px"></svg-icon>
          <br>
          <el-text type="danger" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1kx0') }}</el-text>
          <el-text type="default" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1ke0') }}</el-text>
          <el-button type="primary" link @click="reupload" style="display: inline;">{{
              $t('bulk.BulkImport.5exv06n8x1ko0')
            }}
          </el-button>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadWin" style="text-align: center">
          <br><br>
          <div class='inflexside'>
            <el-progress :percentage="progressWidth.toFixed(0)" stroke-width="8"/>
          </div>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadSuc" style="text-align: center">
          <br>
          <el-text type="default" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1ki1') }} {{
              totalNum
            }}
            {{
              $t('bulk.BulkImport.5exv06n8x1kj1')
            }}
          </el-text>
          <br>
          <el-text type="success" style="display: inline;">{{ sucNum }} {{ $t('bulk.BulkImport.5exv06n8x1kk1') }} </el-text>
          <el-text type="default" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1kl1') }}</el-text>
          <el-text type="danger" style="display: inline;">{{ totalNum - sucNum }} {{ $t('bulk.BulkImport.5exv06n8x1kk1') }}</el-text>
          <el-text type="default" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1km1') }}</el-text>
          <br>
          <br>
          <el-button type="danger" link @click="downLoadErrRep" style="display: inline;">{{
              $t('bulk.BulkImport.5exv06n8x1kn1')
            }}
          </el-button>
          <br>
          <br>
          <el-button @click="open" type="primary">{{
              $t('bulk.BulkImport.5exv06n8x1kr0')
            }}
          </el-button>
          <el-button @click="handleClose">&emsp;{{ $t('bulk.BulkImport.5exv06n8x1ks0') }}&emsp;</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { reactive, ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { bulkImporErrPlan, bulkImportany, bulkuploadPhy } from '@/api/ops'
import useLocale from '@/hooks/locale'
import showMessage from '@/hooks/showMessage'
import { UploadFilled, Delete, Download } from '@element-plus/icons-vue'
import ZipIcon from './ZipIcon.vue'

const { t } = useI18n()
const { currentLocale } = useLocale()

const fileTypeUpper = computed(() => {
  if (data.files[0]) {
    const parts = data.files[0].name.split('.')
    return parts.pop().toUpperCase()
  } else {
    return 'ZIP'
  }
})
const fileIconColor = computed(() => {
  if (data.formstatus === 'pass') {
    return 'success'
  } else {
    return 'default'
  }
})

const activeStep = computed(() => {
  if (data.formLoaFlag >= 6) return 2
  if (data.formLoaFlag >= 5) return 1
  return 0
})

const processStatus = computed(() => {
  return data.formLoaFlag.value === 3 ? 'error' : 'process'
})

const step1Status = computed(() => {
  if (data.formLoaFlag >= 5) return 'success'
  return data.formLoaFlag.value === 3 ? 'error' : 'process'
})

const step2Status = computed(() => {
  if (data.formLoaFlag >= 6) return 'success'
  if (data.formLoaFlag  < 5) return 'wait'
  return 'process'
})

const step3Status = computed(() => {
  return data.formLoaFlag >= 6 ? 'success' : 'wait'
})

const step1Title = computed(() => {
  return data.formLoaFlag === 3 ? t('bulk.BulkImport.step1Error') : t('bulk.BulkImport.step1Content')
})

enum fileStatusEnum {
  unLoad = 0,
  formLoading = 1,
  fileParsing = 2,
}

enum formLoaFlaEnum {
  formWin = -1,
  waiting = 0,
  anyWin = 2,
  loadErr = 3,
  loadWin = 5,
  loadSuc = 6,
}

let intervalIdP: number | null = null
let percentage = ref(0)
let totalNum = ref(0)
let sucNum = ref(0)
const progressWidth = ref(0)

const data = reactive<KeyValue>({
  show: false,
  title: t('bulk.BulkImport.5exv06n8x1kh0'),
  status: fileStatusEnum.unLoad,
  files: [],
  uid: null,
  percent: 0,
  formstatus: 'untest',
  formLoaFlag: formLoaFlaEnum.waiting
})

const emits = defineEmits<{
  (e: 'update:modelValue', value: any): void
  (e: string): void
}>()

const open = () => {
  data.show = true
  emits('update:modelValue', true)
  data.status = fileStatusEnum.unLoad
  data.files = []
  data.uid = 0
  data.title = t('bulk.BulkImport.5exv06n8x1kh0')
  data.formstatus = 'untest'
  data.percent = 0
  data.formLoaFlag = formLoaFlaEnum.waiting
}

const reupload = () => {
  data.status = fileStatusEnum.unLoad
  data.files = []
  data.uid = 0
  data.formstatus = 'untest'
  data.percent = 0
  data.formLoaFlag = formLoaFlaEnum.waiting
  progressWidth.value = 0
}

const handleClose = () => {
  data.show = false
  emits('update:modelValue', false)
  let uploadFlag = 1
  checkUploadFil(uploadFlag)
  data.files = []
  data.uid = 0
  data.percent = 0
  progressWidth.value = 0
  data.formLoaFlag = formLoaFlaEnum.waiting
  clearInterval(intervalIdP)
  window.removeEventListener('beforeunload', handleBeforeUnload)
  emits('finish')
}

const maskColor = computed(() => {
  if (data.formstatus === 'pass') {
    return 'rgba(0, 128, 0, 0.1)'
  }
  const allowTypes = ['formErr', 'mul', 'tooBig', 'form error', 'error']
  if (allowTypes.includes(data.formstatus)) {
    return 'rgba(255, 0, 0, 0.1)'
  }
})

const uploadFileRef = ref()
const uploadchange = (e: any) => {
  reupload()
  const chooseFile = e.file
  data.files = []
  data.files.push(e.file)
  uploadFilPhy(chooseFile)
}

const dropArea = ref(false)
const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  dropArea.value.classList.add('dragover')
}
const handleDragLeave = (e: DragEvent) => {
  e.preventDefault()
  dropArea.value.classList.remove('dragover')
}
const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  reupload()
  dropArea.value.classList.remove('dragover')
  const files = e.dataTransfer?.files
  if (files) {
    for (let i = 0; i < files.length; i++) {
      const file = files[i] as File
      data.files.push(file)
    }
  }
  uploadFilPhy(files)
}

const uploadFilClick = () => {
  uploadFileRef.value.click()
}

const checkFileForm = (rawFile: any) => {
  if (data.files.length > 1) {
    return 'mul'
  } else {
    const allowTypes = ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']
    if (!allowTypes.includes(data.files[0].type)) {
      return 'formErr'
    }
    if (data.files[0].size / 1024 / 1024 > 200) {
      return 'tooBig'
    }
    return 'pass'
  }
}

const progress = ref(0)
const progressbar = () => {
  data.formStatus = 'untest'
  data.status = fileStatusEnum.formLoading
  if (data.formStatus === 'untest') {
    let temp = Math.floor(Math.random() * 20)
    const progressbartimer = setInterval(() => {
      if (progress.value < 100) {
        temp = Math.floor(Math.random() * 20)
        if (progress.value + temp < 100) {
          progress.value = progress.value + temp
        } else {
          progress.value = 100
          data.formLoaFlag = formLoaFlaEnum.formWin
        }
      } else {
        clearInterval(progressbartimer)
        data.formLoaFlag = formLoaFlaEnum.formWin
      }
    }, 70)
  }
}

onMounted(() => {
  progress.value = 0
  progressbar()
})

const uploadFilPhy = (rawFile: any) => {
  const formData = new FormData()
  data.formStatus = 'untest'
  progress.value = 0
  progressbar()
  formData.append('file', data.files[0])
  data.status = fileStatusEnum.formLoading
  const fileformtest = checkFileForm(data.files[0])
  if (fileformtest === 'pass') {
    bulkuploadPhy(formData)
      .then(response => {
        if (response.code == '200') {
          data.uid = response.msg
          data.formstatus = 'pass'
        } else {
          data.formstatus = 'error'
        }
      })
      .catch(error => {
        console.error('Error:', error)
      })
  } else {
    data.formstatus = fileformtest
  }
}

const fetchProgress = async () => {
  const uid = data.uid
  let formLoaFlagTemp = formLoaFlaEnum.loadWin
  try {
    const progressResponse = await axios.get('/host/get_import_plan/', {
      params: {
        uuid: uid
      }
    })
      .catch(() => {
        formLoaFlagTemp = formLoaFlaEnum.loadErr
        data.formLoaFlag = formLoaFlagTemp
        percentage.value = 0
        data.percent = percentage.value
        clearInterval(intervalIdP)
      })
    percentage.value = (progressResponse.data.doneSum / progressResponse.data.totality) * 100
    if (progressResponse.data.isEnd) {
      if (intervalIdP !== null) {
        clearInterval(intervalIdP)
      }
      if (progressResponse.code != '200') {
        formLoaFlagTemp = formLoaFlaEnum.loadErr
        percentage.value = 0
      } else {
        percentage.value = 100
        totalNum.value = progressResponse.data.totality
        sucNum.value = progressResponse.data.successSum
      }
    }
  } catch (error: any) {
    if (error.response && error.response.status === 500) {
      showMessage('error', 'Server error, please try again later.')
    } else {
      console.error('Error fetching progress:', error)
    }
  }
  data.formLoaFlag = formLoaFlagTemp
  data.percent = percentage.value
}

const anaFilClick = () => {
  let uploadFlag = 0
  checkUploadFil(uploadFlag)
  anaFilPhy()
}

const checkUploadFil = (uploadFlag: number) => {
  var formDataUploadFil = new FormData()
  const uid = data.uid
  formDataUploadFil.append('uuid', uid)
  formDataUploadFil.append('isInvoke', uploadFlag)
  formDataUploadFil.append('currentLocale', currentLocale.value)
  bulkImportany(formDataUploadFil).then(response => {
    console.log('waiting for response')
  })
    .catch(error => {
      console.error('Error:', error)
    })
}

const isUploading = ref(false)
const anaFilPhy = async () => {
  data.formLoaFlag = formLoaFlaEnum.anyWin
  isUploading.value = true
  progressWidth.value = 0
  sessionStorage.setItem('isUploading', 'true')
  refreshCount.value = 0
  if (data.uid && data.uid.length <= 1) {
    showMessage('error', 'error uid length')
    data.formLoaFlag = formLoaFlaEnum.loadErr
    data.percent = 0
  }
  data.status = fileStatusEnum.fileParsing
  startProgressBar()
  if (data.percent < 100 && data.formLoaFlag != formLoaFlaEnum.loadErr) {
    intervalIdP = setInterval(fetchProgress, 1000)
  } else {
    if (data.formLoaFlag != formLoaFlaEnum.loadErr) {
      data.formLoaFlag = formLoaFlaEnum.loadWin
    }
    if (data.percent === 100) {
      data.formLoaFlag = formLoaFlaEnum.loadSuc
    }
  }
  if (sessionStorage.getItem('isUploading') === 'true') {
    isUploading.value = true
  }
}

const uploadedBytes = ref(0)

const startProgressBar = () => {
  uploadedBytes.value = 0
  progressWidth.value = 0
  let interval = 1000
  let timeElapsed = 0
  let totalSteps = 0
  let uploadSpeedMbps = 10
  const updateProgress = () => {
    uploadedBytes.value += (uploadSpeedMbps * 1024) / 10
    totalSteps = (uploadedBytes.value / data.files[0].size) * 30
    uploadSpeedMbps = 10.0 / (1 + timeElapsed / 10)
    progressWidth.value = totalSteps
    timeElapsed += 1
    if (progressWidth.value < 90 && data.percent < 100 && progressWidth.value + totalSteps <= 90) {
      setTimeout(updateProgress, interval)
    } else if (data.percent === 100) {
      progressWidth.value = 100
      setTimeout(() => {
        data.formLoaFlag = formLoaFlaEnum.loadSuc
      }, 1000)
    } else {
      setTimeout(updateProgress, interval)
    }
  }
  updateProgress()
}

const downLoadModule = () => {
  let url = `/host/downloadTemplate/${currentLocale.value}`
  axios.get(url, {
    responseType: 'blob', headers: {'Content-Type': 'application/json;application/octet-stream'}
  })
    .then((res) => {
      if (res) {
        const blob = new Blob([res], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
        const link = document.createElement('a')
        const URL = window.URL || window.webkitURL
        let herf = URL.createObjectURL(blob)
        link.href = herf
        link.download = currentLocale.value === 'zh-CN' ? '模板.xlsx' : 'Template.xlsx'
        link.click()
        window.URL.revokeObjectURL(herf)
      }
    }).catch((err) => {
    console.log('error:' + err)
  })
}

const downLoadErrRep = () => {
  const uuid = data.uid
  let url = `/host/downloadErrorExcel/${uuid}`
  axios.get(url, {
    responseType: 'blob', headers: {'Content-Type': 'application/json;application/octet-stream'}
  })
    .then((res) => {
      if (res) {
        const blob = new Blob([res], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
        const link = document.createElement('a')
        const URL = window.URL || window.webkitURL
        let herf = URL.createObjectURL(blob)
        link.href = herf
        const date = new Date()
        const formattedDate = `${date.getFullYear()}${(date.getMonth() + 1).toString().padStart(2, '0')}${date.getDate().toString().padStart(2, '0')}${date.getHours().toString().padStart(2, '0')}${date.getMinutes().toString().padStart(2, '0')}${date.getSeconds().toString().padStart(2, '0')}`
        link.download = currentLocale.value === 'zh-CN' ? `错误报告_${formattedDate}.xlsx` : `Error Report_${formattedDate}.xlsx`
        link.click()
        window.URL.revokeObjectURL(herf)
      }
    }).catch((err) => {
    console.log('error:' + err)
  })
}

const refreshCount = ref(parseInt(sessionStorage.getItem('refreshCount') || '0'))

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (isUploading.value) {
    event.preventDefault()
    event.returnValue = ''
  } else if (refreshCount.value === 1) {
    const confirmationMessage = '数据可能会丢失，确定要刷新页面吗？'
    if (!confirm(confirmationMessage)) {
      event.preventDefault()
      event.returnValue = ''
    } else {
      sessionStorage.setItem('refreshCount', '0')
    }
  } else {
    event.preventDefault()
    event.returnValue = ''
  }
}

window.addEventListener('beforeunload', () => {
  sessionStorage.setItem('refreshCount', (refreshCount.value + 1).toString())
})

defineExpose({
  open
})

</script>
<style scoped lang="scss">
.upload_box {
  height: auto;
  background-color: var(--o--bg-color-base);
  position: relative;
  padding: 4rem;
  margin: 10px;
  z-index: 99999;
  text-align: center;
  margin-left: 0px;

  .content_box {
    padding-left: 26px;
    padding-right: 16px;
    border: 1px dashed #cccc;
    position: absolute;
    top: 0;
    left: 0;
    width: 608px;
    height: 60px;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    z-index: 3;
  }
}

.upload-dialog :deep(.el-dialog) {
  --o-dialog-body-max-height: 600px
}

.upload-win :deep(.el-upload-dragger) {
  width: 600px;
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 14px;
}

.fontsize {
  font-size: 14px;
}

.upload-winauto :deep(.el-upload-dragger) {
  width: 600px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-size: 14px;
}

.el-upload__tip {
  margin-top: 10px;
}

.custom-steps {
  background-color: var(--o--bg-color-base);
  :deep(.el-step.is-process) {
    .el-step__head {
      width: 24px !important;
      height: 24px !important;
      .el-step__icon {
        width: 100% !important;
        height: 100% !important;
        background: var(--el-color-primary) !important;
        border: none !important;
        font-size: 0 !important;
        border-radius: 50% !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        box-shadow: 0 0 0 2px rgba(var(--el-color-primary-rgb), 0.3) !important;
        .el-step__icon-inner {
          color: white !important;
          font-size: 14px !important;
          font-weight: bold !important;
          transform: translateY(0) !important;
        }
      }
    }
  }
}

.step-back {
  margin-left: 100px;
  :deep(.el-step .el-step__main .el-step__description) {
    background-color: var(--o--bg-color-base);
    font-size: 14px;
  }
}

:deep(.dialog-height) {
  min-height: 300px !important;
}

</style>
