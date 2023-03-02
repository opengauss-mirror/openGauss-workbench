<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="upgrade-container">
        <div class="flex-between mb-s">
          <div class="flex-row cond-btns">
            <a-button type="primary" @click="handleAdd('create')">
              <template #icon>
                <icon-plus />
              </template>
              创建</a-button>
            <a-space direction="vertical" :style="{ width: '100%' }" class="mr-s">
              <a-upload action="/" @before-upload="beforeUpload" />
            </a-space>
            <a-button type="outline" @click="downloadTemplate">
              <template #icon>
                <icon-download />
              </template>
              下载模板</a-button>
          </div>
          <div class="flex-row">
            <div class="flex-row mr">
              <div class="label-color top-label mr-s">数据库实例名称:</div>
              <a-input v-model="filter.name" placeholder="请输入数据库实例名称进行查询"></a-input>
            </div>
            <a-button type="primary" @click="getListData">查询</a-button>
          </div>
        </div>
        <a-table class="full-h" :data="list.data" :columns="columns" :pagination="list.page" :loading="list.loading"
          @page-change="currentPage" @page-size-change="pageSizeChange" row-key="clusterId" @expand="handleExpand"
          :expandable="expandable">
          <template #status="{ record }">
            <a-tag bordered v-if="record.state === -1">checking</a-tag>
            <a-tag bordered color="red" v-if="record.state === 0">error</a-tag>
            <a-tag bordered color="green" v-if="record.state === 1">running</a-tag>
          </template>
          <template #operation="{ record }">
            <div class="flex-row">
              <!-- <a-link class="mr" @click="handleDetail(record)">详情</a-link> -->
              <a-link class="mr" @click="handleAdd('update', record)">修改</a-link>
              <a-popconfirm content="确定要删除？" type="warning" ok-text="确定" cancel-text="取消" @ok="handleDel(record)">
                <a-link status="danger">删除</a-link>
              </a-popconfirm>
            </div>
          </template>
        </a-table>
        <add-jdbc ref="addJdbcRef" @finish="getListData"></add-jdbc>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { onMounted, reactive, computed, ref, h } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { jdbcPage, delJdbc, uploadFileJdbc } from '@/api/ops'
import AddJdbc from './AddJdbc.vue'
// import { useI18n } from 'vue-i18n'
// const { t } = useI18n()
import JdbcNodeTable from './JdbcNodeTable.vue'
const expandable = reactive<KeyValue>({
  title: '',
  width: 50,
  expandedRowKeys: [],
  defaultExpandAllRows: true,
  expandedRowRender: (record: KeyValue) => {
    if (record.nodes && record.nodes.length > 0) {
      return h('div', {}, [
        h(JdbcNodeTable, {
          nodes: record.nodes,
          onValidRes(val: boolean) {
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
  { title: '集群名称', dataIndex: 'name' },
  { title: '类型', dataIndex: 'dbType' },
  { title: '状态', dataIndex: 'status', slotName: 'status' },
  { title: '更新时间', dataIndex: 'updateTime' },
  { title: '操作', slotName: 'operation', width: 350 }
])

const filter = reactive({
  name: '',
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
      title: '上传确认',
      content: `确认上传 ${file.name}`,
      onOk: () => {
        if (file) {
          handleUpload(file)
        }
      },
      onCancel: () => reject('cancel')
    })
  })
}

const handleUpload = (fileData: any) => {
  const fileObj = fileData.file
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
        list.loading = false
        Message.success('Import successfully')
        getListData()
      }
    }).catch(() => {
      list.loading = false
    })
  }
}

const addJdbcRef = ref<null | InstanceType<typeof AddJdbc>>(null)
const handleAdd = (type: string, data?: KeyValue) => {
  addJdbcRef.value?.open(type, data)
}

const handleDetail = (record: KeyValue) => {
  console.log('show record', record)
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

const downloadTemplate = () => {
  // window.location.href = './jdbcTemplate.json'
  var a = document.createElement('a')
  a.download = 'jdbc-template.csv'
  a.href = './template.csv'
  a.click()
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
      padding: 8px;
      height: calc(100vh - 138px - 40px);

      .top-label {
        width: 200px
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
