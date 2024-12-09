<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close">
    <a-form :model="data.formData" :rules="formRules" ref="formRef" :label-col="{ style: { width: '100px' } }"
      auto-label-width>
      <a-form-item :label="$t('database.HostPwdDlg.5oxhni610s00')">
        <label>{{ data.formData.ip }}</label>
      </a-form-item>
      <a-form-item field="sshPort" :label="$t('database.HostPwdDlg.5oxhni611qo0')" validate-trigger="blur">
        <a-input-number v-model="data.formData.sshPort" :placeholder="$t('database.HostPwdDlg.5oxhni6126o0')" :min="0" :max="65535"/>
      </a-form-item>
      <a-form-item v-if="data.type === 'terminal'" field="sshUsername" :label="$t('database.HostPwdDlg.5oxhni612g00')"
        validate-trigger="blur">
        <a-input v-model="data.formData.sshUsername" :placeholder="$t('database.HostPwdDlg.5oxhni612mo0')" />
      </a-form-item>
      <a-form-item field="sshPassword"
        :label="data.type === 'terminal' ? $t('database.HostPwdDlg.5oxhni612vk0') : $t('database.HostPwdDlg.5oxhni6132k0')"
        validate-trigger="blur">
        <a-input-password v-model="data.formData.sshPassword" :placeholder="$t('database.HostPwdDlg.5oxhni6138s0')"
          allow-clear />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref, computed } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { encryptPassword } from '@/utils/jsencrypt'
import { addHost } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  loading: false,
  // os(get host os)  terminal(create terminal)
  type: 'terminal',
  formData: {
    ip: '',
    sshPort: 22,
    sshUsername: '',
    sshPassword: ''
  }
})

const formRules = computed(() => {
  return {
    sshPort: [
      { required: true, message: t('database.HostPwdDlg.5oxhni6126o0') }
    ],
    sshUsername: [
      { required: true, message: t('database.HostPwdDlg.5oxhni612mo0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('database.HostPwdDlg.5oxhni613ow0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    sshPassword: [{ required: true, message: t('database.HostPwdDlg.5oxhni6138s0') }]
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
      const encryptPwd = await encryptPassword(data.formData.sshPassword)
      if (data.type === 'getOs') {
        data.loading = true
        // save host
        const param = {
          privateIp: data.formData.ip,
          publicIp: data.formData.ip,
          port: data.formData.sshPort,
          password: encryptPwd
        }
        addHost(param).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            Message.success('Successful detection')
            close()
          }
        }).finally(() => {
          data.loading = false
        })
      } else {
        emits(`finish`, {
          ip: data.formData.ip,
          sshPort: data.formData.sshPort,
          sshUsername: data.formData.sshUsername,
          sshPassword: encryptPwd
        })
        close()
      }
    }
  })
}

const open = (ip: string, type: string) => {
  data.show = true
  data.title = t('database.HostPwdDlg.5oxhni613us0')
  data.formData.ip = ip
  data.type = type
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped></style>
