<template>
  <div class="node-config-c">
    <div class="flex-col">
      <div class="flex-between " :style="{ width: '800px' }">
        <div class="ft-b ft-m mb">
          <span class="mr">{{ $t('enterprise.NodeConfig.5mpme7w69yc0') }}</span> <span class="ft-lg">{{
              data.nodeList.length
          }}</span>
        </div>
        <a-form :model="data.azForm" :rules="data.azRules" :style="{ width: '300px' }" auto-label-width ref="azFormRef">
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
              <icon-minus-circle class="remove-icon add-icon-size" v-if="index > 2" @click="removeNode(index)" />
            </div>
          </div>
          <a-form :model="formItem" :rules="data.rules" :style="{ width: '800px' }" auto-label-width :ref="setRefMap">
            <a-form-item field="hostId" :label="$t('enterprise.NodeConfig.5mpme7w6azo0')">
              <a-select :loading="data.hostListLoading" v-model="formItem.hostId" @change="changeHostId(index)"
                :placeholder="$t('enterprise.NodeConfig.5mpme7w6b3k0')">
                <a-option v-for="item in data.hostList" :key="item.hostId" :value="item.hostId">{{
                    item.privateIp
                    + '(' +
                    (item.publicIp ? item.publicIp : '--') + ')'
                }}</a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="rootPassword" :label="$t('enterprise.NodeConfig.else2')" validate-trigger="blur">
              <a-input-password v-model="formItem.rootPassword" :placeholder="$t('enterprise.NodeConfig.5mpme7w6b700')"
                allow-clear />
            </a-form-item>
            <a-form-item field="installUserId" :label="$t('enterprise.NodeConfig.5mpme7w6bak0')">
              <a-select v-model="formItem.installUserId" @change="changeInstallUserId($event, index)">
                <a-option v-for="item in data.userListByHost[formItem.hostId]" :key="item.hostUserId"
                  :value="item.hostUserId">{{
                      item.username
                  }}</a-option>
              </a-select>
            </a-form-item>
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item field="isInstallCM" :label="$t('enterprise.NodeConfig.else3')">
                  <a-switch v-model="formItem.isInstallCM" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="isCMMaster" :label="$t('enterprise.NodeConfig.5mpme7w6be40')">
                  <a-switch v-model="formItem.isCMMaster" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="cmDataPath" :label="$t('enterprise.NodeConfig.else4')" validate-trigger="blur">
              <a-input v-model="formItem.cmDataPath" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bhg0')" />
            </a-form-item>
            <a-form-item field="cmPort" :label="$t('enterprise.NodeConfig.else5')" validate-trigger="blur">
              <a-input v-model="formItem.cmPort" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bko0')" />
            </a-form-item>
            <div class="ft-m ft-b mb">
              {{ $t('enterprise.NodeConfig.5mpme7w6boc0') }}
            </div>
            <a-form-item field="dataPath" :label="$t('enterprise.NodeConfig.5mpme7w6brs0')" validate-trigger="blur">
              <a-input v-model="formItem.dataPath" :placeholder="$t('enterprise.NodeConfig.5mpme7w6bv40')" />
            </a-form-item>
            <a-form-item field="xlogPath" :label="$t('enterprise.NodeConfig.else1')" validate-trigger="blur">
              <a-input v-model="formItem.xlogPath" :placeholder="$t('enterprise.NodeConfig.5mpme7w6byo0')" />
            </a-form-item>
          </a-form>
          <a-divider v-if="index < (data.nodeList.length - 1)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>

import { onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { ClusterRoleEnum, EnterpriseInstallConfig } from '@/types/ops/install' // eslint-disable-line
import { hostListAll, hostUserListWithoutRoot, azListAll } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useOpsStore } from '@/store'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
import { FormInstance } from '@arco-design/web-vue/es/form'
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

const refList = ref<any>([])

onMounted(async () => {
  initData()
  if (Object.keys(installStore.getEnterpriseConfig).length && installStore.getEnterpriseConfig.nodeConfigList.length) {
    await getHostList()
    installStore.getEnterpriseConfig.nodeConfigList.forEach((item, index) => {
      data.nodeList.push(item)
      changeHostId(index)
    })
  } else {
    addNode(0, true)
    addNode(1, false)
    addNode(1, false)
    getHostList()
  }
  getAZList()
})

const initData = () => {
  data.azRules = {
    azId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6ap00') }]
  }
  data.rules = {
    hostId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6c1w0') }],
    rootPassword: [{ required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6b700') }],
    installUserId: [{ required: true, 'validate-trigger': 'change', message: t('enterprise.NodeConfig.5mpme7w6c5g0') }],
    cmPort: [{ required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6bko0') }],
    cmDataPath: [{ required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6c8s0') }],
    dataPath: [{ required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6cc40') }],
    xlogPath: [{ required: true, 'validate-trigger': 'blur', message: t('enterprise.NodeConfig.5mpme7w6cfw0') }]
  }
}

const addNode = (index: number, isMaster?: boolean) => {
  const nodeData = {
    clusterRole: ClusterRoleEnum.SLAVE,
    hostId: '',
    rootPassword: '',
    publicIp: '',
    privateIp: '',
    hostname: '',
    installUserId: '',
    installUsername: '',
    isInstallCM: true,
    isCMMaster: false,
    cmDataPath: '/opt/openGauss/data/cmserver',
    cmPort: '15300',
    dataPath: '/opt/openGauss/install/data/dn',
    xlogPath: '/opt/openGauss/install/data/xlog'
  }
  if (isMaster) {
    nodeData.clusterRole = ClusterRoleEnum.MASTER
    nodeData.isCMMaster = true
  }
  data.nodeList.splice(index + 1, 0, nodeData)
}

const removeNode = (index: number) => {
  if (index === 0) {
    Message.warning('The primary node cannot be removed')
  } else {
    data.nodeList.splice(index, 1)
  }
}

const getHostList = () => {
  data.hostListLoading = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.hostList = []
      data.hostList = res.data
      res.data.forEach((item: KeyValue) => {
        data.hostObj[item.hostId] = item
      })
      if (data.nodeList.length && !data.nodeList[0].hostId) {
        data.nodeList[0].hostId = data.hostList[0].hostId
        data.nodeList[0].privateIp = data.hostList[0].privateIp
        data.nodeList[0].publicIp = data.hostList[0].publicIp
      }
      changeHostId(0)
    } else {
      Message.error('Failed to obtain the host list data')
    }
  }).finally(() => {
    data.hostListLoading = false
  })
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
      data.azForm.azName = data.azObj[data.azForm.azId].name
    }
  }
}

const changeHostId = (index: number) => {
  const hostId = data.nodeList[index].hostId
  if (hostId) {
    if (data.hostObj[hostId]) {
      data.nodeList[index].privateIp = data.hostObj[hostId].privateIp
      data.nodeList[index].publicIp = data.hostObj[hostId].publicIp
      data.nodeList[index].hostname = data.hostObj[hostId].hostname
    }
    if (data.userListByHost[hostId] && !data.nodeList[index].installUserId) {
      data.nodeList[index].installUserId = data.userListByHost[hostId][0].hostUserId
      data.nodeList[index].installUsername = data.userListByHost[hostId][0].username
    } else {
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

const beforeConfirm = async (): Promise<boolean> => {
  let validRes = true
  const azFormValidaRes = await azFormRef.value?.validate()
  if (azFormValidaRes) {
    validRes = false
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
    const nodes = JSON.parse(JSON.stringify(data.nodeList))
    const param = {
      azId: data.azForm.azId,
      azName: data.azForm.azName,
      nodeConfigList: nodes
    }
    installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
    return true
  }
  return false
}

defineExpose({
  beforeConfirm
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
