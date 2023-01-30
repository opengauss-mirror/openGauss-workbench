<template>
  <div class="install-config-c">
    <div class="flex-col">
      <div v-for="(formItem, index) in data.nodeData" :key="index">
        <div class="flex-col-start">
          <div class="node-top full-w mb">
            <div class="flex-row">
              <a-tag class="mr-s" color="#86909C">{{ index === 0 ? $t('lightweight.InstallConfig.5mpmkfqy71w0') :
                $t('lightweight.InstallConfig.5mpmkfqy8400')
              }}</a-tag>
              {{ $t('lightweight.InstallConfig.5mpmkfqy8es0') }}
            </div>
          </div>
          <a-form :id="`formRef${index}`" :model="formItem" :rules="data.rules" :style="{ width: '800px' }"
            :ref="(el: any) => setRefMap(el)">
            <a-form-item v-if="index === 0" field="clusterId" :label="$t('lightweight.InstallConfig.5mpmkfqy8nc0')"
              validate-trigger="blur">
              <a-input v-model="formItem.clusterId" :placeholder="$t('lightweight.InstallConfig.5mpmkfqy8r00')" />
            </a-form-item>
            <a-form-item field="hostId" :label="$t('lightweight.InstallConfig.5mpmkfqy8vk0')">
              <a-select :loading="hostListLoading" v-model="formItem.hostId" @change="changeHostId(index)"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqy91w0')"
                @popup-visible-change="hostPopupChange($event, index)">
                <a-option v-for="item in hostList" :key="item.hostId" :value="item.hostId">{{
                  item.privateIp
                    + '(' +
                    (item.publicIp ? item.publicIp : '--') + ')'
                }}</a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="rootPassword" :label="$t('lightweight.InstallConfig.else1')" validate-trigger="blur">
              <a-input-password v-model="formItem.rootPassword"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqy9h80')" allow-clear />
            </a-form-item>
            <a-form-item field="installUserId" :label="$t('lightweight.InstallConfig.5mpmkfqy9rw0')">
              <a-select :loading="installUserLoading" v-model="formItem.installUserId"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqy9yo0')"
                @popup-visible-change="hostUserPopupChange($event, index)">
                <a-option v-for="item in userListByHost[formItem.hostId]" :key="item.hostUserId"
                  :value="item.hostUserId">{{
                    item.username
                  }}</a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="installPath" :label="$t('lightweight.InstallConfig.5mpmkfqya4o0')"
              validate-trigger="blur">
              <a-input v-model="formItem.installPath" :placeholder="$t('lightweight.InstallConfig.5mpmkfqyaas0')" />
            </a-form-item>
            <a-form-item field="dataPath" :label="$t('lightweight.InstallConfig.5mpmkfqyah00')" validate-trigger="blur">
              <a-input v-model="formItem.dataPath" :placeholder="$t('lightweight.InstallConfig.5mpmkfqyan00')" />
            </a-form-item>
            <a-form-item v-if="index === 0" field="port" :label="$t('lightweight.InstallConfig.5mpmkfqyasw0')"
              validate-trigger="blur">
              <a-input-number v-model="formItem.port" :placeholder="$t('lightweight.InstallConfig.5mpmkfqyay80')" />
            </a-form-item>
            <a-form-item field="databaseUsername" :label="$t('lightweight.InstallConfig.5mpmkfqyb4c0')"
              validate-trigger="blur" v-if="installType === 'import' && index === 0">
              <a-input v-model="formItem.databaseUsername" :placeholder="$t('lightweight.InstallConfig.5mpmkfqyb9w0')"
                allow-clear />
            </a-form-item>
            <a-form-item v-if="index === 0" field="databasePassword"
              :label="$t('lightweight.InstallConfig.5mpmkfqybf80')" validate-trigger="blur">
              <a-input-password v-model="formItem.databasePassword"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqybo00')" allow-clear />
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ComputedRef, onMounted, reactive, ref } from 'vue'
import {
  ClusterRoleEnum,
  DeployTypeEnum,
  LiteNodeConfig,
  LiteInstallConfig
} from '@/types/ops/install' // eslint-disable-line
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { hasName, hostListAll, hostUserListWithoutRoot } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const installStore = useOpsStore()
const data: {
  nodeData: Array<KeyValue>,
  rules: KeyValue
} = reactive({
  nodeData: [],
  rules: {}
})

const hostListLoading = ref<boolean>(false)
const hostList = ref<KeyValue[]>([])

const refList = ref<any>([])

onMounted(async () => {
  initData()
  if (Object.keys(installStore.getLiteConfig).length && installStore.getLiteConfig.nodeConfigList.length) {
    await getHostList()
    installStore.getLiteConfig.nodeConfigList.forEach((item, index) => {
      data.nodeData.push(item)
      changeHostId(index)
    })
    data.nodeData[0].port = installStore.getLiteConfig.port
    data.nodeData[0].databasePassword = installStore.getLiteConfig.databasePassword
  } else {
    data.nodeData = []
    const masterNode = getFormData()
    masterNode.clusterRole = ClusterRoleEnum.MASTER
    data.nodeData.push(masterNode)
    if (getDeployType.value === DeployTypeEnum.CLUSTER) {
      const backNode = getFormData()
      backNode.clusterRole = ClusterRoleEnum.SLAVE
      data.nodeData.push(backNode)
    }
    getHostList()
  }
})

const initData = () => {
  data.rules = {
    hostId: [{ required: true, 'validate-trigger': 'change', message: t('lightweight.InstallConfig.5mpmkfqybw00') }],
    installUserId: [{ required: true, 'validate-trigger': 'change', message: t('lightweight.InstallConfig.5mpmkfqy9yo0') }],
    clusterId: [
      { required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqy8r00') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const param = {
              name: value
            }
            hasName(param).then((res: KeyValue) => {
              if (Number(res.code) === 200) {
                if (res.data.has === 'Y') {
                  cb(t('lightweight.InstallConfig.5mpmkfqyc0w0'))
                  resolve(false)
                } else {
                  resolve(true)
                }
              } else {
                cb(t('lightweight.InstallConfig.5mpmkfqyc5w0'))
                resolve(false)
              }
            })
          })
        }
      }
    ],
    port: [{ required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyay80') }],
    rootPassword: [{ required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqy9h80') }],
    installPath: [{ required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyaas0') }],
    dataPath: [{ required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyan00') }],
    databaseUsername: [{ required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyb9w0') }],
    databasePassword: [{ required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqybo00') }]
  }
}

const getFormData = (): KeyValue => {
  return {
    clusterId: '',
    hostId: '',
    rootPassword: '',
    privateIp: '',
    publicIp: '',
    installUserId: '',
    installPath: '/opt/software/openGauss/install',
    dataPath: '/opt/software/openGauss/data',
    port: Number(5432),
    databaseUsername: '',
    databasePassword: ''
  }
}

const hostObj = ref<KeyValue>({})
const getHostList = (index: number = 0) => {
  hostListLoading.value = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      hostList.value = []
      hostList.value = res.data
      res.data.forEach((item: KeyValue) => {
        hostObj.value[item.hostId] = item
      })
      if (data.nodeData.length) {
        if (!data.nodeData[index].hostId) {
          data.nodeData[index].hostId = hostList.value[0].hostId
          data.nodeData[index].privateIp = hostList.value[0].privateIp
          data.nodeData[index].publicIp = hostList.value[0].publicIp
        } else {
          const getOldHost = hostList.value.find((item: KeyValue) => {
            return item.hostId === data.nodeData[index].hostId
          })
          if (!getOldHost) {
            data.nodeData[index].hostId = ''
            data.nodeData[index].privateIp = ''
            data.nodeData[index].publicIp = ''
          }
        }
      }
      changeHostId(index)
    } else {
      Message.error(t('lightweight.InstallConfig.5mpmkfqycac0'))
    }
  }).finally(() => {
    hostListLoading.value = false
  })
}

const installUserLoading = ref<boolean>(false)
const userListByHost = ref<KeyValue>({})
const changeHostId = (index: number) => {
  const hostId = data.nodeData[index].hostId
  if (hostId) {
    if (hostObj.value[hostId]) {
      data.nodeData[index].privateIp = hostObj.value[hostId].privateIp
      data.nodeData[index].publicIp = hostObj.value[hostId].publicIp
    }
    if (userListByHost.value[hostId] && !data.nodeData[index].installUserId) {
      data.nodeData[index].installUserId = userListByHost.value[hostId][0].hostUserId
    } else {
      installUserLoading.value = true
      hostUserListWithoutRoot(hostId).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          if (res.data.length) {
            userListByHost.value[hostId] = res.data
            const hostUserId = data.nodeData[index].installUserId
            if (hostUserId) {
              const hasExist = res.data.find((item: KeyValue) => {
                return item.hostUserId === hostUserId
              })
              if (!hasExist) {
                data.nodeData[index].installUserId = userListByHost.value[hostId][0].hostUserId
              }
            } else {
              data.nodeData[index].installUserId = userListByHost.value[hostId][0].hostUserId
            }
          } else {
            data.nodeData[index].installUserId = ''
          }
        } else {
          Message.error('Description Failed to obtain user data from the host')
        }
      }).finally(() => {
        installUserLoading.value = false
      })
    }
  }
}

const hostPopupChange = (val: boolean, index: number) => {
  if (val) {
    getHostList(index)
  }
}

const hostUserPopupChange = (val: boolean, index: number) => {
  if (val) {
    changeHostId(index)
  }
}

const setRefMap = (el: any) => {
  if (el) {
    refList.value.push(el)
  }
}

const beforeConfirm = async (): Promise<boolean> => {
  let validRes = true
  for (let i = 0; i < refList.value.length; i++) {
    if (refList.value[i]) {
      const tempRes = await refList.value[i].validate()
      if (tempRes) {
        validRes = false
      }
    }
  }
  if (validRes) {
    const param = JSON.parse(JSON.stringify(data.nodeData))
    if (param.length) {
      // node use first node port
      param.forEach((item: KeyValue) => {
        item.port = param[0].port
      })
      installStore.setInstallContext({ clusterId: param[0].clusterId })
      const liteConfig = {
        clusterName: '',
        port: param[0].port,
        databaseUsername: param[0].databaseUsername,
        databasePassword: param[0].databasePassword,
        nodeConfigList: param as LiteNodeConfig[]
      }
      installStore.setLiteConfig(liteConfig as LiteInstallConfig)
    }
    return true
  }
  return false
}

defineExpose({
  beforeConfirm
})

const installType = computed(() => installStore.getInstallConfig.installType)
const getDeployType: ComputedRef<DeployTypeEnum> = computed(() => installStore.getInstallConfig.deployType)

</script>

<style lang="less" scope>
.install-config-c {
  height: 100%;
  overflow-y: auto;

  .node-top {
    display: flex;
    align-items: center;
    background-color: #f2f3f5;
    border-radius: 8px;
    padding: 8px 12px;
  }
}
</style>

