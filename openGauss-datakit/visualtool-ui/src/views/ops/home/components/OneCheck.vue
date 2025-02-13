<template>
  <a-drawer
    :width="800"
    :mask-closable="false"
    :footer="false"
    :visible="checkData.checkVisible"
    @cancel="handleCheckCancel"
    unmountOnClose
  >
    <template #title>
      {{ $t('components.OneCheck.5mpiji1q2ng0') }}
    </template>
    <div class="one-check-c">
      <div class="flex-between mb">
        <a-form
          :model="checkData"
          layout="inline"
          :rules="formRules"
          ref="checkFormRef"
        >
          <a-form-item
            field="clusterId"
            :label="$t('components.OneCheck.clusterName')"
          >
            <a-select
              v-if="checkData.isChoose"
              style="width: 170px;"
              :loading="checkData.clusterListLoading"
              v-model="checkData.clusterId"
              :placeholder="$t('components.OneCheck.5mpiji1q3wo0')"
            >
              <a-option
                v-for="(item, index) in checkData.clusterList"
                :key="index"
                :label="item.label"
                :value="item.value"
              />
            </a-select>
            <div v-else>{{ checkData.clusterId }}</div>
          </a-form-item>
        </a-form>
        <a-button
          type="primary"
          :loading="checkData.checkLoading"
          :disabled="checkData.clusterListLoading"
          @click="handleCheck"
        >{{ checkData.checkResult ?
          $t('components.OneCheck.5mpiji1q4a00') : $t('components.OneCheck.5mpiji1q4k40')
        }}</a-button>
      </div>
      <a-spin
        class="full-w"
        :loading="checkData.loading"
        :tip="$t('components.OneCheck.5mpiji1q4tg0')"
      >
        <div v-show="!checkData.checkResult">
          <a-row
            :gutter="24"
            class="mb"
          >
            <a-col :span="12">
              <div
                class="check-item-c"
                style="height: 365px;"
              >
                <div class="ft-lg ft-b mb">
                  {{ $t('components.OneCheck.5mpiji1q57w0') }}
                </div>
                <ul>
                  <li>{{ $t('components.OneCheck.5mpiji1q5r40') }}</li>
                  <!-- <li>CheckClusterState</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1q5zw0') }}</li>
                  <!-- <li>CheckDBParams</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1q6b00') }}</li>
                  <!-- <li>CheckDebugSwitch</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1q8q80') }}</li>
                  <!-- <li>CheckDirPermissions</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1q96c0') }}</li>
                  <!-- <li>CheckReadonlyMode</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1q9r00') }}</li>
                  <!-- <li>CheckEnvProfile</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qa080') }}</li>
                  <!-- <li>CheckDilateSysTab</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qa940') }}</li>
                  <!-- <li>CheckProStartTime</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qahc0') }}</li>
                  <!-- <li>CheckMpprcFile</li> -->
                </ul>
              </div>
            </a-col>
            <a-col :span="12">
              <div
                class="check-item-c"
                style="height: 365px;"
              >
                <div class="ft-lg ft-b mb">
                  {{ $t('components.OneCheck.5mpiji1qas40') }}
                </div>
                <ul>
                  <li>{{ $t('components.OneCheck.5mpiji1qbv80') }}</li>
                  <!-- <li>CheckCurConnCount</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qc780') }}</li>
                  <!-- <li>CheckCursorNum</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qcjs0') }}</li>
                  <!-- <li>CheckPgxcgroup</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qczo0') }}</li>
                  <!-- <li>CheckSysadminUser</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qdeo0') }}</li>
                  <!-- <li>CheckTableSpace</li> -->
                  <li>{{ $t('components.OneCheck.else1') }}</li>
                  <!-- <li>CheckHashIndex</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qdlk0') }}</li>
                  <!-- <li>CheckPgxcRedistb</li> -->
                  <li>{{ $t('components.OneCheck.else2') }}</li>
                  <!-- <li>CheckNodeGroupName</li> -->
                  <li>{{ $t('components.OneCheck.else3') }}</li>
                  <!-- <li>CheckTDDate</li> -->
                </ul>
              </div>
            </a-col>
          </a-row>
          <a-row
            :gutter="24"
            class="mb"
          >
            <a-col :span="12">
              <div
                class="check-item-c"
                style="height: 680px;"
              >
                <div class="ft-lg ft-b mb">
                  {{ $t('components.OneCheck.5mpiji1qds80') }}
                </div>
                <ul>
                  <li>{{ $t('components.OneCheck.5mpiji1qdy40') }}</li>
                  <!-- <li>CheckMemInfo</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qe8s0') }}</li>
                  <!-- <li>CheckCPU</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qehc0') }}</li>
                  <!-- <li>CheckOSVer</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qeoo0') }}</li>
                  <!-- <li>CheckKeyProAdj</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qf280') }}</li>
                  <!-- <li>CheckMaxProcMemory</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qf9k0') }}</li>
                  <!-- <li>CheckEncoding</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qfgo0') }}</li>
                  <!-- <li>CheckFirewall</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qfnc0') }}</li>
                  <!-- <li>CheckKernelVer</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qfx00') }}</li>
                  <!-- <li>CheckMaxHandle</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qg400') }}</li>
                  <!-- <li>CheckNTPD</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qga80') }}</li>
                  <!-- <li>CheckSysParams</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qgl40') }}</li>
                  <!-- <li>CheckTHP</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qgt80') }}</li>
                  <!-- <li>CheckTimeZone</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qh2k0') }}</li>
                  <!-- <li>CheckSshdService</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qh8s0') }}</li>
                  <!-- <li>CheckSshdConfig</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qhfs0') }}</li>
                  <!-- <li>CheckCrondService</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qhn00') }}</li>
                  <!-- <li>CheckStack</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qhto0') }}</li>
                  <!-- <li>CheckSysPortRange</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qi2s0') }}</li>
                  <!-- <li>CheckHyperThread</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qif40') }}</li>
                  <!-- <li>CheckBootItems</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qioc0') }}</li>
                  <!-- <li>CheckFilehandle</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qizk0') }}</li>
                  <!-- <li>CheckDropCache</li> -->
                </ul>
              </div>
            </a-col>
            <a-col :span="12">
              <div
                class="check-item-c"
                style="height: 350px; margin-bottom: 20px;"
              >
                <div class="ft-lg ft-b mb">
                  {{ $t('components.OneCheck.5mpiji1qjag0') }}
                </div>
                <ul>
                  <li>{{ $t('components.OneCheck.5mpiji1qjh80') }}</li>
                  <!-- <li>CheckBlockdev</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qkn00') }}</li>
                  <!-- <li>CheckDiskFormat</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qkyk0') }}</li>
                  <!-- <li>CheckSwapMemory</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1ql580') }}</li>
                  <!-- <li>CheckLogicalBlock</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qldc0') }}</li>
                  <!-- <li>CheckIOrequestqueue</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qlmc0') }}</li>
                  <!-- <li>CheckMaxAsyIOrequests</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qltc0') }}</li>
                  <!-- <li>CheckSpaceUsage</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qm0s0') }}</li>
                  <!-- <li>CheckInodeUsage</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qmbw0') }}</li>
                  <!-- <li>CheckIOConfigure</li> -->
                </ul>
              </div>
              <div
                class="check-item-c"
                style="height: 310px"
              >
                <div class="ft-lg ft-b mb">
                  {{ $t('components.OneCheck.5mpiji1qmj40') }}
                </div>
                <ul>
                  <li>{{ $t('components.OneCheck.5mpiji1qmqg0') }}</li>
                  <!-- <li>CheckPing</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qn1c0') }}</li>
                  <!-- <li>CheckNetWorkDrop</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qn8g0') }}</li>
                  <!-- <li>CheckNICModel</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qnf00') }}</li>
                  <!-- <li>CheckMTU</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qnks0') }}</li>
                  <!-- <li>CheckRXTX</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qnr00') }}</li>
                  <!-- <li>CheckMultiQueue</li> -->
                  <li>{{ $t('components.OneCheck.5mpiji1qnxs0') }}</li>
                  <!-- <li>CheckRouting</li> -->
                </ul>
              </div>
            </a-col>
          </a-row>
        </div>
        <div v-show="checkData.checkResult">
          <div class="flex-row mb">
            <div class="mr"><span class="mr-s">{{ $t('components.OneCheck.else4') }}:</span> <span>{{
              checkData.finishTime ?
              checkData.finishTime : '--'
            }}</span>
            </div>
            <div class="flex-row"><span class="mr-s">{{ $t('components.OneCheck.else5') }}:</span> <span
                class="ft-lg mr-s"
              >{{ checkData.checkNum
              }}</span>{{ $t('components.OneCheck.5mpiji1qo440') }}</div>
          </div>
          <a-tabs>
            <a-tab-pane key="ng">
              <template #title>
                <div class="flex-row">
                  <svg-icon
                    icon-class="ops-error"
                    class="icon-s mr-s"
                  ></svg-icon>
                  <div class="mr-s">{{ $t('components.OneCheck.5mpiji1qoe00') }}</div>
                  <div
                    class="ft-lg"
                    style="color:red"
                  >{{ checkData.ngData.length }}</div>
                </div>
              </template>
              <a-table
                :data="checkData.ngData"
                :columns="columns"
              >
                <template #type="{ record }">
                  {{ getTypeName(record.name) }}
                </template>
              </a-table>
            </a-tab-pane>
            <a-tab-pane key="error">
              <template #title>
                <div class="flex-row">
                  <svg-icon
                    icon-class="ops-error"
                    class="icon-s mr-s"
                  ></svg-icon>
                  <div class="mr-s">{{ $t('components.OneCheck.5mpiji1qom40') }}</div>
                  <div
                    class="ft-lg"
                    style="color:red"
                  >{{ checkData.errorData.length }}</div>
                </div>
              </template>
              <a-table
                :data="checkData.errorData"
                :columns="columns"
              >
                <template #type="{ record }">
                  {{ getTypeName(record.name) }}
                </template>
              </a-table>
            </a-tab-pane>
            <a-tab-pane key="warning">
              <template #title>
                <div class="flex-row">
                  <svg-icon
                    icon-class="ops-warning"
                    class="icon-s mr-s"
                  ></svg-icon>
                  <div class="mr-s">{{ $t('components.OneCheck.5mpiji1qosc0') }}</div>
                  <div
                    class="ft-lg"
                    style="color: orange"
                  >{{ checkData.warningData.length }}</div>
                </div>
              </template>
              <a-table
                :data="checkData.warningData"
                :columns="columns"
              >
                <template #type="{ record }">
                  {{ getTypeName(record.name) }}
                </template>
              </a-table>
            </a-tab-pane>
            <a-tab-pane key="pass">
              <template #title>
                <div class="flex-row">
                  <svg-icon
                    icon-class="ops-pass"
                    class="icon-s mr-s"
                  ></svg-icon>
                  <div class="mr-s">{{ $t('components.OneCheck.5mpiji1qoyw0') }}</div>
                  <div
                    class="ft-lg"
                    style="color: green"
                  >{{ checkData.passData.length }}</div>
                </div>
              </template>
              <a-table
                :data="checkData.passData"
                :columns="columns"
              >
                <template #type="{ record }">
                  {{ getTypeName(record.name) }}
                </template>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-spin>
    </div>
  </a-drawer>
</template>
<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { reactive, ref } from 'vue'
import { clusterList, clusterCheck } from '@/api/ops'
import { oneCheck } from './oneCheck'
import { Message, TableColumnData } from '@arco-design/web-vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'
const { t } = useI18n()
const checkData = reactive<KeyValue>({
  loading: false,
  checkVisible: false,
  checkLoading: false,
  checkResult: false,
  clusterId: '',
  isChoose: true,
  clusterList: [],
  clusterObj: {},
  clusterListLoading: false,
  finishTime: '',
  checkNum: 0,
  errorData: [],
  warningData: [],
  passData: [],
  ngData: []
})

const columns: TableColumnData[] = [
  { title: t('components.OneCheck.5mpiji1qpcc0'), dataIndex: 'type', slotName: 'type', width: 100 },
  { title: t('components.OneCheck.5mpiji1qpns0'), dataIndex: 'name', tooltip: true, width: 180 },
  { title: t('components.OneCheck.5mpiji1qpvg0'), dataIndex: 'msg', ellipsis: true, tooltip: true }
]

const formRules = computed(() => {
  return {
    clusterId: [{ required: true, 'validate-trigger': 'change', message: t('components.OneCheck.5mpiji1q3wo0') }]
  }
})

const getClusterList = () => new Promise(resolve => {
  checkData.clusterList = []
  checkData.clusterListLoading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      res.data.forEach((item: KeyValue) => {
        if (item.version === OpenGaussVersionEnum.ENTERPRISE) {
          checkData.clusterList.push({
            label: item.clusterId,
            value: item.clusterId
          })
          checkData.clusterObj[item.clusterId] = item
        }
      })
    } else resolve(false)
  }).finally(() => {
    checkData.clusterListLoading = false
  })
})

const checkFormRef = ref<null | FormInstance>(null)
const handleCheck = () => {
  checkFormRef.value?.validate().then(result => {
    if (!result) {
      console.log('form validate', result)
      // call interface
      if (!checkData.clusterList.length && checkData.isChoose) {
        Message.warning('please choose cluster')
        return
      }
      if (checkData.clusterId) {
        checkData.loading = true
          const param = {
            clusterId: checkData.clusterId
          }
          clusterCheck(param).then((res: KeyValue) => {
            if (Number(res.code) === 200) {
              checkData.warningData = res.data.summary.WARNING ? res.data.summary.WARNING : []
              checkData.passData = res.data.summary.OK ? res.data.summary.OK : []
              checkData.errorData = res.data.summary.ERROR ? res.data.summary.ERROR : []
              checkData.ngData = res.data.summary.NG ? res.data.summary.NG : []
              checkData.finishTime = getCurrentTime()
              const warningLength = checkData.warningData.length;
              const okLength = checkData.passData.length;
              const errorLength = checkData.errorData.length;
              const ngLength = checkData.ngData.length;
              checkData.checkNum = warningLength + okLength + errorLength + ngLength
              checkData.checkResult = true
            }
          }).finally(() => {
            checkData.loading = false
          })
      }
    }
  })
}

const getCurrentTime = () => {
  const time = new Date()
  return time.getFullYear() + '-' + time.getMonth() + '-' + time.getDate() + ' ' + time.getHours() + ':' + time.getMinutes() + ':' + time.getSeconds()
}

const getTypeName = (name: string) => {
  const temp = name.replace('.', '')
  if (oneCheck.cluster.includes(temp)) {
    return 'CLUSTER'
  }
  if (oneCheck.db.includes(temp)) {
    return 'DB'
  }
  if (oneCheck.network.includes(temp)) {
    return 'NETWORK'
  }
  if (oneCheck.os.includes(temp)) {
    return 'OS'
  }
  if (oneCheck.device.includes(temp)) {
    return 'DEVICE'
  }
}

const handleCheckCancel = () => {
  checkData.clusterId = ''
  checkData.checkVisible = false
}

const open = (clusterId?: string) => {
  if (clusterId) {
    checkData.clusterId = clusterId
    checkData.isChoose = false
  } else {
    checkData.isChoose = true
  }
  getClusterList()
  checkData.checkVisible = true
}

defineExpose({
  open
})

</script>

<style lang="less" scoped>
.one-check-c {
  padding: 20px;

  .check-item-c {
    padding: 20px;
    box-shadow: 0px 0px 8px 0px rgba(0, 0, 0, 0.1);
    border-radius: 2px;
    height: 100%;

    li {
      margin-bottom: 4px;
    }

    .placeholder-h {
      height: 22px;
    }
  }
}
</style>
