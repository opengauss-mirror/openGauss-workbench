<template>
  <div class="system-alarm-c">
    <div class="ft-lg ft-b mb">{{ $t('components.SystemAlarm.5mpihybegg40') }}</div>
    <a-table class="d-a-table-row" :data="list.data" :columns="columns" :pagination="false" :loading="list.loading">
    </a-table>
  </div>
</template>

<script setup lang="ts">

import { TableColumnData } from '@arco-design/web-vue'
import { onMounted, reactive } from 'vue'
import { KeyValue } from '@/types/global'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const columns: TableColumnData[] = [
  { title: t('components.SystemAlarm.5mpihybehhk0'), dataIndex: 'level' },
  { title: t('components.SystemAlarm.5mpihybeho00'), dataIndex: 'content' },
  { title: t('components.SystemAlarm.5mpihybehvw0'), dataIndex: 'time' }
]
const list: {
  data: Array<KeyValue>,
  loading: boolean
} = reactive({
  data: [],
  loading: false
})
onMounted(() => {
  list.data = getListData()
})

const getListData = () => {
  return [{
    level: 0,
    content: t('components.SystemAlarm.cpuWarning'),
    time: '2022-01-02'
  }, {
    level: 1,
    content: t('components.SystemAlarm.5mpihybei2o0'),
    time: '2022-01-02'
  }, {
    level: 2,
    content: t('components.SystemAlarm.5mpihybei740'),
    time: '2022-01-02'
  }, {
    level: 3,
    content: t('components.SystemAlarm.5mpihybeiaw0'),
    time: '2022-01-02'
  }]
}

</script>

<style lang="less" scoped>
.system-alarm-c {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
}
</style>
