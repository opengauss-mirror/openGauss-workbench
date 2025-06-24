<template>
  <div class="full-migration" v-loading="fixLoading">
    <div class="statistics">
      <div class="card-area">
        <statistic-card :count="migrationCount" :description="t('components.SubTaskDetail.migrationObjNum')"
          class="total-card"></statistic-card>
        <statistic-card :count="restWriteInCount"
          :description="t('components.SubTaskDetail.restWriteData')"></statistic-card>
      </div>
      <div class="button-area">
        <el-button type="primary" @click="handleFix('reset')">{{ t('components.SubTaskDetail.oneClickRepair')
          }}</el-button>
        <el-button v-if="currentActive === 3 && subTaskMode === 2 &&
          currentExecStatus === SUB_TASK_STATUS.INCREMENTAL_RUNNING" @click="stopSubIncrese">{{
            t('components.SubTaskDetail.stopIncreaseMigration') }}</el-button>
        <el-button
          v-if="currentActive === 3 && subTaskMode === 2 && currentExecStatus === SUB_TASK_STATUS.INCREMENTAL_STOPPED"
          @click="startSubReverse">{{ t('detail.index.5q09asiwkq40') }}</el-button>
      </div>
    </div>
    <div class="sub-tabs">
      <div class="process-card">
        <div class="info">
          <el-icon v-if="fixStatus.source" class="icon" size="21"><icon-success /></el-icon>
          <el-icon v-else class="icon" size="21"><icon-error /></el-icon>
          {{ t('components.SubTaskDetail.sourceRepair') }}
        </div>
        <span class="fix-button" @click="handleFix('source')">{{ t('components.SubTaskDetail.oneClickRepair') }}</span>
      </div>
      <div class="process-card">
        <div class="info">
          <el-icon v-if="fixStatus.sink" size="21" class="icon"><icon-success /></el-icon>
          <el-icon v-else size="21" class="icon"><icon-error /></el-icon>
          {{ t('components.SubTaskDetail.targetRepair') }}
        </div>
        <span class="fix-button" @click="handleFix('sink')">{{ t('components.SubTaskDetail.oneClickRepair') }}</span>
      </div>
    </div>
    <ReverseDetail v-if="reverseVisible" @closeDialog="closeDialog" :replicationData="replicationData"
      :reverseConfig="reverseConfig"></ReverseDetail>
  </div>
</template>
<script setup lang="ts">
import StatisticCard from '@/components/statisticCard'
import { IconSuccess, IconError } from '@computing/opendesign-icons';
import { computed, inject, onBeforeUnmount, onMounted, watch, ref } from 'vue'
import {
  getOnlineReverseStatus, queryFullCheckDetail,
  startOnlineReverseProcess, subTaskStartReverse, subTaskStopIncremental,
} from '@/api/detail'
import { useSubTaskStore } from '@/store';
import type { Ref } from "vue";
import { SUB_TASK_STATUS } from "@/utils/constants";
import showMessage from '@/utils/showMessage';
import { useI18n } from 'vue-i18n';
import ReverseDetail from './reverseDetail.vue';
const { t } = useI18n();
const subTaskStore = useSubTaskStore();
// The default migration is currently incremental
const currentActive = ref(3)
const reverseConfig = ref({});
const replicationData = ref([])
const props = defineProps(({
  active: {
    type: [Number, String],
    required: true,
  },
  subTaskMode: {
    type: Number,
    default: 1,
  },
}))
const subTaskId = inject('subTaskId')
const fixLoading = ref(false)

// This is the status of the current fix
const fixStatus = ref({
  source: true,
  sink: true
})

const currentExecStatus = computed(() => subTaskStore.subTaskData?.execStatus);
const subTaskMode = computed(() => props.subTaskMode)
// Storage timer
const onlineReverseStatusTimer = ref(null)

const isReverse = computed(() => {
  const reverseSet = new Set([SUB_TASK_STATUS.REVERSE_START, SUB_TASK_STATUS.REVERSE_RUNNING, SUB_TASK_STATUS.REVERSE_STOP, SUB_TASK_STATUS.REVERSE_CONNECT_ERROR])
  return reverseSet.has(subTaskStore.subTaskData?.execStatus)
})
const reverseVisible = ref(false);
// This is the toggle between listening for incremental migration and reverse migration
watch(() => props.active, (newVal, oldVal) => {
  currentActive.value = props.active;
})

const closeDialog = () => {
  reverseVisible.value = false;
}

// The number of successes/failures at the source
const migrationCount = computed(() => {
  let sourceCount = 0;
  try {
    const processObj = props.active === 4 ?
      JSON.parse(subTaskStore.subTaskData.reverseProcess?.execResultDetail || {}) :
      JSON.parse(subTaskStore.subTaskData.incrementalProcess?.execResultDetail || {})
    sourceCount = processObj?.count || 0
    return sourceCount
  } catch (err) {
    console.error('subTaskDetail-increase json.parse error', err)
    return sourceCount;
  }
})

// The number of successes/failures on the target side
const restWriteInCount = computed(() => {
  let sourceCount = 0;
  try {
    const processObj = props.active === 4 ?
      JSON.parse(subTaskStore.subTaskData.reverseProcess?.execResultDetail || {}) :
      JSON.parse(subTaskStore.subTaskData.incrementalProcess?.execResultDetail || {})
    sourceCount = processObj?.rest || 0
    return sourceCount
  } catch (err) {
    console.error('subTaskDetail-increase json.parse error', err)
    return sourceCount;
  }
})

// Stop Increment - You need to determine whether it is a reverse migration
const stopSubIncrese = () => {
  subTaskStopIncremental(subTaskId.value).then((res) => {
    if (res.code === 200) {
      showMessage('success', t('detail.index.stopSuccess'))
    }
  })
}

// 这里是启动反向迁移
const startSubReverse = () => {
  subTaskStartReverse(subTaskId.value)
    .then(() => {
      showMessage('success', t('detail.index.startSuccess'))
    })
    .catch((e) => {
      if (e.code === 50154) {
        reverseConfig.value = e.data
        replicationData.value = [
          {
            label: 'rolcanlogin',
            value: e.data.rolcanlogin === 'true' ? 't' : 'f'
          },
          {
            label: 'rolreplication',
            value: e.data.rolreplication === 'true' ? 't' : 'f'
          }
        ]
        reverseVisible.value = true;
      }
      if (e.code === 50155) {
        showMessage('error', t('detail.index.5qtkk99a2eo0'))
      }
    })
}

// One-click repair method
const handleFix = async (type) => {
  fixLoading.value = true
  try {
    let name = type;
    if (isReverse.value && props.active === 4) {
      name = `reverse-${name}`
    }
    const startRes = await startOnlineReverseProcess(subTaskId.value, name)
    if (startRes.code === 200) {
      showMessage('success', t('components.SubTaskDetail.restartSuccessTip'))
    }
    fixLoading.value = false
  } catch (error) {
    fixLoading.value = false
    console.error(error)
  }
}

onMounted(() => {
  // This is the initial time to determine whether the current migration is incremental or reverse, if there is any change, the watch will listen
  currentActive.value = props.active
  getFixStatus()
// Poll to obtain the migration status
  onlineReverseStatusTimer.value = setInterval(() => {
    getFixStatus()
  }, 6000)
})

onBeforeUnmount(() => {
  // Clear the timer query
  clearInterval(onlineReverseStatusTimer.value)
  onlineReverseStatusTimer.value = null
})

// Gets the status of the current modification
const getFixStatus = async () => {
  try {
    const { data, code } = await getOnlineReverseStatus(subTaskId.value)
    if (code === 200) {
      fixStatus.value.source = data.source
      fixStatus.value.sink = data.sink
    }
  } catch (error) {
    console.error(error)
  }
}
</script>
<style lang="less" scoped>
.full-migration {
  display: flex;
  flex-direction: column;
  height: 100%;

  .statistics {
    height: 80px;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;

    .card-area {
      display: flex;

      .total-card {
        margin-right: 4px;
      }
    }

    .button-area {
      align-self: flex-end;
    }
  }

  .sub-tabs {
    flex: 1;
    border-radius: 0px 4px 4px 4px;
    background-color: var(--o-bg-color-base);
    padding: 24px;

    .process-card {
      display: flex;
      background-color: var(--o-bg-color-light);
      color: var(--o-text-color-primary);
      height: 80px;
      padding: 26px;
      border-radius: 8px;
      justify-content: space-between;
      margin-bottom: 4px;
      align-items: center;

      .info {
        display: flex;
        align-items: center;

        .icon {
          margin-right: 13px;
        }
      }

      .fix-button{
        cursor: pointer;
      }

    }
  }
}
</style>
