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
              <div class="top-label mr-s">数据库实例名称:</div>
              <a-input v-model="filter.search" placeholder="请输入数据库实例名称进行查询"></a-input>
            </div>
            <a-button type="primary" @click="getListData">查询</a-button>
          </div>
        </div>
        <a-table class="d-a-table-row full-h" :data="list.data" :columns="columns" :pagination="list.page"
          :loading="list.loading" @page-change="currentPage">
          <template #ip="{ record }">
            {{ record.nodes[0].ip }}
          </template>
          <template #port="{ record }">
            {{ record.nodes[0].port }}
          </template>
          <template #baseInfo="{}">
            <div class="flex-row">
              <div class="flex-col mr">
                <div>20</div>
                <div>会话</div>
              </div>
              <div class="flex-col mr">
                <div>2</div>
                <div>连接数</div>
              </div>
              <div class="flex-col mr">
                <div>20MB</div>
                <div>内存占用</div>
              </div>
              <div class="flex-col mr">
                <div>200</div>
                <div>qps</div>
              </div>
              <div class="flex-col">
                <div>300</div>
                <div>tps</div>
              </div>
            </div>
          </template>
          <template #warning="{}">
            --
          </template>
          <template #status="{}">
            <a-tag color="green">running</a-tag>
          </template>
          <template #operation="{ record }">
            <div class="flex-row">
              <a-link class="mr" @click="handleDetail(record)">详情</a-link>
              <a-link class="mr" @click="handleWarningConfig(record)">告警配置</a-link>
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
import { onMounted, reactive, computed, ref } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
// import { useI18n } from 'vue-i18n'
import { jdbcPage, delJdbc, uploadFileJdbc } from '@/api/ops'
import AddJdbc from './AddJdbc.vue'
// const { t } = useI18n()

const columns = computed(() => [
  { title: '实例名称', dataIndex: 'name' },
  { title: '类型', dataIndex: 'dbType' },
  { title: 'IP地址', slotName: 'ip' },
  { title: '端口', slotName: 'port' },
  { title: '基本信息', slotName: 'baseInfo' },
  { title: '告警', slotName: 'warning' },
  { title: '状态', slotName: 'status' },
  { title: '操作', slotName: 'operation' }
])

const filter = reactive({
  search: '',
  pageNum: 1,
  pageSize: 10
})

const list = reactive<KeyValue>({
  data: [],
  page: { page: 1, pageSize: 10, total: 0 },
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
      res.rows.forEach((item: KeyValue) => {
        item.state = -1
        item.loading = false
        list.data.push(item)
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

const handleWarningConfig = (record: KeyValue) => {
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
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .upgrade-container {
      padding: 20px;
      box-sizing: border-box;
      padding: 8px;
      background-color: #FFF;
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
