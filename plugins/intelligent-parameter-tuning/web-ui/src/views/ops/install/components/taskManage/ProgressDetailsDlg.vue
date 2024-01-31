<template>
  <a-modal
    :scrollable="true"
    :footer="false"
    :unmount-on-close="true"
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="$t('taskList.ProgressDetails.7mpn80ejri01')"
    :modal-style="{ width: '70vw' }"
    @cancel="close()"
  >
  <div class="session-top-ten-c">
    <a-button type="primary" class="" @click="previewLog">
      <template #icon>
        <icon-select-all />
      </template>
      {{ $t('taskList.TaskList.7mpn70ejri16') }}
    </a-button>
    <div>
      <v-chart
      class="echart-sesion"
      :option="data.progressEchart"
      v-if="data.progressEchart.series[0].data.length > 0 "
    ></v-chart>
     <a-empty v-else />
    </div>
    <a-table
      class="d-a-table-row pd"
      :data="list.data"
      :columns="columns"
      :loading="list.loading"
    >
    </a-table>
  </div>
  </a-modal>
  <a-modal v-model:visible="modalVisible" :modal-style="{ width: '60vw' }" :bodyStyle="{height: '400px'}" :popupContainer="modalContainer" :title="$t('operationLog.LogList.3mpn60ejri12')"  @cancel="handleCancel" :footer="false"  unmountOnClose >
    <div style="white-space: pre-wrap;" v-html="modalContent"></div>
  </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { computed, onMounted, reactive, ref } from 'vue'
import VChart from 'vue-echarts'
import { sessionOption, clearData } from './echarts/option'
import { procssDetails, taskLogPreview } from '@/api/ops'
import { Modal, Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const modalContent = ref();
const modalVisible = ref(false);
const data = reactive<KeyValue>({
  show: false,
  progressEchart: sessionOption
})
const list = reactive<KeyValue>({
  data: [],
  loading: false,
  logPath:''
})
const columns = computed(() => [
  {
    title: t('taskList.ProgressDetails.7mpn80ejri02'),
    dataIndex: 'tuningRound',
    width: 100
  },
  { title: t('taskList.ProgressDetails.7mpn80ejri03'), dataIndex: 'parameterType', width: 100 },
  { title: t('taskList.ProgressDetails.7mpn80ejri04'), dataIndex: 'runningTime', width: 180 },
  { title: t('taskList.ProgressDetails.7mpn80ejri05'), dataIndex: 'trainingParameter' },
  { title: t('taskList.ProgressDetails.7mpn80ejri06'), dataIndex: 'tps', width: 100},
])

const close = () => {
  data.show = false
}
const open = (
  dataDes?: KeyValue
) => {
  data.show = true
  list.logPath = dataDes.logPath
  getProgressList(dataDes)
}
const handleCancel = () => {
  modalVisible.value = false;
}
const previewLog = (e) => {
  const param = {
    logPath: list.logPath
  }
  taskLogPreview(param).then((res: any) => {
    if (Number(res.code) === 200) {
      modalContent.value = res.obj;
      modalVisible.value = true;
    } else {
      Message.error('Preview failed, please try again')
    }
  }).finally(() => {
  })
}
const getProgressList = (param) => {
  list.loading = true
  procssDetails({trainingId:param.trainingId})
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.obj
        clearData()
        data.progressEchart.xAxis.data = []
        data.progressEchart.series[0].data = []
        res.obj.forEach(item => {
          data.progressEchart.xAxis.data.push(item.tuningRound)
          data.progressEchart.series[0].data.push(item.tps)
        });
      } else {
        Message.error('Failed to obtain host information')
      }
    })
    .finally(() => {
      list.loading = false
    })
}

defineExpose({
  open
})
</script>
<style lang="less" scoped>
  .session-top-ten-c {
    max-height: 400px;
    border-radius: 4px;
    background-color: var(--session-top-bg);

    .echart-sesion {
      height: 300px;
      width: 100%;
    }
  }
  .pd {
    padding-bottom: 20px;
  }
</style>
