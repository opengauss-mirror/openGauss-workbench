<template>
  <div class="app-container" id="whiteList">
    <div class="main-bd">
      <div class="search-con">
        <a-form :model="form" layout="inline">
          <a-form-item field="title" :label="$t('whitelist.index.5m6nn2djbsk0')">
            <a-input v-model="form.title" allow-clear :placeholder="$t('whitelist.index.5m6nn2djccs0')" style="width: 220px;" @change="getList(true)"></a-input>
          </a-form-item>
          <a-form-item>
            <a-button type="outline" @click="getList(true)">
              <template #icon>
                <icon-search />
              </template>
              <template #default>{{$t('whitelist.index.5m6nn2djda00')}}</template>
            </a-button>
            <a-button style="margin-left: 10px;" @click="resetQuery">
              <template #icon>
                <icon-sync />
              </template>
              <template #default>{{$t('whitelist.index.5m6nn2djddo0')}}</template>
            </a-button>
          </a-form-item>
        </a-form>
        <div class="btn-con">
          <a-button type="primary" @click="addWhiteList">
            <template #icon>
              <icon-plus />
            </template>
            <template #default>{{$t('whitelist.index.5m6nn2djdgw0')}}</template>
          </a-button>
        </div>
      </div>
      <div class="table-con">
        <a-table :data="tableData" :bordered="false" stripe :pagination="pagination" @page-change="pageChange">
          <template #columns>
            <a-table-column :title="$t('whitelist.index.5m6nn2djbsk0')" data-index="title"></a-table-column>
            <a-table-column :title="$t('whitelist.index.5m6nn2djdjw0')" data-index="ipList"></a-table-column>
            <a-table-column :title="$t('whitelist.index.5m6nn2djdmo0')" data-index="createTime"></a-table-column>
            <a-table-column :title="$t('whitelist.index.5m6nn2djdpc0')" align="center" :width="200">
              <template #cell="{ record }">
                <a-button
                  size="mini"
                  type="text"
                  @click="editWhiteList(record)"
                >
                  <template #icon>
                    <icon-edit />
                  </template>
                  <template #default>{{$t('whitelist.index.5m6nn2djds00')}}</template>
                </a-button>
                <a-popconfirm :content="$t('whitelist.index.5m6nn2djdv00')" @ok="deleteWhiteList(record)">
                  <a-button
                    size="mini"
                    type="text"
                  >
                    <template #icon>
                      <icon-delete />
                    </template>
                    <template #default>{{$t('whitelist.index.5m6nn2djdxo0')}}</template>
                  </a-button>
                </a-popconfirm>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </div>
    </div>

    <!-- add or edit -->
    <edit-condition v-model:open="editWhiteListVisible" :options="currentEditWhiteList" @ok="getList" />
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { getWhiteList, whiteListDelete } from '@/api/whiteList'
  import EditCondition from './components/EditCondition.vue'

  const form = reactive({
    title: undefined
  })

  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10
  })
  const pagination = reactive({
    total: 0,
    current: 1,
    pageSize: 10
  })
  const tableData = ref([])
  const editWhiteListVisible = ref<boolean>(false)
  const currentEditWhiteList = ref({})

  const getList = (fresh?: any) => {
    if (fresh) {
      queryParams.pageNum = 1
      pagination.current = 1
    }
    getWhiteList({
      ...queryParams,
      ...form
    }).then((res: any) => {
      tableData.value = res.rows
      pagination.total = res.total
    })
  }

  const resetQuery = () => {
    queryParams.pageNum = 1
    queryParams.pageSize = 10
    pagination.current = 1
    form.title = undefined
    getList()
  }

  const pageChange = (current: number) => {
    queryParams.pageNum = current
    pagination.current = current
    getList()
  }

  const addWhiteList = () => {
    currentEditWhiteList.value = {}
    editWhiteListVisible.value = true
  }

  const editWhiteList = (value: any) => {
    currentEditWhiteList.value = value
    editWhiteListVisible.value = true
  }

  const deleteWhiteList = (value: any) => {
    whiteListDelete(value.id).then(() => {
      Message.success('Delete success')
      getList()
    })
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .search-con {
      padding: 16px 0 8px;
      margin: 0 20px;
      display: flex;
      justify-content: space-between;
      border-bottom: 1px solid var(--color-border-2);
    }
    .table-con {
      margin-top: 30px;
      padding: 0 20px 30px;
    }
  }
}
</style>
