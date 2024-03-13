<template>
  <div class="dialog">
    <el-dialog
      :width="dialogWith"
      :title="isReinstall ? t('install.reinstallProxy') : editing ? t('install.editProxy') : t('install.installProxy')"
      v-model="visible"
      :close-on-click-modal="false"
      draggable
      @close="closeDialog"
    >
      <div class="dialog-content" v-show="installData.length != 0">
        <div>
          <el-steps direction="vertical" :active="doingIndex">
            <el-step v-for="item in installData" :key="item.name" :title="item.name">
              <template #description>
                <div v-for="msg in item.msgs" :key="msg">
                  <b>{{ msg }}</b>
                </div>
                <el-input v-if="item.error" v-model="item.error" :rows="5" type="textarea" readonly />
              </template>
            </el-step>
          </el-steps>
        </div>
      </div>
      <div class="dialog-content" v-loading="started" v-show="installData.length === 0">
        <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef">
          <el-form-item :label="t('install.machine')" prop="nodeId" v-if="!editing || formData.type === 'PROMETHEUS'">
            <Machines
              width="200"
              @change="changeMachine"
              :disabled="editing"
              :initValue="formData.nodeId"
              autoSelectFirst
              notClearable
              style="width: 200px; margin: 0 4px"
            />
          </el-form-item>
          <el-form-item :label="t('install.installUser')" prop="username" v-if="!editing || formData.type === 'PROMETHEUS'">
            <el-select v-model="formData.username" style="width: 200px; margin: 0 4px" :disabled="editing">
              <el-option
                v-for="item in hostUserList"
                :key="item.hostUserId"
                :label="item.username"
                :value="item.username"
                :disabled="item.username === 'root'"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('install.rootPWD')" prop="rootPassword" v-if="formData.username === ROOT_USER">
            <el-input v-model="formData.rootPassword" show-password style="width: 200px; margin: 0 4px" />
          </el-form-item>
          <el-form-item :label="t('install.proxyPort')" prop="port">
            <el-input-number
              @click.stop="() => {}"
              class="left-align"
              style="width: 200px; margin: 0 4px"
              v-model="formData.port"
              :min="0"
              :max="65535"
              :controls="false"
            >
            </el-input-number>
          </el-form-item>
          <el-form-item :label="t('install.installPath')" prop="path">
            <el-input v-model="formData.path" style="width: 200px; margin: 0 4px" :disabled="editing" />
          </el-form-item>
          <el-form-item :label="t('install.storageDays')" prop="storageDays" v-if="!editing || formData.type === 'PROMETHEUS'">
            <el-input
              v-model="formData.storageDays"
              style="width: 200px; margin: 0 4px"
              @input="allNumber(formData.storageDays)"
            >
              <template #append>{{ t('install.storageDaysUnit') }}</template>
            </el-input>
          </el-form-item>
          <!-- <el-form-item :label="t('install.installMode')" prop="installMode">
            <el-radio-group v-model="formData.installMode">
              <el-radio label="online">{{ t('install.online') }}</el-radio>
              <el-radio label="offline">{{ t('install.offline') }}</el-radio>
            </el-radio-group>
          </el-form-item> -->
          <div v-if="formData.installMode === 'offline'">
            <el-form-item label="Prometheus" style="margin-bottom: 0">
              <el-link :underline="false" @click="showUploadFile('node', formData.pkg)">{{
                t('install.uploadPkg')
              }}</el-link>
              <el-popover
                placement="top-start"
                :width="350"
                trigger="hover"
                :content="t('install.pleaseUpload') + formData.pkg"
              >
                <template #reference>
                  <el-icon class="upload-icon"><Warning /></el-icon>
                </template>
              </el-popover>
            </el-form-item>
            <el-form-item required :label="t('install.nodesrc')">
              <div>{{ formData.src ? formData.src : '--' }}</div>
            </el-form-item>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div v-if="installData.length === 0">
          <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
          <el-button :loading="started" style="padding: 5px 20px" type="primary" @click="install">
            {{ isReinstall ? $t('install.reinstall') : $t('install.install') }}
          </el-button>
        </div>
        <div v-else-if="installData.length != 0 && installData[installData.length - 2].state === 'DONE'">
          <el-button @click="back">{{ $t('app.back') }}</el-button>
          <el-button style="padding: 5px 20px" @click="handleCancelModel" type="primary">
            {{ $t('app.done') }}
          </el-button>
        </div>
        <div v-else>
          <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
          <el-button style="padding: 5px 20px" @click="back">{{ $t('app.back') }}</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      v-if="showUpload"
      v-model="showUpload"
      :close-on-click-modal="false"
      :title="t('install.pleaseUpload') + pgkName"
    >
      <div class="dialog-content" style="padding-bottom: 0px">
        <div class="suggest-info">
          <span>{{ t('install.downloadSuggest') }}：</span><el-link :href="formData.url">{{ formData.url }}</el-link>
        </div>

        <el-upload
          v-model:file-list="fileList"
          drag
          :http-request="upload"
          :limit="1"
          :show-file-list="true"
          :on-exceed="handleExceed"
          :before-upload="uploadBefore"
        >
          <el-icon class="el-icon--upload"><plus /></el-icon>
          <div class="el-upload__text">{{ t('install.uploadInfo') }}</div>
        </el-upload>
        <div>
          <el-progress v-if="showProgress" :percentage="progressPercent" :format="progressFormat" />
          <el-button type="primary" :icon="RefreshRight" @click="retryUpload" v-if="retry">
            {{ t('install.continueUpload') }}
          </el-button>
        </div>
      </div>
    </el-dialog>
    <el-dialog v-model="tipVisible" :title="t('install.tip')">
      <div class="dialog-content">{{t('install.reinstallMainServerTip')}}</div>
      <template #footer>
        <div>
          <el-button style="padding: 5px 20px" @click="tipVisible = false">{{ $t('app.cancel') }}</el-button>
          <el-button :loading="started" style="padding: 5px 20px" type="primary" @click="tipConfirm">
            {{ $t('app.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import { FormRules, FormInstance, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import WebSocketClass from '@/utils/websocket'
import { encryptPassword } from '@/utils/jsencrypt'
import moment from 'moment'
import { Plus, Warning, RefreshRight } from '@element-plus/icons-vue'
import type { UploadProps } from 'element-plus'
import restRequest from '@/request/restful'
const { t } = useI18n()

const visible = ref(false)
const props = withDefaults(
  defineProps<{
    show: boolean
    editing?: boolean
    node?: any,
    title: string
    isReinstall: boolean
  }>(),
  {
    title: '',
    isReinstall: false
  }
)
watch(
  () => props.show,
  (newValue) => {
    visible.value = newValue
  },
  { immediate: true }
)

// form data
const initFormData = {
  nodeId: '',
  username: '',
  rootPassword: '',
  port: 9090,
  path: '',
  storageDays: 15,
  installMode: 'online',
  pkg: '',
  src: '',
  url: '',
  fileList: [],
  uploadPath: '',
  envId: '',
  type: ''
}
const ROOT_USER = 'root'
const hostUserList = ref<any[]>()
const formData = reactive(cloneDeep(initFormData))
const changeMachine = (val: any) => {
  formData.nodeId = val
  if (formData.nodeId) {
    getPkgInfo(formData.nodeId)
    getHostUserList(formData.nodeId)
  }
}
const connectionFormRef = ref<FormInstance>()
const connectionFormRules = reactive<FormRules>({
  nodeId: [{ required: true, message: t('install.proxyRules[0]'), trigger: 'blur' }],
  username: [{ required: true, message: t('install.proxyRules[3]'), trigger: 'blur' }],
  rootPassword: [{ required: false, message: t('install.proxyRules[1]'), trigger: 'blur' }],
  port: [{ required: true, message: t('install.proxyRules[2]'), trigger: 'blur' }],
  storageDays: [{ required: true, message: t('install.proxyRules[4]'), trigger: 'blur' }],
  path: [{ required: true, message: t('install.collectorRules[4]'), trigger: 'blur' }],
})

watch(
  () => formData.username,
  (newValue) => {
    if (newValue === ROOT_USER) {
      connectionFormRules.rootPassword = [{ required: true, message: t('install.collectorRules[1]'), trigger: 'blur' }]
    } else {
      connectionFormRules.rootPassword = [{ required: false, message: t('install.collectorRules[1]'), trigger: 'blur' }]
    }
  }
)

const getPkgInfo = (hostId: string) => {
  const key = 'prometheus'
  restRequest
    .get('/observability/v1/environment/pkg', {
      key,
      hostId,
    })
    .then(function (res) {
      if (res) {
        formData.pkg = res.pkg
        formData.src = res.src
        formData.url = res.url
      }
      return res
    })
    .catch(function (res) {})
}

const getHostUserList = (hostId: string) => {
  restRequest.get(`/observability/v1/environment/hostUser/${hostId}`).then((res) => {
    if (Array.isArray(res)) {
      hostUserList.value = res || []
      if (!formData.username) {
        return
      }
      let userList = hostUserList.value.filter(item => item.username === formData.username) || []
      if (userList.length === 0) {
        formData.username = ''
      }
    }
  })
}
const getInstallPath = () => {
  restRequest.get(`/observability/v1/environment/basePath`).then((res) => {
    if (res) {
      formData.path = res + (res.endsWith('/') ? 'data' : '/data') + '/prometheus'
    }
  })
}

const allNumber = (value: any) => {
  if (!value || !value.trim()) {
    return
  }
  formData.storageDays = value.replace(/[^\d]/g, '')
  if (formData.storageDays < 1) {
    formData.storageDays = 1
  }
  if (formData.storageDays > 365) {
    formData.storageDays = 365
  }
}

const started = ref(false)
const installSucceed = ref(false)
const ws = reactive({
  name: '',
  webUser: '',
  connectionName: '',
  sessionId: '',
  instance: null,
})
const tipVisible = ref<boolean>(false)
const tipConfirm = () => {
  tipVisible.value = false
  createWsAndSendData()
}
const install = async () => {
  let result = await connectionFormRef.value?.validate()
  if (!result) return
  if (props.isReinstall) {
    tipVisible.value = true
  } else {
    createWsAndSendData()
  }
}
const createWsAndSendData = () => {
  started.value = true
  ws.name = moment(new Date()).format("YYYYMMDDHHmmss") as string; // websocket connection name
  ws.sessionId = moment(new Date()).format("YYYYMMDDHHmmss") as string; // websocket connection id
  ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage);
  sendData();
}
const sendData = async () => {
  let key = props.isReinstall ? 'reinstall prometheus' : 'prometheus'
  let sendData = {
    key,
    envId: formData.envId,
    hostId: formData.nodeId,
    username: formData.username,
    installMode: formData.installMode,
    type: formData.type,
    // rootPassword: encryptPwd,
    path: formData.path,
    port: formData.port,
    storageDays: formData.storageDays + 'd',
    language: localStorage.getItem('locale') === 'en-US' ? 'en_US' : 'zh_CN',
  }
  if (formData.rootPassword) {
    const encryptPwd = await encryptPassword(formData.rootPassword)
    sendData = Object.assign(sendData, { rootPassword: encryptPwd })
  }
  ws.instance.send(sendData)
}
const onWebSocketMessage = (data: Array<any>) => {
  if (data) {
    try {
      installData.value = JSON.parse(data)
    } catch (error) {
      installData.value.forEach((item) => {
        if (item.state === 'ERROR') {
          item['error'] = data
          dialogWith.value = '800px'
        }
      })
    }
  }
}

// action
const back = () => {
  started.value = false
  dialogWith.value = '400px'
  ws.instance.close()
  installData.value = []
}

// list Data
const installData = ref<Array<any>>([])
const dialogWith = ref<string>('400px')
const doingIndex = computed(() => {
  for (let index = 0; index < installData.value.length; index++) {
    const element = installData.value[index]
    if (element.state === 'DOING' || element.state === 'ERROR') return index
  }
  if (installData.value.length > 0 && !installSucceed.value) installSucceed.value = true
  return installData.value.length
})

// dialog
const emit = defineEmits(['changeModal', 'installed'])
const handleCancelModel = () => {
  visible.value = false
  if (installSucceed.value) emit('installed')
  emit('changeModal', visible.value)
}
const closeDialog = () => {
  visible.value = false
  if (installSucceed.value) emit('installed')
  emit('changeModal', visible.value)
}

// package info
const refreshPkgInfo = () => {
  if (formData.nodeId) {
    getPkgInfo(formData.nodeId)
  }
}

const showUpload = ref<boolean>(false)
const fileList = ref<any[]>()
const pgkName = ref<string>()
const showProgress = ref<boolean>(false)
const progressPercentList = ref<number[]>([])
const progressPercent = computed({
  get() {
    return fileChunkList.value.length > 0
      ? parseInt(
          progressPercentList.value?.reduce((acc, cur) => {
            return acc + cur
          }, 0) / fileChunkList.value.length
        )
      : 0
  },
  set(value) {
    return value
  },
})
const progressFormat = (percentage: number) => percentage + '%'
const showUploadFile = (_type: string, _pgkName: string) => {
  showUpload.value = true
  pgkName.value = _pgkName
}
const DEFAULT_SIZE = 15 * 1024 * 2024
const fileTotalSize = ref<number>(0)
const chunkCurNum = ref<number>(0)
const errChunkNum = ref<number>(0)
const fileChunkList = ref<any[]>([])
const retry = ref<boolean>(false)
const createFileChunk = (file: any, size = DEFAULT_SIZE) => {
  fileChunkList.value = []
  progressPercentList.value = []
  let cur = 0
  while (cur < file.size) {
    fileChunkList.value?.push(file.slice(cur, cur + size))
    progressPercentList.value?.push(0)
    cur += size
  }
}
const uploadChunk = (fileChunk: any, name: string, index: number) => {
  let formData = new FormData()
  formData.append('name', name + '-' + index)
  formData.append('pkg', fileChunk)
  return new Promise((resolve, reject) => {
    restRequest
      .post('/observability/v1/environment/upload', formData, {
        headers: { contentType: 'multipart/form-data' },
        onUploadProgress: (event) => {
          progressPercentList.value[index] = parseInt((event.loaded / event.total) * 100)
        },
      })
      .then(function (res) {
        if (res && res === 'success') {
          chunkCurNum.value++
          resolve(name + '-' + index)
        } else {
          progressPercentList.value[index] = 0
          errChunkNum.value++
          reject(name + '-' + index)
        }
      })
      .catch(function (res) {
        progressPercentList.value[index] = 0
        errChunkNum.value++
        reject(name + '-' + index)
      })
  })
}
const uploadMerge = (name: string, total: number) => {
  restRequest
    .post(`/observability/v1/environment/merge?name=${name}&total=${total}`)
    .then((res) => {
      if (!res) {
        ElMessage({
          message: t('install.uploadSucceed'),
          type: 'success',
        })
        refreshPkgInfo()
        fileChunkList.value = []
        showProgress.value = false
        showUpload.value = false
        progressPercent.value = 0
      } else {
        retry.value = true
        ElMessage({
          message: t('install.uploadFail'),
          type: 'error',
        })
      }
    })
    .catch((err) => {
      retry.value = true
      ElMessage({
        message: t('install.uploadFail'),
        type: 'error',
      })
    })
}
const upload = async (action: any) => {
  if (!action) {
    return
  }
  chunkCurNum.value = 0
  errChunkNum.value = 0
  fileTotalSize.value = action.file.size
  createFileChunk(action.file)
  showProgress.value = true
  const requestList = fileChunkList.value.map(async (item, index) => {
    return uploadChunk(item, pgkName.value, index)
  })
  Promise.all(requestList)
    .then((res) => {
      fileList.value = [{ name: action.file.name, raw: action.file }]
      if (errChunkNum.value === 0) {
        uploadMerge(pgkName.value, fileChunkList.value.length)
      } else {
        retry.value = true
        ElMessage({
          message: t('install.uploadFail'),
          type: 'error',
        })
      }
    })
    .catch((res) => {
      retry.value = true
      ElMessage({
        message: t('install.uploadFail'),
        type: 'error',
      })
    })
}
const retryUpload = async () => {
  retry.value = false
  if (chunkCurNum.value === fileChunkList.value.length) {
    uploadMerge(pgkName.value, fileChunkList.value.length)
    return
  }
  errChunkNum.value = 0
  const requestList = fileChunkList.value.map((item, index) => {
    if (progressPercentList.value[index] === 0) {
      return uploadChunk(item, pgkName.value, index)
    } else {
      return new Promise((resolve, reject) => {
        resolve('')
      })
    }
  })
  Promise.all(requestList).then((res) => {
    if (errChunkNum.value === 0) {
      uploadMerge(pgkName.value, fileChunkList.value.length)
    } else {
      ElMessage({
        message: t('install.uploadFail'),
        type: 'error',
      })
    }
  })
}

const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
  fileList.value = files
}
const uploadBefore = () => {
  fileList.value = []
  return true
}
onMounted(() => {
  if (!props.editing) {
    getInstallPath()
  } else {
    formData.envId = props.node?.id
    formData.nodeId = props.node?.hostid
    formData.path = props.node?.path
    formData.port = props.node?.port
    formData.username = props.node?.username
    formData.type = props.node?.type
    let param = props.node?.param
    if (param) {
      let paramJson = JSON.parse(param)
      formData.storageDays = paramJson.storageDays.substring(0, paramJson.storageDays.length - 1)
    }
  }
})
onBeforeUnmount(() => {
  if (ws.instance) ws.instance.close()
})
</script>
<style lang="scss" scoped>
@import '../../../assets/style/style1.scss';
.upload-icon {
  margin-left: 5px;
  font-size: 13px;
}

.suggest-info {
  margin-bottom: 10px;
}
</style>
