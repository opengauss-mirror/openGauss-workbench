<template>
  <div class="flow-summary-c flex-col">
    <div class="flex-row full-w mb">
      <div
        class="flex-col"
        style="width: 50%"
      >
        <div class="flow-summary-title">{{ $t('components.BusiFlowSummary.5mpivn7fwjc0') }}</div>
        <div class="echarts-c">
          <busi-flow-summary></busi-flow-summary>
        </div>
      </div>
      <div
        class="flex-col"
        style="width: 50%"
      >
        <div class="flow-summary-title">{{ $t('components.BusiFlowSummary.5mpivn7fx3k0') }}</div>
        <div class="echarts-c">
          <data-source-summary />
        </div>
      </div>
    </div>
    <div v-if="isBaseopsInstalled === false">
      <el-tooltip :content="$t('components.ModuleSummary.else2')" placement="bottom" effect="light">
        <a-table
          class="full-w"
          :data="list.data"
          :columns="columns"
          :pagination="false"
        />
      </el-tooltip>
    </div>
    <div v-else>
      <a-table
        class="full-w"
        :data="list.data"
        :columns="columns"
        :pagination="false"
        :loading="list.loading"
      >
        <template #name="{ record }">
          {{ record.name ? record.name : '--' }}
        </template>
        <template #cluster="{ record }">
          {{ record.clusterId ? record.clusterId : '--' }}
        </template>
        <template #type="{ record }">
          {{ record.type ? record.type : '--' }}
        </template>
        <template #queryCount="{ record }">
          {{ record.queryCount ? record.queryCount : '0' }}
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { onMounted, reactive, computed, ref } from 'vue'
import BusiFlowSummary from './echarts/BusiFlowSummary.vue'
import DataSourceSummary from './echarts/DataSourceSummary.vue'
import { getBusiFlowList, isBaseOpsStart } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const list: {
  data: Array<KeyValue>,
  loading: boolean
} = reactive({
  data: [],
  loading: false
})

onMounted(() => {
  getPluginInstall()
})

const columns = computed(() => [
  { title: t('components.BusiFlowSummary.5mpivn7fxfc0'), dataIndex: 'name', slotName: 'name' },
  { title: t('components.BusiFlowSummary.5mpivn7fxjk0'), dataIndex: 'clusterId', slotName: 'cluster' },
  { title: t('components.BusiFlowSummary.5mpivn7fxnc0'), dataIndex: 'type', slotName: 'type' },
  { title: t('components.BusiFlowSummary.5mpivn7fxrk0'), dataIndex: 'queryCount', slotName: 'queryCount' }
])

const emits = defineEmits(['is-install-plugin'])

const isBaseopsInstalled = ref<boolean>(true)
const getPluginInstall = () => {
  isBaseOpsStart().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      isBaseopsInstalled.value = res.data
    }
  }) .catch((error: any) => {
    console.log(error)
  }) .finally(() => {
    getListData()
  })
}

const getListData = () => {
  if (isBaseopsInstalled.value) {
    list.loading = true
    getBusiFlowList().then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.distribution
      } else {
        Message.error('Failed to get the list of business flow running instances')
      }
    }).catch((error) => {
      console.log('show getBusiFlowList error', error)
      const { response } = error
      if (response.status === 404) {
        emits('is-install-plugin', false)
      }
    }).finally(() => {
      list.loading = false
    })
  } else {
    list.loading = true
  }
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
