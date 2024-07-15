<template>
  <IndexBar :tabId="props.tabId"></IndexBar>
  <div style="margin-bottom: 0px"></div>
  <div class="top-sql">
    <el-tabs v-model="typeTab" class="tab2">
      <el-tab-pane label="DB_TIME" name="db_time" />
      <el-tab-pane label="CPU_TIME" name="cpu_time" />
      <el-tab-pane label="EXEC_TIME" name="execution_time" />
      <el-tab-pane label="IO_TIME" name="data_io_time" />
    </el-tabs>
    <div class="top-sql-table" v-if="!errorInfo" v-loading="loading">
      <el-table :data="data.tableData" border>
        <el-table-column label="SQLID" width="150">
          <template #default="scope">
            <el-link type="primary" @click="gotoTopsqlDetail(scope.row.debugQueryId)">
              {{ scope.row.debugQueryId }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('sql.dbName')"
          prop="dbName"
          width="90"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column
          :label="$t('sql.schemaName')"
          prop="schemaName"
          width="100"
          show-overflow-tooltip
        >
        </el-table-column>
        <el-table-column
          :label="$t('sql.userName')"
          prop="userName"
          width="80"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column
          :label="$t('sql.applicationName')"
          prop="applicationName"
          width="100"
          show-overflow-tooltip
        >
        </el-table-column>
        <el-table-column prop="query" label="SQL" show-overflow-tooltip />
        <el-table-column
          :label="$t('sql.startTime')"
          prop="startTime"
          width="140"
        >
        </el-table-column>
        <el-table-column
          :label="$t('sql.finishTime')"
          prop="finishTime"
          width="140"
        >
        </el-table-column>
        <el-table-column
          :label="$t('sql.dbTime')"
          prop="dbTime"
          :formatter="(r: any) => Math.round(r.dbTime / 1000)"
          width="80"
        ></el-table-column>
        <el-table-column
          :label="$t('sql.cpuTime')"
          prop="cpuTime"
          :formatter="(r: any) => Math.round(r.cpuTime / 1000)"
          width="90"
        ></el-table-column>
        <el-table-column
          :label="$t('sql.excutionTime')"
          prop="executionTime"
          :formatter="(r: any) => Math.round(r.executionTime / 1000)"
          width="90"
        ></el-table-column>
        <el-table-column
          label="IO_TIME(ms)"
          prop="dataIoTime"
          :formatter="(r: any) => Math.round(r.dataIoTime / 1000)"
          width="80"
        ></el-table-column>
      </el-table>
    </div>
    <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
  </div>
</template>

<script setup lang="ts">
import { useRequest } from "vue-request";
import moment from "moment";
import { useIntervalTime } from "@/hooks/time";
import { useMonitorStore } from "@/store/monitor";
import { storeToRefs } from "pinia";
import ogRequest from "@/request";
import router from "@/router";
import { useI18n } from "vue-i18n";
import { tabKeys } from "@/pages/dashboardV2/common";
import { dateFormat } from "@/utils/date";

const { t } = useI18n();

const typeTab = ref("db_time");
const errorInfo = ref<string | Error>();

const props = withDefaults(defineProps<{ tabId: string }>(), {});
const monitorStore = useMonitorStore(props.tabId);
const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId } = storeToRefs(
  monitorStore
);
const { culRangeTimeAndStep } = monitorStore;

const data = reactive<{
  tableData: Array<Record<string, string>>;
}>({
  tableData: [],
});

const getParam = () => {
  return {
    dbid: instanceId,
    startTime: dateFormat(new Date(culRangeTimeAndStep()[0] * 1000)),
    finishTime: dateFormat(new Date(culRangeTimeAndStep()[1] * 1000)),
  };
};

const outsideGoto = (key: string, param: any) => {
  if (param && param.key === tabKeys.InstanceMonitorTOPSQLCPUTime)
    typeTab.value = "cpu_time";
  if (param && param.key === tabKeys.InstanceMonitorTOPSQLIOTime)
    typeTab.value = "data_io_time";
};
defineExpose({ outsideGoto });

// same for every page in index
const timer = ref<number>();
onMounted(() => {
  load();
});
watch(
  updateCounter,
  () => {
    clearInterval(timer.value);
    if (tabNow.value === tabKeys.InstanceMonitorTOPSQL) {
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

watch(typeTab, () => {
  load();
});
const load = () => {
  data.tableData = [];
  requestData(getParam());
};
const { run: requestData, loading } = useRequest(
  (query) => {
    ogRequest
      .get(
        `/observability/v1/topsql/list?id=${query.dbid}&startTime=${query.startTime}&finishTime=${query.finishTime}&orderField=${typeTab.value}`
      )
      .then((r: any) => {
        data.tableData = r
      }).catch((e: any) => {
        if (typeof(e) === 'string') {
          errorInfo.value = e;
        }
        const code = e?.data?.code;
        if (code === 602) {
          errorInfo.value = t("dashboard.topsqlListTip");
        }
      });
  },
  { manual: true }
);

const gotoTopsqlDetail = (id: string) => {
  const curMode = localStorage.getItem("INSTANCE_CURRENT_MODE");
  if (curMode === "wujie") {
    // @ts-ignore plug-in components
    window.$wujie?.props.methods.jump({
      name: `Static-pluginObservability-instanceVemSql_detail`,
      query: {
        dbid: getParam().dbid.value,
        id,
      },
    });
  } else {
    // local
    window.sessionStorage.setItem("sqlId", id);
    router.push(`/vem/sql_detail/${getParam().dbid.value}/${id}`);
  }
};
</script>

<style scoped lang="scss">
.top-sql {
  &:deep(.el-tabs__header) {
    width: 100%;
  }

  &-table-id {
    color: #0093ff;
    cursor: pointer;
  }
}
</style>
