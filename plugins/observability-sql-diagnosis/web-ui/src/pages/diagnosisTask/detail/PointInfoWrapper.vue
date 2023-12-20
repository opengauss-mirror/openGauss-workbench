<template>
  <el-tabs
    v-model="tab"
    class="tast-detail-tabs"
    v-if="props.pointData?.pointType === 'CENTER' && !props.pointData?.pointData"
  >
    <el-tab-pane :label="$t('historyDiagnosis.explanation')" :name="1">
      <div class="explanation">{{ props.pointData?.pointDetail }}</div>
    </el-tab-pane>
  </el-tabs>
  <el-tabs v-model="tab" class="tast-detail-tabs" v-else>
    <el-tab-pane :label="$t('historyDiagnosis.result')" :name="1">
      <div>
        <div class="suggest-content" v-if="props.pointData?.pointState === 'NOT_MATCH_OPTION'">
          <svg-icon name="suggest" class="icon" />
          <div>{{ t('historyDiagnosis.notAnalyze') }}</div>
        </div>

        <div class="suggest-content abnormal" v-else-if="props.pointData?.pointState === 'INITIALIZE'">
          <svg-icon name="suggest_abnormal" class="icon" />
          <div>{{ props.pointData?.pointSuggestion }}</div>
        </div>
        <div class="suggest-content abnormal" v-else-if="props.pointData?.pointState === 'COLLECT_EXCEPTION'">
          <svg-icon name="suggest_abnormal" class="icon" />
          <div>{{ props.pointData?.pointSuggestion }}</div>
        </div>
        <div class="suggest-content abnormal" v-else-if="props.pointData?.pointState === 'ANALYSIS_EXCEPTION'">
          <svg-icon name="suggest_abnormal" class="icon" />
          <div>{{ props.pointData?.pointSuggestion }}</div>
        </div>
        <div class="suggest-content abnormal" v-else-if="props.pointData?.pointState === 'NOT_SATISFIED_DIAGNOSIS'">
          <svg-icon name="suggest_abnormal" class="icon" />
          <div>{{ props.pointData?.pointSuggestion }}</div>
        </div>
        <div class="suggest-content abnormal" v-else-if="props.pointData?.pointState === 'NOT_HAVE_DATA'">
          <svg-icon name="suggest_abnormal" class="icon" />
          <div>{{ props.pointData?.pointSuggestion }}</div>
        </div>

        <div v-else>
          <div class="suggest-content suggestions" v-if="props.pointData?.isHint === 'SUGGESTIONS'">
            <svg-icon name="suggest_suggestions" class="icon" />
            <div>{{ props.pointData?.pointSuggestion }}</div>
          </div>
          <div class="suggest-content" v-else>
            <svg-icon name="suggest" class="icon" />
            <div>{{ props.pointData?.pointSuggestion }}</div>
          </div>
          <slot></slot>
        </div>
      </div>
    </el-tab-pane>
    <el-tab-pane :label="$t('historyDiagnosis.explanation')" :name="2">
      <div class="explanation">{{ props.pointData?.pointDetail }}</div>
    </el-tab-pane>
  </el-tabs>
</template>

<script lang="ts" setup>
import { PointInfo } from '@/api/historyDiagnosis'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const tab = ref(1)
const props = withDefaults(
  defineProps<{
    pointData: PointInfo | null
    taskId: string | null
  }>(),
  {
    pointData: null,
    taskId: '',
  }
)

onMounted(() => {})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
