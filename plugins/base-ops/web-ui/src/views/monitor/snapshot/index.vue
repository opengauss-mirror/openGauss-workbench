<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    @cancel="close"
    :modal-style="{ width: '900px' }"
    :footer="false"
  >
    <div class="snapshot-c">
      <a-spin
        class="full-w"
        :loading="data.createLoading"
        :tip="$t('snapshot.index.5mplywcvqh80')"
      >
        <div class="flex-between mb">
          <a-button
            type="primary"
            @click="handleCreate"
          >
            <template #icon>
              <icon-plus />
            </template>
            {{ $t('snapshot.index.5mplywcvr080') }}
          </a-button>
          <div class="flex-row">
            <div class="flex-row mr">
              <div class="top-label mr-s">{{ $t('snapshot.index.5mplywcvr7c0') }}</div>
              <a-select
                class="select-w"
                :loading="data.clusterListLoading"
                v-model="data.clusterId"
                :placeholder="$t('snapshot.index.5mplywcvrb80')"
                @change="getHostList"
              >
                <a-option
                  v-for="(item, index) in data.clusterList"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </a-select>
            </div>
            <div class="flex-row mr">
              <div class="top-label mr-s">{{ $t('snapshot.index.5mplywcvres0') }}</div>
              <a-select
                class="select-w"
                :loading="data.hostListLoading"
                v-model="data.hostId"
                :placeholder="$t('snapshot.index.5mplywcvrio0')"
              >
                <a-option
                  v-for="(item, index) in data.hostList"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </a-select>
            </div>
            <a-button
              type="primary"
              @click="query"
            >{{ $t('snapshot.index.5mplywcvrmg0') }}</a-button>
          </div>
        </div>
        <a-table
          class="d-a-table-row full-h"
          :data="list.data"
          :columns="columns"
          :pagination="list.page"
          :loading="list.loading"
          @page-change="currentPage"
          @page-size-change="pageSizeChange"
        >
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { onMounted, reactive, computed } from 'vue'
import { Message, TableColumnData } from '@arco-design/web-vue'
import { clusterList, createSnapshot, getHostByClusterId, listSnapshot } from '@/api/ops'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: '',
  createLoading: false,
  clusterId: '',
  hostId: '',
  clusterListLoading: false,
  clusterList: [],
  hostListLoading: false,
  hostList: []
})

const columns = computed(() => [
  { title: t('snapshot.index.5mplywcvrq80'), dataIndex: 'snapshot_id' },
  { title: t('snapshot.index.5mplywcvru80'), dataIndex: 'end_ts' }
])

const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    'show-total': true,
    'show-jumper': true,
    'show-page-size': true
  },
  loading: false
})

const filter = reactive({
  pageNum: 1,
  pageSize: 10
})

onMounted(() => {
  getClusterList()
})

const close = () => {
  data.show = false
}

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
      data.clusterId = data.clusterList[0].value
      getHostList()
    } else resolve(false)
  }).finally(() => {
    data.clusterListLoading = false
  })
})

const getHostList = () => {
  if (data.clusterId) {
    const param = {
      clusterId: data.clusterId
    }
    data.hostListLoading = true
    getHostByClusterId(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        data.hostList = []
        res.data.forEach((item: KeyValue) => {
          data.hostList.push({
            label: `${item.privateIp}(${item.hostname})`,
            value: item.hostId
          })
        })
        data.hostId = data.hostList[0].value
        query()
      } else {
        Message.error('Failed to obtain host information')
      }
    }).finally(() => {
      data.hostListLoading = false
    })
  }
}

const handleCreate = () => {
  data.createLoading = true
  const param = {
    clusterId: data.clusterId,
    hostId: data.hostId
  }
  createSnapshot(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      setTimeout(() => {
        query()
      }, 1000)
    } else {
      Message.error('Failed to Generate a Snapshot')
    }
  }).catch(() => {
    Message.error('Failed to Generate a Snapshot')
  }).finally(() => {
    data.createLoading = false
  })
}

const query = () => {
  list.loading = true
  const param = Object.assign({
    clusterId: data.clusterId,
    hostId: data.hostId
  }, filter)
  listSnapshot(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = res.data.records
      list.page.total = res.data.total
    } else {
      Message.error('Failed to obtain the snapshot query list')
    }
  }).catch(() => {
    Message.error('Failed to obtain the snapshot query list')
  }).finally(() => {
    list.loading = false
  })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  query()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  query()
}

const open = () => new Promise(resolve => { // eslint-disable-line
  data.show = true
  data.title = t('snapshot.index.snapshotManage')
})
defineExpose({
  open
})

</script>

<style lang="less" scoped>
.snapshot-c {
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
