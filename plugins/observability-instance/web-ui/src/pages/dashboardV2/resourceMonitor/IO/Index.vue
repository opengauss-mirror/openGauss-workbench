<template>
  <div class="chart-link">
    <span>{{$t('echart.linkage')}}:&nbsp;&nbsp;</span> <span><el-switch v-model="isLinkage" @change="changeChartLinkage"/></span>
  </div>
  <el-row :gutter="12">
    <el-col :span=spanParams.values[0]>
      <my-card
        :title="$t('resourceMonitor.io.ioDbDiskUsage')"
        height="300"
        :bodyPadding="false"
        :info="dbDiskInfo"
        :showBtns="true"
        @download="(title) => download(title, ioDbDiskUsage)"
      >
        <div class="charts-container">
          <div style="width: 50%;  height: 100%;" v-if="metricsData.xLogShouldShow">
            <LazyLine
              ref="ioDbDiskUsage"
              :tabId="props.tabId"
              :formatter="toFixed"
              :data="metricsData.dbDiskUsage"
              :xData="metricsData.time"
              :unit="'%'"
              :max="100"
              :min="0"
              :chartContent=metricsData.dbChartContent
              :interval="25"
              :tool-tips-exclude-zero="true"
              :isLinkage="isLinkage"
            />
          </div>
          <div style="width: 100%;  height: 100%;" v-else>
            <LazyLine
              ref="ioDbDiskUsage"
              :tabId="props.tabId"
              :formatter="toFixed"
              :data="metricsData.dbDiskUsage"
              :xData="metricsData.time"
              :unit="'%'"
              :max="100"
              :min="0"
              :dbDiskIo="true"
              :chartContent=metricsData.dbChartContent
              :interval="25"
              :tool-tips-exclude-zero="true"
              :isLinkage="isLinkage"
            />
          </div>
          <div style="width: 50%;  height: 100%;" v-if="metricsData.xLogShouldShow">
            <LazyLine
              ref="ioDbDiskUsage"
              :tabId="props.tabId"
              :formatter="toFixed"
              :data="metricsData.xLogDiskUsage"
              :xData="metricsData.time"
              :unit="'%'"
              :max="100"
              :min="0"
              :dbDiskIo="true"
              :chartContent=metricsData.xLogChartContent
              :interval="25"
              :tool-tips-exclude-zero="true"
              :isLinkage="isLinkage"
            />
          </div>
        </div>
      </my-card>
    </el-col>
    <el-col :span=spanParams.values[1]>
      <my-card
        :title="$t('resourceMonitor.io.ioOtherDiskUsage')"
        height="300"
        :bodyPadding="false"
        :info="ioDiskInfo"
        :showBtns="true"
        @download="(title) => download(title, ioOtherDiskUsage)"
      >
        <LazyLine
          ref="ioOtherDiskUsage"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="deviceIODiskUsage"
          :xData="metricsData.time"
          :unit="'%'"
          :max="100"
          :min="0"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span=spanParams.values[2]>
      <my-card
        :title="$t('resourceMonitor.io.ioDiskInodeUsage')"
        height="300"
        :bodyPadding="false"
        :info="ioDiskInodeInfo"
        :showBtns="true"
        @download="(title) => download(title, ioDiskInodeUsage)"
      >
        <LazyLine
          ref="ioDiskInodeUsage"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="deviceIODiskInodeUsage"
          :xData="metricsData.time"
          :unit="'%'"
          :max="100"
          :min="0"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
  </el-row>
  <div class="gap-row"></div>
  <my-card
    :title="$t('resourceMonitor.io.deviceIO')"
    :info="deviceIOInfo"
    :bodyPadding="false"
    skipBodyHeight
    showQuestion
  >
    <el-table
      :data="metricsData.table"
      style="width: 100%"
      border
      :header-cell-class-name="
        () => {
          return 'grid-header';
        }
      "
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="device" label="Device" />
      <el-table-column prop="IO_TPS" label="TPS" />
      <el-table-column prop="IO_RD" label="rd(KB)/s" />
      <el-table-column prop="IO_WT" label="wt(KB)/s" />
      <el-table-column prop="IO_AVGRQ_SZ" label="avgrq-sz (KB)" />
      <el-table-column prop="IO_AVGQU_SZ" label="avgqu-sz" />
      <el-table-column prop="IO_AWAIT" label="await(ms)" />
      <el-table-column prop="IO_UTIL" label="%util" />
    </el-table>
  </my-card>

  <div class="gap-row"></div>

  <el-row :gutter="12">
    <el-col :span="12">
      <my-card
        :title="'IOPS'"
        height="300"
        :bodyPadding="false"
        :showExtBtn="true"
        :showBtns="true"
        @download="(title) => download(title, IOPS)"
        :info="iopsInfo"
      >
        <template #headerExtend>
          <div class="card-links">
            <el-link
              v-if="isManualRangeSelected"
              type="primary"
              @click="
                goto(tabKeys.InstanceMonitorTOPSQL, {
                  key: tabKeys.InstanceMonitorTOPSQLIOTime,
                })
              "
            >
              TOP CPU SQL
            </el-link>
            <el-link
              v-if="isManualRangeSelected"
              type="primary"
              @click="gotoSQLDiagnosis()"
            >
              {{ $t("app.diagnosis") }}
            </el-link>
            <el-link
              v-if="isManualRangeSelected"
              type="primary"
              @click="wdr(tabId)"
              v-loading="wdrLoading"
            >
              {{ $t("instanceIndex.wdrAnalysis") }}
            </el-link>
          </div>
        </template>
        <LazyLine
          ref="IOPS"
          :tips="$t('instanceIndex.activeSessionQtyTips')"
          :rangeSelect="true"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="deviceIOPS"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card
        :title="$t('resourceMonitor.io.rwSecond')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, rwSecond)"
        :info="rwSecondInfo"
      >
        <LazyLine
          ref="rwSecond"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="deviceRW"
          :xData="metricsData.time"
          :unit="'B'"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <el-row :gutter="12">
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.io.queueLength')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, queueLength)"
        :info="queueLengthInfo"
      >
        <LazyLine
          ref="queueLength"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="deviceIOQueue"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.io.ioUsage')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, ioUsage)"
        :info="ioUsageInfo"
      >
        <LazyLine
          ref="ioUsage"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="deviceIOUsage"
          :xData="metricsData.time"
          :max="100"
          :min="0"
          :interval="25"
          :unit="'%'"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.io.ioTime')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, ioTime)"
        :info="ioTimeInfo"
      >
        <LazyLine
          ref="ioTime"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="deviceIOTime"
          :xData="metricsData.time"
          :unit="'ms'"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import LazyLine from "@/components/echarts/LazyLine.vue";
import { useMonitorStore } from "@/store/monitor";
import { toFixed } from "@/shared";
import { storeToRefs } from "pinia";
import { getIOMetrics } from "@/api/observability";
import { useIntervalTime } from "@/hooks/time";
import { tabKeys } from "@/pages/dashboardV2/common";
import { useRequest } from "vue-request";
import { hasSQLDiagnosisModule } from "@/api/sqlDiagnosis";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import { getWDRSnapshot } from "@/api/wdr";
import moment from "moment";
import { useParamsStore } from "@/store/params";

const props = withDefaults(defineProps<{ tabId: string }>(), {});
const { t } = useI18n();

const spanParams = reactive({ values: [8, 8, 8] });

interface LineData {
  name: string;
  data: any[];
  [other: string]: any;
}
interface MetricsData {
  table: any[];
  iops: LineData[];
  rw: LineData[];
  queueLenth: LineData[];
  ioUse: LineData[];
  ioTime: LineData[];
  time: string[];
  dbDiskUsage: LineData[];
  dbVolume: string[];
  dbDiskName: string;
  xLogShouldShow: boolean;
  xLogDiskUsage: LineData[];
  xLogVolume: string[];
  xLogDiskName: string;
  dbDiskData: string[];
  xLogDiskData: string[];
  dbChartContent: any[];
  xLogChartContent: any[];

}
const metricsData = ref<MetricsData>({
  table: [],
  iops: [],
  rw: [],
  queueLenth: [],
  ioUse: [],
  ioTime: [],
  time: [],
  dbDiskUsage: [],
  dbVolume: [],
  dbDiskName: '',
  xLogShouldShow: false,
  xLogDiskUsage: [],
  xLogVolume: [],
  xLogDiskName: '',
  dbDiskData: [],
  xLogDiskData: [],
  dbChartContent: [],
  xLogChartContent: []

});
const multipleSelection = ref<string[]>([]);
const deviceIOPS = computed(() => {
  let baseData = metricsData.value.iops;
  if (multipleSelection.value.length <= 0) {
    return baseData;
  } else {
    let result = [];
    let selectedKeys = [];
    for (let index = 0; index < multipleSelection.value.length; index++) {
      const element: any = multipleSelection.value[index];
      selectedKeys.push(element.device);
    }
    for (let index = 0; index < baseData.length; index++) {
      const element: any = baseData[index];
      if (selectedKeys.indexOf(element.key) >= 0) {
        result.push(element);
      }
    }
    return result;
  }
});
const emit = defineEmits(["goto"]);
const goto = (key: string, param: object) => {
  emit("goto", key, param);
};
const deviceRW = computed(() => {
  let baseData = metricsData.value.rw;
  if (multipleSelection.value.length <= 0) {
    return baseData;
  } else {
    let result = [];
    let selectedKeys = [];
    for (let index = 0; index < multipleSelection.value.length; index++) {
      const element: any = multipleSelection.value[index];
      selectedKeys.push(element.device);
    }
    for (let index = 0; index < baseData.length; index++) {
      const element: any = baseData[index];
      if (selectedKeys.indexOf(element.key) >= 0) {
        result.push(element);
      }
    }
    return result;
  }
});
const deviceIOUsage = computed(() => {
  let baseData = metricsData.value.ioUse;
  if (multipleSelection.value.length <= 0) {
    return baseData;
  } else {
    let result = [];
    let selectedKeys = [];
    for (let index = 0; index < multipleSelection.value.length; index++) {
      const element: any = multipleSelection.value[index];
      selectedKeys.push(element.device);
    }
    for (let index = 0; index < baseData.length; index++) {
      const element: any = baseData[index];
      if (selectedKeys.indexOf(element.key) >= 0) {
        result.push(element);
      }
    }
    return result;
  }
});
const deviceIOQueue = computed(() => {
  let baseData = metricsData.value.queueLenth;
  if (multipleSelection.value.length <= 0) {
    return baseData;
  } else {
    let result = [];
    let selectedKeys = [];
    for (let index = 0; index < multipleSelection.value.length; index++) {
      const element: any = multipleSelection.value[index];
      selectedKeys.push(element.device);
    }
    for (let index = 0; index < baseData.length; index++) {
      const element: any = baseData[index];
      if (selectedKeys.indexOf(element.key) >= 0) {
        result.push(element);
      }
    }
    return result;
  }
});
const deviceIOTime = computed(() => {
  let baseData = metricsData.value.ioTime;
  if (multipleSelection.value.length <= 0) {
    return baseData;
  } else {
    let result = [];
    let selectedKeys = [];
    for (let index = 0; index < multipleSelection.value.length; index++) {
      const element: any = multipleSelection.value[index];
      selectedKeys.push(element.device);
    }
    for (let index = 0; index < baseData.length; index++) {
      const element: any = baseData[index];
      if (selectedKeys.indexOf(element.key) >= 0) {
        result.push(element);
      }
    }
    return result;
  }
});

const deviceIODiskUsage = computed(() => {
  let baseData = metricsData.value.ioDiskUsage;
  if (multipleSelection.value.length <= 0) {
    return baseData;
  } else {
    let result = [];
    let selectedKeys = [];
    for (let index = 0; index < multipleSelection.value.length; index++) {
      const element: any = multipleSelection.value[index];
      selectedKeys.push(element.device);
    }
    for (let index = 0; index < baseData.length; index++) {
      const element: any = baseData[index];
      if (selectedKeys.indexOf(element.key) >= 0) {
        result.push(element);
      }
    }
    return result;
  }
});

const deviceIODiskInodeUsage = computed(() => {
  let baseData = metricsData.value.ioDiskInodeUsage;
  if (multipleSelection.value.length <= 0) {
    return baseData;
  } else {
    let result = [];
    let selectedKeys = [];
    for (let index = 0; index < multipleSelection.value.length; index++) {
      const element: any = multipleSelection.value[index];
      selectedKeys.push(element.device);
    }
    for (let index = 0; index < baseData.length; index++) {
      const element: any = baseData[index];
      if (selectedKeys.indexOf(element.key) >= 0) {
        result.push(element);
      }
    }
    return result;
  }
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
  isLinkage.value = isChartLinkage.value
  load();
});
watch(
  updateCounter,
  () => {
    isLinkage.value = isChartLinkage.value
    clearInterval(timer.value);
    if (tabNow.value === tabKeys.ResourceMonitorIO) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) load();
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

const getWidth = computed(() => {
  return spanParams.values[3] + '%';
});

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
  if (!instanceId.value) return;
  requestData(props.tabId);
};
const { data: indexData, run: requestData } = useRequest(getIOMetrics, { manual: true });
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.iops = [];
    metricsData.value.rw = [];
    metricsData.value.queueLenth = [];
    metricsData.value.ioUse = [];
    metricsData.value.ioTime = [];
    metricsData.value.ioDiskUsage = [];
    metricsData.value.ioDiskInodeUsage = [];
    metricsData.value.dbDiskUsage = [];
    metricsData.value.dbVolume = [];
    metricsData.value.dbDiskTitle = '';
    metricsData.value.xLogDiskUsage = [];
    metricsData.value.xLogVolume = [];
    metricsData.value.xLogDiskTitle = '';
    metricsData.value.xLogShouldShow = false;
    metricsData.value.dbChartContent = [];
    metricsData.value.xLogChartContent = [];
    const baseData = indexData.value;
    if (!baseData) return;

    // db disk usage
    let dataDirName = ''
    let dataPartName = ''
    let xLogDirName = ''
    let xLogPartName = ''
    let dataDirKeys = ''
    let xLogDirKeys = ''
    if (baseData && baseData.IO_DISK_DB_VOLUME_DATA && baseData.IO_DISK_DB_VOLUME_XLOG) {
      dataDirKeys = Object.keys(baseData.IO_DISK_DB_VOLUME_DATA);
      xLogDirKeys = Object.keys(baseData.IO_DISK_DB_VOLUME_XLOG);
      if (dataDirKeys.length > 0 && xLogDirKeys.length > 0) {
        dataDirName = dataDirKeys[0].split(',')[0]
        dataPartName = dataDirKeys[0].split(',')[1]
        xLogDirName = xLogDirKeys[0].split(',')[0]
        xLogPartName = xLogDirKeys[0].split(',')[1]
      }
    }
    let dbDiskName = ''
    let xLogDiskName = ''
    if (xLogDirName && dataDirName && xLogDirName.includes(dataDirName) && xLogDirKeys.length === 1 && baseData.IO_DISK_DB_USAGE_DATA) {
      spanParams.values[0] = 8
      spanParams.values[1] = 8
      spanParams.values[2] = 8
      let totalVolume = 0;
      for (let key in baseData.IO_DISK_DB_VOLUME_TOTAL) {
        for (const element of baseData.IO_DISK_DB_VOLUME_TOTAL[key]) {
            if (element != '0') {
                totalVolume = toFixed(element / 1024 / 1024);
                break;
            }
        }
      }
      const dataKeys = Object.keys(baseData.IO_DISK_DB_USAGE_DATA);
      dbDiskName = dataKeys[0]
      metricsData.value.dbDiskTitle = t('resourceMonitor.io.ioDiskName') + dataKeys[0] + '(' + dataPartName + '),' + totalVolume
      for (let key in baseData.IO_DISK_DB_VOLUME_USED) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_VOLUME_USED[key].forEach((element) => {
          tempData.push(toFixed(element/1024/1024));
        });
        metricsData.value.dbDiskData = tempData
      }
      for (let key in baseData.IO_DISK_DB_VOLUME_DATA) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_VOLUME_DATA[key].forEach((element) => {
          tempData.push(toFixed(element/1024/1024));
        });
        metricsData.value.dbVolume.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioDataDirectory').replace('dataDir',dataDirName)
        });
      }
      for (let key in baseData.IO_DISK_DB_VOLUME_XLOG) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_VOLUME_XLOG[key].forEach((element) => {
          tempData.push(toFixed(element/1024/1024));
        });
        metricsData.value.dbVolume.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioXLogDirectory').replace('xLogDir',xLogDirName)
        });
      }
      for (let key in baseData.IO_DISK_DB_VOLUME_OTHER) {
         let tempData: string[] = [];
         baseData.IO_DISK_DB_VOLUME_OTHER[key].forEach((element) => {
           tempData.push(toFixed(element/1024/1024));
         });
         metricsData.value.dbVolume.push({
           data: tempData,
           areaStyle: {},
           stack: "Total",
           name: t('resourceMonitor.io.ioOtherDirectory')
         });
      }
      for (let key in baseData.IO_DISK_DB_USAGE_DATA) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_USAGE_DATA[key].forEach((element) => {
          tempData.push(toFixed(element));
        });
        metricsData.value.dbDiskUsage.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioDataDirectory').replace('dataDir',dataDirName)
        });
      }
      for (let key in baseData.IO_DISK_DB_USAGE_XLOG) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_USAGE_XLOG[key].forEach((element) => {
          tempData.push(toFixed(element));
        });
        metricsData.value.dbDiskUsage.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioXLogDirectory').replace('xLogDir',xLogDirName)
        });
      }
      for (let key in baseData.IO_DISK_DB_USAGE_OTHER) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_USAGE_OTHER[key].forEach((element) => {
          tempData.push(toFixed(element));
        });
        metricsData.value.dbDiskUsage.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioOtherDirectory')
        });
      }
      metricsData.value.dbChartContent.push({
        chartTitle: metricsData.value.dbDiskTitle,
        chartTitleData: metricsData.value.dbDiskData,
        volumeData: metricsData.value.dbVolume
      });
    } else if (xLogDirName && dataDirName && baseData.IO_DISK_DB_USAGE_DATA && baseData.IO_DISK_DB_USAGE_XLOG){
      metricsData.value.xLogShouldShow = true
      spanParams.values[0] = 12
      spanParams.values[1] = 6
      spanParams.values[2] = 6
      let totalVolume = 0;
      for (let key in baseData.IO_DISK_DB_VOLUME_TOTAL) {
        for (const element of baseData.IO_DISK_DB_VOLUME_TOTAL[key]) {
            if (element != '0') {
                totalVolume = toFixed(element / 1024 / 1024);
                break;
            }
        }
      }
      const dataKeys = Object.keys(baseData.IO_DISK_DB_USAGE_DATA);
      dbDiskName = dataKeys[0]
      metricsData.value.dbDiskTitle = t('resourceMonitor.io.ioDiskName') + dataKeys[0] + '(' + dataPartName + '),' + totalVolume
      let xLogTotalVolume = 0;
      for (let key in baseData.IO_DISK_XLOG_VOLUME_TOTAL) {
        for (const element of baseData.IO_DISK_XLOG_VOLUME_TOTAL[key]) {
            if (element != '0') {
                xLogTotalVolume = toFixed(element / 1024 / 1024);
                break;
            }
        }
      }
      const xLogKeys = Object.keys(baseData.IO_DISK_DB_USAGE_XLOG);
      xLogDiskName = xLogKeys[0]
      metricsData.value.xLogDiskTitle = t('resourceMonitor.io.ioDiskName') + xLogKeys[0] + '(' + xLogPartName + '),' + xLogTotalVolume
      for (let key in baseData.IO_DISK_DB_VOLUME_USED) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_VOLUME_USED[key].forEach((element) => {
          tempData.push(toFixed(element/1024/1024));
        });
        metricsData.value.dbDiskData = tempData
      }
      for (let key in baseData.IO_DISK_XLOG_VOLUME_USED) {
        let tempData: string[] = [];
        baseData.IO_DISK_XLOG_VOLUME_USED[key].forEach((element) => {
          tempData.push(toFixed(element/1024/1024));
        });
        metricsData.value.xLogDiskData = tempData
      }
      for (let key in baseData.IO_DISK_DB_VOLUME_DATA) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_VOLUME_DATA[key].forEach((element) => {
          tempData.push(toFixed(element/1024/1024));
        });
        metricsData.value.dbVolume.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioDataDirectory2').replace('dataDir',dataDirName)
        });
      }
      for (let key in baseData.IO_DISK_DB_VOLUME_OTHER_TWO) {
         let tempData: string[] = [];
         baseData.IO_DISK_DB_VOLUME_OTHER_TWO[key].forEach((element) => {
           tempData.push(toFixed(element/1024/1024));
         });
         metricsData.value.dbVolume.push({
           data: tempData,
           areaStyle: {},
           stack: "Total",
           name: t('resourceMonitor.io.ioOtherDirectory')
         });
      }
      for (let key in baseData.IO_DISK_DB_VOLUME_XLOG) {
        let tempData: string[] = [];
        if ( key.includes(dataDirName) ){
          baseData.IO_DISK_DB_VOLUME_XLOG[key].forEach((element) => {
            tempData.push(toFixed(element/1024/1024));
          });
          metricsData.value.dbVolume.push({
            data: tempData,
            areaStyle: {},
            stack: "Total",
            name: t('resourceMonitor.io.ioXLogDirectory').replace('xLogDir',xLogDirName)
          });
        } else {
          baseData.IO_DISK_DB_VOLUME_XLOG[key].forEach((element) => {
            tempData.push(toFixed(element/1024/1024));
          });
          metricsData.value.xLogVolume.push({
            data: tempData,
            areaStyle: {},
            stack: "Total",
            name: t('resourceMonitor.io.ioXLogDirectory').replace('xLogDir',xLogDirName)
          });
        }
      }
      for (let key in baseData.IO_DISK_XLOG_VOLUME_OTHER_TWO) {
        let tempData: string[] = [];
        if ( key === dbDiskName ) {
         baseData.IO_DISK_XLOG_VOLUME_OTHER_TWO[key].forEach((element) => {
            tempData.push(toFixed(element/1024/1024));
          });
          metricsData.value.dbVolume.push({
            data: tempData,
            areaStyle: {},
            stack: "Total",
            name: t('resourceMonitor.io.ioOtherDirectory')
          });
        } else {
          baseData.IO_DISK_XLOG_VOLUME_OTHER_TWO[key].forEach((element) => {
            tempData.push(toFixed(element/1024/1024));
          });
          metricsData.value.xLogVolume.push({
            data: tempData,
            areaStyle: {},
            stack: "Total",
            name: t('resourceMonitor.io.ioOtherDirectory')
          });
        }
      }
      for (let key in baseData.IO_DISK_DB_USAGE_DATA) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_USAGE_DATA[key].forEach((element) => {
          tempData.push(toFixed(element));
        });
        metricsData.value.dbDiskUsage.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioDataDirectory2').replace('dataDir',dataDirName)
        });
      }
      for (let key in baseData.IO_DISK_DB_USAGE_OTHER_TWO) {
        let tempData: string[] = [];
        baseData.IO_DISK_DB_USAGE_OTHER_TWO[key].forEach((element) => {
          tempData.push(toFixed(element));
        });
        metricsData.value.dbDiskUsage.push({
          data: tempData,
          areaStyle: {},
          stack: "Total",
          name: t('resourceMonitor.io.ioOtherDirectory')
        });
      }
      for (let key in baseData.IO_DISK_DB_USAGE_XLOG) {
        let tempData: string[] = [];
        if ( key === dbDiskName ) {
          baseData.IO_DISK_DB_USAGE_XLOG[key].forEach((element) => {
            tempData.push(toFixed(element));
          });
          metricsData.value.dbDiskUsage.push({
            data: tempData,
            areaStyle: {},
            stack: "Total",
            name: t('resourceMonitor.io.ioXLogDirectory').replace('xLogDir',xLogDirName)
          });
        } else {
          baseData.IO_DISK_DB_USAGE_XLOG[key].forEach((element) => {
            tempData.push(toFixed(element));
          });
          metricsData.value.xLogDiskUsage.push({
            data: tempData,
            areaStyle: {},
            stack: "Total",
            name: t('resourceMonitor.io.ioXLogDirectory').replace('xLogDir',xLogDirName)
          });
        }
      }
      for (let key in baseData.IO_DISK_XLOG_USAGE_OTHER_TWO) {
        let tempData: string[] = [];
        if ( key === dbDiskName ) {
        } else {
          baseData.IO_DISK_XLOG_USAGE_OTHER_TWO[key].forEach((element) => {
            tempData.push(toFixed(element));
          });
          metricsData.value.xLogDiskUsage.push({
            data: tempData,
            areaStyle: {},
            stack: "Total",
            name: t('resourceMonitor.io.ioOtherDirectory')
          });
        }
      }
      metricsData.value.dbChartContent.push({
        chartTitle: metricsData.value.dbDiskTitle,
        chartTitleData: metricsData.value.dbDiskData,
        volumeData: metricsData.value.dbVolume
      });
      metricsData.value.xLogChartContent.push({
        chartTitle: metricsData.value.xLogDiskTitle,
        chartTitleData: metricsData.value.xLogDiskData,
        volumeData: metricsData.value.xLogVolume
      });
    }
    // table
    for (let index = 0; index < baseData.table.length; index++) {
      const element = baseData.table[index];
      element.IO_TPS = toFixed(Math.max(parseFloat(element.IO_TPS) || 0, 0));
      element.IO_RD = toFixed(Math.max(parseFloat(element.IO_RD) || 0, 0));
      element.IO_WT = toFixed(Math.max(parseFloat(element.IO_WT) || 0, 0));
      element.IO_AVGRQ_SZ = toFixed(Math.max(parseFloat(element.IO_AVGRQ_SZ) || 0, 0));
      element.IO_AVGQU_SZ = toFixed(Math.max(parseFloat(element.IO_AVGQU_SZ) || 0, 0));
      element.IO_AWAIT = toFixed(Math.max(parseFloat(element.IO_AWAIT) || 0, 0));
      element.IO_UTIL = toFixed(Math.max(parseFloat(element.IO_UTIL) || 0, 0));
    }
    metricsData.value.table = baseData.table;

    // IOPS
    for (let key in baseData.IOPS_R) {
      let tempData: string[] = [];
      baseData.IOPS_R[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.iops.push({ data: tempData, name: key + "(读)", key });
    }
    for (let key in baseData.IOPS_W) {
      let tempData: string[] = [];
      baseData.IOPS_W[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.iops.push({ data: tempData, name: key + "(写)", key });
    }

    // rw byte
    for (let key in baseData.IO_DISK_READ_BYTES_PER_SECOND) {
      let tempData: string[] = [];
      baseData.IO_DISK_READ_BYTES_PER_SECOND[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.rw.push({ data: tempData, name: key + "(读)", key });
    }
    for (let key in baseData.IO_DISK_WRITE_BYTES_PER_SECOND) {
      let tempData: string[] = [];
      baseData.IO_DISK_WRITE_BYTES_PER_SECOND[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.rw.push({ data: tempData, name: key + "(写)", key });
    }

    // queue
    for (let key in baseData.IO_QUEUE_LENGTH) {
      let tempData: string[] = [];
      baseData.IO_QUEUE_LENGTH[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.queueLenth.push({ data: tempData, name: key, key });
    }

    // io use
    for (let key in baseData.IO_UTIL) {
      let tempData: string[] = [];
      baseData.IO_UTIL[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.ioUse.push({ data: tempData, name: key, key });
    }

    // io time
    for (let key in baseData.IO_AVG_REPONSE_TIME_READ) {
      let tempData: string[] = [];
      baseData.IO_AVG_REPONSE_TIME_READ[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.ioTime.push({ data: tempData, name: key + "(读)", key });
    }
    for (let key in baseData.IO_AVG_REPONSE_TIME_WRITE) {
      let tempData: string[] = [];
      baseData.IO_AVG_REPONSE_TIME_WRITE[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.ioTime.push({ data: tempData, name: key + "(写)", key });
    }
    for (let key in baseData.IO_AVG_REPONSE_TIME_RW) {
      let tempData: string[] = [];
      baseData.IO_AVG_REPONSE_TIME_RW[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.ioTime.push({ data: tempData, name: key + "(读+写)", key });
    }
    for (let key in baseData.IO_DISK_USAGE) {
      if (key === dbDiskName || key === xLogDiskName)
        continue;
      let tempData: string[] = [];
      baseData.IO_DISK_USAGE[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.ioDiskUsage.push({ data: tempData, name: key, key });
    }
    for (let key in baseData.IO_DISK_INODE_USAGE) {
      let tempData: string[] = [];
      baseData.IO_DISK_INODE_USAGE[key].forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.ioDiskInodeUsage.push({ data: tempData, name: key, key });
    }
    metricsData.value.time = baseData.time;
  },
  { deep: true }
);
const handleSelectionChange = (val: any) => {
  multipleSelection.value = val;
};
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

const { data: wdrData, run: wdr, loading: wdrLoading } = useRequest(getWDRSnapshot, {
  manual: true,
});
watch(
  wdrData,
  (res: any) => {
    // goto wdr
    if (res && res.wdrId && res.wdrId.length > 0) {
      const { timeRange } = useMonitorStore(props.tabId);
      let param = {
        operation: "search",
        startTime:
          timeRange == null ? "" : moment(timeRange[0]).format("YYYY-MM-DD HH:mm:ss"),
        endTime:
          timeRange == null ? "" : moment(timeRange[1]).format("YYYY-MM-DD HH:mm:ss"),
      };
      emit("goto", tabKeys.WDR, param);
    } else if (res && res.start && res.end) {
      let param = {
        operation: "edit",
        startId: res.start,
        endId: res.end,
      };
      emit("goto", tabKeys.WDR, param);
    } else {
      ElMessage.error(t("wdrReports.wdrErrtip"));
    }
  },
  { deep: true }
);

const ioDiskUsage = ref();
const ioDiskInodeUsage = ref();
const ioDbDiskUsage = ref();
const ioOtherDiskUsage = ref();
const IOPS = ref();
const rwSecond = ref();
const queueLength = ref();
const ioUsage = ref();
const ioTime = ref();
const download = (title: string, ref: any) => {
  ref.download(title);
};

const ioDiskInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: t("resourceMonitor.io.ioDiskUsage"),
      value: t("resourceMonitor.io.ioDiskUsageContent1"),
    },
  ],
});
const dbDiskInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: t("resourceMonitor.io.dbDiskUsage"),
      value: t("resourceMonitor.io.dbDiskUsageContent"),
    },
  ],
});
const ioDiskInodeInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: t("resourceMonitor.io.ioDiskInodeUsage"),
      value: t("resourceMonitor.io.ioDiskUsageContent3"),
    },
  ],
});
const deviceIOInfo = ref<any>({
  title: t("app.fieldOverview"),
  option: [
    {
      name: "device",
      value: t("resourceMonitor.io.deviceContent"),
    },
    {
      name: "TPS",
      value: t("resourceMonitor.io.tpsContent"),
    },
    {
      name: "rd(KB)/s",
      value: t("resourceMonitor.io.rdContent"),
    },
    {
      name: "wt(KB)/s",
      value: t("resourceMonitor.io.wtContent"),
    },
    {
      name: "avgrq-sz (KB)",
      value: t("resourceMonitor.io.avgrqContent"),
    },
    {
      name: "avgqu-sz",
      value: t("resourceMonitor.io.avgquContent"),
    },
    {
      name: "await(ms)",
      value: t("resourceMonitor.io.awaitContent"),
    },
    {
      name: "%util",
      value: t("resourceMonitor.io.utilContent"),
    },
  ],
});
const iopsInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: 'IOPS',
      value: t("resourceMonitor.io.iopsContent"),
    },
  ],
});
const rwSecondInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: t("resourceMonitor.io.rwSecond"),
      value: t("resourceMonitor.io.rwSecondContent"),
    },
  ],
});
const queueLengthInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: t("resourceMonitor.io.queueLength"),
      value: t("resourceMonitor.io.queueLengthContent"),
    },
  ],
});
const ioUsageInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: t("resourceMonitor.io.ioUsage"),
      value: t("resourceMonitor.io.ioUsageContent"),
    },
  ],
});
const ioTimeInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    {
      name: t("resourceMonitor.io.ioTime"),
      value: t("resourceMonitor.io.ioTimeContent"),
    },
  ],
});

const { isChartLinkage } = storeToRefs(useParamsStore());
const paramsStore = useParamsStore();
const isLinkage = ref<boolean>(false)
const changeChartLinkage = () => {
  paramsStore.setChartLinkage(isLinkage.value)
}
</script>

<style scoped lang="scss">
.chart-link {
  display: flex;
  justify-content: flex-end;
}
.charts-container {
  display: flex;
  height: 100%;
}
</style>
