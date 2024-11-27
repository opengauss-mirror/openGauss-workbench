<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close">
    <a-form v-if="data.hostList.length" :model="data.formData" ref="formRef" :label-col="{ style: { width: '100px' } }"
      :rules="formRules" auto-label-width>
      <a-form-item :label="$t('customControl.HostPwdDlg.5mplfut5bj80')">
        <a-select :loading="data.hostListLoading" v-model="data.formData.hostId"
          :placeholder="$t('customControl.HostPwdDlg.else2')" @change="hostChange">
          <a-option v-for="(item, index) in data.hostList" :key="index" :label="item.label" :value="item.hostId" />
        </a-select>
      </a-form-item>
      <a-form-item :label="$t('customControl.HostPwdDlg.user')">
        <a-select :loading="data.userListLoading" v-model="data.formData.username"
          :placeholder="$t('customControl.HostPwdDlg.userPlaceholder')" @change="userChange">
          <a-option v-for="(item, index) in data.userList" :key="index" :label="item.username" :value="item.username" />
        </a-select>
      </a-form-item>
    </a-form>
    <label v-else>{{ $t('customControl.HostPwdDlg.noDevice') }}</label>
  </a-modal>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref, computed } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { hostListAll, hostPing, hostUserListAll } from '@/api/ops'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: t('customControl.HostPwdDlg.5mplfut5lgo0'),
  loading: false,
  hostListLoading: false,
  userListLoading: false,
  excludeHosts: [],
  hostList: [],
  userList: [],
  hostObj: {},
  isShowPwd: false,
  hideCancel: false,
  formData: {
    hostId: '',
    username: '',
    password: ''
  }
})

const formRules = computed(() => {
  return {
    password: [
      { required: true, message: t('customControl.HostPwdDlg.5mplfut5lus0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ]
  }
})

const formRef = ref<null | FormInstance>(null)
const close = () => {
  data.show = false
  data.hostList = []
  data.excludeHosts = []
  data.formData.hostId = ''
  data.formData.password = ''
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const emits = defineEmits([`finish`])

const handleOk = () => {
  if (data.formData.hostId) {
    formRef.value?.validate().then(async result => {
      if (!result) {
        const encryptPwd = await encryptPassword(data.formData.password)
        console.log(currentHost)
        const { privateIp, publicIp, port } = currentHost
        const { username, password } = currentUser
        const hostParam = {
          privateIp, publicIp, port,username, password:password||undefined
        }
        data.loading = true
        try {
          const passwordValid: KeyValue = await hostPing(hostParam)
          if (Number(passwordValid.code) !== 200) {
            formRef.value?.setFields({
              password: {
                status: 'error',
                message: t('enterprise.NodeConfig.else8')
              }
            })
          } else {
            emits(`finish`, {
              hostId: data.formData.hostId,
              username: data.formData.username,
              password: encryptPwd
            })
            close()
          }
        } catch (err: any) {
          formRef.value?.setFields({
            password: {
              status: 'error',
              message: t('enterprise.NodeConfig.else8')
            }
          })
        } finally {
          data.loading = false
        }
      }
    })
  } else {
    emits(`finish`, {
      hostId: '',
      password: ''
    })
    close()
  }
}

const getHostList = () => {
  data.hostListLoading = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.hostList = []
      res.data.forEach((item: KeyValue) => {
        if (!data.excludeHosts.includes(item.hostId)) {
          data.hostObj[item.hostId] = item
          data.hostList.push({
            label: `${item.privateIp}(${item.publicIp})`,
            ...item
          })
        }
      })
      if (data.hostList.length) {
        data.formData.hostId = data.hostList[0].hostId
        hostChange()
      }
    }
  }).finally(() => {
    data.hostListLoading = false
  })
}

let currentHost:KeyValue = {}
const hostChange = async () => {
  data.isShowPwd = !data.hostObj[data.formData.hostId].isRemember
  data.userList = []
  data.userListLoading = true
  console.log(data.hostList,data.formData.hostId)
  currentHost = data.hostList.find(item => item.hostId === data.formData.hostId)
  try {
    const res = await hostUserListAll(data.formData.hostId)
    if(res.code === 200){
      data.userList = res.data
      data.formData.username = res.data[0]?.username
      userChange()
      data.userListLoading = false
    }
  } catch (error) {
    console.log(error)
    data.userListLoading = false
  }
}

let currentUser:KeyValue = {}
const userChange = () => {
  currentUser = data.userList.find(item => item.username === data.formData.username)
}

const open = (hosts: KeyValue[]) => {
  data.show = true
  data.title = t('customControl.HostPwdDlg.5mplfut5lgo0')
  data.isShowPwd = false
  data.excludeHosts = []
  hosts.forEach((item: KeyValue) => {
    data.excludeHosts.push(item.hostId)
  })
  if (!hosts.length) {
    data.hideCancel = true
  }
  getHostList()
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped></style>
