<template>
  <div class="package-list" id="packageManage">
    <div class="flex-between mb">
      <div class="flex-row">
        <a-button type="primary" class="mr" @click="handleAdd('create')">
          <template #icon>
            <icon-plus />
          </template>
          {{ $t('packageManage.index.5myq5c8yz7c0') }}
        </a-button>
      </div>
      <div class="flex-row">
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">
            {{ $t('packageManage.index.5myq5c8zns00') }}
          </div>
          <a-select
            style="width: 200px"
            v-model="filter.packageVersion"
            :placeholder="$t('packageManage.index.else3')"
            allow-clear
          >
            <a-option
              v-for="(item, index) in packageVersionList"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </a-select>
        </div>
        <a-input-search
          v-model="filter.name"
          :loading="list.loading"
          allowClear
          @search="isFilter"
          @press-enter="isFilter"
          @clear="isFilter"
          :placeholder="$t('packageManage.index.5myq5c8z8540')"
          search-button
          class="mr-s"
        />
        <a-button v-if="route.params.backUrl" @click="handleBackToInstall">
          <template #icon>
            <icon-undo />
          </template>
          {{ $t('packageManage.index.5myq5c8zw681') }}
        </a-button>
      </div>
    </div>
    <a-table
      class="d-a-table-row"
      :data="list.data"
      :columns="columns"
      :pagination="list.page"
      @page-change="currentPage"
      :loading="list.loading"
    >
      <template #version="{ record }">
        {{ getVersionName(record.packageVersion) }}
      </template>
      <template #packagePath="{ record }">
        {{ getPackagePath(record.packagePath) }}
      </template>
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-link class="mr" @click="handleAdd('update', record)">{{
            $t('packageManage.index.5myq5c8zmbk0')
          }}</a-link>
          <a-popconfirm
            :content="$t('packageManage.index.5myq5c8zms40')"
            type="warning"
            :ok-text="$t('packageManage.index.5myq5c8zn100')"
            :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
            @ok="deleteRows(record)"
          >
            <a-link status="danger">{{
              $t('packageManage.index.5myq5c8znew0')
            }}</a-link>
          </a-popconfirm>
        </div>
      </template>
    </a-table>
    <add-package-dlg
      ref="addPackageRef"
      @finish="getListData"
    ></add-package-dlg>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { packagePage, packageDel } from '@/api/ops' // eslint-disable-line
import AddPackageDlg from './AddPackageDlg.vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import router from '@/router'
const { t } = useI18n()
const route = useRoute()
const filter = reactive({
  name: '',
  packageVersion: '',
  pageNum: 1,
  pageSize: 10
})

const columns = computed(() => [
  {
    title: t('packageManage.index.5myq5c8zpu85'),
    dataIndex: 'name',
    ellipsis: true,
    tooltip: true
  },
  { title: t('packageManage.index.5myq5c8zpu83'), dataIndex: 'type' },
  { title: t('packageManage.index.5myq5c8znkk0'), dataIndex: 'os' },
  { title: t('packageManage.index.else1'), dataIndex: 'cpuArch' },
  {
    title: t('packageManage.index.5myq5c8zns00'),
    dataIndex: 'packageVersion',
    slotName: 'version'
  },
  {
    title: t('packageManage.index.5myq5c8zp680'),
    dataIndex: 'packageVersionNum'
  },
  {
    title: t('packageManage.index.5myq5c8zpu80'),
    dataIndex: 'packageUrl',
    ellipsis: true,
    tooltip: true
  },
  {
    title: t('packageManage.index.5myq5c8zpu82'),
    dataIndex: 'packagePath',
    slotName: 'packagePath',
    ellipsis: true,
    tooltip: true
  },
  {
    title: t('packageManage.index.5myq5c8zpu84'),
    dataIndex: 'remark',
    width: 180
  },
  {
    title: t('packageManage.index.5myq5c8zq380'),
    slotName: 'operation',
    width: 180
  }
])

const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    pageSize: 10,
    'show-total': true
  },
  loading: false
})

const packageVersionList = computed(() => [
  { label: t('packageManage.index.else2'), value: '' },
  { label: t('packageManage.AddPackageDlg.5myq6nnec400'), value: 'ENTERPRISE' },
  {
    label: t('packageManage.AddPackageDlg.5myq6nnec8c0'),
    value: 'MINIMAL_LIST'
  },
  { label: t('packageManage.AddPackageDlg.5myq6nnecc40'), value: 'LITE' }
])

onMounted(() => {
  if (route.query?.name) {
    filter.name = route.query?.name
  }
  getListData()
})

const getListData = () => {
  list.loading = true
  packagePage(filter)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.rows
        list.page.total = res.total
      }
    })
    .finally(() => {
      list.loading = false
    })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const isFilter = () => {
  filter.pageNum = 0
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

const getPackagePath = (value: KeyValue) => {
  if (value) {
    return value.name
  }
  return ''
}

const handleBackToInstall = () => {
  router.push({ path: route.params.backUrl })
}
</script>
<style lang="less" scoped>
.package-list {
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
