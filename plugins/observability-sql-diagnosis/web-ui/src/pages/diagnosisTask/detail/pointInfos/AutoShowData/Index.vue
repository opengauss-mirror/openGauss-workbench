<template>
  <el-tabs v-model="tab" class="tast-detail-tabs">
    <point-info-wrapper :point-data="pointInfo">
      <div v-for="row in pointInfo?.pointData?.data" :key="pointInfo?.pointData?.data.indexOf(row)">
        <!-- Table -->
        <my-card
          v-if="row.type == 'TABLE'"
          :title="row.dataName"
          :height="row.height ? row.height + 40 : 500"
          :bodyPadding="false"
          style="margin-top: 10px"
        >
          <el-table
            :data="row.data"
            style="width: 100%"
            :style="{ height: row.height ? row.height + 'px' : '460px' }"
            :border="true"
            :header-cell-class-name="
              () => {
                return 'grid-header'
              }
            "
          >
            <el-table-column
              v-for="column in row.columns"
              :label="column.name"
              :key="column.key"
              :prop="column.key"
              :width="column.width"
            ></el-table-column>
          </el-table>
        </my-card>
        <!-- Line -->
        <my-card
          v-if="row.type == 'LINE_CHART'"
          :title="row.dataName"
          :height="row.height ? row.height + 40 : 350"
          :bodyPadding="false"
          style="margin-top: 10px"
        >
          <LazyLine
            :formatter="toFixed"
            :data="row.series"
            :xData="row.xData"
            :unit="row.unit"
            :max="row.max"
            :min="row.min"
            :interval="row.interval"
          />
        </my-card>
        <!-- PARAM -->
        <my-card
          v-if="row.type == 'PARAM'"
          :title="$t('datasource.editSuggestion')"
          :height="row.height ? row.height + 40 : 400"
          :bodyPadding="false"
          style="margin-top: 10px"
        >
          <div style="padding: 0 10px">
            <p class="s-i-base-item title">{{ $t('datasource.paramName') }}</p>
            <p class="s-i-base-item content">{{ row.data.paramName }}</p>
            <p class="s-i-base-item title">{{ $t('datasource.currentValue') }}</p>
            <p class="s-i-base-item content">
              {{ row.data.currentValue }}{{ row.data.unit === null ? '' : '(' + row.data.unit + ')' }}
            </p>
            <p class="s-i-base-item title">{{ $t('datasource.paramDescription') }}</p>
            <p class="s-i-base-item content">{{ row.data.paramDescription }}</p>
            <p class="s-i-base-item title">{{ $t('datasource.suggestValue') }}</p>
            <p class="s-i-base-item content">{{ row.data.suggestValue }}</p>
            <p class="s-i-base-item title">{{ $t('datasource.suggestReason') }}</p>
            <p class="s-i-base-item content">{{ row.data.suggestReason }}</p>
          </div>
        </my-card>
      </div>
    </point-info-wrapper>
  </el-tabs>
</template>

<script lang="ts" setup>
import { PointInfo, getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { toFixed } from '@/shared'
import PointInfoWrapper from '@/pages/diagnosisTask/detail/PointInfoWrapper.vue'
import moment from 'moment'

const props = withDefaults(
  defineProps<{
    nodesType: string
    taskId: string
    diagnosisType: string
  }>(),
  {
    nodesType: '',
    taskId: '',
    diagnosisType: '',
  }
)

const tab = ref(1)
const pointInfo = ref<PointInfo | null>(null)
const defaultData = {}
const pointData = ref<{}>(defaultData)

onMounted(() => {
  requestData()
  window.$wujie?.bus.$on('opengauss-locale-change', (val: string) => {
    nextTick(() => {
      requestData()
    })
  })
})

const { data: res, run: requestData } = useRequest(
  () => {
    return getPointData(props.taskId, props.nodesType, props.diagnosisType)
  },
  { manual: true }
)
watch(res, (res: any) => {
  // clear data
  pointData.value = defaultData

  const baseData = res
  if (!baseData) return

  pointInfo.value = baseData

  console.log('pointInfo.value', pointInfo.value)
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;

.report-header {
  font-size: 20px;
  color: var(--color-text-1);
  font-weight: 700;
  padding: 8px 0 8px 28px;
  border-bottom: 1px solid $og-border-color;
}
.s-i-base {
  height: 100%;
  box-sizing: border-box;
  overflow: scroll;
  &-item {
    margin: 3px 0;
  }

  &:deep(.el-row) {
    line-height: 39px;
    border: 1px solid $og-border-color;
    border-top: none;
    border-right: none;
  }
  &:deep(.el-col) {
    padding-left: 5px;
    border-right: 1px solid $og-border-color;
  }
  .s-i-base-item {
    line-height: 14px;
    margin-top: 12px;
    &.title {
      font-weight: bold;
    }
    &.content {
      margin-top: 3px;
    }
  }
}
.suggsiton-wrap {
  padding: 8px 16px;

  .report-main {
    padding: 8px 16px;
    font-size: 14px;
    color: $og-text-color;
  }
  .report-main-header {
    font-weight: 700;
    margin-bottom: 8px;
  }
  .report-main-suggestion {
    display: flex;
    align-items: center;
    margin: 16px 0 20px 0;
  }
  .report-main-suggestion-text {
    margin-left: 10px;
    font-weight: 400;
    font-size: 14px;
    color: var(--color-text-1);
    display: inline-block;
    padding: 5px 0;
  }
  .report-main-title {
    margin: 8px 0;
  }
}
</style>
