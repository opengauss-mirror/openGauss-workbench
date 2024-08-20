<template>
  <a-modal
    :title="data.title"
    :visible="data.show"
    :modal-style="{ width: '680px'}"
    @cancel="handleClose"
    @ok="batchExecute"
    unmountOnClose
  >
    <div style="display: flex; align-items: center; justify-content: center;background-color:#FFEBD1;border-radius:8px;">
      <img src="@/assets/images/modeling/ops/task-warning.png" class="task-warning">
      <div
        style="
        width: 599px;
        height: 48px;
        padding-top: 13px;
        padding-left: 8px;">
        执行任务会在目标主机进行数据库安装，请谨慎执行
      </div>
    </div>

    <div style="display:inline-block;color:#576372">确定要执行以下任务吗?</div>
    <a-table
      style="margin-top:8px"
      :data="list.selectedData"
      :pagination="list.page"
      :columns="columns"
      @page-change="currentPage"
    >
      <template #status="{ record }">
        <span v-if="record.status === 'PENDING'">
              待执行
            </span>
        <span v-if="record.status === 'WAITING'">
              <a-spin :loading="true">执行等待中</a-spin>
            </span>
        <span v-if="record.status === 'RUNNING'">
              <a-spin :loading="true">执行中</a-spin>
            </span>
        <span v-if="record.status === 'SUCCESS'">
              执行成功
            </span>
        <span v-if="record.status === 'FAILED'">
              执行失败
            </span>
      </template>
    </a-table>
  </a-modal>
</template>
<script setup lang="ts">
import {KeyValue} from "@/types/global";
import {reactive, computed, watch, toRaw, onMounted} from 'vue'
import {checkTaskStatus, executeTask} from "@/api/ops";
import {Message} from "@arco-design/web-vue";

const data = reactive<KeyValue>({
  show: false,
  title: '执行确认',
})

const filter = reactive({
  name: '',
  packageVersion: '',
  pageNum: 1,
  pageSize: 10
})

const currentPage = (e: number) => {
  filter.pageNum = e
}

const open = (selectTaskIds: string[], taskSeleData: object[]) => {
  data.show = true;
  list.page.total = taskSeleData.length
  list.selectedData = toRaw(taskSeleData);
  list.selectTaskIds = toRaw(selectTaskIds);
  console.log(list.selectedData)
  console.log(list.selectTaskIds)
}
//批量执行
const batchExecute = () => {
  executeTask(list.selectTaskIds)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Message.success("执行" + res.msg);
        checkExecuteStatus();
      }
    })
}
defineExpose({
  open
})
//确认执行，关闭页面
const checkExecuteStatus = () => {
  console.log('checkExecuteStatus')
  data.show = false;
}
const handleClose = () => {
  data.show = false
}

const list = reactive<KeyValue>({
  selectTaskIds:[],
  selectedData:[],
  page: {
    total: 0,
    pageSize: 10,
    'show-total': true,
  },
  loading: false,
})

const columns = computed(() => [
  {title: "任务ID", dataIndex: 'clusterId', width: 90},
  {title: '任务状态', dataIndex: 'status',width: 1, slotName: 'status'},
  {title: '服务器IP', dataIndex: 'hostIp', width: 60,},
  {title: '安装用户', width: 1, dataIndex: 'hostUsername'},
  {title: '服务器系统', dataIndex: 'os', width: 50,},
])

</script>
<style scoped lang="scss">
.task-warning{
  width: 20px;
  height: 20px;
}

</style>
