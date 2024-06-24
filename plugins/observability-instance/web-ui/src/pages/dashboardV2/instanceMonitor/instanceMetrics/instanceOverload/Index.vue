<template>
  <!-- <IndexBar :tabId="props.tabId"></IndexBar>
    <div style="margin-bottom: 38px"></div> -->
  <el-row :gutter="12">
    <el-col :span="12">
      <my-card :title="'TPS'" height="300" :bodyPadding="false" :info="tpsInfo" :showBtns="true" @download="title => download(title,TPS)">
        <template #headerExtend>
          <div class="card-links">
            <el-link
              v-if="isManualRangeSelected"
              type="primary"
              @click="gotoSQLDiagnosis()"
            >
              {{ $t("app.diagnosis") }}
            </el-link>
          </div>
        </template>
        <LazyLine
          ref="TPS"
          :tips="$t('instanceIndex.activeSessionQtyTips')"
          :rangeSelect="true"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.tps"
          :xData="metricsData.time"
        />
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card :title="'QPS'" height="300" :bodyPadding="false" :info="qpsInfo" :showBtns="true" @download="title => download(title,QPS)">
        <LazyLine
          ref="QPS"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.qps"
          :xData="metricsData.time"
        />
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

<el-row :gutter="12">
  <el-col :span="8">
    <my-card
      :title="$t('instanceIndex.databaseIns')"
      height="300"
      :bodyPadding="false"
      :showBtns="true" @download="title => download(title,databaseIns)"
      :info="databaseInsInfo"
    >
      <LazyLine
        ref="databaseIns"
        :tabId="props.tabId"
        :formatter="toFixed"
        :data="metricsData.databaseIns"
        :xData="metricsData.time"
		:toolTipsExcludeZero="true"
        :toolTipsSort="'desc'"      />
    </my-card>
  </el-col>
  <el-col :span="8">
    <my-card
      :title="$t('instanceIndex.databaseUpd')"
      height="300"
      :bodyPadding="false"
      :showBtns="true" @download="title => download(title,databaseUpd)"
      :info="databaseUpdInfo"
    >
      <LazyLine
        ref="databaseUpd"
        :tabId="props.tabId"
        :formatter="toFixed"
        :data="metricsData.databaseUpd"
        :xData="metricsData.time"
        :toolTipsExcludeZero="true"
        :toolTipsSort="'desc'"      />
    </my-card>
  </el-col>
  <el-col :span="8">
    <my-card
      :title="$t('instanceIndex.databaseDel')"
      height="300"
      :bodyPadding="false"
      :showBtns="true" @download="title => download(title,databaseDel)"
      :info="databaseDelInfo"
    >
      <LazyLine
        ref="databaseDel"
        :tabId="props.tabId"
        :formatter="toFixed"
        :data="metricsData.databaseDel"
        :xData="metricsData.time"
        :toolTipsExcludeZero="true"
        :toolTipsSort="'desc'"      />
    </my-card>
  </el-col>
</el-row>

<div class="gap-row"></div>

<el-row :gutter="12">
  <el-col :span="8">
    <my-card
      :title="$t('instanceIndex.databaseReturn')"
      height="300"
      :bodyPadding="false"
      :showBtns="true" @download="title => download(title,databaseReturn)"
      :info="databaseReturnInfo"
    >
      <LazyLine
        ref="databaseReturn"
        :tabId="props.tabId"
        :formatter="toFixed"
        :data="metricsData.databaseReturn"
        :xData="metricsData.time"
        :toolTipsExcludeZero="true"
        :toolTipsSort="'desc'"      />
    </my-card>
  </el-col>
  <el-col :span="8">
    <my-card
      :title="$t('instanceIndex.databaseFecth')"
      height="300"
      :bodyPadding="false"
      :showBtns="true" @download="title => download(title,databaseFecth)"
      :info="databaseFecthInfo"
    >
      <LazyLine
        ref="databaseFecth"
        :tabId="props.tabId"
        :formatter="toFixed"
        :data="metricsData.databaseFecth"
        :xData="metricsData.time"
        :toolTipsExcludeZero="true"
        :toolTipsSort="'desc'"      />
    </my-card>
  </el-col>
  <el-col :span="8">
    <my-card :title="$t('instanceIndex.bgwriter')" :info="bgwriterInfo" height="300" :bodyPadding="false" :showBtns="true" @download="title => download(title,bgwriter)">
      <LazyLine
        ref="bgwriter"
        :tabId="props.tabId"
        :formatter="toFixed"
        :data="metricsData.bgwriter"
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
import { getInstanceOverload } from "@/api/observability";
import { useIntervalTime } from "@/hooks/time";
import { tabKeys } from "@/pages/dashboardV2/common";
import { useRequest } from "vue-request";
import { ElMessage } from "element-plus";
import { hasSQLDiagnosisModule } from "@/api/sqlDiagnosis";

const { t } = useI18n();

const props = withDefaults(defineProps<{ tabId: string }>(), {});

interface LineData {
  name: string;
  data: any[];
  [other: string]: any;
}
interface MetricsData {
  tps: LineData[];
  qps: LineData[];
  databaseIns: LineData[];
  databaseUpd: LineData[];
  databaseDel: LineData[];
  databaseReturn: LineData[];
  databaseFecth: LineData[];
  bgwriter: LineData[];
  time: string[];
}
const metricsData = ref<MetricsData>({
  tps: [],
  qps: [],
  databaseIns: [],
  databaseUpd: [],
  databaseDel: [],
  databaseReturn: [],
  databaseFecth: [],
  bgwriter: [],
  time: [],
});
const {
  updateCounter,
  sourceType,
  autoRefreshTime,
  tabNow,
  instanceId,
  isManualRangeSelected,
  timeRange,
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
    if (tabNow.value === tabKeys.InstanceMonitorInstanceOverload) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) {
        clearData();
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

const clearData = () => {
  metricsData.value.tps = [];
  metricsData.value.qps = [];
  metricsData.value.databaseIns = [];
  metricsData.value.databaseUpd = [];
  metricsData.value.databaseDel = [];
  metricsData.value.databaseReturn = [];
  metricsData.value.databaseFecth = [];
  metricsData.value.bgwriter = [];
  metricsData.value.time = [];
}

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
  if (!instanceId.value) return;
  requestData(props.tabId);
};
const { data: indexData, run: requestData } = useRequest(getInstanceOverload, {
  manual: true,
  onError: () => {
    clearData()
  }
});
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.tps = [];
    metricsData.value.qps = [];
    metricsData.value.databaseIns = [];
    metricsData.value.databaseUpd = [];
    metricsData.value.databaseDel = [];
    metricsData.value.databaseReturn = [];
    metricsData.value.databaseFecth = [];
    metricsData.value.bgwriter = [];

    const baseData = indexData.value;
    if (!baseData) return;

    // TPS
    if (baseData.INSTANCE_TPS_ROLLBACK) {
      let tempData: string[] = [];
      baseData.INSTANCE_TPS_ROLLBACK.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.tps.push({
        data: tempData,
        name: t("instanceMonitor.instance.rollbackQty"),
      });
    }
    if (baseData.INSTANCE_TPS_COMMIT) {
      let tempData: string[] = [];
      baseData.INSTANCE_TPS_COMMIT.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.tps.push({
        data: tempData,
        name: t("instanceMonitor.instance.commitQty"),
      });
    }
    if (baseData.INSTANCE_TPS_CR) {
      let tempData: string[] = [];
      baseData.INSTANCE_TPS_CR.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.tps.push({
        data: tempData,
        name: t("instanceMonitor.instance.transTotalQty"),
      });
    }

    // QPS
    if (baseData.INSTANCE_QPS) {
      let tempData: string[] = [];
      baseData.INSTANCE_QPS.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.qps.push({
        data: tempData,
        name: t("instanceMonitor.instance.queryQty"),
      });
    }

    // databaseIns
    if (baseData.INSTANCE_DB_DATABASE_INS) {
      for (let key in baseData.INSTANCE_DB_DATABASE_INS) {
        let tempData: string[] = []
        baseData.INSTANCE_DB_DATABASE_INS[key]?.forEach((element) => {
          tempData.push(toFixed(element))
        })
        metricsData.value.databaseIns.push({ data: tempData, name: key, areaStyle: {}, stack: "Total" })
      }
    }

    // databaseUpd
    if (baseData.INSTANCE_DB_DATABASE_UPD) {
      for (let key in baseData.INSTANCE_DB_DATABASE_UPD) {
        let tempData: string[] = []
        baseData.INSTANCE_DB_DATABASE_UPD[key]?.forEach((element) => {
          tempData.push(toFixed(element))
        })
        metricsData.value.databaseUpd.push({ data: tempData, name: key, areaStyle: {}, stack: "Total" })
      }
    }

    // databaseDel
    if (baseData.INSTANCE_DB_DATABASE_DEL) {
      for (let key in baseData.INSTANCE_DB_DATABASE_DEL) {
        let tempData: string[] = []
        baseData.INSTANCE_DB_DATABASE_DEL[key]?.forEach((element) => {
          tempData.push(toFixed(element))
        })
        metricsData.value.databaseDel.push({ data: tempData, name: key, areaStyle: {}, stack: "Total" })
      }
    }

    // databaseReturn
    if (baseData.INSTANCE_DB_DATABASE_RETURN) {
      for (let key in baseData.INSTANCE_DB_DATABASE_RETURN) {
        let tempData: string[] = []
        baseData.INSTANCE_DB_DATABASE_RETURN[key]?.forEach((element) => {
          tempData.push(toFixed(element))
        })
        metricsData.value.databaseReturn.push({ data: tempData, name: key, areaStyle: {}, stack: "Total" })
      }
    }

    // databaseFecth
    if (baseData.INSTANCE_DB_DATABASE_FECTH) {
      for (let key in baseData.INSTANCE_DB_DATABASE_FECTH) {
        let tempData: string[] = []
        baseData.INSTANCE_DB_DATABASE_FECTH[key]?.forEach((element) => {
          tempData.push(toFixed(element))
        })
        metricsData.value.databaseFecth.push({ data: tempData, name: key, areaStyle: {}, stack: "Total" })
      }
    }

    // bgwriter
    if (baseData.INSTANCE_DB_BGWRITER_CHECKPOINT) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_BGWRITER_CHECKPOINT.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.bgwriter.push({
        data: tempData,
        name: t("instanceIndex.bgwriterCheckpoint"),
      });
    }
    if (baseData.INSTANCE_DB_BGWRITER_CLEAN) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_BGWRITER_CLEAN.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.bgwriter.push({
        data: tempData,
        name: t("instanceIndex.bgwriterClean"),
      });
    }
    if (baseData.INSTANCE_DB_BGWRITER_BACKEND) {
      let tempData: string[] = [];
      baseData.INSTANCE_DB_BGWRITER_BACKEND.forEach((d: number) => {
        tempData.push(d.toString());
      });
      metricsData.value.bgwriter.push({
        data: tempData,
        name: t("instanceIndex.bgwriterBackend"),
      });
    }

    // time
    metricsData.value.time = baseData.time;
  },
  { deep: true }
);
const gotoSQLDiagnosis = () => {
  hasSQLDiagnosisModule()
    .then(() => {
      const curMode = localStorage.getItem("INSTANCE_CURRENT_MODE");
      if (curMode === "wujie") {
        // @ts-ignore plug-in components
        window.$wujie?.props.methods.jump({
          name: `Static-pluginObservability-sql-diagnosisVemHistoryDiagnosis`,
          query: {
            instanceId: instanceId.value,
            startTime: timeRange.value[0],
            endTime: timeRange.value[1],
          },
        });
      } else ElMessage.error(t("app.needSQLDiagnosis"));
    })
    .catch(() => {
      ElMessage.error(t("app.needSQLDiagnosis"));
    });
};

const TPS = ref();
const QPS = ref();
const databaseIns = ref();
const databaseUpd = ref();
const databaseDel = ref();
const databaseReturn = ref();
const databaseFecth = ref();
const bgwriter = ref();
const download = (title: string, ref: any) => {
  ref.download(title)
}

const tpsInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [{ name: t('instanceMonitor.instance.rollbackQty'), value: t('instanceMonitor.instance.rollbackQtyContent') },
  { name: t('instanceMonitor.instance.commitQty'), value: t('instanceMonitor.instance.commitQtyContent') },
  { name: t('instanceMonitor.instance.transTotalQty'), value: t('instanceMonitor.instance.transTotalQtyContent') }]
})
const qpsInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [{ name: t('instanceMonitor.instance.queryQty'), value: t('instanceMonitor.instance.queryQtyContent') },
    { name: t('dbParam.common.tip'), value: `1:${t('dbParam.common.tipContent')};2:${t('dbParam.trackActivities.tipContent')};3:${t('dbParam.trackSqlCount.tipContent')}` }
  ]
})
const databaseInsInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('instanceIndex.databaseIns'), value: t('instanceIndex.databaseInsContent') }
  ]
})
const databaseUpdInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('instanceIndex.databaseUpd'), value: t('instanceIndex.databaseUpdContent') }
  ]
})
const databaseDelInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('instanceIndex.databaseDel'), value: t('instanceIndex.databaseDelContent') }
  ]
})
const databaseReturnInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('instanceIndex.databaseReturn'), value: t('instanceIndex.databaseReturnContent') }
  ]
})
const databaseFecthInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('instanceIndex.databaseFecth'), value: t('instanceIndex.databaseFecthContent') }
  ]
})
const bgwriterInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [{ name: t('instanceIndex.bgwriterCheckpoint'), value: t('instanceIndex.bgwriterCheckpointContent') },
  { name: t('instanceIndex.bgwriterClean'), value: t('instanceIndex.bgwriterCleanContent') },
  { name: t('instanceIndex.bgwriterBackend'), value: t('instanceIndex.bgwriterBackendContent') }]
})
</script>

<style scoped lang="scss"></style>
