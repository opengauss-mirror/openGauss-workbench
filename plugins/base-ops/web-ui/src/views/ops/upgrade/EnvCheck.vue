<template>
  <div class="env-check-c">
    <a-collapse
      :default-active-key="data.activeKey"
      v-for="item in data.list"
      :key="item.id"
      class="mb"
    >
      <a-collapse-item :key="item.id">
        <template #header>
          <div class="sec-header">
            <div class="mr-s">{{ `${$t('upgrade.EnvCheck.else1')}: ${item.id} ${item.currentVersion}
                          ${$t('upgrade.EnvCheck.5uuo4bwfpzg0')} ${data.upVersion}` }}</div>
            <icon-code-square @click.stop="handleOpenShell(item)" :size="20" class="mr"/>
            <div
              v-if="!item.checkLoading && item.diskUsed >= -1"
              class="flex-row"
            >
              <div class="mr-s">{{ `${$t('upgrade.EnvCheck.else2')}：${item.diskUsed}%` }}</div>
              <icon-check-circle
                v-if="item.diskUsed < 70"
                style="color: green"
                class="state-icon"
              />
              <icon-exclamation-circle
                v-if="item.diskUsed > 90"
                style="color: red"
                class="state-icon"
              />
              <icon-exclamation-circle
                v-else-if="item.diskUsed >= 70"
                style="color: orange"
                class="state-icon"
              />
            </div>
          </div>
        </template>
        <template #extra>
          <div
            v-if="!item.checkLoading"
            class="recheck"
            @click.stop="handleRecheck(item.id)"
          >
            <div>
              {{ $t('upgrade.EnvCheck.5uuo4bwfr6k0') }}
            </div>
            <icon-refresh />
          </div>
        </template>
        <a-spin
          class="result-area"
          :loading="item.checkLoading"
        >
          <pre class="monitor">{{ item.osCheckResult }}</pre>
        </a-spin>
      </a-collapse-item>
    </a-collapse>
  </div>
  <cluster-control-dlg ref="clusterControlRef"></cluster-control-dlg>
</template>

<script lang="ts" setup>
import { reactive, onMounted, ref } from 'vue'
import { upgradeOsCheck } from '@/api/ops'
import { KeyValue } from '@/types/global'
import { useUpgradeStore, upClusterInfo } from '@/store/modules/ops/upgrade'
import { inject } from 'vue';
import { encryptPassword } from '@/utils/jsencrypt'
import ClusterControlDlg from './ClusterControlDlg.vue'
import { Message } from '@arco-design/web-vue'
const upgradeStore = useUpgradeStore()
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const loadingFunc = inject<any>('loading')
const clusterControlRef = ref()

const data = reactive({
  list: [] as upClusterInfo[],
  activeKey: [] as string[],
  upVersion: ''
})

const checkUpgradeOs = async () => {
  const method = []
  for (let index = 0; index < data.list.length; index++) {
    const cluster = data.list[index];
    let rootPassword = ''
    if (cluster.rootPassword) {
      rootPassword = await encryptPassword(cluster.rootPassword)
    }
    const params = {
      clusterId: cluster.id,
      rootPassword: rootPassword
    }
    method.push(upgradeOsCheck(params))
    data.list[index].checkLoading = true
  }
  loadingFunc.startLoading()
  Promise.all(method).then((res:KeyValue) => {
    for (let index = 0; index < data.list.length; index++) {
      data.list[index].osCheckResult = res[index].data.osInfo
      data.list[index].diskUsed = res[index].data.diskUsed
    }
  }).finally(() => {
    data.list.map(item => {
      item.checkLoading = false
    })
    loadingFunc.cancelLoading()
  })
}

const checkCluster = async (cluster: KeyValue) => {
  let rootPassword = ''
  if (cluster.rootPassword) {
    rootPassword = await encryptPassword(cluster.rootPassword)
  }
  const params = {
    clusterId: cluster.id,
    rootPassword: rootPassword
  }
  cluster.osCheckResult = ''
  cluster.checkLoading = true
  loadingFunc.startLoading()
  upgradeOsCheck(params).then((res: KeyValue) => {
    cluster.osCheckResult = res.data.osInfo
    cluster.diskUsed = res.data.diskUsed
  }).finally(() => {
    cluster.checkLoading = false
    loadingFunc.cancelLoading()
  })
}

const handleRecheck = (id: string) => {
  const cluster = data.list.find(item => item.id === id)
  checkCluster(cluster as KeyValue)
}

const handleOpenShell = (cluster: upClusterInfo) => {
  clusterControlRef.value?.open(cluster.hostList, cluster.rootPassword)
}

onMounted(() => {
  data.list = [...upgradeStore.getUpClusterList]
  data.upVersion = upgradeStore.getUpTargetVersion.packageVersionNum
  const activeKey: string[] = []
  upgradeStore.getUpClusterList.map(item => {
    activeKey.push(item.id)
  })
  data.activeKey = [...activeKey]
  checkUpgradeOs()
})

const saveStore = () => {

}

const beforeConfirm = () => {
  const hasOverLimit = data.list.find(item => item.diskUsed > 90)
  if (hasOverLimit) {
    const masterNode = hasOverLimit.hostList.find(item => item.isMaster)
    if (masterNode) {
      Message.warning(t('upgrade.EnvCheck.5uuo4bwfr6k0', {clusterId: hasOverLimit.id, ip: masterNode.publicIp}))
    }
    return false
  }
  return true
}

defineExpose({ saveStore, beforeConfirm })
</script>
<style lang="less" scoped>
.env-check-c {
  height: 100%;
  overflow-y: auto;
}

.result-area {
  width: 100%;
  height: 400px;

  .monitor {
    background-color: black;
    color: white;
    padding: 15px;
  }
}

.recheck {
  display: flex;
  align-items: center;
  color: rgb(var(--primary-6));
}

.sec-header {
  display: flex;
  align-items: center;
}

.state-icon {
  width: 20px;
  height: 20px;
}

:deep(.arco-collapse-item-content) {
  padding-right: 0px !important;
  padding-left: 0px !important;
}

:deep(.arco-collapse-item-content-box) {
  padding: 0 !important;
}

pre {
  height: 100%;
  margin: 0 !important;
}

:deep(.arco-spin) {
  display: block !important
}
</style>