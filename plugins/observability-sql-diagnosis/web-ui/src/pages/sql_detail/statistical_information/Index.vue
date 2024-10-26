<script setup lang="ts">
import { consumingOption } from '@/pages/sql_detail/statistical_information/common'

const props = withDefaults(
  defineProps<{
    dbid: string | string[]
    sqlId: string | string[]
    data: any
    large?: boolean
  }>(),
  {
    data: {},
    dbid: '',
    sqlId: '',
    large: false,
  }
)
const requestData = reactive<{
  sqlText: string
  statisticalInfo: Record<string, string>
  fixedRangeTime: Array<string>
}>({
  sqlText: '',
  statisticalInfo: {},
  fixedRangeTime: [],
})

const data = reactive<{
  useTimeStatistical: Array<{ name: string; value: number; color?: string }>
  consumingPieData: Array<{ name: string; value: number }>
  consumingPieColor: Array<string>
}>({
  useTimeStatistical: [],
  consumingPieData: [],
  consumingPieColor: [],
})

const getValueForTime = (value: string) => {
  if (value === undefined) {
    return '-'
  }
  return value
}
const getValue = (key: string, curData: Record<string, string>) => {
  if (curData != null) {
    if (typeof curData[key] === 'object') return curData[key].value
    else return curData[key] || '-'
  }
  return '-'
}

watch(
  () => requestData.statisticalInfo,
  (res) => {
    // eslint-disable-next-line camelcase
    const { cpu_time, wait_time, db_time } = res
    try {
      const cpuTimeDb =
        db_time && db_time !== '0' && cpu_time && cpu_time !== '0'
          ? (Number.parseInt(cpu_time) / Number.parseInt(db_time)) * 100
          : 0
      const waitTimeDb =
        db_time && db_time !== '0' && wait_time && wait_time !== '0'
          ? (Number.parseInt(wait_time) / Number.parseInt(db_time)) * 100
          : 0
      data.useTimeStatistical = [
        {
          name: 'waitTimeLabel',
          value: waitTimeDb,
          color: '#3DC94C',
        },
        {
          name: 'cpuTimeLabel',
          value: cpuTimeDb,
          color: '#DA864C',
        },
      ]
      consumingOption.forEach((item) => {
        const value = Number.parseInt(getValue(item.value, requestData.statisticalInfo))
        if (value > 0) {
          data.consumingPieColor.push(item.color as string)
          data.consumingPieData.push({
            name: item.label,
            value: Number.parseInt(getValue(item.value, requestData.statisticalInfo)),
          })
        }
      })
    } catch (e) {
      data.useTimeStatistical = []
    }
  },
  { deep: true }
)
// ctl height
const upList = computed(() => {
  let result = []
  for (let index = 0; index < consumingOption.length; index++) {
    if (index % 2 === 0) result.push(consumingOption[index])
  }
  return result
})
const downList = computed(() => {
  let result = []
  for (let index = 0; index < consumingOption.length; index++) {
    if (index % 2 === 1) result.push(consumingOption[index])
  }
  return result
})

onMounted(() => {
  let res = props.data
  if (Object.keys(res).length === 0) return
  if (res != null) {
    requestData.statisticalInfo = res
    requestData.sqlText = res.query
    requestData.fixedRangeTime = [res.start_time, res.finish_time]
  }
})
</script>

<template>
  <div class="statistical-information" id="statisticalInformation">
    <div class="s-i-row">
      <div class="s-i-col-right" style="margin-bottom: 20px">
        <my-card class="my-card" :title="$t('sql.consumingBreakdownTitle')" :bodyPadding="false">
          <div class="s-i-consuming">
            <div class="s-i-consuming-pie">
              <my-pie
                :showLegend="false"
                :center="['50%', '50%']"
                :data="data.useTimeStatistical"
                :color="['#3DC94C', '#DA864C']"
              />
            </div>
            <div class="s-i-consuming-legend" v-if="large">
              <div style="width: 43%" v-if="data.useTimeStatistical && data.useTimeStatistical.length > 0">
                <div
                  class="s-i-consuming-item large"
                  style="width: 100%"
                  v-for="item in data.useTimeStatistical"
                  :key="item.value"
                >
                  <span :style="`background-color: ${item.color}`" class="s-i-consuming-item-block"></span>
                  <span class="s-i-consuming-item-label">{{ $t(`sql.${item.name}`) }}：</span>
                  <span class="s-i-consuming-item-value">{{ `${item.value.toFixed(2)}%` }}</span>
                </div>
              </div>
              <div style="width: 57%">
                <div class="s-i-consuming-item large" style="width: 100%">
                  <span class="s-i-consuming-item-label">{{ $t('sql.dbTimeLabel') }}：</span>
                  <span class="s-i-consuming-item-value">{{
                    `${getValueForTime(requestData.statisticalInfo['db_time'])} ms`
                  }}</span>
                </div>
                <div class="s-i-consuming-item large" style="width: 100%">
                  <span class="s-i-consuming-item-label">{{ $t('sql.cpuTimeLabel') }}：</span>
                  <span class="s-i-consuming-item-value">{{
                    `${getValueForTime(requestData.statisticalInfo['cpu_time'])} ms`
                  }}</span>
                </div>
                <div class="s-i-consuming-item large" style="width: 100%">
                  <span class="s-i-consuming-item-label">{{ $t('sql.waitTimeLabel') }}：</span>
                  <span class="s-i-consuming-item-value">{{
                    `${getValueForTime(requestData.statisticalInfo['wait_time'])} ms`
                  }}</span>
                </div>
              </div>
            </div>
            <div class="s-i-consuming-legend" v-else>
              <div class="s-i-consuming-item" v-for="item in data.useTimeStatistical" :key="item.name">
                <span :style="`background-color: ${item.color}`" class="s-i-consuming-item-block"></span>
                <span class="s-i-consuming-item-label">{{ $t(`sql.${item.name}`) }}：</span>
                <span class="s-i-consuming-item-value">{{ `${item.value.toFixed(2)}%` }}</span>
              </div>
              <div class="s-i-consuming-item">
                <span class="s-i-consuming-item-label">{{ $t('sql.dbTimeLabel') }}：</span>
                <span class="s-i-consuming-item-value">{{
                  `${getValueForTime(requestData.statisticalInfo['db_time'])} ms`
                }}</span>
              </div>
              <div class="s-i-consuming-item">
                <span class="s-i-consuming-item-label">{{ $t('sql.cpuTimeLabel') }}：</span>
                <span class="s-i-consuming-item-value">{{
                  `${getValueForTime(requestData.statisticalInfo['cpu_time'])} ms`
                }}</span>
              </div>
              <div class="s-i-consuming-item">
                <span class="s-i-consuming-item-label">{{ $t('sql.waitTimeLabel') }}：</span>
                <span class="s-i-consuming-item-value">{{
                  `${getValueForTime(requestData.statisticalInfo['wait_time'])} ms`
                }}</span>
              </div>
            </div>
          </div>
        </my-card>
      </div>
      <div class="s-i-col-right">
        <my-card class="my-card" :title="$t('sql.consumingBreakdownTitle')" :bodyPadding="false">
          <div class="s-i-consuming">
            <div class="s-i-consuming-pie">
              <my-pie
                :showLegend="false"
                :center="['50%', '50%']"
                :data="data.consumingPieData"
                :color="data.consumingPieColor"
              />
            </div>
            <div class="s-i-consuming-legend" v-if="large">
              <div class="s-i-consuming-item large" v-for="item in consumingOption" :key="item.value">
                <span :style="`background-color: ${item.color}`" class="s-i-consuming-item-block"></span>
                <span class="s-i-consuming-item-label">{{ $t(`sql.consumingOption.${item.label}`) }}：</span>
                <span class="s-i-consuming-item-value">{{
                  `${getValue(item.value, requestData.statisticalInfo)} ms`
                }}</span>
              </div>
            </div>
            <div class="s-i-consuming-legend" v-else>
              <div class="s-i-consuming-item" v-for="item in upList" :key="item.value">
                <span :style="`background-color: ${item.color}`" class="s-i-consuming-item-block"></span>
                <span class="s-i-consuming-item-label">{{ $t(`sql.consumingOption.${item.label}`) }}：</span>
                <span class="s-i-consuming-item-value">{{
                  `${getValue(item.value, requestData.statisticalInfo)} ms`
                }}</span>
              </div>
              <div class="s-i-consuming-item" v-for="item in downList" :key="item.value">
                <span :style="`background-color: ${item.color}`" class="s-i-consuming-item-block"></span>
                <span class="s-i-consuming-item-label">{{ $t(`sql.consumingOption.${item.label}`) }}：</span>
                <span class="s-i-consuming-item-value">{{
                  `${getValue(item.value, requestData.statisticalInfo)} ms`
                }}</span>
              </div>
            </div>
          </div>
        </my-card>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.statistical-information {
  font-size: 14px;
  // border-top: 1px solid $og-border-color;

  .s-i-row {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    margin-bottom: 18px;
  }

  .s-i-col-left {
    width: 100%;
    margin-bottom: 20px;
    .my-card {
      height: 250px;
    }
  }

  .s-i-col-right {
    width: 100%;
    .my-card {
      height: 300px;
    }
  }

  .s-i-base {
    height: inherit;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 18px 40px;
    overflow-y: auto;

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
  }

  .s-i-execute {
    height: inherit;
    margin-top: 5px;
    border-top: 1px solid #4a4a4a;
    overflow-y: scroll;
    .s-i-el-col {
      background-color: var(--el-bg-color-sub);
    }

    &:deep(.el-row) {
      line-height: 39px;
      border: 1px solid #4a4a4a;
      border-top: none;
    }

    &:deep(.el-row:first-child) {
      border-right: 1px solid #4a4a4a;
    }

    &:deep(.el-col) {
      padding-left: 5px;
      // border-top: 1px solid #4A4A4A;
    }
  }

  .s-i-statistics {
    height: 200px;
    display: flex;
    align-items: center;
    overflow-y: auto;

    &-box {
      display: flex;
      width: 80%;
    }

    &-pie {
      height: 180px;
      width: 45%;
    }

    &-legend {
      width: calc(100% - 120px);
      // height: 220px;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    &-item {
      font-size: 12px;
      display: flex;
      align-items: center;
      margin-bottom: 10px;

      &-block {
        display: inline-block;
        width: 10px;
        height: 10px;
        margin-right: 3px;
      }
    }

    &-list {
      font-size: 12px;
      width: 45%;
    }
  }

  .s-i-consuming {
    height: 250px;
    display: flex;
    align-items: center;
    padding: 0 0;
    overflow-y: auto;

    &-pie {
      height: 180px;
      width: 45%;
    }

    &-legend {
      width: calc(100% - 120px);
      display: flex;
      flex-wrap: wrap;
      padding-left: 10px;
    }

    &-item {
      font-size: 12px;
      display: flex;
      align-items: center;

      &-block {
        display: inline-block;
        width: 10px;
        height: 10px;
        margin-right: 3px;
      }
    }

    &-item.large {
      &:nth-child(1n) {
        width: 43%;
      }

      &:nth-child(2n) {
        width: 57%;
      }
    }

    &-item:not(.large) {
      width: 100%;
      .s-i-consuming-label {
        width: 70%;
      }
      .s-i-consuming-value {
        width: 30%;
        padding-right: 10px;
      }
    }
  }
}
</style>
