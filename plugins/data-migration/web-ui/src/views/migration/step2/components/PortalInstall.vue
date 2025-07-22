<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :title="$t('components.PortalInstall.5q0aajl75f00')"
    class="add-portal-modal"
    width="40%"
  >
    <template #header>
      <div class="dialog-header">
        <span>{{ $t('components.PortalInstall.5q0aajl75f00') }}</span>
        <el-popover
          :title="$t('task.index.portalInstallDesc')"
          placement="top"
          trigger="hover"
          width="500"
        >
          <template #reference>
            <el-link :icon="InfoFilled" type="primary">
              {{ $t('components.PortalInstall.installDesc') }}
            </el-link>
          </template>
          <div>
            <p>
              <b>{{ $t('components.PortalInstall.suiteInfoTitle') }}</b>
              {{ $t('components.PortalInstall.suiteInfoContent') }}
            </p>
            <p>
              <b>{{ $t('components.PortalInstall.systemInfoTitle') }}</b>
              {{ $t('components.PortalInstall.systemInfoContent') }}
            </p>
            <p>
              <b>{{ $t('components.PortalInstall.softwareTitle') }}</b>
              {{ $t('components.PortalInstall.softwareContent') }}
            </p>
            <p>
              <b>{{ $t('components.PortalInstall.dependenceTitle') }}</b>
              {{ $t('components.PortalInstall.dependenceContent') }}
            </p>
            <p>
              <b>{{ $t('components.PortalInstall.sudoTitle') }}</b>
              {{ $t('components.PortalInstall.sudoContent') }}
            </p>
          </div>
        </el-popover>
      </div>
    </template>

    <el-skeleton :loading="loading">
      <el-form ref="formRef" :model="form" label-width="170" class="page-input-size">
        <el-form-item
          :label="$t('components.PortalInstall.5q0aajl76580')"
          :rules="[
          {
            required: true,
            message: $t('components.PortalInstall.5q0aajl76io0'),
          },
        ]"
          prop="hostUserId"
        >
          <el-select
            v-model="form.hostUserId"
            :placeholder="$t('components.PortalInstall.5q0aajl76ug0')"
          >
            <el-option
              v-for="item in hostUserData"
              :key="item.hostUserId"
              :label="item.username"
              :value="item.hostUserId"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-alert :closable="false">
            {{ $t('components.PortalInstall.tempSudoRemind') }}
          </el-alert>
        </el-form-item>

        <el-form-item
          :label="$t('components.PortalInstall.5q0aajl76xw0')"
          prop="installPath"
          :rules="[{
            required: true,
            message: $t('transcribe.create.required'),
          },
          {
            validator: validateInstallPath,
            trigger: 'blur'
          }]"
        >
          <el-input
            maxlength="200"
            v-model.trim="form.installPath"
            :placeholder="$t('components.PortalInstall.5q0aajl77f40')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('step2.compoents.portalInstall.portalType')"
          prop="portalType"
          @change="changePortalType"
        >
          <el-radio-group v-model="form.portalType">
            <el-radio-button label="MULTI_DB">
              PostgreSQL
            </el-radio-button>
            <el-radio-button label="MYSQL_ONLY">
              MySQL
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          :label="$t('components.PortalInstall.5q0aajl77lg33')"
          prop="installType"
          @change="changeInstallType"
        >
          <el-radio-group v-model="form.installType">
            <el-radio-button :label="INSTALL_TYPE.ONLINE">
              {{ $t('components.PortalInstall.5q0aajl77lg1') }}
            </el-radio-button>
            <el-radio-button :label="INSTALL_TYPE.OFFLINE">
              {{ $t('components.PortalInstall.5q0aajl77lg2') }}
            </el-radio-button>
            <el-radio-button :label="INSTALL_TYPE.IMPORTINSTALL">
              {{ $t('components.PortalInstall.5q0aajl77lg15') }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.installType === INSTALL_TYPE.ONLINE">
          <el-form-item
            :label="$t('step2.compoents.portalInstall.installType')"
            prop="pkgDownloadUrl"
          >
            <el-radio-group v-model="form.portalVersion">
              <el-radio-button label="STABLE" v-if="versionTypeNum === 'both' || versionTypeNum === 'STABLE'">
                {{ $t('step2.compoents.portalInstall.stable') }}
              </el-radio-button>
              <el-radio-button label="EXPERIMENTAL"  v-if="versionTypeNum === 'both' || versionTypeNum === 'EXPERIMENTAL'">
                {{ $t('step2.compoents.portalInstall.trial') }}
              </el-radio-button>
              <el-text v-if="versionTypeNum === 'none'">{{ $t('step2.compoents.portalInstall.versionContent') }}</el-text>
            </el-radio-group>
          </el-form-item>
        </template>

        <template v-if="form.installType === INSTALL_TYPE.OFFLINE">
          <el-form-item
            :label="$t('components.PortalInstall.5q0aajl77lg9')"
            :rules="[{
              required: form.installType === INSTALL_TYPE.OFFLINE,
              validator: validUpload,
            },]"
            prop="packagePath"
            class="upload-container"
          >
            <el-upload
              ref="uploadRef"
              v-model:file-list="upload.fileList"
              :auto-upload="false"
              :limit="1"
              accept=".gz"
              drag
              @before-remove="handleBeforeRemove"
            >
              <el-icon class="el-icon--upload" :size="40">
                <Plus/>
              </el-icon>
              <div class="el-upload__text">
                {{ $t('components.PortalInstall.5q0aajl77lg10') }}
              </div>
            </el-upload>
          </el-form-item>
        </template>

        <template v-if="form.installType !== INSTALL_TYPE.IMPORTINSTALL && isMysqlPortaltype === true">
          <el-form-item :label="$t('components.PortalInstall.5q0aajl77lg16')"
                        :rules="[{
                          required: form.installType !== INSTALL_TYPE.IMPORTINSTALL && isMysqlPortaltype === true,
                          message: $t('transcribe.create.required'),
                        }]"
          >
            <el-input-number
              min="1" max="65535"
              v-model="thirdPartyParam.zookeeperPort"
              :placeholder="$t('components.PortalInstall.5q0aajl77lg18')"
            />
          </el-form-item>
          <el-form-item :label="$t('components.PortalInstall.5q0aajl77lg17')"
                        :rules="[{
                          required: form.installType !== INSTALL_TYPE.IMPORTINSTALL && isMysqlPortaltype === true,
                          message: $t('transcribe.create.required'),
                        }]"
          >
            <el-input-number
              min="1" max="65535"
              v-model="thirdPartyParam.kafkaPort"
              :placeholder="$t('components.PortalInstall.5q0aajl77lg19')"
            />
          </el-form-item>
          <el-form-item :label="$t('components.PortalInstall.5q0aajl77lg30')"
                        :rules="[{
                          required: form.installType !== INSTALL_TYPE.IMPORTINSTALL && isMysqlPortaltype === true,
                          message: $t('transcribe.create.required'),
                        }]"
          >
            <el-input-number
              min="1" max="65535"
              v-model="thirdPartyParam.schemaRegistryPort"
              :placeholder="$t('components.PortalInstall.5q0aajl77lg31')"
            />
          </el-form-item>
          <el-form-item :label="$t('components.PortalInstall.5q0aajl77lg27')"
                        :rules="[{
                          required: form.installType !== INSTALL_TYPE.IMPORTINSTALL && isMysqlPortaltype === true,
                          message: $t('transcribe.create.required'),
                        },
                        {
                          validator: validateInstallPath,
                          trigger: 'blur'
                        }]"
          >
            <el-input
              maxlength="255"
              v-model.trim="thirdPartyParam.kafkaInstallDir"
              :placeholder="$t('components.PortalInstall.5q0aajl77lg29')"
            />
          </el-form-item>
        </template>
      </el-form>
    </el-skeleton>

    <template #footer>
      <div class="dialog-footer">
        <div class="footer-left">
          <el-button v-if="!loading" @click="cancel">
            {{ $t('components.PortalInstall.5q0aajl77ik0') }}
          </el-button>
          <el-popconfirm
            :title="$t('components.PortalInstall.sudoRemind')"
            @confirm="checkPermission"
          >
            <template #reference>
              <el-button
                :disabled="!form.hostUserId"
                :loading="checkLoading"
                class="ml-16"
              >
                {{ $t('components.PortalInstall.permissionCheck') }}
              </el-button>
            </template>
          </el-popconfirm>
          <el-button
            :disabled="loading"
            :loading="loading"
            class="ml-16"
            type="primary"
            @click="confirmSubmit"
          >
            {{ $t('components.PortalInstall.5q0aajl77lg0') }}
          </el-button>
        </div>
        <div v-if="showPermission" class="footer-right">
          {{ $t('components.PortalInstall.checkResult') }}
          <el-tag
            :loading="checkLoading"
            :type="permissionStatus ? 'success' : 'danger'"
          >
            {{
              permissionStatus
                ? $t('components.PortalInstall.pass')
                : $t('components.PortalInstall.fail')
            }}
          </el-tag>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {onMounted, reactive, ref, watch} from 'vue'
import { deletePortal, getInstallType, hasRootPermission, hostUsers, installPortal, reInstallPortal } from '@/api/task'
import {getPortalDownloadInfoList} from '@/api/common'
import {INSTALL_TYPE, KAFKA_CONFIG_TYPE} from '@/utils/constants'
import {useI18n} from 'vue-i18n'
import {Modal} from '@arco-design/web-vue'
import {Plus} from '@element-plus/icons-vue'
import showMessage from "@/utils/showMessage";
const {t} = useI18n()

const props = defineProps({
  open: Boolean,
  hostId: String,
  mode: String,
  installInfo: Object
})

const emits = defineEmits(['update:open', 'startInstall'])

const formRef = ref()
const form = reactive({
  hostUserId: undefined,
  installPath: '~',
  installType: INSTALL_TYPE.ONLINE,
  portalVersion: '',
  pkgDownloadUrl: '',
  pkgName: '',
  jarName: 'portalControl-1.0-SNAPSHOT-exec.jar',
  pkgUploadPath: {}
})

const validateInstallPath = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (!/^[~\/]/.test(value)) {
    callback(new Error(t('transcribe.create.formaterror')))
    return
  }
  if (/\/{2,}/.test(value)) {
    callback(new Error(t('transcribe.create.formaterror')))
    return
  }
  callback()
}

const packageInfos = reactive({
  value: []
})

const thirdPartyParam = reactive({
  zookeeperPort: '2181',
  kafkaPort: '9092',
  schemaRegistryPort: '8081',
  kafkaInstallDir: form.installPath + KAFKA_CONFIG_TYPE.INSTALL_DIR_DEFAULT
})

const upload = reactive({
  fileList: []
})

const visible = ref(false)
const loading = ref(false)
const hostUserData = ref([])
const kafkaInstanceData = ref([])

watch(() => form.installPath, (v) => {
  thirdPartyParam.kafkaInstallDir = v + KAFKA_CONFIG_TYPE.INSTALL_DIR_DEFAULT
})

watch(visible, (v) => {
  emits('update:open', v)
})

watch(
  () => props.open,
  (v) => {
    if (v) {
      loading.value = true
      form.portalType = props.installInfo?.portalType?props.installInfo?.portalType: 'MYSQL_ONLY'
      isMysqlPortaltype.value = form.portalType === 'MYSQL_ONLY'? true : false
      if (props.installInfo === null) {
        thirdPartyParam.zookeeperPort = '2181'
        thirdPartyParam.kafkaPort = '9092'
        thirdPartyParam.schemaRegistryPort = '8081'
        thirdPartyParam.kafkaInstallDir = form.installPath + KAFKA_CONFIG_TYPE.INSTALL_DIR_DEFAULT
      }
      getHostUsers()
      getVersionType(form.portalType)
    } else {
      form.hostUserId = ''
      form.installPath = ''
      form.installType = INSTALL_TYPE.ONLINE
      form.pkgDownloadUrl = ''
      form.pkgName = ''
      form.pkgUploadPath = {}
      packageInfos.value = []
      upload.fileList = []
      showPermission.value = false
      isMysqlPortaltype.value = form.portalType === 'MYSQL_ONLY'? true : false
    }
    visible.value = v
  }
)

watch(
  () => form.pkgName,
  (v) => {
    let pkgInfos = packageInfos.value
    for (let i = 0; i < pkgInfos.length; i++) {
      if (pkgInfos[i].portalPkgName === form.pkgName) {
        form.pkgDownloadUrl = pkgInfos[i].portalPkgDownloadUrl
      }
    }
  }
)

const isMysqlPortaltype = ref(true)
const changePortalType =() => {
  form.portalType === 'MULTI_DB'? isMysqlPortaltype.value = false: isMysqlPortaltype.value = true
  getVersionType(form.portalType)
}

const changeInstallType =() => {
  if (form.installType === INSTALL_TYPE.ONLINE) {
    getVersionType(form.portalType)
  } else {
    versionTypeNum.value = 'none'
    form.portalVersion = ''
  }
}

const getHostUsers = () => {
  hostUsers(props.hostId).then((res) => {
    hostUserData.value = res.data.length > 0 ? res.data : []
    if (props.mode === 'reinstall') {
      form.hostUserId = props.installInfo?.hostUserId
      form.installType = props.installInfo?.installType
      form.installPath = props.installInfo?.installPath
      form.pkgUploadPath = props.installInfo?.pkgUploadPath
      if (form.pkgUploadPath?.realPath) {
        upload.fileList = []
      }
    } else {
      form.hostUserId = ''
      form.installPath = '~'
    }
    form.pkgDownloadUrl = props.installInfo?.pkgDownloadUrl || form.pkgDownloadUrl
    form.pkgName = props.installInfo?.pkgName || form.pkgName
  }) .catch((error) => {
    console.log(error)
  }) .finally(() => {
    loading.value = false
  })
}

const showPermission = ref(false)
const checkLoading = ref(false)
const permissionStatus = ref('')
const checkPermission = async () => {
  try {
    checkLoading.value = true
    showPermission.value = true
    const res = await hasRootPermission(form.hostUserId)
    checkLoading.value = false
    if (res.code === 200) {
      permissionStatus.value = res.data
    }
  } catch (error) {
    checkLoading.value = false
  }
}

const cancel = () => {
  visible.value = false
}

const confirmSubmit = () => {
  const onloneTypeNull = versionTypeNum.value === 'none' && form.installType === INSTALL_TYPE.ONLINE? true: false
  formRef.value?.validate(async (valid) => {
    if (valid && !onloneTypeNull) {
      const params = await buildInstallReq()
      loading.value = true
      let method = props.mode === 'install' ? installPortal : reInstallPortal
      method(props.hostId, params)
        .then(() => {
          emits('startInstall', [props.hostId])
          visible.value = false
          loading.value = false
        })
        .catch(() => {
          loading.value = false
        })
    } else {
      if (onloneTypeNull) {
        showMessage('error', '在线下载版本无法提供，请通过其他方式安装')
      }
    }
  })
}

const buildInstallReq = () => {
  const params = new FormData()
  params.append('runHostId', props.hostId)
  params.append('installPath', form.installPath)
  params.append('hostUserId', form.hostUserId)
  params.append('installType', form.installType)
  params.append('pkgDownloadUrl', form.pkgDownloadUrl)
  params.append('jarName', form.jarName)
  params.append('portalVersion', form.portalVersion)
  params.append('thirdPartySoftwareConfig.thirdPartySoftwareConfigType', KAFKA_CONFIG_TYPE.INSTALL)
  params.append('thirdPartySoftwareConfig.zookeeperPort', thirdPartyParam.zookeeperPort)
  params.append('thirdPartySoftwareConfig.kafkaPort', thirdPartyParam.kafkaPort)
  params.append('thirdPartySoftwareConfig.installDir', thirdPartyParam.kafkaInstallDir)
  params.append('thirdPartySoftwareConfig.schemaRegistryPort', thirdPartyParam.schemaRegistryPort)

  params.append('portalType', form.portalType)
  let uploadPath = form.pkgUploadPath
  if (form.installType === INSTALL_TYPE.ONLINE) {
    uploadPath = {}
    params.append('pkgName', form.pkgName)
  }
  params.append('pkgUploadPath', JSON.stringify(uploadPath))
  if (upload.fileList.length > 0 && form.installType === INSTALL_TYPE.OFFLINE) {
    const file = upload.fileList[0]
    if (form.installType === INSTALL_TYPE.OFFLINE) {
      params.append('file', file.raw)
      params.append('pkgName', file.name)
    }
  }
  return params
}

const handleBeforeRemove = (file) => {
  return new Promise((resolve, reject) => {
    if (file.status === 'done') {
      Modal.confirm({
        title: t('components.PortalInstall.5q0aajl77lg11'),
        content: t('components.PortalInstall.5q0aajl77lg12'),
        onOk: () => {
          deletePortal(props.hostId, true).then(() => {
            form.pkgUploadPath = {}
            emits(`pkgDeleted`)
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

const validUpload = (rule, val, cb) => {
  if (upload.fileList.length <= 0) {
    cb(new Error(t('components.PortalInstall.5q0aajl77lg13')))
  } else {
    cb()
  }
}
const versionTypeNum = ref('')
const getVersionType = (installType) => {
  const hostId = props.hostId
  getInstallType(hostId, installType) .then((res) => {
    if(Number(res.code) === 200) {
      const hasStable = res.data.includes("STABLE")
      const hasExperimental = res.data.includes("EXPERIMENTAL")
      console.log('1', hasStable, hasExperimental)
      if (hasStable && hasExperimental) {
        versionTypeNum.value = 'both'
        form.portalVersion = 'STABLE'
      } else if (hasStable) {
        versionTypeNum.value = 'STABLE'
        form.portalVersion = 'STABLE'
      } else if (hasExperimental) {
        versionTypeNum.value = 'EXPERIMENTAL'
        form.portalVersion = 'EXPERIMENTAL'
      } else {
        versionTypeNum.value = 'none'
        form.portalVersion = ''
      }
    } else {
      versionTypeNum.value = 'none'
      form.portalVersion = ''
    }
    console.log(res)
    console.log('form.portalVersion', form.portalVersion)
    console.log('versionTypeNum.value', versionTypeNum.value)
  }) .catch((error) => {
    versionTypeNum.value = 'none'
    form.portalVersion = ''
    console.log(error)
  })
}

onMounted(() => {
  visible.value = props.open
})

</script>

<style lang="less" scoped>
.add-portal-modal {
  .modal-footer {
    display: flex;
    justify-content: space-between;
    flex-direction: row-reverse;

    .ml-16 {
      margin-left: 16px;
    }
  }
}

.upload-info {
  background: var(--color-fill-2);
  border: 1px dotted var(--color-fill-4);
  height: 120px;
  width: 100%;
  border-radius: 2px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .tips-1 {
    align-items: center;
    font-size: 14px;
    color: var(--color-text-2);
    margin-bottom: 12px;

    .highlight {
      color: rgb(var(--primary-6));
    }
  }
}

:deep(.arco-upload-progress) {
  display: none;
}

.upload-container :deep(.el-upload),
.upload-container :deep(.el-upload--text),
.upload-container :deep(.is-drag),
.upload-container :deep(.el-upload-dragger){
  width: 400px !important;
}

.page-input-size :deep(.el-form-item .el-input),
.page-input-size :deep(.el-form-item .el-select),
.page-input-size :deep(.el-form-item .el-input-number){
  width: 450px !important;
}

</style>

