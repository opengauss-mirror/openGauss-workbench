<template>
  <div class="slow-sql-c" id="slowSql">
    <div class="flex-row mb">
      <div class="flex-row mr">
        <div class="label-color top-label mr-s">{{ $t('slowSql.index.5mplw69rv000') }}</div>
        <a-select
          class="select-w"
          :loading="filter.clusterListLoading"
          v-model="filter.clusterId"
          :placeholder="$t('slowSql.index.5mplw69rvmo0')"
          @change="getHostList"
        >
          <a-option
            v-for="(item, index) in filter.clusterList"
            :key="index"
            :label="item.label"
            :value="item.value"
          />
        </a-select>
      </div>
      <div class="flex-row mr">
        <div class="label-color top-label mr-s">{{ $t('slowSql.index.5mplw69rvt40') }}</div>
        <a-select
          class="select-w"
          :loading="filter.hostListLoading"
          v-model="filter.hostId"
          :placeholder="$t('slowSql.index.5mplw69rvz80')"
        >
          <a-option
            v-for="(item, index) in filter.hostList"
            :key="index"
            :label="item.label"
            :value="item.value"
          />
        </a-select>
      </div>
      <div class="flex-row mr">
        <div class="label-color query-label mr-s">{{ $t('slowSql.index.5mplw69rw300') }}</div>
        <a-range-picker
          style="width: 360px; margin: 0 24px 0 0;"
          show-time
          :default-value="getCurrentTime"
          :allow-clear="false"
          :time-picker-props="{ defaultValue: ['00:00:00', '23:59:59'] }"
          format="YYYY-MM-DD HH:mm:ss"
          @ok="dateOnOk"
        />
      </div>
      <a-button
        type="primary"
        @click="query"
      >{{ $t('slowSql.index.5mplw69rw900') }}</a-button>
    </div>
    <a-table
      class="d-a-table-row full-h"
      :data="list.data"
      :columns="columns"
      :pagination="list.page"
      :loading="list.loading"
    >
      <template #query_plan="{ record }">
        {{ record.query_plan ? record.query_plan : '--' }}
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive } from 'vue'
import { Message, TableColumnData } from '@arco-design/web-vue'
import { KeyValue } from '@/types/global'
import { clusterList, getHostByClusterId, slowSql } from '@/api/ops'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const filter: {
  clusterId: string,
  hostId: string,
  start: string,
  end: string,
  clusterListLoading: boolean,
  clusterList: KeyValue[],
  hostListLoading: boolean,
  hostList: KeyValue[]
} = reactive({
  clusterId: '',
  hostId: '',
  start: dayjs().format('YYYY-MM-DD') + ' 00:00:00',
  end: dayjs().format('YYYY-MM-DD') + ' 23:59:59',
  clusterListLoading: false,
  clusterList: [],
  hostListLoading: false,
  hostList: []
})

const columns = computed(() => [
  { title: t('slowSql.index.else1'), dataIndex: 'db_name' },
  { title: t('slowSql.index.else2'), dataIndex: 'node_name' },
  { title: t('slowSql.index.5mplw69rw300'), dataIndex: 'start_time', width: 250 },
  { title: t('slowSql.index.5mplw69rwcw0'), dataIndex: 'finish_time', width: 250 },
  { title: t('slowSql.index.5mplw69rwgs0'), dataIndex: 'slow_sql_threshold', width: 100 },
  { title: t('slowSql.index.5mplw69rwks0'), dataIndex: 'query', ellipsis: true, tooltip: true },
  { title: t('slowSql.index.5mplw69rwog0'), dataIndex: 'query_plan', ellipsis: true, tooltip: true }
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

const dateOnOk = (date: any) => {
  filter.start = date[0]
  filter.end = date[1]
}

const getCurrentTime = computed(() => {
  return [dayjs().format('YYYY-MM-DD') + ' 00:00:00', dayjs().format('YYYY-MM-DD') + ' 23:59:59']
})

onMounted(async () => {
  await getClusterList()
  await getHostList()
  query()
})

const query = () => {
  list.loading = true
  const param = {
    clusterId: filter.clusterId,
    hostId: filter.hostId,
    start: filter.start,
    end: filter.end
  }
  slowSql(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = res.data
    } else {
      Message.error('Failed to obtain the slow SQL query list')
    }
  }).catch(() => {
    Message.error('Failed to obtain the slow SQL query list')
  }).finally(() => {
    list.loading = false
  })
}

const getClusterList = () => new Promise(resolve => {
  filter.clusterListLoading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      res.data.forEach((item: KeyValue) => {
        filter.clusterList.push({
          label: item.clusterId,
          value: item.clusterId
        })
      })
      filter.clusterId = filter.clusterList[0].value
    } else resolve(false)
  }).finally(() => {
    filter.clusterListLoading = false
  })
})

const getHostList = () => new Promise(resolve => {
  if (filter.clusterId) {
    const param = {
      clusterId: filter.clusterId
    }
    filter.hostListLoading = true
    getHostByClusterId(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        resolve(true)
        filter.hostList = []
        res.data.forEach((item: KeyValue) => {
          filter.hostList.push({
            label: `${item.privateIp}(${item.hostname})`,
            value: item.hostId
          })
        })
        filter.hostId = filter.hostList[0].value
      } else resolve(false)
    }).finally(() => {
      filter.hostListLoading = false
    })
  }
})

</script>

<style lang="less" scoped>
.slow-sql-c {
  padding: 20px;
  border-radius: 8px;

  .top-label {
    white-space: nowrap;
  }

  .select-w {
    width: 200px;
  }
}
</style>
