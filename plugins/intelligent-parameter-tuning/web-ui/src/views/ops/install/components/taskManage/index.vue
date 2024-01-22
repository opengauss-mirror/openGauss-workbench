<template>
  <div class="package-list">
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
            {{ $t('taskList.TaskList.7mpn70ejri02') }}
          </div>
          <a-select
            v-model="filter.db"
            :placeholder="$t('taskList.TaskList.7mpn80ejri02')"
            style="width: 210px;"
          >
            <a-option
              v-for="(item, index) in selectParams.dbList"
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
          <a-button type="primary" style="margin-left: 12px;" @click="deleteTask" >
            <template #icon>
              <icon-delete />
            </template>
            {{ $t('taskList.TaskList.7mpn70ejri20') }}
          </a-button>
        </div>
      </div>
    </div>
    <a-table
      class="d-a-table-row"
      :data="list.data"
      :columns="columns"
      :pagination="list.page"
      @page-change="currentPage"
      :loading="list.loading"
      :row-selection="list.rowSelection"
      v-model:selectedKeys="list.selectedRowKeys"
      row-key="trainingId"
    >
      <template #process="{ record }">
        <div class="flex-row-start" @click="handleProgressDetail(record)" >
          <a-progress :status="record.status === '2' ? 'danger' : (record.status === '0' ? 'normal' : 'success')" :percent="Number(record.process/100)" />
        </div>
      </template>
      <template #online="{ record }">
        <span v-if='record.online === "True"'>{{$t('taskList.TaskList.7mpn70ejri18')}}</span>
        <span v-if='record.online === "False"'>{{$t('taskList.TaskList.7mpn70ejri17')}}</span>
      </template>
      <template #status="{ record }">
        <span v-if='record.status === "0"'>{{$t('taskList.TaskList.7mpn70ejri11')}}</span>
        <span v-if='record.status === "1"'>{{$t('taskList.TaskList.7mpn70ejri12')}}</span>
        <span v-if='record.status === "2"'>{{$t('taskList.TaskList.7mpn70ejri13')}}</span>
      </template>
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-link class="" @click="handleRetraining(record)">{{
            $t('taskList.TaskList.7mpn70ejri15')
          }}</a-link>
          <a-link class="" @click="handleTaskDetail(record)">{{
            $t('taskList.TaskList.7mpn70ejri10')
          }}</a-link>
          <a-link class="" @click="stopTask(record)">{{
            $t('taskList.TaskList.7mpn70ejri19')
          }}</a-link>
        </div>
      </template>
    </a-table>
    <progress-details
      ref="progressDetailsRef"
    ></progress-details>
    <task-details
      ref="taskDetailsRef"
    ></task-details>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { Message, Modal } from '@arco-design/web-vue'
import { useOpsStore } from '@/store'
import { getTaskClusterName, getTaskDatabase, deleteTaskList, stopTaskList } from '@/api/ops' // eslint-disable-line
import { computed, onBeforeUnmount, onMounted, reactive, ref, inject, watch } from 'vue'
import ProgressDetails from './ProgressDetailsDlg.vue'
import TaskDetails from './TaskDetailsDlg.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const modalContent = ref();
const modalVisible = ref(false);
const filter = reactive({
  clusterName: '',
  db: '',
  timeInterval: '',
  pageNum: 1,
  pageSize: 10
})
const opsStore = useOpsStore()
const selectParams = reactive({
  clusterNameList: [],
  dbList: []
});
let activeTabFunc = inject("activeTab")
const changeTabsFunc = inject<any>('changeTabs')
const searchListWs = ref<Socket<any, any> | undefined>()
const columns = computed(() => [
  {
    title: t('taskList.TaskList.7mpn70ejri01'),
    dataIndex: 'clusterName',
    width: 180,
    ellipsis: true,
    tooltip: true
  },
  { title: t('taskList.TaskList.7mpn70ejri02'), dataIndex: 'db' },
  { title: t('taskList.TaskList.7mpn70ejri05'), dataIndex: 'online', slotName: 'online', width: 100 },
  { title: t('taskList.TaskList.7mpn70ejri06'), dataIndex: 'benchmark' },
  { title: t('taskList.TaskList.7mpn70ejri07'), dataIndex: 'startTime', width: 200 },
  {
    title: t('taskList.TaskList.7mpn70ejri08'),
    width: 160,
    dataIndex: 'process',
    slotName: 'process'
  },
  {
    title: t('taskList.TaskList.7mpn70ejri09'),
    dataIndex: 'status',
    slotName: 'status'
  },
  {
    title: t('taskList.TaskList.7mpn70ejri14'),
    slotName: 'operation', width: 200,
  }
])
const installStore = computed(() => opsStore.getInstallConfig)
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
watch(activeTabFunc, (newValue) => {
  if(newValue !== '2'){
    searchListWs.ws.destroy()
  }
},{deep:true})
onMounted(async () => {
    await getAllClusterName()
    await getAlldbName()
    const socketKey = new Date().getTime()
    const websocket = new Socket({ url: `COMMAND_EXEC_${socketKey}` })
    searchListWs.ws = websocket
    searchListWs.value = websocket
    websocket.onopen(() => {
      list.loading = true
      websocket.send(filter);
    })
    websocket.onmessage((messageData: any) => {
      list.loading = false
      const receivedData = JSON.parse(messageData);
      if (Number(receivedData.code) === 200) {
        list.data = receivedData.obj
        list.page.total = receivedData.total
        let timerId = ''
        if(!timerId){
           timerId = setTimeout(() => {
            websocket.send(filter);
          }, 2000);
        }
        if (!needRefresh() && timerId) {
          clearTimeout(timerId)
        }
      }
    })
})
const getAllClusterName = () => {
  getTaskClusterName().then((res: KeyValue) => {
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
const getAlldbName = () => {
  getTaskDatabase().then((res: KeyValue) => {
    let dbList = []
    if (Number(res.code) === 200) {
      res.obj.forEach((item: KeyValue) => {
        const temp = {
          label: item,
          value: item
        }
        dbList.push(temp)
      })
      selectParams.dbList = dbList
    }
  }).finally(() => {
  })
}
onBeforeUnmount(() => {
  searchListWs.value?.destroy()
})
const getListData = () => {
  if(searchListWs?.value){
    searchListWs.ws.send(filter);
  }
}
const handleCancel = () => {
  modalVisible.value = false;
}
const dateOnOk = (date: any) => {
  filter.timeInterval = date
};

const needRefresh = () => {
      for (const item of list.data) {
        if (item.status === "0") {
          return true;
        }
      }
      return false;
  }
const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const isFilter = () => {
  filter.pageNum = 1
  getListData()
}

const progressDetailsRef = ref<null | InstanceType<typeof ProgressDetails>>(null)
const taskDetailsRef = ref<null | InstanceType<typeof TaskDetails>>(null)
const handleProgressDetail = (data?: KeyValue) => {
  progressDetailsRef.value?.open(data)
}
const handleTaskDetail = (data?: KeyValue) => {
  taskDetailsRef.value?.open(data)
}
const deleteTask = () => {
  if(list.selectedRowKeys.length === 0){
    return 
  } else {
    Modal.confirm({
        content: t("taskList.TaskList.7mpn80ejri03"),
        bodyStyle:"text-align:center",
        width:"260px",
        onOk: () => {
          if (list.selectedRowKeys.length === 0) return
          deleteTaskList(list.selectedRowKeys).then((res: KeyValue) => {
            let dbList = []
            if (Number(res.code) === 200) {
              Message.success(res.msg);
              searchListWs.ws.send(filter);
            }
          }).finally(() => {
          })
        },
        onCancel() {},
      });
  }
}
const stopTask = (e) => {
  stopTaskList([e.trainingId]).then((res: KeyValue) => {
    let dbList = []
    if (Number(res.code) === 200) {
      Message.success(res.msg);
      searchListWs.ws.send(filter);
    }
  }).finally(() => {
  })
}
const handleRetraining = (data?: KeyValue) => {
  opsStore.setInstallContext({
    installMode: data.online === 'True' ? 'ON_LINE' : 'OFF_LINE',
    benchMark: data.benchmark === 'dwg' ? 'DWG' : 'SYSBENCH',
    isCopy: true,
    copyData: data
    })
  changeTabsFunc.handleSwitchTab("1")
}
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
