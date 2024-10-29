<template>
  <div class="package-list" id="tuningLogsManage">
    <div class="flex-between flex-supplement mb">
      <div class="flex-row">
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">
            {{ $t('taskList.TaskList.7mpn70ejri01') }}
          </div>
          <a-select
            v-model="filter.clusterName"
            :placeholder="$t('taskList.TaskList.7mpn80ejri01')"
            style="width: 210px;"
          >
            <a-option
              v-for="(item, index) in selectParams.clusterNameList"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </a-select>
        </div>
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">
            {{ $t('taskList.TaskList.7mpn70ejri03') }}
          </div>
          <a-range-picker
          show-time
          :allow-clear="false"
          :time-picker-props="{ defaultValue: ['00:00:00', '23:59:59'] }"
          format="YYYY-MM-DD HH:mm:ss"
          @ok="dateOnOk"
        />
        </div>
        <div class="flex-row">
          <a-button type="primary" class="" @click="isFilter">
            <template #icon>
              <icon-search />
            </template>
            {{ $t('taskList.TaskList.7mpn70ejri04') }}
          </a-button>
          <a-button type="primary" style="margin-left: 12px;" @click="deleteLog" >
            <template #icon>
              <icon-delete />
            </template>
            {{ $t('taskList.TaskList.7mpn70ejri20') }}
          </a-button>
        </div>
      </div>
    </div>
    <a-table 
    row-key="logId" 
    :pagination="list.page" 
    @page-change="currentPage" 
    :data="list.data" 
    :columns="columns" 
    :expandable="expandable" 
    @expand="handleExpand"       
    :row-selection="list.rowSelection"
    v-model:selectedKeys="list.selectedRowKeys"
     />
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { Message, Descriptions, Modal } from '@arco-design/web-vue'
import { computed, onMounted, reactive, ref, h, inject } from 'vue'
import { getLogList, getLogClusterName, deleteLogList } from '@/api/ops' // eslint-disable-line
import LogTable from './LogTable.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const opsStore = useOpsStore()
const selectParams = reactive({
  clusterNameList: []
});
const filter = reactive({
  clusterName: '',
  timeInterval: [],
  pageNum: 1,
  pageSize: 10
})
const expandable = reactive<KeyValue>({
  title: '',
  width: 100,
  expandedRowKeys: [],
  defaultExpandAllRows: true,
  expandedRowRender: (record) => {
    if (record.details && record.details.length > 0) {
      return h('div', {}, [
        h(LogTable, {
          logChildData: record,
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
      expandable.expandedRowKeys.push(rowKey)
    }
  } else {
    expandable.expandedRowKeys.push(rowKey)
  }
}
const columns = computed(() => [
  {
    title: t('taskList.TaskList.7mpn70ejri01'),
    dataIndex: 'clusterName',
    ellipsis: true,
    tooltip: true
  },
  { title: t('taskList.TaskList.7mpn70ejri07'), dataIndex: 'startTime' },
])

const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    pageSize: 10,
    'show-total': true
  },
  selectedRowKeys:[],
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false
  },
  loading: false
})

onMounted(async () => {
  await getAllClusterName()
  getListData()
})
const getAllClusterName = () => {
  getLogClusterName().then((res: KeyValue) => {
    let clusterNameList = []
    if (Number(res.code) === 200) {
      res.obj.forEach((item: KeyValue) => {
        const temp = {
          label: item,
          value: item
        }
        clusterNameList.push(temp)
      })
      selectParams.clusterNameList = clusterNameList
    }
  }).finally(() => {
  })
}
const dateOnOk = (date: any) => {
  filter.timeInterval = date
};
const deleteLog = () => {
  if(list.selectedRowKeys.length === 0){
    return 
  } else {
    Modal.confirm({
        content: t("taskList.TaskList.7mpn80ejri03"),
        bodyStyle:"text-align:center",
        width:"260px",
        onOk: () => {
          deleteLogList(list.selectedRowKeys).then((res: KeyValue) => {
            let dbList = []
            if (Number(res.code) === 200) {
              Message.success(res.msg);
              getListData()
            }
          }).finally(() => {
          })
        },
        onCancel() {},
      });
  }
}
const getListData = () => {
  list.loading = true
  getLogList(filter)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.obj
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
  filter.pageNum = 1
  getListData()
}
const installStore = computed(() => opsStore.getInstallConfig)
</script>
<style lang="less" scoped>
.package-list {
  padding: 4px 20px 20px 20px;
  border-radius: 8px;
  .flex-supplement {
    flex-direction: row-reverse;
  }
  .top-label {
    white-space: nowrap;
  }

  .select-w {
    width: 200px;
  }
}
</style>
