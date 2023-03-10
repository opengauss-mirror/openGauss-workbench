<template>
  <a-drawer
    v-model:visible="visible"
    width="60%"
    :footer="false"
    :unmount-on-close="true"
  >
    <template #title>
      <div class="title-con">
        <span>执行机器：{{ props.host.runHost }}</span>
      </div>
    </template>
    <div class="term-con">
      <div id="xterm" class="xterm"></div>
    </div>
  </a-drawer>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import Wsocket from '@/utils/websocket'
import { encryptPassword } from '@/utils/jsencrypt'
import { openSSH } from '@/api/detail'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import { AttachAddon } from 'xterm-addon-attach'
import 'xterm/css/xterm.css'

const props = defineProps({
  open: Boolean,
  host: Object
})

const emits = defineEmits(['update:open'])

const visible = ref(false)

const terminalWs = ref()
const termTerminal = ref()
const fitAddon = ref()

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  if (v) {
    handleConnect()
  }
  visible.value = v
})

const handleConnect = async () => {
  const encryptPwd = await encryptPassword(props.host.runPass)
  const term = getTerminalInstance()
  const socketKey = new Date().getTime()
  const terminalSocket = new Wsocket({ url: `custom_terminal_${socketKey}` })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      hostId: props.host.runHostId,
      rootPassword: encryptPwd,
      wsConnectType: 'SSH',
      businessId: `custom_terminal_${socketKey}`
    }
    openSSH(param).then((res) => {
      if (res.code !== 200) {
        terminalSocket.destroy()
      }
    }).catch(() => {
      terminalSocket.destroy()
    })
    initTerm(term, terminalSocket.ws)
  })
}

const initTerm = (term, ws) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    fitAddon.value = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon.value)
    term.open(document.getElementById('xterm'))
    // fitAddon.value.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
  }
}

const getTerminalInstance = () => new Terminal({
  fontSize: 14,
  rows: 40,
  cols: 100,
  cursorBlink: true,
  convertEol: true,
  disableStdin: false,
  cursorStyle: 'underline',
  theme: {
    background: 'black'
  }
})

onBeforeUnmount(() => {
  terminalWs.value?.destroy()
  termTerminal.value && termTerminal.value.dispose()
})

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.term-con {
  .xterm {
    width: 100%;
    height: 100%;
  }
}
</style>
