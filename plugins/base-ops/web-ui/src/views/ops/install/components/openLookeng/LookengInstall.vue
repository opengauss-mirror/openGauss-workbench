<template>
  <div class="simple-install-c">
    <a-steps :current="currStep">
      <a-step>{{ $t('components.openLooKeng.5mpiji1qpcc17') }}</a-step>
      <a-step>{{ $t('components.openLooKeng.5mpiji1qpcc18') }}</a-step>
      <a-step>{{ $t('components.openLooKeng.5mpiji1qpcc19') }}</a-step>
    </a-steps>
    <a-divider/>
    <lookeng-config v-if="currStep === STEP_ENUM.CONFIG" ref="configRef"/>
    <config-view v-if="currStep === STEP_ENUM.VIEW" ref="viewRef"/>
    <exe-install v-if="currStep === STEP_ENUM.EXE" ref="exeRef"/>
  </div>
</template>

<script setup lang="ts">
import LookengConfig from './LookengConfig.vue'
import ExeInstall from './ExeInstall.vue'
import { inject, onMounted, ref } from 'vue'
import ConfigView from '@/views/ops/install/components/openLookeng/ConfigView.vue'

enum STEP_ENUM {
  CONFIG = 1,
  VIEW,
  EXE
}

const installProps = defineProps({
  currStep: Number
})

const loadingFunc = inject<any>('loading')
const configRef = ref<InstanceType<typeof LookengConfig> | null>(null)
const viewRef = ref<InstanceType<typeof LookengConfig> | null>(null)

onMounted(() => {
  loadingFunc.setBackBtnShow(true)
})

const beforeConfirm = async (): Promise<boolean> => {
  if (configRef.value) {
    return await configRef.value?.beforeConfirm()
  }
  if (viewRef.value) {
    return await viewRef.value?.beforeConfirm()
  }
  return true
}

defineExpose({
  beforeConfirm
})

</script>

<style lang="less" scoped>
.simple-install-c {
  height: calc(100% - 20px - 42px);
}
</style>
