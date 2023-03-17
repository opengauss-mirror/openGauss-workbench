<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '650px' }" @cancel="close" @close="close">
    <template #footer>
      <div class="flex-between">
        <div class="flex-row">
          <div class="label-color mr" v-if="data.status !== hostStatusEnum.unTest">{{
            $t('components.AddHost.currentStatus')
          }}
          </div>
          <a-tag v-if="data.status === hostStatusEnum.success" color="green">{{
            $t('components.AddHost.5mphy3snvg80')
          }}</a-tag>
          <a-tag v-if="data.status === hostStatusEnum.fail" color="red">{{
            $t('components.AddHost.5mphy3snwq40')
          }}</a-tag>
        </div>
        <div>
          <a-button class="mr" @click="close">{{
            $t('components.AddHost.5mphy3snwxs0')
          }}</a-button>
          <a-button :loading="data.testLoading" class="mr" @click="handleTestHost">{{
            $t('components.AddHost.5mphy3snx3o0')
          }}</a-button>
          <a-button :loading="data.loading" type="primary" @click="submit">{{
            $t('components.AddHost.5mphy3snx7c0')
          }}</a-button>
        </div>
      </div>

    </template>
    <a-form :model="data.formData" ref="formRef" auto-label-width :rules="formRules">
      <a-form-item field="privateIp" :label="$t('components.AddHost.ipAddress')" validate-trigger="blur">
        <a-input v-model="data.formData.privateIp" :placeholder="$t('components.AddHost.5mphy3snxdo0')"></a-input>
      </a-form-item>
      <a-form-item field="publicIp" :label="$t('components.AddHost.5mphy3snxis0')" validate-trigger="blur">
        <a-input v-model="data.formData.publicIp" :placeholder="$t('components.AddHost.5mphy3snxmw0')"></a-input>
      </a-form-item>
      <a-form-item field="port" :label="$t('components.AddHost.5mphy3snxtc0')" validate-trigger="blur">
        <a-input-number v-model="data.formData.port" :placeholder="$t('components.AddHost.5mphy3snxzk0')" />
      </a-form-item>
      <a-form-item field="password" :label="$t('components.AddHost.5mphy3sny4w0')" validate-trigger="blur">
        <a-input-password v-model="data.formData.password" :placeholder="$t('components.AddHost.5mphy3snyao0')"
          allow-clear />
        <a-checkbox style="width: 150px" v-model="data.formData.isRemember">{{ $t('components.AddHost.else2')
        }}</a-checkbox>
      </a-form-item>
      <a-form-item :label="$t('components.AddHost.5mphy3snyg40')">
        <a-select :loading="data.azListLoading" v-model="data.formData.azId"
          :placeholder="$t('components.AddHost.5mphy3snyn80')">
          <a-option v-for="item in data.azList" :key="item.azId" :value="item.azId">{{
            item.name
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('components.AddHost.5mphy3snysg0')">
        <a-textarea v-model="data.formData.remark" :placeholder="$t('components.AddHost.5mphy3snyxc0')"></a-textarea>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, toRaw, computed } from 'vue'
import { addHost, editHost, hostPing, azListAll } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
enum hostStatusEnum {
  unTest = -1,
  success = 1,
  fail = 0
}

const data = reactive<KeyValue>({
  show: false,
  title: t('components.AddHost.5mphy3snz5k0'),
  loading: false,
  testLoading: false,
  status: hostStatusEnum.unTest,
  azListLoading: false,
  azList: [],
  formData: {
    hostId: '',
    privateIp: '',
    publicIp: '',
    port: 22,
    password: '',
    isRemember: false,
    azId: '',
    remark: ''
  }
})

const formRules = computed(() => {
  return {
    privateIp: [{ required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snxdo0') }],
    publicIp: [{ required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snxmw0') }],
    port: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snxzk0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('components.AddHost.else1'))
              resolve(false)
            }
          })
        }
      }
    ],
    password: [{ required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snyao0') }]
  }
})

const emits = defineEmits([`finish`])
const formRef = ref<null | FormInstance>(null)
const submit = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      data.loading = true
      encryptPassword(data.formData.password).then((res) => {
        const param = Object.assign({}, data.formData)
        param.password = res
        if (data.formData.hostId) {
          if (data.formData.password) {
            editHost(data.formData.hostId, param).then((res: KeyValue) => {
              data.loading = false
              if (Number(res.code) === 200) {
                Message.success({ content: `Modified success` })
                emits(`finish`)
              }
              close()
            }).finally(() => {
              data.loading = false
            })
          }
        } else {
          addHost(param).then((res: KeyValue) => {
            data.loading = false
            if (Number(res.code) === 200) {
              Message.success({ content: `Create success` })
              emits(`finish`)
            }
            close()
          }).finally(() => {
            data.loading = false
          })
        }
      })
    }
  }).catch()
}
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const handleTestHost = () => {
  formRef.value?.validate().then(async result => {
    if (!result) {
      data.testLoading = true
      const encryptPwd = await encryptPassword(data.formData.password)
      const param = {}
      Object.assign(param, {
        privateIp: data.formData.privateIp,
        publicIp: data.formData.publicIp,
        port: data.formData.port,
        password: encryptPwd,
        isRemember: data.formData.isRemember,
        azId: data.formData.azId,
        remark: data.formData.remark,
        username: 'root'
      })

      hostPing(toRaw(param)).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          data.status = hostStatusEnum.success
        } else {
          data.status = hostStatusEnum.fail
        }
      }).catch(() => {
        data.status = hostStatusEnum.fail
      }).finally(() => {
        data.testLoading = false
      })
    }
  })
}

const getAZList = () => new Promise(resolve => {
  data.azListLoading = true
  azListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      data.azList = res.data
    } else resolve(false)
  }).finally(() => {
    data.azListLoading = false
  })
})

const open = (type: string, editData?: KeyValue) => {
  data.show = true
  getAZList()
  data.status = hostStatusEnum.unTest
  data.loading = false
  if (type === 'update' && data) {
    data.title = t('components.AddHost.5mphy3snzrk0')
    Object.assign(data.formData, editData)
    data.formData.isRemember = false
  } else {
    data.title = t('components.AddHost.5mphy3snz5k0')
    Object.assign(data.formData, {
      hostId: '',
      privateIp: '',
      publicIp: '',
      password: '',
      isRemember: false,
      port: 22,
      azId: '',
      remark: ''
    })
  }
}

defineExpose({
  open
})

</script>
