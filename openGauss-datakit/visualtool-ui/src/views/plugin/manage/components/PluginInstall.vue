<template>
  <div>
    <el-dialog
      :title="$t('manage.index.5m5v55wxk140')"
      :ok-loading="winLoading"
      v-model="visible"
      width="500px"
      :style="{ height: 'auto' }"
      title-align="start"
      @submit="startInstall" @cancel="closeInstall" @finish="closeInstall"
    >
      <el-form :model="data" :rules="formRules" ref="formRef" label-width="130px">
        <el-form-item v-if="showDownloadArea === false" :label="$t('manage.PluginInstall.pluginName')"
                      prop="pluginName">
          <el-select v-model="data.pluginName" filterable :option="unloadedPluginIds" @change="getPluginUrl">
            <el-option v-for="item in unloadedPluginIds" :key="item" :value="item"
                       :label="unloadedPluginName.get(item) + '/' + item">
              {{ unloadedPluginName.get(item) }}/{{ item }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="showDownloadArea === false" :label="$t('manage.PluginInstall.onLineURL')">
          <el-input type="textarea" v-model="data.pluginUrl" :maxlength="1000" disabled/>
          <el-row>
            <el-text>{{ $t('manage.PluginInstall.onlineMsg') }}
              <el-button text type="primary" @click="showDownloadArea = true">
                {{ $t('manage.PluginInstall.offlineUpload') }}
              </el-button>
            </el-text>
          </el-row>
        </el-form-item>
        <el-form-item v-if="showDownloadArea === true" prop="file" label-width="110">
          <el-text type="danger" v-if="uploadErrMsg !== ''">{{ uploadErrMsg }}</el-text>
          <file-upload :percentage="progressPercent" @changeFile="changeFile" accept=".jar" size-limit="300"
                       :tips="$t('manage.PluginInstall.uploadLimit')"></file-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="modal-footer">
          <el-button :loading="winLoading" @click="startInstall" type="primary">{{ $t('manage.index.5m5v55wxlgg0') }}
          </el-button>
          <el-button @click="closeInstall">{{ $t('manage.index.5m5v55wxldg0') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {getUserInfo} from '@/api/user'
import {onMounted, ref, watch, computed} from 'vue'
import {downloadPluginWs, getUnloadpluginUrl, unloadPluginsInfo} from '@/api/plugin'
import {KeyValue} from '@antv/x6/lib/types'
import FileUpload from '@/components/file-upload'
import axios from 'axios'
import showMessage from '@/hooks/showMessage'
import {useI18n} from 'vue-i18n'

const {t} = useI18n();

const data = ref({
  pluginName: '',
  pluginUrl: '',
  type: ''
})
const formRef = ref()
const formRules = computed(() => {
  return {
    pluginName: [{required: (showDownloadArea.value === false), trigger: ['blur', 'change'], message: '请选择插件名称'}]
  }
})

const winLoading = ref<boolean>(false)

let fileList = {}
const showDownloadArea = ref<boolean>(false)
const progressPercent = ref<number>()

const handleBeforeUpload = () => {
  return new Promise((resolve, reject) => {
    getUserInfo().then(() => {
      resolve(true)
    }).catch(() => {
      reject(false)
    })
  })
}

const props = defineProps<{
  visible: boolean;
  wsId: string
}>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'finish'): void;
  (e: 'downloadStart', pluginName: string): void;
}>()

const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const changeFile = (file: any) => {
  uploadErrMsg.value = ''
  fileList = file
  progressPercent.value = 0
}

const uploadErrMsg = ref<string>()
const startInstall = async () => {
  let isValid = await formRef.value.validate()
  let getUserInfo = await handleBeforeUpload()
  if (!isValid || !getUserInfo) {
    showMessage('error', t('transcribe.index.userMsgErr'))
  } else {
    if (showDownloadArea.value === true) {
      if (progressPercent.value === 0) {
        const formData = new FormData
        formData.append('file', fileList.raw)
        axios({
          url: `/system/plugins/offline_install`,
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
        }).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            closeInstall()
          }
        }).catch((error) => {
          uploadErrMsg.value = t('manage.PluginInstall.uploadErrAlert')
          console.log(error)
        })
      }
    } else {
      const params = {
        pluginId: data.value.pluginName,
        pluginUrl: data.value.pluginUrl,
        wsBusinessId: wsBusinessId.value
      }
      visible.value = false
      emit('downloadStart', data.value.pluginName)
      downloadPluginWs(params).catch((error) => {
        emit('downloadStart', 'ERROR')
        console.log(error)
      }).finally(() => {
        emit('downloadStart', 'END')
      })
    }
  }
}

const closeInstall = () => {
  fileList = {}
  emit('update:visible', false)
}

const getPluginUrl = () => {
  if (data.value.pluginName !== '') {
    getUnloadpluginUrl(data.value.pluginName).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        data.value.pluginUrl = res.msg
      }
    }).catch(error => {
      console.log(error)
    })
  }
}

const unloadedPluginTra = new Map<string, string>([
  ['observability-sql-diagnosis', 'manage.PluginInstall.observabilitySqlDiagnosis'],
  ['observability-instance', 'manage.PluginInstall.observabilityInstance'],
  ['monitor-tools', 'manage.PluginInstall.monitorTools'],
  ['oauth-login', 'manage.PluginInstall.oauthLogin'],
  ['alert-monitor', 'manage.PluginInstall.alertMonitor'],
  ['MetaTune', 'manage.PluginInstall.MetaTune'],
  //['container-management-plugin', 'manage.PluginInstall.containerManagement'],
  ['compatibility-assessment', 'manage.PluginInstall.compatibilityAssessment'],
  ['observability-log-search', 'manage.PluginInstall.observabilityLogSearch'],
  ['data-migration', 'manage.PluginInstall.dataMigration'],
  ['base-ops', 'manage.PluginInstall.baseOps'],
  ['webds-plugin', 'manage.PluginInstall.webdsPlugin']
])
const unloadedPluginName = computed(() => {
  return new Map(
    Array.from(unloadedPluginTra).map(([id, i18nKey]) => [id, t(i18nKey)])
  );
});
const unloadedPluginIds = ref([])
const getPluginInfo = () => {
  unloadPluginsInfo().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      unloadedPluginIds.value = []
      res.data.forEach((item: string) => {
        unloadedPluginIds.value.push(item)
      })
    }
  }).catch((error) => {
    console.log(error)
  })
}

const wsBusinessId = ref<string>()

const init = () => {
  formRef.value?.resetFields()
  uploadErrMsg.value = ''
  showDownloadArea.value = false
  wsBusinessId.value = props.wsId
  progressPercent.value = 0
  fileList = {}
  data.value.pluginName = ''
  data.value.pluginUrl = 'https://opengauss.obs.cn-south-q.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/xxx-x.0.0-repackage.tar.gz'
  getPluginInfo()
}

watch(
  () => visible.value,
  (newVal) => {
    if (newVal) {
      init()
    }
  }
)

onMounted(() => {
  if (visible.value) {
    init()
  }
})

</script>

<style scoped lang="scss">
:deep(.el-dialog) {
  --o-dialog-body-max-height: 600px
}

</style>
