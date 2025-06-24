<template>
  <div class="detail-container common-layout" id="migrationDetail">
    <div class="mainDetail">
      <div class="leftContent">
        <div class="top-content basic-title">
          <img src="@/assets/images/list.png" width="40" height="40" />
          <div class="title-right">
            <TextTooltip class="name-text" :content="subTaskId"></TextTooltip>
            <el-tag v-if="execSubStatusMap(subTaskInfo.execStatus)"
              :type="statusColorMap?.[subTaskInfo?.execStatus] || ''" class="status-tag">{{
                execSubStatusMap(subTaskInfo.execStatus) }}
            </el-tag>
          </div>
        </div>
        <div class="bottom-content basic-info">
          <div class="info-title">{{ $t('components.SubTaskDetail.baseInfo') }}</div>
          <div class="basicItem" :key="item.label" v-for="(item) in descData" style="margin-bottom:16px">
            <TextTooltip class="basicLable" :content="item.label"></TextTooltip>
            <TextTooltip class="basicValue" :content="item.value"></TextTooltip>
          </div>
        </div>
      </div>
      <div class="rightContent">
        <div class="switchUpdate">
          <div class="switchDesc">
            {{ switchRefreshText }}
          </div>

          <el-switch v-model="autoRefresh" :active-value="true" :inactive-value="false" size="small"></el-switch>
        </div>
        <div class="row-content">
          <el-tabs stretch v-model="currentTab">
            <!-- with lazy attribute, this card would not render until first click,-->
            <el-tab-pane :label="t('components.SubTaskDetail.migrationProcess')" name="migrationProcess" lazy>
              <MigrationProcess :subTaskMode="subTaskMode" v-if="currentTab === 'migrationProcess'"
                :processObj="processObj" :subTaskStep="subTaskStep" :fullProcessCount="fullProcessCount">
              </MigrationProcess>
            </el-tab-pane>
            <el-tab-pane :label="t('components.SubTaskDetail.abnormalAlarms')" name="errorAlert" lazy>
              <template #label>
                <div>{{ t('components.SubTaskDetail.abnormalAlarms') }}
                  <el-tag type="round" size="small" class="errorAlertCount">{{ phaseNums.total || 0 }}</el-tag>
                </div>
              </template>
              <ErrorAlert v-if="currentTab === 'errorAlert'" :phaseNums="phaseNums"/>
            </el-tab-pane>
            <el-tab-pane :label="t('components.SubTaskDetail.log')" name="log" lazy>
              <MigrationLog />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, h, reactive, computed, provide } from 'vue';
import {
  subTaskInfoDetail,
  subTaskBasicInfo,
  getTotalAlarmNum,
  refreshStatus,
} from '@/api/detail';
import TextTooltip from "@/components/textTooltip/index.vue";
import { useI18n } from 'vue-i18n';
import { MIGRATION_MODE, SUB_TASK_STATUS } from '@/utils/constants';
import MigrationProcess from './migrationProcess/index.vue';
import ErrorAlert from './errorAlert';
import MigrationLog from './migrationLogs.vue';
import { useSubTaskStore } from '@/store';
import Socket from '@/utils/websocket'
const { t } = useI18n();
// The various states included in the migration progress
const stepSet0 = new Set([SUB_TASK_STATUS.NOT_RUN, SUB_TASK_STATUS.CHECK_FAILED]);
const stepSet1 = new Set([SUB_TASK_STATUS.FULL_START, SUB_TASK_STATUS.FULL_RUNNING, SUB_TASK_STATUS.FULL_FINISH, SUB_TASK_STATUS.REVERSE_CONNECT_ERROR])
const stepSet2 = new Set([SUB_TASK_STATUS.FULL_CHECK_START, SUB_TASK_STATUS.FULL_CHECKING, SUB_TASK_STATUS.FULL_CHECK_FINISH, SUB_TASK_STATUS.CHECK_FAILED])
const stepSet3 = new Set([SUB_TASK_STATUS.INCREMENTAL_START, SUB_TASK_STATUS.INCREMENTAL_RUNNING, SUB_TASK_STATUS.INCREMENTAL_FINISHED, SUB_TASK_STATUS.INCREMENTAL_STOPPED, SUB_TASK_STATUS.INCREMENTAL_CONNECT_ERROR])
const stepSet4 = new Set([SUB_TASK_STATUS.REVERSE_START, SUB_TASK_STATUS.REVERSE_RUNNING, SUB_TASK_STATUS.REVERSE_STOP, SUB_TASK_STATUS.REVERSE_CONNECT_ERROR])
const stepSet5 = new Set([SUB_TASK_STATUS.MIGRATION_FINISH]);
// Here for states of 100, 500, 1000, 2000, another field is required
const stepJudge = new Set([SUB_TASK_STATUS.MIGRATION_FINISH, SUB_TASK_STATUS.MIGRATION_ERROR, SUB_TASK_STATUS.WAIT_RESOURCE, SUB_TASK_STATUS.INSTALL_PORTAL])

const subTaskStore = useSubTaskStore();
const subTaskStep = ref(1);
const subTaskMode = ref(2);
const props = defineProps({
  open: Boolean,
  taskInfo: Object,
  subTaskId: [String, Number],
  tab: [String, Number]
});
const subTaskId = ref();
provide('subTaskId', subTaskId);
const currentTab = ref('');// The data definition is empty, and the tab content is rendered after mounted

const currentWS = ref()

// Objects passed to child components - child components need to watch for rendering, because there is a delay in rendering time on the parent component's side
const processObj = ref()

// Data on the number of abnormal alarms
const phaseNums = ref({
  '1': 0,
  '2': 0,
  '3': 0,
  '4': 0,
  total: 0,
})

const fullProcessCount = ref({
  totalErrorCount: 0,
  totalRunningCount: 0,
  totalSuccessCount: 0,
  totalWaitCount: 0,
})

const descValueObj = ref({
  subTaskName: '',
  fatherTask: '',
  executionMode: '',
  sourceIpPort: '',
  sourceLibrary: '',
  sourceDbType: '',
  sinkIpPort: '',
  sinkLibrary: '',
  executeTime: '',
  createTime: '',
  initiateTime: '',
})

const descData = computed(() => [
  {
    label: t('components.SubTaskDetail.subTaskName'),
    value: descValueObj.value.subTaskName || '--',
    prop: 'subTaskName'
  },
  {
    label: t('components.SubTaskDetail.fatherTask'),
    value: descValueObj.value.fatherTask || '--',
    prop: 'fatherTask'
  },
  {
    label: t('components.SubTaskDetail.executionMode'),
    value: descValueObj.value.executionMode || '--',
    prop: 'executionMode'
  },
  {
    label: t('detail.index.sourceIpPort'),
    value: descValueObj.value.sourceIpPort || '--',
    prop: 'sourceLibrary'
  },
  {
    label: t('components.SubTaskDetail.sourceLibrary'),
    value: descValueObj.value.sourceLibrary || '--',
    prop: 'sourceLibrary'
  },
  {
    label: t('components.SubTaskDetail.sourceLibraryType'),
    value: descValueObj.value.sourceDbType || '--',
    prop: 'sourceDbType'
  },
  {
    label: t('detail.index.targetIpPort'),
    value: descValueObj.value.sinkIpPort || '--',
    prop: 'sourceLibrary'
  },
  {
    label: t('components.SubTaskDetail.sinkLibrary'),
    value: descValueObj.value.sinkLibrary || '--',
    prop: 'sinkLibrary'
  },
  {
    label: t('components.SubTaskDetail.executedTime'),
    value: descValueObj.value.executeTime || '--',
    prop: 'executeTime'
  },
  {
    label: t('components.SubTaskDetail.createTime'),
    value: descValueObj.value.createTime || '--',
    prop: 'createTime'
  },
  {
    label: t('components.SubTaskDetail.initiateTime'),
    value: descValueObj.value.initiateTime || '--',
    prop: 'initiateTime'
  }
])
// Obtain the current step and execution time of the step bar
const getTopExpressInfo = (info) => {
  subTaskInfo.value.execStatus = info?.execStatus
  // Gets the state of the runtime returned by the current interface
  let subTaskStatus = info?.execStatus;
  // Determine whether to run success/failure status, these states cannot directly determine the step, you need to use the previous state
  if (stepJudge.has(subTaskStatus)) {
    subTaskStatus = info?.currentExecStatus
  }
  if (stepSet0.has(subTaskStatus)) {
    subTaskStep.value = 0;
  } else if (stepSet5.has(subTaskStatus)) {
    subTaskStep.value = 5;
  } else if (stepSet4.has(subTaskStatus)) {
    subTaskStep.value = 4;
  } else if (stepSet3.has(subTaskStatus)) {
    subTaskStep.value = 3;
  } else if (stepSet2.has(subTaskStatus)) {
    subTaskStep.value = 2;
  } else {
    subTaskStep.value = 1;
  }
  fullProcessCount.value.totalErrorCount = info?.totalErrorCount;
  fullProcessCount.value.totalRunningCount = info?.totalRunningCount;
  fullProcessCount.value.totalSuccessCount = info?.totalSuccessCount;
  fullProcessCount.value.totalWaitCount = info?.totalWaitCount;
  // Time Conversion - Here it is updated every 5 seconds, and it is necessary to judge whether the seconds are displayed or not
  try {
    if (!isNaN(Number(info?.executedTime))) {
      descValueObj.value.executeTime = getExectedTime(info?.executedTime)
      for (let i = 0; i < descData.value.length; i++) {
        if (descData.value[i].prop === 'executedTime') {
          descData.value[i].value = getExectedTime(info?.executedTime)
          return
        }
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const getExectedTime = (timer) => {
  if (!timer) {
    return '--'
  }
  const hours = parseInt(timer / 3600);
  const min = parseInt((timer % 3600) / 60);
  const sec = timer - 3600 * hours - 60 * min;
  let result = '';
  if (timer >= 3600) {
    // Turn to hours
    result = hours + t('components.SubTaskDetail.hour') + min + t('components.SubTaskDetail.min') + sec + t('components.SubTaskDetail.sec')
  } else if (timer >= 60) {
    result = min + t('components.SubTaskDetail.min') + sec + t('components.SubTaskDetail.sec')
  } else {
    result = sec + t('components.SubTaskDetail.sec')
  }
  return result;
}

const connectNums = ref(0);
const maxConnectTimes = ref(15);

// Determine whether the object can be transferred
const testWebsocketFunc = () => {
  const socketUrl = `data-migration/taskInfo`
  const websocket = new Socket({ url: socketUrl })
  currentWS.value = websocket;
  websocket.onopen(() => {
    connectWSTest(subTaskId.value)
  })

  websocket.onmessage((data) => {
    if (data) {
      isWSConnect.value = true;
      try {
        const subTaskDetailObj = JSON.parse(data)
        // Pass information to child components
        processObj.value = subTaskDetailObj;
        if (subTaskDetailObj) {
          subTaskStore.updateSubTaskData(subTaskDetailObj)
        }
        // Here, the information obtained by the websocket is processed accordingly
        getTopExpressInfo(subTaskDetailObj)
      } catch (e) {
        console.error('subTaskDetail websocket message parse failed')
      }
    }
  })
  websocket.onclose(() => {
    console.warn('close-subTaskDetail-webscoket')
  })
}
const isWSConnect = ref(false)
const timer = ref()
const connectWSTest = (id) => {
  subTaskInfoDetail(subTaskId.value, 'taskInfo').then(res => {
    timer.value && clearTimeout(timer)
    if (!isWSConnect.value) {
      timer.value = setTimeout(() => {
        connectWSTest(id)
      }, 5000)
    }
  })
}

onMounted(() => {
  subTaskId.value = window.$wujie?.props.data.id
  currentTab.value = window.$wujie?.props.data.tab || 'migrationProcess'
  
  descValueObj.value.sourceIpPort = sessionStorage.getItem('sourceIpPort')
  descValueObj.value.sinkIpPort = sessionStorage.getItem('sinkIpPort')
  // The webSocket is tested here
  testWebsocketFunc()

  // Get the basics here
  getSubTaskBasicInfo();
});

// Query the basic details of a subtask
const getSubTaskBasicInfo = () => {
  subTaskBasicInfo(subTaskId.value).then(res => {
    if (Number(res.code) === 200) {
      descValueObj.value.subTaskName = res.data?.subTaskId;
      descValueObj.value.fatherTask = res.data?.taskName;
      // offline = 1; online = 2;
      descValueObj.value.executionMode = res.data?.execMode === 1 ? t('components.SubTaskDetail.offLineInstall') : t('components.SubTaskDetail.onLineInstall')
      descValueObj.value.sourceLibrary = res.data?.sourceDb
      descValueObj.value.sourceDbType = res.data?.sourceDbType
      descValueObj.value.sinkLibrary = res.data?.targetDb
      // The fifth one is assigned in the websocket
      if (res.data?.createTime) {
        // descData.value[6].value = transTimeType(res.data?.createTime)
        descValueObj.value.createTime = transTimeType(res.data?.createTime)
      }
      if (res.data?.startTime) {
        // descData.value[7].value = transTimeType(res.data?.startTime)
        descValueObj.value.initiateTime = transTimeType(res.data?.startTime)
      }
      subTaskMode.value = res.data?.execMode || 2;
    }
  }).catch(error => {
    console.error(error)
  })
}

const transTimeType = (isoTypeTime) => {
  const date = new Date(isoTypeTime);
  // Change the timestamp to year, month, and day Hours, minutes, seconds
  const Y = date.getFullYear() + '-';
  const M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
  const D = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()) + ' ';
  const h = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':';
  const m = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes()) + ':';
  const s = (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
  return Y + M + D + h + m + s;
}

// Before/off the corresponding webscoket before leaving the page
const closeWS = () => {
  console.log('close', 'restart')
  if (currentWS.value) {
    console.log(currentWS.value, 'current-ws')
    currentWS.value?.destroy()
    currentWS.value = null;
  }
}

const autoRefresh = ref(true);
provide('autoRefresh', autoRefresh);
const emits = defineEmits(['update:open']);
const loading = ref(false);
const subTaskInfo = ref({});

const switchRefreshText = computed(() => {
  return autoRefresh.value ? t('components.SubTaskDetail.autoRefresh') : t('components.SubTaskDetail.stopRefresh')
})
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
  };
  return maps[status];
};

const statusColorMap = {
  0: 'info',
  1: 'primary',
  2: 'primary',
  3: 'primary',
  4: 'primary',
  5: 'primary',
  6: 'primary',
  7: 'primary',
  8: 'primary',
  9: 'primary',
  10: 'primary',
  11: 'primary',
  12: 'primary',
  13: 'primary',
  30: 'danger',
  40: 'danger',
  100: 'success',
  500: 'danger',
  1000: 'primary',
  3000: 'danger'
};
// timer
const intervalid = ref(null);

// You need to poll for the number of abnormal alarms
const getErrorTotal = async (type) => {
  if (type === 'loopQuery' && !autoRefresh.value) {
    // If it is polled and the auto-refresh has now ended, the query is no longer made
    return;
  }
  if (!subTaskId.value) {
    return;
  }
  try {
    const { data, code } = await getTotalAlarmNum(subTaskId.value)
    if (code === 200) {
      phaseNums.value = data ?? {}
    }
  } catch (error) {
    console.error(error)
  }
}

watch(() => autoRefresh.value, () => {
  if (autoRefresh.value) {
    if (intervalid.value == null) {
      intervalid.value = setInterval(() => {
        getErrorTotal('loopQuery')
      }, 6000);
    }
  } else {
    cancelInterval()
  }
}, { immediate: true })

// Dismiss the timer query
const cancelInterval = () => {
  if (intervalid.value != null) {
    clearInterval(intervalid.value)
    intervalid.value = null
  }
}

onBeforeUnmount(() => {
  closeWS();
  cancelInterval();
});
</script>

<style lang="less" scoped>
.detail-container {
  height: calc(100vh - 114px);
  background-color: var(--o-bg-color-light);
  padding: 20px 24px 28px 20px;
  overflow-x: auto;

  .mainDetail {
    display: flex;
    gap: 24px;
    min-height: 100%;
    min-width: 1000px;

    .leftContent {
      width: 292px;
      display: flex;
      flex-direction: column;
      gap: 16px;

      .basic-title {
        height: 80px;
        background-color: var(--o-bg-color-light2);
        display: flex;
        padding: 0 24px;
        align-items: center;
        gap: 16px;

        img {
          width: 40px;
          height: 40px;
        }

        .title-right {
          flex: 1;
          width: 0;
          display: flex;
          flex-direction: column;
          gap: 8px;
          color: var(--o-text-color-primary);

          .name-text {
            height: 24px;
            line-height: 24px;
            font-size: 16px;
            font-weight: bolder;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .el-tag {
            width: fit-content;
            padding: 4px 24px;
            font-size: 12px;
            max-width: 172px !important;
          }
        }
      }

      .basic-info {
        flex: 1;
        padding: 20px 22px;
        background-color: var(--o-bg-color-light2);
        color: var(--o-text-color-primary);

        .info-title {
          height: 24px;
          line-height: 24px;
          font-size: 16px;
          font-weight: bolder;
        }

        .basicItem {
          display: flex;
          height: 24px;
          line-height: 24px;
          margin-top: 12px;
          gap: 4px;

          .basicLable {
            min-width: 106px;
            color: var(--o-text-color-secondary)
          }

          .basicValue {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }

    .rightContent {
      flex: 1;
      display: flex;
      flex-direction: column;
      width: 0;
      gap: 16px;
      position: relative;

      .switchUpdate {
        position: absolute;
        width: fit-content;
        right: 0;
        top: 0;
        display: flex;
        gap: 8px;
        z-index: 11;

        .switchDesc {
          color: var(--o-text-color-primary);
        }
      }

      ::v-deep(.row-content) {
        height: 100%;

        .el-tabs {
          height: 100%;
        }

        .el-tab-pane {
          height: 100%;
        }

        .el-tabs__nav.is-stretch {
          gap: 32px;

          .el-tabs__item {
            max-width: fit-content;
            padding: 4px 0;
          }
        }

        .errorAlertCount {
          margin-left: 0px;
        }
      }

      .top-content {
        height: 80px;
        display: flex;
        width: 100%;

        .card-area {
          flex: 1;
          display: flex;
          gap: 4px;
          max-width: calc(100% - 192px);

          .main-card {
            max-width: 182px;
            width: calc(100% - 20px) / 6;
          }
        }
      }

      .bottom-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 24px;
        background-color: var(--o-bg-color-light2);

        .list-title {
          height: 24px;
          font-size: 16px;
          font-weight: 600;
          color: #000;
          margin-bottom: 16px;
        }

        .main-table {
          flex: 1;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
        }
      }
    }
  }
}
</style>
