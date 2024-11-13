<template>
  <a-modal
    :title="$t('components.EditCode.5m6nfosuqic0')"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="user-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="password" :label="$t('components.EditCode.5m6nfosus3k0')" :rules="[
        {
          required: true,
          message: $t('components.EditCode.5m6nfosusbo0')
        },
        {
          required: true,
          match: /(?!^\d+$)(?!^[A-Za-z]+$)(?!^[^A-Za-z0-9]+$)(?!^.*[\u4E00-\u9FA5].*$)^\S{6,20}$/,
          message: $t('components.EditCode.5nsczb3r79w0')
        }
      ]">
        <a-input-password v-model="form.password" :placeholder="$t('components.EditCode.5m6nfosusgc0')" minlength="6" maxlength="20" />
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
        <a-input-password v-model="form.checkPassword" :placeholder="$t('components.UpdateCode.5n7vbkp1vm40')" minlength="6" maxlength="20" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="visible = false">{{$t('components.EditCode.5m6nfosuskg0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.EditCode.5m6nfosuspc0')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { watch, ref, reactive, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  import { useUserStore } from '@/store'
  import useUser from '@/hooks/user'
  import { resetCode } from '@/api/user'
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

  const { logout } = useUser()
  const userStore = useUserStore()
  const formRef = ref<FormInstance>()
  const form = reactive<any>({
    password: ref(''),
    checkPassword: ref('')
  })

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  watch(visible, (v) => {
    emits('update:open', v)
  })

  watch(() => props.open, (v) => {
    if (v) {
      formRef.value?.resetFields()
    }
    visible.value = v
  })

  const equalToPassword = (value: string, callback: any) => {
    if (form.password !== value) {
      callback(new Error(t('components.UpdateCode.5n7vbkp1w7c0')))
    } else {
      callback()
    }
  }

  const confirmSubmit = () => {
    formRef.value?.validate(valid => {
      if (!valid) {
        resetCode({
          userId: props.options.userId,
          password: form.password
        }).then(() => {
          Message.success('Reset success')
          visible.value = false

          if (props.options.userId === userStore.userId) {
            logout()
          }
        })
      }
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
