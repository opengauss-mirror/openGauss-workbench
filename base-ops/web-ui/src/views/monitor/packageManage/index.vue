<template>
  <div class="package-list">
    <div class="flex-between mb">
      <div>
        <a-button type="primary" class="mr" @click="handleAdd('create')">
          <template #icon>
            <icon-plus />
          </template>
          {{ $t('packageManage.index.5myq5c8yz7c0') }}
        </a-button>
      </div>
      <div>
        <a-input-search v-model="filter.name" :loading="list.loading" allowClear @search="isFilter"
          @press-enter="isFilter" @clear="isFilter" :placeholder="$t('packageManage.index.5myq5c8z8540')"
          search-button />
      </div>
    </div>
    <a-table class="d-a-table-row" :data="list.data" :columns="columns" :pagination="list.page"
      @page-change="currentPage" :loading="list.loading">
      <template #version="{ record }">
        {{ getVersionName(record.packageVersion) }}
      </template>
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-link class="mr" @click="handleAdd('update', record)">{{ $t('packageManage.index.5myq5c8zmbk0') }}</a-link>
          <a-popconfirm :content="$t('packageManage.index.5myq5c8zms40')" type="warning"
            :ok-text="$t('packageManage.index.5myq5c8zn100')" :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
            @ok="deleteRows(record)">
            <a-link status="danger">{{ $t('packageManage.index.5myq5c8znew0') }}</a-link>
          </a-popconfirm>
        </div>
      </template>
    </a-table>
    <add-package-dlg ref="addPackageRef" @finish="getListData"></add-package-dlg>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { packagePage, packageDel } from '@/api/ops' // eslint-disable-line
import AddPackageDlg from './AddPackageDlg.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const filter = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10
})

const columns = computed(() => [
  { title: t('packageManage.index.5myq5c8znkk0'), dataIndex: 'os', width: 170 },
  { title: t('packageManage.index.else1'), dataIndex: 'cpuArch', width: 170 },
  { title: t('packageManage.index.5myq5c8zns00'), dataIndex: 'packageVersion', slotName: 'version', width: 170 },
  { title: t('packageManage.index.5myq5c8zp680'), dataIndex: 'packageVersionNum', width: 170 },
  { title: t('packageManage.index.5myq5c8zpu80'), dataIndex: 'packageUrl', ellipsis: true, tooltip: true },
  { title: t('packageManage.index.5myq5c8zq380'), slotName: 'operation', width: 180 }
])

const list: {
  data: Array<KeyValue>,
  page: { total: number, pageSize: number },
  loading: boolean
} = reactive({
  data: [],
  page: {
    total: 0,
    pageSize: 10,
    'show-total': true
  },
  loading: false
})

onMounted(() => {
  getListData()
})

const getListData = () => new Promise(resolve => {
  list.loading = true
  packagePage(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      list.data = res.rows
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

const isFilter = () => {
  getListData()
}

const addPackageRef = ref<null | InstanceType<typeof AddPackageDlg>>(null)
const handleAdd = (type: string, data?: KeyValue) => {
  addPackageRef.value?.open(type, data)
}

const deleteRows = (record: KeyValue) => {
  packageDel(record.packageId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'delete success'
      })
      getListData()
    }
  })
}

const getVersionName = (version: string) => {
  if (version === 'ENTERPRISE') {
    return t('packageManage.index.5myq5c8zu5w0')
  } else if (version === 'MINIMAL_LIST') {
    return t('packageManage.index.5myq5c8zuxc0')
  } else {
    return t('packageManage.index.5myq5c8zw680')
  }
}

</script>
<style lang="less" scoped>
.package-list {
  padding: 20px;
  background-color: #FFF;
  border-radius: 8px;
  height: calc(100vh - 136px - 40px);

}
</style>
