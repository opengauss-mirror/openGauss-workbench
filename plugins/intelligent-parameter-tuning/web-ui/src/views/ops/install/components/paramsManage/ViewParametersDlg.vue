<template>
  <a-modal
    :scrollable="true"
    unmount-on-close
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="$t('paramsRecommendation.paramsDetails.9mpn60ejri00')"
    :modal-style="{ width: '60vw' }"
    @cancel="close"
  >
  <div class="session-top-ten-c">
    <a-table
      class="d-a-table-row"
      :data="list.data"
      :columns="columns"
      :loading="list.loading"
      :row-selection="list.rowSelection"
      v-model:selectedKeys="list.selectedRowKeys"
      row-key="parameterName"
      :pagination="false"
      :scroll="{y: 300}"
      :scrollbar="true"
    >
    <template #restart="{ record }">
      <span v-if='record.restart === "是"'>{{$t('install.Offline.5mpn60fjra17')}}</span>
      <span v-if='record.restart === "否"'>{{$t('install.Offline.5mpn60fjrb17')}}</span>
    </template>
    </a-table>
  </div>
  <template #footer>
    <div class="centrClass">
      <a-button size="large"   class="mr-lg" @click="close">{{
        $t('paramsRecommendation.paramsDetails.9mpn60ejri10')
      }}</a-button>
      <a-button size="large"   class="mr-lg" type="primary" @click="applicationParams(true)">{{
        $t('paramsRecommendation.paramsDetails.9mpn60ejri11')
      }}</a-button>
      <a-button size="large"    @click="applicationParams(false)">{{
        $t('paramsRecommendation.paramsDetails.9mpn60ejri12')
      }}</a-button>
    </div>
  </template>
  </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { computed, onMounted, reactive, ref } from 'vue'
import VChart from 'vue-echarts'
import { paramDetails, isApplicationParams } from '@/api/ops'
import { Modal, Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
})
const list = reactive<KeyValue>({
  id: '',
  trainingId: '',
  data: [],
  loading: false,
  selectedRowKeys:[],
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false
  }
})
const columns = computed(() => [
  {
    title: t('paramsRecommendation.paramsDetails.9mpn60ejri06'),
    dataIndex: 'parameterName',
    width: 300
  },
  { title: t('paramsRecommendation.paramsDetails.9mpn60ejri07'), dataIndex: 'initialParameterValues' },
  { title: t('paramsRecommendation.paramsDetails.9mpn60ejri08'), dataIndex: 'suggestedParameterValues' },
  { title: t('paramsRecommendation.paramsDetails.9mpn60ejri09'), dataIndex: 'restart', slotName: 'restart' }
])

const close = () => {
  data.show = false
}
const open = (
  paramsData?: KeyValue
) => {
  data.show = true
  list.id = paramsData.parameterId,
  list.trainingId = paramsData.trainingId,
  getParamsList()
}

const getParamsList = () => {
  list.loading = true
  paramDetails({parameterId:list.id}).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.obj
      } else {
        Message.error('Failed to obtain host information')
      }
    }).finally(() => {
      list.loading = false
    })
}

const applicationParams = (flg) => {
  if (list.selectedRowKeys.length === 0) return
  const selectedRows = list.data.filter(row => list.selectedRowKeys.includes(row.parameterName));
  let param = {
    trainingId: list.trainingId,
    data: selectedRows,
    isOptimization : flg
  }
  if(selectedRows.filter(row => row.restart === '否').length > 0){
    Modal.confirm({
      content: t('paramsRecommendation.paramsDetails.9mpn60ejri17'),
      onOk: () => {
        ApplicationParam(param)
      },
      onCancel: () => {}
    })
  } else{
    ApplicationParam(param)
  }
}
const ApplicationParam = (param) => {
  isApplicationParams(param).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            Message.success(param.isOptimization ? t('paramsRecommendation.paramsDetails.9mpn60ejri13') : t('paramsRecommendation.paramsDetails.9mpn60ejri15'))
          } else {
            Message.error(param.isOptimization ? t('paramsRecommendation.paramsDetails.9mpn60ejri14') : t('paramsRecommendation.paramsDetails.9mpn60ejri16'))
          }
        }).finally(() => {
          data.show = false
        }).catch((error) => {
          Message.error(error)
        })
}
defineExpose({
  open
})
</script>
<style lang="less" scoped>
  .session-top-ten-c {
    border-radius: 4px;
    background-color: var(--session-top-bg);
  }
  .centrClass {
    text-align: center;
  }
</style>
