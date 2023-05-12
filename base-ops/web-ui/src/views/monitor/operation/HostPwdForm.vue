<template>
  <a-form
    :model="form"
    ref="formRef"
    auto-label-width
    :rules="formRules"
  >
    <a-form-item :label="$t('operation.HostPwdDlg.IpAddress')">
      <label>{{ form.publicIp }}({{ form.privateIp }})</label>
    </a-form-item>
    <a-form-item
      :label="$t('operation.HostPwdDlg.rootPwd')"
      field="rootPassword"
      validate-trigger="blur"
    >
      <a-input-password
        v-model="form.rootPassword"
        :placeholder="$t('operation.HostPwdDlg.rootPwdPlaceholder')"
        allow-clear
      />
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { PropType, ref, computed, defineProps } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const props = defineProps({
  formData: {
    type: Object as PropType<KeyValue>,
    required: true
  }
})
const emits = defineEmits([`update:formData`])
const form = computed({
  get: () => props.formData,
  set: (val) => {
    emits(`update:formData`, val)
  }
})

const formRules = computed(() => {
  return {
    rootPassword: [
      { required: true, message: t('operation.HostPwdDlg.rootPwdPlaceholder') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('operation.HostPwdDlg.else1'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ]
  }
})

const formRef = ref<null | FormInstance>(null)

const formValidate = async (): Promise<boolean> => {
  const validRes = await formRef.value?.validate()
  if (!validRes) {
    return true
  }
  return false
}

defineExpose({
  formValidate
})

</script>
