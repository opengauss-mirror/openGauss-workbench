<template>
  <el-dialog v-model="props.errorVisible" draggable :close-on-click-modal="false" @close="closeModal"
    style="width: fit-content;max-width: 1550px;" custom-class="testDialog">
    <template #header>
      <div class="header">{{ $t('components.SubTaskDetail.showDetail') }}</div>
    </template>
    <div class="mainContent loading-area" v-loading="areaLoading">
      <el-row :gutter="20">
        <div class="detailRow">
          <div class="detailLabel">{{ $t('components.SubTaskDetail.alarmLocation') }}</div>
          <div class="desc">{{ locationMap[detailInfo.logSource] }}</div>
        </div>
      </el-row>
      <el-row :gutter="20">
        <div class="detailRow">
          <div class="detailLabel">{{ $t('components.SubTaskDetail.firstAlarmTime') }}</div>
          <div>
            <div class="desc">{{ detailInfo.firstDateTime }}</div>
          </div>
        </div>
      </el-row>
      <el-row :gutter="20">
        <div class="detailRow">
          <div class="detailLabel">{{ $t('components.SubTaskDetail.latestAlarmTime') }}</div>
          <div>
            <div class="desc">{{ detailInfo.latestDateTime }}</div>
          </div>
        </div>
      </el-row>
      <el-row :gutter="20">
        <div class="detailRow">
          <div class="detailLabel">{{ $t('components.SubTaskDetail.alarmReason') }}</div>
          <div :class="hasNewLine(detailInfo.detail) ? 'newLine' : 'desc'">{{ detailInfo.detail }}</div>
        </div>
      </el-row>
    </div>
    <template #footer>
      <div>
        <el-button @click="closeModal" type="primary">{{ $t('components.SubTaskDetail.confirm') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { toRefs, computed, onBeforeUnmount, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const props = defineProps({
  errorVisible: {
    type: Boolean,
    require: true,
    default: false
  },
  areaLoading: {
    type: Boolean,
    require: true
  },
  detailInfo: {
    type: Object,
    require: true
  }
})

const hasNewLine = (text) => {
  return text?.includes('\n')
}
const locationMap = computed(() => {
  return {
    0: t('components.SubTaskDetail.portal', { id: detailInfo.value.taskId }),
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
const { detailInfo, areaLoading } = toRefs(props)
const emits = defineEmits(['update:errorVisible'])
const closeModal = () => {
  emits('update:errorVisible', false)
}
</script>

<style lang="less" scoped>
:deep(.el-loading-mask) {
  --o-loading-mask-color: transparent;
}

.header {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.mainContent {
  width: fit-content;
  max-width: 1500px;
  height: 280px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  .detailRow {
    display: flex;

    .detailLabel {
      padding-right: 32px;
      min-width: 120px
    }

    .desc {
      color: #1d2129;
      width: 400px;
    }

    .newLine {
      white-space: pre-wrap;
      color: #1d2129;
      display: inline-block;
    }
  }

}</style>
