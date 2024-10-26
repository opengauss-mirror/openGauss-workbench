<template>
  <div class="app-container" id="systemLog">
    <div class="main-bd">
      <div class="search-con">
        <a-form :model="form" layout="inline">
          <a-form-item field="title" :label="$t('sys-log.index.5mja76hbg2g0')">
            <a-input v-model="form.filename" allow-clear :placeholder="$t('sys-log.index.5mja76hbh680')" style="width: 200px;" @change="getLogFiles"></a-input>
          </a-form-item>
          <a-form-item>
            <a-button type="outline" @click="getLogFiles">
              <template #icon>
                <icon-search />
              </template>
              <template #default>{{$t('sys-log.index.5mja76hbhes0')}}</template>
            </a-button>
            <a-button style="margin-left: 10px;" @click="resetQuery">
              <template #icon>
                <icon-sync />
              </template>
              <template #default>{{$t('sys-log.index.5mja76hbhl40')}}</template>
            </a-button>
          </a-form-item>
        </a-form>
        <a-button type="primary" @click="handleEditConfig">{{$t('sys-log.index.5mja76hbhqc0')}}</a-button>
      </div>
      <div class="table-con">
        <a-table :data="data" :bordered="false" stripe :pagination="pagination" @page-change="pageChange" @page-size-change="pageSizeChange">
          <template #columns>
            <a-table-column :title="$t('sys-log.index.5mja76hbg2g0')" data-index="name" :width="400"></a-table-column>
            <a-table-column :title="$t('sys-log.index.5mja76hbhvo0')" data-index="size">
              <template #cell="{record}">
                <div>{{formatSize(record.size)}}</div>
              </template>
            </a-table-column>
            <a-table-column :title="$t('sys-log.index.5mja76hbi1c0')" data-index="size">
              <template #cell="{record}">
                <div>{{dayjs(record.createdAt).format('YYYY-MM-DD HH:mm:ss')}}</div>
              </template>
            </a-table-column>
            <a-table-column :title="$t('sys-log.index.5mja76hbi6c0')" align="center">
              <template  #cell="{record}">
                <a-button size="mini" type="text" @click="downloadFile(record.name)">
                  <template #icon>
                    <icon-download />
                  </template>
                  <template #default>{{$t('sys-log.index.5mja76hbib80')}}</template>
              </a-button>
            </template>
          </a-table-column>
          </template>
        </a-table>
      </div>
    </div>
    <edit-config v-model:open="editConfigVisible" :configs="logConfig" @ok="getLogConfig"/>
    <a-modal simple v-model:visible="downloadStatus.downloading" hide-cancel :mask-closable="false">
      <template #title>
        <div>{{$t('sys-log.index.5mja76hbim00')}}</div>
      </template>
      <a-progress :percent="downloadStatus.progress"/>
      <template #footer>
        <div></div>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, reactive } from '@vue/runtime-core'
import { listAllLogFiles, getAllLogConfig, download } from '@/api/sysLog'
import EditConfig from './components/EditConfig.vue'
import dayjs from 'dayjs'
import { Message } from '@arco-design/web-vue'

const data = ref([])
const logConfig = ref({})
const form = ref({
  filename: ''
})
const pagination = reactive({
  total: 0,
  current: 1,
  'show-total': true,
  'show-page-size': true
})
const downloadStatus = reactive({
  progress: 0.1,
  downloading: false
})
const editConfigVisible = ref<boolean>(false)

const getLogFiles = () => {
  listAllLogFiles().then((res: any) => {
    pagination.current = 1
    data.value = res.data.filter((item: any) => {
      return item.name.includes(form.value.filename)
    }).sort((t1: any, t2: any) => {
      return t2.updatedAt - t1.updatedAt
    })
    pagination.total = data.value.length
  })
}

const getLogConfig = () => {
  getAllLogConfig().then((res: any) => {
    logConfig.value = res.data
  })
}

const resetQuery = () => {
  form.value.filename = ''
  getLogFiles()
}

const pageChange = (page: number) => {
  pagination.current = page
}

const pageSizeChange = () => {
  pagination.current = 1
}

const handleEditConfig = () => {
  editConfigVisible.value = true
}

const downloadFile = (fileName: string) => {
  downloadStatus.downloading = true
  downloadStatus.progress = 0
  download(fileName, (event) => {
    downloadStatus.progress = Math.floor(event.loaded / event.total * 100) / 100
  }).then((res: any) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    } else {
      Message.error('Download failed, please try again')
    }
    downloadStatus.downloading = false
  }).catch(() => {
    downloadStatus.downloading = false
    Message.error('Download failed, please try again')
  })
}

const formatSize = (input: number) => {
    const K = 1000

    const M = 1000 * 1000
    const G = 1000 * 1000 * 1000
    const T = 1000 * 1000 * 1000 * 1000
    const P = 1000 * 1000 * 1000 * 1000 * 1000
    if (input < K) {
      return input + 'B'
    } else if (input < M) {
      return (input / K).toFixed(1) + 'KB'
    } else if (input < G) {
      return (input / M).toFixed(1) + 'MB'
    } else if (input < T) {
      return (input / G).toFixed(1) + 'GB'
    } else if (input < P) {
      return (input / T).toFixed(1) + 'TB'
    } else {
      return input
    }
  }

onMounted(() => {
  getLogFiles()
  getLogConfig()
})
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .search-con {
      padding: 16px 0 8px;
      margin: 0 20px;
      display: flex;
      justify-content: space-between;
      border-bottom: 1px solid var(--color-border-2);
    }
    .table-con {
      margin-top: 30px;
      padding: 0 20px 30px;
    }
  }
}
</style>
