<template>
  <div class="dialog">
    <el-dialog
      :width="dialogWith"
      height="200px"
      :title="editing ? t('install.editAgent') : t('install.installAgent')"
      v-model="visible"
      :close-on-click-modal="false"
      draggable
      @close="closeDialog"
    >
      <div class="dialog-content" v-show="installData.length != 0" style="padding-bottom: 0px">
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
        <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef" label-width="130px">
          <el-form-item :label="t('install.collectInstance')" prop="nodeIds">
            <ClusterCascader
              ref="nodeRef"
              width="300"
              multiple
              instanceValueKey="nodeId"
              @getCluster="handleClusterValue"
              notClearable
              @loaded="getClusterList"
            />
          </el-form-item>
          <el-form-item :label="t('install.proxyMachine')" prop="hostId">
            <Machines
              ref="machineRef"
              :disabled="editing"
              width="300"
              @change="changeMachine"
              :autoSelectFirst="!editing"
              notClearable
              style="width: 300px; margin: 0 4px"
            />
          </el-form-item>
          <el-form-item :label="t('install.installUser')" prop="username">
            <el-select v-model="formData.username" style="width: 300px; margin: 0 4px" :disabled="editing">
              <el-option
                v-for="item in hostUserList"
                :key="item.hostUserId"
                :label="item.username"
                :value="item.username"
                :disabled="item.username === 'root'"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('install.httpPort')" prop="httpPort">
            <el-input-number
              @click.stop="() => {}"
              class="left-align"
              style="width: 300px; margin: 0 4px"
              v-model="formData.httpPort"
              :disabled="editing"
              :min="0"
              :max="65535"
              :controls="false"
            >
            </el-input-number>
          </el-form-item>
          <el-form-item :label="t('install.installPath')" prop="path">
            <el-input v-model="formData.path" style="width: 300px; margin: 0 4px" :disabled="editing" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div v-if="installData.length === 0">
          <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
          <el-button :loading="started" style="padding: 5px 20px" type="primary" @click="install">
            {{ $t('install.install') }}
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
  </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import { FormRules, FormInstance, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import WebSocketClass from '@/utils/websocket'
import moment from 'moment'
import restRequest from '@/request/restful'
import { uuid } from '@/shared'
const { t } = useI18n()

const visible = ref(false)
const props = withDefaults(
  defineProps<{
    show: boolean
    editing?: boolean
    node?: any
    delNodeId?: String
  }>(),
  {}
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
  nodeIds: [],
  envId: '',
  hostId: '',
  username: '',
  port: '9090',
  httpPort: 9596,
  installMode: 'online',
  path: '',
  nodepkg: '',
  nodesrc: '',
  nodeurl: '',
  gausspkg: '',
  gausssrc: '',
  gaussurl: '',
  fileList: [],
  uploadPath: '',
}
const formData = reactive(cloneDeep(initFormData))
const connectionFormRef = ref<FormInstance>()
const connectionFormRules = reactive<FormRules>({
  hostId: [{ required: true, message: t('install.collectorRules[5]'), trigger: 'blur' }],
  username: [{ required: true, message: t('install.collectorRules[6]'), trigger: 'blur' }],
  nodeIds: [{ required: true, message: t('install.collectorRules[0]'), trigger: 'blur' }],
  httpPort: [{ required: true, message: t('install.collectorRules[3]'), trigger: 'blur' }],
  path: [{ required: true, message: t('install.collectorRules[4]'), trigger: 'blur' }],
})

// cluster component
const handleClusterValue = (val: any) => {
  let nodeIds: never[] = []
  val.forEach((element: never) => {
    nodeIds.push(element[1])
  })
  formData.nodeIds = nodeIds
}
const clusterList = ref<any[]>()
const getClusterList = (val: any) => {
  clusterList.value = val
}

const hostUserList = ref<any[]>()
const changeMachine = (val: any) => {
  formData.hostId = val
  if (formData.hostId) {
    getHostUserList(formData.hostId)
  }
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

const started = ref(false)
const installSucceed = ref(false)
const ws = reactive({
  name: '',
  webUser: '',
  connectionName: '',
  sessionId: '',
  instance: null,
})
const install = async () => {
  let result = await connectionFormRef.value?.validate()
  if (!result) return
  started.value = true
  // Determine whether the server is installed
  restRequest
    .get('/observability/v1/environment/prometheus', '')
    .then((res) => {
      if (!res || res.length === 0) {
        ElMessage({
          showClose: true,
          message: t('install.installServerAlert'),
          type: 'warning',
        })
        started.value = false
      } else {
        ws.name = moment(new Date()).format('YYYYMMDDHHmmss') as string // websocket connection name
        ws.sessionId = moment(new Date()).format('YYYYMMDDHHmmss') as string // websocket connection id
        ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage)
        sendData()
      }
    })
    .catch(() => {
      started.value = false
    })
}
const sendData = async () => {
  const sendData = {
    key: 'exporter',
    envId: formData.envId,
    nodeIds: formData.nodeIds,
    hostId: formData.hostId,
    username: formData.username,
    path: formData.path,
    httpPort: formData.httpPort,
    language: localStorage.getItem('locale') === 'en-US' ? 'en_US' : 'zh_CN',
  }
  ws.instance.send(sendData)
}
const onWebSocketMessage = (data: any) => {
  // if (Array.isArray(installData.value)) installData.value = JSON.parse(data);
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
  dialogWith.value = '500px'
  ws.instance.close()
  installData.value = []
}

// list Data
const installData = ref<Array<any>>([])
const dialogWith = ref<string>('500px')
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

const basePath = ref<string>('')
const getInstallPath = () => {
  restRequest.get(`/observability/v1/environment/basePath`).then((res) => {
    if (res) {
      basePath.value = res + (res.endsWith('/') ? 'data' : '/data')
      formData.path = basePath.value + '/agent/' + uuid()
    }
  })
}

const machineRef = ref<any>(null)
const nodeRef = ref<any>(null)
onMounted(() => {
  if (!props.editing) getInstallPath()
  else {
    formData.envId = props.node?.id
    formData.path = props.node?.path
    formData.httpPort = props.node?.exporterPort
    formData.username = props.node?.username
    nextTick(() => {
      machineRef.value.setHostId(props.node?.hostId)
      let clusterAndNodeIds: any[] = []
      props.node?.children.forEach((element: any) => {
        element.children.forEach((node: any) => {
          if (node.nodeId === props.delNodeId) return
          let clusterAndNodeId = []
          clusterAndNodeId.push(element.clusterId)
          clusterAndNodeId.push(node.nodeId)
          clusterAndNodeIds.push(clusterAndNodeId)
        })
      })
      console.log('clusterAndNodeIds', clusterAndNodeIds)
      nodeRef.value.setNodeIds(clusterAndNodeIds)
    })
  }
})

onBeforeUnmount(() => {
  if (ws.instance) ws.instance.close()
})
</script>
<style lang="scss" scoped>
@import '@/assets/style/style1.scss';
.upload-icon {
  margin-left: 5px;
  font-size: 13px;
}

.suggest-info {
  margin-bottom: 10px;
}
</style>
