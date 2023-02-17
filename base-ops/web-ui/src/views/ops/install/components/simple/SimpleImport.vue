<template>
  <div class="simple-install-c full-h">
    <a-steps :current="currStep">
      <a-step>{{ $t('simple.SimpleInstall.else0') }}
      </a-step>
      <a-step>
        {{ installType === 'install' ? $t('simple.SimpleInstall.5mpmv5d5bmo0') : $t('simple.SimpleInstall.5mpmv5d5cyk0')
        }}{{ $t('simple.SimpleInstall.else1') }}
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
    <install-prompt v-if="currStep === MINI_ENUM.PROMPT" />
    <exe-import v-if="currStep === MINI_ENUM.EXE"></exe-import>
  </div>
</template>

<script setup lang="ts">
import DeployWay from './DeployWay.vue'
import InstallConfig from './InstallConfig.vue'
import InstallPrompt from './InstallPrompt.vue'
import ExeImport from '../ExeImport.vue'
import { useOpsStore } from '@/store'

import { onMounted, computed, ref, inject } from 'vue'
enum MINI_ENUM {
  DEPLOY = 1,
  INSTALL,
  PROMPT,
  EXE
}

const installStore = useOpsStore()

const instalProps = defineProps({
  currStep: Number,
  customeFunction: Function
})
const loadingFunc = inject<any>('loading')

onMounted(() => {
  loadingFunc.setBackBtnShow(true)
})

const installConfigRef = ref<InstanceType<typeof InstallConfig> | null>(null)

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
