<template>
  <div class="basic-info-c">
    <a-form :model="data" :rules="rules" auto-label-width :style="{ width: '850px' }" ref="formRef">
      <a-divider orientation="left">{{ $t('components.openLooKeng.5mpiji1qpcc0') }}</a-divider>
      <a-form-item field="name" :label="$t('components.openLooKeng.5mpiji1qpcb0')"
                   :extra="$t('components.openLooKeng.5mpiji1qpcb6')">
        <a-input v-model="data.name" :placeholder="$t('components.openLooKeng.5mpiji1qpcb1')"/>
      </a-form-item>
      <a-form-item field="remark" :label="$t('components.openLooKeng.5mpiji1qpcb2')">
        <a-textarea v-model="data.remark" :placeholder="$t('components.openLooKeng.5mpiji1qpcb3')"
                    :auto-size="{minRows:3, maxRows:3}"/>
      </a-form-item>
      <a-divider orientation="left">{{ $t('components.openLooKeng.5mpiji1qpbc1') }}</a-divider>
      <a-form-item field="hostId" :label="$t('components.openLooKeng.5mpiji1qpcc1')">
        <a-select :loading="hostListLoading" v-model="data.hostId" @change="changeHostId(false)"
                  :placeholder="$t('simple.InstallConfig.5mpmu0laqss0')">
          <a-option v-for="item in hostList" :key="item.hostId" :value="item.hostId">{{
              item.privateIp
              + '(' +
              (item.publicIp ? item.publicIp : '--') + ')'
            }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="installUsername" :label="$t('components.openLooKeng.5mpiji1qpcc2')">
        <a-select :loading="installUserLoading" v-model:model-value="data.installUsername" @change="onUserChange">
          <a-option v-for="item in userListByHost" :key="item.hostUserId" :value="item.username">{{
              item.username
            }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item v-if="!data.host.isHostRemPwd"
                   field="rootPassword" :label="$t('components.openLooKeng.5mpiji1qpcc3')">
        <a-input-password v-model.trim="data.rootPassword" :placeholder="$t('components.openLooKeng.5mpiji1qpcc4')"
                          allow-clear/>
      </a-form-item>
      <a-form-item field="dadTarId" :label="$t('components.openLooKeng.5mpiji1qpcb4')">
        <a-select v-model="data.dadTarId" :placeholder="$t('components.openLooKeng.5mpiji1qpcb5')"
                  :loading="packageLoading" class="mr-s">
          <a-option v-for="item in data.dadTarList" :key="item.packageId" :label="item.packagePath?.name"
                    :value="item.packageId"/>
        </a-select>
        <a-button type="primary" @click="onAddDadTar">+</a-button>
      </a-form-item>
      <a-form-item field="installPath" :label="$t('components.openLooKeng.5mpiji1qpcc5')"
                   :extra="$t('components.openLooKeng.5mpiji1qpcc21')">
        <a-input v-model.trim="data.installPath" :placeholder="$t('components.openLooKeng.5mpiji1qpcc6')"/>
      </a-form-item>
      <a-form-item field="port" :label="$t('components.openLooKeng.5mpiji1qpcc40')">
        <a-input-number v-model="data.port" :placeholder="$t('components.openLooKeng.5mpiji1qpcc41')" :min="0" :max="65535"/>
      </a-form-item>
    </a-form>
    <add-package-dlg ref="addPackageRef" @finish="refreshPackageList"/>
  </div>
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useOpsStore } from '@/store'
import { computed, inject, onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import AddPackageDlg from '@/views/monitor/packageManage/AddPackageDlg.vue'
import { PackageType } from '@/types/resource/package'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { OpenLookengInstallConfig } from '@/types/ops/install'
import { hostListAll, hostPing, hostUserListAll, packageListAll, portUsed } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { encryptPassword } from '@/utils/jsencrypt'

const { t } = useI18n()
const installStore = useOpsStore()

const data = reactive<KeyValue>({
  name: '',
  dadTarId: '',
  remark: '',
  hostId: '',
  installUsername: '',
  password: '',
  rootPassword: '',
  installPath: '/data/install',
  port: 8080,
  needEncrypt: false,
  host: {
    privateIp: '',
    publicIp: '',
    hostPort: 22,
    isHostRemPwd: true,
    cpuArch: '',
    os: ''
  },
  dadTarList: []
})

const addPackageRef = ref<null | InstanceType<typeof AddPackageDlg>>(null)
const loadingFunc = inject<any>('loading')
const packageLoading = ref<boolean>(false)
const formRef = ref<FormInstance>()

const onAddDadTar = () => {
  addPackageRef.value?.open('create', { type: PackageType.DISTRIBUTE_DEPLOY })
}

onMounted(() => {
  initData()
  getHostList(true)
})

const initData = () => {
  const config = JSON.parse(JSON.stringify(installStore.openLookengInstallConfig))
  if (Object.keys(config).length <= 0) {
    return
  }
  data.name = config.name
  data.dadTarId = config.dadTarId
  data.remark = config.remark
  data.hostId = config.dadInstallHostId
  data.installUsername = config.dadInstallUsername
  data.password = config.dadInstallPassword
  data.rootPassword = config.dadRootPassword
  data.installPath = config.dadInstallPath
  data.port = config.dadPort
  data.needEncrypt = config.dadNeedEncrypt
}

const saveStore = () => {
  const param = {
    name: data.name,
    dadTarId: data.dadTarId,
    remark: data.remark,
    dadInstallHostId: data.hostId,
    dadInstallUsername: data.installUsername,
    dadInstallPassword: data.password,
    dadRootPassword: data.rootPassword,
    dadInstallPath: data.installPath,
    dadPort: data.port,
    dadNeedEncrypt: data.needEncrypt
  }
  installStore.setOpenLookengConfig(param as OpenLookengInstallConfig)
}

const refreshPackageList = (isInit: boolean) => {
  packageLoading.value = true
  packageListAll({ type: PackageType.DISTRIBUTE_DEPLOY }).then(res => {
    data.dadTarList = res.data
    if (isInit && data.dadTarId) {
      return
    }
    if (data.dadTarList.length > 0) {
      const item = data.dadTarList.find((item: KeyValue) => !!item.packagePath?.name)
      if (item) {
        data.dadTarId = item.packageId
      } else {
        data.dadTarId = ''
      }
    } else {
      data.dadTarId = ''
    }
  }).finally(() => {
    packageLoading.value = false
  })
}

const validatePort = async () => {
  let encryptPwd = ''
  if (!data.host.isHostRemPwd) {
    encryptPwd = await encryptPassword(data.rootPassword)
  } else {
    const result = userListByHost.value.find((item: KeyValue) => item.username === 'root')
    if (result) {
      encryptPwd = result.password
    }
  }
  const portParam = {
    port: data.port,
    rootPassword: encryptPwd
  }
  const portValid: KeyValue = await portUsed(data.hostId, portParam)
  if (Number(portValid.code) === 200) {
    return portValid.data
  }
  return false
}

const onUserChange = () => {
  const user = userListByHost.value.find(item => item.username === data.installUsername)
  if (user) {
    data.password = user.password
    return
  }
}

const validateSpecialFields = async () => {
  let result = true
  formRef.value?.clearValidate()
  // create user mode we don't need to check
  const validMethodArr = []
  let encryptPwd = ''
  if (!data.host.isHostRemPwd) {
    encryptPwd = await encryptPassword(data.rootPassword)
  } else {
    const result = userListByHost.value.find((item: KeyValue) => item.username === 'root')
    if (result) {
      encryptPwd = result.password
    }
  }
  // password validate
  try {
    const param = {
      publicIp: data.host.publicIp,
      password: encryptPwd,
      username: 'root',
      port: data.host.hostPort
    }
    const passwordValid: KeyValue = await hostPing(param)
    if (Number(passwordValid.code) !== 200) {
      formRef.value?.setFields({
        rootPassword: {
          status: 'error',
          message: t('components.openLooKeng.5mpiji1qpcc42')
        }
      })
      result = false
    }
  } catch (err: any) {
    formRef.value?.setFields({
      rootPassword: {
        status: 'error',
        message: t('components.openLooKeng.5mpiji1qpcc42')
      }
    })
    result = false
  }
  if (!result) {
    return result
  }
  //  cluster port is used
  validMethodArr.push(validatePort())
  if (validMethodArr.length) {
    let validResult
    try {
      validResult = await Promise.all(validMethodArr)
    } catch (err: any) {
      return false
    }
    if (validResult[0]) {
      // port valid
      formRef.value?.setFields({
        port: {
          status: 'error',
          message: data.port + t('enterprise.NodeConfig.else11')
        }
      })
      result = false
    }
  }

  return result
}

const beforeConfirm = async (): Promise<boolean> => {
  let validRes = true
  const tempRes = await formRef.value?.validate()
  if (tempRes) {
    validRes = false
  }
  if (validRes) {
    loadingFunc.toLoading()
    validRes = await validateSpecialFields()
  }
  if (validRes) {
    saveStore()
    loadingFunc.cancelLoading()
    return true
  }
  loadingFunc.cancelLoading()
  return false
}

const hostListLoading = ref<boolean>(false)
const installUserLoading = ref<boolean>(false)
const hostList = ref<KeyValue[]>([])
const hostObj = ref<KeyValue>({})

const getHostList = (isInit: boolean) => {
  hostListLoading.value = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      hostList.value = []
      hostList.value = res.data
      res.data.forEach((item: KeyValue) => {
        hostObj.value[item.hostId] = item
      })
      if (!data.hostId) {
        data.hostId = hostList.value[0].hostId
        data.host.privateIp = hostList.value[0].privateIp
        data.host.publicIp = hostList.value[0].publicIp
      } else {
        const getOldHost = hostList.value.find((item: KeyValue) => {
          return item.hostId === data.hostId
        })
        if (!getOldHost) {
          data.hostId = hostList.value[0].hostId
          data.host.privateIp = hostList.value[0].privateIp
          data.host.publicIp = hostList.value[0].publicIp
        }
      }
      changeHostId(isInit)
      refreshPackageList(isInit)
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    hostListLoading.value = false
  })
}

const userListByHost = ref<KeyValue[]>([])

const changeHostId = (isInit: boolean) => {
  if (!data.hostId) {
    return
  }
  if (hostObj.value[data.hostId]) {
    data.host.privateIp = hostObj.value[data.hostId].privateIp
    data.host.publicIp = hostObj.value[data.hostId].publicIp
    data.host.hostPort = hostObj.value[data.hostId].port
    data.host.isHostRemPwd = hostObj.value[data.hostId].isRemember
    data.host.os = hostObj.value[data.hostId].os
    data.host.cpuArch = hostObj.value[data.hostId].cpuArch
    data.needEncrypt = !hostObj.value[data.hostId].isRemember
  }
  installUserLoading.value = true
  hostUserListAll(data.hostId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      userListByHost.value = []
      userListByHost.value = res.data
      if (isInit && data.installUsername) {
        return
      }
      if (userListByHost.value.length) {
        data.installUsername = userListByHost.value[0].username
        data.password = userListByHost.value[0].password
      } else {
        data.installUsername = ''
        data.password = ''
      }
    } else {
      Message.error('Failed to obtain user data from the host')
    }
  }).finally(() => {
    installUserLoading.value = false
  })
}

const pathValidator = (value: any, cb: any) => {
  return new Promise(resolve => {
    const reg = /^\/([^/\0?]+(\/[^/\0?]+)*(\/[^/\0?]+\?)?)?$/
    const re = new RegExp(reg)
    if (re.test(value)) {
      resolve(true)
    } else {
      cb(t('components.openLooKeng.5mpiji1qpcc38'))
      resolve(false)
    }
  })
}

const rules = computed(() => {
  return {
    name: {
      required: true, message: t('components.openLooKeng.5mpiji1qpcb1')
    },
    port: [{ required: true, message: t('components.openLooKeng.5mpiji1qpcc14') }],
    hostId: { required: true, message: t('simpleInstall.index.5mpn813gukw0') },
    installUsername: { required: true, message: t('components.openLooKeng.5mpiji1qpcc12') },
    rootPassword: { required: true, message: t('components.openLooKeng.5mpiji1qpcc13') },
    installPath: [
      { required: true, message: t('components.openLooKeng.5mpiji1qpcc14') },
      {
        validator: pathValidator
      }
    ],
    dadTarId: { required: true, message: t('components.openLooKeng.5mpiji1qpcc39') }
  }
})

defineExpose({
  saveStore,
  beforeConfirm
})
</script>
<style lang="less" scoped>
.basic-info-c {
  padding: 0 20px 20px 20px;
  height: 100%;
  overflow-y: auto;
}
</style>
