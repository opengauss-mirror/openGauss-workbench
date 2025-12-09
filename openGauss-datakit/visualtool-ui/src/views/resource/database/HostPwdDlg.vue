<template>
  <el-dialog :model-value="data.show" :title="data.title" :class="['openDesignDialog', 'normal-dialog']"
             :close-on-click-modal="false" :close-on-press-escape="false" :destroy-on-close="true"
             @close="close" :ok-loading="data.loading"  @ok="handleOk" @cancel="close">
    <el-form :model="data.formData" :rules="formRules" ref="formRef" label-width="100px"
             auto-label-width>
      <el-form-item :label="$t('database.HostPwdDlg.5oxhni610s00')">
        <span>{{ data.formData.ip }}</span>
      </el-form-item>
      <el-form-item prop="sshPort" :label="$t('database.HostPwdDlg.5oxhni611qo0')" validate-trigger="blur">
        <el-input-number v-model="data.formData.sshPort" :placeholder="$t('database.HostPwdDlg.5oxhni6126o0')"
                         :min="0" :max="65535" controls-position="right"/>
      </el-form-item>
      <el-form-item v-if="data.type === 'terminal'" prop="sshUsername" :label="$t('database.HostPwdDlg.5oxhni612g00')"
                    validate-trigger="blur">
        <el-input v-model="data.formData.sshUsername" :placeholder="$t('database.HostPwdDlg.5oxhni612mo0')"/>
      </el-form-item>
      <el-form-item prop="sshPassword"
                    :label="data.type === 'terminal' ? $t('database.HostPwdDlg.5oxhni612vk0') : $t('database.HostPwdDlg.5oxhni6132k0')"
                    validate-trigger="blur">
        <el-input v-model="data.formData.sshPassword" :placeholder="$t('database.HostPwdDlg.5oxhni6138s0')"
                  allow-clear type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="close">{{ $t('database.index.5oxhr0qz3t80') }}</el-button>
        <el-button type="primary" :loading="data.loading" @click="handleOk">
          {{ $t('database.index.5oxhr0qz3m80') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref, computed } from 'vue'
import { KeyValue } from '@/types/global'
import { ElForm, ElMessage } from 'element-plus'
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
      { required: true, message: t('database.HostPwdDlg.5oxhni6126o0'), trigger: 'blur' }
    ],
    sshUsername: [
      { required: true, message: t('database.HostPwdDlg.5oxhni612mo0'), trigger: 'blur' },
      {
        validator: (rule: any, value: any, callback: any) => {
          if (!value.trim()) {
            callback(new Error(t('database.HostPwdDlg.5oxhni613ow0')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    sshPassword: [
      { required: true, message: t('database.HostPwdDlg.5oxhni6138s0'), trigger: 'blur' }
    ]
  }
})

const formRef = ref<null | InstanceType<typeof ElForm>>(null)
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const emits = defineEmits([`finish`])

const handleOk = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

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
          ElMessage.success('Successful detection')
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
  } catch (error) {
    console.log('Validation failed:', error)
  }
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

<style lang="scss" scoped>
:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>
