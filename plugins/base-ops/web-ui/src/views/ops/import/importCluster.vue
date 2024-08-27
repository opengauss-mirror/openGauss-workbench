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
          <p class="content" >{{ $t ('components.BatchImport.5mpmzg3zt1o0')}}</p>
          <input ref="uploadFileRef" style="display: none" type="file" name="file" @change="uploadchange"/>
          <a-button class="mr" @click = "uploadFilClick" :style="{ backgroundColor: 'red', color: 'white'}">{{ $t ('components.BatchImport.5mpmzg3zt1o1')}}</a-button>
        </div>
      </div>
      <div><a style="float:left; text-align:left" class="content">{{ $t ('components.BatchImport.5mpmzg3zesl0')}}</a>
        <a-link style="float:right; text-align:right " class="btn" @click="downLoadModule">
          <svg-icon icon-class="download" class = "icon.xg"></svg-icon>
          <a class="mr" type="text" style="color: red" download = "模板.xlsx">{{ $t ('components.BatchImport.5mpmzg3zesl1')}}</a>
        </a-link>
      </div>
    </div>
    <div v-if="data.status === fileStatusEnum.formLoading" >
      <div v-if="data.formLoaFlag === formLoaFlaEnum.waiting">
        <p>{{ $t ('components.BatchImport.5mpmzg3zesl2')}}</p>
        <p>{{data.files.name}}</p>
        <div class = 'inflexside'>
          <div class = 'progress'>
            <div class = 'progress-bar' :style = "{ width: `${progress}%`}"></div>
          </div>
          <p style="float:right; text-align:right ">&nbsp;&nbsp;{{ progress}}%</p>
        </div>
      </div>
    </div>
    <div v-if="data.formLoaFlag === formLoaFlaEnum.formWin">
      <a-button class = 'btn' @click = "reupload">{{ $t ('components.BatchImport.5mpmzg3zesl3')}}</a-button>
      <p>{{ $t ('components.BatchImport.5mpmzg3zesl4')}}</p>
      <div :class="['upload_box']" :style="{ height: '5px' }">
        <div class="content_box"
             ref="dropArea"
             @dragover.prevent = "handleDragOver"
             @dragenter.prevent = "handleDragLeave"
             @drop.prevent = "handleDrop"
             :style = "{backgroundColor: maskColor}"
        >
          <svg-icon icon-class="file-zip" style="font-size: 100px"></svg-icon>
          <p style="float:left; text-align:left">{{data.files.name}}</p>
          <a-button class = "mr" @click = "reupload" style="float:right; text-align:right">{{ $t ('components.BatchImport.5mpmzg3zesl5')}}</a-button>
          <p :style="{ color: 'lightgrey'} " >{{(data.files.size / 1024).toFixed(2) }}KB</p>
          <input ref="uploadFileRef" style="display: none" type="file" name="file" @change="uploadchange"/>
        </div>
      </div>
      <div v-if="data.formstatus === 'pass'" >
        <p>{{ $t ('components.BatchImport.5mpmzg3zesl6')}}</p>
        <div style="text-align: center;">
          <a-button class = 'mr' @click = "anaFilClick">{{ $t ('components.BatchImport.5mpmqndzeok0')}}</a-button>
          <a-button class = 'mr' @click = "handleClose">{{ $t ('components.BatchImport.5mpmqndzeok1')}}</a-button>
        </div>
      </div>
      <div v-else-if="data.formstatus === 'tooBig'"><p>{{ $t('components.BatchImport.5mpmeddf3zl0')}}</p></div>
      <div v-else-if="data.formstatus === 'mul'"><p>{{ $t('components.BatchImport.5mpmeddf3zl1')}}</p></div>
      <div v-else-if="data.formstatus === 'formErr'"><p>{{ $t('components.BatchImport.5mpmeddf3zl2')}}</p></div>
      <div v-else><p>{{ $t('components.BatchImport.5mpmeddf3zl3')}} + {{data.formstatus}}</p></div>
    </div>

    <div v-if="data.status === fileStatusEnum.fileParsing" class = "['upload_box']" :style="{ height: 'auto' }">
      <div class = "content_box">
        <div v-if="data.formLoaFlag === formLoaFlaEnum.anyWin" style="text-align: center">
          <img src="@/assets/images/modeling/ops/bulkimport-ing.png" alt="Icon">
          <div class="steps"><span>1 {{ $t('components.BatchImport.5mpmqndzeok2')}}</span>
            <span>2 {{ $t('components.BatchImport.5mpmqndzeok3')}}</span>
            <span>3 {{ $t('components.BatchImport.5mpmqndzeok4')}}</span></div>
          <br><svg-icon icon-class="file-search-suc" style="font-size: 100px"></svg-icon><br>
          <p :style="{ color: 'gray'}" >{{$t('components.BatchImport.5mpmeddf3zl4')}}</p>
          <br><p :style="{ color: 'light-gray'}" >{{ $t('components.BatchImport.5mpmeddf3zl5')}}</p>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadErr" style="text-align: center">
          <img src="@/assets/images/modeling/ops/bulkimport-fail.png" alt="Icon">
          <div class="steps"><span>1 {{ $t('components.BatchImport.5mpmqndzeok2')}}</span>
            <span>2 {{ $t('components.BatchImport.5mpmqndzeok3')}}</span>
            <span>3 {{ $t('components.BatchImport.5mpmqndzeok4')}}</span></div>
          <br><svg-icon icon-class="file-search-fail" style="font-size: 100px"></svg-icon><br>
          <p :style="{ color: 'red'}" style="display: inline;">{{ $t('components.BatchImport.5mpmeddf3zl7')}}</p>
          <p :style="{ color: 'grey'}" style="display: inline;">{{ $t('components.BatchImport.5mpmeddf3zl3')}}</p>
          <a-button :style="{ color: 'rgb(5, 158, 247)'}" @click = "reupload" style="display: inline;">{{ $t('components.BatchImport.5mpmzg3zesl3')}}</a-button>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadWin" style="text-align: center">
          <img src="@/assets/images/modeling/ops/bulkimport-import.png" alt="Icon">
          <div class="steps"><span>1 {{ $t('components.BatchImport.5mpmqndzeok2')}}</span>
            <span>2 {{ $t('components.BatchImport.5mpmqndzeok3')}}</span>
            <span>3 {{ $t('components.BatchImport.5mpmqndzeok4')}}</span></div>
          <!--        <p :style="{ color: 'gray'}" >{{ $t('bulk.BulkImport.5exv06n8x1ki1')}}</p>-->
          <div class = 'inflexside'>
            <div class = 'progress'>
              <div class = 'progress-bar' :style = "{ width: `${progressWidth}%`}"></div>
            </div>
            <p style="float:right; text-align:right; color: lightgrey ">&nbsp;&nbsp;{{progressWidth.toFixed(0)}}%</p>
          </div>
        </div>
        <div v-if="data.formLoaFlag === formLoaFlaEnum.loadSuc" style="text-align: center">
          <img src="@/assets/images/modeling/ops/bulkimport-done.png" alt="Icon">
          <div class="steps"><span>1 {{ $t('components.BatchImport.5mpmqndzeok2')}}</span>
            <span>2 {{ $t('components.BatchImport.5mpmqndzeok3')}}</span>
            <span>3 {{ $t('components.BatchImport.5mpmqndzeok4')}}</span></div>
          <br><p :style="{ color: 'grey'}" style="display: inline;">{{ $t('components.BatchImport.5mpmqndzeok5',{totalNum:totalNum})}}</p><br>
          <p :style="{ color: 'lightgreen'}" style="display: inline;"> {{ $t('components.BatchImport.5mpmewrfzse0',{num:sucNum})}}</p>
          <p :style="{ color: 'grey'}" style="display: inline;">{{ $t('components.BatchImport.5mpmewrfzse1')}}</p>
          <p :style="{ color: 'red'}" style="display: inline;"> {{ $t('components.BatchImport.5mpmewrfzse0',{num:totalNum-sucNum})}}</p>
          <p :style="{ color: 'grey'}" style="display: inline;">{{ $t('components.BatchImport.5mpmewrfzse3')}}</p><br>
          <br><a-button :style="{ color: 'red'}" @click = "downLoadErrRep" style="display: inline;">{{ $t('components.BatchImport.5mpmewrfzse4')}}</a-button><br>
          <br><a-button @click = "open" :style="{ backgroundColor: 'red', color: 'white'}">{{ $t('components.BatchImport.5mpmewrfzse5')}}</a-button>
          <a-button @click = "handleClose">&emsp;{{ $t('components.BatchImport.5mpmewrfzse6')}}&emsp; </a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { nextTick, reactive, ref, toRaw, computed, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import useLocale from "@/hooks/locale";

const { currentLocale } = useLocale()

const { t } = useI18n()

enum fileStatusEnum {
  unLoad = 0, // 批量导入最初界面
  formLoading = 1, // 表格上传/成功/失败界面
  // formTest = 2,
  fileParsing = 2, // 文件解析界面
}

enum formLoaFlaEnum{
  formWin = -1, // 展示上传文件是否符合要求
  waiting = 0, // 等待上传文件进度条走完
  anyWin = 2, // 1文件解析界面
  loadErr = 3, // 解析失败界面
  loadWin = 5, // 2导入到服务器界面
  loadSuc = 6, // 3导入完成界面
}

let intervalIdP : number | null = null
let percentage = ref(0)
let totalNum = ref(0)
let sucNum = ref(0)
// const isRunning = ref(false)
const progressWidth = ref(0)

const data = reactive<KeyValue>({
  show: false,
  title: t('components.ChooseVersion.5mpmxod901g1'),
  status: fileStatusEnum.unLoad,
  // formstatus:'ready',
  files: [],
  fileCount: 0,

  percent: 0,
  formstatus: 'untest',
  formLoaFlag: formLoaFlaEnum.waiting
  // untest没有测试，ready测试通过，其他字符代表测试未通过原因
})

// const dragEnter = ref(false)
// const emits = defineEmits<{
//   (e: 'update:modelValue', value: any): void
//   (e: string): void
// }>()
// 打开
const open = () => {
  data.show = true
  // emits('update:modelValue', true)
  data.status = fileStatusEnum.unLoad
  data.files = []
  data.fileCount = 0

  data.title = t('components.ChooseVersion.5mpmxod901g1')
  data.formstatus = 'untest'
  data.percent = 0
  data.formLoaFlag = formLoaFlaEnum.waiting
}
//重新上传
const reupload = () => {
  data.status = fileStatusEnum.unLoad
  data.files = []
  data.fileCount = 0

  data.formstatus = 'untest'
  data.percent = 0
  data.formLoaFlag = formLoaFlaEnum.waiting
  progressWidth.value = 0
}
// 关闭
const handleClose = () => {
  data.show = false
  let uploadFlag = 1
  // checkUploadFil(uploadFlag)
  data.files = []
  data.fileCount = 0

  data.percent = 0
  progressWidth.value = 0
  data.formLoaFlag = formLoaFlaEnum.waiting
  clearInterval(intervalIdP)
  // 在组件卸载时移除事件监听器
  window.removeEventListener('beforeunload', handleBeforeUnload);
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
// 原生上传
const uploadchange = (e:any) => {
  const chooseFile = e.target.files
  data.files = e.target.files[0]
  data.fileCount = 1
  uploadFilPhy(chooseFile)
}
// 拖拽区域
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
  const files = e.dataTransfer?.files as FileList
  data.files = files[0]
  uploadFilPhy(files)
}
// 点击上传事件
const uploadFilClick = () => {
  uploadFileRef.value.click()
}
// 检测文件大小格式
const checkFileForm = (rawFile: any) => {
  if (data.filesCount > 1) {
    return 'mul'
  } else {
    const allowTypes = ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']
    if (!allowTypes.includes(data.files.type)) {
      return 'formErr'
    } if (data.files.size / 1024 / 1024 > 200) {
      return 'tooBig'
    }
    return 'pass'
  }
  // alert('form error')
}
// 上传文件进度条
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
        // data.status = fileStatusEnum.formLoading
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
// 上传文件
const uploadFilPhy = (rawFile: any) => {
  const formData = new FormData()
  data.formStatus = 'untest'
  progress.value = 0
  progressbar()
  formData.append('file', data.files)
  data.status = fileStatusEnum.formLoading
  const fileformtest = checkFileForm(data.files)
  if (fileformtest === 'pass') {
    axios.post('/opsCluster/uploadImportFile', formData)
      .then(response => {
        if (response.code == '200') {
          data.uid = response.msg
          data.formstatus = 'pass'
        } else {
          data.formstatus = 'error'
        }
      })
      .catch(error => {
        console.error(error)
      })
  } else {
    data.formstatus = fileformtest
  }
}

const fetchProgress = async () => {
  let formLoaFlagTemp = formLoaFlaEnum.loadWin
  try {
    const progressResponse = await axios.get('/opsCluster/parseExcel')
      .catch(() => {
        console.log('error catch')
        formLoaFlagTemp = formLoaFlaEnum.loadErr
        data.formLoaFlag = formLoaFlagTemp
      })
    console.log("list的值:"+progressResponse)
    percentage.value = (progressResponse.data.doneSum / progressResponse.data.totality) * 100
    if (progressResponse.data !== null) {
      if(progressResponse.code != '200'){
        formLoaFlagTemp = formLoaFlaEnum.loadErr
      }else{
        percentage.value = 100
        progressWidth.value = 100
        setTimeout(() => {data.formLoaFlag = formLoaFlaEnum.loadSuc}, 1000)
        totalNum.value = progressResponse.data
        const importSuccessCount = await axios.get('/opsCluster/importSuccessCount')
          .catch(() => {
            formLoaFlagTemp = formLoaFlaEnum.loadErr
            data.formLoaFlag = formLoaFlagTemp
          })
        sucNum.value = importSuccessCount.data
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
  anaFilPhy()
}


const isUploading = ref(false)
// 分析文件
const anaFilPhy = async () => {
  data.status = fileStatusEnum.fileParsing
  console.log(1)
  setTimeout(() => {data.formLoaFlag = formLoaFlaEnum.anyWin},0)
  // isRunning.value = true
  isUploading.value = true
  progressWidth.value = 0 //进度条是0
  sessionStorage.setItem('isUploading', 'true');
  // data.formLoaFlag = formLoaFlaEnum.loadWin
  startProgressBar()
  if (data.percent < 100 && data.formLoaFlag != formLoaFlaEnum.loadErr){
    fetchProgress()
  } else{
    if(data.formLoaFlag != formLoaFlaEnum.loadErr){
      data.formLoaFlag = formLoaFlaEnum.loadWin
    }
  }
  if (sessionStorage.getItem('isUploading') === 'true') {
    isUploading.value = true;
  }
}

const uploadedBytes = ref(0);
// 根据时间设置假进度条
const startProgressBar = () => {
  console.log(2)
  uploadedBytes.value = 0
  progressWidth.value = 0
  let interval = 500
  let timeElapsed = 0
  let totalSteps = 0
  let uploadSpeedMbps = 0.0005
  const updateProgress = () => {
    uploadedBytes.value += (uploadSpeedMbps * 1024) / 10
    totalSteps = Math.min((uploadedBytes.value / data.files.size ) * 100, 100)
    uploadSpeedMbps = 10.0 / (1 + timeElapsed / 10)
    progressWidth.value = totalSteps
    timeElapsed += 1
    if (progressWidth.value < 90 && data.percent < 100) {
      data.formLoaFlag = formLoaFlaEnum.loadWin
      setTimeout(updateProgress, interval)
    }
  }
  updateProgress()
}

const downLoadModule = () => {
  let url=`/opsCluster/downloadImportFile/${currentLocale.value}`;
  axios.get(url,{responseType: 'blob',headers: {'Content-Type':'application/json;application/octet-stream'}
  })
    .then((res)=>{
      if (res) {
        const blob = new Blob([res], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
        const link = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        let herf = URL.createObjectURL(blob);
        link.href = herf;
        link.download = `${t('components.BatchImport.5mpmertfggy0')}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(herf);
      }
    }).catch((err)=>{
    console.log("error:"+err);
  })
}

const downLoadErrRep = () => {
  let url=`/opsCluster/downloadErrorFile/${currentLocale.value}`;
  axios.get(url,{responseType: 'blob',headers: {'Content-Type':'application/json;application/octet-stream'}
  })
    .then((res)=>{
      if (res) {
        const blob = new Blob([res], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
        const link = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        let herf = URL.createObjectURL(blob);
        link.href = herf;
        link.download = `${t('components.BatchImport.5mpmertfggy1')}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(herf);
      }
    }).catch((err)=>{
    console.log("error:"+err);
  })
}

// 添加 beforeunload 事件监听以拦截页面刷新
const refreshCount = ref(parseInt(sessionStorage.getItem('refreshCount') || '0'));

//添加事件监听器
onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload);
});




// 处理 beforeunload 事件
function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (isUploading.value) {
    event.preventDefault();
    event.returnValue = '';
  } else if (refreshCount.value === 1) {
    // 在此调用自定义的确认对话框
    const confirmationMessage = t('components.BatchImport.5mpmeddf3zl6');
    if (!confirm(confirmationMessage)) {
      event.preventDefault();
      event.returnValue = '';
    } else {
      sessionStorage.setItem('refreshCount', '0'); // 重置刷新计数
    }
  } else {
    event.preventDefault();
    event.returnValue = '';
  }
}

// 更新刷新计数
window.addEventListener('beforeunload', () => {
  sessionStorage.setItem('refreshCount', (refreshCount.value + 1).toString());
});



// const close = () => {
//   nextTick(() => {
//   })
// }

defineExpose({
  open
})

</script>
<style scoped lang="scss">
  .steps {
     display: flex;
     justify-content: space-between;
     padding:0 95px
    }
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
    flex-direction: column;
    justify-content: center;
    align-items: center;
    z-index: 3;
  }

  .liner_box {
    position: absolute; // 绝对定位
    top: 0;
    left: 0;
    width: 100% ; // 减去 margin 的宽度
    height: 100%; // 减去 margin 的高度
    background-color: rgba(255, 255, 255, 1);
    border: 2px dashed #e2e8f0; // 设置较宽的虚线边框
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
    //justify-content: space-between;
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
