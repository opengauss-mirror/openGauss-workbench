<template>
  <div class="file-log-c">
    <a-spin class="flex-col-start" :loading="data.loading" :tip="$t('components.FileLog.5mplk6504fg0')">
      <div v-if="data.fileList.length">
        <div v-for="(item, index) in data.fileList" :key="index">
          <div class="flex-row mb">
            <div class="label-color folder-c flex-row" v-if="item.type === 'DIRECTORY'" @click="getUnderFolder(item)">
              <icon-folder class="mr" />
              <div class=" mr">{{ item.name }}</div>
              <div>{{ item.size }}</div>
            </div>
            <div class="value-color flex-row" v-else>
              <icon-file class="mr" />
              <div class="mr">{{ item.name }}</div>
              <div class="mr">{{ item.size }}</div>
              <icon-download style="cursor: pointer" @click="fileDownload(item)" />
            </div>
          </div>
        </div>
      </div>
      <div class="empty-c" v-if="data.fileList.length === 0 && !data.loading">
        <svg-icon icon-class="ops-empty" class="icon-size mb"></svg-icon>
        <div class="tip-content">{{ $t('components.FileLog.5mplk65055s0') }}</div>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">

import { reactive, watch } from 'vue'
import { getLs, downloadFile } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { KeyValue } from '@/types/global'

const data = reactive<KeyValue>({
  // choose path
  path: '',
  fileList: [],
  loading: false
})

const systemProps = defineProps({
  rootPath: String,
  hostId: String
})

watch(() => systemProps.rootPath, (rootPath) => {
  if (rootPath) {
    getUnderPath()
  }
})

const getUnderPath = () => {
  if (systemProps.rootPath) {
    data.path = systemProps.rootPath
    getFiles(systemProps.rootPath)
  }
}

const getUnderFolder = (folder: any) => {
  data.path = data.path + '/' + folder.name
  getFiles(data.path)
}

const getFiles = (path: string) => {
  data.loading = true
  const param = {
    hostId: systemProps.hostId,
    path: path
  }
  getLs(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.fileList = res.data
    } else {
      Message.error('Failed to obtain the path of the system log file')
    }
  }).catch(() => {
    Message.error('Failed to obtain the path of the system log file')
  }).finally(() => {
    data.loading = false
  })
}

const fileDownload = (file: any) => {
  data.loading = true
  const param = {
    hostId: systemProps.hostId,
    path: data.path,
    filename: file.name
  }
  downloadFile(param).then((res: any) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = file.name
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    }
  }).finally(() => {
    data.loading = false
  })
}

</script>

<style lang="less" scoped>
.file-log-c {
  padding: 20px;

  .folder-c {
    cursor: pointer;
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }
}
</style>
