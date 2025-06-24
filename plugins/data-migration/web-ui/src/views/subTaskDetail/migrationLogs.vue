<template>
  <div class="migration-log openDesignTableArea minWid912">
    <div class="search-con">
      <el-input class="o-style-search" allow-clear v-model="filter.fileName" :placeholder="t('components.SubTaskDetail.fileName')"
        :prefix-icon="IconSearch" @search="searchLog" @change="searchLog"></el-input>
      <div class="button-wrapper">
        <el-button class="o-button--icon" @click="searchLog" :icon="IconRefresh"></el-button>
      </div>
    </div>
    <el-table :data="logData">
      <template #empty>
        <empty-page></empty-page>
      </template>
      <el-table-column prop="name" width="350" :label="$t('components.SubTaskDetail.fileName')">

      </el-table-column>

      <el-table-column prop="" :label="$t('components.SubTaskDetail.remark')">
        <template #default="{ row }">
          <span style="line-height: 1.3">
            {{ logsMap(row.name) }}
          </span>
        </template></el-table-column>
      <el-table-column prop="operation" :label="$t('components.SubTaskDetail.operate')">
        <template #default="{ row }">
          <el-button text @click="handleDownloadLog(row)">
            {{ $t('components.SubTaskDetail.5q09prnznvg0') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :total="total" :layout="layout" v-model:page-size="filter.pageSize" :page-sizes="[15, 30, 50]"
      v-model:current-page="filter.pageNum" @change="clickSearch"></el-pagination>
  </div>
</template>
<script setup lang="ts">
import { ref, inject, onMounted, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import EmptyPage from '@/components/emptyPage'
import {
  downloadLog,
  migLogsInfo,
} from '@/api/detail'
import FusionSearch from "@/components/fusion-search/index.vue";
import { searchType } from "@/types/searchType";
import usePagination from "@/utils/usePagination";
import { IconSearch, IconRefresh, IconEmpty } from '@computing/opendesign-icons'

const { t } = useI18n()
const subTaskId = inject('subTaskId')
const logData = ref([])
const filter = reactive({
  pageNum: 1,
  pageSize: 15,
  fileName: '',
})
const { layout } = usePagination();
const total = ref(0)
// Check the corresponding logs here.
const queryLogList = async () => {
  try {
    const res = await migLogsInfo(subTaskId.value, filter);
    logData.value = res.data?.logs?.map((item: any) => {
      const name = item.substring(item.lastIndexOf('/') + 1)
      return {
        name,
        url: item
      }
    }) || [];
    total.value = res.data.total;
  } catch (err) {
    console.error(err, 'log-list-error')
  }
}

onMounted(() => {
  queryLogList()
})

const logsMap = (key) => {
  const maps = {
    'sink.log': t('components.SubTaskDetail.5q09prnzpkg0'),
    'source.log': t('components.SubTaskDetail.5q09prnzpmk0'),
    'check.log': t('components.SubTaskDetail.5q09prnzpps0'),
    'full_migration.log': t('components.SubTaskDetail.5q09prnzprs0'),
    'server.log': t('components.SubTaskDetail.5q09vm8iktw0'),
    'schema-registry.log': t('components.SubTaskDetail.5q09wjm47ow0'),
    'connect.log': t('components.SubTaskDetail.5q09prnzpug0'),
    'connect_source.log': t('components.SubTaskDetail.5q09prnzpxc0'),
    'connect_sink.log': t('components.SubTaskDetail.5q09prnzq080'),
    'reverse_connect.log': t('components.SubTaskDetail.5q09prnzq2o0'),
    'reverse_connect_source.log': t('components.SubTaskDetail.5q09prnzq680'),
    'reverse_connect_sink.log': t('components.SubTaskDetail.5q09prnzq880'),
    'error.log': t('components.SubTaskDetail.5q09prnzqac0'),
    'business-check.log': t('components.SubTaskDetail.5q09prnzqac1'),
    'business-sink.log': t('components.SubTaskDetail.5q09prnzqac2'),
    'business-source.log': t('components.SubTaskDetail.5q09prnzqac3'),
    'check-debug.log': t('components.SubTaskDetail.5q09prnzqac4'),
    'check-error.log': t('components.SubTaskDetail.5q09prnzqac5'),
    'kafka-check.log': t('components.SubTaskDetail.5q09prnzqac6'),
    'kafka-sink.log': t('components.SubTaskDetail.5q09prnzqac7'),
    'kafka-source.log': t('components.SubTaskDetail.5q09prnzqac8'),
    'sink-debug.log': t('components.SubTaskDetail.5q09prnzqac9'),
    'sink-error.log': t('components.SubTaskDetail.5q09prnzqad0'),
    'source-debug.log': t('components.SubTaskDetail.5q09prnzqad1'),
    'source-error.log': t('components.SubTaskDetail.5q09prnzqad2')
  }
  return maps[key] || '--'
}

const clickSearch = (params) => {
  const { fileName } = params;
  filter.fileName = fileName || ''
  queryLogList()
}

const searchLog = () => {
  queryLogList()
}

const subTaskInfo = ref({})
const handleDownloadLog = (urlObj) => {
  const url = urlObj.url;
  if (!url) {
    return;
  }
  downloadLog(subTaskId.value, { filePath: url }).then((res) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = `#${subTaskId.value}_${url.substring(
        url.lastIndexOf('/') + 1,
        url.lastIndexOf('.')
      )}_${dayjs().format('YYYYMMDDHHmmss')}.log`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a);
      window.URL.revokeObjectURL(herf)
    }
  })
}

</script>
<style lang="less" scoped>
.migration-log {
  height: calc(100% - 16px);
  margin-top: 16px;
  padding: 24px;
  background-color: var(--o-bg-color-base);
  display: flex;
  flex-direction: column;

  .el-table {
    flex: 1;
  }

  :deep(.el-table__empty-block) {
    margin-top: 0;    }

  .search-con {
    display: flex;

    .input-wrapper {
      flex-grow: 1
    }

    .button-wrapper {
      margin-left: 8px;
    }
  }
}
</style>
