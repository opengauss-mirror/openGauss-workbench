<template>
  <a-modal
    :mask-closable="false"
    :visible="data.show"
    :title="data.title"
    :render-to-body="false"
    :unmount-on-close="true"
    :modal-style="{ width: '70%' }"
    :footer="false"
    @cancel="close"
  >
    <div class="flex-col-start">
      <div class="flex-between mb-s full-w">
        <label class="mb-s">{{ $t('components.HostTerminal.else1') }} - {{ data.formData.ip }}</label>
        <div
          class="mb-s"
          style="color: red; font-weight: bold;"
        >
          {{ $t('components.HostTerminal.else2') }}
        </div>
      </div>
      <div
        id="xterm"
        class="xterm"
      ></div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">

import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { reactive, ref, nextTick, onBeforeUnmount } from 'vue'
import { openSSH } from '@/api/ops'
import { WsConnectType } from '@/types/ops/install'
import { Terminal } from 'xterm'
import { FitAddon } from '@xterm/addon-fit'
import { AttachAddon } from '@xterm/addon-attach'
import 'xterm/css/xterm.css'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from "@/utils/jsencrypt";
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  loading: false,
  formData: {
    hostId: '',
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
  const bid = `install_host_${socketKey}`
  const terminalSocket = new Socket({ url: bid })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      hostId: data.formData.hostId,
      rootPassword: data.formData.sshPassword,
      sshUsername: data.formData.sshUsername,
      wsConnectType: WsConnectType.SSH,
      businessId: bid
    }
    data.loading = true
    initTerm(term, terminalSocket.ws)
    openSSH(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        term.writeln(res.msg)
        terminalSocket.destroy()
      }
    }).catch((error: any) => {
      term.writeln(error.toString())
      terminalSocket.destroy()
    }).finally(() => {
      data.loading = false
    })
  })
}

let fitAddon = ref<FitAddon | undefined>()

const fitAddonTerm = ref<any>(null)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeScreenTerm)
})

const resizeScreenTerm = () => {
  try { // When the window size changes, trigger the xterm resize method to make it adaptive.
    fitAddonTerm.value?.fit()
  } catch (e) {
    console.log("e", e)
  }
}

const initTerm = (term: Terminal, ws: WebSocket | undefined) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    fitAddon.value = new FitAddon()
    fitAddonTerm.value = fitAddon
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon.value)
    term.open(document.getElementById('xterm') as HTMLElement)
    fitAddon.value.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
    window.addEventListener("resize", resizeScreenTerm)
  }
}

const getTermObj = (): Terminal => {
  const termConfig: any = {
    // rendererType: 'dom',
    fontSize: 14,
    rows: 40,
    cols: 90,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    letterSpacing: 8,
    lineHeight: 2,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  }
  return new Terminal(termConfig)
}

const open = async (hostData: KeyValue) => {
  data.show = true
  data.title = t('components.HostTerminal.5oxhoh5r6ls0')
  Object.assign(data.formData, {
    hostId: hostData.hostId,
    ip: hostData.ip,
    sshPort: hostData.port,
    sshUsername: hostData.sshUsername,
    sshPassword: await encryptPassword(hostData.password)
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
.xterm {
  width: 100%;
  height: 100%;
}
</style>
