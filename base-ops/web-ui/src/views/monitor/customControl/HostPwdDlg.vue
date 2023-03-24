<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close">
    <a-form :model="data.formData" ref="formRef" :label-col="{ style: { width: '100px' } }" :rules="formRules"
      auto-label-width>
      <a-form-item :label="$t('customControl.HostPwdDlg.5mplfut5bj80')">
        <a-select :loading="data.hostListLoading" v-model="data.formData.hostId"
          :placeholder="$t('customControl.index.5mplgrscm4s0')" @change="hostChange">
          <a-option v-for="(item, index) in data.hostList" :key="index" :label="item.label" :value="item.value" />
        </a-select>
      </a-form-item>
      <a-form-item v-if="data.isShowPwd" field="password" :label="$t('customControl.HostPwdDlg.else1')"
        validate-trigger="blur">
        <a-input-password v-model="data.formData.password" :placeholder="$t('customControl.HostPwdDlg.5mplfut5kuc0')"
          allow-clear />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">

import { nextTick, reactive, ref, computed } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { hostListAll, hostPingById } from '@/api/ops'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: t('customControl.HostPwdDlg.5mplfut5lgo0'),
  loading: false,
  hostListLoading: false,
  excludeHosts: [],
  hostList: [],
  hostObj: {},
  isShowPwd: false,
  hideCancel: false,
  formData: {
    hostId: '',
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
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const emits = defineEmits([`finish`])

const handleOk = () => {
  formRef.value?.validate().then(async result => {
    if (!result) {
      const encryptPwd = await encryptPassword(data.formData.password)
      // valid password
      const param = {
        rootPassword: encryptPwd
      }
      data.loading = true
      try {
        const passwordValid: KeyValue = await hostPingById(data.formData.hostId, param)
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
            value: item.hostId
          })
        }
      })
      data.formData.hostId = data.hostList[0].value
      data.isShowPwd = !data.hostObj[data.formData.hostId].isRemember
    }
  }).finally(() => {
    data.hostListLoading = false
  })
}

const hostChange = () => {
  data.isShowPwd = !data.hostObj[data.formData.hostId].isRemember
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
