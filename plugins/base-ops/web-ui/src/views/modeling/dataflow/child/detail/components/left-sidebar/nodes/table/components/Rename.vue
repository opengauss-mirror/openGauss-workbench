
<template>
  <a-modal
    class="rename-container"
    v-model:visible="dData.show" :title="$t('modeling.components.Rename.5mpu3fm9iw00')"
    :ok-text="$t('modeling.components.Rename.5mpu3fm9jbw0')" :confirm-loading="dData.loading" :cancel-text="$t('modeling.components.Rename.5mpu3fm9jf40')"
    @ok="submit" @cancel="close"
  >
    <div class="cu-dialog">
      <a-form :model="dData.formData" ref="formRef" :label-col="{ style: { width: '90px' } }">
        <a-form-item name="name" :label="$t('modeling.components.Rename.5mpu3fm9jhc0')" :rules="{ required: true, message: 'please enter table name' }">
          <a-input :max-length="140" show-word-limit  v-model:value="dData.formData.name" :placeholder="dData.placeholder"></a-input>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import {
  Modal as AModal, Form as AForm, FormItem as AFormItem, Input as AInput
} from '@arco-design/web-vue'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { KeyValue } from '@antv/x6/lib/types'
const emits = defineEmits([`finish`])
const dData = reactive({
  show: false, loading: false, placeholder: '',
  formData: { name: '' } as KeyValue,
  formModal: { name: '' }
})
const open = (data: string) => {
  dData.show = true
  dData.placeholder = data
}
const formRef = ref<null | FormInstance>(null)
const close = () => {
  dData.show = false
  nextTick(() => {
    dData.formData = JSON.parse(JSON.stringify(dData.formModal))
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}
const submit = () => {
  formRef.value?.validate().then(() => {
    dData.loading = true
    emits(`finish`, 'rename', dData.formData.name)
    dData.loading = false
    close()
  }).catch()
}
defineExpose({ open })
</script>
<style scoped lang="scss"></style>
