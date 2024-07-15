<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :ok-loading="data.loading"
    :modal-style="{ width: '650px'}"
    :footer="false"
    @open="open"
    @close="handleClose"
    @cancel="handleClose"
  >
    <div v-if="data.status === fileStatusEnum.unLoad">
      <div class = "upload_box">
        <div class="upload_context"
             ref="dropArea"
             @dragover.prevent="handleDragOver"
             @dragenter.prevent="handleDragLeave"
             @drop.prevent="handleDrop"
        >
          <svg-icon icon-class="clouduploadsharp" style="font-size: 100px"></svg-icon>
          <p class="content" >{{ $t ('bulk.BulkImport.5exv06n8x1kj0')}}</p>
          <input ref="uploadFileRef" style="display: none" type="file" name="file" @change="uploadchange"/>
          <a-button class="mr" @click = "uploadFilClick" :style="{ backgroundColor: 'red', color: 'white'}">{{ $t ('bulk.BulkImport.5exv06n8x1ki0')}}</a-button>
        </div>
      </div>
      <div><a style="float:left; text-align:left" class="content">{{ $t ('bulk.BulkImport.5exv06n8x1ky0')}}</a>
        <a-link style="float:right; text-align:right " class="btn" @click="downLoadModule">
          <svg-icon icon-class="download" class = "icon.xg"></svg-icon>
          <a class="mr" type="text" style="color: red" download = "模板.xlsx">{{ $t ('bulk.BulkImport.5exv06n8x1ku0')}}</a>
        </a-link>
      </div>
    </div>
    <div v-if="data.status === fileStatusEnum.formLoading" >
      <div v-if="data.formLoaFlag === formLoaFlaEnum.waiting">
        <p>{{ $t ('bulk.BulkImport.5exv06n8x1kv0')}}</p>
        <div style="display: grid; grid-auto-flow: column; gap: 5px; overflow-x: auto;">
          <p v-for="file in data.files" :key="file.id">{{ file.name }}</p>
        </div>
        <div class = 'inflexside'>
          <div class = 'progress'>
            <div class = 'progress-bar' :style = "{ width: `${progress}%`}"></div>
          </div>
          <p style="float:right; text-align:right ">&nbsp;&nbsp;{{ progress}}%</p>
        </div>
      </div>
    </div>
    <div v-if="data.formLoaFlag === formLoaFlaEnum.formWin">
      <a-button class = 'btn' @click = "reupload">{{ $t ('bulk.BulkImport.5exv06n8x1ko0')}}</a-button>
      <p>{{ $t ('bulk.BulkImport.5exv06n8x1kp0')}}</p>
      <div :class="['upload_box']" :style="{ height: '5px' }">
        <div class="content_box"
             ref="dropArea"
             @dragover.prevent = "handleDragOver"
             @dragenter.prevent = "handleDragLeave"
             @drop.prevent = "handleDrop"
             :style = "{backgroundColor: maskColor}"
        >
          <div style="flex: 1; display: flex; align-items: center;">
            <svg-icon icon-class="file-zip" style="font-size: 50px"></svg-icon>
            <div style="display: grid; grid-auto-flow: column; gap: 5px; overflow-x: auto;">
              <p v-for="file in data.files" :key="file.id">{{ file.name }}</p>
            </div>
            <br><p :style="{ color: 'lightgrey'} " >{{(data.files[0].size / 1024).toFixed(2) }}KB</p>
          </div>
          <a-button class = "mr" @click = "reupload" style="margin-left: auto;">{{ $t ('bulk.BulkImport.5exv06n8x1kz0')}}</a-button>
          <input ref="uploadFileRef" style="display: none" type="file" name="file" @change="uploadchange"/>
        </div>
      </div>
      <div v-if="data.formstatus === 'pass'" >
        <p>{{ $t ('bulk.BulkImport.5exv06n8x1kq0')}}</p>
        <div style="text-align: center;">
          <a-button class = 'mr' @click = "anaFilClick">{{ $t ('bulk.BulkImport.5exv06n8x1ka0')}}</a-button>
          <a-button class = 'mr' @click = "handleClose">{{ $t ('bulk.BulkImport.5exv06n8x1kb0')}}</a-button>
        </div>
      </div>
      <div v-else-if="data.formstatus === 'tooBig'"><p>{{ $t('bulk.BulkImport.5exv06n8x1kc0')}}</p></div>
      <div v-else-if="data.formstatus === 'mul'"><p>{{ $t('bulk.BulkImport.5exv06n8x1kd0')}}</p></div>
      <div v-else-if="data.formstatus === 'formErr'"><p>{{ $t('bulk.BulkImport.5exv06n8x1kw0')}}</p></div>
      <div v-else><p>{{ $t('bulk.BulkImport.5exv06n8x1ke0)')}} + {{data.formstatus}}</p></div>
    </div>

    <div v-if="data.status === fileStatusEnum.fileParsing" class = "['upload_box']" :style="{ height: 'auto' }">
      <div class = "content_box">
        <div v-if="data.formLoaFlag === formLoaFlaEnum.anyWin" style="text-align: center">
          <img src="@/assets/images/ops/bulkimport-ing.png" alt="Icon">
          <br><svg-icon icon-class="file-search-suc" style="font-size: 100px"></svg-icon><br>
          <p :style="{ color: 'gray'}" >{{$t('bulk.BulkImport.5exv06n8x1kn0')}}</p>
          <br><p :style="{ color: 'light-gray'}" >{{ $t('bulk.BulkImport.5exv06n8x1kh1')}}</p>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadErr" style="text-align: center">
          <img src="@/assets/images/ops/bulkimport-fail.png" alt="Icon">
          <br><svg-icon icon-class="file-search-fail" style="font-size: 100px"></svg-icon><br>
          <p :style="{ color: 'red'}" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1kx0')}}</p>
          <p :style="{ color: 'grey'}" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1ke0')}}</p>
          <a-button :style="{ color: 'lightblue'}" @click = "reupload" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1ko0')}}</a-button>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadWin" style="text-align: center">
          <img src="@/assets/images/ops/bulkimport-import.png" alt="Icon">
          <!--        <p :style="{ color: 'gray'}" >{{ $t('bulk.BulkImport.5exv06n8x1ki1')}}</p>-->
          <div class = 'inflexside'>
            <div class = 'progress'>
              <div class = 'progress-bar' :style = "{ width: `${progressWidth}%`}"></div>
            </div>
            <p style="float:right; text-align:right; color: lightgrey ">&nbsp;&nbsp;{{progressWidth.toFixed(0)}}%</p>
          </div>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadSuc" style="text-align: center">
          <img src="@/assets/images/ops/bulkimport-done.png" alt="Icon">
          <br><p :style="{ color: 'grey'}" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1ki1')}} {{totalNum}} {{ $t('bulk.BulkImport.5exv06n8x1kj1')}}</p><br>
          <p :style="{ color: 'lightgreen'}" style="display: inline;">{{sucNum}} {{ $t('bulk.BulkImport.5exv06n8x1kk1')}}</p>
          <p :style="{ color: 'grey'}" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1kl1')}}</p>
          <p :style="{ color: 'red'}" style="display: inline;">{{totalNum-sucNum}} {{ $t('bulk.BulkImport.5exv06n8x1kk1')}}</p>
          <p :style="{ color: 'grey'}" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1km1')}}</p><br>
          <br><a-button :style="{ color: 'red'}" @click = "downLoadErrRep" style="display: inline;">{{ $t('bulk.BulkImport.5exv06n8x1kn1')}}</a-button><br>
          <br><a-button @click = "open" :style="{ backgroundColor: 'red', color: 'white'}">{{ $t('bulk.BulkImport.5exv06n8x1kr0')}}</a-button>
          <a-button @click = "handleClose">&emsp;{{ $t('bulk.BulkImport.5exv06n8x1ks0')}}&emsp; </a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { nextTick, reactive, ref, toRaw, computed, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import {bulkImporErrPlan, bulkImportany, bulkuploadPhy} from '@/api/ops'

const { t } = useI18n()

enum fileStatusEnum {
  unLoad = 0,
  formLoading = 1,
  fileParsing = 2,
}

enum formLoaFlaEnum{
  formWin = -1,
  waiting = 0,
  anyWin = 2,
  loadErr = 3,
  loadWin = 5,
  loadSuc = 6,
}

let intervalIdP : number | null = null
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
const uploadchange = (e:any) => {
  const chooseFile = e.target.files
  data.files.push(e.target.files[0])
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
    } if (data.files[0].size / 1024 / 1024 > 200) {
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
    const progressResponse = bulkImporErrPlan({
      params: {
        uuid: uid
      }
    })
      .catch(() => {
        console.log('error catch')
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
      alert('Server error, please try again later.')
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

const checkUploadFil = (uploadFlag:number) => {
  var formDataUploadFil = new FormData()
  const uid = data.uid
  formDataUploadFil.append('uuid', uid)
  formDataUploadFil.append('isInvoke', uploadFlag)
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
  setTimeout(() => {}, 300)
  isUploading.value = true
  progressWidth.value = 0
  sessionStorage.setItem('isUploading', 'true')
  refreshCount.value = 0
  if (data.uid.length <= 1) {
    alert('error uid length')
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
      setTimeout(() => {}, 500)
      data.formLoaFlag = formLoaFlaEnum.loadSuc
    }
  }
  if (sessionStorage.getItem('isUploading') === 'true') {
    isUploading.value = true
  }
}

const uploadedBytes = ref(0)
onMounted(anaFilPhy)
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
  let url='/host/downloadTemplate';
  axios.get(url,{responseType: 'blob',headers: {'Content-Type':'application/json;application/octet-stream'}
  })
    .then((res)=>{
      if (res) {
        const blob = new Blob([res], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
        const link = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        let herf = URL.createObjectURL(blob);
        link.href = herf;
        link.download = "模板.xlsx";
        link.click();
        window.URL.revokeObjectURL(herf);
      }
    }).catch((err)=>{
    console.log("error:"+err);
  })
}

const downLoadErrRep = () => {
  const uuid = data.uid
  let url=`/host/downloadErrorExcel/${uuid}`;
  axios.get(url,{responseType: 'blob',headers: {'Content-Type':'application/json;application/octet-stream'}
  })
    .then((res)=>{
      if (res) {
        const blob = new Blob([res], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
        const link = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        let herf = URL.createObjectURL(blob);
        link.href = herf;
        const date = new Date();
        const formattedDate = `${date.getFullYear()}${(date.getMonth() + 1).toString().padStart(2, '0')}${date.getDate().toString().padStart(2, '0')}${date.getHours().toString().padStart(2, '0')}${date.getMinutes().toString().padStart(2, '0')}${date.getSeconds().toString().padStart(2, '0')}`;
        link.download = `错误报告_${formattedDate}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(herf);
      }
    }).catch((err)=>{
    console.log("error:"+err);
  })
}

const refreshCount = ref(parseInt(sessionStorage.getItem('refreshCount') || '0'))

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

function handleBeforeUnload (event: BeforeUnloadEvent) {
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

const close = () => {
  nextTick(() => {
  })
}

defineExpose({
  open
})

</script>
<style scoped lang="scss">
.upload_box {
  background-color: rgba(255, 255, 255, 1);
  position: relative;
  padding: 4rem;
  margin: 10px;
  z-index: 99999;
  border: 2px dashed #e2e8f0;
  text-align: center;

  .content_box {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    z-index: 3;
  }

  .liner_box {
    position: absolute;
    top: 0;
    left: 0;
    width: 100% ;
    height: 100%;
    background-color: rgba(255, 255, 255, 1);
    border: 2px dashed #e2e8f0;
    padding: 4rem;
    margin: 10px;
    z-index: 2;
    text-align: center;
  }

  .upload_content {
    border: 1px dashed #cccc;
    height: auto;
    cursor: pointer;
    position: relative;
    z-index: 1;

    .imge {
      width: 70px;
      height: 70px;
    }

    .desc {
      padding: 0 40px;
    }
  }
}

.upload_files {
  margin-top: 8px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(#9999, 0.2);

  > a {
    color: #02a7f0;
  }
  .loading {
    animation: loading 1s linear infinite;
  }
  .inflexside{
    display: flex;
    align-items: center;
  }
  .btn{
    width:80px;
    height: 30px;
    vertical-align:middle;
    box-sizing: content-box;
    color: red;
  }
  .content{
    width:200px;
    height:30px;
    vertical-align:middle;
    color: grey;
  }
}
.progress {
  margin-top: 10px;
  width: 95%;
  height: 12px;
  background-color: lightgrey;
}
.progress-bar {
  height: 100%;
  background-color: red;
  transition: width 0.2s;
}
</style>
