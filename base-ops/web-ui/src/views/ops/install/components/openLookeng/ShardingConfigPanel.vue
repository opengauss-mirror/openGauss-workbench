<template>
  <a-form :model="data.form" :rules="rules" auto-label-width :style="{ width: '850px' }" ref="formRef">
    <a-divider orientation="left">基础信息</a-divider>
    <a-form-item field="hostId" label="部署主机IP">
      <a-select :loading="hostListLoading" v-model="data.form.hostId" @change="changeHostId"
                :placeholder="$t('simple.InstallConfig.5mpmu0laqss0')">
        <a-option v-for="item in hostList" :key="item.hostId" :value="item.hostId">{{
            item.privateIp
            + '(' +
            (item.publicIp ? item.publicIp : '--') + ')'
          }}
        </a-option>
      </a-select>
    </a-form-item>
    <a-form-item field="installUserId" label="用户名" extra="输入后按下回车，可以增加新的用户">
      <a-select :loading="installUserLoading" v-model="data.form.installUserId" allow-create>
        <a-option v-for="item in userListByHost" :key="item.hostUserId" :value="item.hostUserId">{{
            item.username
          }}
        </a-option>
      </a-select>
    </a-form-item>
    <a-form-item field="password" label="密码">
      <a-input-password v-model="data.form.password" placeholder="请输入密码" allow-clear/>
    </a-form-item>
    <a-form-item field="installPath" label="安装路径" extra="安装路径不存时将会自动创建">
      <a-input v-model="data.form.installPath" placeholder="请输入安装路径"/>
    </a-form-item>
    <a-divider orientation="left">安装包信息</a-divider>
    <a-form-item field="shardingTarId" label="Sharding安装包路径">
      <a-select v-model="data.form.shardingTarId" placeholder="请选择Sharding安装包">
        <a-option v-for="item in data.shardingTarList" :key="item.id" :label="item.name" :value="item.id"/>
      </a-select>
    </a-form-item>
    <a-form-item field="shardingPort" label="Sharding绑定端口">
      <a-input v-model="data.form.shardingPort" placeholder="请输入Sharding绑定端口"/>
    </a-form-item>
    <a-form-item field="zkTarId" label="Zookeeper安装包路径">
      <a-select v-model="data.form.zkTarId" placeholder="请选择Zookeeper安装包">
        <a-option v-for="item in data.zookeeperTarList" :key="item.id" :label="item.name" :value="item.id"/>
      </a-select>
    </a-form-item>
    <a-form-item field="zkPort" label="Zookeeper绑定端口">
      <a-input v-model="data.form.zkPort" placeholder="请输入Zookeeper绑定端口"/>
    </a-form-item>
    <a-divider orientation="left">分片配置</a-divider>
    <a-form-item field="tableName" label="分片表名">
      <a-input v-model="data.form.tableName" placeholder="请输入分片表名，以逗号,分割"/>
    </a-form-item>
    <a-form-item field="column" label="分片键">
      <a-input v-model="data.form.column" placeholder="请输入分片键名，以逗号,分割"/>
    </a-form-item>
  </a-form>
</template>
<script setup lang="ts">
import { inject, onMounted, reactive, ref, watch } from 'vue'
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import {
  mockHostListAll,
  mockPathEmpty,
  mockUserListAll,
  mockPortUsed,
  mockHostPingById,
  mockEncryptPassword
} from '@/api/ops/mock'
import { TarType } from '@/types/resource/package'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { OpenLookengInstallConfig } from '@/types/ops/install'
import { useOpsStore } from '@/store'

const { t } = useI18n()
const installStore = useOpsStore()
const props = defineProps({
  tarMap: {
    type: Object,
    default: () => {
      return {}
    }
  }
})

const data = reactive<KeyValue>({
  form: {
    hostId: '',
    installUserId: '',
    password: '',
    installPath: '/opt/software/shard',
    shardingTarId: '',
    shardingPort: 1234,
    zkTarId: '',
    zkPort: 2345,
    privateIp: '',
    publicIp: '',
    tableName: '',
    column: ''
  },
  shardingTarList: [],
  zkTarList: []
})

const hostListLoading = ref<boolean>(false)
const installUserLoading = ref<boolean>(false)
const hostList = ref<KeyValue[]>([])
const hostObj = ref<KeyValue>({})

const getHostList = () => {
  hostListLoading.value = true
  mockHostListAll().then((res) => {
    if (Number(res.code) === 200) {
      hostList.value = []
      hostList.value = res.data
      res.data.forEach((item: KeyValue) => {
        hostObj.value[item.hostId] = item
      })
      if (!data.form.hostId) {
        data.form.hostId = hostList.value[0].hostId
        data.form.privateIp = hostList.value[0].privateIp
        data.form.publicIp = hostList.value[0].publicIp
      } else {
        const getOldHost = hostList.value.find((item: KeyValue) => {
          return item.hostId === data.form.hostId
        })
        if (!getOldHost) {
          data.form.hostId = hostList.value[0].hostId
          data.form.privateIp = hostList.value[0].privateIp
          data.form.publicIp = hostList.value[0].publicIp
        }
      }
      changeHostId()
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    hostListLoading.value = false
  })
}

const userListByHost = ref<KeyValue[]>([])

const changeHostId = () => {
  if (data.form.hostId) {
    if (hostObj.value[data.form.hostId]) {
      data.form.privateIp = hostObj.value[data.form.hostId].privateIp
      data.form.publicIp = hostObj.value[data.form.hostId].publicIp
    }
    installUserLoading.value = true
    mockUserListAll(data.form.hostId).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        userListByHost.value = []
        userListByHost.value = res.data
        if (userListByHost.value.length) {
          data.form.installUserId = userListByHost.value[0].hostUserId
        } else {
          data.form.installUserId = ''
        }
      } else {
        Message.error('Failed to obtain user data from the host')
      }
    }).finally(() => {
      installUserLoading.value = false
    })
  }
}

onMounted(() => {
  initData()
  getHostList()
})

const blankValidator = (value: any, cb: any) => {
  return new Promise(resolve => {
    if (!value.trim()) {
      cb(t('enterprise.ClusterConfig.else2'))
      resolve(false)
    } else {
      resolve(true)
    }
  })
}

const portValidator = (value: any, cb: any) => {
  return new Promise(resolve => {
    const reg = /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/
    const re = new RegExp(reg)
    if (re.test(value)) {
      resolve(true)
    } else {
      cb('请输入正确的端口号')
      resolve(false)
    }
  })
}

const rules: KeyValue = {
  shardingPort: [{
    required: true, message: '请输入Sharding端口'
  }, {
    validator: portValidator
  }],
  zkPort: [{
    required: true, message: '请输入Zookeeper端口'
  }, {
    validator: portValidator
  }],
  hostId: { required: true, message: '请选择主机' },
  installUserId: { required: true, message: '请选择用户名' },
  password: [
    { required: true, message: '请输入密码' },
    {
      validator: blankValidator
    }
  ],
  installPath: [
    { required: true, message: '请输入安装路径' },
    {
      validator: blankValidator
    }
  ],
  shardingTarId: { required: true, message: '请选择ShardingProxy安装包' },
  zkTarId: { required: true, message: '请选择Zookeeper安装包' },
  tableName: [{ required: true, message: '请输入分片表名称' }, { validator: blankValidator }],
  column: [{ required: true, message: '请输入分片键名称' }, { validator: blankValidator }]
}

const loadingFunc = inject<any>('loading')
const formRef = ref<FormInstance>()

const initData = () => {
  if (!installStore.openLookengInstallConfig.shardingInstallPath) {
    return
  }
  const config = JSON.parse(JSON.stringify(installStore.openLookengInstallConfig))
  data.form.hostId = config.shardingInstallHostId
  data.form.installUserId = config.shardingInstallUserId
  data.form.password = config.shardingInstallPassword
  data.form.installPath = config.shardingInstallPath
  data.form.shardingTarId = config.shardingTarId
  data.form.shardingPort = config.shardingPort
  data.form.zkTarId = config.zkTarId
  data.form.zkPort = config.zkPort
  data.form.tableName = config.tableName
  data.form.column = config.column
}

const saveStore = () => {
  const param = {
    shardingInstallHostId: data.form.hostId,
    shardingInstallUserId: data.form.installUserId,
    shardingInstallPassword: data.form.password,
    shardingInstallPath: data.form.installPath,
    shardingTarId: data.form.shardingTarId,
    shardingPort: data.form.shardingPort,
    zkTarId: data.form.zkTarId,
    zkPort: data.form.zkPort,
    tableName: data.form.tableName,
    column: data.form.column
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

const validateSpecialFields = async () => {
  let result = true
  formRef.value?.clearValidate()
  if (data.form.rootPassword) {
    const validMethodArr = []
    const encryptPwd = await mockEncryptPassword(data.form.rootPassword)
    // password validate
    try {
      const param = {
        rootPassword: encryptPwd
      }
      const passwordValid: KeyValue = await mockHostPingById(data.form.hostId, param)
      if (Number(passwordValid.code) !== 200) {
        formRef.value?.setFields({
          rootPassword: {
            status: 'error',
            message: t('enterprise.NodeConfig.else8')
          }
        })
        result = false
      }
    } catch (err: any) {
      formRef.value?.setFields({
        rootPassword: {
          status: 'error',
          message: t('enterprise.NodeConfig.else9')
        }
      })
      result = false
    }
    if (!result) {
      return result
    }
    //  cluster port is used
    validMethodArr.push(validatePort(data.form.port, encryptPwd, data.form.hostId))
    validMethodArr.push(validatePath(data.form.installPath, encryptPwd, data.form.hostId))
    if (validMethodArr.length) {
      let validResult
      try {
        validResult = await Promise.all(validMethodArr)
      } catch (err: any) {
        return false
      }
      if (!validResult[0]) {
        // port valid
        formRef.value?.setFields({
          port: {
            status: 'error',
            message: data.form.port + t('enterprise.NodeConfig.else11')
          }
        })
        result = false
      }
      if (!validResult[1]) {
        // dataPath Valid
        formRef.value?.setFields({
          installPath: {
            status: 'error',
            message: t('simple.InstallConfig.else4')
          }
        })
        result = false
      }
    }
  }

  return result
}

const validatePort = async (port: number, password: string, hostId: string) => {
  const portParam = {
    port: port,
    rootPassword: password
  }
  const portValid: KeyValue = await mockPortUsed(hostId, portParam)
  if (Number(portValid.code) === 200) {
    return portValid.data
  }
  return false
}

const validatePath = async (path: string, password: string, hostId: string) => {
  const pathParam = {
    path: path,
    rootPassword: password
  }
  const pathValid: KeyValue = await mockPathEmpty(hostId, pathParam)
  if (Number(pathValid.code) === 200) {
    return pathValid.data
  }
  return false
}

watch(() => props.tarMap, (val) => {
  if (val[TarType.SHARDING_PROXY] && val[TarType.SHARDING_PROXY].length > 0) {
    data.shardingTarList = val[TarType.SHARDING_PROXY]
  }
  if (val[TarType.ZOOKEEPER] && val[TarType.ZOOKEEPER].length > 0) {
    data.zookeeperTarList = val[TarType.ZOOKEEPER]
  }
}, { deep: true })

defineExpose({
  saveStore,
  beforeConfirm
})
</script>
