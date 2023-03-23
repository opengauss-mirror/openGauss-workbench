<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '450px' }" @ok="handleOk" @cancel="close">
    <a-form v-if="data.show" :model="data.formData" ref="formRef" :rules="data.rules" auto-label-width>
      <a-form-item :label="$t('components.BatchLabelDlg.5pbjt280spg0')">
        {{ data.formData.hostIds.length }} {{ $t('components.BatchLabelDlg.5pbjt280xuo0') }}
      </a-form-item>
      <a-form-item field="tags" :label="$t('components.BatchLabelDlg.5pbjt280yik0')" validate-trigger="change">
        <a-select :loading="data.tagsLoading" v-model="data.formData.tags"
          :placeholder="$t('components.AddHost.tagsPlaceholder')" allow-create multiple allow-clear>
          <a-option v-for="item in data.tagsList" :key="item.value" :value="item.value">{{
            item.label
          }}</a-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">

import { nextTick, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { hostTagListAll, hostBatchAddTag } from '@/api/ops'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: '',
  loading: false,
  tagsLoading: false,
  tagsList: [],
  formData: {
    hostIds: [],
    tags: []
  }
})

const formRef = ref<null | FormInstance>(null)
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const emits = defineEmits([`finish`])

const handleOk = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      data.loading = true
      const param = {
        hostIds: data.formData.hostIds,
        names: data.formData.tags
      }
      hostBatchAddTag(param).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          emits('finish')
          close()
        }
      }).finally(() => {
        data.loading = false
      })
    }
  })
}

const getAllTag = () => {
  data.tagsLoading = true
  hostTagListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.tagsList = []
      res.data.forEach((item: KeyValue) => {
        const temp = {
          label: item.name,
          value: item.name
        }
        data.tagsList.push(temp)
      })
    }
  }).finally(() => {
    data.tagsLoading = false
  })
}

const open = (hostIds: (string | number)[]) => {
  data.show = true
  data.title = t('components.BatchLabelDlg.5pbjt280z040')
  data.formData.hostIds = hostIds
  data.formData.tags = []
  getAllTag()
}

defineExpose({
  open
})

</script>

<style lang="scss" scoped></style>
