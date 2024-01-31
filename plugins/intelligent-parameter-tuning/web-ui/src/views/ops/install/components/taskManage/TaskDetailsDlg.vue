<template>
  <a-modal
    :footer="false"
    :unmount-on-close="true"
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="$t('taskList.TaskDetails.7mpn90ejri01')"
    :modal-style="{ width: '50vw' }"
    @cancel="close()"
  >
  <a-descriptions :column="2" bordered >
    <a-descriptions-item v-for="item of dataDescription" :label="item.label" :span="item.span ?? 1">
      {{ item.value }}
    </a-descriptions-item>
  </a-descriptions>
  </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { computed, reactive, ref } from 'vue'
import {
  addPackage,
  analysisPkg,
  delPkgTar,
  editPackage,
  getSysUploadPath,
  hasPkgName
} from '@/api/ops'
import { Modal, Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
})
const descriptionObj = ref({});

const dataDescription = computed(() => [{
      label: t('taskList.TaskList.7mpn70ejri01'),
      value: descriptionObj.value.clusterName,
      span: 2,
    }, {
      label: t('taskList.TaskList.7mpn70ejri02'),
      value: descriptionObj.value.db,
    }, {
      label: t('install.Offline.5mpn60ejri14'),
      value: descriptionObj.value.schemaName,
    }, {
      label: t('taskList.TaskList.7mpn70ejri05'),
      value: descriptionObj.value.online === 'True' ? t('taskList.TaskList.7mpn70ejri18') : t('taskList.TaskList.7mpn70ejri17'),
    }, {
      label: t('taskList.TaskList.7mpn70ejri06'),
      value: descriptionObj.value.benchmark,
    }, {
      label: t('taskList.TaskList.7mpn70ejri07'),
      value: descriptionObj.value.startTime,
    }, {
      label: t('taskList.TaskList.7mpn70ejri09'),
      value: descriptionObj.value.status,
    }, {
      label: t('install.Offline.5mpn60ejri21'),
      value: descriptionObj.value.mode,
    }, {
      label: t('install.Offline.5mpn60ejri24'),
      value: descriptionObj.value.threads,
    }, {
      label: t('install.Offline.5mpn60ejri22'),
      value: descriptionObj.value.samplingNumber,
    }, {
      label: t('install.Offline.5mpn60ejri23'),
      value: descriptionObj.value.iteration,
    }, {
      label: t('install.Offline.5mpn60ejri25'),
      value: descriptionObj.value.runningTime,
    }, {
      label: t('install.Offline.5mpn60ejri26'),
      value: descriptionObj.value.rankedKnobsNumber,
    }]);

const close = () => {
  data.show = false
}

const open = (
  dataDes?: KeyValue
) => {
  descriptionObj.value = dataDes
  data.show = true
}

defineExpose({
  open
})
</script>
<style lang="less" scoped>
</style>
