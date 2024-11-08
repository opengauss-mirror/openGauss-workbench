<template>
  <a-drawer
    v-model:visible="visible"
    width="60%"
    :footer="false"
    :unmount-on-close="true"
  >
    <template #title>
      <div class="title-con">
        <div class="tab-con">
          <div></div>
          <span
            class="tab-item"
            :class="tabActive === 1 && 'tab-item-active'"
            @click="tabChange(1)"
            >{{
              $t('components.SubTaskDetail.5q09ruxan0s0', {
                subTaskId: props.subTaskId
              })
            }}{{
              subTaskInfo.migrationModelId === MIGRATION_MODE.OFFLINE
                ? $t('components.SubTaskDetail.5q09t2cud100')
                : $t('components.SubTaskDetail.5q09t2cudw80')
            }}）</span
          >
          <span
            class="tab-item"
            :class="tabActive === 2 && 'tab-item-active'"
            @click="tabChange(2)"
            >{{ $t('components.SubTaskDetail.5q09prnzmfw0') }}</span
          >
          <a-button v-if="isRefresh.visible" @click="startGetSubTaskDetail" :loading="loading">{{
              $t('detail.index.5q09asiwg4g0')
            }}</a-button>
          <a-button v-if="!isRefresh.visible" @click="stopGetSubTaskDetail" :loading="loading">{{
              $t('detail.index.5q09asiwg4g1')
            }}</a-button>
        </div>
        <span class="task-status">
          {{ $t('components.SubTaskDetail.5q09prnzn6c0') }}
          <b>{{ execSubStatusMap(subTaskInfo.execStatus) }}</b>
        </span>
      </div>
    </template>
    <div class="task-detail-con">
      <div v-if="descData.length" class="task-desc-con">
        <a-descriptions
          :data="descData"
          layout="inline-horizontal"
          :column="5"
          bordered
        />
      </div>
      <div
        v-if="
          tabActive === 1 &&
          subTaskInfo.migrationModelId === MIGRATION_MODE.OFFLINE
        "
        class="progress-con"
      >
        <span class="progress-info">{{
          $t('components.SubTaskDetail.5q09prnzne80')
        }}</span>
        <a-progress
          :status="
            subTaskInfo.execStatus === SUB_TASK_STATUS.MIGRATION_ERROR
              ? 'danger'
              : 'success'
          "
          size="large"
          :percent="
            subTaskInfo.execStatus === SUB_TASK_STATUS.MIGRATION_FINISH
              ? 1
              : Number(subTaskInfo.migrationProcess) || 0
          "
        />
      </div>
      <div
        v-if="
          tabActive === 1 &&
          subTaskInfo.migrationModelId === MIGRATION_MODE.OFFLINE
        "
        class="table-con"
      >
        <big-data-list
          :full-data="fullData"
          :sub-task-info="subTaskInfo"
          :record-counts="recordCounts"
        />
      </div>
      <div
        v-if="
          tabActive === 1 &&
          subTaskInfo.migrationModelId === MIGRATION_MODE.ONLINE
        "
        class="record-con"
      >
        <div class="record-title">
          {{ $t('components.SubTaskDetail.5q09prnzngw0') }}
        </div>
        <div class="record-list">
          <a-steps
            :default-current="Object.keys(statusRecords).length + 1"
            type="dot"
            direction="vertical"
          >
            <a-step v-for="(value, key) in statusRecords" :key="key">
              <div class="record-item-hd">
                <span class="hd-info">{{ recordsMap(key) }}</span>
                <span class="hd-time"
                  >by {{ value[0].operateUser || '-' }},
                  {{ value[0].operateTime || '-' }}</span
                >
              </div>
              <a-card hoverable>
                <div class="record-item-con">
                  <div v-for="item in value" :key="item.id" class="record-item">
                    <div class="record-item-info">
                      <span class="info">
                        <div class="info-title">
                          {{ execSubStatusMap(item.statusId) }}
                        </div>
                        <div
                          v-if="item.statusId === SUB_TASK_STATUS.FULL_RUNNING"
                          class="info-row"
                        >
                          <i @click="globalVisible = !globalVisible">{{
                            globalVisible
                              ? $t('components.SubTaskDetail.5q09prnznk00')
                              : $t('components.SubTaskDetail.5q09prnznms0')
                          }}</i>
                          <div
                            v-if="
                              item.statusId === SUB_TASK_STATUS.FULL_RUNNING
                            "
                            :class="{'status-text':true,'status-text-en':currentLocale==='en-US'}"
                          >
                            <span
                              >{{ $t('components.SubTaskDetail.5q0a5opxm3c1')
                              }}<strong
                                >{{ fullData?.total.data || 0 }}MB</strong
                              ></span
                            >
                            <span
                              >{{ $t('components.SubTaskDetail.5q0a5opxm3c2')
                              }}<strong
                                >{{ fullData?.total.speed || 0 }}MB/s</strong
                              ></span
                            >
                            <span
                              >{{ $t('components.SubTaskDetail.5q0a5opxm3c3')
                              }}<strong
                                >{{ fullData?.total.time || 0 }}s</strong
                              ></span
                            >
                          </div>
                        </div>
                        <div
                          v-if="item.statusId === SUB_TASK_STATUS.FULL_CHECKING"
                          class="info-row"
                        >
                          <div :class="{'status-text':true,'status-text-en':currentLocale==='en-US'}">
                            <span
                              >{{ $t('components.SubTaskDetail.5q0a5opxm3c4')
                              }}<strong
                                >{{ dataCheckData.total || 0 }}行</strong
                              ></span
                            >
                            <span
                              >{{ $t('components.SubTaskDetail.5q0a5opxm3c5')
                              }}<strong
                                >{{ dataCheckData.avgSpeed || 0 }}行/s</strong
                              ></span
                            >
                            <span
                              >{{ $t('components.SubTaskDetail.5q0a5opxm3c6')
                              }}<strong
                                >{{ dataCheckData.tableCount || 0 }}
                                {{
                                  $t('components.SubTaskDetail.5q0a5opxm3c8')
                                }}</strong
                              ></span
                            >
                            <span
                              >{{ $t('components.SubTaskDetail.5q0a5opxm3c7')
                              }}<strong
                                >{{ dataCheckData.completeCount || 0 }}
                                {{
                                  $t('components.SubTaskDetail.5q0a5opxm3c8')
                                }}</strong
                              ></span
                            >
                          </div>
                        </div>
                      </span>
                      <span class="time">{{ item.createTime }}</span>
                    </div>
                    <div
                      v-if="
                        item.statusId === SUB_TASK_STATUS.FULL_RUNNING &&
                        globalVisible
                      "
                      class="table-con"
                    >
                      <big-data-list
                        :full-data="fullData"
                        :sub-task-info="subTaskInfo"
                        :record-counts="recordCounts"
                      />
                    </div>
                    <div
                      v-if="
                        item.statusId === SUB_TASK_STATUS.INCREMENTAL_RUNNING
                      "
                      class="list-con"
                    >
                      <div class="list-item-con">
                        <div class="list-title">
                          <div class="list-title-l">
                            <icon-info-circle size="15" />
                            <span
                              >{{ $t('components.SubTaskDetail.5q09tn4uzy40')
                              }}{{ increaseData.count }}
                              {{
                                $t('components.SubTaskDetail.5q09prnznpk0')
                              }}</span
                            >
                          </div>
                          <div class="list-title-r">
                            <span>{{ increaseData.createTime }}</span>
                          </div>
                        </div>
                        <div class="list-info-em">
                          <span
                            >{{ $t('components.SubTaskDetail.5q09urmrt580')
                            }}{{ increaseData.sourceSpeed || 0 }}
                            {{
                              $t('components.SubTaskDetail.5q09prnzns40')
                            }}</span
                          >
                        </div>
                        <div class="list-info-em">
                          <span
                            >{{ $t('components.SubTaskDetail.5q09urmruc00')
                            }}{{ increaseData.sinkSpeed || 0 }}
                            {{
                              $t('components.SubTaskDetail.5q09prnzns40')
                            }}</span
                          >
                        </div>
                        <div class="list-info">
                          <span
                            >{{ $t('components.SubTaskDetail.5q09urmruhc0')
                            }}{{ increaseData.rest }}
                            {{
                              $t('components.SubTaskDetail.5q09prnznpk0')
                            }}</span
                          >
                        </div>
                      </div>
                    </div>
                    <div
                      v-if="item.statusId === SUB_TASK_STATUS.REVERSE_RUNNING"
                      class="list-con"
                    >
                      <div class="list-item-con">
                        <div class="list-title">
                          <div class="list-title-l">
                            <icon-info-circle size="15" />
                            <span
                              >{{ $t('components.SubTaskDetail.5q09tn4uzy41')
                              }}{{ reverseData.count }}
                              {{
                                $t('components.SubTaskDetail.5q09prnznpk0')
                              }}</span
                            >
                          </div>
                          <div class="list-title-r">
                            <span>{{ reverseData.createTime }}</span>
                          </div>
                        </div>
                        <div class="list-info-em">
                          <span
                            >{{ $t('components.SubTaskDetail.5q09urmrt580')
                            }}{{ reverseData.sourceSpeed || 0 }}
                            {{
                              $t('components.SubTaskDetail.5q09prnzns40')
                            }}</span
                          >
                        </div>
                        <div class="list-info-em">
                          <span
                            >{{ $t('components.SubTaskDetail.5q09urmruc00')
                            }}{{ reverseData.sinkSpeed || 0 }}
                            {{
                              $t('components.SubTaskDetail.5q09prnzns40')
                            }}</span
                          >
                        </div>
                        <div class="list-info">
                          <span
                            >{{ $t('components.SubTaskDetail.5q09urmruhc0')
                            }}{{ reverseData.rest || 0 }}
                            {{
                              $t('components.SubTaskDetail.5q09prnznpk0')
                            }}</span
                          >
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </a-card>
            </a-step>
          </a-steps>
        </div>
      </div>
      <div v-if="tabActive === 2" class="log-con">
        <a-list>
          <a-list-item v-for="item in logData" :key="item.url">
            <div class="log-detail-info">
              <span>{{ item.name }}</span>
              <span class="desc">{{ logsMap(item.name) }}</span>
            </div>
            <template #actions>
              <a-button
                type="text"
                size="mini"
                @click="handleDownloadLog(item.url)"
              >
                <template #icon>
                  <icon-download />
                </template>
                {{ $t('components.SubTaskDetail.5q09prnznvg0') }}
              </a-button>
            </template>
          </a-list-item>
        </a-list>
      </div>
    </div>
  </a-drawer>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, h, reactive } from 'vue'
import { subTaskDetail, downloadLog } from '@/api/detail'
import BigDataList from './BigDataList.vue'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { MIGRATION_MODE, SUB_TASK_STATUS } from '@/utils/constants'
import useLocale from '@/hooks/locale'

const { currentLocale } = useLocale()

const { t } = useI18n()

const props = defineProps({
  open: Boolean,
  taskInfo: Object,
  subTaskId: [String, Number],
  tab: [String, Number]
})

const emits = defineEmits(['update:open'])

let timer = null

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
    100: t('components.SubTaskDetail.5q09prnzp540'),
    500: t('components.SubTaskDetail.5q09prnzp740'),
    1000: t('components.SubTaskDetail.5q09prnzp980')
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

watch(
  () => props.open,
  (v) => {
    if (v) {
      tabActive.value = props.tab
      globalVisible.value = false
      increaseData.value = {}
      reverseData.value = {}
      logData.value = []
      getSubTaskDetail()
      if (!timer) {
        timer = setInterval(() => {
          getSubTaskDetail()
        }, 6000)
      }
    } else {
      globalVisible.value = false
      increaseData.value = {}
      reverseData.value = {}
      subTaskInfo.value = {}
      descData.value = []
      logData.value = []
      loading.value = false
    }
    visible.value = v
  }
)

const tabChange = (tab) => {
  tabActive.value = tab
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

const isRefresh = reactive({
  visible: false,
});

const stopGetSubTaskDetail = () => {
  isRefresh.visible = !isRefresh.visible;
  clearInterval(timer)
}
const startGetSubTaskDetail = () => {
  isRefresh.visible = !isRefresh.visible;
  timer = setInterval(() => {getSubTaskDetail()}, 6000)
}

const getSubTaskDetail = () => {
  loading.value = true
  subTaskDetail(props.subTaskId)
    .then((res) => {
      loading.value = false
      subTaskInfo.value = res.data.task
      if (subTaskInfo.value.finishTime !== null) {
        clearInterval(timer)
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
          value: `${
            hour ? hour + t('components.SubTaskDetail.5q09prnzqpw0') : ''
          } ${
            minute ? minute + t('components.SubTaskDetail.5q09prnzqs00') : ''
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
          label: t('components.SubTaskDetail.5q09prnzr4s0'),
          value: `${subTaskInfo.value.sourceDb} ${t(
            'components.SubTaskDetail.5q0a5opxm3c0'
          )} ${subTaskInfo.value.targetDb}`,
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
          span: 3
        },
        {
          label: t('components.SubTaskDetail.5q09prnzqnw0'),
          value: `${
            hour ? hour + t('components.SubTaskDetail.5q09prnzqpw0') : ''
          } ${
            minute ? minute + t('components.SubTaskDetail.5q09prnzqs00') : ''
          }`,
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
          span: 5
        }
      ]

      descData.value =
        res.data.task.migrationModelId === 1 ? offlineDesc : onlineDesc

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
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="less" scoped>
.title-con {
  width: calc(60vw - 60px);
  display: flex;
  justify-content: space-between;
  align-items: center;

  .tab-con {
    .tab-item {
      cursor: pointer;
      color: var(--color-text-3);
      font-weight: 400;
      font-size: 15px;
      margin-right: 10px;
      transition: all 0.1s linear;

      &.tab-item-active {
        color: var(--color-text-1);
        font-weight: 500;
        font-size: 16px;
      }

      &:hover {
        color: var(--color-text-1);
      }
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
    .data-count {
      cursor: pointer;
      color: var(--color-text-3);
    }
  }

  .record-con {
    margin-top: 20px;

    .record-title {
      font-weight: bold;
      margin-bottom: 10px;
    }

    .record-list {
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
                .status-text-en{
                  left:215px;
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
}
</style>
