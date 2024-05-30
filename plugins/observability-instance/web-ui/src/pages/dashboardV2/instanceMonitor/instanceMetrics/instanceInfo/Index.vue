<template>
  <el-row :gutter="12">
    <el-col :span="24">
      <my-card :title="$t('instanceIndex.cacheHit')" height="300" :bodyPadding="false" style="position: relative; overflow: auto">
        <el-table
          :data="metricsData.cacheHitTable"
          style="width: 100%;height: 260px"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
        >
          <el-table-column prop="datname" :label="$t('instanceIndex.datname')" />
          <el-table-column prop="hit" :label="$t('instanceIndex.hit')" />
          <el-table-column prop="resettime" :label="$t('instanceIndex.resettime')" />
        </el-table>
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <el-row :gutter="12">
    <el-col :span="12">
      <my-card
        :title="$t('instanceMonitor.instance.connectionQty')"
        height="300"
        :bodyPadding="false"
        :showBtns="true" @download="title => download(title,connectionQty)"
        :info="connectionQtyInfo"
      >
        <LazyLine
          ref="connectionQty"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.connection"
          :xData="metricsData.time"
        />
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card
        :title="$t('instanceMonitor.instance.slowSQL3s') + metricsData.threshold + ')'"
        height="300"
        :bodyPadding="false"
        :showBtns="true" @download="title => download(title,slowSQL3s)"
        :info="slowSQL3sInfo"
      >
        <LazyLine
          ref="slowSQL3s"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.slowSQL"
          :xData="metricsData.time"
        />
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <el-row :gutter="12">
    <el-col :span="12">
      <my-card :title="$t('instanceIndex.respTime')" height="300" :bodyPadding="false" :info="respTimeInfo" :showBtns="true" @download="title => download(title,respTime)">
        <LazyLine
          ref="respTime"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.respTime"
          :xData="metricsData.time"
        />
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card
        :title="$t('instanceIndex.databaseBlk')"
        height="300"
        :bodyPadding="false"
        :showBtns="true" @download="title => download(title,databaseBlk)"
        :info="databaseBlkInfo"
      >
        <LazyLine
          ref="databaseBlk"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.databaseBlk"
          :xData="metricsData.time"
        />
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
import { getInstanceInfo } from "@/api/observability";
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
  respTime: LineData[];
  cacheHitTable: any[];
  connection: LineData[];
  slowSQL: LineData[];
  databaseBlk: LineData[];
  time: string[];
  threshold: string;
}
const metricsData = ref<MetricsData>({
  respTime: [],
  cacheHitTable: [],
  connection: [],
  slowSQL: [],
  databaseBlk: [],
  time: [],
  threshold: ''
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
    if (tabNow.value === tabKeys.InstanceMonitorInstanceInfo) {
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
const { data: indexData, run: requestData } = useRequest(getInstanceInfo, {
  manual: true,
});
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.respTime = [];
    metricsData.value.cacheHitTable = [];
    metricsData.value.connection = [];
    metricsData.value.databaseBlk = [];
    metricsData.value.slowSQL = [];
    metricsData.value.threshold = '';

    const baseData = indexData.value;
    if (!baseData) return;

    // respTime
    respTimeInfo.value.option = []
    if (baseData.INSTANCE_DB_RESPONSETIME_P80) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_RESPONSETIME_P80.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.respTime.push({
        data: tempData,
        name: 'p80',
      });
      respTimeInfo.value.option.push({ name: 'p80', value: t('instanceIndex.p80Content') })
    }
    if (baseData.INSTANCE_DB_RESPONSETIME_P95) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_RESPONSETIME_P95.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.respTime.push({
        data: tempData,
        name: 'p95',
      });
      respTimeInfo.value.option.push({ name: 'p95', value: t('instanceIndex.p95Content') })
    }
    // cacheHitTable
    if (baseData.cacheHit) {
      baseData.cacheHit.forEach(item => {
        item.hit = toFixed(item.hit)
      })
      metricsData.value.cacheHitTable = baseData.cacheHit
    }

    connectionQtyInfo.value.option = []
    // connections
    if (baseData.INSTANCE_DB_CONNECTION_ACTIVE) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_CONNECTION_ACTIVE.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.connection.push({
        data: tempData,
        areaStyle: {},
        stack: "Total",
        name: t("instanceMonitor.instance.idleConnectionQty"),
      });
      connectionQtyInfo.value.option.push({ name: t("instanceMonitor.instance.idleConnectionQty"), value: t("instanceMonitor.instance.idleConnectionQtyContent") })
    }
    if (baseData.INSTANCE_DB_CONNECTION_IDLE) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_CONNECTION_IDLE.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.connection.push({
        data: tempData,
        areaStyle: {},
        stack: "Total",
        name: t("instanceMonitor.instance.activeConnectionQty"),
      });
      connectionQtyInfo.value.option.push({ name: t("instanceMonitor.instance.activeConnectionQty"), value: t("instanceMonitor.instance.activeConnectionQtyContent") })
    }
    if (baseData.INSTANCE_DB_CONNECTION_CURR) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_CONNECTION_CURR.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.connection.push({
        data: tempData,
        name: t("instanceMonitor.instance.connectionQtyNow"),
      });
      connectionQtyInfo.value.option.push({ name: t("instanceMonitor.instance.connectionQtyNow"), value: t("instanceMonitor.instance.connectionQtyNowContent") })
    }
    if (baseData.INSTANCE_DB_CONNECTION_TOTAL) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_CONNECTION_TOTAL.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.connection.push({
        data: tempData,
        name: t("instanceMonitor.instance.maxConnectionQty"),
      });
      connectionQtyInfo.value.option.push({ name: t("instanceMonitor.instance.maxConnectionQty"), value: t("instanceMonitor.instance.maxConnectionQtyContent") })
    }

    // slow SQL
    slowSQL3sInfo.value.option = []
    if (baseData.INSTANCE_DB_SLOWSQL) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_SLOWSQL.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.slowSQL.push({
        data: tempData,
        name: t("instanceMonitor.instance.slowSQLQty"),
      });
      slowSQL3sInfo.value.option.push({ name: t("instanceMonitor.instance.slowSQLQty"), value: t("instanceMonitor.instance.slowSQLQtyContent") })
    }

    // databaseBlk
    for (let key in baseData.INSTANCE_DB_DATABASE_BLK) {
      let tempData: string[] = []
      baseData.INSTANCE_DB_DATABASE_BLK[key]?.forEach((element) => {
        tempData.push(toFixed(element))
      })
      metricsData.value.databaseBlk.push({ data: tempData, name: key })
    }

    //threshold
    metricsData.value.threshold=baseData.slowSqlThreshold

    // time
    metricsData.value.time = baseData.time;
  },
  { deep: true }
);
   
const connectionQty = ref();
const slowSQL3s = ref();
const respTime = ref();
const databaseBlk = ref();
const download = (title: string, ref: any) => {
  ref.download(title)
}

const connectionQtyInfo = ref<any>({
  title: t("app.lineOverview"),
  option: []
})
const slowSQL3sInfo = ref<any>({
  title: t("app.lineOverview"),
  option: []
})
const respTimeInfo = ref<any>({
  title: t("app.lineOverview"),
  option: []
})
const databaseBlkInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('instanceIndex.databaseBlk'), value: t('instanceIndex.databaseBlkContent') }
  ]
})
</script>

<style scoped lang="scss"></style>
