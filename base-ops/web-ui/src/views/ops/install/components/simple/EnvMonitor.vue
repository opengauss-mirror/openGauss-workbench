<template>
  <div class="env-monitor-c">
    <div class="flex-col">
      <div class="flex-row-center" style="width: 50%">
        <a-spin class="full-w" :loading="envData.loading" :tip="$t('simple.EnvMonitor.5mpmqpcxjpc0')">
          <div class="flex-col-start">
            <a-alert class="mb" style="width: fit-content;" type="warning"
              v-if="envData.result !== -1 && envData.noPassNum > 0">
              <div class="flex-row">
                {{ $t('simple.EnvMonitor.5mpmqpcxke40') }}
                <a-badge :count="envData.noPassNum" />{{ $t('simple.EnvMonitor.else1') }}
              </div>
            </a-alert>
            <div class="env-item-c flex-between full-w mb">
              <div class="flex-row">
                <a-tag class="mr" color="#86909C">{{ getRoleName(envData.clusterRole) }}</a-tag>
                {{ $t('simple.EnvMonitor.5mpmqpcxkq80') }} {{ envData.privateIp }}({{
                    envData.publicIp
                }})
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
                        <div class="mr">{{ item.name }}:</div>
                        <div>{{ item.value }}</div>
                      </div>
                      <div style="min-width: 500px;" class="flex-row-start">
                        <div class="mr">
                          <icon-check-circle-fill v-if="item.status === hostEnvStatusEnum.NORMAL" style="color: green"
                            :size="20"></icon-check-circle-fill>
                          <icon-exclamation-circle-fill
                            v-if="item.status === hostEnvStatusEnum.WARMING || item.status === hostEnvStatusEnum.ERROR"
                            style="color: orange" :size="20" />
                          <icon-info-circle-fill v-if="item.status === hostEnvStatusEnum.INFO" style="color: gray"
                            :size="20" />
                        </div>
                        <div :style="`color: ` + getMsgColor('hard', item)">{{ item.statusMessage }}</div>
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
                        <div :style="`max-width: 300px; color: ` + getMsgColor('soft', item)">{{ item.statusMessage }}
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
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
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

const noPassNum = ref<number>(0)

let envData = reactive<KeyValue>({
  loading: false,
  isShow: true,
  result: -1,
  errMsg: '',
  noPassNum: 0,
  noPassNumHard: 0,
  noPassNumSoft: 0,
  hostId: '',
  clusterRole: '',
  privateIp: '',
  publicIp: '',
  hardwareEnv: {
    envProperties: []
  },
  softwareEnv: {
    envProperties: []
  }
})

onMounted(() => {
  loadingFunc.toLoading()
  getHostInfo()
  envData.loading = true
  getEnvMonitorData(envData.hostId).then((res: KeyValue) => {
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
  getEnvMonitorData(envData.hostId).then((res: KeyValue) => {
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

const getHostInfo = () => {
  installStore.getMiniConfig.nodeConfigList.forEach(item => {
    envData.hostId = item.hostId
    envData.privateIp = item.privateIp
    envData.publicIp = item.publicIp
    envData.clusterRole = item.clusterRole
  })
}

const getErrorNum = () => {
  envData.hardwareEnv.envProperties.forEach((item: KeyValue) => {
    if (item.status === hostEnvStatusEnum.ERROR || item.status === hostEnvStatusEnum.WARMING) {
      envData.noPassNum = envData.noPassNum + 1
      envData.noPassNumHard = envData.noPassNumHard + 1
    }
  })
  envData.softwareEnv.envProperties.forEach((item: KeyValue) => {
    if (item.status === hostEnvStatusEnum.ERROR || item.status === hostEnvStatusEnum.WARMING) {
      envData.noPassNum = envData.noPassNum + 1
      envData.noPassNumSoft = envData.noPassNumSoft + 1
    }
  })
}

const getMsgColor = (type: string, item: envProps) => {
  switch (item.status) {
    case hostEnvStatusEnum.ERROR:
      if (type === 'soft') {
        return 'red'
      } else {
        return 'orange'
      }
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
const beforeConfirm = () => {
  let result = true
  if (envData.result !== 200) {
    result = false
    Message.warning('If the host fails to be detected, configure the host and re-detect the host for installation')
  }
  return result
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
    background-color: #f2f3f5;
    line-height: 40px;
    border-radius: 4px;
    padding: 0 16px;
  }
}

.hardware-env {
  margin-left: 40px;
}
</style>
