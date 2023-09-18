<template>
  <a-modal
    :title="$t('components.PortalInstall.5q0aajl75f00')"
    v-model:visible="visible"
    width="40vw"
    modal-class="add-portal-modal"
    :mask-closable="false"
    :esc-to-close="false"
  >
    <a-spin :loading="loading" style="display: block">
      <a-form ref="formRef" :model="form" auto-label-width>
        <a-form-item
          field="hostUserId"
          :label="$t('components.PortalInstall.5q0aajl76580')"
          :rules="[
            {
              required: true,
              message: $t('components.PortalInstall.5q0aajl76io0'),
            },
          ]"
        >
          <a-select
            v-model="form.hostUserId"
            :placeholder="$t('components.PortalInstall.5q0aajl76ug0')"
          >
            <a-option
              v-for="item in hostUserData"
              :key="item.hostUserId"
              :value="item.hostUserId"
              >{{ item.username }}</a-option
            >
          </a-select>
        </a-form-item>
        <a-form-item
          field="installPath"
          :label="$t('components.PortalInstall.5q0aajl76xw0')"
        >
          <a-input
            v-model="form.installPath"
            :placeholder="$t('components.PortalInstall.5q0aajl77f40')"
          />
        </a-form-item>
        <a-form-item
          field="installType"
          :label="$t('components.PortalInstall.5q0aajl77lg0')"
        >
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
          <a-form-item
            field="pkgDownloadUrl"
            :label="$t('components.PortalInstall.5q0aajl77lg3')"
            :rules="[
              {
                required: true,
                message: $t('components.PortalInstall.5q0aajl77lg4'),
              },
            ]"
          >
            <a-input v-model="form.pkgDownloadUrl"></a-input>
          </a-form-item>
          <a-form-item
            field="pkgName"
            :label="$t('components.PortalInstall.5q0aajl77lg5')"
            :rules="[
              {
                required: true,
                message: $t('components.PortalInstall.5q0aajl77lg6'),
              },
            ]"
          >
            <a-input v-model="form.pkgName"></a-input>
          </a-form-item>
        </template>
        <template v-if="form.installType != INSTALL_TYPE.IMPORTINSTALL">
        <a-form-item
          field="jarName"
          :label="$t('components.PortalInstall.5q0aajl77lg7')"
          :rules="[
            {
              required: true,
              message: $t('components.PortalInstall.5q0aajl77lg8'),
            },
          ]"
        >
          <a-input v-model="form.jarName"></a-input>
        </a-form-item>
      </template>
        <template v-if="form.installType === INSTALL_TYPE.OFFLINE">
          <a-form-item
            field="packagePath"
            :label="$t('components.PortalInstall.5q0aajl77lg9')"
            :rules="[
              {
                required: true,
                validator: validUpload,
              },
            ]"
          >
            <a-upload
              ref="uploadRef"
              v-model:file-list="upload.fileList"
              :limit="1"
              :show-file-list="true"
              :auto-upload="false"
              draggable
              accept=".tar.gz"
              @before-remove="handleBeforeRemove"
              :show-retry-button="false"
              :show-cancel-button="false"
            >
              <template #upload-button>
                <div class="upload-info">
                  <div class="upload-icon">
                    <icon-plus
                      :style="{ fontSize: '48px', color: '#86909C' }"
                    />
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
      </a-form>
    </a-spin>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel" v-if="!loading">{{
          $t('components.PortalInstall.5q0aajl77ik0')
        }}</a-button>
        <a-button
          type="primary"
          :disabled="loading"
          style="margin-left: 16px"
          @click="confirmSubmit"
          :loading="loading"
          >{{ $t('components.PortalInstall.5q0aajl77lg0') }}</a-button
        >
      </div>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
// import { Message } from '@arco-design/web-vue'
import { hostUsers, installPortal, reInstallPortal } from '@/api/task'
import { getSysSetting } from '@/api/common'
import { INSTALL_TYPE } from '@/utils/constants'
import { deletePortal } from '@/api/task'
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
  jarName: '',
  pkgUploadPath: {}
})

const upload = reactive({
  fileList: []
})

const visible = ref(false)
const loading = ref(false)
const hostUserData = ref([])
const sysSetting = ref()

watch(visible, (v) => {
  emits('update:open', v)
})

watch(
  () => props.open,
  (v) => {
    if (v) {
      getSysSetting().then((res) => {
        sysSetting.value = res.data
        getHostUsers()
      })
    } else {
      form.hostUserId = ''
      form.installPath = ''
      form.installType = INSTALL_TYPE.ONLINE
      form.pkgDownloadUrl = ''
      form.pkgName = ''
      form.pkgUploadPath = {}
      form.jarName = ''
      upload.fileList = []
    }
    visible.value = v
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
    form.pkgDownloadUrl = props.installInfo?.pkgDownloadUrl
      ? props.installInfo?.pkgDownloadUrl
      : sysSetting.value.portalPkgDownloadUrl
    form.pkgName = props.installInfo?.pkgName
      ? props.installInfo?.pkgName
      : sysSetting.value.portalPkgName
    form.jarName = props.installInfo?.jarName
      ? props.installInfo?.jarName
      : sysSetting.value.portalJarName
  })
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
    text-align: center;
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
</style>
