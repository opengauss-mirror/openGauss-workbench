<template>
  <a-row justify="center">
    <a-alert class="mb tip" type="warning" v-if="status === STATUS.INSTALLING">
      {{ $t("dtCluster.installing") }}
    </a-alert>
    <a-alert class="mb tip" type="error" v-if="status === STATUS.FAILURE">
      {{ $t("dtCluster.installFailure") }}
    </a-alert>
  </a-row>
  <a-row justify="center" :gutter="20">
    <a-col :span="12" class="logterm-item">
      <div class="logterm-title">{{ $t("dtCluster.primary") }}{{ $t("dtCluster.cluster") }}</div>
      <div id="dt-cluster-xtermLog_primary" class="logterm"></div>
    </a-col>
    <a-col :span="12" class="logterm-item"
      ><div class="logterm-title">{{ $t("dtCluster.standby") }}{{ $t("dtCluster.cluster") }}</div>
      <div id="dt-cluster-xtermLog_standby" class="logterm"></div>
    </a-col>
  </a-row>
</template>
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import "xterm/css/xterm.css";
import { Terminal } from "xterm";
import { FitAddon } from "@xterm/addon-fit";
import { KeyValue } from "@/types/global";
import Socket from "@/utils/websocket";
import { STATUS } from "./constant";
import { apiInstallDtCluster } from "@/api/resource/dtCluster";

const props = defineProps<{
  installParams: any;
  status: string;
  logs: any;
}>();

const emit = defineEmits(["updateStatus"]);

const logSockets = ref<Socket<any, any>[]>([]);
const logTerms = ref<Terminal[]>([]);

onMounted(() => {
  openLogSocket();
});

onBeforeUnmount(() => {
  destroySocketsAndterm();
});

const install = () => {
  destroySocketsAndterm();
  openLogSocket();
};

const destroySocketsAndterm = () => {
  logSockets.value.forEach((x) => x.destroy());
  logTerms.value.forEach((x) => x.dispose());
};

const initLogSocket = (url: string, elId: string) => {
  const term = new Terminal({
    fontSize: 14,
    rows: 30,
    cols: 100,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: "underline",
    theme: {
      background: "black",
    },
  });
  const logSocket: Socket<any, any> = new Socket({ url });
  logSockets.value.push(logSocket);
  logTerms.value.push(term);

  logSocket.onmessage((messageData: any) => {
    term.writeln(`\x1b[2K\r${messageData}`);
    props.logs[url] += messageData + "\r\n";
    if (messageData.indexOf("FINAL_EXECUTE_EXIT_CODE") > -1) {
      const flag = Number(messageData.split(":")[1]);
      if (flag === 0) {
        emit("updateStatus", STATUS.SUCCESS);
        logSocket.destroy();
        term.dispose();
      } else {
        emit("updateStatus", STATUS.FAILURE);
        logSocket.destroy();
      }
    }
  });

  initLogTerm(term, logSocket.ws, elId);
  return new Promise((resolve) => {
    logSocket.onopen(() => {
      props.logs[url] = "";
      resolve(0);
    });
  });
};

const openLogSocket = () => {
  Promise.all([
    initLogSocket(props.installParams.primaryBusinessId, "dt-cluster-xtermLog_primary"),
    initLogSocket(props.installParams.standbyBusinessId, "dt-cluster-xtermLog_standby"),
  ]).then(
    () => {
      return apiInstallDtCluster(props.installParams)
        .then((res: KeyValue) => {
          if (res?.code != 200) {
            logTerms.value.forEach((x) => x.writeln(res?.msg || "network error"));
            logSockets.value.forEach((x) => x.destroy());
            emit("updateStatus", STATUS.FAILURE);
          }
        })
        .catch((error) => {
          logTerms.value.forEach((x) => x.writeln(error.toString() || "network error"));
          logSockets.value.forEach((x) => x.destroy());
          emit("updateStatus", STATUS.FAILURE);
        });
    },
    () => {
      logTerms.value.forEach((x) => x.writeln("network error"));
      logSockets.value.forEach((x) => x.destroy());
      emit("updateStatus", STATUS.FAILURE);
    }
  );
};

const initLogTerm = (term: Terminal, ws: WebSocket | undefined, elId: string) => {
  if (ws) {
    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById(elId) as HTMLElement);
    fitAddon.fit();
    term.clear();
    term.focus();
  }
};

defineExpose({ install });
</script>

<style lang="less" scoped>
.logterm {
  width: 100%;
  height: 100%;
}

.logterm-title {
  color: var(--color-text-1);
}

.tip {
  width: fit-content;
}
</style>
