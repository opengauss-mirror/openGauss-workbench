<template>
  <div class="cluster-top-c flex-between mb">
    <div class="flex-row">
      <div class="mr-lg cluster-title">{{ $t('components.ClusterTop.5mpinksdbe40') }}</div>
    </div>
    <div class="flex-row">
      <div class="oper-c cursor-c flex-col ml" @click="showOneCheck">
        <svg-icon class="icon-size mb-s" icon-class="ops-one-check"></svg-icon>
        <div class="txt">{{ $t('components.ClusterTop.5mpinksdby80') }}</div>
      </div>
      <div class="oper-c cursor-c flex-col ml" @click="$router.push({ name: 'Static-pluginBase-opsMonitorDailyOps' })">
        <svg-icon class="icon-size mb-s" icon-class="ops-cluster-ops"></svg-icon>
        <div class="txt">{{ $t('components.ClusterTop.5mpinksdc4g0') }}</div>
      </div>
      <div class="flex-col mlg">
        <div class="mb-s"><span class="mr-xs ft-xlg">{{ data.clusterNum }}</span>{{ $t('components.ClusterTop.5mpinksdc8g0') }}</div>
        <div class="txt">{{ $t('components.ClusterTop.5mpinksdcc40') }}</div>
      </div>
      <div class="flex-col mlg">
        <div class="mb-s"><span class="mr-xs ft-xlg">{{ data.hostNum }}</span>{{ $t('components.ClusterTop.5mpinksdc8g0') }}</div>
        <div class="txt">{{ $t('components.ClusterTop.5mpinksdcgk0') }}</div>
      </div>
      <div class="flex-col mlg">
        <div class="mb-s"><span class="mr-xs ft-xlg">{{ data.nodeNum }}</span>{{ $t('components.ClusterTop.5mpinksdc8g0') }}</div>
        <div class="txt">{{ $t('components.ClusterTop.5mpinksdckw0') }}</div>
      </div>
    </div>
    <one-check ref="oneCheckRef"></one-check>
  </div>
</template>

<script lang="ts" setup>

import { onMounted, reactive, ref } from 'vue'
import { getSummary } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { KeyValue } from '@/types/global'
import OneCheck from './OneCheck.vue'
const data = reactive<KeyValue>({
  loading: false,
  clusterNum: 0,
  hostNum: 0,
  nodeNum: 0
})

onMounted(() => {
  // get list data
  getSummary().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.loading = true
      data.clusterNum = res.data.clusterNum
      data.hostNum = res.data.hostNum
      data.nodeNum = res.data.nodeNum
    } else {
      Message.error('Failed to obtain cluster summary information')
    }
  }).finally(() => {
    data.loading = false
  })
})

const oneCheckRef = ref<null | InstanceType<typeof OneCheck>>(null)
const showOneCheck = () => {
  oneCheckRef.value?.open()
}

</script>

<style lang="less" scoped>
.cluster-top-c {
  padding: 10px 15px;
  background-color: var(--color-bg-2);
  border-radius: 2px;
  .cluster-title {
    font-size: 16px;
    color: var(--color-text-1);
  }
  .ml {
    margin-left: 13px;
    .txt {
      font-size: 12px;
      color: var(--color-text-2);
    }
  }
  .mlg {
    margin-left: 24px;
    .mb-s {
      color: var(--color-text-2);
    }
    .txt {
      font-size: 14px;
      color: var(--color-text-2);
    }
  }
  .cursor-c {
    cursor: pointer;
  }

  .oper-c {
    background-color: var(--color-fill-2);
    border-radius: 2px;
    padding: 8px;
  }

  .icon-size {
    width: 18px;
    height: 18px;
  }
}
</style>
