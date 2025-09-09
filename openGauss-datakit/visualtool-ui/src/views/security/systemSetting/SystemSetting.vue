<template>
  <a-modal
    v-if="visible"
    :title="$t('components.EditSysSettings.5mpivn7fwjc1')"
    v-model:visible="visible"
    width="50vw"
    title-align="start"
    modal-class="user-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="uploadPath"
                   :label="$t('components.EditSysSettings.5mpivn7fwjc2')"
                   :rules="[{required: true, message: $t('components.EditSysSettings.5mpivn7fwjc6')}, { validator: pathValidator }]">
        <a-input v-model.trim="form.uploadPath" :placeholder="$t('components.EditSysSettings.5mpivn7fwjc6')"/>
      </a-form-item>
      <a-form-item field="portalPkgDownloadUrl"
                   :label="$t('components.EditSysSettings.5mpivn7fwj11')"
                   :rules="[{required: true, message: $t('components.EditSysSettings.5mpivn7fwj12')}]">
        <a-input v-model.trim="form.portalPkgDownloadUrl" :placeholder="$t('components.EditSysSettings.5mpivn7fwj12')"/>
      </a-form-item>
      <a-form-item field="portalPkgName"
                   :label="$t('components.EditSysSettings.5mpivn7fwj13')"
                   :rules="[{required: true, message: $t('components.EditSysSettings.5mpivn7fwj14')}]">
        <a-input v-model.trim="form.portalPkgName" :placeholder="$t('components.EditSysSettings.5mpivn7fwj14')"/>
      </a-form-item>
      <a-form-item field="portalJarName"
                   :label="$t('components.EditSysSettings.5mpivn7fwj15')"
                   :rules="[{required: true, message: $t('components.EditSysSettings.5mpivn7fwj16')}]">
        <a-input v-model.trim="form.portalJarName" :placeholder="$t('components.EditSysSettings.5mpivn7fwj16')"/>
      </a-form-item>
        <a-form-item field="serverHost"
                     :label="$t('components.EditSysSettings.5mpivn7fwj17')"
                     :rules="[{required: false, message: $t('components.EditSysSettings.5mpivn7fwj18')}]">
          <a-input v-model.trim="form.serverHost" :placeholder="$t('components.EditSysSettings.5mpivn7fwj18')"/>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="visible = false">{{ $t('components.EditCode.5m6nfosuskg0') }}</a-button>
        <a-button type="primary" :loading="loading" style="margin-left: 16px;" @click="confirmSubmit">
          {{ $t('components.EditCode.5m6nfosuspc0') }}
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { watch, ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { checkUploadPath, listSysSetting, updateSysSetting } from '@/api/sysSetting'
import { SysSetting } from '@/types/sysSetting'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  open: boolean,
  options?: any
}>(), {
  open: false,
  options: {}
})

const emits = defineEmits(['update:open'])

const formRef = ref<FormInstance>()
let form = reactive<SysSetting>({
  id: '',
  userId: '',
  uploadPath: '',
  portalPkgDownloadUrl: '',
  portalPkgName: '',
  portalJarName: '',
  serverHost: ''
})
const visible = ref<boolean>(false)
const loading = ref<boolean>(false)

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  if (v) {
    formRef.value?.resetFields()
    querySystemSetting()
  }
  visible.value = v
})

const pathValidator = (value: any, cb: any) => {
  return new Promise(resolve => {
    const reg = /^(\/[\u4e00-\u9fa5\w-\\.]+)*\/$/
    const re = new RegExp(reg)
    const regWin = /[a-zA-Z]:\\(?:[^\\/:*?\"<>|\r\n]+\\)*[^\\/:*?\"<>|\r\n]*\\$/
    const reWin = new RegExp(regWin)
    if (re.test(value) || reWin.test(value)) {
      checkUploadPath(value).then(res => {
        if (res.data) {
          resolve(true)
        } else {
          cb(t('components.EditSysSettings.5mpivn7fwj10'))
          resolve(false)
        }
      })
    } else {
      cb(t('components.EditSysSettings.5mpivn7fwjc9'))
      resolve(false)
    }
  })
}

const confirmSubmit = async () => {
  formRef.value?.validate(valid => {
    if (!valid) {
      loading.value = true
      updateSysSetting(form).then(() => {
        Message.success('system setting update success')
        visible.value = false
      }).finally(() => {
        loading.value = false
      })
    }
  })
}

const querySystemSetting = () => {
  listSysSetting().then(res => {
    form.id = res.data?.id
    form.uploadPath = res.data?.uploadPath
    form.userId = res.data?.userId
    form.portalPkgDownloadUrl = res.data?.portalPkgDownloadUrl
    form.portalPkgName = res.data?.portalPkgName
    form.portalJarName = res.data?.portalJarName
    form.serverHost = res.data?.serverHost
  })
}

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.user-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
