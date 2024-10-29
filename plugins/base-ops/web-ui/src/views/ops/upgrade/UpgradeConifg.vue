<template>
  <div class="upgrade-config-c" id="upgradeConfig">
    <div class="cluster-select-c">
      <div class="flex-between mb">
        <div class="select-cluster-top-left">
          <span class="label-color sec-title mr-s">{{ $t('upgrade.UpgradeConifg.5uuo00svvrc0') }}</span>
          <icon-refresh @click="getClusterList" class="refresh-button mr" />
        </div>
        <a-input-search class="mr-s" v-model="filter.keyword" :style="{ width: '320px' }"
          :placeholder="$t('upgrade.UpgradeConifg.5uuo00svwnk0')" @input="handleSearchClusterName" />
      </div>
      <div class="cluster-list-c">
        <a-spin :loading="data.clusterLoading" class="list-spin" :tip="$t('upgrade.UpgradeConifg.5uuo00svwu80')">
          <a-form :model="data" :scrollToFirstError="true" ref="clusterFormRef">
            <div v-for="(item, index) of data.clusterList" :key="index" class="cluster-item label-color mb">
              <a-checkbox v-model="item.isChecked" class="mr-s" :disabled="item.disabled"></a-checkbox>
              <div :class="(item.isChecked ? 'selected' : '') + ' cluster-info-c'" @click="handleClickCluster(item)">
                <div class="flex-between">
                  <div class="mb">
                    <div class="mr cluster-name">{{ $t('upgrade.UpgradeConifg.else1') }}：{{ item.clusterId }}</div>
                  </div>
                  <a-tag>{{ item.versionNum }}</a-tag>
                </div>
                <div class="mb-s flex-row">
                  <div class="mr-s">
                    {{ $t('upgrade.UpgradeConifg.else2') }}：{{ calcClusterNode(item) }}
                  </div>
                </div>
                <div class="mb-s">
                  {{ $t('upgrade.UpgradeConifg.else3') }}：{{ item.envPath }}
                </div>
                <div class="flex-between mb-s">
                  <div>{{ $t('upgrade.UpgradeConifg.else4') }}：{{ getSysInfo(item.clusterId) }}</div>
                  <a-tag :color="enterpriseClusterStateColor(item.clusterState)">{{
                    enterpriseClusterState(item.clusterState) }}</a-tag>
                </div>
                <div class="flex-row" v-if="!item.isRemember">
                  <div>{{ $t('upgrade.UpgradeConifg.else5') }}：({{ getMainNodeIp(item) }})</div>
                  <a-form-item hide-asterisk :field="`clusterList.${index}.rootPassword`"
                    :rules="item.clusterState === ClusterState.normal && item.isChecked ? { required: true, message: $t('upgrade.UpgradeConifg.5uuo00svwy40')} : {}">
                    <a-input-password :placeholder="$t('upgrade.UpgradeConifg.5uuo00svwy40')" allow-clear @click.stop=""
                      v-model="item.rootPassword" />
                  </a-form-item>
                </div>
              </div>
            </div>
          </a-form>
          <div v-if="data.clusterList.length <= 0 && !data.clusterLoading" class="empty-c">
            <svg-icon icon-class="ops-empty" class="icon-size"></svg-icon>
            <div class="tip-content mb-s">{{ $t('upgrade.UpgradeConifg.5uuo00svx1g0') }}</div>
            <a-button @click="goToInstall">{{ $t('upgrade.UpgradeConifg.5uuo00svx580') }}</a-button>
          </div>
        </a-spin>
      </div>
    </div>
    <img src="@/assets/images/modeling/ops/right-arrow.png" alt="" class="upgrade-icon" />
    <div class="target-version-c">
      <a-spin :loading="targetSelData.loading" class="list-spin" :tip="$t('upgrade.UpgradeConifg.5uuo00svx8k0')">
        <div class="flex-row version-title">
          <div class="mr-s label-color sec-title">{{ $t('upgrade.UpgradeConifg.5uuo00svxc00') }}</div>
          <icon-refresh @click="getUpgradeTargetVersion" class="refresh-button" />
        </div>
        <div class="target-select">
          <a-collapse :default-active-key="targetSelData.activeKey">
            <a-collapse-item v-for=" item  in  targetSelData.versionList" :key="item.versionKey"
              :header="item.versionKey">
              <div class="version-item mb" v-for=" info  in  item.versionInfo " :key="info.packageId"
                @click.prevent="handleClickVersionItem(info)">
                <a-checkbox :model-value="info.isChecked" class="mr-s" :disabled="info.disabled"></a-checkbox>
                <div>
                  <div class="flex-row">
                    <div class="mr-s">{{ info.name }}</div>
                    <svg-icon v-if="info.isOffical" icon-class="og-db" class="offical-icon"></svg-icon>
                  </div>
                  <a-tag class="mr-s">{{ info.os }}</a-tag>
                  <a-tag>{{ info.cpuArch }}</a-tag>
                </div>
              </div>
            </a-collapse-item>
          </a-collapse>
          <div v-if="targetSelData.versionList.length <= 0 && !targetSelData.loading" class="empty-c">
            <svg-icon icon-class="ops-empty" class="icon-size"></svg-icon>
            <div class="tip-content mb-s">{{ $t('upgrade.UpgradeConifg.5uuo00svx1g1') }}</div>
            <a-button @click="goToPackage">{{ $t('upgrade.UpgradeConifg.5uuo00svx1g2') }}</a-button>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref, defineExpose } from 'vue'
import { KeyValue } from '@/types/global'
import { build, listEnterpriseCluster } from '@/api/ops'
import { packageListAll } from '@/api/ops'
import { PackageType } from '@/types/resource/package'
import { ClusterRoleEnum, OpenGaussVersionEnum } from '@/types/ops/install'
import router from '@/router'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { number } from '@intlify/core-base'
import { dataTool } from 'echarts'
const { t } = useI18n()
import { useUpgradeStore, upClusterInfo, targetVersion, hostInfo } from '@/store/modules/ops/upgrade'
const upgradeStore = useUpgradeStore()

enum ClusterState {
  normal = 'Normal',
  unavailable = 'Unavailable',
  degraded = 'Degraded'
}

const data = reactive<KeyValue>({
  clusterList: [],
  oldData: [],
  clusterLoading: false,
  selectedOs: '',
  selectedCpuArch: '',
  selectedSourceVersion: [],
  selectedTargetVersion: 0
})
const filter = reactive({
  keyword: '',
  os: '',
  cpuArch: ''
})

const targetSelData = reactive({
  activeKey: [] as string[],
  versionList: [] as KeyValue[],
  loading: false
})

const clusterFormRef = ref()

const getClusterList = () => {
  data.clusterLoading = true
  listEnterpriseCluster().then((res: KeyValue) => {
    buildClusterData(res)
  }).finally(() => {
    data.clusterLoading = false
  })
}

const buildClusterData = (res: KeyValue) => {
  data.clusterList = res.data
  data.clusterList.map((cluster: KeyValue) => {
    // any of server is not remember, the cluster needs to input password
    const hasNoRemember = cluster.clusterNodes.some((item: KeyValue) => !item.isRemember)
    cluster.isRemember = !hasNoRemember
    cluster.vNum = Number(cluster.versionNum.replaceAll('.', ''))
    cluster.rootPassword = ''
  })
  data.oldData = res.data
}

const getUpgradeTargetVersion = () => {
  targetSelData.loading = true
  const param = {
    type: PackageType.OPENGAUSS,
    packageVersion: OpenGaussVersionEnum.ENTERPRISE
  }
  packageListAll(param).then((res: KeyValue) => {
    buildVersionData(res)
  }).finally(() => {
    targetSelData.loading = false
  })
}

const buildVersionData = (res: KeyValue) => {
  const majorVersionSet = new Set<string>()
    res.data.map((item: KeyValue) => {
      majorVersionSet.add(item.packageVersionNum)
      const versionNumStr = item.packageVersionNum.replaceAll('.', '')
      item.vNum = Number(versionNumStr)
      if (item.packageUrl && item.packageUrl.indexOf('opengauss.obs.cn-south-1.myhuaweicloud.com') >= 0) {
        item.isOffical = true
      }
    })
    const result: KeyValue[] = []
    const allKeys: string[] = []
    majorVersionSet.forEach((key: string) => {
      const filteredList = res.data.filter((item: KeyValue) => item.packageVersionNum === key)
      result.push({
        versionKey: key,
        versionInfo: filteredList
      })
      allKeys.push(key)
    })
    targetSelData.activeKey.push(...allKeys)
    result.sort((x: KeyValue, y: KeyValue) => {
      const xKey = Number(x.versionKey.replaceAll('.', ''))
      const yKey = Number(y.versionKey.replaceAll('.', ''))
      return xKey < yKey ? 1 : -1
    })
    targetSelData.versionList = result
}

const handleClickCluster = (item: KeyValue) => {
  if (!item.disabled) {
    if (item.isChecked) {
      const selectedClusters = data.clusterList.filter((cluster: KeyValue) => cluster.isChecked)
      if (selectedClusters.length <= 1) {
        return
      }
    }
    item.isChecked = !item.isChecked
    const sysInfo = getSysInfo(item.clusterId).split(' ')
    data.selectedOs = sysInfo[0]
    data.selectedCpuArch = sysInfo[1]
    const selectedClusters = data.clusterList.filter((cluster: KeyValue) => cluster.isChecked)
    const sourceVersion:number[] = []
    selectedClusters.map((item:KeyValue) => sourceVersion.push(item.vNum))
    data.selectedSourceVersion = sourceVersion
    filterCluster()
    filterTargetVersion()
  }
}

const filterTargetVersion = () => {
  const versionInfoList: KeyValue[] = []
  targetSelData.versionList.map(item => {
    versionInfoList.push(...item.versionInfo)
  })
  const maxVersion = Math.max.apply(null, data.selectedSourceVersion)
  versionInfoList.map(version => {
    if ((data.selectedOs && (version.os !== data.selectedOs)) || 
        (data.selectedCpuArch && (version.cpuArch !== data.selectedCpuArch)) || 
        (version.vNum && data.selectedSourceVersion.length > 0 && (version.vNum <= maxVersion))) {
      version.disabled = true
      version.isChecked = false
    } else {
      version.disabled = false
    }
  })
}

const filterCluster = () => {
  data.clusterList.map((cluster: KeyValue) => {
    const masterNode = cluster.clusterNodes.find((item: KeyValue) => item.clusterRole === ClusterRoleEnum.MASTER)
    if ((data.selectedOs && (masterNode.hostOs !== data.selectedOs)) || 
        (data.selectedCpuArch && (masterNode.hostCpuArch !== data.selectedCpuArch)) || 
        (cluster.vNum && data.selectedTargetVersion && (cluster.vNum >= data.selectedTargetVersion)) ||
        cluster.clusterState !== ClusterState.normal) {
      cluster.disabled = true
      cluster.isChecked = false
    } else {
      cluster.disabled = false
    }
  })
}

const goToInstall = () => {
  router.push({
    path: '/ops/install'
  })
}

const goToPackage = () => {
  router.push({
    name: 'PackageManage',
    params: { backUrl: '/ops/upgrade' }
  })
}

const enterpriseClusterState = (stateStr: string) => {
  switch (stateStr) {
    case ClusterState.normal:
      return t('operation.DailyOps.5mplp1xbz600')
    case ClusterState.unavailable:
      return t('operation.DailyOps.5mplp1xc4x80')
    case ClusterState.degraded:
      return t('operation.DailyOps.nodeField1')
    default:
      return t('operation.DailyOps.5mplp1xc51s0')
  }
}

const enterpriseClusterStateColor = (stateStr: string) => {
  switch (stateStr) {
    case ClusterState.normal:
      return 'green'
    case ClusterState.unavailable:
    case ClusterState.degraded:
      return 'red'
    default:
      return 'gray'
  }
}

const calcClusterNode = (clusterData: KeyValue) => {
  if (clusterData.clusterNodes.length <= 1) {
    return t('upgrade.UpgradeConifg.5uuo00svxfo0')
  }
  return `${t('upgrade.UpgradeConifg.else6')}${clusterData.clusterNodes.length - 1}${t('upgrade.UpgradeConifg.else7')}`
}

const getMainNodeIp = (clusterData: KeyValue) => {
  const masterNode = clusterData.clusterNodes.find((item: KeyValue) => item.clusterRole === ClusterRoleEnum.MASTER)
  if (masterNode) {
    return masterNode.publicIp
  }
}

const getSysInfo = (clusterId: string) => {
  const cluster = data.clusterList.find((item: KeyValue) => item.clusterId === clusterId)
  if (cluster) {
    const masterNode = cluster.clusterNodes.find((item: KeyValue) => item.clusterRole === ClusterRoleEnum.MASTER)
    if (masterNode) {
      return masterNode.hostOs + ' ' + masterNode.hostCpuArch
    }
  }
  return ''
}

const saveStore = () => {
  const upClusterList: upClusterInfo[] = []
  data.clusterList.map((item: KeyValue) => {
    if (item.isChecked) {
      if (item.clusterNodes.length > 0) {
        const masterNode = item.clusterNodes.find((item: KeyValue) => item.clusterRole === ClusterRoleEnum.MASTER)
        const rootPassword = masterNode.isRemember ? '' : item.rootPassword
        upClusterList.push({
          id: item.clusterId,
          currentVersion: item.versionNum,
          rootPassword: rootPassword,
          hostList: getHostList(item)
        })
      }
    }
  })
  const versionInfoList: KeyValue[] = []
  targetSelData.versionList.map(item => {
    versionInfoList.push(...item.versionInfo)
  })
  const selectedTargetVersion = versionInfoList.find(item => item.isChecked)
  if (selectedTargetVersion) {
    upgradeStore.setUpgradeContext(upClusterList, {
      packageId: selectedTargetVersion.packageId,
      packageUrl: selectedTargetVersion.packageUrl,
      packagePath: selectedTargetVersion.packagePath,
      packageVersionNum: selectedTargetVersion.packageVersionNum
    })
  }
}

const restoreSelectedClusterList = () => {
  if (upgradeStore.getUpClusterList.length > 0) {
    data.selectedSourceVersion = []
    upgradeStore.getUpClusterList.map((cluster: KeyValue) => {
      const result = data.clusterList.find((item: KeyValue) => item.clusterId === cluster.id)
      const masterNode = result.clusterNodes.find((item: KeyValue) => item.clusterRole === ClusterRoleEnum.MASTER)
      result.isChecked = true
      result.rootPassword = cluster.rootPassword
      if (result) {
        data.selectedOs = masterNode.hostOs
        data.selectedCpuArch = masterNode.hostCpuArch
        data.selectedSourceVersion.push(result.vNum)
      }
    })
  }
}

const restoreSelectedTargetVersion = () => {
  if (upgradeStore.getUpTargetVersion.packageId) {
    const versionInfoList: KeyValue[] = []
    targetSelData.versionList.map(item => {
      versionInfoList.push(...item.versionInfo)
    })
    const result = versionInfoList.find(item => item.packageId === upgradeStore.getUpTargetVersion.packageId)
    if (result) {
      result.isChecked = true
      data.selectedTargetVersion = result.vNum
    }
  }
}

const getHostList = (cluster: KeyValue) => {
  const hostList: hostInfo[] = []
  cluster.clusterNodes.map((item: KeyValue) => {
    if (item.clusterRole === ClusterRoleEnum.MASTER) {
      hostList.push({
        isMaster: true,
        hostId: item.hostId,
        publicIp: item.publicIp
      })
    } else {
      hostList.push({
        hostId: item.hostId,
        publicIp: item.publicIp
      })
    }
  })
  return hostList
}

const handleSearchClusterName = () => {
  data.clusterList = []
  data.clusterList.push(...data.oldData)
  if (!filter.keyword) {
    return
  }
  const filteredList = data.clusterList.filter((item: KeyValue) => item.clusterId.indexOf(filter.keyword) >= 0)
  if (filteredList.length > 0) {
    data.clusterList = filteredList
  } else {
    data.clusterList = []
  }
}

const handleClickVersionItem = (versionInfo: KeyValue) => {
  if (versionInfo.disabled) {
    return
  }
  const versionInfoList: KeyValue[] = []
  targetSelData.versionList.map(item => {
    versionInfoList.push(...item.versionInfo)
  })
  if (!versionInfo.isChecked) {
    versionInfo.isChecked = true
    data.selectedOs = versionInfo.os
    data.selectedCpuArch = versionInfo.cpuArch
    data.selectedTargetVersion = versionInfo.vNum
    versionInfoList.map(item => {
      if (versionInfo.packageId !== item.packageId) {
        item.isChecked = false
      }
    })
  }
  filterCluster()
}

const beforeConfirm = async () => {
  const selectedCluster = data.clusterList.find((item: KeyValue) => item.isChecked)
  if (!selectedCluster) {
    Message.warning(t('upgrade.UpgradeConifg.5uuo00svxlo0'))
    return false
  }
  const versionInfoList: KeyValue[] = []
  targetSelData.versionList.map(item => {
    versionInfoList.push(...item.versionInfo)
  })
  const selectedVersion = versionInfoList.find(version => version.isChecked)
  if (!selectedVersion) {
    Message.warning(t('upgrade.UpgradeConifg.5uuo00svxpc0'))
    return false
  }
  const result = await new Promise((resolve, reject) => {
    clusterFormRef.value.validate((err: any) => {
      resolve(err)
    })
  })
  if (result) {
    Message.warning(t('upgrade.UpgradeConifg.5uuo00svwy40'))
    return false
  }
  return true
}

onMounted(() => {
  data.clusterLoading = true
  targetSelData.loading = true
  const param = {
    type: PackageType.OPENGAUSS,
    packageVersion: OpenGaussVersionEnum.ENTERPRISE
  }
  Promise.all([listEnterpriseCluster(), packageListAll(param)]).then(res => {
    buildClusterData(res[0])
    buildVersionData(res[1])
    restoreSelectedClusterList()
    restoreSelectedTargetVersion()
    filterCluster()
    filterTargetVersion()
    console.log(data)
    console.log(targetSelData)
  }).finally(() => {
    data.clusterLoading = false
    targetSelData.loading = false
  })
})

defineExpose({ saveStore, beforeConfirm })
</script>
<style lang="less" scoped>
.upgrade-config-c {
  padding: 30px;
  display: flex;
  height: 100%;
  position: relative;
  justify-content: center;
  align-items: center;

  .upgrade-icon {
    z-index: 9999;
    width: 40px;
    height: 40px;
    margin: 0 50px;
  }
}

.sec-title {
  font-size: 20px;
}

.list-spin {
  width: 100%;
  height: 99%;
}

.refresh-button {
  color: rgb(var(--primary-6));
  cursor: pointer;
}

.selected {
  border: 1px rgb(var(--primary-6)) solid !important;
}

.cluster-item {
  width: 100%;
  display: flex;
  align-items: center;

  .cluster-info-c {
    border: 1px rgb(var(--gray-2)) solid;
    padding: 20px;
    flex-grow: 1;

    .cluster-name {
      font-size: 18px;
    }

    &:hover {
      box-shadow: 0 4px 10px rgb(var(--gray-2));
      cursor: pointer;
      transition: all 0.3s ease-in-out;
    }

    .offical-icon {
      width: 40px;
      height: 40px;
    }
  }
}

.target-version-c {
  width: 50%;

  .target-select {
    height: 60vh;
    overflow-y: auto;
  }
}

.version-title {
  margin-bottom: 20px;
}

.version-item {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.icon-size {
  width: 130px;
  height: 130px;
}

.cluster-select-c {
  width: 50%;

  .select-cluster-top-left {
    display: flex;
    flex-shrink: 0;
    align-items: center;
  }

  .cluster-list-c {
    height: 60vh;
    overflow-y: auto;
  }
}

:deep(.arco-form-item) {
  margin-bottom: 0 !important;
}
</style>