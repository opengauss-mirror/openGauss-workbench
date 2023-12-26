<template>
  <el-row :gutter="12">
    <el-col :span="12">
      <my-card :title="$t('instanceIndex.tablespaceInfo')" height="300" :bodyPadding="false" style="position: relative; overflow: auto">
        <el-table
          :data="metricsData.tablespaceInfo"
          style="width: 100%"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
        >
          <el-table-column prop="spcname" :label="$t('instanceIndex.spcname')" />
          <el-table-column prop="owner" :label="$t('instanceIndex.owner')" />
          <el-table-column prop="spcoptions" :label="$t('instanceIndex.spcoptions')" />
        </el-table>
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card :title="$t('instanceIndex.tablespaceSize')" height="300" :info="tablespaceSizeInfo" :bodyPadding="false" style="position: relative; overflow: auto" :showBtns="true" @download="title => download(title,tablespaceSize)">
        <LazyLine
          ref="tablespaceSize"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.tablespaceSize"
          :xData="metricsData.time"
        />
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <el-row :gutter="12">
    <el-col :span="12">
      <my-card
        :title="$t('instanceIndex.tablesTop10')"
        height="300"
        :bodyPadding="false"
        style="position: relative; overflow: auto"
      >
      <el-table
          :data="metricsData.tablesTop10"
          style="width: 100%"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
        >
          <el-table-column prop="datname" :label="$t('instanceIndex.datname')" />
          <el-table-column prop="schemaname" :label="$t('instanceIndex.schemaname')" />
          <el-table-column prop="tablename" :label="$t('instanceIndex.tablename')" />
          <el-table-column prop="tablesize" :label="$t('instanceIndex.tablesize')" />
        </el-table>
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card
        :title="$t('instanceIndex.indexsTop10')"
        height="300"
        :bodyPadding="false"
        style="position: relative; overflow: auto"
      >
      <el-table
          :data="metricsData.indexsTop10"
          style="width: 100%"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
        >
          <el-table-column prop="datname" :label="$t('instanceIndex.datname')" />
          <el-table-column prop="schemaname" :label="$t('instanceIndex.schemaname')" />
          <el-table-column prop="tablename" :label="$t('instanceIndex.tablename')" />
          <el-table-column prop="indexname" :label="$t('instanceIndex.indexname')" />
          <el-table-column prop="indexsize" :label="$t('instanceIndex.indexsize')" />
        </el-table>
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <el-row :gutter="12">
    <el-col :span="12">
      <my-card
        :title="$t('instanceIndex.deadTableTop10')"
        height="300"
        :bodyPadding="false"
        style="position: relative; overflow: auto"
      >
      <el-table
          :data="metricsData.deadTableTop10"
          style="width: 100%"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
        >
        <el-table-column prop="datname" :label="$t('instanceIndex.datname')" />
        <el-table-column prop="schemaname" :label="$t('instanceIndex.schemaname')" />
        <el-table-column prop="tablename" :label="$t('instanceIndex.tablename')" />
        <el-table-column prop="n_dead_tup" :label="$t('instanceIndex.deadTup')" />
        </el-table>
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card
        :title="$t('instanceIndex.vacuumTop10')"
        height="300"
        :bodyPadding="false"
        style="position: relative; overflow: auto"
      >
      <el-table
          :data="metricsData.vacuumTop10"
          style="width: 100%"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
        >
        <el-table-column prop="datname" :label="$t('instanceIndex.datname')" />
        <el-table-column prop="schemaname" :label="$t('instanceIndex.schemaname')" />
        <el-table-column prop="tablename" :label="$t('instanceIndex.tablename')" />
        </el-table>
      </my-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import LazyLine from "@/components/echarts/LazyLine.vue";
import { useMonitorStore } from "@/store/monitor";
import { toFixed } from "@/shared";
import { storeToRefs } from "pinia";
import { getInstanceTablespace } from "@/api/observability";
import { useIntervalTime } from "@/hooks/time";
import { tabKeys } from "@/pages/dashboardV2/common";
import { useRequest } from "vue-request";
const { t } = useI18n();

const props = withDefaults(defineProps<{ tabId: string }>(), {});

interface LineData {
  name: string;
  data: any[];
  [other: string]: any;
}
interface MetricsData {
  tablespaceInfos: any[];
  tablespaceSize: LineData[];
  tablesTop10: any[];
  indexsTop10: any[];
  deadTableTop10: any[];
  vacuumTop10: any[];
  time: string[];
}
const metricsData = ref<MetricsData>({
  tablespaceInfos: [],
  tablespaceSize: [],
  tablesTop10: [],
  indexsTop10: [],
  deadTableTop10: [],
  vacuumTop10: [],
  time: [],
});
const {
  updateCounter,
  sourceType,
  autoRefreshTime,
  tabNow,
  instanceId,
} = storeToRefs(useMonitorStore(props.tabId));

// same for every page in index
const timer = ref<number>();
onMounted(() => {
  load();
});
watch(
  updateCounter,
  () => {
    clearInterval(timer.value);
    if (tabNow.value === tabKeys.InstanceMonitorInstanceTablespace) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) {
        load();
      }
      if (updateCounter.value.source === sourceType.value.MANUALREFRESH) load();
      if (updateCounter.value.source === sourceType.value.TIMETYPE) load();
      if (updateCounter.value.source === sourceType.value.TIMERANGE) load();
      if (updateCounter.value.source === sourceType.value.TABCHANGE) load();
      const time = autoRefreshTime.value;
      timer.value = useIntervalTime(
        () => {
          load();
        },
        computed(() => time * 1000)
      );
    }
  },
  { immediate: false }
);

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
  if (!instanceId.value) return;
  requestData(props.tabId);
};
const { data: indexData, run: requestData } = useRequest(getInstanceTablespace, {
  manual: true,
});
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.tablespaceInfo = [];
    metricsData.value.tablespaceSize = [];
    metricsData.value.tablesTop10 = [];
    metricsData.value.indexsTop10 = [];
    metricsData.value.deadTableTop10 = [];
    metricsData.value.vacuumTop10 = [];

    const baseData = indexData.value;
    if (!baseData) return;

    // tablespaceInfo
    if (baseData.tablespaceInfo) {
      metricsData.value.tablespaceInfo = baseData.tablespaceInfo
    }

    // tablespaceSize
    for (let key in baseData.PG_TABLESPACE_SIZE) {
      let tempData: string[] = []
      baseData.PG_TABLESPACE_SIZE[key]?.forEach((element) => {
        tempData.push(toFixed(element))
      })
      metricsData.value.tablespaceSize.push({ data: tempData, name: key })
    }

    // tablesTop10
    if (baseData.tablesTop10) {
      metricsData.value.tablesTop10 = baseData.tablesTop10
    }

    // indexsTop10
    if (baseData.indexsTop10) {
      metricsData.value.indexsTop10 = baseData.indexsTop10
    }

    // deadTableTop10
    if (baseData.deadTableTop10) {
      metricsData.value.deadTableTop10 = baseData.deadTableTop10
    }

    // vacuumTop10
    if (baseData.vacuumTop10) {
      metricsData.value.vacuumTop10 = baseData.vacuumTop10
    }

    // time
    metricsData.value.time = baseData.time;
  },
  { deep: true }
);

const tablespaceSize = ref();
const download = (title: string, ref: any) => {
  ref.download(title)
}

const tablespaceSizeInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('instanceIndex.tablespaceSize'), value: t('instanceIndex.tablespaceSizeContent') }
  ]
})
</script>

<style scoped lang="scss"></style>
