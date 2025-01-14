<template>
  <div class="env-monitor-c">
    <div class="flex-col">
      <div class="mb" style="width: 50%;" v-for="(itemEnv, index) in data.nodeData" :key="index">
        <a-spin class="full-w" :loading="itemEnv.loading" :tip="$t('lightweight.EnvMonitor.5mpmgwswi880')">
          <div class="flex-col-start">
            <a-alert class="mb" style="width: fit-content;" type="warning"
              v-if="itemEnv.result !== -1 && itemEnv.noPassNum > 0">
              <div class="flex-row">
                {{ $t('lightweight.EnvMonitor.5mpmgwswj8g0') }}
                <a-badge :count="itemEnv.noPassNum" />{{ $t('lightweight.EnvMonitor.else1') }}
              </div>
            </a-alert>
            <div class="env-item-c flex-between full-w mb">
              <div class="flex-row">
                <a-tag class="mr" color="#86909C">{{ getRoleName(itemEnv.clusterRole) }}</a-tag>
                <div class="label-color">{{ $t('lightweight.EnvMonitor.5mpmgwswjlc0') }}: {{ itemEnv.privateIp }}({{
                  itemEnv.publicIp
                }})</div>
              </div>
              <div class="flex-row">
                <a-button class="mr" type="text" long @click="envRetest(itemEnv)">
                  <template #icon>
                    <icon-refresh />
                  </template>
                  {{ $t('lightweight.EnvMonitor.5mpmgwswjuk0') }}
                </a-button>
                <icon-down style="cursor: pointer;" v-if="!itemEnv.isShow" @click="itemEnv.isShow = true" />
                <icon-up style="cursor: pointer;" v-else @click="itemEnv.isShow = false" />
              </div>
            </div>
            <div v-show="itemEnv.isShow">
              <div v-if="itemEnv.result === 200">
                <div class="flex-row mb">
                  <icon-exclamation-circle-fill v-if="itemEnv.noPassNumHard > 0" class="mr" style="color: orange"
                    :size="30"></icon-exclamation-circle-fill>
                  <icon-check-circle-fill v-else style="color: green" class="mr" :size="30"></icon-check-circle-fill>
                  <div class="label-color ft-xlg">{{ $t('lightweight.EnvMonitor.5mpmgwswk640') }}</div>
                </div>
                <div class="hardware-env">
                  <div class="flex-col" v-if="itemEnv.hardwareEnv.envProperties.length">
                    <div class="flex-row mb" v-for="(item, index) in itemEnv.hardwareEnv.envProperties" :key="index">
                      <div style="width: 250px;" class="label-color flex-row">
                        <div class="mr">{{ item.name }}:</div>
                        <div>{{ item.value }}</div>
                      </div>
                      <div style="min-width: 500px;" class="flex-row-start">
                        <div class="mr">
                          <icon-close-circle-fill v-if="item.status === hostEnvStatusEnum.ERROR" style="color: red"
                            :size="20"></icon-close-circle-fill>
                          <icon-check-circle-fill v-if="item.status === hostEnvStatusEnum.NORMAL" style="color: green"
                            :size="20"></icon-check-circle-fill>
                          <icon-exclamation-circle-fill v-if="item.status === hostEnvStatusEnum.WARMING"
                            style="color: orange" :size="20" />
                          <icon-info-circle-fill v-if="item.status === hostEnvStatusEnum.INFO" style="color: gray"
                            :size="20" />
                        </div>
                        <div :style="`max-width: 400px; color: ${getMsgColor(item)}`">{{ item.statusMessage }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="flex-row mb">
                  <icon-exclamation-circle-fill v-if="itemEnv.noPassNumSoft > 0" class="mr" style="color: orange"
                    :size="30"></icon-exclamation-circle-fill>
                  <icon-check-circle-fill v-else style="color: green" class="mr" :size="30"></icon-check-circle-fill>
                  <div class="label-color ft-xlg">{{ $t('lightweight.EnvMonitor.5mpmgwswkb40') }}</div>
                </div>
                <div class="hardware-env">
                  <div class="flex-col" v-if="itemEnv.softwareEnv.envProperties.length">
                    <div class="flex-row mb" v-for="(item, index) in itemEnv.softwareEnv.envProperties" :key="index">
                      <div style="width: 250px;" class="label-color flex-row">
                        <div class="mr">{{ item.name }}:</div>
                        <div>{{ item.value }}</div>
                      </div>
                      <div style="min-width: 500px;" class="flex-row-start">
                        <div class="mr">
                          <icon-close-circle-fill v-if="item.status === hostEnvStatusEnum.ERROR" style="color: red"
                            :size="20"></icon-close-circle-fill>
                          <icon-check-circle-fill v-if="item.status === hostEnvStatusEnum.NORMAL" style="color: green"
                            :size="20"></icon-check-circle-fill>
                          <icon-exclamation-circle-fill v-if="item.status === hostEnvStatusEnum.WARMING"
                            style="color: orange" :size="20" />
                          <icon-info-circle-fill v-if="item.status === hostEnvStatusEnum.INFO" style="color: gray"
                            :size="20" />
                        </div>
                        <div :style="`max-width: 400px; color: ${getMsgColor(item)}`">{{ item.statusMessage }}</div>
                      </div>
                    </div>
                  </div>
                  <div v-if="itemEnv.result === 500">
                    <a-tag color="red">{{ $t('lightweight.EnvMonitor.5mpmgwswkeo0') }}</a-tag>
                  </div>
                </div>
              </div>
              <div v-else-if="itemEnv.result !== -1 && itemEnv.result !== 200">
                {{ $t('lightweight.EnvMonitor.5mpmgwswki80') }} {{ itemEnv.errMsg }}
              </div>
              <div v-else>
                {{ $t('lightweight.EnvMonitor.5mpmgwswkls0') }}
              </div>
            </div>
          </div>
        </a-spin>
        <a-modal :title="$t('simple.EnvMonitor.confirm')" @close="resolveAction(false)" type="warning"
          @cancel="resolveAction(false)" @ok="resolveAction(true)" v-model:visible="nextConfirmVisible">
          <div>
            <icon-exclamation-circle-fill style="color: #ff7d00" />
            {{ confirmMessage }}
          </div>
        </a-modal>
        <a-divider></a-divider>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { inject, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { getEnvMonitorData } from '@/api/ops'
import { useOpsStore } from '@/store'
import { KeyValue } from '@/types/global'
import { ClusterRoleEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
import useConfirm from '@/hooks/useConfirm'

const { t } = useI18n()
const installStore = useOpsStore()

enum hostEnvStatusEnum {
  NORMAL = 'NORMAL',
  WARMING = 'WARMING',
  ERROR = 'ERROR',
  INFO = 'INFO'
}
type envProps = {
  sortNum: number,
  name: string,
  value: string,
  status: hostEnvStatusEnum,
  statusMessage: string
}

const loadingFunc = inject<any>('loading')

const data: {
  nodeData: KeyValue[]
} = reactive({
  nodeData: []
})

const loadingInterval = ref<any>(null)

onMounted(async () => {
  loadingFunc.toLoading()
  data.nodeData = []
  await getHostInfo()
  data.nodeData.forEach((item) => {
    item.loading = true
    const param = {
      expectedOs: installStore.getInstallConfig.installOs,
    }
    getEnvMonitorData(item.hostId, param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        item.result = 200
        item.hardwareEnv.envProperties = res.data.hardwareEnv.envProperties
        item.softwareEnv.envProperties = res.data.softwareEnv.envProperties
        getErrorNum(item)
      }
    }).catch((err) => {
      item.result = 500
      item.errMsg = err
    }).finally(() => {
      item.loading = false
    })
  })
  loadingInterval.value = setInterval(() => {
    const getExeEnv = data.nodeData.filter((item: KeyValue) => {
      return item.result !== 200 && item.result !== 500
    })
    if (getExeEnv.length === 0) {
      clearInterval(loadingInterval.value)
      loadingFunc.cancelLoading()
    }
  }, 100)
})

onBeforeUnmount(() => {
  if (loadingInterval.value) {
    clearInterval(loadingInterval.value)
  }
})

const envRetest = (envData: KeyValue) => {
  loadingFunc.toLoading()
  envData.loading = true
  const param = {
    expectedOs: installStore.getInstallConfig.installOs
  }
  getEnvMonitorData(envData.hostId, param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      envData.result = 200
      envData.hardwareEnv.envProperties = res.data.hardwareEnv.envProperties
      envData.softwareEnv.envProperties = res.data.softwareEnv.envProperties
      getErrorNum(envData)
    } else {
      envData.result = 500
      envData.errMsg = res.msg
    }
  }).catch((err) => {
    envData.result = 500
    envData.errMsg = err
  }).finally(() => {
    envData.loading = false
    loadingFunc.cancelLoading()
  })
}

const getHostInfo = async () => {
  data.nodeData = []
  for (let i = 0; i < installStore.getLiteConfig.nodeConfigList.length; i++) {
    const item = installStore.getLiteConfig.nodeConfigList[i]
    const isExist = data.nodeData.find(arrItem => {
      return arrItem.privateIp === item.privateIp
    })
    if (!isExist) {
      const tempData = getHostEnvData()
      tempData.hostId = item.hostId
      tempData.privateIp = item.privateIp
      tempData.publicIp = item.publicIp
      tempData.clusterRole = item.clusterRole
      data.nodeData.push(tempData)
    }
  }
}

const getHostEnvData = () => {
  return {
    result: -1,
    errMsg: '',
    noPassNum: 0,
    noPassNumHard: 0,
    noPassNumSoft: 0,
    isShow: true,
    loading: false,
    hostId: '',
    clusterRole: '',
    privateIp: '',
    publicIp: '',
    hardwareEnv: {
      envProperties: []
    },
    softwareEnv: {
      envProperties: []
    },
  }
}

const getErrorNum = (envData: KeyValue) => {
  envData.noPassNum = 0
  envData.noPassNumHard = 0
  envData.noPassNumSoft = 0
  envData.hardwareEnv.envProperties.forEach((item: KeyValue) => {
    if (item.status === hostEnvStatusEnum.ERROR) {
      envData.noPassNum = envData.noPassNum + 1
      envData.noPassNumHard = envData.noPassNumHard + 1
    }
  })
  envData.softwareEnv.envProperties.forEach((item: KeyValue) => {
    if (item.status === hostEnvStatusEnum.ERROR) {
      envData.noPassNum = envData.noPassNum + 1
      envData.noPassNumHard = envData.noPassNumHard + 1
    }
  })
}

const getMsgColor = (item: envProps) => {
  switch (item.status) {
    case hostEnvStatusEnum.ERROR:
      return 'red'
    case hostEnvStatusEnum.INFO:
      return 'gray'
    case hostEnvStatusEnum.NORMAL:
      return 'green'
    default:
      return 'orange'
  }
}

const getRoleName = (type: ClusterRoleEnum) => {
  switch (type) {
    case ClusterRoleEnum.MASTER:
      return t('lightweight.EnvMonitor.5mpmgwswkpc0')
    case ClusterRoleEnum.SLAVE:
      return t('lightweight.EnvMonitor.5mpmgwswkso0')
    default:
      return t('lightweight.EnvMonitor.5mpmgwswkwg0')
  }
}

const {
  nextConfirmVisible,
  confirmMessage,
  waitConfirm,
  resolveAction
} = useConfirm()

const beforeConfirm = async () => {
  const unPass = data.nodeData.filter((item: KeyValue) => {
    return item.result !== 200
  })
  const totalError = data.nodeData.reduce((acc, cur) => acc + cur.noPassNum, 0)
  if (unPass.length > 0) {
    confirmMessage.value = t('simple.EnvMonitor.failConfirm')
    nextConfirmVisible.value = true
    return await waitConfirm()
  }
  if (totalError > 0) {
    confirmMessage.value = t('simple.EnvMonitor.errorConfirm', { noPassNum: totalError })
    nextConfirmVisible.value = true
    return await waitConfirm()
  }
  return true
}

defineExpose({
  beforeConfirm
})

</script>

<style lang="less" scoped>
.env-monitor-c {
  height: calc(100% - 28px - 42px);
  overflow-y: auto;

  .env-item-c {
    background-color: var(--color-text-4);
    line-height: 40px;
    border-radius: 4px;
    padding: 0 16px;
  }
}

.hardware-env {
  margin-left: 40px;
}
</style>
