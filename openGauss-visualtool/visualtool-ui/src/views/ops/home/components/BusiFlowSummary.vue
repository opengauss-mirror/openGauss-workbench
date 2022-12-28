<template>
  <div class="flow-summary-c flex-col mb">
    <div class="flex-row full-w mb">
      <div class="flex-col" style="width: 50%">
        <div class="flow-summary-title">{{ $t('components.BusiFlowSummary.5mpivn7fwjc0') }}</div>
        <div class="echarts-c">
          <busi-flow-summary></busi-flow-summary>
        </div>
      </div>
      <div class="flex-col" style="width: 50%">
        <div class="flow-summary-title">{{ $t('components.BusiFlowSummary.5mpivn7fx3k0') }}</div>
        <div class="echarts-c">
          <data-source-summary />
        </div>
      </div>
    </div>
    <a-table class="full-w" :data="list.data" :columns="columns" :pagination="false" :loading="list.loading">
      <template #name="{ record }">
        {{ record.name ? record.name : '--' }}
      </template>
      <template #cluster="{ record }">
        {{ record.cluster ? record.cluster : '--' }}
      </template>
      <template #type="{ record }">
        {{ record.type ? record.type : '--' }}
      </template>
      <template #queryCount="{ record }">
        {{ record.queryCount ? record.queryCount : '--' }}
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { Message, TableColumnData } from '@arco-design/web-vue'
import { KeyValue } from '@/types/global'
import { onMounted, reactive } from 'vue'
import BusiFlowSummary from './echarts/BusiFlowSummary.vue'
import DataSourceSummary from './echarts/DataSourceSummary.vue'
import { getBusiFlowList } from '@/api/ops'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const columns: TableColumnData[] = [
  { title: t('components.BusiFlowSummary.5mpivn7fxfc0'), dataIndex: 'name', slotName: 'name' },
  { title: t('components.BusiFlowSummary.5mpivn7fxjk0'), dataIndex: 'cluster', slotName: 'cluster' },
  { title: t('components.BusiFlowSummary.5mpivn7fxnc0'), dataIndex: 'type', slotName: 'type' },
  { title: t('components.BusiFlowSummary.5mpivn7fxrk0'), dataIndex: 'queryCount', slotName: 'queryCount' }
]
const list: {
  data: Array<KeyValue>,
  loading: boolean
} = reactive({
  data: [],
  loading: false
})

onMounted(() => {
  getListData()
})

const getListData = () => {
  list.data = []
}

</script>

<style lang="less" scoped>
.flow-summary-c {
  background-color: var(--color-bg-2);
  border-radius: 2px;
  padding: 15px;
  .flow-summary-title {
    font-size: 16px;
    color: var(--color-text-1);
    margin-bottom: 10px;
  }
  .echarts-c {
    width: 150px;
    height: 240px;
  }
}
</style>
