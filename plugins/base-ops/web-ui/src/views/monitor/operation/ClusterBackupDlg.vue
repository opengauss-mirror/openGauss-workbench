<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title" id="clusterBackUpDlg"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close"
    :okText="$t('operation.ClusterBackupDlg.5mplmzbrmkg0')">
    <a-form :model="data.formData" ref="formRef" :label-col="{ style: { width: '200px' } }" :rules="data.rules">
      <a-form-item :label="$t('operation.ClusterBackupDlg.5mplmzbrntg0')">
        <div class="label-color">{{ data.formData.clusterId }}</div>
      </a-form-item>
      <a-form-item :label="$t('operation.ClusterBackupDlg.5mplmzbro8g0')">
        <a-textarea v-model="data.formData.backupPath"
          :placeholder="$t('operation.ClusterBackupDlg.5mplmzbroj00')"></a-textarea>
      </a-form-item>
      <a-form-item :label="$t('operation.ClusterBackupDlg.5mplmzbrosg0')">
        <a-textarea v-model="data.formData.remark"
          :placeholder="$t('operation.ClusterBackupDlg.5mplmzbrp1s0')"></a-textarea>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">

import { nextTick, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: t('operation.ClusterBackupDlg.5mplmzbrpas0'),
  loading: false,
  formData: {
    clusterId: '',
    backupPath: '',
    remark: ''
  },
  rules: {}
})

const formRef = ref<null | FormInstance>(null)
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const emits = defineEmits(['finish', 'cancel'])

const handleOk = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      emits('finish', data.formData)
      data.show = false
    }
  })
}

const open = (clusterId: string, installerUser: string) => {
  data.show = true
  data.title = t('operation.ClusterBackupDlg.5mplmzbrpas0')
  data.formData.clusterId = clusterId
  data.formData.backupPath = `/home/${installerUser}/backup`
  data.formData.remark = ''
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped></style>
