<template>
  <a-modal :mask-closable="false" :visible="data.show" :title="data.title" :render-to-body="false"
    :unmount-on-close="true" :modal-style="{ width: '850px', height: '700px' }" :footer="false" @cancel="close">
    <div class="flex-col-start">
      <label class="mb-s">IP地址: {{ data.formData.ip }}</label>
      <div class="xterm-c">
        <div id="xterm" class="xterm"></div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">

import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { reactive, ref, nextTick } from 'vue'
import { hostSSH } from '@/api/ops'
import { WsConnectType } from '@/types/ops/install'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import { AttachAddon } from 'xterm-addon-attach'
import 'xterm/css/xterm.css'

const data = reactive<KeyValue>({
  show: false,
  loading: false,
  formData: {
    ip: '',
    sshPort: '',
    sshUsername: '',
    sshPassword: ''
  }
})

const terminalWs = ref<Socket<any, any> | undefined>()

const termTerminal = ref<Terminal>()

const close = () => {
  terminalWs.value?.destroy()
  if (termTerminal.value) {
    termTerminal.value.dispose()
  }
  data.show = false
}

const openSocket = () => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const terminalSocket = new Socket({ url: `${WsConnectType.SSH}/jdbc_terminal_${socketKey}` })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = Object.assign({}, {
      businessId: `jdbc_terminal_${socketKey}`
    }, data.formData)
    data.loading = true
    initTerm(term, terminalSocket.ws)
    hostSSH(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        term.writeln(res.msg)
        terminalSocket.destroy()
      }
    }).catch((error) => {
      term.writeln(error.toString())
      terminalSocket.destroy()
    }).finally(() => {
      data.loading = false
    })
  })
  terminalSocket.onclose(() => {
    console.log('jdbc terminal close')
  })
}

let fitAddon = ref<FitAddon | undefined>()

const initTerm = (term: Terminal, ws: WebSocket | undefined) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    fitAddon.value = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon.value)
    term.open(document.getElementById('xterm') as HTMLElement)
    fitAddon.value.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
  }
}

const getTermObj = (): Terminal => {
  return new Terminal({
    // rendererType: 'dom',
    fontSize: 14,
    rows: 40,
    cols: 90,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    // lineHeight: 2,
    // letterSpacing: 5,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  })
}

const open = (sshData: KeyValue) => {
  data.show = true
  data.title = '终端'
  Object.assign(data.formData, {
    ip: sshData.ip,
    sshPort: sshData.sshPort,
    sshUsername: sshData.sshUsername,
    sshPassword: sshData.sshPassword
  })
  nextTick(() => {
    openSocket()
  })
}

defineExpose({
  open
})

</script>

<style lang="less" scoped>
.xterm-c {
  width: 810px;
  height: 580px;
}

.xterm {
  width: 100%;
  height: 100%;
}
</style>
