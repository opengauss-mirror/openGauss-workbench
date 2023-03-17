<template>
  <div class="lookeng-config-container flex-justify-center">
    <div class="tab-c">
      <a-tabs
        class="d-a-tabs"
        v-model:activeKey="data.activeKey" centered size="large" :tabBarGutter="40"
        tabBarStyle="{ color: '#fff' }"
      >
        <a-tab-pane key="basicInfo" :title="$t('components.openLooKeng.5mpiji1qpcb35')">
          <basic-info-config-panel ref="basicInfoRef"/>
        </a-tab-pane>
        <a-tab-pane key="sharding" :title="$t('components.openLooKeng.5mpiji1qpcc35')">
          <sharding-config-panel ref="ssPanelRef"/>
        </a-tab-pane>
        <a-tab-pane key="lookeng" :title="$t('components.openLooKeng.5mpiji1qpcc36')">
          <lookeng-config-panel ref="olkPanelRef"/>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import LookengConfigPanel from './LookengConfigPanel.vue'
import ShardingConfigPanel from './ShardingConfigPanel.vue'
import BasicInfoConfigPanel from '@/views/ops/install/components/openLookeng/BasicInfoConfigPanel.vue'

const basicInfoRef = ref<InstanceType<typeof BasicInfoConfigPanel> | null>(null)
const ssPanelRef = ref<InstanceType<typeof ShardingConfigPanel> | null>(null)
const olkPanelRef = ref<InstanceType<typeof LookengConfigPanel> | null>(null)

const beforeConfirm = async (): Promise<boolean> => {
  let res = await basicInfoRef.value?.beforeConfirm()
  if (!res) {
    data.activeKey = 'basicInfo'
    return false
  }
  res = await ssPanelRef.value?.beforeConfirm()
  if (!res) {
    data.activeKey = 'sharding'
    return false
  }
  res = await olkPanelRef.value?.beforeConfirm()
  if (!res) {
    data.activeKey = 'lookeng'
    return false
  }
  return true
}

const data = reactive<KeyValue>({
  activeKey: 'basicInfo'
})

defineExpose({
  beforeConfirm
})
</script>
<style lang="less" scoped>
.lookeng-config-container {
  padding: 0 20px 20px 20px;
  height: 100%;
  overflow-y: auto;
  .tab-c{
    width: 85%;
  }
}
</style>
