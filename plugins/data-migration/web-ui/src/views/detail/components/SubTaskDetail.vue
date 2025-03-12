<template>
  <a-drawer v-model:visible="visible" width="60%" :footer="false" :unmount-on-close="true">
    <template #title>
      <div class="title-con">
        <div class="tab-con">
          <span class="tab-item" :class="tab - item - active">{{
            $t('components.SubTaskDetail.5q09ruxan0s0', {
              subTaskId: props.subTaskId
            })
          }}
            {{
              subTaskInfo.migrationModelId === MIGRATION_MODE.OFFLINE
              ? $t('components.SubTaskDetail.5q09t2cud100')
              : $t('components.SubTaskDetail.5q09t2cudw80')
            }}）</span>
          <span class="task-status">
            {{ $t('components.SubTaskDetail.5q09prnzn6c0') }}
            <b>{{ execSubStatusMap(subTaskInfo.execStatus) }}</b>
          </span>
        </div>
        <div>
          <span class="refresh-txt">{{ $t('components.SubTaskDetail.autoRefresh') }}</span>
          <el-switch v-model="isRefresh" @change="changeRefresh"></el-switch>
        </div>
      </div>
    </template>
    <div class="task-detail-con" id="detailDrawer">
      <div v-if="descData.length" class="task-desc-con">
        <a-descriptions :data="descData" layout="inline-horizontal" :column="5" bordered />
      </div>
      <el-tabs class="mainTabs" :stretch="true" v-model="tabActive" @tab-change="tabChange">
        <el-tab-pane :label="$t('components.SubTaskDetail.migrationProgress')" :name="1">
          <div v-if="tabActive === 1 &&
            subTaskInfo.migrationModelId === MIGRATION_MODE.OFFLINE
            " class="record-con">
            <div class="record-list">
              <a-card>
                <div class="record-item-offline">
                  {{ $t('components.SubTaskDetail.fullMigrationDetail') }}
                  <i class="visible-button" @click="offlineFullVisible = !offlineFullVisible">{{
                    offlineFullVisible
                    ? $t('components.SubTaskDetail.5q09prnznk00')
                    : $t('components.SubTaskDetail.5q09prnznms0')
                  }}</i>
                </div>
                <div v-if="offlineFullVisible">
                  <big-data-list :full-data="fullData" :sub-task-info="subTaskInfo" :record-counts="recordCounts" />
                </div>
                <div class="record-item-con">
                  <span class="info-offline">
                    <span class="info-title">
                      <span>{{ $t('components.SubTaskDetail.fullCheckDetail') }}</span>
                      <i class="visible-button" @click="globalVisible = !globalVisible">{{
                        globalVisible
                        ? $t('components.SubTaskDetail.5q09prnznk00')
                        : $t('components.SubTaskDetail.5q09prnznms0')
                      }}</i>
                    </span>
                    <span class="info-row-offline">
                      <div :class="{ 'status-text': true, 'status-text-en': currentLocale === 'en-US' }">
                        <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c4')
                        }} {{ dataCheckData.total || 0 }}行 </span>
                        <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c5')
                        }} {{ dataCheckData.avgSpeed || 0 }}行/s </span>
                        <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c6')
                        }} {{ dataCheckData.tableCount || 0 }}
                          {{
                            $t('components.SubTaskDetail.5q0a5opxm3c8')
                          }} </span>
                        <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c7')
                        }} {{ dataCheckData.completeCount || 0 }}
                          {{
                            $t('components.SubTaskDetail.5q0a5opxm3c8')
                          }} </span>

                      </div>
                    </span>
                  </span>
                  <full-check-table :id="props.subTaskId" v-model:full-check-page="fullCheckPage"
                    :full-check-table-data="fullCheckTableData" @refreshTableDate="getFullCheckData"
                    v-if="globalVisible"></full-check-table>
                </div>
              </a-card>
            </div>
          </div>
          <div v-if="tabActive === 1 &&
            subTaskInfo.migrationModelId === MIGRATION_MODE.ONLINE
            " class="record-con">
            <div class="record-title">
              {{ $t('components.SubTaskDetail.5q09prnzngw0') }}
            </div>
            <div class="record-list">
              <a-steps :default-current="Object.keys(statusRecords).length + 1" type="dot" direction="vertical">
                <a-step v-for="(value, key) in statusRecords" :key="key">
                  <div class="record-item-hd">
                    <span class="hd-info">{{ recordsMap(key) }}</span>
                    <span class="hd-time">by {{ value[0].operateUser || '-' }},
                      {{ value[0].operateTime || '-' }}</span>
                  </div>
                  <a-card hoverable>
                    <div class="record-item-con">
                      <div v-for="item in value" :key="item.id" class="record-item">
                        <div class="record-item-info">
                          <span class="info">
                            <div class="info-title">
                              {{ execSubStatusMap(item.statusId) }}
                            </div>
                            <div v-if="item.statusId === SUB_TASK_STATUS.FULL_RUNNING" class="info-row">
                              <i @click="globalVisible = !globalVisible">{{
                                globalVisible
                                ? $t('components.SubTaskDetail.5q09prnznk00')
                                : $t('components.SubTaskDetail.5q09prnznms0')
                              }}</i>
                              <div v-if="item.statusId === SUB_TASK_STATUS.FULL_RUNNING
                                  " :class="{ 'status-text': true, 'status-text-en': currentLocale === 'en-US' }">
                                <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c1')
                                }}<strong>{{ fullData?.total.data || 0 }}MB</strong></span>
                                <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c2')
                                }}<strong>{{ fullData?.total.speed || 0 }}MB/s</strong></span>
                                <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c3')
                                }}<strong>{{ fullData?.total.time || 0 }}s</strong></span>
                              </div>
                            </div>
                            <div v-if="item.statusId === SUB_TASK_STATUS.FULL_CHECKING" class="info-row">
                              <div :class="{ 'status-text': true, 'status-text-en': currentLocale === 'en-US' }">
                                <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c4')
                                }}<strong>{{ dataCheckData.total || 0 }}行</strong></span>
                                <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c5')
                                }}<strong>{{ dataCheckData.avgSpeed || 0 }}行/s</strong></span>
                                <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c6')
                                }}<strong>{{ dataCheckData.tableCount || 0 }}
                                    {{
                                      $t('components.SubTaskDetail.5q0a5opxm3c8')
                                    }}</strong></span>
                                <span>{{ $t('components.SubTaskDetail.5q0a5opxm3c7')
                                }}<strong>{{ dataCheckData.completeCount || 0 }}
                                    {{
                                      $t('components.SubTaskDetail.5q0a5opxm3c8')
                                    }}</strong></span>
                                <el-button text class="" @click="showFullCheckDetail = !showFullCheckDetail">{{
                                  showFullCheckDetail
                                  ? $t('components.SubTaskDetail.5q09prnznk00')
                                  : $t('components.SubTaskDetail.5q09prnznms0') }}</el-button>
                              </div>
                            </div>
                          </span>
                          <span class="time">{{ item.createTime }}</span>
                        </div>
                        <div v-show="item.statusId === SUB_TASK_STATUS.FULL_CHECKING && showFullCheckDetail"
                          class="info-row">
                          <full-check-table :id="props.subTaskId" v-model:full-check-page="fullCheckPage"
                            :full-check-table-data="fullCheckTableData"
                            @refreshTableDate="getFullCheckData"></full-check-table>
                        </div>
                        <div v-if="item.statusId === SUB_TASK_STATUS.FULL_RUNNING &&
                          globalVisible
                          " class="table-con">
                          <big-data-list :full-data="fullData" :sub-task-info="subTaskInfo"
                            :record-counts="recordCounts" />
                        </div>
                        <template v-if="item.statusId === SUB_TASK_STATUS.INCREMENTAL_RUNNING">
                          <div class="fix-options list-con" v-if="isIncrement">
                            <el-checkbox-group v-model="fixOptions">
                              <el-checkbox value="source">
                                <span :class="fixStatus.source ? 'success-color' : 'danger-color'">connect-source</span>
                              </el-checkbox>
                              <el-checkbox label="connect-sink" value="sink">
                                <span :class="fixStatus.sink ? 'success-color' : 'danger-color'">connect-sink</span>
                              </el-checkbox>
                            </el-checkbox-group>
                            <el-button text @click="handleFix" :loading="fixLoading" :disabled="!fixOptions.length">
                              {{ restartText }}
                            </el-button>
                          </div>
                          <div class="list-con">
                            <div class="list-item-con">
                              <div class="list-title">
                                <div class="list-title-l">
                                  <icon-info-circle size="15" />
                                  <span>{{ $t('components.SubTaskDetail.5q09tn4uzy40')
                                  }}{{ increaseData.count }}
                                    {{
                                      $t('components.SubTaskDetail.5q09prnznpk0')
                                    }}</span>
                                </div>
                                <div class="list-title-r">
                                  <span>{{ increaseData.createTime }}</span>
                                </div>
                              </div>
                              <div class="list-info-em">
                                <span>{{ $t('components.SubTaskDetail.5q09urmrt580')
                                }}{{ increaseData.sourceSpeed || 0 }}
                                  {{
                                    $t('components.SubTaskDetail.5q09prnzns40')
                                  }}</span>
                              </div>
                              <div class="list-info-em">
                                <span>{{ $t('components.SubTaskDetail.5q09urmruc00')
                                }}{{ increaseData.sinkSpeed || 0 }}
                                  {{
                                    $t('components.SubTaskDetail.5q09prnzns40')
                                  }}</span>
                              </div>
                              <div class="list-info">
                                <span>{{ $t('components.SubTaskDetail.5q09urmruhc0')
                                }}{{ increaseData.rest }}
                                  {{
                                    $t('components.SubTaskDetail.5q09prnznpk0')
                                  }}</span>
                              </div>
                            </div>
                          </div>
                        </template>
                        <template v-if="item.statusId === SUB_TASK_STATUS.REVERSE_RUNNING">
                          <div class="fix-options list-con" v-if="isReverse">
                            <el-checkbox-group v-model="fixOptions">
                              <el-checkbox value="source">
                                <span :class="fixStatus.source ? 'success-color' : 'danger-color'">connect-source</span>
                              </el-checkbox>
                              <el-checkbox label="connect-sink" value="sink">
                                <span :class="fixStatus.sink ? 'success-color' : 'danger-color'">connect-sink</span>
                              </el-checkbox>
                            </el-checkbox-group>
                            <el-button text @click="handleFix" :loading="fixLoading" :disabled="!fixOptions.length">
                              {{ restartText }}
                            </el-button>
                          </div>
                          <div class="list-con">
                            <div class="list-item-con">
                              <div class="list-title">
                                <div class="list-title-l">
                                  <icon-info-circle size="15" />
                                  <span>{{ $t('components.SubTaskDetail.5q09tn4uzy41')
                                  }}{{ reverseData.count }}
                                    {{
                                      $t('components.SubTaskDetail.5q09prnznpk0')
                                    }}</span>
                                </div>
                                <div class="list-title-r">
                                  <span>{{ reverseData.createTime }}</span>
                                </div>
                              </div>
                              <div class="list-info-em">
                                <span>{{ $t('components.SubTaskDetail.5q09urmrt580')
                                }}{{ reverseData.sourceSpeed || 0 }}
                                  {{
                                    $t('components.SubTaskDetail.5q09prnzns40')
                                  }}</span>
                              </div>
                              <div class="list-info-em">
                                <span>{{ $t('components.SubTaskDetail.5q09urmruc00')
                                }}{{ reverseData.sinkSpeed || 0 }}
                                  {{
                                    $t('components.SubTaskDetail.5q09prnzns40')
                                  }}</span>
                              </div>
                              <div class="list-info">
                                <span>{{ $t('components.SubTaskDetail.5q09urmruhc0')
                                }}{{ reverseData.rest || 0 }}
                                  {{
                                    $t('components.SubTaskDetail.5q09prnznpk0')
                                  }}</span>
                              </div>
                            </div>
                          </div>
                        </template>
                      </div>
                    </div>
                  </a-card>
                </a-step>
              </a-steps>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane :name="2">
          <template #label>
            <div class="errorLabel" v-if="phaseNums.total && phaseNums.total > 0">
              <el-badge :value="phaseNums.total">
                {{ $t('components.SubTaskDetail.abnormalAlaram') }}
              </el-badge>
            </div>
            <div class="errorLabel" v-else>{{ $t('components.SubTaskDetail.abnormalAlaram') }}</div>
          </template>
          <el-tabs type="card" style="padding-top: 16px;" @tab-change="alarmPhaseChange" v-model="phaseTab">
            <el-tab-pane :label="$t('components.SubTaskDetail.fullMigration')" :name="item.key" v-for="item in phaseList"
              :key="item.key">
              <template #label>
                <div v-if="phaseNums[item.key] && phaseNums[item.key] > 0">
                  <el-badge :value="phaseNums[item.key]">{{ item.label }}</el-badge>
                </div>
                <span v-else>{{ item.label }}</span>
              </template>
            </el-tab-pane>
          </el-tabs>
          <div class="loading-area" v-loading="tableLoading">
            <el-table :data="alarmPhaseList" style="padding-top: 16px;">
              <el-table-column prop="sourceName" width="350" :label="$t('components.SubTaskDetail.alarmLocation')">
                <template #default="{ row }">
                  <span style="line-height: 1.3">
                    {{ locationMap[row.logSource] }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="causeCn" :label="$t('components.SubTaskDetail.alarmReason')">
                <template #default="{ row }">
                  <div>
                    {{ currentLocale === 'en-US' ? row.causeEn : row.causeCn }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="firstDateTime" :label="$t('components.SubTaskDetail.firstAlarmTime')"></el-table-column>
              <el-table-column prop="latestDateTime" :label="$t('components.SubTaskDetail.latestAlarmTime')"></el-table-column>
              <el-table-column prop="operation" :label="$t('components.SubTaskDetail.operate')">
                <template #default="{ row }">
                  <el-button text :icon="IconVisible" @click="showErrorDetail(row)">
                    {{ $t('components.SubTaskDetail.showDetail') }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="pageNum" v-model:pageSize="pageSize" :page-sizes="pageSizes"
              :total="phaseTotal" :teleported="false" :layout="paginationLayout"
              @change="fetchPhaseAlarmList"></el-pagination>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('components.SubTaskDetail.5q09prnzmfw0')" :name="3">
          <div class="log-con">
            <a-list>
              <a-list-item v-for="item in logData" :key="item.url">
                <div class="log-detail-info">
                  <span>{{ item.name }}</span>
                  <span class="desc">{{ logsMap(item.name) }}</span>
                </div>
                <template #actions>
                  <a-button type="text" size="mini" @click="handleDownloadLog(item.url)">
                    <template #icon>
                      <icon-download />
                    </template>
                    {{ $t('components.SubTaskDetail.5q09prnznvg0') }}
                  </a-button>
                </template>
              </a-list-item>
            </a-list>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </a-drawer>
  <error-detail v-model:error-visible="errorVisible" :detail-info="detailInfo" :area-loading="areaLoading"></error-detail>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, h, reactive, computed } from 'vue'
import {
  subTaskDetail,
  downloadLog,
  getAlarmDetail,
  getPhaseAlarmList,
  getTotalAlarmNum,
  queryFullCheckSummary,
  queryFullCheckDetail,
  getOnlineReverseStatus,
  startOnlineReverseProcess,
} from '@/api/detail'
import { Message } from '@arco-design/web-vue'
import BigDataList from './BigDataList.vue'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { MIGRATION_MODE, SUB_TASK_STATUS } from '@/utils/constants'
import useLocale from '@/hooks/locale'
import { IconVisible } from '@computing/opendesign-icons'
import ErrorDetail from './ErrorDetail.vue'
import FullCheckTable from './FullCheckTable.vue'

const { currentLocale } = useLocale()

const { t } = useI18n()

const props = defineProps({
  open: Boolean,
  taskInfo: Object,
  subTaskId: [String, Number],
  tab: [String, Number]
})

const emits = defineEmits(['update:open'])
const loading = ref(false)
const visible = ref(false)
const tabActive = ref(1)
const globalVisible = ref(false)
const subTaskInfo = ref({})
const descData = ref([])
const increaseData = ref({})
const dataCheckData = ref({})
const reverseData = ref({})

// sub task status map
const execSubStatusMap = (status) => {
  const maps = {
    0: t('components.SubTaskDetail.5q09prnznzg0'),
    1: t('components.SubTaskDetail.5q09prnzo3s0'),
    2: t('components.SubTaskDetail.5q09prnzo6g0'),
    3: t('components.SubTaskDetail.5q09prnzoa00'),
    4: t('components.SubTaskDetail.5q09prnzodo0'),
    5: t('components.SubTaskDetail.5q09prnzohc0'),
    6: t('components.SubTaskDetail.5q09prnzok00'),
    7: t('components.SubTaskDetail.5q09prnzong0'),
    8: t('components.SubTaskDetail.5q09prnzoq80'),
    9: t('components.SubTaskDetail.5q09prnzotc0'),
    10: t('components.SubTaskDetail.5q09prnzotc1'),
    11: t('components.SubTaskDetail.5q09prnzoxc0'),
    12: t('components.SubTaskDetail.5q09prnzp000'),
    13: t('components.SubTaskDetail.5q09prnzp2k0'),
    30: t('components.SubTaskDetail.incrementError'),
    40: t('components.SubTaskDetail.reverseError'),
    100: t('components.SubTaskDetail.5q09prnzp540'),
    500: t('components.SubTaskDetail.5q09prnzp740'),
    1000: t('components.SubTaskDetail.5q09prnzp980'),
    3000: t('detail.index.5q09asiwlca0')
  }
  return maps[status]
}

// record map
const recordsMap = (key) => {
  const maps = {
    1: t('components.SubTaskDetail.5q09prnzpb80'),
    2: t('components.SubTaskDetail.5q09prnzpd40'),
    3: t('components.SubTaskDetail.5q09prnzpfk0'),
    100: t('components.SubTaskDetail.5q09prnzphk0')
  }
  return maps[key]
}

// log map
const logsMap = (key) => {
  const maps = {
    'sink.log': t('components.SubTaskDetail.5q09prnzpkg0'),
    'source.log': t('components.SubTaskDetail.5q09prnzpmk0'),
    'check.log': t('components.SubTaskDetail.5q09prnzpps0'),
    'full_migration.log': t('components.SubTaskDetail.5q09prnzprs0'),
    'server.log': t('components.SubTaskDetail.5q09vm8iktw0'),
    'schema-registry.log': t('components.SubTaskDetail.5q09wjm47ow0'),
    'connect.log': t('components.SubTaskDetail.5q09prnzpug0'),
    'connect_source.log': t('components.SubTaskDetail.5q09prnzpxc0'),
    'connect_sink.log': t('components.SubTaskDetail.5q09prnzq080'),
    'reverse_connect.log': t('components.SubTaskDetail.5q09prnzq2o0'),
    'reverse_connect_source.log': t('components.SubTaskDetail.5q09prnzq680'),
    'reverse_connect_sink.log': t('components.SubTaskDetail.5q09prnzq880'),
    'error.log': t('components.SubTaskDetail.5q09prnzqac0'),
    'business-check.log': t('components.SubTaskDetail.5q09prnzqac1'),
    'business-sink.log': t('components.SubTaskDetail.5q09prnzqac2'),
    'business-source.log': t('components.SubTaskDetail.5q09prnzqac3'),
    'check-debug.log': t('components.SubTaskDetail.5q09prnzqac4'),
    'check-error.log': t('components.SubTaskDetail.5q09prnzqac5'),
    'kafka-check.log': t('components.SubTaskDetail.5q09prnzqac6'),
    'kafka-sink.log': t('components.SubTaskDetail.5q09prnzqac7'),
    'kafka-source.log': t('components.SubTaskDetail.5q09prnzqac8'),
    'sink-debug.log': t('components.SubTaskDetail.5q09prnzqac9'),
    'sink-error.log': t('components.SubTaskDetail.5q09prnzqad0'),
    'source-debug.log': t('components.SubTaskDetail.5q09prnzqad1'),
    'source-error.log': t('components.SubTaskDetail.5q09prnzqad2')
  }
  return `(${maps[key]})` || ''
}

watch(visible, (v) => {
  emits('update:open', v)
})

let timer = null
let alarmNumTimer = null
let alarmPhaseListTimer = null
let fullCheckTimer = null
let onlineReverseStatusTimer = null

watch(
  () => subTaskInfo.value.execStatus,
  (v) => {
    if (v >= SUB_TASK_STATUS.FULL_CHECK_START && !fullCheckTimer) {
      getFullCheckData()
      fullCheckTimer = setInterval(() => {
        getFullCheckData()
      }, 6000)
    }
    if (v >= SUB_TASK_STATUS.INCREMENTAL_START && v <= SUB_TASK_STATUS.REVERSE_CONNECT_ERROR && !onlineReverseStatusTimer) {
      getFixStatus()
      onlineReverseStatusTimer = setInterval(() => {
        getFixStatus()
      }, 6000)
    }
  },
  { immediate: true }
)

const fixStatus = ref({
  source: true,
  sink: true
})
const getFixStatus = async () => {
  try {
    const { data, code } = await getOnlineReverseStatus(props.subTaskId)
    if (code === 200) {
      fixStatus.value.source = data.source
      fixStatus.value.sink = data.sink
    }
  } catch (error) {

  }
}

const showFullCheckDetail = ref(false)
const fullCheckTableData = ref([])
const fullCheckPage = ref({
  status: '',
  pageSize: 10,
  pageNum: 1,
  loading: false,
  total: 0,
})
const getFullCheckData = async () => {
  fullCheckPage.value.loading = true
  try {
    const { pageNum, pageSize, status } = fullCheckPage.value
    const res = await queryFullCheckDetail({
      id: props.subTaskId,
      status,
      pageSize,
      pageNum,
    })
    if (res.code === 200) {
      fullCheckTableData.value = res.data.records
      fullCheckPage.value.total = res.data.total
    }
    fullCheckPage.value.loading = false
  } catch (error) {
    fullCheckPage.value.loading = false

  }
}

const initBasicData = () => {
  if (subTaskInfo.value.migrationModelId !== MIGRATION_MODE.OFFLINE) {
    descData.value = [
      {
        label: t('components.SubTaskDetail.5q09prnzqck0'),
        value: '--',
        span: 3
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqew0'),
        value: '--',
        span: 2
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqxs0'),
        value: '--',
        span: 3
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqzs0'),
        value: '--',
        span: 2
      },
      {
        label: t('components.SubTaskDetail.5q09prnzux41'),
        value: '--',
        span: 3
      },

      {
        label: t('components.SubTaskDetail.5q09prnzr4s0'),
        value: `-- ${t(
          'components.SubTaskDetail.5q0a5opxm3c0'
        )} --`,
        span: 2
      },
      {
        label: t('components.SubTaskDetail.5q09prnzr2o0'),
        value: '--',
        span: 3
      },

      {
        label: t('components.SubTaskDetail.5q09prnzqnw0'),
        value: '--',
        span: 2
      },
      {
        label: t('components.SubTaskDetail.5q09prnzux40'),
        value: '--',
        span: 5
      },
    ]
  } else {
    descData.value = [
      {
        label: t('components.SubTaskDetail.5q09prnzqck0'),
        value: '--',
        span: 3
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqew0'),
        value: '--',
        span: 2
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqgw0'),
        value: '--',
        span: 3
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqk00'),
        value: '--',
        span: 2
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqlw0'),
        value: '--',
        span: 3
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqnw0'),
        value: '--',
        span: 2
      },
      {
        label: t('components.SubTaskDetail.5q09prnzqvk0'),
        value: '--',

        span: 5
      },
      {
        label: t('components.SubTaskDetail.5q09prnzux41'),
        value: '--',
        span: 5
      }
    ]
  }
}

const offlineFullVisible = ref(true)
watch(
  () => props.open,
  (v) => {
    if (v) {
      initBasicData()
      fullCheckPage.value = {
        status: '',
        pageSize: 10,
        pageNum: 1,
        loading: false,
        total: 0,
      }
      tabActive.value = props.tab
      globalVisible.value = false
      offlineFullVisible.value = true
      increaseData.value = {}
      reverseData.value = {}
      logData.value = []
      getSubTaskDetail()
      if (!timer) {
        timer = setInterval(() => {
          getSubTaskDetail()
        }, 6000)
      }
      fetchTotalAlarmNum()
      if (!alarmNumTimer) {
        alarmNumTimer = setInterval(() => {
          fetchTotalAlarmNum()
        }, 6000)
      }
    } else {
      globalVisible.value = false
      offlineFullVisible.value = true
      increaseData.value = {}
      reverseData.value = {}
      subTaskInfo.value = {}
      descData.value = []
      logData.value = []
      loading.value = false
      clearInterval(alarmNumTimer)
      clearInterval(timer)
      clearInterval(alarmPhaseListTimer)
      alarmNumTimer = null
      timer = null
      alarmPhaseListTimer = null
    }
    visible.value = v
  }
)

const phaseNums = ref({})
const fetchTotalAlarmNum = async () => {
  try {
    const { data, code } = await getTotalAlarmNum(props.subTaskId)
    if (code === 200) {
      phaseNums.value = data ?? {}
    }
  } catch (error) {
    console.log(error)
  }
}

const phaseList = computed(() => {
  if (subTaskInfo.value.migrationModelId === MIGRATION_MODE.OFFLINE) {
    return [{
      key: '1',
      label: t('components.SubTaskDetail.fullMigration')
    },
    {
      key: '2',
      label: t('components.SubTaskDetail.fullCheck'),
    },]
  } else {
    return [
      {
        key: '1',
        label: t('components.SubTaskDetail.fullMigration')
      },
      {
        key: '2',
        label: t('components.SubTaskDetail.fullCheck'),
      },
      {
        key: '3',
        label: t('components.SubTaskDetail.incrementMigration'),
      },
      {
        key: '4',
        label: t('components.SubTaskDetail.reverseMigration'),
      }
    ]
  }

})

const locationMap = computed(() => {
  return {
    0: t('components.SubTaskDetail.portal', { id: props.subTaskId }),
    10: t('components.SubTaskDetail.fullMigrationLog'),
    20: t('components.SubTaskDetail.checkCheck'),
    21: t('components.SubTaskDetail.checkSource'),
    22: t('components.SubTaskDetail.checkSink'),
    31: t('components.SubTaskDetail.connectSource'),
    32: t('components.SubTaskDetail.connectSink'),
    41: t('components.SubTaskDetail.reverseConnectSource'),
    42: t('components.SubTaskDetail.reverseConnectSink'),
  }
})
const pageSize = ref(10)
const pageSizes = ref([10, 20, 30, 40])
const paginationLayout = ref('total,sizes,prev,pager,next,jumper')
const pageNum = ref(1)
const alarmPhaseList = ref([])
const phaseTotal = ref(0)
const tableLoading = ref(false)
const fetchPhaseAlarmList = async () => {
  try {
    tableLoading.value = true
    const { code, rows, total } = await getPhaseAlarmList(props.subTaskId, phaseTab.value, pageSize.value, pageNum.value)
    tableLoading.value = false
    if (code === 200) {
      alarmPhaseList.value = rows || []
      phaseTotal.value = total
    }
  } catch (error) {
    tableLoading.value = false
  }
}

const errorVisible = ref(false)
const areaLoading = ref(false)
const detailInfo = ref({})
const showErrorDetail = async (rows) => {
  try {
    detailInfo.value = {}
    areaLoading.value = true
    errorVisible.value = true
    const { firstDateTime, latestDateTime, sourceName, logSource, id, taskId } = rows
    const { code, data } = await getAlarmDetail(id)
    areaLoading.value = false
    detailInfo.value = {
      firstDateTime, latestDateTime, sourceName, logSource, taskId
    }
    if (code === 200) {
      detailInfo.value.detail = data.detail
    }
  } catch (error) {
    areaLoading.value = false
  }
}

const tabChange = (tab) => {
  tabActive.value = tab
  if (tab === 2) {
    fetchPhaseAlarmList()
    alarmPhaseListTimer = setInterval(() => {
      fetchPhaseAlarmList()
    }, 6000)
  } else if (alarmPhaseListTimer) {
    clearInterval(alarmPhaseListTimer)
    alarmPhaseListTimer = null
  } else {
    // do nothing
  }
}

const phaseTab = ref('1')
const alarmPhaseChange = () => {
  pageSize.value = 10
  pageNum.value = 1
  fetchPhaseAlarmList()
}

const fullData = ref({})
const recordCounts = ref()
const statusRecords = ref({})
const logData = ref([])

const handleDownloadLog = (url) => {
  downloadLog(subTaskInfo.value.id, { filePath: url }).then((res) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = `#${subTaskInfo.value.id}_${url.substring(
        url.lastIndexOf('/') + 1,
        url.lastIndexOf('.')
      )}_${dayjs(subTaskInfo.value.currentTime).format('YYYYMMDDHHmmss')}.log`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    }
  })
}

const restartText = computed(() => {
  if (fixLoading.value) {
    return t('components.SubTaskDetail.restarting')
  } else {
    return fixStatus.value.sink && fixStatus.value.source ? t('components.SubTaskDetail.restart') :
      t('components.SubTaskDetail.oneClickFix')
  }
})
const fixOptions = ref([])
const fixLoading = ref(false)
const isIncrement = computed(() => {
  const incrementSet = new Set([SUB_TASK_STATUS.INCREMENTAL_START, SUB_TASK_STATUS.INCREMENTAL_RUNNING, SUB_TASK_STATUS.INCREMENTAL_FINISHED, SUB_TASK_STATUS.INCREMENTAL_STOPPED, SUB_TASK_STATUS.INCREMENTAL_CONNECT_ERROR])
  return incrementSet.has(subTaskInfo.value.execStatus)
})
const isReverse = computed(() => {
  const reverseSet = new Set([SUB_TASK_STATUS.REVERSE_START, SUB_TASK_STATUS.REVERSE_RUNNING, SUB_TASK_STATUS.REVERSE_STOP, SUB_TASK_STATUS.REVERSE_CONNECT_ERROR])
  return reverseSet.has(subTaskInfo.value.execStatus)
})
const handleFix = async () => {
  fixLoading.value = true
  try {
    let name = fixOptions.value.length === 2 ? 'reset' : fixOptions.value[0]
    if (isReverse.value) {
      name = `reverse-${name}`
    }
    const startRes = await startOnlineReverseProcess(props.subTaskId, name)
    if (startRes.code === 200) {
      Message.success(t('components.SubTaskDetail.restartSuccessTip'))
    }
    fixLoading.value = false
  } catch (error) {
    fixLoading.value = false
  }
}

const isRefresh = ref(true)
const changeRefresh = (e) => {
  if (e) {
    startGetSubTaskDetail()
  } else {
    stopGetSubTaskDetail()
  }
}

const stopGetSubTaskDetail = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  if (alarmNumTimer) {
    clearInterval(alarmNumTimer)
    alarmNumTimer = null
  }
  if (alarmPhaseListTimer) {
    clearInterval(alarmPhaseListTimer)
    alarmPhaseListTimer = null
  }
  if (fullCheckTimer) {
    clearInterval(fullCheckTimer)
    fullCheckTimer = null
  }
  if (onlineReverseStatusTimer) {
    clearInterval(onlineReverseStatusTimer)
    onlineReverseStatusTimer = null
  }
}
const startGetSubTaskDetail = () => {
  timer = setInterval(() => { getSubTaskDetail() }, 6000)
  alarmNumTimer = setInterval(() => {
    fetchTotalAlarmNum()
  }, 6000)
  alarmPhaseListTimer = setInterval(() => {
    fetchPhaseAlarmList()
  }, 6000)
  if (subTaskInfo.value.execStatus >= SUB_TASK_STATUS.FULL_CHECK_START && !fullCheckTimer) {
    fullCheckTimer = setInterval(() => {
      getFullCheckData()
    }, 6000)
  }
  if (subTaskInfo.value.execStatus >= SUB_TASK_STATUS.INCREMENTAL_START && subTaskInfo.value.execStatus <= SUB_TASK_STATUS.REVERSE_CONNECT_ERROR && !onlineReverseStatusTimer) {
    onlineReverseStatusTimer = setInterval(() => {
      getFixStatus()
    }, 6000)
  }
}

const getSubTaskDetail = () => {
  loading.value = true
  subTaskDetail(props.subTaskId)
    .then((res) => {
      loading.value = false
      subTaskInfo.value = res.data.task
      if (subTaskInfo.value.finishTime !== null) {
        clearInterval(timer)
        timer = null
      }
      const seconds = subTaskInfo.value.finishTime
        ? dayjs(subTaskInfo.value.finishTime).diff(
          dayjs(subTaskInfo.value.execTime),
          'seconds'
        )
        : dayjs(subTaskInfo.value.currentTime).diff(dayjs(subTaskInfo.value.execTime), 'seconds')
      const hour = parseInt(seconds / 3600)
      const minute = parseInt((seconds - hour * 3600) / 60)

      recordCounts.value = {
        table: res.data.tableCounts,
        view: res.data.viewCounts,
        function: res.data.funcCounts,
        trigger: res.data.triggerCounts,
        procedure: res.data.produceCounts
      }

      // fullProcess data
      const fullProcessDetail = res.data.fullProcess?.execResultDetail
        ? JSON.parse(res.data.fullProcess?.execResultDetail)
        : null
      fullData.value = fullProcessDetail || null
      // process record
      if (subTaskInfo.value.migrationModelId === 2) {
        setStatusRecord(res.data.statusRecords)
      }

      // increase process detail
      const increaseProcessDetail = res.data.incrementalProcess
        ?.execResultDetail
        ? JSON.parse(res.data.incrementalProcess?.execResultDetail)
        : null
      if (increaseProcessDetail) {
        increaseData.value = {
          createTime: res.data.incrementalProcess.createTime,
          ...increaseProcessDetail
        }
      }

      // data check process detail
      const dataCheckDetail = res.data.dataCheckProcess?.execResultDetail
        ? JSON.parse(res.data.dataCheckProcess?.execResultDetail)
        : null
      if (dataCheckDetail) {
        dataCheckData.value = {
          ...dataCheckDetail
        }
      }
      // reverse process detail
      const reverseDetail = res.data.reverseProcess?.execResultDetail
        ? JSON.parse(res.data.reverseProcess?.execResultDetail)
        : null
      if (reverseDetail) {
        reverseData.value = {
          ...reverseDetail
        }
      }

      const offlineDesc = [
        {
          label: t('components.SubTaskDetail.5q09prnzqck0'),
          value: props.taskInfo.taskName,
          span: 3
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqew0'),
          value: subTaskInfo.value.createTime,
          span: 2
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqgw0'),
          value: subTaskInfo.value.sourceDb,
          span: 3
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqk00'),
          value: subTaskInfo.value.targetDb,
          span: 2
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqlw0'),
          value: subTaskInfo.value.execTime,
          span: 3
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqnw0'),
          value: `${hour ? hour + t('components.SubTaskDetail.5q09prnzqpw0') : ''
            } ${minute ? minute + t('components.SubTaskDetail.5q09prnzqs00') : ''
            }`,
          span: 2
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqvk0'),
          value: fullProcessDetail
            ? h(
              'div',
              null,
              h('div', { class: 'desc-value-row' }, [
                h(
                  'div',
                  null,
                  t('components.SubTaskDetail.5q09xhsvcq40', {
                    total:
                      (res.data.totalWaitCount || 0) +
                      (res.data.totalRunningCount || 0) +
                      (res.data.totalSuccessCount || 0) +
                      (res.data.totalErrorCount || 0),
                    totalWaitCount: res.data.totalWaitCount || 0,
                    totalRunningCount: res.data.totalRunningCount || 0,
                    totalSuccessCount: res.data.totalSuccessCount || 0,
                    totalErrorCount: res.data.totalErrorCount || 0
                  })
                ),
                h(
                  'div',
                  { class: 'arco-tag arco-tag-checked speed-tag' },
                  t('components.SubTaskDetail.5q09xhsvcq42', {
                    speed: fullData.value?.total?.speed || 0
                  })
                )
              ]),
              h(
                'div',
                null,
                t('components.SubTaskDetail.5q09xhsvcq41', {
                  totalDataSize: fullData.value?.total?.data || 0,
                  cost: fullData.value?.total?.time || 0
                })
              )
            )
            : t('components.SubTaskDetail.5q09prnznzg0'),
          span: 5
        },
        {
          label: t('components.SubTaskDetail.5q09prnzux41'),
          value: dataCheckDetail
            ? h(
              'div',
              null,
              h('div', { class: 'desc-value-row' }, [
                // h('div', null, t('components.SubTaskDetail.5q09xhsvcq43', { total: (res.data.totalWaitCount || 0) + (res.data.dataCheck?.totalRunningCount || 0) + (res.data.dataCheck?.totalFinishCount || 0) + (res.data.dataCheck?.totalErrorCount || 0), totalWaitCount: res.data.dataCheck?.totalWaitCount || 0, totalRunningCount: res.data.dataCheck?.totalRunningCount || 0, totalFinishCount: res.data.dataCheck?.totalFinishCount || 0, totalErrorCount: res.data.dataCheck?.totalErrorCount || 0 })),
                h(
                  'div',
                  null,
                  t('components.SubTaskDetail.5q09xhsvcq44', {
                    totalRowCount: dataCheckData.value?.total || 0,
                    totalTableCount: dataCheckData.value?.tableCount || 0,
                    totalTableFinishCount:
                      dataCheckData.value?.completeCount || 0
                  })
                ),
                h(
                  'div',
                  { class: 'arco-tag arco-tag-checked speed-tag' },
                  t('components.SubTaskDetail.5q09xhsvcq45', {
                    avgSpeed: dataCheckData.value?.avgSpeed || 0
                  })
                )
              ])
            )
            : t('components.SubTaskDetail.5q09prnznzg0'),
          span: 5
        }
      ]

      const onlineDesc = [
        {
          label: t('components.SubTaskDetail.5q09prnzqck0'),
          value: props.taskInfo.taskName,
          span: 3
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqew0'),
          value: subTaskInfo.value.createTime,
          span: 2
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqxs0'),
          value: fullProcessDetail
            ? h('div', null, [
              h(
                'div',
                null,
                t('components.SubTaskDetail.5q09xhsvcq40', {
                  total:
                    (res.data.totalWaitCount || 0) +
                    (res.data.totalRunningCount || 0) +
                    (res.data.totalSuccessCount || 0) +
                    (res.data.totalErrorCount || 0),
                  totalWaitCount: res.data.totalWaitCount || 0,
                  totalRunningCount: res.data.totalRunningCount || 0,
                  totalSuccessCount: res.data.totalSuccessCount || 0,
                  totalErrorCount: res.data.totalErrorCount || 0
                })
              ),
              h(
                'div',
                null,
                t('components.SubTaskDetail.5q09xhsvcq41', {
                  totalDataSize: fullData.value?.total?.data || 0,
                  cost: fullData.value?.total?.time || 0
                })
              )
            ])
            : t('components.SubTaskDetail.5q09prnznzg0'),
          span: 3
        },

        {
          label: t('components.SubTaskDetail.5q09prnzqzs0'),
          value: subTaskInfo.value.execTime,
          span: 2
        },
        {
          label: t('components.SubTaskDetail.5q09prnzux41'),
          value: dataCheckDetail
            ? h('div', null, [
              h(
                'div',
                null,
                t('components.SubTaskDetail.5q09xhsvcq44', {
                  totalRowCount: dataCheckData.value?.total || 0,
                  totalTableCount: dataCheckData.value?.tableCount || 0,
                  totalTableFinishCount:
                    dataCheckData.value?.completeCount || 0
                })
              )
            ])
            : t('components.SubTaskDetail.5q09prnznzg0'),
          span: 3
        },

        {
          label: t('components.SubTaskDetail.5q09prnzr4s0'),
          value: `${subTaskInfo.value.sourceDb} ${t(
            'components.SubTaskDetail.5q0a5opxm3c0'
          )} ${subTaskInfo.value.targetDb}`,
          span: 2
        },
        {
          label: t('components.SubTaskDetail.5q09prnzr2o0'),
          value: increaseProcessDetail
            ? t('components.SubTaskDetail.5q0a1jl7o9s1', {
              total: increaseData.value?.count || 0,
              totalWaitCount: increaseData.value?.rest || 0,
              totalRunningCount:
                (increaseData.value?.count || 0) -
                (increaseData.value?.rest || 0) -
                (increaseData.value?.successCount || 0) -
                (increaseData.value?.failCount || 0),
              totalFinishCount: increaseData.value?.successCount || 0,
              totalErrorCount: increaseData.value?.failCount || 0
            })
            : t('components.SubTaskDetail.5q09prnznzg0'),
          span: 3
        },

        {
          label: t('components.SubTaskDetail.5q09prnzqnw0'),
          value: `${hour ? hour + t('components.SubTaskDetail.5q09prnzqpw0') : ''
            } ${minute ? minute + t('components.SubTaskDetail.5q09prnzqs00') : ''
            }`,
          span: 2
        },
        {
          label: t('components.SubTaskDetail.5q09prnzux40'),
          value: reverseDetail
            ? t('components.SubTaskDetail.5q0a1jl7o9s0', {
              total: reverseData.value?.count || 0,
              totalWaitCount: reverseData.value?.rest || 0,
              totalRunningCount:
                (reverseData.value?.count || 0) -
                (reverseData.value?.rest || 0) -
                (reverseData.value?.successCount || 0) -
                (reverseData.value?.failCount || 0),
              totalFinishCount: reverseData.value?.successCount || 0,
              totalErrorCount: reverseData.value?.failCount || 0
            })
            : t('components.SubTaskDetail.5q09prnznzg0'),
          span: 5
        },
      ]

      descData.value =
        res.data.task.migrationModelId === MIGRATION_MODE.OFFLINE ? offlineDesc : onlineDesc

      // log
      logData.value = res.data.logs.map((item) => {
        const name = item.substring(item.lastIndexOf('/') + 1)
        return {
          name,
          url: item
        }
      })
    })
    .catch(() => {
      loading.value = false
    })
}

const setStatusRecord = (resData) => {
  Object.keys(resData).map((newKey) => {
    // not have this key
    const notHaveThisKey = Object.keys(statusRecords.value).indexOf(newKey) < 0
    if (notHaveThisKey) {
      statusRecords.value[newKey] = resData[newKey]
    } else {
      // alreay have this key
      const newInnerArray = resData[newKey]
      const oldInnerArray = statusRecords.value[newKey]
      const subArray = newInnerArray.filter((item) => {
        const result = oldInnerArray.find(
          (one) => item.statusId === one.statusId
        )
        return !result
      })
      if (subArray.length > 0) {
        oldInnerArray.push(...subArray)
      }
    }
  })
}

onMounted(() => {
  visible.value = props.open
})
onBeforeUnmount(() => {
  stopGetSubTaskDetail()
})
</script>

<style lang="less" scoped>
:deep(.el-table thead) {
  height: 40px
}

:deep(.el-tabs--card>.el-tabs__header .el-tabs__item) {
  height: 38px;
  width: 115px;
  background-color: #fff;
  border: 1px solid #dfe5ef;
  margin: 0 -1px 0 0;

  .is-active {
    border: none
  }
}

:deep(.el-tabs--card>.el-tabs__header .el-tabs__item.is-active) {
  position: relative;
  border-top: none;
  border-bottom: none;

  &::before {
    content: '';
    position: absolute;
    top: 0px;
    left: 0;
    width: 100%;
    height: 1px;
    background: linear-gradient(to right, #dfe5ef 8px, transparent 10px, transparent calc(100% - 8px), #dfe5ef calc(100% - 8px));
  }
}

.mainTabs {
  .danger {
    color: var(--o-color-danger);
  }

  .success {
    color: var(--o-color-success);
  }

  margin-top: 20px;


  :deep(.el-loading-mask) {
    --o-loading-mask-color: transparent;
  }

  :deep(.el-popper) {
    max-width: 800px;
  }

  :deep(.el-table th.el-table__cell) {
    background-color: #f2f3f5;
  }

  :deep(.el-table+.el-loading-mask+.el-pagination, .el-table+.el-pagination) {
    margin-top: 8px
  }

  .errorLabel {
    height: 24px;
    display: flex;
    align-items: center;
  }
}

.title-con {
  width: calc(60vw - 60px);
  display: flex;
  justify-content: space-between;
  align-items: center;

  .refresh-txt {
    font-size: 12px;
    padding-right: 8px;
  }

  .tab-con {
    .tab-item {
      margin-right: 10px;
      font-weight: 500;
      font-size: 16px;
      color: var(--color-text-1);
    }
  }

  .task-status {
    font-size: 14px;
    font-weight: normal;

    b {
      color: rgb(var(--primary-6));
    }
  }
}

.task-detail-con {
  .loading-con {
    width: 60vw;
    height: 200px;
  }

  .progress-con {
    margin-top: 15px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;

    .progress-info {
      white-space: nowrap;
      margin-right: 10px;
    }
  }

  .table-con {
    padding-top: 16px;

    .data-count {
      cursor: pointer;
      color: var(--color-text-3);
    }
  }

  .record-con {
    margin-top: 20px;

    .status-icon {
      display: flex;
      align-items: center
    }

    .record-title {
      font-weight: bold;
      margin-bottom: 10px;
    }

    .record-list {
      .record-item-offline {
        display: flex;
        font-size: 13px
      }

      .visible-button {
        cursor: pointer;
        font-style: normal;
        color: rgb(var(--primary-6));
        margin-right: 4px;
        margin-left: 4px
      }

      .info-row-offline {
        color: var(--color-text-3)
      }

      .info-offline {
        display: flex;
        font-size: 13px;
        justify-content: space-between;
        padding-top: 8px
      }

      :deep(.arco-steps-item) {
        &:last-child {
          margin-right: 16px;
        }

        .arco-steps-item-content {
          width: 100%;

          .arco-steps-item-title {
            width: 100%;
            padding-right: 8px;
          }
        }
      }

      .record-item-hd {
        display: flex;
        justify-content: space-between;

        .hd-info {
          font-size: 14px;
        }

        .hd-time {
          font-size: 12px;
          color: var(--color-text-3);
        }
      }

      .record-item-con {
        .record-item {
          .record-item-info {
            display: flex;
            align-items: center;
            justify-content: space-between;

            .info {
              font-size: 13px;
              position: relative;
              display: flex;
              align-items: center;

              .info-title {
                margin-right: 2px;
              }

              .info-row {
                display: flex;
                color: var(--color-text-3);

                .status-text {
                  position: absolute;
                  top: 0;
                  left: 130px;

                  strong {
                    font-weight: normal;
                  }

                  span {
                    margin-right: 10px;
                  }
                }

                .status-text-en {
                  left: 215px;
                }
              }

              i {
                cursor: pointer;
                font-style: normal;
                color: rgb(var(--primary-6));
                margin-right: 4px;
              }
            }

            .time {
              font-size: 12px;
              color: var(--color-text-3);
            }
          }

          .fix-options {
            background-color: #f2f3f5;
            width: 100%;
            height: 40px;
            margin-bottom: 8px;
            display: flex;
            justify-content: space-between;
            padding: 0 20px;
          }

          .list-con {
            border: 1px solid var(--color-border-1);
            border-radius: 3px;
            padding: 5px 10px;

            .list-item-con {
              .list-title {
                display: flex;
                align-items: center;
                justify-content: space-between;

                .list-title-l {
                  .arco-icon {
                    color: rgb(var(--primary-6));
                  }

                  span {
                    font-size: 14px;
                    margin-left: 3px;
                    color: var(--color-text-2);
                  }
                }

                .list-title-r {
                  span {
                    font-size: 12px;
                    color: var(--color-text-3);
                  }
                }
              }

              .list-info-em {
                margin-left: 18px;

                span {
                  font-size: 12px;
                  color: rgb(var(--warning-6));
                }
              }

              .list-info {
                margin-left: 18px;

                span {
                  font-size: 12px;
                  color: var(--color-text-2);
                }
              }
            }
          }
        }
      }
    }
  }

  .log-con {
    margin-top: 10px;

    .log-detail-info {
      white-space: pre-wrap;

      .desc {
        margin-left: 5px;
        color: var(--color-text-2);
      }
    }
  }

  :deep(.arco-descriptions-item-label-inline) {
    vertical-align: top;
  }

  :deep(.desc-value-row) {
    display: flex;
    align-items: center;
  }

  :deep(.speed-tag) {
    margin-left: 50px;
  }
}</style>
