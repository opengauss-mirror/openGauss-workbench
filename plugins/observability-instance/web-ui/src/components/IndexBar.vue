<template>
  <div class="refresh-bar-container">
    <div class="refresh-bar-filter">
      <div style="white-space: nowrap">{{ $t("app.autoRefresh") }}:</div>
      <el-select v-model="autoRefreshTime" style="width: 130px; margin: 0 4px">
        <el-option :value="99999999" label="NO-AUTO" />
        <el-option :value="1" label="1s" />
        <el-option :value="15" label="15s" />
        <el-option :value="30" label="30s" />
        <el-option :value="60" label="60s" />
      </el-select>
      <el-button
        class="refresh-button"
        type="primary"
        :icon="Refresh"
        style="padding: 8px"
        @click="autoRefreshFn"
      />
      <div class="divider"></div>
      <span style="white-space: nowrap">{{ $t("dashboard.range") }}:</span>
      <el-select
        @change="selectType"
        v-model="timeType"
        :style="{ width: i18n.global.locale.value === 'en' ? '115px' : '125px' }"
      >
        <el-option :value="timeTypeSelection.HOUR1" :label="$t('dashboard.last1H')" />
        <el-option :value="timeTypeSelection.HOUR12" :label="$t('dashboard.last12H')" />
        <el-option :value="timeTypeSelection.DAY1" :label="$t('dashboard.last1D')" />
        <el-option :value="timeTypeSelection.DAY2" :label="$t('dashboard.last2D')" />
        <el-option :value="timeTypeSelection.DAY7" :label="$t('dashboard.last7D')" />
        <el-option :value="timeTypeSelection.CUSTOM" :label="$t('app.custom')" />
      </el-select>
      <MyDatePicker
        v-model="timeRange"
        :disabled="timeType !== timeTypeSelection.CUSTOM"
        type="datetimerange"
        :valueFormatToUTC="true"
        @change="selectDate"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Refresh } from "@element-plus/icons-vue";
import { i18n } from "@/i18n";
import { useMonitorStore } from "@/store/monitor";
import { storeToRefs } from "pinia";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import timezone from "dayjs/plugin/timezone";

dayjs.extend(utc);
dayjs.extend(timezone);

const props = withDefaults(defineProps<{ tabId: string }>(), {});

const {
  sourceType,
  autoRefreshTime,
  timeType,
  timeTypeSelection,
  timeRange,
} = storeToRefs(useMonitorStore(props.tabId));

watch(autoRefreshTime, () => {
  useMonitorStore(props.tabId).increaseCounter(sourceType.value.AUTOREFRESHTIME);
});

const autoRefreshFn = () => {
  useMonitorStore(props.tabId).increaseCounter(sourceType.value.MANUALREFRESH);
};

const selectDate = () => {
  useMonitorStore(props.tabId).increaseCounter(sourceType.value.TIMERANGE);
};
const selectType = (val: string) => {
  if (val === timeTypeSelection.value.CUSTOM) {
    const end = new Date();
    const start = new Date();
    start.setTime(start.getTime() - 3600 * 1000 * 1);
    timeRange.value = [dayjs(start).utc().format(), dayjs(end).utc().format()];
    useMonitorStore(props.tabId).increaseCounter(sourceType.value.TIMERANGE);
  } else {
    timeRange.value = [];
    useMonitorStore(props.tabId).increaseCounter(sourceType.value.TIMETYPE);
  }
};
</script>

<style lang="scss" scoped>
.refresh-bar-filter {
  width: 72%;
}
.filter {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}
</style>
