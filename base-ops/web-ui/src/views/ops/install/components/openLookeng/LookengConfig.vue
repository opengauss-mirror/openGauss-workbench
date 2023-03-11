<template>
  <div class="lookeng-config-container flex-justify-center">
    <div class="tab-c">
      <a-tabs
        class="d-a-tabs"
        v-model:activeKey="data.activeKey" centered size="large" :tabBarGutter="40"
        tabBarStyle="{ color: '#fff' }"
      >
        <a-tab-pane key="sharding" title="ShardingProxy配置">
          <sharding-config-panel :tar-map="data.tarMap" ref="ssPanelRef"/>
        </a-tab-pane>
        <a-tab-pane key="lookeng" title="OpenLookEng配置">
          <lookeng-config-panel :tar-map="data.tarMap" ref="olkPanelRef"/>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { KeyValue } from '@/types/global'
import LookengConfigPanel from './LookengConfigPanel.vue'
import ShardingConfigPanel from './ShardingConfigPanel.vue'
import { mockTarListAll } from '@/api/ops/mock'
import { Message } from '@arco-design/web-vue'

const ssPanelRef = ref<InstanceType<typeof ShardingConfigPanel> | null>(null)
const olkPanelRef = ref<InstanceType<typeof LookengConfigPanel> | null>(null)

const beforeConfirm = async (): Promise<boolean> => {
  let res = await ssPanelRef.value?.beforeConfirm()
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
  activeKey: 'sharding',
  tarMap: {}
})

defineExpose({
  beforeConfirm
})

let tarMap = {}

const getTarListAll = () => {
  mockTarListAll().then(res => {
    if (Number(res.code) === 200) {
      res.data.forEach((item:KeyValue) => {
        if (!data.tarMap[item.type]) {
          data.tarMap[item.type] = [item]
        } else {
          data.tarMap[item.type].push(item)
        }
      })
    } else {
      Message.error('Failed to obtain the tar list data')
    }
  }).finally(() => {
  })
}

onMounted(() => {
  getTarListAll()
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
