<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close">
    <a-form :model="data.formData" :rules="formRules" ref="formRef" :label-col="{ style: { width: '100px' } }"
      auto-label-width>
      <a-form-item label="IP地址">
        <label>{{ data.formData.ip }}</label>
      </a-form-item>
      <a-form-item field="sshPort" label="端口号" validate-trigger="blur">
        <a-input-number v-model="data.formData.sshPort" placeholder="请输入SSH端口号" />
      </a-form-item>
      <a-form-item field="sshUsername" label="用户名" validate-trigger="blur">
        <a-input v-model="data.formData.sshUsername" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item field="sshPassword" label="密码" validate-trigger="blur">
        <a-input-password v-model="data.formData.sshPassword" placeholder="请输入用户密码" allow-clear />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">

import { nextTick, reactive, ref, computed } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { encryptPassword } from '@/utils/jsencrypt'
const data = reactive<KeyValue>({
  show: false,
  loading: false,
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
      { required: true, message: '请输入SSH端口号' },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb('请输入正确的端口号')
              resolve(false)
            }
          })
        }
      }
    ],
    sshUsername: [
      { required: true, message: '请输入用户名' },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb('不能为纯空格')
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    sshPassword: [{ required: true, message: '请输入用户密码' }]
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
      emits(`finish`, {
        ip: data.formData.ip,
        sshPort: 22,
        sshUsername: data.formData.sshUsername,
        sshPassword: encryptPwd
      })
      close()
    }
  })
}

const open = (ip: string) => {
  data.show = true
  data.title = 'SSH连接信息'
  data.formData.ip = ip
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped></style>
