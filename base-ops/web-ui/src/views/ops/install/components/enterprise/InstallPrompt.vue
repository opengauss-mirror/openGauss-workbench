<template>
  <div class="install-prompt-c">
    <div class="flex-col">
      <div class="mb" style="width: 50%;">
        <div class="item-node-top flex-between full-w mb">
          <div class="flex-row">
            <div class="mr">{{ $t('enterprise.InstallPrompt.5mpmb9e6puk0') }}</div>
            <div>{{ data.clusterInfo.clusterId }} - {{ data.clusterInfo.clusterName }}</div>
          </div>
          <div class="flex-row">
            <div>{{ $t('enterprise.InstallPrompt.else1') }}: AZ</div>
          </div>
        </div>
        <div class="label-color item-node-center">
          <div class="flex-row">
            <div class="lable-w">{{ $t('enterprise.InstallPrompt.else2') }}:</div>
            <div>{{ data.clusterInfo.installPath }}</div>
          </div>
          <a-divider></a-divider>
          <div class="flex-row">
            <div class="lable-w">{{ $t('enterprise.InstallPrompt.else3') }}:</div>
            <div>{{ data.clusterInfo.port }}</div>
          </div>
        </div>
      </div>
      <div class="mb" style="width: 50%;" v-for="(itemNode, index) in data.nodeData" :key="index">
        <div class="full-w">
          <div class="item-node-top flex-between full-w">
            <div class="flex-row">
              <a-tag class="mr" color="#86909C">{{ getRoleName(itemNode.clusterRole) }}</a-tag>
              {{ $t('enterprise.InstallPrompt.5mpmb9e6qmg0') }} {{ itemNode.privateIp }}({{
                itemNode.publicIp
              }})
            </div>
            <div class="flex-row">
              <icon-down style="cursor: pointer;" v-if="!itemNode.isShow" @click="itemNode.isShow = true" />
              <icon-up style="cursor: pointer;" v-else @click="itemNode.isShow = false" />
            </div>
          </div>
          <div class="label-color item-node-center full-w flex-col-start" v-show="itemNode.isShow">
            <div class="flex-row">
              <div class="lable-w">{{ $t('enterprise.InstallPrompt.5mpmb9e6qsc0') }}</div>
              <div>{{ itemNode.publicIp }}</div>
            </div>
            <a-divider></a-divider>
            <div class="flex-row">
              <div class="lable-w">{{ $t('enterprise.InstallPrompt.5mpmb9e6qww0') }}</div>
              <div>{{ itemNode.dataPath }}</div>
            </div>
            <a-divider></a-divider>
            <div class="flex-row">
              <div class="lable-w">{{ $t('enterprise.InstallPrompt.else4') }}</div>
              <div>{{ itemNode.xlogPath }}</div>
            </div>
            <a-divider></a-divider>
            <div class="flex-row">
              <div class="lable-w">{{ $t('enterprise.InstallPrompt.5mpmb9e6r0k0') }} CM</div>
              <div>{{
                itemNode.isInstallCM ? $t('enterprise.InstallPrompt.5mpmb9e6r4g0') :
                  $t('enterprise.InstallPrompt.5mpmb9e6r800')
              }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { useOpsStore } from '@/store'
import { KeyValue } from '@/types/global'
import { inject, onMounted, reactive } from 'vue'
import { ClusterRoleEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const installStore = useOpsStore()

const data: {
  clusterInfo: KeyValue,
  nodeData: KeyValue
} = reactive({
  clusterInfo: {},
  nodeData: []
})

const loadingFunc = inject<any>('loading')

onMounted(() => {
  loadingFunc.setNextBtnShow(true)
  loadingFunc.setBackBtnShow(true)
  data.nodeData = []
  const storeEnterpriseData = installStore.getEnterpriseConfig
  const storeInstallData = installStore.getInstallConfig
  Object.assign(data.clusterInfo, {
    clusterId: storeInstallData.clusterId,
    clusterName: storeInstallData.clusterName,
    installPath: storeEnterpriseData.installPath,
    port: storeEnterpriseData.port
  })
  storeEnterpriseData.nodeConfigList.forEach(item => {
    const itemNode = getNodeData()
    Object.assign(itemNode, {
      clusterRole: item.clusterRole,
      publicIp: item.publicIp,
      privateIp: item.privateIp,
      dataPath: item.dataPath,
      xlogPath: item.xlogPath,
      isInstallCM: item.isInstallCM
    })
    data.nodeData.push(itemNode)
  })
})

const getNodeData = () => {
  return {
    isShow: true,
    clusterRole: '',
    publicIp: '',
    privateIp: '',
    installPath: '',
    dataPath: '',
    port: ''
  }
}

const getRoleName = (type: ClusterRoleEnum) => {
  switch (type) {
    case ClusterRoleEnum.MASTER:
      return t('enterprise.InstallPrompt.5mpmb9e6rcw0')
    case ClusterRoleEnum.SLAVE:
      return t('enterprise.InstallPrompt.5mpmb9e6rj80')
    default:
      return t('enterprise.InstallPrompt.5mpmb9e6rpo0')
  }
}

</script>
<style lang="less" scoped>
.install-prompt-c {
  height: 100%;
  overflow-y: auto;

  .item-node-top {
    background-color: #f2f3f5;
    line-height: 40px;
    border-radius: 4px;
    padding: 0 16px;
  }

  .item-node-center {
    padding: 20px;
  }

  .lable-w {
    width: 100px;
  }
}
</style>
