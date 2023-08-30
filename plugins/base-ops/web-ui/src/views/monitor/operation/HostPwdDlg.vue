<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :ok-loading="data.loading"
    :modal-style="{ width: '450px' }"
    @ok="handleOk"
    @cancel="close"
  >
    <div
      v-for="host in data.form"
      :key="host.hostId"
    >
      <host-pwd-form
        :form-data="host"
        :ref="(el: any) => setRefMap(el, host.hostId)"
      ></host-pwd-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">

import { nextTick, reactive, ref, computed } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import HostPwdForm from './HostPwdForm.vue'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: t('customControl.HostPwdDlg.5mplfut5lgo0'),
  loading: false,
  form: []
})

const formRef = ref<null | FormInstance>(null)
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const refObj = ref<KeyValue>({})
const setRefMap = (el: HTMLElement, key: string) => {
  if (!refObj.value[key]) {
    refObj.value[key] = el
  }
}

const refList = computed(() => {
  const result = []
  const refs = Object.keys(refObj.value)
  if (refs.length) {
    for (let key in refObj.value) {
      if (refObj.value[key]) {
        result.push(refObj.value[key])
      }
    }
  }
  return result
})

const emits = defineEmits([`finish`])

const handleOk = () => {

  const methodArr = []
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      methodArr.push(refList.value[i].formValidate())
    }
  }
  Promise.all(methodArr).then(async (res) => {
    const validRes = res.filter((item: boolean) => {
      return item === false
    })
    if (validRes.length) {
      return
    }
    const result: KeyValue = {}
    for (let i = 0; i < data.form.length; i++) {
      const temp = data.form[i]
      const encryptPwd = await encryptPassword(temp.rootPassword)
      result[temp.hostId] = encryptPwd
    }
    data.show = false
    emits('finish', result)
  })
}

const open = (nodes: KeyValue[]) => {
  data.show = true
  data.title = t('operation.HostPwdDlg.pwdValid')
  data.form = nodes
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped></style>
