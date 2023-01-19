<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="az-list">
        <div class="flex-between mb">
          <div>
            <a-button type="primary" class="mr" @click="handleAddAz('create')">
              <template #icon>
                <icon-plus />
              </template>
              {{ $t('az.index.5mpi9hkpfhk0') }}
            </a-button>
          </div>
          <div>
            <a-input-search v-model="filter.name" :loading="list.loading" allowClear @search="getListData"
              @press-enter="getListData" @clear="getListData" :placeholder="$t('az.index.5mpi9hkpgdw0')"
              search-button />
          </div>
        </div>
        <a-table class="d-a-table-row" :data="list.data" :columns="columns" :pagination="list.page"
          @page-change="currentPage" :loading="list.loading">
          <template #operation="{ record }">
            <div class="flex-row-start">
              <a-link class="mr" @click="handleAddAz('update', record)">{{ $t('az.index.5mpi9hkpgns0') }}</a-link>
              <a-popconfirm :content="$t('az.index.5mpi9hkpgv80')" type="warning" :ok-text="$t('az.index.5mpi9hkph9k0')"
                :cancel-text="$t('az.index.5mpi9hkphi40')" @ok="deleteRows(record)">
                <a-link status="danger">{{ $t('az.index.5mpi9hkphpc0') }}</a-link>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
        <add-az ref="addAzRef" @finish="getListData"></add-az>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { onMounted, reactive, ref, computed } from 'vue'
import { azPage, delAz } from '@/api/ops'
import AddAz from './components/AddAz.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const filter = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10
})

const columns = computed(() => [
  { title: t('az.index.azName'), dataIndex: 'name' },
  { title: t('az.index.5mpi9hkphvo0'), dataIndex: 'address' },
  { title: t('az.index.5mpi9hkpi2o0'), dataIndex: 'priority' },
  { title: t('az.index.5mpi9hkpi940'), dataIndex: 'remark' },
  { title: t('az.index.5mpi9hkpif40'), slotName: 'operation' }
])

// list data
const list: {
  data: Array<KeyValue>,
  page: { pageSize: number, total: number },
  loading: boolean
} = reactive({
  data: [],
  page: { pageSize: 10, total: 0 },
  loading: false
})

// page
const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

onMounted(() => {
  getListData()
})

const getListData = () => new Promise(resolve => {
  list.loading = true
  azPage(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      list.data = res.rows
      list.page.total = res.total
    } else resolve(false)
  }).finally(() => {
    list.loading = false
  })
})
// add or edit az
const addAzRef = ref<null | InstanceType<typeof AddAz>>(null)
const handleAddAz = (type: string, data?: KeyValue) => {
  addAzRef.value?.open(type, data)
}

const deleteRows = (record: KeyValue) => {
  delAz(record.azId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'Delete success'
      })
      getListData()
    } else {
      console.log(res)
    }
  })
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
