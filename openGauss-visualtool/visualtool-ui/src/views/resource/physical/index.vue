<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="physical-list">
        <div class="flex-between mb">
          <div>
            <a-button type="primary" class="mr" @click="handleAddHost('create')">
              <template #icon>
                <icon-plus />
              </template>
              {{ $t('physical.index.5mphf11rr080') }}
            </a-button>
          </div>
          <div>
            <a-input-search v-model="filter.name" :loading="list.loading" allowClear @search="isFilter"
              @press-enter="isFilter" @clear="isFilter" :placeholder="$t('physical.index.5mphf11szdk0')" search-button />
          </div>
        </div>
        <a-table class="d-a-table-row" :data="list.data" :columns="columns" :pagination="list.page"
          @page-change="currentPage" @page-size-change="pageSizeChange" :loading="list.loading">
          <template #state="{ record }">
            <a-tag :loading="record.loading" :color="getStateColor(record.state)">{{
              getStateDesc(record.state)
            }}</a-tag>
          </template>
          <template #operation="{ record }">
            <div class="flex-row-start">
              <a-link class="mr" @click="showHostUserMng(record)">{{ $t('physical.index.5mphf11szks0') }}</a-link>
              <a-link class="mr" @click="handleTest(record)">{{ $t('physical.index.5mphf11syw80') }}</a-link>
              <a-link class="mr" @click="handleAddHost('update', record)">{{
                $t('physical.index.5mphf11szqo0')
              }}</a-link>
              <a-popconfirm :content="$t('physical.index.5mphf11szws0')" type="warning"
                :ok-text="$t('physical.index.5mphf11t05c0')" :cancel-text="$t('physical.index.5mphf11t0bc0')"
                @ok="deleteRows(record)">
                <a-link status="danger">{{ $t('physical.index.5mphf11t0hc0') }}</a-link>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
        <add-host ref="addHostRef" @finish="getListData"></add-host>
        <host-pwd-dlg ref="hostPwdRef" @finish="handleTestConnect($event)"></host-pwd-dlg>
        <host-user-mng ref="hostUserRef"></host-user-mng>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { hostPage, delHost, hostPingById } from '@/api/ops' // eslint-disable-line
import AddHost from './components/AddHost.vue'
import HostUserMng from './components/HostUserMng.vue'
import HostPwdDlg from './components/HostPwdDlg.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const filter = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10
})

const columns = computed(() => [
  { title: t('physical.index.ipAddress'), dataIndex: 'privateIp' },
  { title: t('physical.index.5mphf11t0yc0'), dataIndex: 'publicIp' },
  { title: t('physical.index.5mphf11te5c0'), dataIndex: 'azName' },
  { title: t('physical.index.5mphf11teq40'), dataIndex: 'state', slotName: 'state', width: 200 },
  { title: t('physical.index.5mphf11tf5g0'), dataIndex: 'remark' },
  { title: t('physical.index.5mphf11tfjw0'), slotName: 'operation' }
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

const data = reactive<KeyValue>({
  hasTestObj: {}
})

onMounted(() => {
  data.hasTestObj = {}
  getListData()
})

const getListData = () => new Promise(resolve => {
  list.loading = true
  hostPage(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      list.data = []
      console.log('show hasTest obj ', data.hasTestObj)
      res.rows.forEach((item: KeyValue) => {
        if (data.hasTestObj[item.hostId]) {
          item.state = data.hasTestObj[item.hostId]
        } else {
          item.state = -1
        }
        item.loading = false
        list.data.push(item)
      })
      list.page.total = res.total
    } else resolve(false)
  }).finally(() => {
    list.loading = false
  })
})

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData()
}

const isFilter = () => {
  getListData()
}

const addHostRef = ref<null | InstanceType<typeof AddHost>>(null)
const handleAddHost = (type: string, data?: KeyValue) => {
  addHostRef.value?.open(type, data)
}

const currRecord = ref<KeyValue>({})

const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)
const handleTest = (record: KeyValue) => {
  currRecord.value = record
  if (!record.isRemember) {
    hostPwdRef.value?.open(record)
  } else {
    handleTestConnect({
      hostId: record.hostId
    })
  }
}

const handleTestConnect = (hostData: any) => {
  if (hostData.hostId) {
    currRecord.value.loading = true
    const param = {
      rootPassword: hostData.password
    }
    hostPingById(hostData.hostId, param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        currRecord.value.state = 1
      } else {
        currRecord.value.state = 0
      }
      data.hasTestObj[hostData.hostId] = currRecord.value.state
    }).finally(() => {
      currRecord.value.loading = false
    })
  }
}

const deleteRows = (record: KeyValue) => {
  delHost(record.hostId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'delete success'
      })
      getListData()
    }
  })
}

const getStateColor = (state: number) => {
  switch (state) {
    case -1:
      return 'gray'
    case 0:
      return 'red'
    case 1:
      return 'green'
  }
}

const getStateDesc = (state: number) => {
  switch (state) {
    case -1:
      return t('physical.index.5mphf11tfr80')
    case 0:
      return t('physical.index.5mphf11tfx80')
    case 1:
      return t('physical.index.5mphf11tg780')
  }
}

const hostUserRef = ref<null | InstanceType<typeof HostUserMng>>(null)
const showHostUserMng = (record: KeyValue) => {
  hostUserRef.value?.open(record)
}

</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .physical-list {
      padding: 20px;
    }
  }
}
</style>
