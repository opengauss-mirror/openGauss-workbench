<template>
  <div class="package-list">
    <a-table
      class="d-a-table-row"
      :data="list.data"
      :columns="columns"
      :pagination="false"
      :loading="list.loading"
    >
      <template #logType="{ record }">
        <span v-if='record.logType === "DB"'>{{$t('operationLog.LogList.3mpn60ejri09')}}</span>
        <span v-if='record.logType === "BENCHMARK"'>{{$t('operationLog.LogList.3mpn60ejri10')}}</span>
        <span v-if='record.logType === "TUNE"'>{{$t('operationLog.LogList.3mpn60ejri11')}}</span>
      </template>
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-link class="" @click="previewLog(record)">{{
            $t('operationLog.LogList.3mpn60ejri08')
          }}</a-link>
          <a-link class="" @click="downUploadLog(record)">{{
            $t('operationLog.LogList.3mpn60ejri07')
          }}</a-link>
        </div>
      </template>
    </a-table>
    <a-modal v-model:visible="modalVisible" :modal-style="{ width: '60vw' }" :bodyStyle="{height: '400px'}" :popupContainer="modalContainer" :title="$t('operationLog.LogList.3mpn60ejri12')"  @cancel="handleCancel" :footer="false"  unmountOnClose >
      <div style="white-space: pre-wrap;" v-html="modalContent"></div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { computed, onMounted, reactive, ref, PropType } from 'vue'
import { downloadTemplate, previewTemplate } from '@/api/ops' // eslint-disable-line
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const modalContent = ref();
const modalVisible = ref(false);
const columns = computed(() => [
  { title: '', width:84 },  
  { title: t('operationLog.LogList.3mpn60ejri00'), dataIndex: 'logType', slotName: 'logType'},
  { title: t('operationLog.LogList.3mpn60ejri04'), dataIndex: 'startTime' },
  { title: t('operationLog.LogList.3mpn60ejri05'), dataIndex: 'fielSize' },
  { title: t('operationLog.LogList.3mpn60ejri06'), slotName: 'operation' }
])

const list = reactive<KeyValue>({
  data: [],
})

const props = defineProps({
  logChildData: {
    type: Array as PropType<KeyValue>,
    required: true
  }
})
onMounted(() => {
  list.data = props.logChildData.details
})
const downUploadLog = (e) => {
  const param = {
    trainingId: props.logChildData.trainingId,
    type: e.logType
  }
  downloadTemplate(param).then((res: any) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      if(e.logType === 'DB'){
        a.download = `${t('operationLog.LogList.3mpn60ejri09')}.log`
      } else if(e.logType === 'BENCHMARK'){
        a.download = `${t('operationLog.LogList.3mpn60ejri10')}.log`
      } else {
        a.download = `${t('operationLog.LogList.3mpn60ejri11')}.log`
      }
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    } else {
      Message.error('Download failed, please try again')
    }
  }).finally(() => {
  })
}

const previewLog = (e) => {
  const param = {
    trainingId: props.logChildData.trainingId,
    type: e.logType
  }
  previewTemplate(param).then((res: any) => {
    if (Number(res.code) === 200) {
      modalContent.value = res.obj;
      modalVisible.value = true;
    } else {
      Message.error('Preview failed, please try again')
    }
  }).finally(() => {
  })
}
const handleCancel = () => {
  modalVisible.value = false;
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
