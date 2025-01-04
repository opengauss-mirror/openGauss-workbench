<template>
  <div class="cluster-config-c">
    <div class="flex-col">
      <div
        class="flex-col-start"
        :style="{ width: '80%' }"
      >
        <a-tabs
          size="medium"
          type="card-gutter"
          :editable="true"
          :show-add-button="data.form.nodes.length <= 8"
          style="width: 100%;"
          @add="handleAdd"
          @delete="handleDelete"
          v-model:active-key="data.activeTab"
          show-add-button
          auto-switch
        >
          <a-tab-pane
            :title="$t('enterprise.ClusterConfig.5mpm3ku3go40')"
            key="clusterPane"
            :closable="false"
          >
            <cluster-form
              :form-data="data.form.cluster"
              @isEnvSeparateChange="isEnvChange"
              ref="clusterFormRef"
            ></cluster-form>
          </a-tab-pane>
          <a-tab-pane
            v-for="(item, index) in data.form.nodes"
            :key="item.id"
            :closable="data.form.nodes.length > (data.form.cluster.databaseKernelArch === DatabaseKernelArch.SHARING_STORAGE ? 2 : (data.form.cluster.isInstallCM ? minNodeSize : 1)) && item.clusterRole !== ClusterRoleEnum.MASTER"
          >
            <template #title>
              {{ item.clusterRole === ClusterRoleEnum.MASTER ? $t('enterprise.ClusterConfig.else3') :
                ($t('enterprise.ClusterConfig.else4') + index) }}
            </template>
            <node-info
              :form-data="item"
              :cluster-data="data.form.cluster"
              :ref="(el: any) => setRefMap(el, item.id)"
              @install-user="installUserChange"
            ></node-info>
          </a-tab-pane>
          <a-tab-pane
            :title="$t('enterprise.ClusterConfig.5mpm3ku3jpo0')"
            key="sharingStorage"
            :closable="false"
            v-if="data.form.cluster.databaseKernelArch === DatabaseKernelArch.SHARING_STORAGE"
          >
            <sharing-storage
              :form-data="data.form.cluster.sharingStorageInstallConfig"
              :cluster-data="data.form"
              ref="sharingStorageFormRef"
            ></sharing-storage>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch, nextTick, computed, inject, provide } from 'vue'
import { KeyValue } from '@/types/global';
import ClusterForm from './components/ClusterForm.vue';
import NodeInfo from './components/NodeInfo.vue';
import SharingStorage from './components/SharingStorage.vue';
import { ClusterRoleEnum, DeployTypeEnum, DatabaseKernelArch, ConnectTypeEnum, EnterpriseInstallConfig } from '@/types/ops/install'
import { Message } from '@arco-design/web-vue'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useOpsStore } from '@/store'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import {checkDiskSpace} from "@/api/ops";
const { t } = useI18n()
const installStore = useOpsStore()

const data = reactive<KeyValue>({
  activeTab: 'clusterPane',
  form: {
    cluster: {
      clusterId: '',
      clusterName: '',
      databaseKernelArch: 'MASTER_SLAVE',
      installPath: '/opt/openGauss/install/app',
      installPackagePath: '/opt/software/openGauss',
      logPath: '/opt/openGauss/log/omm',
      tmpPath: '/opt/openGauss/tmp',
      omToolsPath: '/opt/openGauss/install/om',
      corePath: '/opt/openGauss/corefile',
      port: 5432,
      enableDCF: false,
      dcfPort:17783,
      databaseUsername: '',
      databasePassword: '',
      isInstallCM: false,
      isEnvSeparate: true,
      envPath: '',
      sharingStorageInstallConfig: {
        dssHome: '',
        dssVgName: 'data',
        dssDataLunPath: '',
        xlogVgName: 'xlog',
        xlogLunPath: [],
        cmSharingLunPath: '',
        cmVotingLunPath: '',
        interconnectType: ConnectTypeEnum.TCP,
        rdmaConfig: '',
      }
    },
    nodes: []
  },
  envPrefix: '/home/',
  envSuffix: '/cluster_' + dayjs().format('YYYYMMDD') + '_' + dayjs().format('HHMMSSS') + '.bashrc'
})

const loadingFunc = inject<any>('loading')

onMounted(() => {
  initData()
})

const installType = computed(() => installStore.getInstallConfig.installType)
const isInstallCM = computed(() => installStore.getEnterpriseConfig.isInstallCM)
const minNodeSize = computed(() => {
  const ogVerNumStr = installStore.getInstallConfig.openGaussVersionNum
  const ogVerNum = Number(ogVerNumStr.replaceAll('.', ''))
  if (ogVerNum > 311) {
    return 2
  } else {
    return 3
  }
})

watch([() => data.form.cluster.databaseKernelArch, () => data.form.cluster.isInstallCM], (val) => {
  if (val[0] === DatabaseKernelArch.SHARING_STORAGE) {
    data.form.cluster.isInstallCM = true;
    data.form.cluster.enableDCF = false;
    if (data.form.nodes.length < 2) {
      for (let i = 0; i <= (2 - data.form.nodes.length); i++) {
        addNode(data.form.nodes.length - 1)
      }
    }
  } else {
    if (val[1]) {
      if (data.form.nodes.length < minNodeSize.value) {
        for (let i = 0; i <= (minNodeSize.value - data.form.nodes.length); i++) {
          addNode(data.form.nodes.length - 1)
        }
      }
    }
  }
})

watch(() => data.form.cluster.installPath, (newValue, oldValue) => {
  if (newValue) {
    if (newValue.endsWith('app') || newValue.endsWith('app/')) {
      newValue = newValue.replace(/app$/, '');
    }
    const logPath = newValue.endsWith('/') ? newValue + 'log' : newValue + '/log';
    data.form.cluster.logPath = logPath;
    const tempPath = newValue.endsWith('/') ? newValue + 'tmp' : newValue + '/tmp';
    data.form.cluster.tmpPath = tempPath;
    const omToolsPath = newValue.endsWith('/') ? newValue + 'om' : newValue + '/om';
    data.form.cluster.omToolsPath = omToolsPath;
    const tempNewValue = newValue.endsWith('/app')? newValue.slice(0, -4): newValue
    const tempOldValue = oldValue.endsWith('/app')? oldValue.slice(0, -4): oldValue
    data.form.nodes.forEach((node) => {
      if (node.dataPath.includes(tempOldValue)) {
        let startIndex = node.dataPath.indexOf(tempOldValue)
        let beforeMatch = node.dataPath.slice(0, startIndex)
        let afterMatch = node.dataPath.slice(startIndex + tempOldValue.length + 1)
        let newStr = beforeMatch + tempNewValue + afterMatch;
        node.dataPath = newStr
      }
      if (isInstallCM) {
        const cmDataPath = newValue.endsWith('/') ? newValue + 'cm' : newValue + '/cm';
        node.cmDataPath = cmDataPath;
      }
    })

    const dssHome = newValue.endsWith('/') ? newValue + 'dss_home' : newValue + '/dss_home';
    data.form.cluster.sharingStorageInstallConfig.dssHome = dssHome;
  }
});

const initData = () => {
  if (Object.keys(installStore.getEnterpriseConfig).length) {
    Object.assign(data.form.cluster, installStore.getEnterpriseConfig)
    if (installStore.getEnterpriseConfig.nodeConfigList.length) {
      installStore.getEnterpriseConfig.nodeConfigList.forEach((item) => {
        data.form.nodes.push(item)
      })
    }
  } else {
    addNode(0, true)
  }
}

const addNode = (index: number, isMaster: boolean = false) => {
  const nodeData = {
    id: new Date().getTime() + '_' + index + '',
    clusterRole: ClusterRoleEnum.SLAVE,
    hostId: '',
    isNeedPwd: false,
    rootPassword: '',
    publicIp: '',
    privateIp: '',
    hostname: '',
    installUserId: '',
    installUsername: '',
    isCMMaster: false,
    cmDataPath: '/opt/openGauss/data/cmserver',
    cmPort: 15300,
    dataPath: '/opt/openGauss/install/data/dn',
    azName: '',
    azPriority: 1
  }
  if (isMaster) {
    nodeData.clusterRole = ClusterRoleEnum.MASTER
    nodeData.isCMMaster = true
  }
  data.form.nodes.splice(index + 1, 0, nodeData)
}

const handleAdd = () => {
  addNode(data.form.nodes.length - 1)
}

const handleDelete = (val: any) => {
  const currentNode = data.form.nodes.find((item: KeyValue) => {
    return item.id === val
  })
  data.form.nodes = data.form.nodes.filter((item: KeyValue) => {
    return item.id !== val
  })
  if (currentNode && currentNode.isCMMaster) {
    // default first Master is Master
    data.form.nodes[0].isCMMaster = true
  }

  const newRefObj :any= {}
  for(let k in refObj.value) {
    if (k !== val) {
      newRefObj[k] = refObj.value[k];
    }
  }
  refObj.value = newRefObj;

  nextTick(() => {
    data.activeTab = data.form.nodes[0].id
  })
}

const isHasCMMaster = (id: string, isMaster: boolean) => {
  if (isMaster) {
    data.form.nodes.forEach((item: KeyValue) => {
      if (item.id !== id) {
        item.isCMMaster = false
      }
    })
  } else {
    const findTrueNode = data.form.nodes.filter((item: KeyValue) => {
      return item.isCMMaster === true
    })
    const currentNode = data.form.nodes.find((item: KeyValue) => {
      return item.id === id
    })
    if (!findTrueNode.length) {
      currentNode.isCMMaster = true
      Message.warning('One node must be the primary node')
    }
  }
}

const isEnvChange = (val: boolean) => {
  if (!val) {
    data.form.cluster.envPath = ''
  } else {
    if (data.form.nodes[0].installUsername) {
      installUserChange(data.form.nodes[0].installUsername)
    }
  }
}

const installUserChange = (val: string) => {
  if (val && installType.value !== 'import'  && data.form.cluster.isEnvSeparate === true) {
    data.form.cluster.envPath = data.envPrefix + val + data.envSuffix
  }
}

provide('cmUtil', {
  isHasCMMaster
})

const refObj = ref<KeyValue>({})
const setRefMap = (el: HTMLElement, key: string) => {
  if (!refObj.value[key]) {
    refObj.value[key] = el
  }
}

const refList = computed(() => {
  const result = []
  const refs = Object.keys(refObj.value)
  if (refs.length) {
    for (let key in refObj.value) {
      if (refObj.value[key]) {
        result.push(refObj.value[key])
      }
    }
  }
  return result
})

const saveStore = () => {
}

const azFormRef = ref<FormInstance>()
const clusterFormRef = ref<null | InstanceType<typeof ClusterForm>>(null)
const sharingStorageFormRef = ref<null | InstanceType<typeof SharingStorage>>(null)
const beforeConfirm = async (): Promise<boolean> => {
  let validRes = true
  if (installType.value !== 'import') {
    const azFormValidaRes = await azFormRef.value?.validate()
    if (azFormValidaRes) {
      validRes = false
    }
  }
  // valid cluster form
  if (validRes) {
    const clusterFormValidaRes: any = await clusterFormRef.value?.formValidate()
    if (!clusterFormValidaRes.res) {
      validRes = false
      data.activeTab = 'clusterPane'
    }

    if (data.form.cluster.installPath === data.form.cluster.installPackagePath) {
      clusterFormRef.value?.pathValidate("installPackagePath", t('enterprise.ClusterConfig.5mpm3ku3joo0'))
      validRes = false
      data.activeTab = 'clusterPane'
    }

    if (data.form.cluster.isInstallCM && data.form.cluster.enableDCF && data.form.cluster.dcfPort === data.form.cluster.port) {
      clusterFormRef.value?.pathValidate("dcfPort", t('enterprise.ClusterConfig.5mpm3ku3jxo0'))
      validRes = false
      data.activeTab = 'clusterPane'
    }
  }

  if (validRes) {
    if (data.form.cluster.databaseKernelArch === DatabaseKernelArch.SHARING_STORAGE) {
      const sharingStorageFormValidRes: any = await sharingStorageFormRef.value?.formValidate()
      if (!sharingStorageFormValidRes.res) {
        validRes = false
        data.activeTab = 'sharingStorage'
      }

      const sharingStorageFormLunValidRes: any = await sharingStorageFormRef.value?.lunValidate()
      if (!sharingStorageFormLunValidRes.res) {
        validRes = false
        data.activeTab = 'sharingStorage'
      }
    }
  }

  // ensure install user is the same and ip is different, and cm port
  if (validRes) {
    loadingFunc.toLoading()

    if (refList.value.length > 1 && data.form.nodes.length === refList.value.length) {
      for (let i = 0; i < refList.value.length - 1; i++) {
        for (let j = i + 1; j < refList.value.length; j++) {
          // 校验各个节点host不能相同
          if (data.form.nodes[i].hostId === data.form.nodes[j].hostId) {
            refList.value[j].userDefineValidate("hostId", t('enterprise.ClusterConfig.5mpm3ku3jso0'))
            data.activeTab = data.form.nodes[j].id;
            validRes = false
          }

          // 校验各个节点username必须相同
          if (data.form.nodes[i].installUsername !== data.form.nodes[j].installUsername) {
            refList.value[j].userDefineValidate("installUserId", t('enterprise.ClusterConfig.5mpm3ku3jto0'))
            data.activeTab = data.form.nodes[j].id;
            validRes = false
          }

          // 校验各个节点cm port是否相同
          if (data.form.cluster.isInstallCM) {
            if (data.form.nodes[i].cmPort !== data.form.nodes[j].cmPort) {
              refList.value[j].userDefineValidate("cmPort", t('enterprise.ClusterConfig.5mpm3ku3jwo0'))
              data.activeTab = data.form.nodes[j].id;
              validRes = false
            }
          }
        }
      }

      if (data.form.cluster.isInstallCM) {
        console.log("data.form.node = ", data.form.nodes, data.form.cluster.port)
        for (let i = 0; i < refList.value.length; i++)   {
            // cm port和 database port不能相同
            if (data.form.nodes[i].cmPort === data.form.cluster.port) {
              refList.value[i].userDefineValidate("cmPort", t('enterprise.ClusterConfig.5mpm3ku3jyx0'))
              data.activeTab = data.form.nodes[i].id;
              validRes = false
            }
            // dcf端口需要与数据库及CM端口不同
            if (data.form.cluster.enableDCF) {
              if (data.form.nodes[i].cmPort === data.form.cluster.dcfPort) {
                refList.value[i].userDefineValidate("cmPort", t('enterprise.ClusterConfig.5mpm3ku3jzx0'))
                data.activeTab = data.form.nodes[i].id;
                validRes = false
              }
            }
        }
      }
    }

  }
  // valid nodes form
  if (validRes) {
    const methodArr = []
    for (let i = 0; i < refList.value.length; i++) {
      if (refList.value[i]) {
        methodArr.push(refList.value[i].formValidate())
      }
    }
    const nodeFormValid = await Promise.all(methodArr)

    const nodeValidRes = nodeFormValid.filter((item: KeyValue) => {
      return item.res === false
    })
    if (nodeValidRes.length) {
      data.activeTab = nodeValidRes[0].id
      validRes = false
    }
  }

  if (validRes) {
    loadingFunc.toLoading()
    const methodArr = []
    for (let i = 0; i < refList.value.length; i++) {
      if (refList.value[i]) {
        methodArr.push(refList.value[i].dataValidate())
      }
    }
    const nodeFormValid = await Promise.all(methodArr)

    const nodeValidRes = nodeFormValid.filter((item: KeyValue) => {
      return item.res === false
    })
    if (nodeValidRes.length) {
      data.activeTab = nodeValidRes[0].id
      validRes = false
    }
  }
  if (validRes && installType.value !== 'import') {
    await checkFreeDisk().catch(() => {
      loadingFunc.cancelLoading()
    })
    validRes = flag.value
    console.log("checkFreeDisk is more than 2GB:" + validRes)
  }
  if (validRes) {
    saveClusterData()
    saveNodesData()
    loadingFunc.cancelLoading()
    return true
  }

  loadingFunc.cancelLoading()
  return false
}

const saveClusterData = () => {
  const param = JSON.parse(JSON.stringify(data.form.cluster))
  installStore.setInstallContext({
    clusterId: param.clusterId,
    clusterName: param.clusterName,
    deployType: DeployTypeEnum.CLUSTER,
    isEnvSeparate: param.isEnvSeparate,
    envPath: param.envPath
  })
  installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
  console.log('show store info', installStore.getInstallConfig, installStore.getEnterpriseConfig);
  console.log("sharing storage info = ", data.form.cluster.sharingStorageInstallConfig)

}

const saveNodesData = () => {
  const nodes = JSON.parse(JSON.stringify(data.form.nodes))
  const param = {
    nodeConfigList: nodes
  }
  installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
  console.log('show store info2', installStore.getEnterpriseConfig);
}

const flag = ref(true);

const checkFreeDisk = async () => {
  flag.value = true;
  const promises = [];
  data.form.nodes.forEach(item => {
    const itemPaths = [item.dataPath];
    if (data.form.cluster.isInstallCM) {
      itemPaths.push(item.cmDataPath)
    }
    if (data.form.cluster.isEnvSeparate) {
      itemPaths.push(data.form.cluster.envPath)
    }
    const combinedPaths = [
      data.form.cluster.installPath,
      data.form.cluster.installPackagePath,

      data.form.cluster.logPath,
      data.form.cluster.tmpPath,
      data.form.cluster.omToolsPath,
      data.form.cluster.corePath,
      ...itemPaths,
    ];
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
  beforeConfirm,
  saveStore
})

</script>

<style lang="less" scoped>
:deep(.arco-tabs-nav-tab) {
  color: var(--color-text-1);
}

.cluster-config-c {
  padding: 20px;
  height: 100%;
  overflow-y: auto;

  .node-top {
    height: 34px;
    display: flex;
    background-color: #f2f3f5;
    border-radius: 8px;
    padding: 8px 12px;
  }
}
</style>
