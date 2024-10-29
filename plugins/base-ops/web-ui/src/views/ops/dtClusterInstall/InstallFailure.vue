<template>
  <a-row id="dtInstallFail">
    <div class="panel-w flex-col-start">
      <div class="flex-between mb">
        <a-select class="mr-s" style="width: 300px" v-model="hostIdx" @change="hostChange">
          <a-option
            v-for="(item, index) in hosts"
            :value="index"
            :label="`${item.clusterRole[0]} Cluster ${item.nodeRole[0]} Node ${item.privateIp}(${item.publicIp})`"
          >
          </a-option>
        </a-select>
        <div>
          <a-button type="primary" class="mr-s" @click="retry">{{
            $t("dtCluster.retry")
          }}</a-button>
          <a-button type="primary" @click="downloadLog"
            >{{ $t("dtCluster.downloadLog") }}
          </a-button>
        </div>
      </div>
      <div class="mb-s current_ip" v-show="hosts[hostIdx]?.privateIp">
        {{ hosts[hostIdx]?.privateIp }}({{ hosts[hostIdx]?.publicIp }})
      </div>
      <div id="dt-cluster-exe-term"></div>
    </div>
  </a-row>
</template>
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import "xterm/css/xterm.css";
import { Terminal } from "xterm";
import { FitAddon } from "@xterm/addon-fit";
import { AttachAddon } from "@xterm/addon-attach";
import Socket from "@/utils/websocket";
import { apiGetHosts, openSSH } from "@/api/resource/dtCluster";

const hosts = ref<any>([]);
const hostIdx = ref<any>(0);

const props = defineProps({
  installParams: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["retry", "downloadLog"]);

const terminalWs = ref<Socket<any, any> | undefined>();
const termTerminal = ref<Terminal>();

onMounted(() => {
  apiGetHosts(props.installParams.primaryClusterName, props.installParams.standbyClusterName).then(
    (res: any) => {
      if (res?.code == 200) {
        hosts.value = res.data;
        hostChange(0);
      }
    },
    () => {}
  );
});

onBeforeUnmount(() => {
  terminalWs.value?.destroy();
  termTerminal.value?.dispose();
});

const retry = () => {
  terminalWs.value?.destroy();
  termTerminal.value?.dispose();
  emit("retry");
};

const hostChange = (hostIdx: any) => {
  console.log("hostChange", hostIdx);
  if (terminalWs.value) {
    terminalWs.value?.destroy();
  }
  if (termTerminal.value) {
    termTerminal.value.dispose();
  }
  openSocket(hosts.value[hostIdx]);
};

const getTermObj = () =>
  new Terminal({
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

const openSocket = (hostItem: any) => {
  const businessId = `disaster-cluster-install-terminal_${Date.now()}`;
  const terminalSocket = new Socket({ url: businessId });
  terminalWs.value = terminalSocket;
  terminalSocket.onopen(async () => {
    const param = {
      hostId: hostItem?.hostId,
      rootPassword: hostItem?.password,
      businessId,
    };
    const term = getTermObj();
    initTerm(term, terminalSocket.ws);
    openSSH(param).then(
      (res: any) => {
        if (res?.code != 200) {
          term.writeln(res.msg);
          terminalSocket.destroy();
        }
      },
      (error) => {
        term.writeln(error.toString());
        terminalSocket.destroy();
      }
    );
  });
};

const initTerm = (term: any, ws: any) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws);
    const fitAddon = new FitAddon();
    term.loadAddon(attachAddon);
    term.loadAddon(fitAddon);
    term.open(document.getElementById("dt-cluster-exe-term") as HTMLElement);
    fitAddon.fit();
    term.clear();
    term.focus();
    term.write("\r\n\x1b[33m$\x1b[0m ");
    termTerminal.value = term;
  }
};

const downloadLog = () => {
  emit("downloadLog");
};
</script>

<style lang="less" scoped>
#dt-cluster-exe-term {
  width: 100%;
  height: 100%;
}

.current_ip {
  color: var(--color-text-1);
}
</style>
