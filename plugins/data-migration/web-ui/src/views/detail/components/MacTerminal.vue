<template>
  <div>
    <el-drawer v-model="visible" size="50%" :footer="false" destroy-on-close>
      <template #title>
        <div class="title-con">
          <span>{{ $t('components.MacTerminal.5q08wsv861k0') }}:
            {{ props.host.runHost }}</span>
        </div>
      </template>
      <template #default>
        <div class="term-con">
          <div id="xterm" class="xterm"></div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import Wsocket from '@/utils/websocket';
import { openSSH } from '@/api/detail';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import { AttachAddon } from 'xterm-addon-attach';
import 'xterm/css/xterm.css';

const props = defineProps({
  open: Boolean,
  host: Object
});

const emits = defineEmits(['update:open']);

const visible = ref(false);

const terminalWs = ref();
const termTerminal = ref();
const fitAddon = ref();

watch(visible, (v) => {
  emits('update:open', v);
});

watch(
  () => props.open,
  (v) => {
    if (v) {
      handleConnect();
    }
    visible.value = v;
  }
);

const handleConnect = async () => {
  const term = getTerminalInstance();
  const socketKey = new Date().getTime();
  const terminalSocket = new Wsocket({ url: `SSH/jdbc_terminal_${socketKey}`, type: 'websocket' });
  terminalWs.value = terminalSocket;
  terminalSocket.onopen(() => {
    const param = {
      businessId: `jdbc_terminal_${socketKey}`,
      ip: props.host.runHost,
      sshPassword: props.host.runPass,
      sshPort: props.host.runPort,
      sshUsername: props.host.runUser
    };
    initTerm(term, terminalSocket.ws);
    openSSH(props.host?.runHostId, param)
      .then((res) => {
        if (res.code !== 200) {
          term.writeln(res.msg);
          terminalSocket.destroy();
        }
      })
      .catch((error) => {
        term.writeln(error.toString());
        terminalSocket.destroy();
      });
  });
  terminalSocket.onclose(() => {
    console.log('jdbc terminal close');
  });
};

const initTerm = (term, ws) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws);
    fitAddon.value = new FitAddon();
    term.loadAddon(attachAddon);
    term.loadAddon(fitAddon.value);
    term.open(document.getElementById('xterm'));
    fitAddon.value.fit();
    term.clear();
    term.focus();
    term.write('\r\n\x1b[33m$\x1b[0m ');
    termTerminal.value = term;
  }
};

const getTerminalInstance = () =>
  new Terminal({
    fontSize: 14,
    rows: 73,
    cols: 200,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    },
  });

onBeforeUnmount(() => {
  terminalWs.value?.destroy();
  termTerminal.value && termTerminal.value.dispose();
});

onMounted(() => {
  visible.value = props.open;
});
</script>

<style lang="less" scoped>
:deep(.el-drawer__header) {
  align-items: start;
  height: 54px;
  color: #191919;
  font-weight: 600;
  margin-bottom: 0;
  border-bottom: 1px solid var(--o-border-color-light)
}

.term-con {
  padding: 4px;
  background-color: #000;
  border-radius: 4px;

  .xterm {
    width: 100%;
    height: 100%;
  }
}
</style>
