<template>
  <a-modal
    :title="data.title"
    :visible="data.show"
    :modal-style="{ width: '680px'}"
    @cancel="handleClose"
    @ok="batchExecute"
    unmountOnClose
  >
    <div style="display: flex; align-items: center; justify-content: center;background-color:#FFEBD1;border-radius:8px;height: auto;padding:10px;">
      <img src="@/assets/images/modeling/ops/task-warning.png" class="task-warning">
      <div style="
        width: 599px;
        padding-left: 8px;"
      >
        {{ $t('operation.DailyOps.sl3u5s5cf242') }}
      </div>
    </div>

    <div style="display:inline-block;color:#576372">{{ $t('operation.DailyOps.sl3u5s5cf241') }}</div>
    <a-table
      style="margin-top:8px"
      :data="list.selectedData"
      :pagination="list.page"
      :columns="columns"
      @page-change="currentPage"
    >
      <template #status="{ record }">
        <span v-if="record.status === 'PENDING'">
              {{ $t('operation.DailyOps.sl3u5s5cf214') }}
            </span>
        <span v-if="record.status === 'WAITING'">
              <a-spin :loading="true">{{ $t('operation.DailyOps.sl3u5s5cf215') }}</a-spin>
            </span>
        <span v-if="record.status === 'RUNNING'">
              <a-spin :loading="true">{{ $t('operation.DailyOps.sl3u5s5cf216') }}</a-spin>
            </span>
        <span v-if="record.status === 'SUCCESS'">
              {{ $t('operation.DailyOps.sl3u5s5cf217') }}
            </span>
        <span v-if="record.status === 'FAILED'">
              {{ $t('operation.DailyOps.sl3u5s5cf218') }}
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

import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const data = reactive<KeyValue>({
  show: false,
  title: '',
})
console.log('typeof data.title')
console.log(data.title)
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
  data.title = t('operation.DailyOps.sl3u5s5cf243')
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
        Message.success(res.msg);
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
  {title: t('operation.DailyOps.sl3u5s5cf244'), dataIndex: 'clusterId', width: 90},
  {title: t('operation.DailyOps.sl3u5s5cf245'), dataIndex: 'status',width: 1, slotName: 'status'},
  {title: t('operation.DailyOps.sl3u5s5cf246'), dataIndex: 'hostIp', width: 60,},
  {title: t('enterprise.NodeConfig.5mpme7w6bak0'), width: 1, dataIndex: 'hostUsername'},
  {title: t('operation.DailyOps.sl3u5s5cf230'), dataIndex: 'os', width: 50,},
])

</script>
<style scoped lang="scss">
.task-warning{
  width: 20px;
  height: 20px;
}

</style>
