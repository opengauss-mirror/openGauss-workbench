<template>
  <el-dialog :mask-closable="false" :esc-to-close="false" v-model="data.show" :title="data.title" :z-index="1000"
             v-loading="data.loading" :modal-style="{ width: '650px' }" @cancel="close" @close="close">
    <template #footer>
      <div class="flex-center">
        <div>
          <el-button class="mr" @click="close">{{
              $t('components.AddHost.5mphy3snwxs0')
            }}</el-button>
          <el-button v-if="isAdd" :loading="data.testLoading" class="mr" @click="handleTestHost">{{
              $t('components.AddHost.5mphy3snx3o0')
            }}</el-button>
          <el-button :loading="data.loading" type="primary" @click="submit">{{
              $t('components.AddHost.5mphy3snx7c0')
            }}</el-button>
        </div>
      </div>
    </template>

    <el-form :model="data.formData" ref="formRef" label-position="left" label-width="150px" :rules="formRules">
      <el-form-item prop="name" :label="$t('components.AddHost.name')" >
        <el-input v-model.trim="data.formData.name" :placeholder="$t('components.AddHost.namePlaceholder')"
                  maxlength="100" class="input-width" />
      </el-form-item>
      <el-form-item prop="privateIp" :label="$t('components.AddHost.ipAddress')" >
        <el-input v-model.trim="data.formData.privateIp" :disabled="!isAdd" class="input-width"
                  :placeholder="$t('components.AddHost.5mphy3snxdo0')" />
      </el-form-item>
      <el-form-item prop="publicIp" :label="$t('components.AddHost.5mphy3snxis0')" >
        <el-input v-model.trim="data.formData.publicIp" :disabled="!isAdd" class="input-width"
                  :placeholder="$t('components.AddHost.5mphy3snxmw0')" @blur="handleBlur" />
      </el-form-item>
      <el-form-item prop="port" :label="$t('components.AddHost.5mphy3snxtc0')" >
        <el-input-number v-model="data.formData.port" :placeholder="$t('components.AddHost.5mphy3snxzk0')" min="0"
                         max="65535" class="input-width inner-class" controls-position="right" />
      </el-form-item>
      <el-form-item prop="username" :label="$t('components.AddHost.username')" validate-trigger="blur" v-if="isAdd">
        <el-input v-model.trim="data.formData.username" class="input-width"
                  :placeholder="$t('components.AddHost.usernamePlaceholder')"  maxlength="100" />
      </el-form-item>
      <el-form-item v-if="isAdd" prop="password" :label="$t('components.AddHost.5mphy3sny4w0')" >
        <el-input type="password" v-model="data.formData.password" :placeholder="$t('components.AddHost.5mphy3snyao0')"
                  @focus="passwordFocus" @blur="passwordBlur" class="input-width"
                  :invisible-button="data.formData.password !== data.emptyPwd" clearable ref="formPwdRef" />
      </el-form-item>
      <el-form-item prop="tags" :label="$t('components.AddHost.tags')">
        <el-select :loading="data.tagsLoading" v-model="data.formData.tags" class="input-width"
                   :placeholder="$t('components.AddHost.tagsPlaceholder')" allow-create multiple clearable @change="tagsChange">
          <el-option v-for="item in data.tagsList" :key="item.value" :value="item.value">{{
              item.label
            }}</el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('components.AddHost.5mphy3snysg0')">
        <el-input type="textarea" v-model.trim="data.formData.remark"
                  :placeholder="$t('components.AddHost.5mphy3snyxc0')"  maxlength="255" />
      </el-form-item>
    </el-form>
  </el-dialog>
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
    username: '',
    isRemember: true,
    tags: [],
    remark: ''
  }
})

import { IpRegex } from '@/types/global'
import showMessage from "@/hooks/showMessage";

const formRules = computed(() => {
  return {
    name: [
      { required: true, trigger: 'blur', message: t('components.AddHost.namePlaceholder') },
      {
        validator: (rule: any, value: any, cb: any) => {
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
      { required: true, trigger: ['blur', 'change'], message: t('components.AddHost.5mphy3snxdo0') },
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
            if (IpRegex.ipv4Reg.test(value) || IpRegex.ipv6Reg.test(value)) {
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
      { required: true, trigger: ['blur', 'change'], message: t('components.AddHost.5mphy3snxmw0') },
      {
        validator: (rule: any, value: any, cb: any) => {
          return new Promise(resolve => {
            if (IpRegex.ipv4Reg.test(value) || IpRegex.ipv6Reg.test(value)) {
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
      { required: true, trigger: ['blur', 'change'], message: t('components.AddHost.5mphy3snxzk0') }
    ],
    username: [
      { required: true, trigger: ['blur', 'change'], message: t('components.AddHost.usernamePlaceholder') },
      {
        validator: (rule: any, value: any, cb: any) => {
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
    password: [{ required: true, trigger: ['blur', 'change'], message: t('components.AddHost.5mphy3snyao0') }]
  }
})

const emits = defineEmits([`finish`])
const formRef = ref<null | FormInstance>(null)
const submit = () => {
  formRef.value?.validate().then(result => {
    if (result) {
      data.loading = true
      if (data.formData.hostId) {
        const { privateIp, publicIp, port, remark, username, hostId, password, tags, name } = data.formData
        const param = { privateIp, publicIp, port, remark, hostId, tags, name, password, username }
        editHost(data.formData.hostId, param).then((res: KeyValue) => {
          data.loading = false
          if (Number(res.code) === 200) {
            showMessage('success', t('components.AddHost.editServerSuc'))
            emits(`finish`)
            close()
          }
        }).catch((error) => {
          showMessage('error', error)
          console.log(error)
        }).finally(() => {
          data.loading = false
        })
      }
    } else {
      showMessage('error', t('components.AddHost.unFill'))
    }
  }).catch((error) => {
    showMessage('error', t('components.AddHost.unFill'))
    console.log(error)
  })
}
const close = () => {
  data.show = false
  data.oldPwd = ''
  data.formData.password = ''
  data.formData.username = ''
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
      const { privateIp, publicIp, port, remark, username, hostId, password } = data.formData
      data.testLoading = true
      let encryptPwd
      if (hostId && password === data.emptyPwd) {
        encryptPwd = data.oldPwd
      } else {
        encryptPwd = await encryptPassword(password)
      }
      const param = {}
      Object.assign(param, {
        privateIp,
        publicIp,
        port,
        password: encryptPwd,
        isRemember: true,
        remark,
        username
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

const getHostPassword = async (hostId: string) => {
  try {
    const res: KeyValue = await hostUserListAll(hostId)
    if (Number(res.code) === 200) {
      const userEntity = res.data.find((e: any) => e && e.password)
      if (userEntity) {
        data.formData.username = userEntity.username
        data.formData.password = userEntity.password
      } else {
        Message.error(t('components.AddHost.noPasswordTip'))
      }
    }
  } catch (error) {

  }
}

const isAdd = ref(true)
const open = (type: string, editData?: KeyValue) => {
  data.show = true
  getAllTag()
  data.status = hostStatusEnum.unTest
  data.loading = false
  isAdd.value = true
  if (type === 'update' && data) {
    isAdd.value = false
    data.title = t('components.AddHost.5mphy3snzrk0')
    Object.assign(data.formData, editData)
    getHostPassword(data.formData.hostId)
  } else {
    data.title = t('components.AddHost.5mphy3snz5k0')
    Object.assign(data.formData, {
      hostId: '',
      privateIp: '',
      publicIp: '',
      password: '',
      isRemember: true,
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

<style scoped>
.input-width {
  width: 400px;
}
.inner-class :deep(.el-input__inner) {
  text-align: left !important;
  padding-right: 30px;
}
</style>
