<template>
  <div class="daily-ops-c">
    <div v-if="data.clusterList.length && !data.loading">
      <div v-for="(clusterData, index) in data.clusterList" :key="index">
        <a-spin class="full-w" :loading="clusterData.loading">
          <div class="item-c mb">
            <div class="flex-between mb">
              <div class="flex-row ft-lg ft-b">
                <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xbxl40') }}</div>
                <div class="main-color mr-s">{{ clusterData.clusterId ? clusterData.clusterId : '--' }}</div>
                <div class="main-color mr-s">{{ clusterData.version ? getVersionName(clusterData.version) : '--' }}
                </div>
                <div class="main-color">{{ clusterData.versionNum ? clusterData.versionNum : '--' }}</div>
              </div>
              <div class="flex-row">
                <div class="flex-row ft-b mr" v-if="clusterData.version === OpenGaussVersionEnum.ENTERPRISE">
                  <icon-exclamation-circle />
                  <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xbyi40') }}</div>
                  <div class="mr">{{ clusterData.warningNum ? clusterData.warningNum : 0 }}</div>

                  <a-button type="text" @click="showOneCheck(clusterData)">{{ $t('operation.DailyOps.5mplp1xbyqc0')
                  }}</a-button>
                </div>
                <icon-down class="open-close-c" v-if="!clusterData.isShow" @click="clusterData.isShow = true" />
                <icon-up class="open-close-c" v-else @click="clusterData.isShow = false" />
              </div>
            </div>
            <div v-if="clusterData.isShow">
              <!-- cluster info -->
              <div class="item-cluster-info">
                <div class="flex-row">
                  <svg-icon icon-class="ops-cluster" class="cluster-icon-size mr"></svg-icon>
                  <div class="flex-col-start">
                    <div class="flex-row mb">
                      <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xbyxw0') }}</div>
                      <a-tag :color="getClusterColor(clusterData)">{{
                          getClusterState(clusterData)
                      }}</a-tag>
                    </div>
                    <div class="flex-row">
                      <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xbzew0') }}</div>
                      <div class="flex-row" v-if="clusterData.deployType === 'CLUSTER'">
                        <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xbzmw0') }}</div>
                        <div class="ft-lg ft-b mr-s">{{ clusterData.clusterNodes.length - 1 }}</div>
                        {{ $t('operation.DailyOps.5mplp1xbztc0') }}
                      </div>
                      <div class="ft-b" v-else>{{ $t('operation.DailyOps.5mplp1xc0000') }}</div>
                    </div>
                  </div>
                </div>
                <div class="flex-col-start">
                  <div class="mb">{{ $t('operation.DailyOps.5mplp1xc0640') }}</div>
                  <div class="flex-row">
                    <a-button type="outline" class="mr" @click="handleEnable(clusterData, index)">{{
                        $t('operation.DailyOps.5mplp1xc0c00')
                    }}</a-button>
                    <a-button type="outline" class="mr" @click="handleStop(clusterData)">{{
                        $t('operation.DailyOps.5mplp1xc0i80')
                    }}</a-button>
                    <a-button type="outline" class="mr" @click="handleReset(clusterData, index)">{{
                        $t('operation.DailyOps.5mplp1xc0o40')
                    }}</a-button>
                    <a-button type="outline" class="mr" @click="handleBackupDlg(index)">{{
                        $t('operation.DailyOps.5mplp1xc0u40')
                    }}</a-button>
                    <a-popconfirm :content="$t('operation.DailyOps.5mplp1xc0zo0')" type="warning"
                      :ok-text="$t('operation.DailyOps.5mplp1xc1580')"
                      :cancel-text="$t('operation.DailyOps.5mplp1xc1b00')"
                      @ok="handleUninstall(clusterData, index, false)">
                      <a-button type="outline" status="danger" class="mr">{{ $t('operation.DailyOps.5mplp1xc1gs0')
                      }}</a-button>
                    </a-popconfirm>
                    <a-popconfirm :content="$t('operation.DailyOps.5mplp1xc0zo0')" type="warning"
                      :ok-text="$t('operation.DailyOps.5mplp1xc1580')"
                      :cancel-text="$t('operation.DailyOps.5mplp1xc1b00')"
                      @ok="handleUninstall(clusterData, index, true)">
                      <a-button type="outline" status="danger" class="mr">{{ $t('operation.DailyOps.5mplp1xc1ms0')
                      }}</a-button>
                    </a-popconfirm>
                  </div>
                </div>
              </div>
              <!-- mini cluster -->
              <div v-if="clusterData.version === 'MINIMAL_LIST' && clusterData.deployType === 'CLUSTER'">
                <div class="flex-row">
                  <div class="host-c simple-w">
                    <div class="flex-between mb">
                      <div class="flex-row">
                        <div class="mr">{{ $t('operation.DailyOps.5mplp1xc1t80') }}</div>
                        <div class="">{{ clusterData.clusterNodes[0].azAddress ? clusterData.clusterNodes[0].azAddress :
                            '--'
                        }}</div>
                      </div>
                      <a-dropdown @select="handleHostOper($event, index, 0)">
                        <a-button>{{ $t('operation.DailyOps.5mplp1xc20k0') }}</a-button>
                        <template #content>
                          <a-doption value="start">{{ $t('operation.DailyOps.5mplp1xc25o0') }}</a-doption>
                          <a-doption value="stop">{{ $t('operation.DailyOps.5mplp1xc2b40') }}</a-doption>
                        </template>
                      </a-dropdown>
                    </div>
                    <div class="flex-row-center">
                      <svg-icon icon-class="ops-host" class="host-icon-size mr-lg"></svg-icon>
                      <div class="host-info">
                        <div class="flex-col-start mr">
                          <div class="flex-row mb">
                            <div>{{ $t('operation.DailyOps.5mplp1xc2gw0') }}</div>
                            <div>{{ clusterData.clusterNodes[0].privateIp }}</div>
                          </div>
                          <div class="flex-row mb">
                            <div>{{ $t('operation.DailyOps.5mplp1xc2ls0') }}</div>
                            <div>{{ clusterData.clusterNodes[0].publicIp }}</div>
                          </div>
                          <div class="flex-row mb">
                            <div>{{ $t('operation.DailyOps.5mplp1xc2sw0') }}</div>
                            <div>{{ clusterData.clusterNodes[0].hostname }}</div>
                          </div>
                          <div class="flex-row">
                            <div>{{ $t('operation.DailyOps.5mplp1xc2xs0') }}</div>
                            <div>{{ clusterData.clusterNodes[0].kernel ? clusterData.clusterNodes[0].kernel : '-'
                            }}{{ $t('operation.DailyOps.else1') }}{{ clusterData.clusterNodes[0].memorySize ?
    clusterData.clusterNodes[0].memorySize :
    '-'
}}G</div>
                          </div>
                        </div>
                        <div class="flex-col-start mr">
                          <div class="flex-row mb">
                            <div class="mr-s">CPU:</div>
                            <div>{{ clusterData.clusterNodes[0].cpu ? clusterData.clusterNodes[0].cpu : '--' }}%</div>
                          </div>
                          <div class="flex-row mb">
                            <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc32g0') }}</div>
                            <div>{{ clusterData.clusterNodes[0].memory ? clusterData.clusterNodes[0].memory : '--' }}%
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="instance-c simple-w" v-for="(instance, nodeIndex) in clusterData.clusterNodes"
                    :key="nodeIndex">
                    <div class="node-type">
                      <div class="content">{{ getRoleName(instance.clusterRole) }}</div>
                    </div>
                    <div class="flex-between mb">
                      <div class="flex-row mb-s" style="margin-left: 20px">
                        <div class="mr-s">{{ $t('operation.DailyOps.else2') }}{{ nodeIndex + 1 }}</div>
                        <a-tag :color="getInstanceStateColor(clusterData.clusterNodes[0])">{{
                            getInstanceState(clusterData.clusterNodes[0])
                        }}</a-tag>
                      </div>
                      <div class="flex-row">
                        <a-switch class="mr" v-model="instance.state" checked-value="true" unchecked-value="false"
                          @change="handleInstanceSwitchChange($event, index, nodeIndex)">
                          <template #checked>
                            ON
                          </template>
                          <template #unchecked>
                            OFF
                          </template>
                        </a-switch>
                        <a-dropdown @select="handleInstanceOper($event, index, nodeIndex)">
                          <a-button>{{ $t('operation.DailyOps.5mplp1xc20k0') }}</a-button>
                          <template #content>
                            <a-doption value="restart">{{ $t('operation.DailyOps.5mplp1xc0o40') }}</a-doption>
                          </template>
                        </a-dropdown>
                      </div>
                    </div>
                    <div class="flex-row-center">
                      <svg-icon icon-class="ops-instance" class="host-icon-size mr-lg"></svg-icon>
                      <div class="flex-col-start mr">
                        <div class="flex-row mb">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3740') }}</div>
                          <div>{{ clusterData.clusterNodes[0].session ? clusterData.clusterNodes[0].session : '--' }}{{
                              $t('operation.DailyOps.else3')
                          }}
                          </div>
                        </div>
                        <div class="flex-row ">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3bw0') }}</div>
                          <div>{{ clusterData.clusterNodes[0].connectNum ? clusterData.clusterNodes[0].connectNum : '--'
                          }}{{ $t('operation.DailyOps.else3') }}</div>
                        </div>
                      </div>
                      <div class="flex-col-start">
                        <div class="flex-row mb">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3hs0') }}</div>
                          <div>{{ clusterData.clusterNodes[0].lock ? clusterData.clusterNodes[0].lock : '--' }}{{
                              $t('operation.DailyOps.else3')
                          }}</div>
                        </div>
                        <div class="flex-row">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3no0') }}</div>
                          <div>{{ clusterData.clusterNodes[0].azName ? clusterData.clusterNodes[0].azName : '--' }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <!-- host info -->
              <div class="item-node-info" v-else v-for="(instance, nodeIndex) in clusterData.clusterNodes"
                :key="nodeIndex">
                <div class="host-c node-w">
                  <div class="flex-between mb">
                    <div class="flex-row">
                      <div class="mr">{{ $t('operation.DailyOps.5mplp1xc1t80') }}</div>
                      <div class="">{{ instance.azAddress ? instance.azAddress : '--' }}</div>
                    </div>
                    <a-dropdown @select="handleHostOper($event, index, nodeIndex)">
                      <a-button>{{ $t('operation.DailyOps.5mplp1xc20k0') }}</a-button>
                      <template #content>
                        <a-doption value="start">{{ $t('operation.DailyOps.5mplp1xc25o0') }}</a-doption>
                        <a-doption value="stop">{{ $t('operation.DailyOps.5mplp1xc2b40') }}</a-doption>
                      </template>
                    </a-dropdown>
                  </div>
                  <div class="flex-row-center">
                    <svg-icon icon-class="ops-host" class="host-icon-size mr-lg"></svg-icon>
                    <div class="host-info">
                      <div class="flex-col-start mr">
                        <div class="flex-row mb">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc2gw0') }}</div>
                          <div>{{ instance.privateIp }}</div>
                        </div>
                        <div class="flex-row">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc2ls0') }}</div>
                          <div>{{ instance.publicIp }}</div>
                        </div>
                      </div>
                      <div class="flex-col-start mr">
                        <div class="flex-row mb">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc2sw0') }}</div>
                          <div>{{ instance.hostname }}</div>
                        </div>
                        <div class="flex-row">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc2xs0') }}</div>
                          <div>{{ instance.kernel ? instance.kernel : '-' }}{{ $t('operation.DailyOps.else1') }}{{
                              instance.memorySize ?
                                instance.memorySize : '-'
                          }}G</div>
                        </div>
                      </div>
                      <div class="flex-col-start mr">
                        <div class="flex-row mb">
                          <div class="mr-s">CPU</div>
                          <div>{{ instance.cpu ? instance.cpu : '--' }}%</div>
                        </div>
                        <div class="flex-row">
                          <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc32g0') }}</div>
                          <div>{{ instance.memory ? instance.memory : '--' }}%</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="instance-c node-w">
                  <div class="node-type">
                    <div class="content">{{ getRoleName(instance.clusterRole) }}</div>
                  </div>
                  <div class="flex-between">
                    <div class="flex-row mb-s" style="margin-left: 20px">
                      <div class="mr-s">{{ $t('operation.DailyOps.else2') }}{{ nodeIndex + 1 }}</div>
                      <a-tag :color="getInstanceStateColor(instance)">{{
                          getInstanceState(instance)
                      }}</a-tag>
                    </div>
                    <div class="flex-row">
                      <a-switch class="mr" v-model="instance.state" checked-value="true" unchecked-value="false"
                        @change="handleInstanceSwitchChange($event, index, nodeIndex)">
                        <template #checked>
                          ON
                        </template>
                        <template #unchecked>
                          OFF
                        </template>
                      </a-switch>
                      <a-dropdown @select="handleInstanceOper($event, index, nodeIndex)">
                        <a-button>{{ $t('operation.DailyOps.5mplp1xc20k0') }}</a-button>
                        <template #content>
                          <a-doption v-for="(itemOption, index) in getDropdownList(clusterData, instance)"
                            :value="itemOption.value" :key="index">{{ itemOption.label }}</a-doption>
                        </template>
                      </a-dropdown>
                    </div>
                  </div>
                  <div class="flex-row-center">
                    <svg-icon icon-class="ops-instance" class="host-icon-size mr-lg"></svg-icon>
                    <div class="flex-col-start mr">
                      <div class="flex-row mb">
                        <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3740') }}</div>
                        <div>{{ instance.session ? instance.session : '--' }}{{ $t('operation.DailyOps.else3') }}</div>
                      </div>
                      <div class="flex-row ">
                        <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3bw0') }}</div>
                        <div>{{ instance.connectNum ? instance.connectNum : '--' }}{{ $t('operation.DailyOps.else3') }}
                        </div>
                      </div>
                    </div>
                    <div class="flex-col-start">
                      <div class="flex-row mb">
                        <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3hs0') }}</div>
                        <div>{{ instance.lock ? instance.lock : '--' }}{{ $t('operation.DailyOps.else3') }}</div>
                      </div>
                      <div class="flex-row">
                        <div class="mr-s">{{ $t('operation.DailyOps.5mplp1xc3no0') }}</div>
                        <div>{{ instance.azName ? instance.azName : '--' }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-spin>
      </div>
    </div>
    <div class="full-w full-h flex-col" v-if="data.clusterList.length === 0 && !data.loading">
      <svg-icon icon-class="ops-empty" class="empty-icon-size mb"></svg-icon>
      <div class="empty-content mb">{{ $t('operation.DailyOps.5mplp1xc3ss0') }}</div>
      <a-button type="outline" size="large" @click="$router.push({ name: 'OpsInstall' })">{{
          $t('operation.DailyOps.5mplp1xc3xg0')
      }}</a-button>
    </div>
    <div class="full-h flex-col" v-if="data.loading">
      <a-spin :tip="$t('operation.DailyOps.5mplp1xc4200')" />
    </div>
    <cluster-backup-dlg ref="backupRef" @finish="handleBackup"></cluster-backup-dlg>
    <one-check ref="oneCheckRef"></one-check>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { uninstallOpenGauss, clusterList, start, stop, restart, switchover, generateconf, build, clusterBackup } from '@/api/ops'
import { useWinBox } from 'vue-winbox'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import { AttachAddon } from 'xterm-addon-attach'
import Socket from '@/utils/websocket'
import { clusterMonitor } from '@/api/ops'
import ClusterBackupDlg from '@/views/monitor/operation/ClusterBackupDlg.vue'
import { ClusterRoleEnum, OpenGaussVersionEnum } from '@/types/ops/install'
import OneCheck from '@/views/ops/home/components/OneCheck.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data: {
  loading: boolean,
  clusterList: KeyValue[],
  socketArr: Socket<any, any>[]
} = reactive({
  clusterList: [],
  loading: false,
  socketArr: []
})

onMounted(() => {
  getList()
})

onBeforeUnmount(() => {
  if (data.socketArr.length) {
    data.socketArr.forEach((item: Socket<any, any>) => {
      if (item) {
        item.destroy()
      }
    })
  }
})

const getList = () => new Promise(resolve => {
  data.loading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      data.clusterList = []
      res.data.forEach((item: KeyValue, index: number) => {
        item.isShow = true
        item.state = -1
        item.loading = false
        if (item.version === 'MINIMAL_LIST' && item.deployType === 'CLUSTER') {
          const slaveNode = JSON.parse(JSON.stringify(item.clusterNodes[0]))
          slaveNode.clusterRole = ClusterRoleEnum.SLAVE
          item.clusterNodes.push(slaveNode)
        }
        openWebSocket(item, index)
        data.clusterList.push(item)
      })
    } else resolve(false)
  }).finally(() => {
    data.loading = false
  })
})

const oneCheckRef = ref<null | InstanceType<typeof OneCheck>>(null)
const showOneCheck = (clusterData: KeyValue) => {
  oneCheckRef.value?.open(clusterData.clusterId)
}

const getVersionName = (version: string) => {
  switch (version) {
    case 'MINIMAL_LIST':
      return t('operation.DailyOps.5mplp1xc46o0')
    case 'LITE':
      return t('operation.DailyOps.5mplp1xc4b40')
    default:
      return t('operation.DailyOps.5mplp1xc4fg0')
  }
}

const getRoleName = (type: ClusterRoleEnum) => {
  switch (type) {
    case ClusterRoleEnum.MASTER:
      return t('operation.DailyOps.5mplp1xc4jw0')
    case ClusterRoleEnum.SLAVE:
      return t('operation.DailyOps.5mplp1xbztc0')
    default:
      return t('operation.DailyOps.5mplp1xc4oc0')
  }
}

// open webSocket
const openWebSocket = (data: KeyValue, clusterIndex: number) => {
  if (data.version === 'MINIMAL_LIST' && data.deployType === 'CLUSTER') {
    // mini cluster
    data.clusterNodes[0].state = -1
    openHostWebSocket(data, data.clusterNodes[0], clusterIndex, 0)
  } else {
    data.clusterNodes.forEach((item: KeyValue, index: number) => {
      item.state = -1
      // open websocket
      openHostWebSocket(data, item, clusterIndex, index)
    })
  }
}

const openHostWebSocket = (clusterData: KeyValue, nodeData: KeyValue, clusterIndex: number, index: number) => {
  const socketKey = new Date().getTime()
  const param = {
    clusterId: clusterData.clusterId,
    hostId: nodeData.hostId,
    privateIp: nodeData.privateIp,
    businessId: 'monitor_ops_' + clusterData.clusterId + '_' + nodeData.hostId + '_' + socketKey
  }
  const websocket = new Socket({ url: `${param.businessId}` })
  websocket.onopen(() => {
    clusterMonitor(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        data.clusterList[clusterIndex].clusterNodes[index].state = 'false'
        websocket.destroy()
      } else {
        // websocket push socketArr
        data.socketArr.push(websocket)
      }
    }).catch(() => {
      data.clusterList[clusterIndex].clusterNodes[index].state = 'false'
      websocket.destroy()
    })
  })
  websocket.onmessage((messageData: any) => {
    const eventData = JSON.parse(messageData)
    console.log(`ops monitor data- ${clusterData.clusterId} - ${clusterData.privateIp}`, eventData, clusterIndex, index)
    data.clusterList[clusterIndex].clusterNodes[index].state = eventData.state
    data.clusterList[clusterIndex].clusterNodes[index].cpu = eventData.cpu
    data.clusterList[clusterIndex].clusterNodes[index].memory = eventData.memory
    data.clusterList[clusterIndex].clusterNodes[index].lock = eventData.lock
    data.clusterList[clusterIndex].clusterNodes[index].session = eventData.session
    data.clusterList[clusterIndex].clusterNodes[index].connectNum = eventData.connectNum
    data.clusterList[clusterIndex].clusterNodes[index].kernel = eventData.kernel
    if (eventData.memorySize) {
      if (!isNaN(Number(eventData.memorySize))) {
        data.clusterList[clusterIndex].clusterNodes[index].memorySize = (Number(eventData.memorySize) / 1024 / 1024).toFixed(2)
      }
    }
  })
}

const handleUninstall = (clusterData: KeyValue, index: number, force: boolean) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `uninstall_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `uninstall_${clusterData.clusterId}_${socketKey}`,
      force: force
    }
    uninstallOpenGauss(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'uninstall')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      if (force) {
        data.clusterList.splice(index, 1)
      } else {
        const flag = Number(messageData.split(':')[1])
        if (flag === 0) {
          data.clusterList.splice(index, 1)
        }
      }
      clusterData.loading = false
      webSocket.destroy()
    }
  })
}

const handleEnable = (clusterData: KeyValue, index: number) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `start_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `start_${clusterData.clusterId}_${socketKey}`
    }
    start(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'start')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln('enable success')
        openWebSocket(clusterData, index)
      } else {
        term.writeln('enable failed')
      }
    }
  })
}

const handleStop = (clusterData: KeyValue) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `stop_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `stop_${clusterData.clusterId}_${socketKey}`
    }
    stop(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'stop')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln('stop success')
      } else {
        term.writeln('stop failed')
      }
    }
  })
}

const handleReset = (clusterData: KeyValue, index: number) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `restart_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `restart_${clusterData.clusterId}_${socketKey}`
    }
    restart(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'restart')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln('reset success')
        openWebSocket(clusterData, index)
      } else {
        term.writeln('reset failed')
      }
    }
  })
}

const backupRef = ref<null | InstanceType<typeof ClusterBackupDlg>>(null)
const backupClusterIndex = ref()
const handleBackupDlg = (clusterIndex: number) => {
  console.log('index: ', clusterIndex, data.clusterList[clusterIndex].clusterId)
  backupClusterIndex.value = clusterIndex
  backupRef.value?.open(data.clusterList[clusterIndex].clusterId)
}

const handleBackup = (backupData: KeyValue) => {
  const clusterData = data.clusterList[backupClusterIndex.value]
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `backup_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      backupPath: backupData.backupPath,
      remark: backupData.remark,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `backup_${clusterData.clusterId}_${socketKey}`
    }
    clusterBackup(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'backup')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln('backup success')
      } else {
        term.writeln('backup failed')
      }
    }
  })
}

const createXterm = (idName: string) => {
  const div = document.createElement('div')
  const divId = document.createAttribute('id')
  divId.value = idName
  div.setAttributeNode(divId)
  const divClass = document.createAttribute('class')
  divClass.value = 'xterm'
  div.setAttributeNode(divClass)
  const styleClass = document.createAttribute('style')
  div.setAttributeNode(styleClass)
  div.style.marginTop = '35px'
}

const getTermObj = (): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows: 40,
    cols: 100,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  })
}

const initTerm = (term: Terminal, socket: Socket<any, any> | null, xtermId: string) => {
  if (socket) {
    const attachAddon = new AttachAddon(socket.ws)
    const fitAddon = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon)
    term.open(document.getElementById(xtermId) as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
  } else {
    const fitAddon = new FitAddon()
    term.loadAddon(fitAddon)
    term.open(document.getElementById(xtermId) as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
  }
}

const createWinbox = (clusterData: KeyValue, type?: string) => {
  const createWindow = useWinBox()
  createWindow({
    id: clusterData.clusterId,
    title: clusterData.clusterId + '_' + type,
    mount: document.getElementById(clusterData.clusterId),
    class: ['custom-winbox', 'no-full', 'no-max'],
    background: '#1D2129',
    x: 'center',
    y: 'center',
    width: '50%',
    height: '50%',
    onClose: function () {
      clusterData.loading = false
    },
    onminimize: function () {
      const oldClass = this.window?.getAttribute('class')
      if (oldClass) {
        const newClass = oldClass.replace('custom-winbox', 'custom-winbox-mini')
        this.window?.setAttribute('class', newClass)
      }
    },
    onrestore: function () {
      const oldClass = this.window?.getAttribute('class')
      if (oldClass) {
        const newClass = oldClass.replace('custom-winbox-mini', 'custom-winbox')
        this.window?.setAttribute('class', newClass)
      }
    }
  })
}

// host oper
const handleHostOper = (type: any, clusterIndex: number, nodeIndex: number) => {
  console.log('host type', type);
  handleInstanceOper(type, clusterIndex, nodeIndex)
}

// instance start or stop
const handleInstanceSwitchChange = (type: any, clusterIndex: number, nodeIndex: number) => {
  console.log('instance switch', type, clusterIndex, nodeIndex)
  let operType = type ? 'start' : 'stop'
  handleInstanceOper(operType, clusterIndex, nodeIndex)
}

// instance oper
const handleInstanceOper = (type: any, clusterIndex: number, nodeIndex: number) => {
  data.clusterList[clusterIndex].loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `${type}_instance_${data.clusterList[clusterIndex].clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: data.clusterList[clusterIndex].clusterId,
      nodeIds: [data.clusterList[clusterIndex].clusterNodes[nodeIndex].nodeId],
      wsConnectType: 'COMMAND_EXEC',
      businessId: `${type}_instance_${data.clusterList[clusterIndex].clusterId}_${socketKey}`
    }
    const method = getInstanceMethod(type, param)
    method?.then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        // Restore the original status if the stop/start fails
        if (type === 'start') {
          data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'false'
        } else if (type === 'stop') {
          data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'true'
        }
        webSocket.destroy()
      } else {
        createXterm(data.clusterList[clusterIndex].clusterId)
        createWinbox(data.clusterList[clusterIndex], type)
        initTerm(term, webSocket, data.clusterList[clusterIndex].clusterId)
      }
    }).catch(() => {
      if (type === 'start') {
        data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'false'
      } else if (type === 'stop') {
        data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'true'
      }
      data.clusterList[clusterIndex].loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      data.clusterList[clusterIndex].loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln(type + ' success')
        if (type === 'restart' || type === 'start') {
          openHostWebSocket(data.clusterList[clusterIndex], data.clusterList[clusterIndex].clusterNodes[nodeIndex], clusterIndex, nodeIndex)
        }
      } else {
        if (type === 'start') {
          data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'false'
        } else if (type === 'stop') {
          data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'true'
        }
        term.writeln(type + ' failed')
      }
    }
  })
}

const getInstanceMethod = (type: string, param: KeyValue) => {
  switch (type) {
    case 'start':
      return start(param)
    case 'stop':
      return stop(param)
    case 'restart':
      return restart(param)
    case 'switch':
      return switchover(param)
    case 'config':
      return generateconf(param)
    case 'build':
      return build(param)
  }
}

const getClusterState = (clusterData: KeyValue) => {
  // all nodes is ok, cluster is ok
  if (clusterData.clusterNodes.length) {
    const findOkNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
      return item.state === 'true'
    })
    if (findOkNodes.length) {
      return t('operation.DailyOps.5mplp1xbz600')
    } else {
      const findHasCheckNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
        return item.state !== -1
      })
      if (findHasCheckNodes.length) {
        return t('operation.DailyOps.5mplp1xc4x80')
      } else {
        return t('operation.DailyOps.5mplp1xc51s0')
      }
    }
  }
  return t('operation.DailyOps.5mplp1xc51s0')
}

const getClusterColor = (clusterData: KeyValue) => {
  // all nodes is ok, cluster is ok
  if (clusterData.clusterNodes.length) {
    const findOkNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
      return item.state === 'true'
    })
    if (findOkNodes.length) {
      return 'green'
    } else {
      const findHasCheckNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
        return item.state !== -1
      })
      if (findHasCheckNodes.length) {
        return 'red'
      } else {
        return 'gray'
      }
    }
  }
  return 'gray'
}

const getInstanceState = (instanceData: KeyValue) => {
  if (instanceData.state === -1) {
    return t('operation.DailyOps.5mplp1xc51s0')
  } else {
    if (instanceData.state === 'true') {
      return t('operation.DailyOps.5mplp1xbz600')
    } else {
      return t('operation.DailyOps.5mplp1xc4x80')
    }
  }
}

const getInstanceStateColor = (instanceData: any) => {
  if (instanceData.state === -1) {
    return 'gray'
  } else {
    if (instanceData.state === 'true') {
      return 'green'
    } else {
      return 'red'
    }
  }
}

const getDropdownList = (clusterData: KeyValue, nodeData: KeyValue) => {
  const result = [{ label: t('operation.DailyOps.5mplp1xc0o40'), value: 'restart' }]
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE && nodeData.clusterRole === ClusterRoleEnum.SLAVE) {
    result.push({ label: t('operation.DailyOps.5mplp1xc56k0'), value: 'switch' })
    result.push({ label: t('operation.DailyOps.5mplp1xc5cg0'), value: 'config' })
    result.push({ label: t('operation.DailyOps.nodeBuild'), value: 'build' })
  }
  return result
}

</script>

<style lang="less" scoped>
.main-color {
  color: rgb(var(--primary-6));
}

.daily-ops-c {
  padding: 20px 20px 0px;
  height: calc(100vh - 178px);
  overflow-y: auto;

  .empty-icon-size {
    width: 100px;
    height: 100px;
  }

  .empty-content {
    font-weight: bold;
    color: var(--color-neutral-4);
  }

  .item-c {
    padding: 20px;
    border-radius: 8px;
    background-color: white;

    .item-cluster-info {
      border: 1px solid #f2f3f5;
      background-color: #f8f9fa;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px;
    }

    .host-c {
      padding: 20px;
      border: 1px solid #f2f3f5;
      border-right: 0px;

      .host-info {
        display: flex;
        align-items: flex-start
      }
    }

    .instance-c {
      padding: 20px;
      border: 1px dashed #00b42a;
      position: relative;

      .node-type {
        position: absolute;
        top: 0;
        left: 0;
        border: 24px solid;
        border-color: #00b42a transparent transparent #00b42a;

        .content {
          color: white;
          margin-top: -18px;
          margin-left: -18px;
        }
      }
    }

    .open-close-c {
      cursor: pointer;
    }

    .simple-w {
      width: 33.3%;
    }

    .node-w {
      width: 50%;
    }

    .item-node-info {
      display: flex;
    }
  }

  .cluster-icon-size {
    width: 100px;
    height: 100px;
  }

  .host-icon-size {
    height: 80px;
    width: 80px;
  }
}

.xterm {
  width: 100%;
  height: 80%;
}
</style>
