<template>
  <el-dialog class="o-dialog--large physicalTerminalDia" :mask-closable="false" v-model="data.show" :title="data.title" :before-close="close">
    <div class="flex-col-start openDesignBody">
      <div class="flex-between mb-s full-w">
        <label class="mb-s">{{ $t('database.HostTerminal.else1') }} - {{ data.formData.ip }}</label>
        <div class="mb-s" style="color: red; font-weight: bold;">
          {{ $t('database.HostTerminal.else2') }}
        </div>
      </div>
      <div id="xterm" class="xterm"></div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { reactive, ref, nextTick } from 'vue'
import { hostSSHByHostId, hostUserPage } from '@/api/ops'
import { WsConnectType } from '@/types/ops/install'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import { AttachAddon } from 'xterm-addon-attach'
import 'xterm/css/xterm.css'
import { useI18n } from 'vue-i18n'
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
  const terminalSocket = new Socket({ url: `${WsConnectType.SSH}/jdbc_terminal_${socketKey}` })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      ip: data.formData.ip,
      sshPort: data.formData.sshPort,
      sshUsername: data.formData.sshUsername,
      businessId: `jdbc_terminal_${socketKey}`
    }
    data.loading = true
    initTerm(term, terminalSocket.ws)
    hostSSHByHostId(data.formData.hostId, param).then((res: KeyValue) => {
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
  const termConfig: any = {
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
  data.title = t('database.HostTerminal.5oxhoh5r6ls0')
  const { hostId, ip, port, password, username } = hostData
  Object.assign(data.formData, {
    hostId,
    ip,
    sshPort: port,
    sshUsername: username,
    sshPassword: password
  })
  nextTick(() => {
    openSocket()
  })
}

defineExpose({
  open
})

</script>

<style lang="less">
.physicalTerminalDia {
  .el-dialog__body {
    max-height: fit-content;
    .openDesignBody {
      max-height: fit-content;
      overflow: auto;
      .xterm-c {
        width: 100%;
        height: 100%;
      }

      .xterm {
        width: 100%;
        height: calc(100% - 32px);
        flex: 1;
        max-height: 472px;
        overflow: auto;
      }
    }
    .body-c {
      box-sizing: border-box;
    }
  }
}
</style>
