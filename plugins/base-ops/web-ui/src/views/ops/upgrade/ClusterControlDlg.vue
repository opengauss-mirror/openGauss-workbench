<template>
  <a-modal
    :mask-closable="false"
    :visible="data.show"
    :title="data.title"
    :render-to-body="false"
    :unmount-on-close="true"
    :footer="false"
    :modal-style="{ width: '70%' }"
    @cancel="close"
  >
    <div class="flex-col-start">
      <div class="flex-between full-w">
        <div></div>
        <div
          class="mb-s"
          style="color: red; font-weight: bold;"
        >
          {{ $t('components.HostTerminal.else2') }}
        </div>
      </div>
      <a-tabs
        v-model="data.hostId"
        style="width: 100%;height: 100%;"
      >
        <a-tab-pane
          v-for="item in data.hostList"
          :key="item.hostId"
          :title="item.ip"
        >
          <div
            :id="`xterm_${item.hostId}`"
            class="xterm"
          ></div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-modal>
</template>
  
<script setup lang="ts">

import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { reactive, ref, nextTick } from 'vue'
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
  hostList: [],
  hostId: ''
})

const close = () => {
  data.hostList.forEach((item: KeyValue) => {
    item.termSocket?.destroy()
    if (item.terminal) {
      item.terminal.dispose()
    }
  })
  data.hostList = []
  data.hostId = ''
  data.show = false
}

const openSocket = (item: KeyValue) => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const bid = `upgrade_host_${item.hostId}_${socketKey}`
  const terminalSocket = new Socket({ url: bid })
  item.termSocket = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      hostId: item.hostId,
      rootPassword: item.sshPassword,
      wsConnectType: WsConnectType.SSH,
      businessId: bid
    }
    data.loading = true
    initTerm(term, terminalSocket.ws, item)
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


const initTerm = (term: Terminal, ws: WebSocket | undefined, item: KeyValue) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    const fitAddon = new FitAddon()
    term.loadAddon(fitAddon)
    term.loadAddon(attachAddon)
    term.open(document.getElementById('xterm_' + item.hostId) as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    item.terminal = term
  }
}

const getTermObj = (): Terminal => {
  const termConfig: any = {
    // rendererType: 'dom',
    fontSize: 14,
    rows: 40,
    cols: 200,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    // lineHeight: 2,
    // letterSpacing: 5,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  }
  if (window.screen.width >= 2560 && window.screen.width < 3840) {
    termConfig.rows = 60
    termConfig.lineHeight = 1
    termConfig.letterSpacing = 4
  } else if (window.screen.width >= 3840) {
    termConfig.rows = 80
    termConfig.lineHeight = 2
    termConfig.letterSpacing = 8
  }
  return new Terminal(termConfig)
}

const open = async (hostList: KeyValue, rootPassword: string) => {
  data.show = true
  data.title = t('upgrade.ClusterControlDlg.5uuobb2rd640')
  let pwd = ''
  if (rootPassword) {
    pwd = await encryptPassword(rootPassword)
  }
  data.hostList = hostList.map((item: KeyValue) => {
    const temp = {
      hostId: item.hostId,
      ip: item.publicIp,
      sshPort: 22,
      sshUsername: 'root',
      sshPassword: pwd,
      terminal: null,
      termSocket: null
    }
    return temp
  })
  data.hostId = data.hostList[0].hostId
  console.log('show data ', data.hostId, data.hostList);
  nextTick(() => {
    data.hostList.forEach((item: KeyValue) => {
      openSocket(item)
    })
  })
}

defineExpose({
  open
})

</script>
  
<style lang="less" scoped>
.label {
  white-space: nowrap;
  margin-right: 16px;
}

.xterm {
  width: 100%;
  height: 520px;
}
</style>
  