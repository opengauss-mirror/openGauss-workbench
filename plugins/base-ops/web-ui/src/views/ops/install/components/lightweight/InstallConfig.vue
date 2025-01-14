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
              <a-input v-model.trim="formItem.clusterId" :placeholder="$t('lightweight.InstallConfig.5mpmkfqy8r00')" />
            </a-form-item>
            <a-form-item field="hostId" :label="$t('lightweight.InstallConfig.5mpmkfqy8vk0')">
              <a-select :loading="hostListLoading" v-model="formItem.hostId" @change="changeHostId(index)" class="mr-s"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqy91w0')"
                @popup-visible-change="hostPopupChange($event, index)">
                <a-option v-for="item in hostList" :key="item.hostId" :value="item.hostId">{{
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
            <a-form-item field="installUserId" :label="$t('lightweight.InstallConfig.5mpmkfqy9rw0')">
              <a-select :loading="installUserLoading" v-model="formItem.installUserId"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqy9yo0')" @change="hostUserChange($event, index)"
                @popup-visible-change="hostUserPopupChange($event, index)">
                <a-option v-for="item in userListByHost[formItem.hostId]" :key="item.hostUserId"
                  :value="item.hostUserId">{{
                    item.username
                  }}</a-option>
              </a-select>
            </a-form-item>
            <a-form-item field="installPath" :label="$t('lightweight.InstallConfig.5mpmkfqya4o0')"
              validate-trigger="blur">
              <div class="flex-col-start full-w">
                <div class="mb-s full-w">
                  <a-input v-model.trim="formItem.installPath"
                    :placeholder="$t('lightweight.InstallConfig.5mpmkfqyaas0')" />
                </div>
                <div class="label-color">{{ $t('simple.InstallConfig.else12') }}</div>
              </div>
            </a-form-item>
            <a-form-item field="installPackagePath" :label="$t('simple.InstallConfig.else6')" validate-trigger="blur">
              <div class="flex-col-start full-w">
                <div class="mb-s full-w">
                  <a-input v-model.trim="formItem.installPackagePath" :placeholder="$t('simple.InstallConfig.else7')"
                    @blur="handleInstallPackageBlur(index)" />
                </div>
                <div class="label-color">{{ $t('lightweight.InstallConfig.else2') }}</div>
              </div>
            </a-form-item>
            <a-form-item field="dataPath" :label="$t('lightweight.InstallConfig.5mpmkfqyah00')" validate-trigger="blur">
              <a-input v-model.trim="formItem.dataPath" :placeholder="$t('lightweight.InstallConfig.5mpmkfqyan00')" />
            </a-form-item>
            <a-form-item v-if="index === 0" field="port" :label="$t('lightweight.InstallConfig.5mpmkfqyasw0')"
              validate-trigger="blur">
              <a-input-number v-model="formItem.port" :placeholder="$t('lightweight.InstallConfig.5mpmkfqyay80')" :min="0"
                :max="65535" />
            </a-form-item>
            <a-form-item field="databaseUsername" :label="$t('lightweight.InstallConfig.5mpmkfqyb4c0')"
              validate-trigger="blur" v-if="installType === 'import' && index === 0">
              <a-input v-model.trim="formItem.databaseUsername"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqyb9w0')" allow-clear />
            </a-form-item>
            <a-form-item v-if="index === 0" field="databasePassword" :label="$t('lightweight.InstallConfig.5mpmkfqybf80')"
              validate-trigger="blur">
              <a-input-password v-model="formItem.databasePassword"
                :placeholder="$t('lightweight.InstallConfig.5mpmkfqybo00')" allow-clear />
            </a-form-item>
            <a-form-item v-if="index === 0" field="isEnvSeparate" :label="$t('simple.InstallConfig.else11')"
              validate-trigger="blur">
              <a-switch v-model="formItem.isEnvSeparate" @change="isEnvSeparateChange(formItem)" />
            </a-form-item>
            <a-form-item v-if="formItem.isEnvSeparate && index === 0" field="envPath"
              :label="$t('simple.InstallConfig.else9')" validate-trigger="blur">
              <a-input v-model.trim="formItem.envPath" :placeholder="$t('simple.InstallConfig.else10')" />
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
    <host-terminal ref="hostTerminalRef"></host-terminal>
  </div>
</template>

<script lang="ts" setup>
import { computed, ComputedRef, onMounted, reactive, ref, inject } from 'vue'
import {
  ClusterRoleEnum,
  DeployTypeEnum,
  LiteNodeConfig,
  LiteInstallConfig
} from '@/types/ops/install' // eslint-disable-line
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { hasName, hostListAll, hostUserListWithoutRoot, portUsed, pathEmpty, fileExist, hostPingById, checkDiskSpace } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import HostTerminal from "@/views/ops/install/components/hostTerminal/HostTerminal.vue";
const { t } = useI18n()
const installStore = useOpsStore()
const data = reactive<KeyValue>({
  nodeData: [],
  rules: {},
  envPrefix: '/home/',
  envSuffix: '/cluster_' + dayjs().format('YYYYMMDD') + '_' + dayjs().format('HHMMSSS') + '.bashrc'
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
            if (!value.trim()) {
              cb(t('enterprise.ClusterConfig.else2'))
              resolve(false)
            }
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
    port: [
      { required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyay80') }
    ],
    installPath: [
      { required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyaas0') },
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
    installPackagePath: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.else7') },
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
      { required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyan00') },
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
    databaseUsername: [
      { required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqyb9w0') },
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
    databasePassword: [
      { required: true, 'validate-trigger': 'blur', message: t('lightweight.InstallConfig.5mpmkfqybo00') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^(?![\da-z]+$)(?![\dA-Z]+$)(?![\d~!@#$%^&*()_=+\|{};:,<.>/?]+$)(?![a-zA-Z]+$)(?![a-z~!@#$%^&*()_=+\|{};:,<.>/?]+$)(?![A-Z~!@#$%^&*()_=+\|{};:,<.>/?]+$)[\da-zA-z~!@#$%^&*()_=+\|{};:,<.>/?]{8,}$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('simple.InstallConfig.else5'))
              resolve(false)
            }
          })
        }
      }
    ],
    envPath: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.else10') },
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
}

const getFormData = (): KeyValue => {
  //  auto generate envPath
  return {
    clusterId: '',
    hostId: '',
    isNeedPwd: false,
    privateIp: '',
    publicIp: '',
    installUserId: '',
    installUserName: '',
    installPath: '/opt/software/openGauss/install',
    installPackagePath: '/opt/software/openGauss',
    dataPath: '/opt/software/openGauss/data',
    port: Number(5432),
    databaseUsername: '',
    databasePassword: '',
    isEnvSeparate: true,
    envPath: ''
  }
}

const hostObj = ref<KeyValue>({})
const getHostList = (index = 0) => {
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
const hostUserObj = ref<KeyValue>({})
const changeHostId = (index: number) => {
  const hostId = data.nodeData[index].hostId
  if (hostId) {
    if (hostObj.value[hostId]) {
      data.nodeData[index].privateIp = hostObj.value[hostId].privateIp
      data.nodeData[index].publicIp = hostObj.value[hostId].publicIp
      data.nodeData[index].isNeedPwd = !hostObj.value[hostId].isRemember
    }
    if (userListByHost.value[hostId] && !data.nodeData[index].installUserId) {
      data.nodeData[index].installUserId = userListByHost.value[hostId][0].hostUserId
    } else {
      installUserLoading.value = true
      hostUserListWithoutRoot(hostId).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          if (res.data.length) {
            userListByHost.value[hostId] = res.data
            res.data.forEach((installUser: KeyValue) => {
              hostUserObj.value[installUser.hostUserId] = installUser
            })
            const hostUserId = data.nodeData[index].installUserId
            if (hostUserId) {
              const hasExist = res.data.find((item: KeyValue) => {
                return item.hostUserId === hostUserId
              })
              if (!hasExist) {
                data.nodeData[index].installUserId = userListByHost.value[hostId][0].hostUserId
                data.nodeData[index].installUserName = userListByHost.value[hostId][0].username
                calEnvPath(index)
              }
            } else {
              data.nodeData[index].installUserId = userListByHost.value[hostId][0].hostUserId
              data.nodeData[index].installUserName = userListByHost.value[hostId][0].username
              calEnvPath(index)
            }
          } else {
            data.nodeData[index].installUserId = ''
            data.nodeData[index].installUserName = ''
            calEnvPath(index)
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

const hostUserChange = (val: string, index: number) => {
  if (val && hostUserObj.value[val]) {
    data.nodeData[index].installUserName = hostUserObj.value[val].username
    calEnvPath(index)
  }
}

const calEnvPath = (index: number) => {
  if (installType.value !== 'import') {
    if (data.nodeData[index].clusterRole === ClusterRoleEnum.MASTER && data.nodeData[index].installUserName) {
      data.nodeData[index].envPath = data.envPrefix + data.nodeData[index].installUserName + data.envSuffix
    }
  }
}

const hostUserPopupChange = (val: boolean, index: number) => {
  if (val) {
    changeHostId(index)
  }
}

const handleInstallPackageBlur = (index: number) => {
  const path = data.nodeData[index].installPackagePath
  data.nodeData.forEach((item: KeyValue) => {
    item.installPackagePath = path
  })
}

const isEnvSeparateChange = (formItem: any) => {
  if (!formItem.isEnvSeparate) {
    formItem.envPath = ''
  } else {
    calEnvPath(0)
  }
}

const setRefMap = (el: any) => {
  if (el) {
    refList.value.push(el)
  }
}

const saveStore = () => {
  // doNothing
}

const loadingFunc = inject<any>('loading')

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
    loadingFunc.toLoading()
    validRes = await validateSpecialFields()
  }
  if (validRes && installType.value !== 'import') {
    await checkFreeDisk().catch(() => {
      loadingFunc.cancelLoading()
    })
    validRes = flag.value
    console.log("checkFreeDisk is more than 2GB:" + validRes)
  }
  if (validRes) {
    const param = JSON.parse(JSON.stringify(data.nodeData))
    if (param.length) {
      // node use first node port
      param.forEach((item: KeyValue) => {
        item.port = param[0].port
      })
      installStore.setInstallContext({ clusterId: param[0].clusterId, envPath: param[0].envPath })
      const liteConfig = {
        clusterName: '',
        port: param[0].port,
        databaseUsername: param[0].databaseUsername,
        databasePassword: param[0].databasePassword,
        installPackagePath: param[0].installPackagePath,
        nodeConfigList: param as LiteNodeConfig[]
      }
      installStore.setLiteConfig(liteConfig as LiteInstallConfig)
      console.log('lite config', installStore.getLiteConfig);
    }
    loadingFunc.cancelLoading()
    return true
  }
  loadingFunc.cancelLoading()
  return false
}

const validatePort = async (port: number, hostId: string) => {
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
  if (data.nodeData.length) {
    for (let i = 0; i < data.nodeData.length; i++) {
      refList.value[i].clearValidate()
    }
    for (let i = 0; i < data.nodeData.length; i++) {
      const validMethodArr = []

      //  cluster port is used
      validMethodArr.push(await validatePort(data.nodeData[i].port, data.nodeData[i].hostId))
      validMethodArr.push(await validatePath(data.nodeData[i].dataPath, data.nodeData[i].hostId))
      validMethodArr.push(await validatePath(data.nodeData[i].installPackagePath, data.nodeData[i].hostId))
      if (data.nodeData[i].isEnvSeparate && installType.value === 'import' && i === 0) {
        validMethodArr.push(await validateFile(data.nodeData[i].envPath, data.nodeData[i].hostId))
      }
      if (validMethodArr.length) {
        const validResult = await Promise.all(validMethodArr)
        if (i === 0) {
          if ((installType.value !== 'import' && validResult[0]) || (installType.value === 'import' && !validResult[0])) {
            // port valid
            refList.value[i].setFields({
              port: {
                status: 'error',
                message: data.nodeData[i].port + (installType.value === 'import' ? t('enterprise.NodeConfig.else10') : t('enterprise.NodeConfig.else11'))
              }
            })
            result = false
          }
        }
        if ((installType.value !== 'import' && !validResult[1]) || (installType.value === 'import' && validResult[1])) {
          // dataPath Valid
          refList.value[i].setFields({
            dataPath: {
              status: 'error',
              message: installType.value === 'import' ? t('enterprise.NodeConfig.else12') : t('enterprise.NodeConfig.else13')
            }
          })
          result = false
        }
        if ((installType.value !== 'import' && !validResult[2]) || (installType.value === 'import' && validResult[2])) {
          // dataPath Valid
          refList.value[i].setFields({
            installPackagePath: {
              status: 'error',
              message: installType.value === 'import' ? t('enterprise.NodeConfig.else14') : t('enterprise.NodeConfig.else15')
            }
          })
          result = false
        }
        if (installType.value == 'import' && data.nodeData[i].isEnvSeparate && !validResult[3] && i === 0) {
          // dataPath Valid
          refList.value[i].setFields({
            envPath: {
              status: 'error',
              message: t('enterprise.NodeConfig.else17')
            }
          })
          result = false
        }
      }
    }
  }
  return result
}

const flag = ref(true);

const checkFreeDisk = async () => {
  flag.value = true;
  const promises = [];
  data.nodeData.forEach(item => {
    const combinedPaths = [
      item.installPath,
      item.installPackagePath,
      item.dataPath,
    ];
    if (data.nodeData[0].isEnvSeparate) {
      combinedPaths.push(data.nodeData[0].envPath)
    }
    promises.push(
      Promise.all(combinedPaths.map(path => {
        return checkDiskSpace([path], item.hostId).then(res => {
          if (res.code === 200) {
            const space = Number(res.data[path].slice(0, res.data[path].length - 1));
            if (space < 2) {
              Message.error(`${item.publicIp}(${item.privateIp}):${path} disk space is less than 2G`);
              flag.value = false;
            }
          }
        });
      }))
    );
  });
  await Promise.all(promises);
};

defineExpose({
  saveStore,
  beforeConfirm
})

const installType = computed(() => installStore.getInstallConfig.installType)
const getDeployType: ComputedRef<DeployTypeEnum> = computed(() => installStore.getInstallConfig.deployType)

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

