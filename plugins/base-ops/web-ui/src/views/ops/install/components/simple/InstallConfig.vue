<template>
  <div class="install-config-c">
    <div class="flex-row-center">
      <div class="flex-col-start">
        <div class="label-color ft-b mb">{{ $t('simple.InstallConfig.5mpmu0lapic0') }}</div>
        <a-form :model="data.form" :rules="data.rules" :style="{ width: '800px' }" ref="formRef">
          <a-form-item field="clusterId" :label="$t('simple.InstallConfig.5mpmu0laqc80')" validate-trigger="blur">
            <a-input v-model.trim="data.form.clusterId" :placeholder="$t('simple.InstallConfig.5mpmu0laqiw0')" />
          </a-form-item>
          <a-form-item field="hostId" :label="$t('simple.InstallConfig.5mpmu0laqow0')">
            <a-select :loading="hostListLoading" v-model="data.form.hostId" @change="changeHostId" class="mr-s"
              allow-clear :placeholder="$t('simple.InstallConfig.5mpmu0laqss0')" @popup-visible-change="hostPopupChange">
              <a-option v-for="item in hostList" :key="item.hostId" :value="item.hostId">{{
                item.privateIp
                + '(' +
                (item.publicIp ? item.publicIp : '--') + ')'
              }}
              </a-option>
            </a-select>
            <icon-code-square :size="25" class="label-color" style="cursor: pointer;" @click="showTerminal" />
          </a-form-item>
          <a-form-item v-if="data.isNeedPwd">
              <a-alert type="warning">{{ $t('enterprise.NodeConfig.noRootTip') }}</a-alert>
            </a-form-item>
          <a-form-item field="installUserId" :label="$t('simple.InstallConfig.5mpmu0lar0c0')">
            <a-select :loading="installUserLoading" v-model="data.form.installUserId" @change="hostUserChange"
              @popup-visible-change="hostUserPopupChange">
              <a-option v-for="item in userListByHost" :key="item.hostUserId" :value="item.hostUserId">{{
                item.username
              }}
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="installPath" :label="$t('simple.InstallConfig.5mpmu0lar480')" validate-trigger="blur">
            <div class="flex-col-start full-w">
              <div class="mb-s full-w">
                <a-input v-model.trim="data.form.installPath" :placeholder="$t('simple.InstallConfig.5mpmu0lar800')" />
              </div>
              <div class="label-color">{{ $t('simple.InstallConfig.else12') }}</div>
            </div>
          </a-form-item>
          <a-form-item v-if="installType !== 'import'" field="installPackagePath"
            :label="$t('simple.InstallConfig.else6')" validate-trigger="blur">
            <a-input v-model.trim="data.form.installPackagePath" :placeholder="$t('simple.InstallConfig.else7')" />
          </a-form-item>
          <a-form-item
            v-if="installType === 'import' && deployType === 'SINGLE_NODE'"
            field="editDataPath"
            :label="$t('simple.InstallConfig.5mpmu0laran0')"
            validate-trigger="blur"
          >
            <a-switch
              v-model="editDataPath"
              style="margin-right: 20px;"
            />
            <div v-if="!editDataPath" class="label-color">{{ $t('simple.InstallConfig.5mpmu0larbb0') }}</div>
          </a-form-item>
          <a-form-item v-if="editDataPath" field="dataPath"
            :label="$t('simple.InstallConfig.5mpmu0larbk0')" validate-trigger="blur">
            <div class="flex-col-start full-w">
              <div class="mb-s full-w">
                <a-input v-model.trim="data.form.dataPath" :placeholder="$t('simple.InstallConfig.5mpmu0larew0')" />
              </div>
              <div class="label-color">{{ $t('simple.InstallConfig.5mpmu0larsl0') }}</div>
            </div>
          </a-form-item>
          <a-form-item field="port" :label="$t('simple.InstallConfig.5mpmu0larj40')" validate-trigger="blur">
            <a-input-number v-model="data.form.port" :placeholder="$t('simple.InstallConfig.5mpmu0larmo0')" :min="0"
              :max="65535" />
          </a-form-item>
          <a-form-item field="databaseUsername" :label="$t('simple.InstallConfig.5mpmu0larq40')" validate-trigger="blur"
            v-if="installType === 'import'">
            <a-input v-model.trim="data.form.databaseUsername" :placeholder="$t('simple.InstallConfig.5mpmu0larto0')"
              allow-clear />
          </a-form-item>
          <a-form-item field="databasePassword" :label="$t('simple.InstallConfig.5mpmu0larx00')" validate-trigger="blur">
            <a-input-password v-model="data.form.databasePassword" :placeholder="$t('simple.InstallConfig.5mpmu0las0k0')"
              allow-clear />
          </a-form-item>
          <a-form-item
            v-if="installType === 'import' && deployType === 'SINGLE_NODE'"
            field="isEnvSeparate"
            :label="$t('simple.InstallConfig.else11')"
            validate-trigger="blur"
          >
            <a-switch
              v-model="data.form.isEnvSeparate"
              style="margin-right: 20px;"
            />
            <div v-if="!data.form.isEnvSeparate" class="label-color">{{ $t('simple.InstallConfig.else14') }}</div>
          </a-form-item>
          <a-form-item
            v-if="data.form.isEnvSeparate"
            field="envPath"
            :label="$t('simple.InstallConfig.else9')"
            validate-trigger="blur"
          >
            <a-input
              v-model.trim="data.form.envPath"
              :placeholder="$t('simple.InstallConfig.else10')"
            />
          </a-form-item>
        </a-form>
      </div>
    </div>
    <host-terminal ref="hostTerminalRef"></host-terminal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, inject } from 'vue'
import { MiniNodeConfig, MinimalistInstallConfig, ClusterRoleEnum } from '@/types/ops/install'
import { KeyValue } from '@/types/global'

import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { hostListAll, hostUserListWithoutRoot, hasName, portUsed, pathEmpty, checkDiskSpace } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import HostTerminal from '@/views/ops/install/components/hostTerminal/HostTerminal.vue'

const { t } = useI18n()
const installStore = useOpsStore()
const data = reactive({
  isNeedPwd: false,
  form: {
    clusterId: '',
    hostId: '',
    privateIp: '',
    publicIp: '',
    installUserId: '',
    installUserName: '',
    installPath: '/opt/openGauss',
    installPackagePath: '/opt/software/openGauss',
    dataPath: '',
    isEnvSeparate: false,
    envPath: '',
    port: Number(5432),
    databaseUsername: '',
    databasePassword: '',
    isInstallDemoDatabase: true
  },
  rules: {}
})
const deployStore = useOpsStore()
const deployType = computed(() => deployStore.getInstallConfig.deployType)
const editDataPath = ref<boolean>(false)
const hostListLoading = ref<boolean>(false)
const hostList = ref<KeyValue[]>([])

onMounted(() => {
  initData()
  if (installStore.getMiniConfig && installStore.getMiniConfig.nodeConfigList) {
    const miniConfig: any = installStore.getMiniConfig
    Object.assign(data.form, {
      clusterId: miniConfig.nodeConfigList[0].clusterId,
      hostId: miniConfig.nodeConfigList[0].hostId,
      privateIp: miniConfig.nodeConfigList[0].privateIp,
      publicIp: miniConfig.nodeConfigList[0].publicIp,
      installUserId: miniConfig.nodeConfigList[0].installUserId,
      installUserName: miniConfig.nodeConfigList[0].installUserName,
      installPath: miniConfig.nodeConfigList[0].installPath,
      installPackagePath: miniConfig.nodeConfigList[0].installPackagePath,
      dataPath: miniConfig.nodeConfigList[0].dataPath,
      port: miniConfig.port,
      databaseUsername: miniConfig.databaseUsername,
      databasePassword: miniConfig.databasePassword,
      isInstallDemoDatabase: miniConfig.nodeConfigList[0].isInstallDemoDatabase
    })
  }
  getHostList()
})

const initData = () => {
  data.rules = {
    hostId: [{ required: true, 'validate-trigger': 'change', message: t('simple.InstallConfig.5mpmu0laqss0') }],
    installUserId: [{ required: true, 'validate-trigger': 'change', message: t('lightweight.InstallConfig.5mpmkfqy9yo0') }],
    clusterId: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0laqiw0') },
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
                  cb(t('simple.InstallConfig.5mpmu0las4g0'))
                  resolve(false)
                } else {
                  resolve(true)
                }
              } else {
                cb(t('simple.InstallConfig.5mpmu0las800'))
                resolve(false)
              }
            })
          })
        }
      }
    ],
    port: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0larmo0') }
    ],
    installPath: [
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0lar800') },
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
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0larew0') },
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
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0larto0') },
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
      { required: true, 'validate-trigger': 'blur', message: t('simple.InstallConfig.5mpmu0las0k0') },
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
    ]
  }
}

const hostObj = ref<KeyValue>({})
const getHostList = () => {
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
const installUserLoading = ref<boolean>(false)
const userListByHost = ref<KeyValue[]>([])
const hostUserObj = ref<KeyValue>({})
const changeHostId = () => {
  if (data.form.hostId) {
    if (hostObj.value[data.form.hostId]) {
      data.form.privateIp = hostObj.value[data.form.hostId].privateIp
      data.form.publicIp = hostObj.value[data.form.hostId].publicIp
      data.isNeedPwd = !hostObj.value[data.form.hostId].isRemember
    }
    installUserLoading.value = true
    hostUserListWithoutRoot(data.form.hostId).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        userListByHost.value = []
        userListByHost.value = res.data
        res.data.forEach((item: KeyValue) => {
          hostUserObj.value[item.hostUserId] = item
        })
        if (data.form.installUserId) {
          const findOne = userListByHost.value.find((item: KeyValue) => {
            return item.hostUserId === data.form.installUserId
          })
          if (findOne) {
            data.form.installUserId = findOne.hostUserId
            data.form.installUserName = findOne.username
          } else {
            data.form.installUserId = userListByHost.value[0].hostUserId
            data.form.installUserName = userListByHost.value[0].username
          }
        } else {
          if (userListByHost.value.length) {
            data.form.installUserId = userListByHost.value[0].hostUserId
            data.form.installUserName = userListByHost.value[0].username
          } else {
            data.form.installUserId = ''
            data.form.installUserName = ''
          }
        }
      } else {
        Message.error('Failed to obtain user data from the host')
      }
    }).finally(() => {
      installUserLoading.value = false
    })
  }
}

const hostPopupChange = (val: boolean) => {
  if (val) {
    getHostList()
  }
}

const hostUserChange = (val: string) => {
  if (val && hostUserObj.value[val]) {
    data.form.installUserName = hostUserObj.value[val].username
  }
}

const hostUserPopupChange = (val: boolean) => {
  if (val) {
    changeHostId()
  }
}

const formRef = ref<FormInstance>()

const saveStore = () => {
  const param = JSON.parse(JSON.stringify(data.form))
  console.log('show installStore mini111', param)
  param.clusterRole = ClusterRoleEnum.MASTER
  param.clusterName = ''
  installStore.setInstallContext({ clusterId: param.clusterId, envPath: param.envPath })
  const miniConfig = {
    clusterName: '',
    port: param.port,
    databaseUsername: param.databaseUsername,
    databasePassword: param.databasePassword,
    installPackagePath: param.installPackagePath,
    nodeConfigList: [param as MiniNodeConfig]
  }
  installStore.setMiniConfig(miniConfig as MinimalistInstallConfig)
  console.log('show installStore mini', installStore.getMiniConfig)
}

const loadingFunc = inject<any>('loading')

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
  if (validRes && installType.value !== 'import') {
    await checkFreeDisk().catch(() => {
      loadingFunc.cancelLoading()
    })
    validRes = flag.value
    console.log("checkFreeDisk is more than 2GB:" + validRes)
  }
  if (validRes) {
    saveStore()
    loadingFunc.cancelLoading()
    return true
  }
  loadingFunc.cancelLoading()
  return false
}

const validatePort = async (port: number, password: string, hostId: string) => {
  const portParam = {
    port: port,
  }
  const portValid: KeyValue = await portUsed(hostId, portParam)
  if (Number(portValid.code) === 200) {
    return portValid.data
  }
  return false
}

const validatePath = async (path: string, password: string, hostId: string) => {
  const pathParam = {
    path: path,
  }
  const pathValid: KeyValue = await pathEmpty(hostId, pathParam)
    .catch((error) => {
      loadingFunc.cancelLoading();
    })
  if (Number(pathValid.code) === 200) {
    return pathValid.data
  }
  return false
}


const validateSpecialFields = async () => {
  let result = true
  formRef.value?.clearValidate()
  const validMethodArr = []
  let encryptPwd = ''
  //  cluster port is used
  validMethodArr.push(await validatePort(data.form.port, encryptPwd, data.form.hostId))
  validMethodArr.push(await validatePath(data.form.installPath, encryptPwd, data.form.hostId))
  if (installType.value !== 'import') {
    validMethodArr.push(await validatePath(data.form.installPackagePath, encryptPwd, data.form.hostId))
  }
  if (installType.value === 'import' && editDataPath) {
    validMethodArr.push(await validatePath(data.form.dataPath, encryptPwd, data.form.hostId))
  }
  if (validMethodArr.length) {
    let validResult
    try {
      validResult = await Promise.all(validMethodArr)
    } catch (err: any) {
      return result = false
    }
    if ((installType.value !== 'import' && validResult[0]) || (installType.value === 'import' && !validResult[0])) {
      // port valid
      formRef.value?.setFields({
        port: {
          status: 'error',
          message: data.form.port + (installType.value === 'import' ? t('enterprise.NodeConfig.else10') : t('enterprise.NodeConfig.else11'))
        }
      })
      result = false
    }
    if ((installType.value !== 'import' && !validResult[1]) || (installType.value === 'import' && validResult[1])) {
      formRef.value?.setFields({
        installPath: {
          status: 'error',
          message: installType.value === 'import' ? t('simple.InstallConfig.else3') : t('simple.InstallConfig.else4')
        }
      })
      result = false
    }
    if (installType.value !== 'import' && !validResult[2]) {
      // installPackagePath Valid
      formRef.value?.setFields({
        installPackagePath: {
          status: 'error',
          message: t('enterprise.NodeConfig.else15')
        }
      })
      result = false
    }
    if (editDataPath && installType.value === 'import' && validResult[2]) {
      formRef.value?.setFields({
        dataPath: {
          status: 'error',
          message: t('simple.InstallConfig.else15')
        }
      })
      result = false
    }
  }
  return result
}

const hostTerminalRef = ref<null | InstanceType<typeof HostTerminal>>(null)
const showTerminal = () => {
  // isRemember password
  if (!data.form.hostId) {
    formRef.value?.setFields({
      hostId: {
        status: 'error',
        message: t('simple.InstallConfig.5mpmu0laqss0')
      }
    })
    return
  }
  // showTerminal
  handleShowTerminal({
    hostId: data.form.hostId,
    port: data.form.port,
    ip: data.form.publicIp,
  })
}

const handleShowTerminal = (data: KeyValue) => {
  hostTerminalRef.value?.open(data)
}

const flag = ref(true);

const checkFreeDisk = async () => {
  flag.value = true;
  const promises = [];
  const combinedPaths = [
    data.form.installPath,
    data.form.installPackagePath,
  ];
  promises.push(
    Promise.all(combinedPaths.map(path => {
      return checkDiskSpace([path], data.form.hostId).then(res => {
        if (res.code === 200) {
          const space = Number(res.data[path].slice(0, res.data[path].length - 1));
          if (space < 2) {
            Message.error(`${data.form.publicIp}(${data.form.privateIp}):${path} disk space is less than 2G`);
            flag.value = false;
          }
        }
      });
    }))
  );
  await Promise.all(promises);
};

const installType = computed(() => installStore.getInstallConfig.installType)
defineExpose({
  saveStore,
  beforeConfirm
})

</script>

<style lang="less" scoped>
.install-config-c {
  height: calc(100% - 28px - 42px);
  overflow-y: auto;
}
</style>
