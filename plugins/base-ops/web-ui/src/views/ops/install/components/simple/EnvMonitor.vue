<template>
  <div class="env-monitor-c">
    <div class="flex-col">
      <div class="flex-row-center" style="width: 50%">
        <a-spin class="label-color full-w" :loading="envData.loading" :tip="$t('simple.EnvMonitor.5mpmqpcxjpc0')">
          <div class="flex-col-start">
            <a-alert class="mb" style="width: fit-content;" type="warning"
              v-if="envData.result !== -1 && envData.noPassNumError > 0">
              <div class="flex-row">
                {{ $t('simple.EnvMonitor.5mpmqpcxke40') }}
                <a-badge :count="envData.noPassNumError" />{{ $t('simple.EnvMonitor.else1') }}
              </div>
            </a-alert>
            <div class="env-item-c flex-between full-w mb">
              <div class="flex-row">
                <a-tag class="title-color mr" color="#86909C">{{ getRoleName(envData.clusterRole) }}</a-tag>
                <div class="label-color">{{ $t('simple.EnvMonitor.5mpmqpcxkq80') }} {{ envData.privateIp }}({{
                  envData.publicIp
                }})</div>
              </div>
              <div class="flex-row">
                <a-button class="mr" type="text" long @click="envRetest(envData)">
                  <template #icon>
                    <icon-refresh />
                  </template>
                  {{ $t('simple.EnvMonitor.5mpmqpcxkzc0') }}
                </a-button>
                <icon-down style="cursor: pointer;" v-if="!envData.isShow" @click="envData.isShow = true" />
                <icon-up style="cursor: pointer;" v-else @click="envData.isShow = false" />
              </div>
            </div>
            <div v-if="envData.isShow">
              <div v-if="envData.result === 200">
                <div class="flex-row mb">
                  <icon-exclamation-circle-fill v-if="envData.noPassNumHard > 0" class="mr" style="color: orange"
                    :size="30"></icon-exclamation-circle-fill>
                  <icon-check-circle-fill v-else style="color: green" class="mr" :size="30"></icon-check-circle-fill>
                  <div class="ft-xlg">{{ $t('simple.EnvMonitor.5mpmqpcxlas0') }}</div>
                </div>
                <div class="hardware-env">
                  <div class="flex-col" v-if="envData.hardwareEnv.envProperties.length">
                    <div class="flex-row mb" v-for="(item, index) in envData.hardwareEnv.envProperties" :key="index">
                      <div style="width: 250px;" class="flex-row">
                        <div class="label-color mr">{{ item.name }}:</div>
                        <div class="label-color">{{ item.value }}</div>
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
                  <icon-exclamation-circle-fill v-if="envData.noPassNumSoft > 0" class="mr" style="color: orange"
                    :size="30"></icon-exclamation-circle-fill>
                  <icon-check-circle-fill v-else style="color: green" class="mr" :size="30"></icon-check-circle-fill>
                  <div class="ft-xlg">{{ $t('simple.EnvMonitor.5mpmqpcxlk40') }}</div>
                </div>
                <div class="hardware-env">
                  <div class="flex-col" v-if="envData.softwareEnv.envProperties.length">
                    <div class="flex-row mb" v-for="(item, index) in envData.softwareEnv.envProperties" :key="index">
                      <div style="width: 250px;" class="flex-row">
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
                        <div :style="`max-width: 400px; color: ${getMsgColor(item)}`">{{ item.statusMessage }}
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-if="envData.result === 500">
                    <a-tag color="red">{{ $t('simple.EnvMonitor.5mpmqpcxlxc0') }}</a-tag>
                  </div>
                </div>
              </div>
              <div v-else-if="envData.result !== -1 && envData.result !== 200">
                {{ $t('simple.EnvMonitor.5mpmqpcxm6o0') }} {{ envData.errMsg }}
              </div>
              <div v-else>
                {{ $t('simple.EnvMonitor.5mpmqpcxmfs0') }}
              </div>
            </div>
          </div>
          <a-modal :title="$t('simple.EnvMonitor.confirm')" @close="resolveAction(false)" type="warning"
            @cancel="resolveAction(false)" @ok="resolveAction(true)" v-model:visible="nextConfirmVisible">
            <div>
              <icon-exclamation-circle-fill style="color: #ff7d00" />
              {{ confirmMessage }}
            </div>
          </a-modal>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { inject, onMounted, reactive, ref } from 'vue'
import { getEnvMonitorData } from '@/api/ops'
import { useOpsStore } from '@/store'
import { KeyValue } from '@/types/global'
import { ClusterRoleEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
import useConfirm from '@/hooks/useConfirm'

const { t } = useI18n()
const installStore = useOpsStore()

const loadingFunc = inject<any>('loading')

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

let envData = reactive<KeyValue>({
  loading: false,
  isShow: true,
  result: -1,
  errMsg: '',
  noPassNum: 0,
  noPassNumHard: 0,
  noPassNumSoft: 0,
  noPassNumError: 0,
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
})

onMounted(async () => {
  loadingFunc.toLoading()
  await getHostInfo()
  envData.loading = true
  const param = {
    expectedOs: installStore.getInstallConfig.installOs,
  }
  getEnvMonitorData(envData.hostId, param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      envData.result = 200
      envData.hardwareEnv.envProperties = res.data.hardwareEnv.envProperties
      envData.softwareEnv.envProperties = res.data.softwareEnv.envProperties
      getErrorNum()
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
})

const envRetest = (envData: KeyValue) => {
  loadingFunc.toLoading()
  envData.loading = true
  const param = {
    expectedOs: installStore.getInstallConfig.installOs,
  }
  getEnvMonitorData(envData.hostId, param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      envData.result = 200
      envData.hardwareEnv.envProperties = res.data.hardwareEnv.envProperties
      envData.softwareEnv.envProperties = res.data.softwareEnv.envProperties
      getErrorNum()
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
  for (let i = 0; i < installStore.getMiniConfig.nodeConfigList.length; i++) {
    const item = installStore.getMiniConfig.nodeConfigList[i]
    envData.hostId = item.hostId
    envData.privateIp = item.privateIp
    envData.publicIp = item.publicIp
    envData.clusterRole = item.clusterRole
  }
}

const getErrorNum = () => {
  envData.noPassNum = 0
  envData.noPassNumHard = 0
  envData.noPassNumSoft = 0
  envData.noPassNumError = 0
  envData.hardwareEnv.envProperties.forEach((item: KeyValue) => {
    if (item.status === hostEnvStatusEnum.ERROR || item.status === hostEnvStatusEnum.WARMING) {
      envData.noPassNum = envData.noPassNum + 1
      envData.noPassNumHard = envData.noPassNumHard + 1
    }
    if (item.status === hostEnvStatusEnum.ERROR) {
      envData.noPassNumError += 1
    }
  })
  envData.softwareEnv.envProperties.forEach((item: KeyValue) => {
    if (item.status === hostEnvStatusEnum.ERROR || item.status === hostEnvStatusEnum.WARMING) {
      envData.noPassNum = envData.noPassNum + 1
      envData.noPassNumSoft = envData.noPassNumSoft + 1
    }
    if (item.status === hostEnvStatusEnum.ERROR) {
      envData.noPassNumError += 1
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
      return t('simple.EnvMonitor.5mpmqpcxmog0')
    case ClusterRoleEnum.SLAVE:
      return t('simple.EnvMonitor.5mpmqpcxmwg0')
    default:
      return t('simple.EnvMonitor.5mpmqpcxn5o0')
  }
}

const {
  nextConfirmVisible,
  confirmMessage,
  waitConfirm,
  resolveAction
} = useConfirm()

const beforeConfirm = async () => {
  if (envData.result !== 200) {
    confirmMessage.value = t('simple.EnvMonitor.failConfirm')
    nextConfirmVisible.value = true
    return await waitConfirm()
  }
  if (envData.noPassNumError > 0) {
    confirmMessage.value = t('simple.EnvMonitor.errorConfirm', { noPassNum: envData.noPassNumError })
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
.title-color {
  color: var(--color-black);
}

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
