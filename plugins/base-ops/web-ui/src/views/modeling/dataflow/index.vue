<template>
  <div class="app-container" id="dataflow">
    <div class="main-bd">
      <div class="list data-flow-list">
        <div class="tool mb-s">
          <div class="add mr">
            <a-button type="primary" @click="openDialog('cu', 'create')">
              <template #icon><icon-plus /></template>
              {{ $t('modeling.dataflow.index.5m77w0y57io0') }}
            </a-button>
          </div>
          <div class="filter">
            <div class="filter-item">
              <a-input-search
                :max-length="140"
                v-model="filter.search"
                :loading="filter.loading"
                allowClear
                @search="isFilter"
                @press-enter="isFilter"
                @clear="isFilter"
                :placeholder="$t('modeling.dataflow.index.5m77w0y5b3k0')"
                search-button
              />
            </div>
          </div>
        </div>
        <div class="table">
          <div class="header">
            <div class="text label-color">
              {{ $t('modeling.dataflow.index.5m77w0y5brg0') }}
            </div>
          </div>
          <div class="content">
            <a-table
              class="d-a-table-row"
              :data="list.data"
              :columns="columns"
              :pagination="list.page"
              rowKey="id"
              :bordered="false"
              :loading="list.loading"
              :row-selection="list.rowSelection"
              v-model:selectedKeys="list.selectedRowKeys"
              @page-change="currentPage"
            >
              <template #pagination-left>
                <div style="flex: 1; padding-left: 10px">
                  <a-button
                    type="primary"
                    @click="deleteMutl"
                    size="mini"
                    :loading="deleteMutlLoading"
                    >{{ $t('modeling.dy_common.batchDelete') }}</a-button
                  >
                </div>
              </template>
              <template #operation="{ record }">
                <a-button size="mini" type="text" @click="toDetail(record)">
                  <template #icon><icon-edit /></template>
                  <template #default>{{
                    $t('modeling.dataflow.index.5m77w0y5caw0')
                  }}</template>
                </a-button>
                <a-button
                  size="mini"
                  type="text"
                  @click="openDialog('cu', 'update', record)"
                >
                  <template #icon><icon-tool /></template>
                  <template #default>{{
                    $t('modeling.dataflow.index.5m77w0y5cgg0')
                  }}</template>
                </a-button>
                <a-popconfirm
                  :content="$t('modeling.dataflow.index.5m77w0y5fq00')"
                  type="warning"
                  :ok-text="$t('modeling.dataflow.index.5m77w0y5g400')"
                  :cancel-text="$t('modeling.dataflow.index.5m77w0y5g9k0')"
                  @ok="deleteRows(record)"
                >
                  <a-button size="mini" type="text" status="danger">
                    <template #icon><icon-delete /></template>
                    <template #default>{{
                      $t('modeling.dataflow.index.5m77w0y5ghc0')
                    }}</template>
                  </a-button>
                </a-popconfirm>
              </template>
            </a-table>
          </div>
        </div>
        <c-u ref="cuRef" @finish="cuFinish" />
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import CU from './components/CU.vue'
import {
  Table as ATable,
  InputSearch as AInputSearch,
  Button as AButton,
  Popconfirm as APopconfirm,
  Message,
  Modal
} from '@arco-design/web-vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import { dataFlowDelete, getList } from '@/api/modeling'
import { KeyValue } from '@/api/modeling/request'
import router from '@/router'
import { useI18n } from 'vue-i18n'
import { useModelCommonStore } from '@/store/modules/modeling/common'
const { t } = useI18n()
const mCStore = useModelCommonStore()
mCStore.setI18n(t)
const columns = computed(() => [
  { title: t('modeling.dataflow.index.5m77w0y5in80'), dataIndex: 'name' },
  { title: t('modeling.dataflow.index.5m77w0y5ivk0'), dataIndex: 'tags' },
  { title: t('modeling.dataflow.index.5m77w0y5mb80'), dataIndex: 'clusterId' },
  {
    title: t('modeling.dataflow.index.5m77w0y5mgc0'),
    dataIndex: 'clusterNodeId'
  },
  { title: 'Schema', dataIndex: 'schema' },
  { title: t('modeling.dataflow.index.5m77w0y5qsw0'), dataIndex: 'createTime' },
  { title: t('modeling.dataflow.index.5m77w0y5rm80'), dataIndex: 'createBy' },
  { title: t('modeling.dataflow.index.5m77w0y5ruw0'), slotName: 'operation' }
])
const filter = reactive({
  search: '',
  loading: false
})
const list: {
  data: Array<KeyValue>
  page: KeyValue
  selectedRowKeys: string[]
  loading: boolean
  rowSelection: any
} = reactive({
  data: [],
  page: { pageNum: 1, current: 1, pageSize: 10, total: 10, size: 10 },
  loading: false,
  selectedRowKeys: [],
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false
  }
})
const isFilter = () => {
  filter.loading = true
  list.page.pageNum = 1
  list.page.current = 1
  getListData().then(() => {
    filter.loading = false
  })
}
const currentPage = (e: any) => {
  list.page.pageNum = e
  list.page.current = e
  getListData()
}
const getListData = () =>
  new Promise((resolve) => {
    let sendData: KeyValue = { ...list.page }
    if (filter.search) sendData.name = filter.search
    list.loading = true
    getList(sendData).then((res: KeyValue) => {
      list.loading = false
      if (Number(res.code) === 200) {
        resolve(true)
        res.rows.forEach((item: KeyValue) => {
          if (item.type === 1)
            item.typeText = t('modeling.dataflow.index.5m77w0y5rzk0')
          else if (item.type === 2)
            item.typeText = t('modeling.dataflow.index.5m77w0y5s2s0')
          else if (item.type === 3)
            item.typeText = t('modeling.dataflow.index.5m77w0y5s6o0')
          else if (item.type === 4)
            item.typeText = t('modeling.dataflow.index.5m77w0y5ghc0')
          else item.typeText = ''
        })
        list.data = res.rows
        console.log(res)
        list.page.total = res.data && res.data.total ? res.data.total : 10
      } else resolve(false)
    })
  })
getListData()
const deleteRows = (deleteInfo: KeyValue) => {
  dataFlowDelete(deleteInfo.id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.dataflow.index.5m77w0y5sak0') })
      getListData()
    }
  })
}
const cuRef = ref<null | InstanceType<typeof CU>>(null)
const openDialog = (open: string, type: string, data?: KeyValue) => {
  if (open === 'cu') cuRef.value?.open(type, data)
}
const cuFinish = (type: string) => {
  if (type === 'create') {
    getListData()
  } else if (type === 'update') {
    getListData()
  }
}
const toDetail = (record: KeyValue) => {
  window.$wujie?.props.methods.jump({
    name: `Static-pluginBase-opsModelingDataflowDetail`,
    query: { id: record.id }
  })
}
const deleteMutlLoading = ref(false)
const deleteMutl = () => {
  if (list.selectedRowKeys.length === 0) return
  Modal.warning({
    title: `${t('modeling.dy_common.warning')}`,
    content: `${t('modeling.dy_common.warningBatchDelete1')} ${
      list.selectedRowKeys.length
    } ${t('modeling.dy_common.warningBatchDelete2')}？`,
    onOk: () => {
      let sendData = list.selectedRowKeys.join(',')
      dataFlowDelete(sendData).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          Message.success({
            content: t('modeling.dataflow.index.5m77w0y5sak0')
          })
          getListData()
        }
      })
    }
  })
}
</script>
<style scoped lang="less">
.app-container {
  .main-bd {
    .list {
      .banner {
        height: 140px;
        background-color: #4e9dfe;
        margin-bottom: 10px;
      }
      .tool {
        display: flex;
        align-items: center;
      }
      .table {
        border: 1px solid var(--color-border-2);
        padding-bottom: 20px;
        .header {
          display: flex;
          align-items: center;
          box-sizing: border-box;
          padding: 15px;
          img {
            width: 19px;
            height: 19px;
            margin-right: 15px;
          }
          .text {
            font-size: 16px;
            font-weight: bold;
          }
        }
        .d-links {
          display: flex;
          flex-wrap: wrap;
          width: 110px;
          .d-link {
            color: #2d69ed;
            font-size: 14px;
            display: flex;
            align-items: center;
            position: relative;
            cursor: pointer;
            margin-right: 11px;
            &::after {
              content: '';
              position: absolute;
              right: -6px;
              top: 50%;
              transform: translateY(-50%);
              width: 1px;
              height: 14px;
              background-color: #d4d4d4;
            }
            &:nth-child(3n) {
              margin-right: 0;
              &::after {
                display: none;
              }
            }
            &::before {
              content: '';
              position: absolute;
              bottom: 0;
              left: 0;
              width: 100%;
              height: 1px;
              background-color: #4e9dfe;
              opacity: 0;
            }
            &:hover {
              &::before {
                opacity: 1;
              }
            }
            &:last-child {
              &::before {
                background-color: #ff2a2a;
              }
            }
          }
        }
      }
    }
    .data-flow-list {
      width: 100%;
      height: 100%;
      box-sizing: border-box;
      padding: 8px;
    }
  }
}
</style>
