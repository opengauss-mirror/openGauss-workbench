<template>
  <div class="node-config-c">
    <div class="flex-col">
      <div class="flex-between " :style="{ width: '800px' }">
        <div class="label-color ft-b ft-m mb">
          <span class="mr">{{ $t('enterprise.NodeConfig.5mpme7w69yc0') }}</span> <span class="ft-lg">{{
            data.nodeList.length
          }}</span>
        </div>
        <a-form v-if="installType !== 'import'" :model="data.azForm" :rules="data.azRules" :style="{ width: '300px' }"
          auto-label-width ref="azFormRef">
          <a-form-item field="azId" :label="$t('enterprise.NodeConfig.5mpme7w6aj40')" validate-trigger="change">
            <a-select :loading="data.azListLoading" v-model="data.azForm.azId"
              :placeholder="$t('enterprise.NodeConfig.5mpme7w6ap00')" @change="azChange">
              <a-option v-for="item in data.azList" :key="item.azId" :value="item.azId">{{
                item.name
              }}</a-option>
            </a-select>
          </a-form-item>
        </a-form>
      </div>
      <div v-for="(formItem, index) in data.nodeList" :key="index">
        <div class="flex-col-start">
          <div class="node-top full-w mb">
            <div class="flex-row">
              <a-tag color="#86909C" class="mr-s">{{ getRoleName(formItem.clusterRole) }}</a-tag>
              {{ $t('enterprise.NodeConfig.5mpme7w6aw80') }}
            </div>
            <div class="flex-row">
              <icon-plus-circle class="add-icon-size mr" style="color: green" @click="addNode(index)" />
              <icon-minus-circle class="remove-icon add-icon-size"
                v-if="(!isInstallCM && index > 0) || (isInstallCM && index > 2)" @click="removeNode(index)" />
            </div>
          </div>
          <a-form :model="formItem" :rules="data.rules" :style="{ width: '800px' }" auto-label-width :ref="setRefMap">
            <a-form-item field="hostId" :label="$t('enterprise.NodeConfig.5mpme7w6azo0')">
              <a-select :loading="data.hostListLoading" v-model="formItem.hostId" @change="changeHostId(index)"
                :placeholder="$t('enterprise.NodeConfig.5mpme7w6b3k0')"
                @popup-visible-change="hostPopupChange($event, index)" class="mr-s">
                <a-option v-for="item in data.hostList" :key="item.hostId" :value="item.hostId">{{
                  item.privateIp
                  + '(' +
                  (item.publicIp ? item.publicIp : '--') + ')'
                }}</a-option>
              </a-select>
              <icon-code-square :size="25" class="label-color" style="cursor: pointer;"
                @click="showTerminal(formItem, index)" />
            </a-form-item>
            <a-form-item v-if="formItem.isNeedPwd">
              <a-alert type="warning">{{ $t('enterprise.NodeConfig.noRootTip') }}</a-alert>
            </a-form-item>
            <a-form-item field="installUserId" :label="$t('enterprise.NodeConfig.5mpme7w6bak0')">
              <a-select :loading="installUserLoading" v-model="formItem.installUserId"
                @change="changeInstallUserId($event, index)" @popup-visible-change="hostUserPopupChange($event, index)">
                <a-option v-for="item in data.userListByHost[formItem.hostId]" :key="item.hostUserId"
                  :value="item.hostUserId">{{
                    item.username
                  }}</a-option>
              </a-select>
            </a-form-item>
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item v-if="isInstallCM" field="isCMMaster" :label="$t('enterprise.NodeConfig.5mpme7w6be40')">
                  <a-switch v-model="formItem.isCMMaster" @change="handleNodeCMChange($event, index)" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item v-if="isInstallCM" field="cmDataPath" :label="$t('enterprise.NodeConfig.else4')"
              validate-trigger="blur">
              <a-input v-model="formItem.cmDataPath" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bhg0')" />
            </a-form-item>
            <a-form-item v-if="isInstallCM" field="cmPort" :label="$t('enterprise.NodeConfig.else5')"
              validate-trigger="blur">
              <a-input-number v-model="formItem.cmPort" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bko0')" :min="0"
                :max="65535" />
            </a-form-item>
            <div class="label-color ft-m ft-b mb">
              {{ $t('enterprise.NodeConfig.5mpme7w6boc0') }}
            </div>
            <a-form-item field="dataPath" :label="$t('enterprise.NodeConfig.5mpme7w6brs0')" validate-trigger="blur">
              <a-input v-model="formItem.dataPath" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bv40')" />
            </a-form-item>
            <a-form-item field="azPriority" :label="$t('enterprise.NodeConfig.else7')" v-if="installType !== 'import'">
              <a-input-number :min="1" :max="10" v-model="formItem.azPriority"
                :placeholder="$t('enterprise.NodeConfig.else6')" />
            </a-form-item>
          </a-form>
          <a-divider v-if="index < (data.nodeList.length - 1)" />
        </div>
      </div>
    </div>
    <host-terminal ref="hostTerminalRef"></host-terminal>
  </div>
</template>

<script lang="ts" setup>

import { onMounted, reactive, ref, computed, inject } from 'vue'
import { KeyValue } from '@/types/global'
import { ClusterRoleEnum, EnterpriseInstallConfig } from '@/types/ops/install' // eslint-disable-line
import { hostListAll, hostUserListWithoutRoot, azListAll, portUsed, pathEmpty, fileExist, multiPathQuery, hostPingById } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import HostTerminal from "@/views/ops/install/components/hostTerminal/HostTerminal.vue";
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const installStore = useOpsStore()

const data = reactive<KeyValue>({
  nodeList: [],
  azForm: {
    azId: '',
    azName: ''
  },
  azRules: {},
  rules: {},
  hostListLoading: false,
  hostObj: {},
  hostList: [],
  installUserObj: {},
  azListLoading: false,
  azObj: {},
  azList: [],
  userListByHost: {}
})

const isInstallCM = computed(() => installStore.getEnterpriseConfig.isInstallCM)

const refList = ref<any>([])

onMounted(async () => {
  initData()
  console.log('onMounted store', installStore.getEnterpriseConfig, Object.keys(installStore.getEnterpriseConfig).length, installStore.getEnterpriseConfig.nodeConfigList.length);
  if (Object.keys(installStore.getEnterpriseConfig).length && installStore.getEnterpriseConfig.nodeConfigList.length) {
    await getHostList()
    installStore.getEnterpriseConfig.nodeConfigList.forEach((item, index) => {
      data.nodeList.push(item)
      changeHostId(index)
    })
  } else {
    console.log('2', isInstallCM.value)
    addNode(0, true)
    if (isInstallCM.value) {
      addNode(1, false)
      addNode(1, false)
    }
    getHostList()
  }
  getAZList()
})

const installType = computed(() => installStore.getInstallConfig.installType)

const initData = () => {
  data.azRules = {
    azId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6ap00') }]
  }
  data.rules = {
    hostId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6c1w0') }],
    installUserId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6c5g0') }],
    cmPort: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6bko0') }
    ],
    cmDataPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6c8s0') },
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
    ],
    dataPath: [
      { required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6cc40') },
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
    ],
    azPriority: [{ required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.else6') }]
  }
}

const addNode = (index: number, isMaster?: boolean) => {
  refList.value = []
  const nodeData = {
    clusterRole: ClusterRoleEnum.SLAVE,
    hostId: '',
    isNeedPwd: false,
    publicIp: '',
    privateIp: '',
    hostname: '',
    installUserId: '',
    installUsername: '',
    isCMMaster: false,
    cmDataPath: '/opt/openGauss/data/cmserver',
    cmPort: '15300',
    dataPath: '/opt/openGauss/install/data/dn',
    azPriority: 1
  }
  if (isMaster) {
    nodeData.clusterRole = ClusterRoleEnum.MASTER
    nodeData.isCMMaster = true
  }
  data.nodeList.splice(index + 1, 0, nodeData)
}

const removeNode = (index: number) => {
  refList.value = []
  if (index === 0) {
    Message.warning('The primary node cannot be removed')
  } else {
    data.nodeList.splice(index, 1)
  }
}

const getHostList = (index = 0) => {
  data.hostListLoading = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.hostList = []
      data.hostList = res.data
      res.data.forEach((item: KeyValue) => {
        data.hostObj[item.hostId] = item
      })
      if (data.nodeList.length && !data.nodeList[index].hostId) {
        data.nodeList[index].hostId = data.hostList[index].hostId
        data.nodeList[index].privateIp = data.hostList[index].privateIp
        data.nodeList[index].publicIp = data.hostList[index].publicIp
      }
      changeHostId(index)
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    data.hostListLoading = false
  })
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

const handleNodeCMChange = (val: boolean, index: number) => {
  console.log('node isCMMaster change', val, index, data.nodeList.length)
  if (val) {
    data.nodeList.forEach((item: KeyValue, nodeIndex: number) => {
      if (nodeIndex !== index) {
        item.isCMMaster = false
      }
    })
  } else {
    const findTrueNode = data.nodeList.filter((item: KeyValue) => {
      return item.isCMMaster === true
    })
    if (!findTrueNode.length) {
      data.nodeList[index].isCMMaster = true
      Message.warning('One node must be the primary node')
    }
  }

}

const getAZList = () => {
  data.azListLoading = true
  azListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.azList = []
      data.azList = res.data
      res.data.forEach((item: KeyValue) => {
        data.azObj[item.azId] = item.name
      })
      data.azForm.azId = res.data[0].azId
      data.azForm.azName = res.data[0].name
    } else {
      Message.error('Failed to obtain the AZ list data')
    }
  }).finally(() => {
    data.azListLoading = false
  })
}

const azChange = () => {
  if (data.azForm.azId) {
    if (data.azObj[data.azForm.azId]) {
      data.azForm.azName = data.azObj[data.azForm.azId]
    }
  }
}

const installUserLoading = ref<boolean>(false)
const changeHostId = (index: number) => {
  const hostId = data.nodeList[index].hostId
  if (hostId) {
    if (data.hostObj[hostId]) {
      data.nodeList[index].privateIp = data.hostObj[hostId].privateIp
      data.nodeList[index].publicIp = data.hostObj[hostId].publicIp
      data.nodeList[index].hostname = data.hostObj[hostId].hostname
      data.nodeList[index].isNeedPwd = !data.hostObj[hostId].isRemember
    }
    if (data.userListByHost[hostId] && !data.nodeList[index].installUserId) {
      data.nodeList[index].installUserId = data.userListByHost[hostId][0].hostUserId
      data.nodeList[index].installUsername = data.userListByHost[hostId][0].username
    } else {
      installUserLoading.value = true
      hostUserListWithoutRoot(hostId).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          if (res.data.length) {
            data.userListByHost[hostId] = res.data
            res.data.forEach((installUser: KeyValue) => {
              data.installUserObj[installUser.hostUserId] = installUser
            })
            const hostUserId = data.nodeList[index].installUserId
            if (hostUserId) {
              const hasExist = res.data.find((item: KeyValue) => {
                return item.hostUserId === hostUserId
              })
              if (!hasExist) {
                data.nodeList[index].installUserId = data.userListByHost[hostId][0].hostUserId
                data.nodeList[index].installUsername = data.userListByHost[hostId][0].username
              }
            } else {
              data.nodeList[index].installUserId = data.userListByHost[hostId][0].hostUserId
              data.nodeList[index].installUsername = data.userListByHost[hostId][0].username
            }
          } else {
            data.nodeList[index].installUserId = ''
            data.nodeList[index].installUsername = ''
          }
        } else {
          Message.error('Failed to obtain user data from the host')
        }
      }).finally(() => {
        installUserLoading.value = false
      })
    }
  }
}

const changeInstallUserId = (installUserId: any, index: number) => {
  const getInstallUser = data.installUserObj[installUserId]
  if (getInstallUser) {
    data.nodeList[index].installUsername = getInstallUser.username
  }
}

const getRoleName = (type: ClusterRoleEnum) => {
  switch (type) {
    case ClusterRoleEnum.MASTER:
      return t('enterprise.NodeConfig.5mpme7w6ckk0')
    case ClusterRoleEnum.SLAVE:
      return t('enterprise.NodeConfig.5mpme7w6cng0')
    default:
      return t('enterprise.NodeConfig.5mpme7w6cqk0')
  }
}

const setRefMap = (el: any) => {
  if (el) {
    refList.value.push(el)
  }
}

const azFormRef = ref<FormInstance>()

const loadingFunc = inject<any>('loading')

const saveStore = () => {
  const nodes = JSON.parse(JSON.stringify(data.nodeList))
  const param = {
    azId: data.azForm.azId,
    azName: data.azForm.azName,
    nodeConfigList: nodes
  }
  installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
  console.log("enterprise config: ", installStore.getEnterpriseConfig)
}

const beforeConfirm = async (): Promise<boolean> => {
  let validRes = true
  if (installType.value !== 'import') {
    const azFormValidaRes = await azFormRef.value?.validate()
    if (azFormValidaRes) {
      validRes = false
    }
  }
  if (validRes) {
    for (let i = 0; i < refList.value.length; i++) {
      if (refList.value[i]) {
        const tempRes = await refList.value[i].validate()
        if (tempRes) {
          validRes = false
        }
      }
    }
  }
  if (validRes) {
    loadingFunc.toLoading()
    validRes = await validateSpecialFields()
  }
  if (validRes) {
    const nodes = JSON.parse(JSON.stringify(data.nodeList))
    const param = {
      azId: data.azForm.azId,
      azName: data.azForm.azName,
      nodeConfigList: nodes
    }
    installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
    loadingFunc.cancelLoading()
    return true
  }
  loadingFunc.cancelLoading()
  return false
}

const clusterData = computed(() => installStore.getEnterpriseConfig)

const validatePort = async (port: number, hostId: string): Promise<any> => {
  const portParam = {
    port: port,
  }
  const portValid: KeyValue = await portUsed(hostId, portParam)
  if (Number(portValid.code) === 200) {
    return portValid.data
  }
  return false
}

const validatePath = async (path: string, hostId: string) => {
  const pathParam = {
    path: path,
  }
  const pathValid: KeyValue = await pathEmpty(hostId, pathParam)
    .catch(() => {
      loadingFunc.cancelLoading();
    })
  if (Number(pathValid.code) === 200) {
    return pathValid.data
  }
  return false
}

const validateFile = async (file: string, hostId: string) => {
  const pathParam = {
    file: file,
  }
  const pathValid: KeyValue = await fileExist(hostId, pathParam)
  if (Number(pathValid.code) === 200) {
    return pathValid.data
  }
  return false
}

const validateSpecialFields = async () => {
  let result = true
  if (data.nodeList.length) {
    for (let i = 0; i < data.nodeList.length; i++) {
      refList.value[i].clearValidate()
    }
    for (let i = 0; i < data.nodeList.length; i++) {
      const validMethodArr = []
      //  cluster port is used
      validMethodArr.push(await validatePort(clusterData.value.port, data.nodeList[i].hostId))
      validMethodArr.push(await validatePath(data.nodeList[i].dataPath, data.nodeList[i].hostId))
      validMethodArr.push(await validatePath(clusterData.value.installPackagePath, data.nodeList[i].hostId))
      if (isInstallCM.value) {
        validMethodArr.push(await validatePort(data.nodeList[i].cmPort, data.nodeList[i].hostId))
      }
      if (installStore.getInstallConfig.envPath && installType.value === 'import') {
        validMethodArr.push(await validateFile(installStore.getInstallConfig.envPath, data.nodeList[i].hostId))
      }
      if (validMethodArr.length) {
        const validResult = await Promise.all(validMethodArr)
        if ((installType.value !== 'import' && validResult[0]) || (installType.value === 'import' && !validResult[0])) {
          // port valid
          refList.value[i].setFields({
            hostId: {
              status: 'error',
              message: clusterData.value.port + (installType.value === 'import' ? t('enterprise.NodeConfig.else10') : t('enterprise.NodeConfig.else11'))
            }
          })
          result = false
        }
        if ((installType.value !== 'import' && !validResult[1]) || (installType.value === 'import' && validResult[1])) {
          // dataPath valid
          refList.value[i].setFields({
            dataPath: {
              status: 'error',
              message: installType.value === 'import' ? t('enterprise.NodeConfig.else12') : t('enterprise.NodeConfig.else13')
            }
          })
          result = false
        }

        if ((installType.value !== 'import' && !validResult[2]) || (installType.value === 'import' && validResult[2])) {
          // installPackagePath valid
          refList.value[i].setFields({
            hostId: {
              status: 'error',
              message: installType.value === 'import' ? t('enterprise.NodeConfig.else14') : t('enterprise.NodeConfig.else16')
            }
          })
          result = false
        }
        if (isInstallCM.value) {
          if ((installType.value !== 'import' && validResult[3]) || (installType.value === 'import' && !validResult[3])) {
            // cmPort valid
            refList.value[i].setFields({
              cmPort: {
                status: 'error',
                message: data.nodeList[i].cmPort + (installType.value === 'import' ? t('enterprise.NodeConfig.else10') : t('enterprise.NodeConfig.else11'))
              }
            })
            result = false
          }
          if (installType.value === 'import' && installStore.getInstallConfig.envPath && !validResult[4]) {
            // envPath valid
            refList.value[i].setFields({
              hostId: {
                status: 'error',
                message: t('enterprise.NodeConfig.else17')
              }
            })
            result = false
          }
        } else {
          if (installType.value === 'import' && installStore.getInstallConfig.envPath && !validResult[3]) {
            // envPath valid
            refList.value[i].setFields({
              hostId: {
                status: 'error',
                message: t('enterprise.NodeConfig.else17')
              }
            })
            result = false
          }
        }

      }
    }
  }
  return result
}


const hostTerminalRef = ref<null | InstanceType<typeof HostTerminal>>(null)
const showTerminal = (item: KeyValue, index: number) => {
  if (!item.hostId) {
    refList.value[index]?.setFields({
      hostId: {
        status: 'error',
        message: t('lightweight.InstallConfig.5mpmkfqybw00')
      }
    })
    return
  }
  // showTerminal
  handleShowTerminal({
    hostId: item.hostId,
    port: item.port,
    ip: item.publicIp,
    password: item.rootPassword
  })
}

const handleShowTerminal = (data: KeyValue) => {
  hostTerminalRef.value?.open(data)
}

defineExpose({
  beforeConfirm,
  saveStore
})

</script>

<style lang="less" scoped>
.node-config-c {
  height: 100%;
  overflow-y: auto;

  .node-top {
    position: relative;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #f2f3f5;
    border-radius: 8px;
    padding: 8px 12px;

    .remove-icon {
      color: red;
      position: absolute;
      top: -8px;
      right: -8px;
    }

    .add-icon-size {
      width: 24px;
      height: 24px;
      cursor: pointer;
    }
  }
}
</style>
