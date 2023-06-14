<template>
  <a-modal
    :unmount-on-close="true"
    title="上传portal安装包"
    v-model:visible="visible"
    width="40vw"
    modal-class="add-portal-modal"
    :mask-closable="false"
    :esc-to-close="false"
  >
    <a-spin :loading="loading" style="display: block">
      <a-form ref="formRef" :model="form" auto-label-width>
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
              :action="upload.url"
              v-model:file-list="upload.fileList"
              :limit="1"
              :show-file-list="true"
              :auto-upload="false"
              draggable
              accept=".tar.gz"
              @success="handleUploadSuccess"
              @error="handleUploadError"
              @before-remove="handleBeforeRemove"
              :show-retry-button="false"
              :show-cancel-button="false"
              :headers="upload.headers"
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
                  <div class="tips-1">
                    <span>安装包将会上传到系统设置的上传路径下</span>
                  </div>
                </div>
              </template>
            </a-upload>
          </a-form-item>
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
import { Message } from '@arco-design/web-vue'
import { getToken } from '@/utils/auth'

const { t } = useI18n()

const props = defineProps({
  open: Boolean,
  hostId: String,
  mode: String,
  installInfo: Object
})

const emits = defineEmits(['update:open', 'startInstall'])

const formRef = ref()
const uploadRef = ref()
const form = reactive({
  pkgUploadPath: {}
})

const upload = reactive({
  fileList: [],
  headers: { Authorization: 'Bearer ' + getToken() },
  url: process.env.VUE_APP_BASE_API + '/plugins/data-migration/resource/uploadPortal'
})

const visible = ref(false)
const loading = ref(false)

watch(visible, (v) => {
  emits('update:open', v)
})

watch(
  () => props.open,
  (v) => {
    visible.value = v
  }
)

const cancel = () => {
  visible.value = false
}

const confirmSubmit = (e) => {
  e.stopPropagation()
  formRef.value?.validate((valid) => {
    if (!valid) {
      loading.value = true
      uploadRef.value.submit()
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

const handleUploadSuccess = () => {
  emits('startInstall')
  loading.value = false
  visible.value = false
}

const handleUploadError = () => {
  loading.value = false
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

    .highlight {
      color: rgb(var(--primary-6));
    }
  }
}
:deep(.arco-upload-progress) {
  display: none;
}
</style>
