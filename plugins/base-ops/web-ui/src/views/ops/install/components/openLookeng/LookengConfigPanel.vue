<template>
  <a-form :model="data.form" :rules="rules" auto-label-width :style="{ width: '850px' }" ref="formRef">
    <a-divider orientation="left">{{ $t('components.openLooKeng.5mpiji1qpcc0') }}</a-divider>
    <a-form-item field="hostId" :label="$t('components.openLooKeng.5mpiji1qpcc1')">
      <a-select :loading="hostListLoading" v-model="data.form.hostId" @change="changeHostId(false)"
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
      <a-select :loading="installUserLoading" v-model="data.form.installUsername" @change="onUserChange">
        <a-option v-for="item in userListByHost" :key="item.hostUserId" :value="item.username">{{
            item.username
          }}
        </a-option>
      </a-select>
    </a-form-item>
    <a-form-item v-if="!data.host.isHostRemPwd" field="rootPassword"
                 :label="$t('components.openLooKeng.5mpiji1qpcc3')">
      <a-input-password v-model.trim="data.form.rootPassword" :placeholder="$t('components.openLooKeng.5mpiji1qpcc4')"
                        allow-clear/>
    </a-form-item>
    <a-form-item field="installPath" :label="$t('components.openLooKeng.5mpiji1qpcc5')">
      <a-input v-model.trim="data.form.installPath" :placeholder="$t('components.openLooKeng.5mpiji1qpcc6')"/>
    </a-form-item>
    <a-form-item field="uploadPath" :label="$t('components.openLooKeng.5mpiji1qpcc43')"
                 :extra="$t('components.openLooKeng.5mpiji1qpcc45')">
      <a-input v-model.trim="data.form.uploadPath" :placeholder="$t('components.openLooKeng.5mpiji1qpcc50')"/>
    </a-form-item>
    <a-divider orientation="left">{{ $t('components.openLooKeng.5mpiji1qpcc7') }}</a-divider>
    <a-form-item field="olkTarId" :label="$t('components.openLooKeng.5mpiji1qpcc8')">
      <a-select v-model="data.form.olkTarId" :placeholder="$t('components.openLooKeng.5mpiji1qpcc9')"
                :loading="packageLoading" class="mr-s">
        <a-option v-for="item in data.olkTarList" :key="item.packageId" :label="item.packagePath.name"
                  :value="item.packageId"/>
      </a-select>
      <a-button type="primary" @click="onAddOlkTar">+</a-button>
    </a-form-item>
    <a-form-item field="olkPort" :label="$t('components.openLooKeng.5mpiji1qpcc10')">
      <a-input-number v-model="data.form.olkPort" :placeholder="$t('components.openLooKeng.5mpiji1qpcc11')" :min="0" :max="65535"/>
    </a-form-item>
    <a-collapse :bordered="false">
      <a-collapse-item :header="$t('components.openLooKeng.5mpiji1qpcc52')" key="1">
        <a-form-item field="xmx" label="xmx">
          <a-input v-model="data.form.olkParamConfig.xmx" :placeholder="$t('components.openLooKeng.5mpiji1qpcc53')"/>
        </a-form-item>
        <a-form-item field="maxMemory" label="MaxMemory">
          <a-input v-model="data.form.olkParamConfig.maxMemory" :placeholder="$t('components.openLooKeng.5mpiji1qpcc54')"/>
        </a-form-item>
        <a-form-item field="maxTotalMemory" label="MaxTotalMemory">
          <a-input v-model="data.form.olkParamConfig.maxTotalMemory"
                   :placeholder="$t('components.openLooKeng.5mpiji1qpcc55')"/>
        </a-form-item>
        <a-form-item field="maxMemoryPer" label="MaxMemoryPer">
          <a-input v-model="data.form.olkParamConfig.maxMemoryPer"
                   :placeholder="$t('components.openLooKeng.5mpiji1qpcc56')"/>
        </a-form-item>
        <a-form-item field="maxTotalMemoryPer" label="MaxTotalMemoryPer">
          <a-input v-model="data.form.olkParamConfig.maxTotalMemoryPer"
                   :placeholder="$t('components.openLooKeng.5mpiji1qpcc57')"/>
        </a-form-item>
      </a-collapse-item>
    </a-collapse>
  </a-form>
  <add-package-dlg ref="addPackageRef" @finish="refreshPackageList"/>
</template>
<script setup lang="ts">
import { computed, inject, onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { PackageType } from '@/types/resource/package'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useOpsStore } from '@/store'
import { useI18n } from 'vue-i18n'
import { OlkParamConfig, OpenLookengInstallConfig } from '@/types/ops/install'
import { hostListAll, hostPing, hostUserListAll, packageListAll, portUsed } from '@/api/ops'
import AddPackageDlg from '@/views/monitor/packageManage/AddPackageDlg.vue'
import { encryptPassword } from '@/utils/jsencrypt'

const { t } = useI18n()
const installStore = useOpsStore()

const data = reactive<KeyValue>({
  form: {
    hostId: '',
    installUserId: '',
    password: '',
    rootPassword: '',
    installPath: '/data/install',
    uploadPath: '/data/tar',
    olkTarId: '',
    olkPort: 2345,
    olkParamConfig: {
      xmx: '16G',
      maxMemory: '50GB',
      maxTotalMemory: '50GB',
      maxMemoryPer: '10GB',
      maxTotalMemoryPer: '10GB'
    } as OlkParamConfig,
    needEncrypt: false
  },
  host: {
    privateIp: '',
    publicIp: '',
    isHostRemPwd: false,
    hostPort: 22,
    cpuArch: '',
    os: ''
  },
  olkTarList: []
})
const hostListLoading = ref<boolean>(false)
const installUserLoading = ref<boolean>(false)
const packageLoading = ref<boolean>(false)
const hostList = ref<KeyValue[]>([])
const hostObj = ref<KeyValue>({})

const addPackageRef = ref<null | InstanceType<typeof AddPackageDlg>>(null)
const onAddOlkTar = () => {
  addPackageRef.value?.open('create', { type: PackageType.OPENLOOKENG })
}

const initData = () => {
  const config = JSON.parse(JSON.stringify(installStore.openLookengInstallConfig))
  if (Object.keys(config).length <= 0) {
    return
  }
  data.form.hostId = config.olkInstallHostId
  data.form.installUsername = config.olkInstallUsername
  data.form.password = config.olkInstallPassword
  data.form.rootPassword = config.olkRootPassword
  data.form.installPath = config.olkInstallPath
  data.form.uploadPath = config.olkUploadPath
  data.form.olkTarId = config.olkTarId
  data.form.olkPort = config.olkPort
  data.form.olkParamConfig = config.olkParamConfig
  data.form.needEncrypt = config.olkNeedEncrypt
}

const saveStore = () => {
  const param = {
    olkInstallHostId: data.form.hostId,
    olkInstallUsername: data.form.installUsername,
    olkInstallPassword: data.form.password,
    olkRootPassword: data.form.rootPassword,
    olkInstallPath: data.form.installPath,
    olkUploadPath: data.form.uploadPath,
    olkTarId: data.form.olkTarId,
    olkPort: data.form.olkPort,
    olkNeedEncrypt: data.form.needEncrypt,
    olkParamConfig: data.form.olkParamConfig
  }
  installStore.setOpenLookengConfig(param as OpenLookengInstallConfig)
}

const getHostList = (isInit: boolean) => {
  hostListLoading.value = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      hostList.value = []
      hostList.value = res.data
      res.data.forEach((item: KeyValue) => {
        hostObj.value[item.hostId] = item
      })
      if (!data.form.hostId) {
        data.form.hostId = hostList.value[0].hostId
        data.host.privateIp = hostList.value[0].privateIp
        data.host.publicIp = hostList.value[0].publicIp
      } else {
        const getOldHost = hostList.value.find((item: KeyValue) => {
          return item.hostId === data.form.hostId
        })
        if (!getOldHost) {
          data.form.hostId = hostList.value[0].hostId
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
  if (!data.form.hostId) {
    return
  }
  if (hostObj.value[data.form.hostId]) {
    data.host.privateIp = hostObj.value[data.form.hostId].privateIp
    data.host.publicIp = hostObj.value[data.form.hostId].publicIp
    data.host.hostPort = hostObj.value[data.form.hostId].port
    data.host.isHostRemPwd = hostObj.value[data.form.hostId].isRemember
    data.host.cpuArch = hostObj.value[data.form.hostId].cpuArch
    data.host.os = hostObj.value[data.form.hostId].os
    data.form.needEncrypt = !hostObj.value[data.form.hostId].isRemember
  }
  listUserByHost(isInit)
}

const listUserByHost = (isInit: boolean) => {
  installUserLoading.value = true
  hostUserListAll(data.form.hostId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      userListByHost.value = []
      userListByHost.value = res.data
      if (isInit && data.form.installUsername) {
        return
      }
      if (userListByHost.value.length) {
        data.form.installUsername = userListByHost.value[0].username
        data.form.password = userListByHost.value[0].password
      } else {
        data.form.installUsername = ''
        data.form.password = ''
      }
    } else {
      Message.error('Failed to obtain user data from the host')
    }
  }).finally(() => {
    installUserLoading.value = false
  })
}

const refreshPackageList = (isInit: boolean) => {
  packageLoading.value = true
  packageListAll({ type: PackageType.OPENLOOKENG }).then(res => {
    data.olkTarList = res.data
    if (isInit && data.form.olkTarId) {
      return
    }
    if (data.olkTarList.length > 0) {
      const item = data.olkTarList.find((item: KeyValue) => !!item.packagePath?.name)
      if (item) {
        data.form.olkTarId = item.packageId
      } else {
        data.form.olkTarId = ''
      }
    } else {
      data.form.olkTarId = ''
    }
  }).finally(() => {
    packageLoading.value = false
  })
}

onMounted(() => {
  initData()
  getHostList(true)
})

const validatePort = async (port: number) => {
  let encryptPwd = ''
  if (!data.host.isHostRemPwd) {
    encryptPwd = await encryptPassword(data.form.rootPassword)
  } else {
    const result = userListByHost.value.find((item: KeyValue) => item.username === 'root')
    if (result) {
      encryptPwd = result.password
    }
  }
  const portParam = {
    port: port,
    rootPassword: encryptPwd
  }
  const portValid: KeyValue = await portUsed(data.form.hostId, portParam)
  if (Number(portValid.code) === 200) {
    return portValid.data
  }
  return false
}

const loadingFunc = inject<any>('loading')
const formRef = ref<FormInstance>()

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

const validateSpecialFields = async () => {
  let result = true
  formRef.value?.clearValidate()
  const validMethodArr = []
  let encryptPwd = ''
  if (!data.host.isHostRemPwd) {
    encryptPwd = await encryptPassword(data.form.rootPassword)
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
  validMethodArr.push(validatePort(data.form.olkPort))
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
          message: data.form.port + t('enterprise.NodeConfig.else11')
        }
      })
      result = false
    }
  }

  return result
}

const rules = computed(() => {
  return {
    hostId: { required: true, message: t('simpleInstall.index.5mpn813gukw0') },
    installUsername: { required: true, message: t('components.openLooKeng.5mpiji1qpcc12') },
    rootPassword: [
      { required: true, message: t('components.openLooKeng.5mpiji1qpcc13') }
    ],
    installPath: [
      { required: true, message: t('components.openLooKeng.5mpiji1qpcc14') }
    ],
    uploadPath: [
      { required: true, message: t('components.openLooKeng.5mpiji1qpcc50') }
    ],
    olkPort: [{
      required: true, message: t('components.openLooKeng.5mpiji1qpcc15')
    }],
    olkTarId: { required: true, message: t('components.openLooKeng.5mpiji1qpcc16') }
  }
})

const onUserChange = () => {
  const user = userListByHost.value.find(item => item.username === data.installUsername)
  if (user) {
    data.password = user.password
  }
}

defineExpose({
  saveStore,
  beforeConfirm
})
</script>
