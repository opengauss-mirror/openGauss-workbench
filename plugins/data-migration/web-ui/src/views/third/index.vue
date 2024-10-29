<template>
  <div class="app-container" id="migrationThird">
    <div class="search-con">
      <a-form :model="form" layout="inline">
        <a-form-item field="kafkaIp" style="margin-left: -17px;">
          <a-input v-model="form.kafkaIp" allow-clear :placeholder="$t('third.index.5q08thptsw88')" style="width: 200px;" @change="getList"></a-input>
        </a-form-item>
        <a-form-item>
          <a-button type="outline" @click="getList">
            <template #icon>
              <icon-search />
            </template>
            <template #default>{{$t('third.index.5q08thptsw90')}}</template>
          </a-button>
          <a-button style="margin-left: 10px;" @click="resetQuery">
            <template #icon>
              <icon-sync />
            </template>
            <template #default>{{$t('third.index.5q08thptsw91')}}</template>
          </a-button>
        </a-form-item>
      </a-form>
      
    </div>

    <div class="table-con">
      <a-table :loading="loading" row-key="id" :data="tableData" :row-selection="rowSelection" v-model:selectedKeys="selectedKeys" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme" :pagination="pagination" @page-change="pageChange">
        <template #columns>
          <a-table-column :title="$t('third.index.5q08thptsw81')" data-index="kafkaIp" :width="150"></a-table-column>
          <a-table-column :title="$t('third.index.5q08thptsw84')" data-index="kafkaPort" :width="150"></a-table-column>
          <a-table-column :title="$t('third.index.5q08thptsw85')" data-index="zookeeperPort" :width="150"></a-table-column>
          <a-table-column :title="$t('third.index.5q08thptsw86')" data-index="schemaRegistryPort" :width="150"></a-table-column>
          <a-table-column :title="$t('third.index.5q08thptsw89')" data-index="installDir" :width="150"></a-table-column>
          <a-table-column :title="$t('third.index.5q08thptsw87')" data-index="bindPortalHost" :width="150"></a-table-column>
        </template>
      </a-table>
    </div>
   
        
  </div>
</template>

<script setup>

import { reactive, ref, onMounted } from 'vue'

import { listKafkaInstance } from '@/api/task'

import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const resetQuery = () => {
  form.kafkaIp = undefined
  getList()
}

const form = reactive({
  softwareName: undefined,
  kafkaIp: undefined,
  kafkaPort: undefined,
  zkIp: undefined,
  zookeeperPort: undefined,
  schemaRegistryIp: undefined,
  schemaRegistryPort: undefined,
  installDir: undefined,
  bindPortalHost: undefined
})
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})

const pageChange = current => {
  queryParams.pageNum = current
  pagination.current = current
  getList()
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([])

const getList = () => {
  listKafkaInstance({
    kafkaIp: form.kafkaIp,
    ...queryParams
  }).then(res => {
    tableData.value = res.rows
    pagination.total = res.total
  })
}

onMounted(() => {
  getList()
  window.$wujie?.bus.$on('data-migration-update', () => {
    getList()
  })
})


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
