<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close">
    <a-form :model="data.formData" ref="formRef" :label-col="{ style: { width: '100px' } }" :rules="data.rules"
      auto-label-width>
      <a-form-item :label="$t('customControl.HostPwdDlg.5mplfut5bj80')">
        {{ data.formData.privateIp }}({{ data.formData.publicIp }})
      </a-form-item>
      <a-form-item field="password" :label="$t('customControl.HostPwdDlg.else1')" validate-trigger="blur"
        :rules="[{ required: true, message: t('customControl.HostPwdDlg.5mplfut5lus0') }]">
        <a-input-password v-model="data.formData.password" :placeholder="$t('customControl.HostPwdDlg.5mplfut5kuc0')"
          allow-clear />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">

import { nextTick, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: t('customControl.HostPwdDlg.5mplfut5lgo0'),
  loading: false,
  formData: {
    hostId: '',
    privateIp: '',
    publicIp: '',
    password: ''
  },
  rules: {
    password: [{ required: true, 'validate-trigger': 'blur', message: t('customControl.HostPwdDlg.5mplfut5lus0') }]
  }
})

const formRef = ref<null | FormInstance>(null)
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const emits = defineEmits([`finish`])

const handleOk = () => {
  formRef.value?.validate().then(async result => {
    if (!result) {
      const encryptPwd = await encryptPassword(data.formData.password)
      emits(`finish`, {
        hostId: data.formData.hostId,
        password: encryptPwd
      })
      close()
    }
  })
}

const open = (hostData: KeyValue) => {
  data.show = true
  data.title = t('customControl.HostPwdDlg.5mplfut5lgo0')
  data.formData.hostId = hostData.hostId
  data.formData.privateIp = hostData.privateIp
  data.formData.publicIp = hostData.publicIp
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped>

</style>
