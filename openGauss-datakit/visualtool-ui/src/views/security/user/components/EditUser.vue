<template>
  <a-modal
    :title="`${props.options.userId ? $t('components.EditUser.5m6nh3mgjns0'): $t('components.EditUser.else1')}${$t('components.EditUser.else2')}`"
    v-model:visible="visible"
    width="800px"
    title-align="start"
    modal-class="user-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-row :gutter="10">
        <a-col :span="12">
          <a-form-item field="userName" :label="$t('components.EditUser.5m6nh3mgkn80')" :rules="[{
            required: true,
            message: $t('components.EditUser.5m6nh3mgl2k0')
          }]">
            <a-input v-model="form.userName" :placeholder="$t('components.EditUser.5m6nh3mglh40')" maxlength="30" />
          </a-form-item>
        </a-col>
        <a-col v-if="!props.options.userId" :span="12">
          <a-form-item field="password" :label="$t('components.EditUser.5m6nh3mglmw0')" :rules="[
            {
              required: true,
              message: $t('components.EditUser.5m6nh3mglt40')
            },
            {
              required: true,
              match: /(?!^\d+$)(?!^[A-Za-z]+$)(?!^[^A-Za-z0-9]+$)(?!^.*[\u4E00-\u9FA5].*$)^\S{6,20}$/,
              message: $t('components.EditUser.5n7v8xco9u80')
            }
          ]">
            <a-input-password :key="passwordKey" v-model="form.password" :placeholder="$t('components.EditUser.5m6nh3mglzg0')" minlength="6" maxlength="20" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="nickName" :label="$t('components.EditUser.5m6nh3mgm380')" :rules="[{
            required: true,
            message: $t('components.EditUser.5m6nh3mgm6o0')
          }]">
            <a-input v-model="form.nickName" :placeholder="$t('components.EditUser.5m6nh3mgm9s0')" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="phonenumber" :label="$t('components.EditUser.5m6nh3mgmds0')" :rules="[
            {
              required: true,
              message: $t('components.EditUser.5m6nh3mgmko0')
            },
            {
              required: true,
              match: /^(?:(?:\+|00)86)?1\d{10}$/,
              message: $t('components.EditUser.5nsg5pkyao80')
            }
          ]">
            <a-input v-model="form.phonenumber" :placeholder="$t('components.EditUser.5m6nh3mgmp40')" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="role" :label="$t('components.EditUser.5m6nh3mgmss0')">
            <a-select v-model="form.roleIds" :placeholder="$t('components.EditUser.5m6nh3mgmvs0')">
              <a-option v-for="item in roles" :key="item.roleId" :value="item.roleId">{{ item.roleName }}</a-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="status" :label="$t('components.EditUser.5m6nh3mgmz40')">
            <a-radio-group v-model="form.status">
              <a-radio value="0">{{$t('components.EditUser.5m6nh3mgn1w0')}}</a-radio>
              <a-radio value="1">{{$t('components.EditUser.5m6nh3mgn4k0')}}</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item field="remark" :label="$t('components.EditUser.5m6nh3mgn9c0')">
            <a-textarea v-model="form.remark" :placeholder="$t('components.EditUser.5m6nh3mgncw0')" allow-clear :max-length="200" show-word-limit />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel">{{$t('components.EditUser.5m6nh3mgnhs0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.EditUser.5m6nh3mgnkg0')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { reactive, ref, watch, toRaw, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  import { createUser, updateUser, userRoleList } from '@/api/user'

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
  const roles = ref<any[]>([])

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  watch(visible, (v) => {
    emits('update:open', v)
  })

  const passwordKey = ref(1)

  watch(() => props.open, (v) => {
    if (v) {
      if (props.options.userId) {
        const { userName, nickName, phonenumber, status, remark } = toRaw(props.options)
        form.userName = userName
        form.nickName = nickName
        form.phonenumber = phonenumber
        form.status = status
        form.remark = remark
        form.password = void 0
      } else {
        form.userName = undefined
        form.password = undefined
        form.nickName = undefined
        form.phonenumber = undefined
        form.roleIds = undefined
        form.status = '0'
        form.remark = undefined
        passwordKey.value += 1
      }
      formRef.value?.clearValidate()
      getUserRole(props.options.userId)
    }
    visible.value = v
  })

  const getUserRole = (userId?: string) => {
    userRoleList(userId ? userId : '').then((res: any) => {
      roles.value = res.roles

      if (props.options.userId) {
        form['roleIds'] = res.roleIds[0]
      }
    })
  }

  const cancel = () => {
    visible.value = false
  }

  const confirmSubmit = () => {
    formRef.value?.validate(valid => {
      if (!valid) {
        console.log(form)

        if (props.options.userId) {
          updateUser({
            userId: props.options.userId,
            ...form
          }).then(() => {
            Message.success('Modified success')
            emits('ok')
            visible.value = false
          })
        } else {
          createUser(form).then(() => {
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
.user-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
