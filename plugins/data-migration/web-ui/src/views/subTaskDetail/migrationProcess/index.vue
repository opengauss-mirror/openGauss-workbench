<template>
  <div class="common-layout">
    <div class="steps">
      <common-steps :steps="steps" :current="current" :subTaskDbType="subTaskDbType" v-model:active="active" class="steps-content"></common-steps>
    </div>
    <div class="process-item">
      <full-migration v-if="active === 1"></full-migration>
      <full-check v-else-if="active === 2"></full-check>
      <increment v-else :active="active" :subTaskMode="subTaskMode" :subTaskDbType="subTaskDbType"></increment>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, watch, computed, inject } from 'vue'
import CommonSteps from '@/components/commonSteps'
import FullMigration from './FullMigration.vue'
import FullCheck from './FullCheck.vue'
import Increment from './Increment.vue'
import { useSubTaskStore } from '@/store';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const subTaskStore = useSubTaskStore();
const props = defineProps({
  processObj: {
    type: [Object, null]
  },
  subTaskStep: {
    type: Number,
    default: 1
  },
  subTaskMode: {
    type: Number,
    default: 1,
  },
  fullProcessCount: {
    type: Object
  },
  subTaskDbType :{
    type: String,
  }
})
const subTaskId = inject('subTaskId');
const current = ref(1)
const active = ref(1)

// It is necessary to judge the current step as the first step,,, and then there is no need to judge again if it has already been completed
const finishStep = reactive({
  firstStep: false,
  secondStep: false,
  thirdStep: false,
  fourthStep: false,
})

const totalProcessDesc = reactive({
  dataName: t('components.SubTaskDetail.dataTotal'),
  data: 0,
  dataRate: 'MB',
  speedName: t('components.SubTaskDetail.speed'),
  speed: 0,
  speedRate: 'MB/s',
  timeName: t('components.SubTaskDetail.costTime'),
  time: 0,
  timerRate: 's'
})

const totalCheckDesc = reactive({
  data: 0,
  speed: 0,
})

const constantRate = computed(() => {
  return {
    dataName: t('components.SubTaskDetail.dataTotal'),
    speedName: t('components.SubTaskDetail.speed'),
    timeName: t('components.SubTaskDetail.costTime'),
    increaseSpeed: t('components.SubTaskDetail.increaseSpeed'),
    increaseWriteSpeed: t('components.SubTaskDetail.increaseWriteSpeed'),
    speedRate: t('components.SubTaskDetail.speedRate')
  }
})

const increaseProcessDesc = reactive({
  sourceSpeed: 0,
  sinkSpeed: 0,
})

const reverseProcess = reactive({
  sourceSpeed: 0,
  sinkSpeed: 0,
})

const subTaskMode = ref(2);
const subTaskDbType = ref('')
const steps = computed(() => {
  // A value is assigned to the full migration description
  const totalDesc = constantRate.value.dataName + totalProcessDesc.data + 'MB ' +
    constantRate.value.speedName + ' ' + totalProcessDesc.speed + totalProcessDesc.speedRate;
  // Full check describes the assignment
  const fullCheckDesc = current.value > 1 ? constantRate.value.dataName + ' ' + totalCheckDesc.data + t('components.SubTaskDetail.5q09prnznpk0') + ' ' +
    constantRate.value.speedName + ' ' + totalCheckDesc.speed + t('components.SubTaskDetail.5q09prnzns40') + ' ' : '';
  // Incremental migration describes the assignment
  let nextStep = [];
  // If offline, incremental and reverse migrations are not shown [1 is offline, 2 is online]
  if (subTaskMode.value !== 1) {
    const increProcessDesc = current.value >= 3 ? constantRate.value.increaseSpeed + ' ' + increaseProcessDesc.sourceSpeed + constantRate.value.speedRate + ' ' +
      constantRate.value.increaseWriteSpeed + ' ' + increaseProcessDesc.sinkSpeed + constantRate.value.speedRate : ''
    // Reverse migration describes the assignment
    const reverseProcessDesc = current.value >= 4 ? constantRate.value.increaseSpeed + ' ' + reverseProcess.sourceSpeed + constantRate.value.speedRate + ' ' +
      constantRate.value.increaseWriteSpeed + ' ' + reverseProcess.sinkSpeed + constantRate.value.speedRate : ''
    nextStep = [
      {
        stepIndex: 3,
        title: t('components.SubTaskDetail.incrementMigration'),
        description: increProcessDesc
      }, {
        stepIndex: 4,
        title: t('components.SubTaskDetail.reverseMigration'),
        description: reverseProcessDesc
      }
    ]
  }

  return [{
    stepIndex: 1,
    title: t('components.SubTaskDetail.fullMigration'),
    description: current.value > 1 ? totalDesc + ' ' + constantRate.value.timeName + ' ' + totalProcessDesc.time + totalProcessDesc.timerRate : totalDesc
  }, {
    stepIndex: 2,
    title: t('components.SubTaskDetail.fullCheck'),
    description: fullCheckDesc
  }, ...nextStep]
})

watch(props, (newVal, oldVal) => {
  //  Here the total amount of data and the time taken for each rate are updated --- Note: The time taken is only displayed when it is completed
  if (active.value === current.value) {
    active.value = props.subTaskStep
  }
  current.value = props.subTaskStep
  subTaskMode.value = props.subTaskMode;
  subTaskDbType.value = props.subTaskDbType
  try {
    // Assign a value for full migration
    if (subTaskStore.subTaskData?.fullProcess?.execResultDetail) {
      const fullProcessObj = JSON.parse(subTaskStore.subTaskData.fullProcess.execResultDetail);
      if (fullProcessObj) {
        totalProcessDesc.data = fullProcessObj.total?.data || 0;
        totalProcessDesc.speed = fullProcessObj.total?.speed || 0;
        totalProcessDesc.time = fullProcessObj.total?.time || 0;
      }
    }
    // Full check assignment
    if (subTaskStore.subTaskData?.dataCheckProcess?.execResultDetail) {
      const fullCheckProcessObj = JSON.parse(subTaskStore.subTaskData.dataCheckProcess.execResultDetail)
      if (fullCheckProcessObj) {
        totalCheckDesc.data = fullCheckProcessObj.total || 0;
        totalCheckDesc.speed = fullCheckProcessObj.avgSpeed || 0;
      }
    }
    // Incremental migration assignments
    if (subTaskStore.subTaskData?.incrementalProcess?.execResultDetail) {
      const increProcessObj = JSON.parse(subTaskStore.subTaskData.incrementalProcess.execResultDetail);
      if (increProcessObj) {
        increaseProcessDesc.sourceSpeed = increProcessObj.sourceSpeed || 0;
        increaseProcessDesc.sinkSpeed = increProcessObj.sinkSpeed || 0;
      }
    }
    // Reverse migration is currently empty [it is full migration reverse]
    if (subTaskStore.subTaskData?.reverseProcess?.execResultDetail) {
      const reverseProcessObj = JSON.parse(subTaskStore.subTaskData.reverseProcess.execResultDetail);
      if (reverseProcessObj) {
        reverseProcess.sourceSpeed = reverseProcessObj.sourceSpeed || 0;
        reverseProcess.sinkSpeed = reverseProcessObj.sinkSpeed || 0;
      }
    }
  } catch(err) {
    if (props.subTaskStep && props.subTaskStep !== current.value) {
      current.value = props.subTaskStep
      active.value = props.subTaskStep
    }
  } finally {

  }
}, {deep: true, immediate: true})
</script>
<style lang="less" scoped>
.common-layout {
  padding-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;

  .steps {
    background-color: var(--o-bg-color-light2);
    border-radius: 4px;
    height: 80px;
    box-sizing: border-box;

    .steps-content {
      margin: 19px 88px 18px 24px;
    }
  }

  .process-item {
    flex: 1;
  }

  :deep(.el-tabs--card>.el-tabs__header .el-tabs__item) {
    background-color: var(--o-bg-color-base);
    z-index: 1;
    position: relative;

    &:not(.is-active)::after {
      content: '';
      position: absolute;
      left: 0;
      top: 88%;
      width: 100%;
      height: 4px;
      background-color: var(--o-bg-color-light);
      z-index: 999;          }

    }
}
</style>
