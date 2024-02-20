<template>
  <div class="dt-cluster-item">
    <div v-if="data.disasterClusterName">
      <a-row class="mb" style="font-size: 18px"
        >{{ $t("dtCluster.dtClusterName") }}: {{ data.disasterClusterName }}</a-row
      >
      <a-row>
        <a-col :span="12" class="dt-cluster__info">
          <div class="flex-row">
            <svg-icon icon-class="ops-cluster" class="icon-size mr"></svg-icon>
            <div class="flex-col-start">
              <div class="flex-row mr mb-s info-row">
                <div class="label-color mr-s">{{ $t("dtCluster.currentStatus") }}:</div>
                <a-tag :color="getClusterStateColor()">{{ getClusterState() }}</a-tag>
              </div>
              <div class="flex-row mr mb-s info-row">
                <div class="label-color mr-s">{{ $t("dtCluster.dtClusterType") }}:</div>
                {{ $t("dtCluster.resourcePoolingDt") }}
              </div>
              <div class="flex-row mr mb-s info-row">
                <div class="label-color mr-s">{{ $t("dtCluster.replicationType") }}:</div>
                {{ $t("dtCluster.storageReplication") }}
              </div>
            </div>
          </div>
        </a-col>
        <a-col :span="12" class="flex-col-start">
          <div class="label-color mb">
            {{ $t("dtCluster.operations") }}
          </div>
          <div class="flex-row">
            <a-button type="outline" class="mr" @click="switchover()">{{
              $t("dtCluster.switchover")
            }}</a-button>
            <a-button type="outline" class="mr" @click="releaseDt()">{{
              $t("dtCluster.releaseDt")
            }}</a-button>
          </div>
        </a-col>
      </a-row>
      <a-row class="cluster-list" :gutter="20">
        <a-col :span="12" v-for="(instance, nodeIndex) in data.subClusters" :key="nodeIndex">
          <ClusterItem :data="instance" />
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from "@/types/global";
import { onMounted, onBeforeUnmount, ref, watch } from "vue";
import { useWinBox } from "vue-winbox";
import "xterm/css/xterm.css";
import { Terminal } from "xterm";
import { FitAddon } from "@xterm/addon-fit";
import { AttachAddon } from "@xterm/addon-attach";
import Socket from "@/utils/websocket";
import ClusterItem from "./ClusterItem.vue";
import { useI18n } from "vue-i18n";
import { CLUSTER_STATE, DT_CLUSTER_STATE } from "./constant";
import { apiMonitorDtCluster, apiSwitchover, apiRleaseDtCluster } from "@/api/resource/dtCluster";

const { t } = useI18n();

const props = defineProps({
  data: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["refresh"]);

const socket = ref<any>(null);
const dtClusterState = ref<string>("");
const switchoverTerms = ref<Terminal[]>([]);
const switchoverWebsockets = ref<Socket<any, any>[]>([]);

onMounted(() => {
  socket.value?.destroy();
  openWebSocket();
});

watch(
  () => props.data,
  () => {
    socket.value?.destroy();
    openWebSocket();
  }
);

onBeforeUnmount(() => {
  socket.value?.destroy();
  switchoverTerms.value?.forEach((x) => x.dispose());
  switchoverWebsockets.value?.forEach((x) => x.destroy());
});

const openWebSocket = () => {
  const businessId = `disasterCluster_monitor_${props.data.disasterClusterName}_${Date.now()}`;
  const websocket = new Socket({ url: businessId });
  websocket.onopen(() => {
    apiMonitorDtCluster(props.data.disasterClusterName, businessId)
      .then((res: KeyValue) => {
        if (Number(res.code) == 200) {
          socket.value = websocket;
        } else {
          dtClusterState.value = DT_CLUSTER_STATE.UNKWON;
          props.data.subClusters[0].state = CLUSTER_STATE.UNKWON;
          props.data.subClusters[1].state = CLUSTER_STATE.UNKWON;
          websocket.destroy();
        }
      })
      .catch(() => {
        dtClusterState.value = DT_CLUSTER_STATE.UNKWON;
        props.data.subClusters[0].state = CLUSTER_STATE.UNKWON;
        props.data.subClusters[1].state = CLUSTER_STATE.UNKWON;
        websocket.destroy();
      });
  });
  websocket.onclose(() => {
    dtClusterState.value = DT_CLUSTER_STATE.UNKWON;
    props.data.subClusters[0].state = CLUSTER_STATE.UNKWON;
    props.data.subClusters[1].state = CLUSTER_STATE.UNKWON;
  });
  websocket.onmessage((message: any) => {
    try {
      const messageData = JSON.parse(message);
      dtClusterState.value = messageData.disasterClusterState;
      props.data.subClusters[0].state = messageData.primaryClusterState;
      props.data.subClusters[1].state = messageData.standbyClusterState;
    } catch (e) {}
  });
};

const initSwitchoverSocket = (url: string, elId: string, isRefresh: boolean = false) => {
  const term = getTermObj(30, 50);
  const webSocket = new Socket({
    url,
  });

  switchoverTerms.value.push(term);
  switchoverWebsockets.value.push(webSocket);

  initTerm(term, webSocket, elId);

  webSocket.onmessage((messageData: any) => {
    term.writeln("");
    if (messageData.indexOf("FINAL_EXECUTE_EXIT_CODE") > -1) {
      webSocket.destroy();
      const flag = Number(messageData.split(":")[1]);
      if (flag === 0) {
        term.writeln("switchover success.");
      } else {
        term.writeln("switchover failed.");
      }
      if (isRefresh) {
        emit("refresh");
      }
    }
  });

  return new Promise((resolve) => {
    webSocket.onopen(() => {
      resolve(0);
    });
  });
};

const switchover = () => {
  const switchoverTermId = `dt-cluster-switchover-term_${Date.now()}`;

  createWinbox(
    switchoverTermId,
    t("dtCluster.switchover"),
    "60%",
    "60%",
    `<div style="display:flex;width:100%;height:100%;"><div id=${`${switchoverTermId}_primary`} style="width:50%;height:100%;"></div><div id=${`${switchoverTermId}_standby`} style="width:50%;height:100%;"></div></div>`
  );

  const primaryBusinessId = `disasterCluster_switchover_${
    props.data.disasterClusterName
  }_primary_${Date.now()}`;
  const primaryElId = `${switchoverTermId}_primary`;

  const standbyBusinessId = `disasterCluster_switchover_${
    props.data.disasterClusterName
  }_standby_${Date.now()}`;
  const standbyElId = `${switchoverTermId}_standby`;

  Promise.all([
    initSwitchoverSocket(primaryBusinessId, primaryElId, true),
    initSwitchoverSocket(standbyBusinessId, standbyElId),
  ]).then(() => {
    const param = {
      clusterId: props.data.disasterClusterName,
      primaryBusinessId,
      standbyBusinessId,
    };
    apiSwitchover(param)
      .then((res: KeyValue) => {
        if (Number(res.code) != 200) {
          switchoverTerms.value?.forEach((x) => x.dispose());
          switchoverWebsockets.value?.forEach((x) => x.destroy());
        }
      })
      .catch(() => {
        switchoverTerms.value?.forEach((x) => x.dispose());
        switchoverWebsockets.value?.forEach((x) => x.destroy());
      });
  });
};

const releaseDt = () => {
  const term = getTermObj();
  const businessId = `disasterCluster_delete_${props.data.disasterClusterName}_${Date.now()}`;
  const webSocket = new Socket({
    url: businessId,
  });

  const elId = `dt-cluster-releasedt-term_${Date.now()}`;

  createWinbox(elId, t("dtCluster.releaseDt"));

  initTerm(term, webSocket, elId);

  webSocket.onopen(() => {
    const param = {
      clusterId: props.data.disasterClusterName,
      businessId,
    };
    apiRleaseDtCluster(param)
      .then((res: KeyValue) => {
        if (Number(res.code) != 200) {
          webSocket.destroy();
        }
      })
      .catch(() => {
        webSocket.destroy();
      });
  });

  webSocket.onmessage((messageData: any) => {
    term.writeln("");
    if (messageData.indexOf("FINAL_EXECUTE_EXIT_CODE") > -1) {
      webSocket.destroy();
      const flag = Number(messageData.split(":")[1]);
      if (flag === 0) {
        term.writeln("operation success");
      } else {
        term.writeln("operation failed");
      }
      emit("refresh");
    }
  });
};

const getTermObj = (rows: number = 40, cols: number = 200): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows,
    cols,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: "underline",
    theme: {
      background: "black",
    },
  });
};

const initTerm = (term: Terminal, socket: Socket<any, any> | null, xtermId: string) => {
  if (socket) {
    const attachAddon = new AttachAddon(socket.ws);
    const fitAddon = new FitAddon();
    term.loadAddon(attachAddon);
    term.loadAddon(fitAddon);
    term.open(document.getElementById(xtermId) as HTMLElement);
    fitAddon.fit();
    term.clear();
    term.focus();
  } else {
    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById(xtermId) as HTMLElement);
    fitAddon.fit();
    term.clear();
    term.focus();
  }
};

const createWinbox = (
  elId: string,
  title: string,
  width: string = "50%",
  height: string = "50%",
  html: string = ""
) => {
  const createWindow = useWinBox();
  createWindow({
    id: elId,
    html,
    title,
    mount: document.getElementById(elId),
    class: ["custom-winbox", "no-full", "no-max"],
    background: "#1D2129",
    x: "center",
    y: "center",
    width,
    height,
    modal: true,
    onClose: function () {},
    onminimize: function () {
      const oldClass = this.window?.getAttribute("class");
      if (oldClass) {
        const newClass = oldClass.replace("custom-winbox", "custom-winbox-mini");
        this.window?.setAttribute("class", newClass);
      }
    },
    onrestore: function () {
      const oldClass = this.window?.getAttribute("class");
      if (oldClass) {
        const newClass = oldClass.replace("custom-winbox-mini", "custom-winbox");
        this.window?.setAttribute("class", newClass);
      }
    },
  });
};
const getClusterState = () => {
  switch (dtClusterState.value?.toUpperCase()) {
    case DT_CLUSTER_STATE.ARCHIVE:
      return t("dtCluster.archive");
    case DT_CLUSTER_STATE.FULL_BACKUP:
      return t("dtCluster.fullBackup");
    case DT_CLUSTER_STATE.BACKUP_FAIL:
      return t("dtCluster.backupFail");
    case DT_CLUSTER_STATE.ARCHIVE_FAIL:
      return t("dtCluster.archiveFail");
    case DT_CLUSTER_STATE.SWITCHOVER:
      return t("dtCluster.switchovering");
    case DT_CLUSTER_STATE.RESTORE:
      return t("dtCluster.restore");
    case DT_CLUSTER_STATE.RESTORE_FAIL:
      return t("dtCluster.restoreFail");
    case DT_CLUSTER_STATE.RECOVERY:
      return t("dtCluster.recovery");
    case DT_CLUSTER_STATE.RECOVERY_FAIL:
      return t("dtCluster.recoveryFail");
    case DT_CLUSTER_STATE.PROMOTE:
      return t("dtCluster.promote");
    case DT_CLUSTER_STATE.PROMOTE_FAIL:
      return t("dtCluster.promoteFail");
    default:
      return t("dtCluster.unknown");
  }
};

const getClusterStateColor = () => {
  switch (dtClusterState.value?.toUpperCase()) {
    case DT_CLUSTER_STATE.ARCHIVE:
    case DT_CLUSTER_STATE.FULL_BACKUP:
    case DT_CLUSTER_STATE.SWITCHOVER:
    case DT_CLUSTER_STATE.RESTORE:
    case DT_CLUSTER_STATE.RECOVERY:
    case DT_CLUSTER_STATE.PROMOTE:
      return "green";
    case DT_CLUSTER_STATE.BACKUP_FAIL:
    case DT_CLUSTER_STATE.ARCHIVE_FAIL:
    case DT_CLUSTER_STATE.RESTORE_FAIL:
    case DT_CLUSTER_STATE.RECOVERY_FAIL:
    case DT_CLUSTER_STATE.PROMOTE_FAIL:
      return "red";
    default:
      return "gray";
  }
};
</script>

<style lang="less" scoped>
.dt-cluster-item {
  padding: 20px 0;
  &:nth-of-type(n + 2) {
    border-top: 2px solid var(--color-fill-3);
  }

  .info-row {
    height: 28px;
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }

  .cluster-list {
    margin-top: 40px;
  }
}
</style>
