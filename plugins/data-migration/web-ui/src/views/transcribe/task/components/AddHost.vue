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
              $t('当前状态')
          }}
          </div>
          <a-tag
            v-if="data.status === hostStatusEnum.success"
            color="green"
          >{{
              $t('可用')
          }}</a-tag>
          <a-tag
            v-if="data.status === hostStatusEnum.fail"
            color="red"
          >{{
              $t('不可用')
          }}</a-tag>
        </div>
        <div>
          <a-button
            class="mr"
            @click="close"
          >{{
              $t('取消')
          }}</a-button>
          <a-button
            v-if="isAdd"
            :loading="data.testLoading"
            class="mr"
            @click="handleTestHost"
          >{{
              $t('连通性测试')
          }}</a-button>
          <a-button
            :loading="data.loading"
            type="primary"
            @click="submit"
          >{{
              $t('确定')
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
        :label="$t('名称')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.formData.name"
          :placeholder="$t('请输入物理机名称')"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="privateIp"
        :label="$t('内网IP')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.formData.privateIp"
          :disabled="!isAdd"
          :placeholder="$t('请输入内网IP')"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="publicIp"
        :label="$t('外网IP')"
        validate-trigger="blur"
      >
        <a-input
          v-model.trim="data.formData.publicIp"
          disabled
          :placeholder="$t('请输入外网IP')"
          @blur="handleBlur"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="port"
        :label="$t('端口号')"
        validate-trigger="blur"
      >
        <a-input-number
          v-model="data.formData.port"
          :placeholder="$t('请输入端口号')"
          :min="0"
          :max="65535"
        />
      </a-form-item>
      <a-form-item
        field="username"
        :label="$t('用户名')"
        validate-trigger="blur"
        v-if="isAdd"
      >
        <a-input
          v-model.trim="data.formData.username"
          :placeholder="$t('请输入用户名称')"
        ></a-input>
      </a-form-item>
      <a-form-item
        v-if="isAdd"
        field="password"
        :label="$t('密码')"
        validate-trigger="blur"
      >
        <a-input-password
          v-model="data.formData.password"
          :placeholder="$t('请输入密码')"
          @focus="passwordFocus"
          @blur="passwordBlur"
          :invisible-button="data.formData.password !== data.emptyPwd"
          allow-clear
          ref="formPwdRef"
        />
      </a-form-item>
      <a-form-item
        field="tags"
        :label="$t('标签')"
      >
        <a-select
          :loading="data.tagsLoading"
          v-model="data.formData.tags"
          :placeholder="$t('请输入或者选择标签')"
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
      <a-form-item :label="$t('备注')">
        <a-textarea
          v-model.trim="data.formData.remark"
          :placeholder="$t('请输入备注')"
        ></a-textarea>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, toRaw, computed } from 'vue'
// /openGauss-datakit/visualtool-ui/src/api/ops
import { addHost, hostPing, hostTagListAll, hostUserListAll } from '@/api/playback'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
const hostStatusEnum = {
  unTest: -1,
  success: 1,
  fail: 0
}

const data = reactive({
  show: false,
  title: t('新增物理机'),
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
    isRemember: true,
    tags: [],
    remark: ''
  }
})

const formRules = computed(() => {
  return {
    name: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.namePlaceholder') },
      {
        validator: (value, cb) => {
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
        validator: (value, cb) => {
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
        validator: (value, cb) => {
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
    username: [
      { required: true, 'validate-trigger': 'blur', message: t('components.AddHost.usernamePlaceholder') },
      {
        validator: (value, cb) => {
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
        editHost(data.formData.hostId, param).then((res) => {
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
            editHost(data.formData.hostId, param).then((res) => {
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
            addHost(param).then((res) => {
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

const tagsChange = (val) => {
  data.formData.tags = val.filter((item) => {
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
        isRemember: true,
        remark: data.formData.remark,
        username: 'root'
      })

      hostPing(toRaw(param)).then((res) => {
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
  hostTagListAll().then((res) => {
    if (Number(res.code) === 200) {
      data.tagsList = []
      res.data.forEach((item) => {
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

const getHostPassword = (hostId) => {
  hostUserListAll(hostId).then((res) => {
    if (Number(res.code) === 200) {
      const rootObj = res.data.find((item) => {
        return item.username === 'root'
      })
      if (rootObj) {
        data.oldPwd = rootObj.password
      }
    }
  })
}

const isAdd = ref(true)
const open = (type, publicIp) => {
  data.show = true
  getAllTag()
  data.status = hostStatusEnum.unTest
  data.loading = false
  isAdd.value = true
  data.title = t('components.AddHost.5mphy3snz5k0')
  console.log(publicIp)
  Object.assign(data.formData, {
    hostId: '',
    privateIp: '',
    publicIp: publicIp,
    password: '',
    isRemember: true,
    port: 22,
    remark: ''
  })
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
