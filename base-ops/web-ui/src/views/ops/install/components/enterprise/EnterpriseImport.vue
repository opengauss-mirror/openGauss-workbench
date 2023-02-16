<template>
  <div class="simple-install-c">
    <a-steps :current="currStep">
      <a-step>{{ $t('enterprise.EnterpriseInstall.5mpm4jewxn00') }}</a-step>
      <a-step>{{ $t('enterprise.EnterpriseInstall.5mpm4jewye00') }}</a-step>
      <a-step>{{ installType === 'install' ? $t('enterprise.EnterpriseInstall.5mpm4jewyp40') :
        $t('enterprise.EnterpriseInstall.5mpm4jewysw0')
      }}{{ $t('enterprise.EnterpriseInstall.else2') }}</a-step>
      <a-step>{{ $t('enterprise.EnterpriseInstall.else3') }}{{ installType === 'install' ?
        $t('enterprise.EnterpriseInstall.5mpm4jewyp40') :
        $t('enterprise.EnterpriseInstall.5mpm4jewysw0')
      }}</a-step>
    </a-steps>
    <a-divider />
    <!-- one -->
    <cluster-config v-if="currStep === STEP_ENUM.CLUSTER" ref="clusterConfigRef"></cluster-config>
    <!-- two -->
    <node-config v-if="currStep === STEP_ENUM.NODE" ref="nodeConfigRef"></node-config>
    <!-- four -->
    <install-prompt v-if="currStep === STEP_ENUM.PROMPT" ref="promptRef" />
    <!-- five -->
    <exe-import v-if="currStep === STEP_ENUM.EXE"></exe-import>
  </div>
</template>

<script setup lang="ts">
import ClusterConfig from '@/views/ops/install/components/enterprise/ClusterConfig.vue'
import NodeConfig from '@/views/ops/install/components/enterprise/NodeConfig.vue'
import { computed, ref, inject, onMounted } from 'vue'
import EnvMonitor from './EnvMonitor.vue'
import InstallPrompt from './InstallPrompt.vue'
import ExeImport from '../ExeImport.vue'
import { useOpsStore } from '@/store'

const installStore = useOpsStore()

enum STEP_ENUM {
  CLUSTER = 1,
  NODE,
  PROMPT,
  EXE
}

const installProps = defineProps({
  currStep: Number
})

const loadingFunc = inject<any>('loading')

onMounted(() => {
  loadingFunc.setBackBtnShow(true)
})

const clusterConfigRef = ref<InstanceType<typeof ClusterConfig> | null>(null)
const nodeConfigRef = ref<InstanceType<typeof NodeConfig> | null>(null)
const envRef = ref<InstanceType<typeof EnvMonitor> | null>(null)

const saveStore = () => {
  if (installProps.currStep === STEP_ENUM.CLUSTER) {
    clusterConfigRef.value?.saveStore()
  }
  if (installProps.currStep === STEP_ENUM.NODE) {
    nodeConfigRef.value?.saveStore()
  }
}

const beforeConfirm = async (): Promise<boolean> => {
  if (installProps.currStep === STEP_ENUM.CLUSTER) {
    const res = await clusterConfigRef.value?.beforeConfirm()
    if (!res) return false
    return res
  }
  if (installProps.currStep === STEP_ENUM.NODE) {
    const res = await nodeConfigRef.value?.beforeConfirm()
    if (!res) return false
    return res
  }
  return true
}
const installType = computed(() => installStore.getInstallConfig.installType)

defineExpose({
  beforeConfirm,
  saveStore
})

</script>

<style lang="less" scoped>
.simple-install-c {
  padding: 20px;
  height: calc(100% - 20px - 42px);
}
</style>
