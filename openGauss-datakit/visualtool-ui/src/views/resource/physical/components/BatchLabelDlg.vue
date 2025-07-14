<template>
  <el-dialog :mask-closable="false" v-model="data.show" :title="data.title" :modal-style="{ width: '450px' }"
    @ok="handleOk" :before-close="close" :z-index="960">
    <el-form v-if="data.show" :model="data.formData" ref="formRef" :rules="data.rules" auto-label-width>
      <el-form-item :label="$t('components.BatchLabelDlg.5pbjt280spg0')">
        {{ data.formData.hostIds.length }} {{ $t('components.BatchLabelDlg.5pbjt280xuo0') }}
      </el-form-item>
      <el-form-item :label="$t('components.BatchLabelDlg.5quz8t9a2xg0')">
        <el-radio-group v-model="data.operationType">
          <el-radio :value="0">{{ $t('components.BatchLabelDlg.5quz8t9a5x00') }}</el-radio>
          <el-radio :value="1">{{ $t('components.BatchLabelDlg.5quz8t9a6ik0') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="data.operationType === 0" field="tags" :label="$t('components.BatchLabelDlg.5pbjt280yik0')"
        validate-trigger="change">
        <el-select :loading="data.tagsLoading" v-model="data.formData.tags"
          :placeholder="$t('components.AddHost.tagsPlaceholder')" allow-create multiple allow-clear>
          <el-option v-for="item in data.tagsList" :key="item.value" :value="item.value">{{
            item.label
          }}</el-option>
        </el-select>
      </el-form-item>
      <div v-else>
        <div v-if="data.commonLabels.length">
          <el-form-item :label="$t('components.BatchLabelDlg.5quz8t9a6zk0')">
            <el-tag class="mr-s" v-for="item in data.commonLabels" :key="item.id">{{ item.name }}</el-tag>
          </el-form-item>
          <el-form-item :label="$t('components.BatchLabelDlg.5quz8t9a7fs0')">
            <el-select v-model="data.formData.removeTags" :placeholder="$t('components.AddHost.tagsPlaceholder')"
              multiple allow-clear>
              <el-option v-for="item in data.commonLabels" :key="item.name" :value="item.hostTagId"
                :label="item.name">{{
                  item.name
                }}</el-option>
            </el-select>
          </el-form-item>
        </div>
        <div style="color: red; font-weight: bold;" v-else>
          {{ $t('components.BatchLabelDlg.5quz8t9a7w00') }}
        </div>
      </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button class="o-dlg-btn" type="primary" size="small" @click="handleOk">{{ t('physical.index.5mphf11t05c0') }}</el-button>
        <el-button class="o-dlg-btn" size="small" @click="close">{{ t('physical.index.5mphf11t0bc0') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useI18n } from 'vue-i18n'
import { hostTagListAll, hostBatchAddTag, hostBatchDelTag } from '@/api/ops'
const { t } = useI18n()
const data = reactive<KeyValue>({
  show: false,
  title: '',
  loading: false,
  tagsLoading: false,
  tagsList: [],
  formData: {
    hostIds: [],
    tags: [],
    removeTags: []
  },
  operationType: 0,
  commonLabels: []
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
    if (result) {
      data.loading = true
      if (data.operationType === 0) {
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
      } else {
        const param = {
          hostIds: data.formData.hostIds,
          names: data.formData.removeTags
        }
        hostBatchDelTag(param).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            emits('finish')
            close()
          }
        }).finally(() => {
          data.loading = false
        })
      }
    }
  })
}

const getAllTag = (commonLabelNames: string[]) => {
  data.tagsLoading = true
  hostTagListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.tagsList = []
      data.commonLabels = []
      res.data.forEach((item: KeyValue) => {
        const temp = {
          label: item.name,
          value: item.name
        }
        data.tagsList.push(temp)
        if (commonLabelNames.indexOf(item.name) > -1) {
          data.commonLabels.push(item)
        }
      })
    }
  }).finally(() => {
    data.tagsLoading = false
  })
}

const open = (hostIds: (string | number)[], commonLabelNames: string[]) => {
  data.show = true
  data.title = t('components.BatchLabelDlg.5pbjt280z040')
  data.formData.hostIds = hostIds
  data.formData.tags = []
  data.operationType = 0
  data.commonLabels = []
  data.formData.removeTags = []
  getAllTag(commonLabelNames)
}

defineExpose({
  open
})
</script>

<style lang="scss" scoped></style>
