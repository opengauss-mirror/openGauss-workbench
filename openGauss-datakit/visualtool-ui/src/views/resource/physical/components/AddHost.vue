<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :ok-loading="data.loading"
    :modal-style="{ width: '650px' }"
    @cancel="close"
    @close="close"
  >
    <template #footer>
      <div class="flex-between">
        <div class="flex-row">
          <div
            class="label-color mr"
            v-if="data.status !== hostStatusEnum.unTest"
          >{{
            $t('components.AddHost.currentStatus')
          }}
          </div>
          <a-tag
            v-if="data.status === hostStatusEnum.success"
            color="green"
          >{{
            $t('components.AddHost.5mphy3snvg80')
          }}</a-tag>
          <a-tag
            v-if="data.status === hostStatusEnum.fail"
            color="red"
          >{{
            $t('components.AddHost.5mphy3snwq40')
          }}</a-tag>
        </div>
        <div>
          <a-button
            class="mr"
            @click="close"
          >{{
            $t('components.AddHost.5mphy3snwxs0')
          }}</a-button>
          <a-button
            :loading="data.testLoading"
            class="mr"
            @click="handleTestHost"
          >{{
            $t('components.AddHost.5mphy3snx3o0')
          }}</a-button>
          <a-button
            :loading="data.loading"
            type="primary"
            @click="submit"
          >{{
            $t('components.AddHost.5mphy3snx7c0')
          }}</a-button>
        </div>
      </div>

    </template>
    <a-form
      :model="data.formData"
      ref="formRef"
      auto-label-width
      :rules="formRules"
    >
      <a-form-item
        field="name"
        :label="$t('components.AddHost.name')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.formData.name"
          :placeholder="$t('components.AddHost.namePlaceholder')"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="privateIp"
        :label="$t('components.AddHost.ipAddress')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.formData.privateIp"
          :placeholder="$t('components.AddHost.5mphy3snxdo0')"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="publicIp"
        :label="$t('components.AddHost.5mphy3snxis0')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.formData.publicIp"
          :placeholder="$t('components.AddHost.5mphy3snxmw0')"
          @blur="handleBlur"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="port"
        :label="$t('components.AddHost.5mphy3snxtc0')"
        validate-trigger="blur"
      >
        <a-input-number
          v-model="data.formData.port"
          :placeholder="$t('components.AddHost.5mphy3snxzk0')"
          :min="0"
          :max="65535"
        />
      </a-form-item>
      <a-form-item
        field="password"
        :label="$t('components.AddHost.5mphy3sny4w0')"
        validate-trigger="blur"
      >
        <a-input-password
          v-model="data.formData.password"
          :placeholder="$t('components.AddHost.5mphy3snyao0')"
          @focus="passwordFocus"
          @blur="passwordBlur"
          :invisible-button="data.formData.password !== data.emptyPwd"
          allow-clear
          ref="formPwdRef"
        />
        <a-checkbox
          style="width: 150px"
          v-model="data.formData.isRemember"
        >{{ $t('components.AddHost.else2')
        }}</a-checkbox>
      </a-form-item>
      <a-form-item
        field="tags"
        :label="$t('components.AddHost.tags')"
      >
        <a-select
          :loading="data.tagsLoading"
          v-model="data.formData.tags"
          :placeholder="$t('components.AddHost.tagsPlaceholder')"
          allow-create
          multiple
          allow-clear
          @change="tagsChange"
        >
          <a-option
            v-for="item in data.tagsList"
            :key="item.value"
            :value="item.value"
          >{{
            item.label
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('components.AddHost.5mphy3snysg0')">
        <a-textarea
          v-model.trim="data.formData.remark"
          :placeholder="$t('components.AddHost.5mphy3snyxc0')"
        ></a-textarea>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, toRaw, computed } from 'vue'
import { addHost, editHost, hostPing, hostTagListAll, hostUserListAll } from '@/api/ops'
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
  tagsLoading: false,
  tagsList: [],
  oldPwd: '',
  emptyPwd: 'emptyPassword',
  formData: {
    name: '',
    hostId: '',
    privateIp: '',
    publicIp: '',
    port: 22,
    password: '',
    isRemember: false,
    tags: [],
    remark: ''
  }
})

const formRules = computed(() => {
  return {
    name: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.namePlaceholder') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('database.JdbcInstance.5oxhtcbobtc0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    privateIp: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snxdo0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.((1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)\.){2}(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('database.JdbcInstance.5oxhtcboblw0'))
              resolve(false)
            }
          })
        }
      }
    ],
    publicIp: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snxmw0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.((1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)\.){2}(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('database.JdbcInstance.5oxhtcboblw0'))
              resolve(false)
            }
          })
        }
      }
    ],
    port: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.5mphy3snxzk0') }
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
      if (data.formData.hostId && data.formData.password === data.emptyPwd) {
        const param = Object.assign({}, data.formData)
        param.password = data.oldPwd
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
      } else {
        encryptPassword(data.formData.password).then((res) => {
          const param = Object.assign({}, data.formData)
          param.password = res
          if (data.formData.hostId) {
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
    }
  }).catch()
}
const close = () => {
  data.show = false
  data.oldPwd = ''
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const tagsChange = (val: any) => {
  data.formData.tags = val.filter((item: string) => {
    return item.trim() !== ''
  })
}

const passwordFocus = () => {
  if (data.formData.password === data.emptyPwd) {
    data.formData.password = ''
  }
}
const formPwdRef = ref()
const passwordBlur = () => {
  if (!data.formData.password && data.oldPwd) {
    formPwdRef.value.invisible = true
    data.formData.password = data.emptyPwd
  }
}

const handleTestHost = () => {
  formRef.value?.validate().then(async result => {
    if (!result) {
      data.testLoading = true
      let encryptPwd
      if (data.formData.hostId && data.formData.password === data.emptyPwd) {
        encryptPwd = data.oldPwd
      } else {
        encryptPwd = await encryptPassword(data.formData.password)
      }
      const param = {}
      Object.assign(param, {
        privateIp: data.formData.privateIp,
        publicIp: data.formData.publicIp,
        port: data.formData.port,
        password: encryptPwd,
        isRemember: data.formData.isRemember,
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

const getAllTag = () => {
  data.tagsLoading = true
  hostTagListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.tagsList = []
      res.data.forEach((item: KeyValue) => {
        const temp = {
          label: item.name,
          value: item.name
        }
        data.tagsList.push(temp)
      })
    }
  }).finally(() => {
    data.tagsLoading = false
  })
}

const getHostPassword = (hostId: string) => {
  hostUserListAll(hostId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      const rootObj = res.data.find((item: KeyValue) => {
        return item.username === 'root'
      })
      if (rootObj) {
        data.oldPwd = rootObj.password
      }
    }
  })
}

const open = (type: string, editData?: KeyValue) => {
  data.show = true
  getAllTag()
  data.status = hostStatusEnum.unTest
  data.loading = false
  if (type === 'update' && data) {
    data.title = t('components.AddHost.5mphy3snzrk0')
    Object.assign(data.formData, editData)
    if (data.formData.isRemember) {
      data.formData.password = data.emptyPwd
      // get password
      getHostPassword(data.formData.hostId)
    }
  } else {
    data.title = t('components.AddHost.5mphy3snz5k0')
    Object.assign(data.formData, {
      hostId: '',
      privateIp: '',
      publicIp: '',
      password: '',
      isRemember: false,
      port: 22,
      remark: ''
    })
  }
}

const handleBlur = () => {
  if (!data.formData.name) {
    data.formData.name = data.formData.publicIp
  }
}

defineExpose({
  open
})

</script>
