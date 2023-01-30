<template>
  <div class="upgrade-container" id="backup">
    <div class="flex-row-end mb">
      <div class="flex-row mr">
        <div class="label-color top-label mr-s">{{ $t('backup.index.else1') }}:</div>
        <a-select style="width: 200px;" :loading="data.clusterListLoading" v-model="data.clusterId" allow-clear
          :placeholder="$t('backup.index.5mpm2oya7bg0')">
          <a-option v-for="(item, index) in data.clusterList" :key="index" :label="item.label" :value="item.value" />
        </a-select>
      </div>
      <a-button type="primary" @click="getListData">{{ $t('backup.index.5mpm2oya7y80') }}</a-button>
    </div>
    <a-table class="d-a-table-row full-h" :data="list.data" :columns="columns" :pagination="list.page"
      :loading="list.loading" @page-change="currentPage">
      <template #host="{ record }">
        {{ record.privateIp }}({{ record.publicIp }})
      </template>
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-link class="mr" @click="handleRecover(record)">{{ $t('backup.index.5mpm2oya83s0') }}</a-link>
          <a-popconfirm :content="$t('backup.index.5mpm2oya87g0')" type="warning"
            :ok-text="$t('backup.index.5mpm2oya8b00')" :cancel-text="$t('backup.index.5mpm2oya8ek0')"
            @ok="handleDel(record)">
            <a-link status="danger">{{ $t('backup.index.5mpm2oya8i80') }}</a-link>
          </a-popconfirm>
        </div>
      </template>
    </a-table>
    <!-- <div id="xterm"></div> -->
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { onMounted, reactive, computed } from 'vue'
import { useWinBox } from 'vue-winbox'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import { AttachAddon } from 'xterm-addon-attach'
import Socket from '@/utils/websocket'
import { clusterList, backupPage, backupDel, clusterRecover } from '@/api/ops'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data: {
  clusterId: string,
  clusterListLoading: boolean,
  clusterList: KeyValue[]
} = reactive({
  clusterId: '',
  clusterListLoading: false,
  clusterList: []
})

const columns = computed(() => [
  { title: t('backup.index.5mpm2oya8lk0'), dataIndex: 'clusterId' },
  { title: t('backup.index.5mpm2oya8r80'), dataIndex: 'backupId' },
  { title: t('backup.index.5mpm2oya8uo0'), dataIndex: 'hostId', slotName: 'host' },
  { title: t('backup.index.5mpm2oya8y00'), dataIndex: 'backupPath' },
  { title: t('backup.index.5mpm2oya9280'), dataIndex: 'createTime' },
  { title: t('backup.index.5mpm2oya95o0'), slotName: 'operation' }
])

const filter = reactive({
  search: '',
  pageNum: 1,
  pageSize: 10
})

const list: {
  data: Array<KeyValue>,
  page: { page: number, pageSize: number, total: number },
  loading: boolean
} = reactive({
  data: [],
  page: { page: 1, pageSize: 10, total: 0 },
  loading: false
})

onMounted(() => {
  getListData()
  getClusterList()
})

const getClusterList = () => new Promise(resolve => {
  data.clusterListLoading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      res.data.forEach((item: KeyValue) => {
        data.clusterList.push({
          label: item.clusterId,
          value: item.clusterId
        })
      })
    } else resolve(false)
  }).finally(() => {
    data.clusterListLoading = false
  })
})

const getListData = () => new Promise(resolve => {
  list.loading = true
  backupPage(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      list.data = res.data.records
      list.page.total = res.data.total
    } else resolve(false)
  }).finally(() => {
    list.loading = false
  })
})

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const handleRecover = (row: KeyValue) => {
  list.loading = true
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `backup_${row.backupId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      wsConnectType: 'COMMAND_EXEC',
      businessId: `backup_${row.backupId}_${socketKey}`
    }
    clusterRecover(row.backupId, param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(row.backupId)
        createWinbox(row)
        const term = getTermObj()
        initTerm(term, webSocket, row.backupId)
      }
    }).catch(() => {
      list.loading = false
      console.log('catch destroy')

      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    console.log('onmessage destroy')
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      list.loading = false
      webSocket.destroy()
    }
  })
}

const handleDel = (row: KeyValue) => {
  list.loading = true
  backupDel(row.backupId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      getListData()
    } else {
      Message.error('delete backup fail')
    }
  }).catch(() => {
    Message.error('delete backup fail')
  }).finally(() => {
    list.loading = false
  })
}

const createXterm = (idName: string) => {
  const div = document.createElement('div')
  const divId = document.createAttribute('id')
  divId.value = idName
  div.setAttributeNode(divId)
  const divClass = document.createAttribute('class')
  divClass.value = 'xterm'
  div.setAttributeNode(divClass)
  const styleClass = document.createAttribute('style')
  div.setAttributeNode(styleClass)
  document.getElementById('backup')?.append(div)
}

const getTermObj = (): Terminal => {
  return new Terminal({
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
}

const initTerm = (term: Terminal, socket: Socket<any, any> | null, xtermId: string) => {
  if (socket) {
    console.log('initTerm created', xtermId, document.getElementById(xtermId))
    const attachAddon = new AttachAddon(socket.ws)
    const fitAddon = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon)
    term.open(document.getElementById(xtermId) as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
  }
}

const createWinbox = (row: KeyValue) => {
  const createWindow = useWinBox()
  createWindow({
    id: row.backId,
    title: row.clusterId + ' backup recover',
    mount: document.getElementById(row.backupId),
    class: ['custom-winbox', 'no-full', 'no-max'],
    background: '#1D2129',
    x: 'center',
    y: 'center',
    onClose: function () {
      list.loading = false
      getListData()
    },
    onminimize: function () {
      const oldClass = this.window?.getAttribute('class')
      if (oldClass) {
        const newClass = oldClass.replace('custom-winbox', 'custom-winbox-mini')
        this.window?.setAttribute('class', newClass)
      }
    },
    onrestore: function () {
      const oldClass = this.window?.getAttribute('class')
      if (oldClass) {
        const newClass = oldClass.replace('custom-winbox-mini', 'custom-winbox')
        this.window?.setAttribute('class', newClass)
      }
    }
  })
}

</script>

<style lang="less" scoped>
.upgrade-container {
  padding: 16px;
  box-sizing: border-box;

  .top-label {
    white-space: nowrap;
  }

  .icon-size {
    width: 300px;
    height: 300px;
  }

  .content {
    font-weight: bold;
    color: var(--color-neutral-4);
  }

  .xterm {
    width: 100%;
    height: 100px;
  }
}
</style>
