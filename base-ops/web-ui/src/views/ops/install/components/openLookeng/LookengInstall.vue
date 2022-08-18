<template>
  <div class="simple-install-c">
    <a-steps :current="currStep">
      <a-step>添加配置</a-step>
      <a-step>执行安装</a-step>
    </a-steps>
    <a-divider />
    <!-- one -->
    <lookeng-config v-if="currStep === STEP_ENUM.CONFIG" ref="configRef"></lookeng-config>
    <!-- two -->
    <exe-install v-if="currStep === STEP_ENUM.EXE" ref="exeRef" />
  </div>
</template>

<script setup lang="ts">
import LookengConfig from './LookengConfig.vue'
import ExeInstall from './ExeInstall.vue'
import { ref } from 'vue'


enum STEP_ENUM {
  CONFIG = 1,
  EXE
}

const installProps = defineProps({
  currStep: Number
})

const configRef = ref<InstanceType<typeof LookengConfig> | null>(null)

const beforeConfirm = async (): Promise<boolean> => {
  if (installProps.currStep === STEP_ENUM.CONFIG) {
    const res = await configRef.value?.beforeConfirm()
    if (!res) return false
    return res
  }
  return true
}

defineExpose({
  beforeConfirm
})

</script>

<style lang="less" scoped>
.simple-install-c {
  padding: 20px;
  height: calc(100% - 20px - 42px);
}
</style>
