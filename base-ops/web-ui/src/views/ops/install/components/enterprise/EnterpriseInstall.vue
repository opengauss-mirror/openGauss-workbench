<template>
  <div class="simple-install-c">
    <a-steps :current="currStep">
      <a-step>{{ $t('enterprise.EnterpriseInstall.5mpm4jewxn00') }}</a-step>
      <a-step>{{ $t('enterprise.EnterpriseInstall.5mpm4jewykk0') }}</a-step>
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
    <!-- <cluster-config v-if="currStep === STEP_ENUM.CLUSTER" ref="clusterConfigRef"></cluster-config> -->
    <!-- two -->
    <!-- <node-config v-if="currStep === STEP_ENUM.NODE" ref="nodeConfigRef"></node-config> -->
    <!-- one -->
    <cluster-config-all
      v-if="currStep === STEP_ENUM.CLUSTER"
      ref="clusterConfigAllRef"
    ></cluster-config-all>
    <!-- three -->
    <env-monitor
      v-if="currStep === STEP_ENUM.ENV"
      ref="envRef"
    />
    <!-- four -->
    <install-prompt
      v-if="currStep === STEP_ENUM.PROMPT"
      ref="promptRef"
    />
    <!-- five -->
    <exe-install
      v-if="installType === 'install' && currStep === STEP_ENUM.EXE"
      ref="exeRef"
    />
    <exe-import v-if="installType === 'import' && currStep === STEP_ENUM.EXE"></exe-import>
  </div>
</template>

<script setup lang="ts">
import ClusterConfigAll from './ClusterConfigAll.vue'
import { computed, ref } from 'vue'
import EnvMonitor from './EnvMonitor.vue'
import InstallPrompt from './InstallPrompt.vue'
import ExeInstall from './ExeInstall.vue'
import ExeImport from '../ExeImport.vue'
import { useOpsStore } from '@/store'

const installStore = useOpsStore()

enum STEP_ENUM {
  CLUSTER = 1,
  ENV,
  PROMPT,
  EXE
}

const installProps = defineProps({
  currStep: Number
})

const clusterConfigAllRef = ref<InstanceType<typeof ClusterConfigAll> | null>(null)

const envRef = ref<InstanceType<typeof EnvMonitor> | null>(null)

const saveStore = () => {
  if (installProps.currStep === STEP_ENUM.CLUSTER) {
    clusterConfigAllRef.value?.saveStore()
  }
}

const beforeConfirm = async (): Promise<boolean> => {
  if (installProps.currStep === STEP_ENUM.CLUSTER) {
    const res = await clusterConfigAllRef.value?.beforeConfirm()
    if (!res) return false
    return res
  }
  if (installProps.currStep === STEP_ENUM.ENV) {
    const res = await envRef.value?.beforeConfirm()
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

<style lang="less" scoped>.simple-install-c {
  padding: 20px;
  height: calc(100% - 36px);
}</style>
