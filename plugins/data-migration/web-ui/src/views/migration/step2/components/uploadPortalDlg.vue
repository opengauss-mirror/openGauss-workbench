<template>
  <el-dialog
    :unmount-on-close="true"
    :title="$t('step2.compoents.uploadPortalDlg.uploadTitle')"
    v-model="visible"
    width="40vw"
    modal-class="add-portal-modal"
    :mask-closable="false"
    :esc-to-close="false"
  >
    <el-spin :loading="loading" style="display: block">
      <el-form ref="formRef" :model="form" label-width="auto" >
        <el-form-item prop="pluginName"
          :rules="formRules">
          <file-upload :percentage="progressPercent" @changeFile="changeFile" accept=".gz" size-limit="300"
                       :tips="$t('step2.compoents.uploadPortalDlg.uploadTypeMsg')" ref='uploadRef'></file-upload>
        </el-form-item>
        <div class="el-upload__tip">
          <span>{{ $t('step2.compoents.uploadPortalDlg.uploadPathMsg') }}</span>
        </div>
      </el-form>
    </el-spin>
    <template #footer>
      <div class="modal-footer">
        <el-button @click="cancel" v-if="!loading">{{
          $t('components.PortalInstall.5q0aajl77ik0')
        }}</el-button>
        <el-button
          type="primary"
          style="margin-left: 16px"
          @click="startInstall"
          :loading="loading"
          >{{ $t('components.PortalInstall.5q0aajl77lg0') }}</el-button
        >
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {getToken} from '@/utils/auth'
import FileUpload from '@/components/file-upload'
import axios from "axios";
import showMessage from "@/utils/showMessage";

const { t } = useI18n()

const props = defineProps({
  open: Boolean,
  hostId: String,
  mode: String,
  installInfo: Object
})

const progressPercent = ref(0)
const emits = defineEmits(['update:open', 'startInstall'])

const formRef = ref()
const uploadRef = ref()
const form = reactive({
  pkgUploadPath: {}
})

const upload = reactive({
  fileList: [],
  headers: { Authorization: 'Bearer ' + getToken() },
  url:
    process.env.VUE_APP_BASE_API +
    '/plugins/data-migration/resource/uploadPortal'
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
  progressPercent.value = 0
  upload.fileList = []
  if(uploadRef.value) {
    uploadRef.value?.closeWin()
  }
}

const handleUploadSuccess = () => {
  showMessage('success', t('step2.compoents.uploadPortalDlg.uploadSuccMsg'))
  emits('startInstall')
  loading.value = false
  visible.value = false
  progressPercent.value = 0
  upload.fileList = []
}

const handleUploadError = () => {
  showMessage('error', t('step2.compoents.uploadPortalDlg.uploadErrorMsg'))
  loading.value = false
  progressPercent.value = 0
}
const formRules = computed(() => {
  return {
    pluginName: [{ required: true, trigger: ['blur', 'change'], message: t('step2.compoents.uploadPortalDlg.uploadFormRule') }]
  }
})
const changeFile = (file) => {
  upload.fileList = [file]
  progressPercent.value = 0
}
const startInstall = async () => {
  if (upload.fileList.length <= 0) {
    showMessage('error', t('step2.compoents.uploadPortalDlg.uploadErrorMsg'))
  } else {
    if (progressPercent.value === 0) {
      const formData = new FormData
      formData.append('file', upload.fileList[0].raw)
      axios({
        url: '/plugins/data-migration/resource/uploadPortal',
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
      }).then((res) => {
        if (Number(res.code) === 200) {
          handleUploadSuccess()
        }
      }).catch((error) => {
        handleUploadError()
        console.log(error)
      })
    }
  }
}

onMounted(() => {
  visible.value = props.open
  upload.fileList = []
})
</script>

<style lang="less" scoped>
.add-portal-modal {
  .modal-footer {
    text-align: center;
  }
}

.upload-info {
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
