<template>
  <a-modal
  :title="`${props.options.id ? $t('components.EditCondition.5m6nmc7dugg0'): $t('components.EditCondition.else1')}${$t('components.EditCondition.else2')}`"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="whiteList-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="title" :label="$t('components.EditCondition.5m6nmc7dvbk0')" :rules="[{
        required: true,
        message: $t('components.EditCondition.5m6nmc7dvgw0')
      }]">
        <a-input v-model="form.title" :placeholder="$t('components.EditCondition.5m6nmc7dvkg0')" maxlength="30" />
      </a-form-item>
      <a-form-item field="condition" :label="$t('components.EditCondition.5m6nmc7dvnk0')" :rules="[{
        required: true,
        match: /^(?:(?:^|,)(?:[0-9]|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])(?:\.(?:[0-9]|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])){3})+$/,
        message: $t('components.EditCondition.5nse3d27z0k0')
      }]">
        <a-textarea v-model="form.condition" :placeholder="$t('components.EditCondition.5m6nmc7dvrg0')" allow-clear/>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="visible = false">{{$t('components.EditCondition.5m6nmc7dvug0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.EditCondition.5m6nmc7dwk00')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { watch, ref, reactive, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  import { createWhiteList, updateWhiteList } from '@/api/whiteList'

  const props = withDefaults(defineProps<{
    open: boolean,
    options?: any
  }>(), {
    open: false,
    options: {}
  })

  const emits = defineEmits(['update:open', 'ok'])

  const formRef = ref<FormInstance>()
  const form = reactive<any>({})

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  watch(visible, (v) => {
    emits('update:open', v)
  })

  watch(() => props.open, (v) => {
    if (v) {
      if (props.options.id) {
        form['title'] = props.options.title
        form['condition'] = props.options.ipList
      }
    }
    visible.value = v
  })

  const confirmSubmit = () => {
    formRef.value?.validate(valid => {
      if (!valid) {
        console.log(form)

        if (props.options.id) {
          updateWhiteList({
            id: props.options.id,
            title: form.title,
            ipList: form.condition
          }).then(() => {
            Message.success('Modified success')
            emits('ok')
            visible.value = false
          })
        } else {
          createWhiteList({
            title: form.title,
            ipList: form.condition
          }).then(() => {
            Message.success('Added success')
            emits('ok')
            visible.value = false
          })
        }
      }
    })
  }

  onMounted(() => {
    visible.value = props.open
  })
</script>

<style lang="less" scoped>
.whiteList-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
