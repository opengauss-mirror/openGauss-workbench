<template>
  <div class="chart-link">
    <span>{{$t('echart.linkage')}}:&nbsp;&nbsp;</span> <span><el-switch v-model="isLinkage" @change="changeChartLinkage"/></span>
  </div>
  <el-row :gutter="12">
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.network.in')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, netIn)"
        :info="netInInfo"
      >
        <template #headerExtend>
          <div class="card-links">
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
          ref="netIn"
          :tips="$t('instanceIndex.activeSessionQtyTips')"
          :rangeSelect="true"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.flowIn"
          :xData="metricsData.time"
          :unit="'MB'"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.network.out')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, netOut)"
        :info="netOutInfo"
      >
        <LazyLine
          ref="netOut"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.flowOut"
          :xData="metricsData.time"
          :unit="'MB'"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <!-- <el-col :span="8">
      <my-card :title="$t('resourceMonitor.network.lost')" height="300" :bodyPadding="false">
        <LazyLine
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.lost"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
        />
      </my-card>
    </el-col> -->
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.network.lostIn')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, lostIn)"
        :info="lostInInfo"
      >
        <LazyLine
          ref="lostIn"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.lostIn"
          :xData="metricsData.time"
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
        :title="$t('resourceMonitor.network.lostOut')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, lostOut)"
        :info="lostOutInfo"
      >
        <LazyLine
          ref="lostOut"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.lostOut"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.network.errIn')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, errIn)"
        :info="errInInfo"
      >
        <LazyLine
          ref="errIn"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.errIn"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.network.errOut')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, errOut)"
        :info="errOutInfo"
      >
        <LazyLine
          ref="errOut"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.errOut"
          :xData="metricsData.time"
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
        :title="$t('resourceMonitor.network.connection')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, connection)"
        :info="connectionInfo"
      >
        <LazyLine
          ref="connection"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.networkSocket"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.network.tcpQty')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, tcpQty)"
        :info="tcpInfo"
      >
        <LazyLine
          ref="tcpQty"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.tcpSocket"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card
        :title="$t('resourceMonitor.network.udpQty')"
        height="300"
        :bodyPadding="false"
        :showBtns="true"
        @download="(title) => download(title, UDPQty)"
        :info="udpInfo"
      >
        <LazyLine
          ref="UDPQty"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.udpSocket"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
          :isLinkage="isLinkage"
        />
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <my-card
    :title="$t('resourceMonitor.network.card')"
    :bodyPadding="false"
    skipBodyHeight
    showQuestion
    :info="tableInfo"
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
    >
      <el-table-column prop="device" label="IFACE" />
      <el-table-column prop="NETWORK_RXPCK" label="rxpck/s" />
      <el-table-column prop="NETWORK_TXPCK" label="txpck/s" />
      <el-table-column prop="NETWORK_RX" label="rx(KB)/s" />
      <el-table-column prop="NETWORK_TX" label="tx(KB)/s" />
      <el-table-column prop="NETWORK_RXERR" label="rxerr/s" />
      <el-table-column prop="NETWORK_TXERR" label="txerr/s" />
      <el-table-column prop="NETWORK_RXDROP" label="rxdrop/s" />
      <el-table-column prop="NETWORK_TXDROP" label="txdrop/s" />
      <el-table-column prop="NETWORK_RXFIFO" label="rxfifo/s" />
      <el-table-column prop="NETWORK_TXFIFO" label="txfifo/s" />
    </el-table>
  </my-card>
</template>

<script setup lang="ts">
import LazyLine from "@/components/echarts/LazyLine.vue";
import { useMonitorStore } from "@/store/monitor";
import { toFixed } from "@/shared";
import { storeToRefs } from "pinia";
import { getNetworkMetrics } from "@/api/observability";
import { useIntervalTime } from "@/hooks/time";
import { tabKeys } from "@/pages/dashboardV2/common";
import { useRequest } from "vue-request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { hasSQLDiagnosisModule } from "@/api/sqlDiagnosis";
import { getWDRSnapshot } from "@/api/wdr";
import moment from "moment";
import { useParamsStore } from "@/store/params";

const props = withDefaults(defineProps<{ tabId: string }>(), {});
const { t } = useI18n();

const emit = defineEmits(["goto"]);

interface LineData {
  name: string;
  data: any[];
  [other: string]: any;
}
interface MetricsData {
  flowIn: LineData[];
  flowOut: LineData[];
  lost: LineData[];
  lostIn: LineData[];
  lostOut: LineData[];
  errIn: LineData[];
  errOut: LineData[];
  networkSocket: LineData[];
  tcpSocket: LineData[];
  udpSocket: LineData[];
  table: any[];
  time: string[];
}
const defaultData = {
  flowIn: [],
  flowOut: [],
  lost: [],
  lostIn: [],
  lostOut: [],
  errIn: [],
  errOut: [],
  networkSocket: [],
  tcpSocket: [],
  udpSocket: [],
  table: [],
  time: [],
};
const metricsData = ref<MetricsData>(defaultData);

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
    if (tabNow.value === tabKeys.ResourceMonitorNetwork) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) {
        clearData()
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
  metricsData.value.flowIn = [];
  metricsData.value.flowOut = [];
  metricsData.value.lost = [];
  metricsData.value.lostIn = [];
  metricsData.value.lostOut = [];
  metricsData.value.errIn = [];
  metricsData.value.errOut = [];
  metricsData.value.networkSocket = [];
  metricsData.value.tcpSocket = [];
  metricsData.value.udpSocket = [];
  metricsData.value.table = [];
  metricsData.value.time = [];
}

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
  if (!instanceId.value) return;
  requestData(props.tabId);
};
const { data: indexData, run: requestData } = useRequest(getNetworkMetrics, {
  manual: true,
  onError: () => {
    clearData()
  }
});
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.flowIn = [];
    metricsData.value.flowOut = [];
    metricsData.value.lost = [];
    metricsData.value.lostIn = [];
    metricsData.value.lostOut = [];
    metricsData.value.errIn = [];
    metricsData.value.errOut = [];
    metricsData.value.networkSocket = [];
    metricsData.value.tcpSocket = [];
    metricsData.value.udpSocket = [];
    metricsData.value.table = [];
    metricsData.value.time = [];

    const baseData = indexData.value;
    if (!baseData) return;

    // network in
    for (let key in baseData.NETWORK_IN) {
      let tempData: string[] = [];
      baseData.NETWORK_IN[key]?.forEach((element) => {
        tempData.push(kbyteToMB(element));
      });
      metricsData.value.flowIn.push({ data: tempData, name: key });
    }

    // network out
    for (let key in baseData.NETWORK_OUT) {
      let tempData: string[] = [];
      baseData.NETWORK_OUT[key]?.forEach((element) => {
        tempData.push(kbyteToMB(element));
      });
      metricsData.value.flowOut.push({ data: tempData, name: key });
    }

    // lost
    for (let key in baseData.NETWORK_LOST_PACKAGE) {
      let tempData: string[] = [];
      baseData.NETWORK_LOST_PACKAGE[key]?.forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.lost.push({ data: tempData, name: key });
    }
    for (let key in baseData.NETWORK_LOST_PACKAGE_IN) {
      let tempData: string[] = [];
      baseData.NETWORK_LOST_PACKAGE_IN[key]?.forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.lostIn.push({ data: tempData, name: key });
    }
    for (let key in baseData.NETWORK_LOST_PACKAGE_OUT) {
      let tempData: string[] = [];
      baseData.NETWORK_LOST_PACKAGE_OUT[key]?.forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.lostOut.push({ data: tempData, name: key });
    }
    for (let key in baseData.NETWORK_ERR_PACKAGE_IN) {
      let tempData: string[] = [];
      baseData.NETWORK_ERR_PACKAGE_IN[key]?.forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.errIn.push({ data: tempData, name: key });
    }
    for (let key in baseData.NETWORK_ERR_PACKAGE_OUT) {
      let tempData: string[] = [];
      baseData.NETWORK_ERR_PACKAGE_OUT[key]?.forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.errOut.push({ data: tempData, name: key });
    }

    // connection
    {
      let tempData: string[] = [];
      baseData.NETWORK_TCP_ALLOC?.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.networkSocket.push({ data: tempData, name: "tcpalloc" });
    }
    {
      let tempData: string[] = [];
      baseData.NETWORK_CURRESTAB?.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.networkSocket.push({ data: tempData, name: "currestab" });
    }
    {
      let tempData: string[] = [];
      baseData.NETWORK_TCP_INSEGS?.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.networkSocket.push({ data: tempData, name: "tcpinsegs" });
    }
    {
      let tempData: string[] = [];
      baseData.NETWORK_TCP_OUTSEGS?.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.networkSocket.push({ data: tempData, name: "tcpooutsegs" });
    }

    // tcp socket
    for (let key in baseData.NETWORK_TCP_SOCKET) {
      let tempData: string[] = [];
      baseData.NETWORK_TCP_SOCKET[key]?.forEach((element) => {
        tempData.push(toFixed(element));
      });
      metricsData.value.tcpSocket.push({ data: tempData, name: key });
    }

    // udp socket
    {
      let tempData: string[] = [];
      baseData.NETWORK_UDP_SOCKET?.forEach((d: number) => {
        tempData.push(toFixed(d));
      });
      metricsData.value.udpSocket.push({ data: tempData, name: "UDP sockets" });
    }

    for (let index = 0; index < baseData.table.length; index++) {
      const element = baseData.table[index];
      element.NETWORK_RXPCK = toFixed(element.NETWORK_RXPCK);
      element.NETWORK_TXPCK = toFixed(element.NETWORK_TXPCK);
      element.NETWORK_RX = toFixed(element.NETWORK_RX);
      element.NETWORK_TX = toFixed(element.NETWORK_TX);
      element.NETWORK_RXERR = toFixed(element.NETWORK_RXERR);
      element.NETWORK_TXERR = toFixed(element.NETWORK_TXERR);
      element.NETWORK_RXDROP = toFixed(element.NETWORK_RXDROP);
      element.NETWORK_TXDROP = toFixed(element.NETWORK_TXDROP);
      element.NETWORK_RXFIFO = toFixed(element.NETWORK_RXFIFO);
      element.NETWORK_TXFIFO = toFixed(element.NETWORK_TXFIFO);
    }
    metricsData.value.table = baseData.table;

    metricsData.value.time = baseData.time;
  },
  { deep: true }
);
const kbyteToMB = (val: number) => {
  return (val / 1024).toFixed(4);
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

const netIn = ref();
const netOut = ref();
const lostIn = ref();
const lostOut = ref();
const errIn = ref();
const connection = ref();
const errOut = ref();
const tcpQty = ref();
const UDPQty = ref();
const download = (title: string, ref: any) => {
  ref.download(title);
};

const netInInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.in'), value: t('resourceMonitor.network.inContent') }
  ]
})
const netOutInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.out'), value: t('resourceMonitor.network.outContent') }
  ]
})
const lostInInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.lostIn'), value: t('resourceMonitor.network.lostInContent') }
  ]
})
const lostOutInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.lostOut'), value: t('resourceMonitor.network.lostOutContent') }
  ]
})
const errInInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.errIn'), value: t('resourceMonitor.network.errInContent') }
  ]
})
const errOutInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.errOut'), value: t('resourceMonitor.network.errOutContent') }
  ]
})
const connectionInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [{ name: 'tcpalloc', value: t('resourceMonitor.network.tcpallocContent')},
  { name: 'currestab', value: t('resourceMonitor.network.currestabContent')},
  { name: 'tcpinsegs', value: t('resourceMonitor.network.tcpinsegsContent')},
  { name: 'tcpooutsegs', value: t('resourceMonitor.network.tcpooutsegsContent') }]
})
const tcpInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.tcpQty'), value: t('resourceMonitor.network.tcpContent') }
  ]
})
const udpInfo = ref<any>({
  title: t("app.lineOverview"),
  option: [
    { name: t('resourceMonitor.network.updQty'), value: t('resourceMonitor.network.updContent') }
  ]
})
const tableInfo = ref<any>({
  title: t("app.fieldOverview"),
  option: [
    { name: 'IFACE', value: t('resourceMonitor.network.ifaceContent') },
    { name: 'rxpck/s', value: t('resourceMonitor.network.rxpckContent') },
    { name: 'txpck/s', value: t('resourceMonitor.network.txpckContent') },
    { name: 'rx(KB)/s', value: t('resourceMonitor.network.rxContent') },
    { name: 'tx(KB)/s', value: t('resourceMonitor.network.txContent') },
    { name: 'rxerr/s', value: t('resourceMonitor.network.rxerrContent') },
    { name: 'txerr/s', value: t('resourceMonitor.network.txerrContent') },
    { name: 'rxdrop/s', value: t('resourceMonitor.network.rxdropContent') },
    { name: 'txdrop/s', value: t('resourceMonitor.network.txdropContent') },
    { name: 'rxfifo/s', value: t('resourceMonitor.network.rxfifoContent') },
    { name: 'txfifo/s', value: t('resourceMonitor.network.txfifoContent') },
  ]
})  
    
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
</style>
