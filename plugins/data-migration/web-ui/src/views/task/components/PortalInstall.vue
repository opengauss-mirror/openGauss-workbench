<template>
  <a-modal :title="$t('components.PortalInstall.5q0aajl75f00')" v-model:visible="visible" width="40vw"
    modal-class="add-portal-modal" :mask-closable="false" :esc-to-close="false">
    <template #title>
      <div>{{ $t('components.PortalInstall.5q0aajl75f00') }}</div>
      <a-popover :title="$t('task.index.portalInstallDesc')">
        <a-link>
          <template #icon>
            <icon-info-circle-fill />
          </template>
         {{ $t('components.PortalInstall.installDesc')}} 
          </a-link>
        <template #content>
          <p>
            <b>{{ $t('components.PortalInstall.suiteInfoTitle') }}</b>{{
              $t('components.PortalInstall.suiteInfoContent') }}
          </p>
          <p>
            <b>{{ $t('components.PortalInstall.systemInfoTitle') }}</b>{{
              $t('components.PortalInstall.systemInfoContent') }}
          </p>
          <p>
            <b>{{ $t('components.PortalInstall.softwareTitle') }}</b>{{
              $t('components.PortalInstall.softwareContent') }}
          </p>
          <p>
            <b>{{ $t('components.PortalInstall.dependenceTitle') }}</b>{{
              $t('components.PortalInstall.dependenceContent') }}
          </p>
          <p>
            <b>{{ $t('components.PortalInstall.sudoTitle') }}</b>{{ $t('components.PortalInstall.sudoContent')
            }}
          </p>
        </template>
      </a-popover>
    </template>
    <a-spin :loading="loading" style="display: block">
      <a-form ref="formRef" :model="form" auto-label-width>
        <a-form-item field="hostUserId" :label="$t('components.PortalInstall.5q0aajl76580')" :rules="[
          {
            required: true,
            message: $t('components.PortalInstall.5q0aajl76io0'),
          },
        ]">
          <a-select v-model="form.hostUserId" :placeholder="$t('components.PortalInstall.5q0aajl76ug0')">
            <a-option v-for="item in hostUserData" :key="item.hostUserId" :value="item.hostUserId">{{ item.username
            }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-alert>{{ $t('components.PortalInstall.tempSudoRemind') }}</a-alert>
        </a-form-item>
        <a-form-item field="installPath" :label="$t('components.PortalInstall.5q0aajl76xw0')">
          <a-input v-model="form.installPath" :placeholder="$t('components.PortalInstall.5q0aajl77f40')" />
        </a-form-item>
        <a-form-item field="installType" :label="$t('components.PortalInstall.5q0aajl77lg33')">
          <a-radio-group type="button" v-model="form.installType">
            <a-radio :value="INSTALL_TYPE.ONLINE">{{
              $t('components.PortalInstall.5q0aajl77lg1')
            }}</a-radio>
            <a-radio :value="INSTALL_TYPE.OFFLINE">{{
              $t('components.PortalInstall.5q0aajl77lg2')
            }}</a-radio>
            <a-radio :value="INSTALL_TYPE.IMPORTINSTALL">{{
              $t('components.PortalInstall.5q0aajl77lg15')
            }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <template v-if="form.installType === INSTALL_TYPE.ONLINE">
          <a-form-item v-if="false" field="pkgDownloadUrl" :label="$t('components.PortalInstall.5q0aajl77lg3')" :rules="[
            {
              required: true,
              message: $t('components.PortalInstall.5q0aajl77lg4'),
            },
          ]">
            <a-input v-model="form.pkgDownloadUrl"></a-input>
          </a-form-item>
          <a-form-item field="pkgName" :label="$t('components.PortalInstall.5q0aajl77lg5')" :rules="[
            {
              required: true,
              message: $t('components.PortalInstall.5q0aajl77lg6'),
            },
          ]">
            <a-select v-model="form.pkgName">
              <a-option v-for="packageInfo in packageInfos.value" :value="packageInfo.portalPkgName">{{
                packageInfo.portalPkgName }}</a-option>
            </a-select>
          </a-form-item>
        </template>
        <template v-if="form.installType != INSTALL_TYPE.IMPORTINSTALL">
          <a-form-item v-if="false" field="jarName" :label="$t('components.PortalInstall.5q0aajl77lg7')" :rules="[
            {
              required: true,
              message: $t('components.PortalInstall.5q0aajl77lg8'),
            },
          ]">
            <a-input v-model="form.jarName"></a-input>
          </a-form-item>
        </template>
        <template v-if="form.installType === INSTALL_TYPE.OFFLINE">
          <a-form-item field="packagePath" :label="$t('components.PortalInstall.5q0aajl77lg9')" :rules="[
            {
              required: true,
              validator: validUpload,
            },
          ]">
            <a-upload ref="uploadRef" v-model:file-list="upload.fileList" :limit="1" :show-file-list="true"
              :auto-upload="false" draggable accept=".gz" @before-remove="handleBeforeRemove" :show-retry-button="false"
              :show-cancel-button="false">
              <template #upload-button>
                <div class="upload-info">
                  <div class="upload-icon">
                    <icon-plus :style="{ fontSize: '48px', color: '#86909C' }" />
                  </div>
                  <div class="tips-1">
                    <span>{{
                      $t('components.PortalInstall.5q0aajl77lg10')
                    }}</span>
                  </div>
                </div>
              </template>
            </a-upload>
          </a-form-item>
        </template>

        <template v-if="form.installType !== INSTALL_TYPE.IMPORTINSTALL">
          <a-form-item field="zookeeperPort" :label="$t('components.PortalInstall.5q0aajl77lg16')">
            <a-input v-model="thirdPartyParam.zookeeperPort"
              :placeholder="$t('components.PortalInstall.5q0aajl77lg18')" />
          </a-form-item>
          <a-form-item field="kafkaPort" :label="$t('components.PortalInstall.5q0aajl77lg17')">
            <a-input v-model="thirdPartyParam.kafkaPort" :placeholder="$t('components.PortalInstall.5q0aajl77lg19')" />
          </a-form-item>
          <a-form-item field="schemaRegistryPort" :label="$t('components.PortalInstall.5q0aajl77lg30')">
            <a-input v-model="thirdPartyParam.schemaRegistryPort"
              :placeholder="$t('components.PortalInstall.5q0aajl77lg31')" />
          </a-form-item>
          <a-form-item field="kafkaInstallDir" :label="$t('components.PortalInstall.5q0aajl77lg27')">
            <a-input v-model="thirdPartyParam.kafkaInstallDir"
              :placeholder="$t('components.PortalInstall.5q0aajl77lg29')" />
          </a-form-item>
        </template>
      </a-form>
    </a-spin>
    <template #footer>
      <div class="modal-footer">
        <div>
          <a-button @click="cancel" v-if="!loading">{{
            $t('components.PortalInstall.5q0aajl77ik0')
          }}</a-button>
          <a-popconfirm :content="$t('components.PortalInstall.sudoRemind')" @ok="checkPermission">
            <a-button :loading="checkLoading" class="ml-16" :disabled="!form.hostUserId">{{
              $t('components.PortalInstall.permissionCheck') }}</a-button>
          </a-popconfirm>
          <a-button type="primary" :disabled="loading" class="ml-16" @click="confirmSubmit" :loading="loading">{{
            $t('components.PortalInstall.5q0aajl77lg0') }}</a-button>
        </div>
        <div v-if="showPermission">{{ $t('components.PortalInstall.checkResult') }}<a-tag :loading="checkLoading"
            :color="permissionStatus ? 'green' : 'red'">{{
              permissionStatus ? $t('components.PortalInstall.pass') : $t('components.PortalInstall.fail') }}</a-tag> </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
import { hostUsers, installPortal, reInstallPortal } from '@/api/task'
import { getPortalDownloadInfoList } from '@/api/common'
import { INSTALL_TYPE, KAFKA_CONFIG_TYPE } from '@/utils/constants'
import { deletePortal, hasRootPermission } from '@/api/task'
import { useI18n } from 'vue-i18n'
import { Modal } from '@arco-design/web-vue'

const { t } = useI18n()

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
  pkgDownloadUrl: '',
  pkgName: '',
  jarName: 'portalControl-1.0-SNAPSHOT-exec.jar',
  pkgUploadPath: {}
})

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
      getPortalDownloadInfoList(props.hostId).then((res) => {
        packageInfos.value = res.data
        getHostUsers()
      })
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

const getHostUsers = () => {
  hostUsers(props.hostId).then((res) => {
    hostUserData.value = res.data.length > 0 ? res.data : []
    if (props.mode === 'reinstall') {
      form.hostUserId = props.installInfo?.hostUserId
      form.installType = props.installInfo?.installType
      form.installPath = props.installInfo?.installPath
      form.pkgUploadPath = props.installInfo?.pkgUploadPath
      if (form.pkgUploadPath?.realPath) {
        upload.fileList = [
          {
            uid: '-1',
            name: form.pkgUploadPath.name,
            url: form.pkgUploadPath.realPath
          }
        ]
      }
    } else {
      form.hostUserId = ''
      form.installPath = '~'
    }
    form.pkgDownloadUrl = props.installInfo?.pkgDownloadUrl || form.pkgDownloadUrl
    form.pkgName = props.installInfo?.pkgName || form.pkgName
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
  formRef.value?.validate((valid) => {
    if (!valid) {
      const params = buildInstallReq()
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

  params.append('thirdPartySoftwareConfig.thirdPartySoftwareConfigType', KAFKA_CONFIG_TYPE.INSTALL)
  params.append('thirdPartySoftwareConfig.zookeeperPort', thirdPartyParam.zookeeperPort)
  params.append('thirdPartySoftwareConfig.kafkaPort', thirdPartyParam.kafkaPort)
  params.append('thirdPartySoftwareConfig.installDir', thirdPartyParam.kafkaInstallDir)
  params.append('thirdPartySoftwareConfig.schemaRegistryPort', thirdPartyParam.schemaRegistryPort)

  let uploadPath = form.pkgUploadPath
  if (form.installType === INSTALL_TYPE.ONLINE) {
    uploadPath = {}
    params.append('pkgName', form.pkgName)
  }
  params.append('pkgUploadPath', JSON.stringify(uploadPath))
  if (upload.fileList.length > 0) {
    const file = upload.fileList[0]
    // onlu init file need here
    if (file.status === 'init') {
      params.append('file', file.file)
      if (form.installType === INSTALL_TYPE.OFFLINE) {
        params.append('pkgName', file.name)
      }
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

const validUpload = (val, cb) => {
  if (upload.fileList.length > 0) {
    cb()
  } else {
    cb(t('components.PortalInstall.5q0aajl77lg13'))
  }
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
}</style>
