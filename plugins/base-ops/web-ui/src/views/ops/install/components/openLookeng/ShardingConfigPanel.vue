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
      <a-select :loading="installUserLoading" v-model:model-value="data.form.installUsername" @change="onUserChange">
        <a-option v-for="item in userListByHost" :key="item.hostUserId" :value="item.username">{{
            item.username
          }}
        </a-option>
      </a-select>
    </a-form-item>
    <a-form-item v-if="!data.host.isHostRemPwd" field="rootPassword"
                 :label="$t('components.openLooKeng.5mpiji1qpcc3')">
      <a-input-password v-model="data.form.rootPassword" :placeholder="$t('components.openLooKeng.5mpiji1qpcc4')"
                        allow-clear/>
    </a-form-item>
    <a-form-item field="installPath" :label="$t('components.openLooKeng.5mpiji1qpcc5')"
                 :extra="$t('components.openLooKeng.5mpiji1qpcc21')">
      <a-input v-model="data.form.installPath" :placeholder="$t('components.openLooKeng.5mpiji1qpcc6')"/>
    </a-form-item>
    <a-form-item field="uploadPath" :label="$t('components.openLooKeng.5mpiji1qpcc43')"
                 :extra="$t('components.openLooKeng.5mpiji1qpcc45')">
      <a-input v-model="data.form.uploadPath" :placeholder="$t('components.openLooKeng.5mpiji1qpcc44')"/>
    </a-form-item>
    <a-divider orientation="left">{{ $t('components.openLooKeng.5mpiji1qpcc7') }}</a-divider>
    <a-form-item field="shardingTarId" :label="$t('components.openLooKeng.5mpiji1qpcc22')">
      <a-select v-model="data.form.shardingTarId" :placeholder="$t('components.openLooKeng.5mpiji1qpcc23')"
                :loading="packageLoading" class="mr-s">
        <a-option v-for="item in data.shardingTarList" :key="item.packageId" :label="item.packagePath.name"
                  :value="item.packageId"/>
      </a-select>
      <a-button type="primary" @click="onAddShardingTar">+</a-button>
    </a-form-item>
    <a-form-item field="shardingPort" :label="$t('components.openLooKeng.5mpiji1qpcc24')">
      <a-input-number v-model="data.form.shardingPort" :placeholder="$t('components.openLooKeng.5mpiji1qpcc25')" :min="0" :max="65535"/>
    </a-form-item>
    <a-form-item field="zkTarId" :label="$t('components.openLooKeng.5mpiji1qpcc26')">
      <a-select v-model="data.form.zkTarId" :placeholder="$t('components.openLooKeng.5mpiji1qpcc27')"
                :loading="packageLoading" class="mr-s">
        <a-option v-for="item in data.zkTarList" :key="item.packageId" :label="item.packagePath.name"
                  :value="item.packageId"/>
      </a-select>
      <a-button type="primary" @click="onAddZkTar">+</a-button>
    </a-form-item>
    <a-form-item field="zkPort" :label="$t('components.openLooKeng.5mpiji1qpcc28')">
      <a-input-number v-model="data.form.zkPort" :placeholder="$t('components.openLooKeng.5mpiji1qpcc29')" :min="0" :max="65535"/>
    </a-form-item>
    <a-divider orientation="left">{{ $t('components.openLooKeng.5mpiji1qpcc30') }}</a-divider>
    <a-form-item field="dsConfig" :extra="$t('components.openLooKeng.5mpiji1qpcc48')"
                 :label="$t('components.openLooKeng.5mpiji1qpcc46')" :labelCol="{ span: 6, offset: 0 }"
                 labelAlign="left" :colon="false">
      <a-cascader
        path-mode
        multiple
        v-model:model-value="data.form.dsConfig"
        :options="data.dataSourceList"
        :placeholder="$t('components.openLooKeng.5mpiji1qpcc47')"
      />
    </a-form-item>
    <a-form-item field="tableName" :label="$t('components.openLooKeng.5mpiji1qpcc31')">
      <a-input v-model="data.form.tableName" :placeholder="$t('components.openLooKeng.5mpiji1qpcc32')"/>
    </a-form-item>
    <a-form-item field="columns" :label="$t('components.openLooKeng.5mpiji1qpcc33')">
      <a-input v-model="data.form.columns" :placeholder="$t('components.openLooKeng.5mpiji1qpcc34')"/>
    </a-form-item>
  </a-form>
  <add-package-dlg ref="addPackageRef" @finish="refreshPackageList"/>
</template>
<script setup lang="ts">
import { computed, inject, onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { hostListAll, hostPing, hostUserListAll, packageListAll, portUsed } from '@/api/ops'
import { PackageType } from '@/types/resource/package'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { OpenLookengInstallConfig } from '@/types/ops/install'
import { useOpsStore } from '@/store'
import AddPackageDlg from '@/views/monitor/packageManage/AddPackageDlg.vue'
import { encryptPassword } from '@/utils/jsencrypt'
import { dataSourceDbList } from '@/api/modeling'
import {useRouter} from "vue-router";

const { t } = useI18n()
const router = useRouter()
const installStore = useOpsStore()

const data = reactive<KeyValue>({
  form: {
    hostId: '',
    installUsername: '',
    password: '',
    rootPassword: '',
    installPath: '/data/install',
    uploadPath: '/data/tar',
    shardingTarId: '',
    shardingPort: 1234,
    zkTarId: '',
    zkPort: 2181,
    dsConfig: [],
    tableName: '',
    columns: '',
    needEncrypt: false
  },
  host: {
    isHostRemPwd: true,
    privateIp: '',
    publicIp: '',
    hostPort: 22,
    cpuArch: '',
    os: ''
  },
  dataSourceList: [],
  shardingTarList: [],
  zkTarList: []
})

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
  if (data.form.hostId) {
    if (hostObj.value[data.form.hostId]) {
      data.host.privateIp = hostObj.value[data.form.hostId].privateIp
      data.host.publicIp = hostObj.value[data.form.hostId].publicIp
      data.host.hostPort = hostObj.value[data.form.hostId].port
      data.host.isHostRemPwd = hostObj.value[data.form.hostId].isRemember
      data.host.os = hostObj.value[data.form.hostId].os
      data.host.cpuArch = hostObj.value[data.form.hostId].cpuArch
      data.form.needEncrypt = !hostObj.value[data.form.hostId].isRemember
    }
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
}

const addPackageRef = ref<null | InstanceType<typeof AddPackageDlg>>(null)
const onAddShardingTar = () => {
  addPackageRef.value?.open('create', { type: PackageType.SHARDING_PROXY })
}
const onAddZkTar = () => {
  addPackageRef.value?.open('create', { type: PackageType.ZOOKEEPER })
}

const getDsListData = () => {
  dataSourceDbList().then((res: KeyValue) => {
    res.data.forEach((item: KeyValue) => {
      item.value = item.clusterId
      item.label = item.clusterId
      item.clusterNodes && item.clusterNodes.forEach((item2: KeyValue) => {
        item2.label = item2.publicIp
        item2.value = item2.nodeId
        let children = [] as KeyValue[]
        item2.dbName = JSON.parse(item2.dbName)
        item2.dbName.forEach((item3: any) => {
          children.push({
            label: item3,
            value: item3,
            parentId: item2.nodeId
          })
        })
        item2.children = children
      })
      item.children = item.clusterNodes
    })
    data.dataSourceList = res.data
  })
}

onMounted(() => {
  initData()
  getHostList(true)
  getDsListData()
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

const validateDs = (value: any, cb: any) => {
  return new Promise(resolve => {
    if (value.length < 2) {
      cb(t('components.openLooKeng.5mpiji1qpcc47'))
      resolve(false)
    } else {
      resolve(true)
    }
  })
}

const rules = computed(() => {
  return {
    shardingPort: [{
      required: true, message: t('components.openLooKeng.5mpiji1qpcc24')
    }],
    zkPort: [{
      required: true, message: t('components.openLooKeng.5mpiji1qpcc28')
    }],
    hostId: { required: true, message: t('simpleInstall.index.5mpn813gukw0') },
    installUsername: { required: true, message: t('components.openLooKeng.5mpiji1qpcc12') },
    rootPassword: [
      { required: true, message: t('components.openLooKeng.5mpiji1qpcc13') }
    ],
    installPath: [
      { required: true, message: t('components.openLooKeng.5mpiji1qpcc14') }
    ],
    uploadPath: [
      { required: true, message: t('components.openLooKeng.5mpiji1qpcc44') }
    ],
    shardingTarId: { required: true, message: t('components.openLooKeng.5mpiji1qpcc23') },
    zkTarId: { required: true, message: t('components.openLooKeng.5mpiji1qpcc27') },
    tableName: [{ required: true, message: t('components.openLooKeng.5mpiji1qpcc32') }],
    columns: [{ required: true, message: t('components.openLooKeng.5mpiji1qpcc34') }],
    dsConfig: [{ required: true, message: t('components.openLooKeng.5mpiji1qpcc47') }, { validator: validateDs }]
  }
})

const loadingFunc = inject<any>('loading')
const packageLoading = ref<boolean>(false)
const formRef = ref<FormInstance>()

const initData = () => {
  const config = JSON.parse(JSON.stringify(installStore.openLookengInstallConfig))
  if (Object.keys(config).length <= 0) {
    return
  }
  data.form.hostId = config.ssInstallHostId
  data.form.installUsername = config.ssInstallUsername
  data.form.password = config.ssInstallPassword
  data.form.rootPassword = config.ssRootPassword
  data.form.installPath = config.ssInstallPath
  data.form.shardingTarId = config.ssTarId
  data.form.shardingPort = config.ssPort
  data.form.zkTarId = config.zkTarId
  data.form.zkPort = config.zkPort
  data.form.tableName = config.tableName
  data.form.columns = config.columns
  data.form.uploadPath = config.ssUploadPath
  data.form.dsConfig = config.dsConfig
  data.form.needEncrypt = config.ssNeedEncrypt
}

const saveStore = () => {
  const param = {
    ssInstallHostId: data.form.hostId,
    ssInstallUsername: data.form.installUsername,
    ssInstallPassword: data.form.password,
    ssRootPassword: data.form.rootPassword,
    ssInstallPath: data.form.installPath,
    ssTarId: data.form.shardingTarId,
    ssPort: data.form.shardingPort,
    zkTarId: data.form.zkTarId,
    zkPort: data.form.zkPort,
    tableName: data.form.tableName,
    columns: data.form.columns,
    ssUploadPath: data.form.uploadPath,
    dsConfig: data.form.dsConfig,
    ssNeedEncrypt: data.form.needEncrypt
  }
  installStore.setOpenLookengConfig(param as OpenLookengInstallConfig)
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

const refreshPackageList = (isInit: boolean) => {
  packageLoading.value = true
  Promise.all([
    packageListAll({ type: PackageType.SHARDING_PROXY }),
    packageListAll({ type: PackageType.ZOOKEEPER })
  ]).then(res => {
    data.shardingTarList = res[0].data
    data.zkTarList = res[1].data
    if (isInit && data.form.zkTarId && data.form.shardingTarId) {
      return
    }
    if (data.shardingTarList.length > 0) {
      const item = data.shardingTarList.find((item: KeyValue) => !!item.packagePath?.name)
      if (item) {
        data.form.shardingTarId = item.packageId
      } else {
        data.form.shardingTarId = ''
      }
    } else {
      data.form.shardingTarId = ''
    }
    if (data.zkTarList.length > 0) {
      const item = data.zkTarList.find((item: KeyValue) => !!item.packagePath?.name)
      if (item) {
        data.form.zkTarId = item.packageId
      } else {
        data.form.zkTarId = ''
      }
    } else {
      data.form.zkTarId = ''
    }
  }).finally(() => {
    packageLoading.value = false
  })
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
  validMethodArr.push(validatePort(data.form.shardingPort))
  validMethodArr.push(validatePort(data.form.zkPort))
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
        shardingPort: {
          status: 'error',
          message: data.form.shardingPort + t('enterprise.NodeConfig.else11')
        }
      })
      result = false
    }
    if (validResult[1]) {
      // port valid
      formRef.value?.setFields({
        zkPort: {
          status: 'error',
          message: data.form.zkPort + t('enterprise.NodeConfig.else11')
        }
      })
      result = false
    }
  }

  return result
}

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
