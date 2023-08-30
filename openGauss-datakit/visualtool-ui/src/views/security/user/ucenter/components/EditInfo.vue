<template>
  <a-modal
    :title="$t('components.EditInfo.5m6njon1w080')"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="user-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="userName" :label="$t('components.EditInfo.5m6njon1www0')" :rules="[{
        required: true,
        message: $t('components.EditInfo.5m6njon1x200')
      }]">
        <a-input v-model="form.userName" :placeholder="$t('components.EditInfo.5m6njon1x5c0')" maxlength="30" />
      </a-form-item>
      <a-form-item field="nickName" :label="$t('components.EditInfo.5m6njon1x840')" :rules="[{
        required: true,
        message: $t('components.EditInfo.5m6njon1xbk0')
      }]">
        <a-input v-model="form.nickName" :placeholder="$t('components.EditInfo.5m6njon1xek0')" />
      </a-form-item>
      <a-form-item field="phonenumber" :label="$t('components.EditInfo.5m6njon1xhg0')" :rules="[
        {
          required: true,
          message: $t('components.EditInfo.5m6njon1xkg0')
        },
        {
          required: true,
          match: /^(?:(?:\+|00)86)?1\d{10}$/,
          message: $t('components.EditInfo.5nslhhf6a7k0')
        }
      ]">
        <a-input v-model="form.phonenumber" :placeholder="$t('components.EditInfo.5m6njon1zco0')" />
      </a-form-item>
      <a-form-item field="email" :label="$t('components.EditInfo.5m6njon1zmw0')" :rules="[
        {
          required: true,
          match: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
          message: $t('components.EditInfo.5nslhhf6z9o0')
        }
      ]">
        <a-input v-model="form.email" :placeholder="$t('components.EditInfo.5m6njon1zrg0')" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel">{{$t('components.EditInfo.5m6njon1zus0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.EditInfo.5m6njon1zxs0')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { reactive, ref, watch, toRaw, onMounted } from 'vue'
  import { useUserStore } from '@/store'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  import { updateUserInfo } from '@/api/user'

  const props = withDefaults(defineProps<{
    open: boolean,
    options?: any
  }>(), {
    open: false,
    options: {}
  })
  const emits = defineEmits(['update:open'])

  const userStore = useUserStore()

  const formRef = ref<FormInstance>()
  const form = reactive<any>({})

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  watch(visible, (v) => {
    emits('update:open', v)
  })

  watch(() => props.open, (v) => {
    if (v) {
      if (props.options.userId) {
        const userInfo = toRaw(props.options)
        form['userName'] = userInfo.userName
        form['nickName'] = userInfo.nickName
        form['phonenumber'] = userInfo.phonenumber
        form['email'] = userInfo.email
      }
      formRef.value?.clearValidate()
    }
    visible.value = v
  })

  const cancel = () => {
    visible.value = false
  }

  const confirmSubmit = () => {
    formRef.value?.validate(valid => {
      if (!valid) {
        console.log(form)

        if (props.options.userId) {
          updateUserInfo({
            userId: props.options.userId,
            ...form
          }).then(() => {
            Message.success('Modified success')
            userStore.info()
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
.user-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
