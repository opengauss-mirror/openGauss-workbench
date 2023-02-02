<template>
  <a-modal
    :title="$t('components.UpdateDefaultCode.5nus586u4jg0')"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="user-code-modal"
    :closable="false"
    :esc-to-close="false"
    :mask-closable="false"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="newPassword" :label="$t('components.UpdateCode.5n7vbkp1two0')" :rules="[
        {
          required: true,
          message: $t('components.UpdateCode.5n7vbkp1u340')
        },
        {
          required: true,
          match: /(?!^\d+$)(?!^[A-Za-z]+$)(?!^[^A-Za-z0-9]+$)(?!^.*[\u4E00-\u9FA5].*$)^\S{6,20}$/,
          message: $t('components.UpdateCode.5n7vbkp1u8o0')
        }
      ]">
        <a-input-password v-model="form.newPassword" :placeholder="$t('components.UpdateCode.5n7vbkp1uyw0')" minLength="6" maxlength="20" />
      </a-form-item>
      <a-form-item field="checkPassword" :label="$t('components.UpdateCode.5n7vbkp1v8o0')" :rules="[
        {
          required: true,
          message: $t('components.UpdateCode.5n7vbkp1vg00')
        },
        {
          required: true,
          validator: equalToPassword
        }
      ]">
        <a-input-password v-model="form.checkPassword" :placeholder="$t('components.UpdateCode.5n7vbkp1vm40')" maxlength="20" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button type="primary" :disabled="loading" @click="confirmSubmit">{{$t('components.UpdateCode.5n7vbkp1vy00')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { watch, ref, reactive, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  // import { useUserStore } from '@/store'
  import { updateCode } from '@/api/user'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  const props = withDefaults(defineProps<{
    open: boolean,
    oldPassword?: string
  }>(), {
    open: false,
    oldPassword: ''
  })

  const emits = defineEmits(['update:open', 'change-success'])

  // const userStore = useUserStore()
  const formRef = ref<FormInstance>()
  const form = reactive<any>({
    oldPassword: ref(props.oldPassword),
    newPassword: ref(''),
    checkPassword: ref('')
  })

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  watch(visible, (v) => {
    emits('update:open', v)
  })

  watch(() => props.oldPassword, (v) => {
    if (v) {
      form.oldPassword = v
    }
  })

  watch(() => props.open, (v) => {
    if (v) {
      formRef.value?.resetFields()
    }
    visible.value = v
  })

  const equalToPassword = (value: string, callback: any) => {
    if (form.newPassword !== value) {
      callback(new Error(t('components.UpdateCode.5n7vbkp1w7c0')))
    } else {
      callback()
    }
  }

  const confirmSubmit = () => {
    formRef.value?.validate(valid => {
      if (!valid) {
        // return
        updateCode({
          oldPassword: form.oldPassword,
          newPassword: form.newPassword
        }).then(() => {
          Message.success('Reset success')
          visible.value = false
          emits('change-success', form.newPassword)
        })
      }
    })
  }

  onMounted(() => {
    visible.value = props.open
  })
</script>

<style lang="less" scoped>
.user-code-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
