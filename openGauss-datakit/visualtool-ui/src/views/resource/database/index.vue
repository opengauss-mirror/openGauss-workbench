<template>
  <div class="app-container" id="databaseManage">
    <div class="main-bd">
      <div class="upgrade-container">
        <div class="flex-between mb-s">
          <div>
            <div class="flex-row cond-btns">
              <a-button type="primary" @click="handleAdd('create')">
                <template #icon>
                  <icon-plus />
                </template>
                {{ $t('database.index.5oxhr0qz15w0') }}</a-button>
              <a-space direction="vertical" :style="{ width: '100%' }" class="mr-s">
                <a-upload action="/" @before-upload="beforeUpload" accept=".csv" />
              </a-space>
              <a-button :loading="list.downloadLoading" type="outline" @click="downloadTemp">
                <template #icon>
                  <icon-download />
                </template>
                {{ $t('database.index.5oxhr0qz2bs0') }}</a-button>
            </div>
          </div>
          <div>
            <a-form :model="filter" layout="inline">
              <a-form-item field="name" :label="$t('database.index.else1')">
                <a-input v-model.trim="filter.name" allow-clear :placeholder="$t('database.index.5oxhr0qz2s00')"
                  style="width: 180px;"></a-input>
              </a-form-item>
              <a-form-item field="ip" :label="$t('database.index.elseIp')">
                <a-input v-model.trim="filter.ip" allow-clear :placeholder="$t('database.index.elseIpPlaceholder')"
                  style="width: 170px;"></a-input>
              </a-form-item>
              <a-form-item field="type" :label="$t('database.index.else3')">
                <a-select v-model="filter.type" allow-clear :placeholder="$t('database.index.else3Placeholder')"
                  style="width: 150px;">
                  <a-option value="OPENGAUSS">openGauss</a-option>
                  <a-option value="MYSQL">MySQL</a-option>
                  <a-option value="POSTGRESQL">PostgreSQL</a-option>
                </a-select>
              </a-form-item>
              <a-form-item>
                <a-button type="outline" @click="getListData()">
                  <template #icon>
                    <icon-search />
                  </template>
                  <template #default>{{ $t('database.index.5oxhr0qz30g0') }}</template>
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-table style="height: 95%" :data="list.data" :columns="columns" :pagination="list.page" :loading="list.loading"
          @page-change="currentPage" @page-size-change="pageSizeChange" row-key="clusterId" @expand="handleExpand"
          :expandable="expandable">
          <template #dbType="{ record }">
            <a-text bordered v-if="record.dbType === 'MYSQL'">MySQL</a-text>
            <a-text bordered v-if="record.dbType === 'OPENGAUSS'">openGauss</a-text>
            <a-text bordered v-if="record.dbType === 'POSTGRESQL'">PostgreSQL</a-text>
          </template>
          <template #status="{ record }">
            <a-tag bordered v-if="record.state === -1">checking</a-tag>
            <a-tag bordered color="red" v-if="record.state === 0">error</a-tag>
            <a-tag bordered color="green" v-if="record.state === 1">running</a-tag>
          </template>
          <template #operation="{ record }">
            <div class="flex-row">
              <a-link class="mr" @click="handleAdd('update', record)">{{ $t('database.index.5oxhr0qz37o0') }}</a-link>
              <a-popconfirm :content="$t('database.index.5oxhr0qz3f40')" type="warning"
                :ok-text="$t('database.index.5oxhr0qz3m80')" :cancel-text="$t('database.index.5oxhr0qz3t80')"
                @ok="handleDel(record)">
                <a-link status="danger">{{ $t('database.index.5oxhr0qz40k0') }}</a-link>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
        <add-jdbc ref="addJdbcRef" @finish="getListData"></add-jdbc>
        <host-import-dlg ref="hostImportDlgRef"></host-import-dlg>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { onMounted, reactive, computed, ref, h } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { jdbcPage, delJdbc, uploadFileJdbc, downloadTemplate, uploadRealJdbc } from '@/api/ops'
import AddJdbc from './AddJdbc.vue'
import HostImportDlg from './HostImportDlg.vue'
import JdbcNodeTable from './JdbcNodeTable.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const expandable = reactive<KeyValue>({
  title: '',
  width: 50,
  expandedRowKeys: [],
  defaultExpandAllRows: true,
  expandedRowRender: (record: KeyValue) => {
    if (record.nodes && record.nodes.length > 0) {
      return h('div', {}, [
        h(JdbcNodeTable, {
          jdbcData: record,
          onValidRes (val: boolean) {
            console.log('receive state', val)
            record.state = val
          }
        })
      ])
    }
    return ''
  }
})

const handleExpand = (rowKey: string | number) => {
  if (expandable.expandedRowKeys.length > 0) {
    let index = expandable.expandedRowKeys.indexOf(rowKey)
    if (index > -1) {
      expandable.expandedRowKeys.splice(index, 1)
    } else {
      // expandable.expandedRowKeys.splice(0, expandable.expandedRowKeys.length)
      expandable.expandedRowKeys.push(rowKey)
    }
  } else {
    expandable.expandedRowKeys.push(rowKey)
  }
}

const columns = computed(() => [
  { title: t('database.index.5oxhr0qz48w0'), dataIndex: 'name', width: 300, ellipsis: true, tooltip: true },
  { title: t('database.index.5oxhr0qz4fs0'), dataIndex: 'dbType', width: 220, slotName: 'dbType' },
  { title: t('database.index.5oxhr0qz4no0'), dataIndex: 'status', width: 200, slotName: 'status' },
  { title: t('database.index.5oxhr0qz4zk0'), dataIndex: 'updateTime', width: 300 },
  { title: t('database.index.5oxhr0qz58o0'), slotName: 'operation', width: 300, fixed: 'right' }
])

const filter = reactive({
  name: '',
  ip: '',
  type: '',
  pageNum: 1,
  pageSize: 10
})

const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    'show-total': true,
    'show-jumper': true,
    'show-page-size': true
  },
  loading: false,
  downloadLoading: false,
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true
  },
  selectedKeys: []
})

onMounted(() => {
  getListData()
})

const getListData = () => {
  list.loading = true
  jdbcPage(filter).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      list.data = []
      expandable.expandedRowKeys = []
      res.rows.forEach((item: KeyValue) => {
        item.state = -1
        item.loading = false
        list.data.push(item)
        expandable.expandedRowKeys.push(item.clusterId)
      })
      list.page.total = res.total
    }
  }).finally(() => {
    list.loading = false
  })
}

const beforeUpload = (file?: any) => {
  return new Promise((resolve, reject) => {
    Modal.confirm({
      title: t('database.index.5oxhr0qz5g40'),
      content: `${t('database.index.else2')} ${file.name}`,
      onOk: () => {
        if (file) {
          handleUpload(file)
        }
      },
      onCancel: () => reject('cancel')
    })
  })
}

const hostImportDlgRef = ref<null | InstanceType<typeof HostImportDlg>>(null)
// import analysis
const handleUpload = (fileObj: any) => {
  const index1 = fileObj.name.lastIndexOf('.')
  const index2 = fileObj.name.length
  const type = fileObj.name.substring(index1, index2)
  let flag = true
  const file6 = '.csv'
  if (file6 !== type) {
    flag = false
  }
  if (!flag) {
    Message.error('Only.csv files can be uploaded. Upload the files again')
    return
  } else {
    const data = new FormData()
    data.append('file', fileObj)
    list.loading = true
    uploadFileJdbc(data).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        if (res.data.succNum === res.data.total) {
          handleRealUpload(fileObj)
        }
        if (res.data.failNum > 0 && res.data.failDetail.length) {
          hostImportDlgRef.value?.open(res.data.failDetail)
        }
      }
    }).finally(() => {
      list.loading = false
    })
  }
}

// real import
const handleRealUpload = (fileObj: any) => {
  const data = new FormData()
  data.append('file', fileObj)
  list.loading = true
  uploadRealJdbc(data).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success('Import successfully')
      getListData()
    }
  }).finally(() => {
    list.loading = false
  })
}

const addJdbcRef = ref<null | InstanceType<typeof AddJdbc>>(null)
const handleAdd = (type: string, data?: KeyValue) => {
  addJdbcRef.value?.open(type, data)
}

const handleDel = (record: KeyValue) => {
  delJdbc(record.clusterId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({
        content: 'delete success'
      })
      getListData()
    }
  })
}

const downloadTemp = () => {
  list.downloadLoading = true
  downloadTemplate().then((res: any) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = 'jdbc-template.csv'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    } else {
      Message.error('Download failed, please try again')
    }
  }).finally(() => {
    list.downloadLoading = false
  })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const pageSizeChange = (e: number) => {
  filter.pageSize = e
  getListData()
}
</script>

<style lang="less" scoped>
:deep(.arco-table-container .arco-table-content-scroll-x) {
  overflow: scroll;
}

.app-container {
  .main-bd {
    .upgrade-container {
      padding: 20px;
      box-sizing: border-box;
      height: calc(100vh - 76px - 40px);

      .top-label {
        width: 200px;
        text-align: right;
      }

      .cond-btns {
        button {
          margin-right: 10px;
        }
      }
    }
  }
}
</style>
