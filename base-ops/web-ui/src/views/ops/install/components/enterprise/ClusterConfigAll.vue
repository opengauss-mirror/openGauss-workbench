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
              ref="clusterFormRef"
            ></cluster-form>
          </a-tab-pane>
          <a-tab-pane
            v-for="(item, index) in data.form.nodes"
            :key="item.id"
            :closable="data.form.nodes.length > (data.form.cluster.isInstallCM ? 3 : 1) && item.clusterRole !== ClusterRoleEnum.MASTER"
          >
            <template #title>
              {{ item.clusterRole === ClusterRoleEnum.MASTER ? $t('enterprise.ClusterConfig.else3') :
                ($t('enterprise.ClusterConfig.else4') + index) }}
            </template>
            <node-info
              :form-data="item"
              :cluster-data="data.form.cluster"
              :ref="(el: any) => setRefMap(el, item.id)"
            ></node-info>
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
import { ClusterRoleEnum, DeployTypeEnum, EnterpriseInstallConfig } from '@/types/ops/install'
import { Message } from '@arco-design/web-vue'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useOpsStore } from '@/store'
import { useI18n } from 'vue-i18n'
import { install } from 'vue-demi';
const { t } = useI18n()
const installStore = useOpsStore()

const data = reactive<KeyValue>({
  activeTab: 'clusterPane',
  form: {
    cluster: {
      clusterId: '',
      clusterName: '',
      installPath: '/opt/openGauss/install/app',
      installPackagePath: '/opt/software/openGauss',
      logPath: '/opt/openGauss/log/omm',
      tmpPath: '/opt/openGauss/tmp',
      omToolsPath: '/opt/openGauss/install/om',
      corePath: '/opt/openGauss/corefile',
      port: '5432',
      enableDCF: false,
      databaseUsername: '',
      databasePassword: '',
      isInstallCM: false,
      isEnvSeparate: false,
      envPath: '',
      azId: '',
      azName: ''
    },
    nodes: []
  }
})

const loadingFunc = inject<any>('loading')

onMounted(() => {
  initData()
})

const installType = computed(() => installStore.getInstallConfig.installType)

watch(() => data.form.cluster.isInstallCM, (val) => {
  if (val) {
    if (data.form.nodes.length < 3) {
      for (let i = 0; i <= (3 - data.form.nodes.length); i++) {
        addNode(data.form.nodes.length - 1)
      }
    }
  }
})

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
    cmPort: '15300',
    dataPath: '/opt/openGauss/install/data/dn',
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
    envPath: param.envPath
  })
  installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
  console.log('show store info', installStore.getInstallConfig, installStore.getEnterpriseConfig);

}

const saveNodesData = () => {
  const nodes = JSON.parse(JSON.stringify(data.form.nodes))
  const param = {
    nodeConfigList: nodes
  }
  installStore.setEnterpriseConfig(param as EnterpriseInstallConfig)
  console.log('show store info2', installStore.getEnterpriseConfig);
}

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