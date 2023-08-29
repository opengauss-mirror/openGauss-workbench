<template>
  <div class="audit-log-c">
    <div class="query-c mb">
      <div class="label-color mr-s">{{ $t('components.AuditLog.else1') }}:</div>
      <a-range-picker style="width: 360px; margin: 0 24px 0 0;" show-time :default-value="getCurrentTime"
        :allow-clear="false" :time-picker-props="{ defaultValue: ['00:00:00', '23:59:59'] }"
        format="YYYY-MM-DD HH:mm:ss" @ok="dateOnOk" />
      <a-button type="outline" @click="getAuditLog">
        <template #icon>
          <icon-search />
        </template>
        <template #default>{{ $t('components.AuditLog.5mplidlico80') }}</template>
      </a-button>
    </div>
    <a-table class="d-a-table-row full-h" :data="list.data" :columns="columns" :pagination="list.page"
      :page-change="currentPage" :loading="list.loading">
    </a-table>
  </div>
</template>

<script setup lang="ts">

import { computed, reactive, watch } from 'vue'
import { auditLog } from '@/api/ops'
import { Message, TableColumnData } from '@arco-design/web-vue'
import { KeyValue } from '@/types/global'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const filter = reactive({
  start: dayjs().format('YYYY-MM-DD') + ' 00:00:00',
  end: dayjs().format('YYYY-MM-DD') + ' 23:59:59'
})

const columns = computed(() => [
  { title: t('components.AuditLog.5mplidlidfs0'), dataIndex: 'time' },
  { title: t('components.AuditLog.5mplidlido80'), dataIndex: 'type' },
  { title: t('components.AuditLog.5mplidlie1k0'), dataIndex: 'result' },
  { title: t('components.AuditLog.5mplidlielo0'), dataIndex: 'userid' },
  { title: t('components.AuditLog.5mplidlietk0'), dataIndex: 'username' },
  { title: t('components.AuditLog.5mplidlif1o0'), dataIndex: 'database' },
  { title: t('components.AuditLog.5mplidlifdw0'), dataIndex: 'client_conninfo' },
  { title: t('components.AuditLog.5mplidlifj80'), dataIndex: 'object_name' },
  { title: t('components.AuditLog.5mplidlifnc0'), dataIndex: 'detail_info' },
  { title: t('components.AuditLog.5mplidlifr40'), dataIndex: 'node_name' },
  { title: t('components.AuditLog.5mplidlifv40'), dataIndex: 'thread_id' },
  { title: t('components.AuditLog.5mplidlig300'), dataIndex: 'local_port' },
  { title: t('components.AuditLog.5mplidlig7w0'), dataIndex: 'remote_port' }
])

const list: {
  data: Array<KeyValue>,
  page: { page: number, pageSize: number },
  loading: boolean
} = reactive({
  data: [],
  page: { page: 1, pageSize: 20 },
  loading: false
})

const getCurrentTime = computed(() => {
  return [dayjs().format('YYYY-MM-DD') + ' 00:00:00', dayjs().format('YYYY-MM-DD') + ' 23:59:59']
})

const auditProps = defineProps({
  clusterId: String,
  hostId: String
})

const dateOnOk = (date: any) => {
  filter.start = date[0]
  filter.end = date[1]
}

watch(() => auditProps.hostId, (hostId) => {
  if (hostId) {
    getAuditLog()
  }
})

const getAuditLog = () => {
  const param = {
    clusterId: auditProps.clusterId,
    hostId: auditProps.hostId,
    start: filter.start,
    end: filter.end
  }
  list.loading = true
  auditLog(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = res.data
    } else {
      Message.error('Error Obtaining audit log')
    }
  }).catch(() => {
    Message.error('Error Obtaining audit log')
  }).finally(() => {
    list.loading = false
  })
}

const currentPage = (e: number) => {
  list.page.page = e
  getAuditLog()
}

</script>

<style lang="less" scoped>
.audit-log-c {
  padding: 20px;

  .query-c {
    display: flex;
    align-items: center;
  }
}
</style>
