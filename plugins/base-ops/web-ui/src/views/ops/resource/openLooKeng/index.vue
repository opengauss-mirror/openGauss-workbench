<template>
  <div class="app-container" id="opsResource">
    <div class="main-bd">
      <div class="az-list">
        <div class="flex-between mb">
          <div>
            <a-button type="primary" class="mr" @click="handleDeployOlk('create')">
              <template #icon>
                <icon-plus/>
              </template>
              {{ $t('components.openLooKeng.5mpiji1qpcc70') }}
            </a-button>
          </div>
          <div>
            <a-input-search v-model="filter.name" :loading="list.loading" allowClear @search="getListData"
                            @press-enter="getListData" @clear="getListData" :placeholder="$t('components.openLooKeng.5mpiji1qpcc71')"
                            search-button/>
          </div>
        </div>
        <a-table class="d-a-table-row" :data="list.data" :columns="columns" :pagination="list.page"
                 @page-change="currentPage" @page-size-change="pageSizeChange" :loading="list.loading" :expandable="expandable">
          <template #operation="{ record }">
            <div class="flex-row-start">
              <a-link class="mr" @click="initWs(record, OlkOp.START)" :loading="record.startLoading" :disabled="isStartDisabled(record)">{{ $t('components.openLooKeng.5mpiji1qpcc72') }}</a-link>
              <a-link class="mr" @click="initWs(record, OlkOp.STOP)" :loading="record.stopLoading" :disabled="isStopDisabled(record)">{{ $t('components.openLooKeng.5mpiji1qpcc73') }}</a-link>
              <a-popconfirm :content="$t('components.openLooKeng.5mpiji1qpcc74')" type="warning" :ok-text="$t('components.openLooKeng.5mpiji1qpcc75')"
                            :cancel-text="$t('components.openLooKeng.5mpiji1qpcc76')" @ok="handleRemove(record)">
                <a-link status="danger" class="mr" :disabled="isDelDisabled(record)">{{ $t('components.openLooKeng.5mpiji1qpcb76') }}</a-link>
              </a-popconfirm>
              <a-popconfirm :content="$t('components.openLooKeng.5mpiji1qpcc77')" type="warning" :ok-text="$t('components.openLooKeng.5mpiji1qpcc75')"
                            :cancel-text="$t('components.openLooKeng.5mpiji1qpcc76')" @ok="initWs(record, OlkOp.DESTROY)">
                <a-link status="danger" :loading="record.destroyLoading" class="mr" :disabled="isDestroyDisabled(record)">{{$t('components.openLooKeng.5mpiji1qpcc78')}}</a-link>
              </a-popconfirm>
              <a-link @click="handleDownloadLog(record)" v-if="record.logs">{{$t('components.openLooKeng.5mpiji1qpcc79')}}</a-link>
            </div>
          </template>
        </a-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {KeyValue} from '@/types/global'
import {Message} from '@arco-design/web-vue'
import {onMounted, reactive, computed, h} from 'vue'
import {pageOlk, removeOlk, startOlk, stopOlk, destroyOlk} from '@/api/ops'
import {useI18n} from 'vue-i18n'
import Socket from "@/utils/websocket";
import router from "@/router";
import expandInfo from "@/views/ops/resource/openLooKeng/expandInfo.vue";
import dayjs from "dayjs";

const {t} = useI18n()
const filter = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10
})

enum OlkOp {
  START = 'Start',
  STOP = 'Stop',
  DESTROY = 'Destroy'
}

const columns = computed(() => [
  {title: t('components.openLooKeng.5mpiji1qpcc80'), dataIndex: 'name', width: "350"},
  {title: t('components.openLooKeng.5mpiji1qpcc81'), dataIndex: 'tableName'},
  {title: t('components.openLooKeng.5mpiji1qpcc82'), dataIndex: 'columns'},
  {title: t('components.openLooKeng.5mpiji1qpcc83'), dataIndex: 'updateTime'},
  {title: t('components.openLooKeng.5mpiji1qpcc84'), dataIndex: 'remark'},
  {title: t('components.openLooKeng.5mpiji1qpcc85'), slotName: 'operation', width: '400'}
])

// list data
const list = reactive({
  data: [],
  page: {
    total: 0,
    current: 1,
    pageSize: 10,
    'show-total': true,
    'show-jumper': true,
    'show-page-size': true
  },
  loading: false
})

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData()
}

onMounted(() => {
  getListData()
})

const getListData = () => {
  list.loading = true
  pageOlk(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = res.rows
      list.page.total = res.total
    }
  }).finally(() => {
    list.loading = false
  })
}

const handleStart = (record: KeyValue, bid: string) => {
  record.startLoading = true
  startOlk(record.id, bid).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'Send Start Command Success'
      })
    } else {
      record.ws.destroy()
      Message.success({
        content: 'Send Start Command Failed: ' + res.msg
      })
      record.startLoading = false
    }
  }).catch(() => {
    record.ws.destroy()
    record.startLoading = false
  })
}

const handleStop = (record: KeyValue, bid: string) => {
  record.stopLoading = true
  stopOlk(record.id, bid).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'Send Stop Command Success'
      })
    } else {
      record.ws.destroy()
      Message.success({
        content: 'Send Stop Command Failed: ' + res.msg
      })
      record.stopLoading = false
    }
  }).catch(() => {
    record.ws.destroy()
    record.stopLoading = false
  })
}

const handleDestroy = (record: KeyValue, bid: string) => {
  destroyOlk(record.id, bid).then((res: KeyValue) => {
    record.destroyLoading = true
    if (Number(res.code) === 200) {
      Message.success({
        content: 'Send Destroy Command Success'
      })
    } else {
      record.ws.destroy()
      Message.success({
        content: 'Send Destroy Command Failed: ' + res.msg
      })
      record.destroyLoading = false
    }
  }).catch(() => {
    record.ws.destroy()
    record.destroyLoading = false
  })
}
const handleRemove = (record: KeyValue) => {
  record.delLoading = true
  removeOlk(record.id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'Delete success'
      })
      getListData()
    }
  }).finally(() => {
    record.delLoading = false
  })
}

const handleDeployOlk = () => {
  router.push('/ops/install')
}

const handleDownloadLog = (record: KeyValue) => {
  const time = dayjs().format('YYYY-MM-DD_HH:mm:ss')
  const filename = `operation_${time}.log`

  const blob = new Blob([record.logs], {type: 'text/plain'})
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const initWs = (record: KeyValue, op: string) => {
  const socketKey = new Date().getTime()
  const bid = `olk_op_log_${socketKey}`
  const logSocket = new Socket({url: bid})
  logSocket.onopen(() => {
    let method
    switch (op) {
      case OlkOp.START:
        method = handleStart
        break
      case OlkOp.STOP:
        method = handleStop
        break
      default:
        method = handleDestroy
        break
    }
    record.ws = logSocket
    record.logs = ''
    method(record, bid)
  })
  logSocket.onclose(() => {

  })
  logSocket.onmessage((messageData: any) => {
    record.logs += messageData + '\r\n'
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      const flag = Number(messageData.split(':')[1])
      switch (op) {
        case OlkOp.START:
          record.startLoading = false
          break
        case OlkOp.STOP:
          record.stopLoading = false
          break
        default:
          record.destroyLoading = false
          break
      }
      if (flag === 0) {
        Message.success(op + ' OpenLooKeng Service Success')
        if (op === OlkOp.DESTROY) {
          getListData()
        }
      } else {
        Message.error(op + ' OpenLooKeng Service Error')
      }
      logSocket.destroy()
    }
  })
}

const expandable = reactive({
    title: '详细',
    width: 80,
    expandedRowRender: (record: KeyValue) => {
      return h('div', {}, [
        h(expandInfo, {
          record: record
        })
      ])
    }
})

const isStartDisabled = (record: KeyValue) => {
  if (!record) {
    return false
  }
  return record.delLoading || record.destroyLoading || record.stopLoading
}
const isStopDisabled = (record: KeyValue) => {
  if (!record) {
    return false
  }
  return record.delLoading || record.destroyLoading || record.startLoading
}
const isDestroyDisabled = (record: KeyValue) => {
  if (!record) {
    return false
  }
  return record.delLoading || record.startLoading || record.stopLoading
}
const isDelDisabled = (record: KeyValue) => {
  if (!record) {
    return false
  }
  return record.startLoading || record.destroyLoading || record.stopLoading
}
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .az-list {
      padding: 20px;
    }
  }
}
</style>
