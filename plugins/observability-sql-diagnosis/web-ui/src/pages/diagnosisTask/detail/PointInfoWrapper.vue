<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <el-tab-pane :label="$t('historyDiagnosis.result')" :name="1">
            <div>
                <div class="suggest-content" v-if="props.pointData?.pointState === 'NOT_ANALYZED'">
                    <svg-icon name="suggest" class="icon" />
                    <div>{{t('historyDiagnosis.notAnalyze')}}</div>
                </div>

                <div class="suggest-content abnormal" v-else-if="props.pointData?.pointState === 'ABNORMAL'">
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
import { i18n } from '@/i18n'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const tab = ref(1)
const props = withDefaults(
    defineProps<{
        pointData: PointInfo | null
        taskId: string
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
