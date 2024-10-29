<template>
  <div class="simple-install-c full-h" id="simpleInstall">
    <a-steps :current="currStep">
      <a-step>{{ $t('simple.SimpleInstall.else0') }}
      </a-step>
      <a-step>
        {{ installType === 'install' ? $t('simple.SimpleInstall.5mpmv5d5bmo0') : $t('simple.SimpleInstall.5mpmv5d5cyk0')
        }}{{ $t('simple.SimpleInstall.else1') }}
      </a-step>
      <a-step>
        {{ $t('simple.SimpleInstall.5mpmv5d5dak0') }}
      </a-step>
      <a-step>
        {{ installType === 'install' ? $t('simple.SimpleInstall.5mpmv5d5bmo0') : $t('simple.SimpleInstall.5mpmv5d5cyk0')
        }}{{ $t('simple.SimpleInstall.else2') }}
      </a-step>
      <a-step>
        {{ $t('simple.SimpleInstall.else2') }}{{ installType === 'install' ? $t('simple.SimpleInstall.5mpmv5d5bmo0') :
  $t('simple.SimpleInstall.5mpmv5d5cyk0')
        }}
      </a-step>
    </a-steps>
    <a-divider />
    <deploy-way v-if="currStep === MINI_ENUM.DEPLOY" />
    <install-config :before-confirm="customeFunction" ref="installConfigRef" v-if="currStep === MINI_ENUM.INSTALL" />
    <env-monitor v-if="currStep === MINI_ENUM.ENV" ref="envRef" />
    <install-prompt v-if="currStep === MINI_ENUM.PROMPT" />
    <exe-install v-if="currStep === MINI_ENUM.EXE" />
  </div>
</template>

<script setup lang="ts">
import DeployWay from './DeployWay.vue'
import InstallConfig from './InstallConfig.vue'
import EnvMonitor from './EnvMonitor.vue'
import ExeInstall from './ExeInstall.vue'
import InstallPrompt from './InstallPrompt.vue'
import { useOpsStore } from '@/store'

import { computed, ref } from 'vue'
enum MINI_ENUM {
  DEPLOY = 1,
  INSTALL,
  ENV,
  PROMPT,
  EXE
}

const installStore = useOpsStore()

const instalProps = defineProps({
  currStep: Number,
  customeFunction: Function
})

const installConfigRef = ref<InstanceType<typeof InstallConfig> | null>(null)
const envRef = ref<InstanceType<typeof EnvMonitor> | null>(null)

const saveStore = () => {
  if (instalProps.currStep === MINI_ENUM.INSTALL) {
    installConfigRef.value?.saveStore()
  }
}

const beforeConfirm = async (): Promise<boolean> => {
  if (instalProps.currStep === MINI_ENUM.INSTALL) {
    const res = await installConfigRef.value?.beforeConfirm()
    if (!res) return false
    return res
  }
  if (instalProps.currStep === MINI_ENUM.ENV) {
    const res = await envRef.value?.beforeConfirm()
    if (!res) return false
    return res
  }
  return true
}

defineExpose({
  saveStore,
  beforeConfirm
})

const installType = computed(() => installStore.getInstallConfig.installType)

</script>

<style lang="less" scoped>
.simple-install-c {}
</style>
