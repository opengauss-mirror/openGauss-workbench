<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title" @cancel="close"
    :modal-style="{ width: '550px' }" :footer="false">
    <div class="flex-col-start">
      <label class="mb">{{ $t('database.HostImportDlg.5oxhmjuzltc0') }}</label>
      <a-table class="full-w" :data="list.data" :columns="columns" :pagination="false" :loading="list.loading"
        size="mini">
      </a-table>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const data = reactive<KeyValue>({
  show: false,
  title: '',
  hostData: {}
})

const list: {
  data: Array<KeyValue>,
  loading: boolean
} = reactive({
  data: [],
  loading: false
})
const columns = computed(() => [
  { title: t('database.HostImportDlg.5oxhmjuzmxw0'), dataIndex: 'clusterName', width: 120 },
  { title: t('database.HostImportDlg.5oxhmjuzn9s0'), dataIndex: 'url', width: 220 },
  { title: t('database.HostImportDlg.5oxhmjuznig0'), dataIndex: 'errMsg', width: 150, ellipsis: true, tooltip: true }
])

const close = () => {
  data.show = false
}

const open = (jdbcData: KeyValue[]) => new Promise(() => {
  data.show = true
  data.title = t('database.HostImportDlg.5oxhmjuzz7w0')
  list.data = jdbcData
})
defineExpose({
  open
})
</script>

<style lang="less" scoped></style>
