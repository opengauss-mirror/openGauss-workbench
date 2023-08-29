<template>
  <div class="lite-install-c">
    <a-steps :current="currStep">
      <a-step>{{ $t('lightweight.LightWeightInstall.5mpmmqzpre40') }}</a-step>
      <a-step>{{ installType === 'install' ? $t('lightweight.LightWeightInstall.5mpmmqzprz80') :
        $t('lightweight.LightWeightInstall.5mpmmqzps6w0')
      }}{{ $t('lightweight.LightWeightInstall.else1') }}</a-step>
      <a-step>{{ installType === 'install' ? $t('lightweight.LightWeightInstall.5mpmmqzprz80') :
        $t('lightweight.LightWeightInstall.5mpmmqzps6w0')
      }}{{ $t('lightweight.LightWeightInstall.else2') }}</a-step>
      <a-step>{{ $t('lightweight.LightWeightInstall.else3') }}{{ installType === 'install' ?
        $t('lightweight.LightWeightInstall.5mpmmqzprz80') :
        $t('lightweight.LightWeightInstall.5mpmmqzps6w0')
      }}</a-step>
    </a-steps>
    <a-divider />
    <deploy-way v-if="currStep === MINI_ENUM.DEPLOY" />
    <install-config
      v-if="currStep === MINI_ENUM.INSTALL"
      ref="installConfigRef"
    />
    <install-prompt v-if="currStep === MINI_ENUM.PROMPT" />
    <exe-import v-if="currStep === MINI_ENUM.EXE"></exe-import>
  </div>
</template>

<script setup lang="ts">
import DeployWay from './DeployWay.vue'
import InstallConfig from './InstallConfig.vue'
import { computed, ref, inject, onMounted } from 'vue'
import InstallPrompt from './InstallPrompt.vue'
import ExeImport from '../ExeImport.vue'
import { useOpsStore } from '@/store'

const installStore = useOpsStore()

enum MINI_ENUM {
  DEPLOY = 1,
  INSTALL,
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

const installConfigRef = ref<InstanceType<typeof InstallConfig> | null>(null)

const saveStore = () => {
  if (installProps.currStep === MINI_ENUM.INSTALL) {
    installConfigRef.value?.saveStore()
  }
}

const beforeConfirm = async (): Promise<boolean> => {
  if (installProps.currStep === MINI_ENUM.INSTALL) {
    const res = await installConfigRef.value?.beforeConfirm()
    if (!res) return false
    return res
  }
  return true
}
const installType = computed(() => installStore.getInstallConfig.installType)

defineExpose({
  saveStore,
  beforeConfirm
})

</script>

<style lang="less" scoped>
.lite-install-c {
  padding: 20px;
  height: calc(100% - 20px - 42px);
}
</style>
