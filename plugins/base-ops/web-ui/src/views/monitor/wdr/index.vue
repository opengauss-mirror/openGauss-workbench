<template>
  <div class="wdr-c" id="wdr">
    <div class="flex-col mb">
      <div class="full-w mb" style="display: flex; justify-content:space-around">
        <div class="flex-col-start" style="width: 33.3%">
          <div class="flex-row mb">
            <div class="label-color top-label mr-s">{{ $t('wdr.index.5mpm1cuaq540') }}</div>
            <a-select style="width: 300px" :loading="filter.clusterListLoading" v-model="filter.clusterId"
              :placeholder="$t('wdr.index.5mpm1cuaqvk0')">
              <a-option v-for="(item, index) in filter.clusterList" :key="index" :label="item.label"
                :value="item.value" />
            </a-select>
          </div>
          <div class="flex-row ">
            <div class="label-color query-label mr-s">{{ $t('wdr.index.5mpm1cuar0w0') }}</div>
            <a-range-picker style="width: 300px" show-time :default-value="getCurrentTime" :allow-clear="false"
              :time-picker-props="{ defaultValue: ['00:00:00', '23:59:59'] }" format="YYYY-MM-DD HH:mm:ss"
              @ok="dateOnOk" />
          </div>
        </div>
        <div class="flex-col-start" style="width: 33.3%; margin-left: 100px">
          <div class="flex-row">
            <div class="label-color top-label mr-s">{{ $t('wdr.index.5mpm1cuar500') }}</div>
            <a-select style="width: 300px" v-model="filter.wdrScope" :placeholder="$t('wdr.index.5mpm1cuar9s0')">
              <a-option v-for="(item, index) in filter.wdrScopeList" :key="index" :label="item.label"
                :value="item.value" />
            </a-select>
          </div>
        </div>
        <div class="flex-col-end" style="width: 33.3%">
          <div class="flex-row mb">
            <div class="label-color top-label mr-s">{{ $t('wdr.index.5mpm1cuardo0') }}</div>
            <a-select style="width: 300px" v-model="filter.wdrType" :placeholder="$t('wdr.index.5mpm1cuarh00')">
              <a-option v-for="(item, index) in filter.wdrTypeList" :key="index" :label="item.label"
                :value="item.value" />
            </a-select>
          </div>
          <div class="flex-row-end">
            <a-button class="mr" @click="query">
              <template #icon>
                <icon-search />
              </template>
              {{ $t('wdr.index.5mpm1cuarks0') }}
            </a-button>
            <a-button class="mr" @click="resetQuery">
              <template #icon>
                <icon-sync />
              </template>
              {{ $t('wdr.index.5mpm1cuaro40') }}
            </a-button>
            <a-button class="mr" type="primary" @click="showSnapshot">{{ $t('wdr.index.snapshotManage') }}</a-button>
            <a-button type="primary" @click="generateWdr">{{ $t('wdr.index.5mpm1cuarrk0') }}</a-button>
          </div>
        </div>
      </div>
    </div>
    <a-table class="d-a-table-row full-h" :data="list.data" :columns="columns" :pagination="list.page"
      :loading="list.loading">
      <template #scope="{ record }">
        {{ getScoptName(record.scope) }}
      </template>
      <template #reportType="{ record }">
        {{ getReportTypeName(record.reportType) }}
      </template>
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-button type="text" @click="viewWdr(record)">
            <template #icon>
              <icon-eye />
            </template>
            {{ $t('wdr.index.5mpm1cuarv40') }}
          </a-button>
          <a-button type="text" @click="downloadWdr(record)">
            <template #icon>
              <icon-download />
            </template>
            {{ $t('wdr.index.5mpm1cuaryo0') }}
          </a-button>
          <a-popconfirm :content="$t('wdr.index.5mpm1cuas2s0')" type="warning" :ok-text="$t('wdr.index.5mpm1cuas6c0')"
            :cancel-text="$t('wdr.index.5mpm1cuasa40')" @ok="deleteRows(record)">
            <a-button type="text">
              <template #icon>
                <icon-delete />
              </template>
              {{ $t('wdr.index.5mpm1cuasdc0') }}
            </a-button>
          </a-popconfirm>
        </div>
      </template>
    </a-table>
    <generate-wdr-dlg ref="generateWdrRef" @finish="query()"></generate-wdr-dlg>
    <snapshot-manage ref="snapshotManageRef" @finish="query()"></snapshot-manage>
  </div>
</template>

<script lang="ts" setup>

import { KeyValue } from '@/types/global'
import { computed, onMounted, reactive, ref } from 'vue'
import { Message, TableColumnData } from '@arco-design/web-vue'
import { clusterList, downloadWdrFile, wdrList, wdrDelete } from '@/api/ops'
import GenerateWdrDlg from './GenerateWdrDlg.vue'
import SnapshotManage from '../snapshot/index.vue'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const getCurrentTime = computed(() => {
  return [dayjs().subtract(7, 'day').format('YYYY-MM-DD') + ' 00:00:00', dayjs().format('YYYY-MM-DD') + ' 23:59:59']
})

const filter = reactive<KeyValue>({
  clusterId: '',
  hostId: '',
  wdrScope: '',
  wdrType: '',
  start: getCurrentTime.value[0],
  end: getCurrentTime.value[1],
  wdrScopeList: [],
  wdrTypeList: [],
  clusterListLoading: false,
  clusterList: [],
  hostListLoading: false,
  hostList: []
})

const columns = computed(() => [
  { title: t('wdr.index.5mpm1cuar500'), dataIndex: 'scope', slotName: 'scope' },
  { title: t('wdr.index.5mpm1cuasys0'), dataIndex: 'reportAt' },
  { title: t('wdr.index.5mpm1cuardo0'), dataIndex: 'reportType', slotName: 'reportType' },
  { title: t('wdr.index.5mpm1cuat200'), dataIndex: 'reportName' },
  { title: t('wdr.index.5mpm1cuat4o0'), slotName: 'operation' }
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

onMounted(async () => {
  initData()
  await getClusterList()
  query()
})

const initData = () => {
  filter.wdrScopeList = [
    { label: t('wdr.index.5mpm1cuasvs0'), value: '' },
    { label: t('wdr.index.5mpm1cuasgo0'), value: 'CLUSTER' },
    { label: t('wdr.index.5mpm1cuask80'), value: 'NODE' }
  ]
  filter.wdrTypeList = [
    { label: t('wdr.index.5mpm1cuasvs0'), value: '' },
    { label: t('wdr.index.5mpm1cuasng0'), value: 'DETAIL' },
    { label: t('wdr.index.5mpm1cuasr40'), value: 'SUMMARY' },
    { label: t('wdr.index.5mpm1cuasvs0'), value: 'ALL' }
  ]
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

const dateOnOk = (date: any) => {
  filter.start = date[0]
  filter.end = date[1]
}

const query = () => {
  list.loading = true
  const param = {
    clusterId: filter.clusterId,
    wdrScope: filter.wdrScope,
    wdrType: filter.wdrType,
    start: filter.start,
    end: filter.end
  }
  wdrList(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = res.data
    } else {
      Message.error('Failed to obtain the WDR report query list')
    }
  }).catch(() => {
    Message.error('Failed to obtain the WDR report query list')
  }).finally(() => {
    list.loading = false
  })
}

const resetQuery = () => {
  Object.assign(filter, {
    clusterId: filter.clusterList[0].value,
    wdrScope: '',
    wdrType: '',
    start: getCurrentTime.value[0],
    end: getCurrentTime.value[1]
  })
}

const viewWdr = (record: KeyValue) => {
  list.loading = true
  const param = {
    wdrId: record.wdrId
  }
  downloadWdrFile(param).then((res: any) => {
    const newWindow = window.open(record.reportName, '_blank')
    newWindow?.document.write(res)
  }).finally(() => {
    list.loading = false
  })
}

const downloadWdr = (record: KeyValue) => {
  list.loading = true
  const param = {
    wdrId: record.wdrId
  }
  downloadWdrFile(param).then((res: any) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = record.reportName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    }
  }).finally(() => {
    list.loading = false
  })

}

const deleteRows = (record: KeyValue) => {
  list.loading = true
  wdrDelete(record.wdrId).then(() => {
    query()
  }).finally(() => {
    list.loading = false
  })
}

const getScoptName = (val: string) => {
  switch (val) {
    case 'CLUSTER':
      return t('wdr.index.5mpm1cuasgo0')
    case 'NODE':
      return t('wdr.index.5mpm1cuask80')
  }
}

const getReportTypeName = (val: string) => {
  switch (val) {
    case 'DETAIL':
      return t('wdr.index.5mpm1cuasng0')
    case 'SUMMARY':
      return t('wdr.index.5mpm1cuasr40')
    case 'ALL':
      return t('wdr.index.5mpm1cuasvs0')
  }
}

const generateWdrRef = ref<null | InstanceType<typeof GenerateWdrDlg>>(null)
const generateWdr = () => {
  generateWdrRef.value?.open()
}

const snapshotManageRef = ref<null | InstanceType<typeof SnapshotManage>>(null)
const showSnapshot = () => {
  snapshotManageRef.value?.open()
}

</script>

<style lang="less" scoped>
.wdr-c {
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
