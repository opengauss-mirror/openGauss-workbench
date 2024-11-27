<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close">
    <a-form v-if="data.show" :model="data.formData" ref="formRef" :rules="data.rules" auto-label-width>
      <a-form-item :label="$t('components.HostPwdDlg.5mpi0giveys0')">
        {{ data.formData.privateIp }}({{ data.formData.publicIp }})
      </a-form-item>
      <a-form-item field="password" :label="$t('components.HostPwdDlg.else1')" validate-trigger="blur"
        :rules="[{ required: true, message: t('components.HostPwdDlg.5mpi0givfhc0') }]">
        <a-input-password v-model="data.formData.password" :placeholder="$t('components.HostPwdDlg.5mpi0givf8o0')"
          allow-clear />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: t('components.HostPwdDlg.5mpi0givfcw0'),
  loading: false,
  formData: {
    hostId: '',
    port: '',
    privateIp: '',
    publicIp: '',
    password: ''
  },
  rules: {
    password: [{ required: true, 'validate-trigger': 'blur', message: t('components.HostPwdDlg.5mpi0givfhc0') }]
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
  formRef.value?.validate().then(result => {
    if (!result) {
      data.loading = true
      encryptPassword(data.formData.password).then((res: string) => {
        console.log('encryptPassword...', res)
        emits(`finish`, {
          hostId: data.formData.hostId,
          ip: data.formData.publicIp,
          port: data.formData.port,
          password: res
        })
        close()
      }).catch(() => {
        Message.error(t('components.HostPwdDlg.5mpi0givfl40'))
      }).finally(() => {
        data.loading = false
      })
    }
  })
}

const open = (hostData: KeyValue) => {
  data.show = true
  data.title = t('components.HostPwdDlg.5mpi0givfcw0')
  data.formData.hostId = hostData.hostId
  data.formData.privateIp = hostData.privateIp
  data.formData.publicIp = hostData.publicIp
  data.formData.port = hostData.port
  data.formData.password = ''
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped></style>
