<template>
  <div class="panel-c" id="installStep">
    <simple-install
      v-if="installStore.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST && installType === 'install'"
      :curr-step="currStep"
      :custom-func="customFunction"
      ref="simpleInstallRef"
    />
    <simple-import
      v-if="installStore.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST && installType === 'import'"
      :curr-step="currStep"
      :custom-func="customFunction"
      ref="simpleImportRef"
    />
    <light-weight-install
      v-if="installStore.openGaussVersion === OpenGaussVersionEnum.LITE && installType === 'install'"
      :curr-step="currStep"
      ref="liteInstallRef"
    ></light-weight-install>
    <light-weight-import
      v-if="installStore.openGaussVersion === OpenGaussVersionEnum.LITE && installType === 'import'"
      :curr-step="currStep"
      ref="liteImportRef"
    ></light-weight-import>
    <enterprise-install
      v-if="installStore.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE && installType === 'install'"
      :curr-step="currStep"
      ref="enterpriseInstallRef"
    ></enterprise-install>
    <enterprise-import
      v-if="installStore.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE && installType === 'import'"
      :curr-step="currStep"
      ref="enterpriseImportRef"
    ></enterprise-import>
    <lookeng-install
      v-if="installStore.openGaussVersion === OpenGaussVersionEnum.OPENlOOKENG"
      :curr-step="currStep"
      ref="lookengInstallRef"
    ></lookeng-install>
  </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue'
import SimpleInstall from './simple/SimpleInstall.vue'
import SimpleImport from './simple/SimpleImport.vue'
import { useOpsStore } from '@/store'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import LightWeightInstall from './lightweight/LightWeightInstall.vue'
import LightWeightImport from './lightweight/LightWeightImport.vue'
import EnterpriseInstall from './enterprise/EnterpriseInstall.vue'
import EnterpriseImport from './enterprise/EnterpriseImport.vue'
import LookengInstall from './openLookeng/LookengInstall.vue'
const currStep = ref<number>(1)

const opsStore = useOpsStore()

const simpleInstallRef = ref<InstanceType<typeof SimpleInstall> | null>(null)
const simpleImportRef = ref<InstanceType<typeof SimpleImport> | null>(null)

const liteInstallRef = ref<InstanceType<typeof LightWeightInstall> | null>(null)
const liteImportRef = ref<InstanceType<typeof LightWeightImport> | null>(null)

const enterpriseInstallRef = ref<InstanceType<typeof EnterpriseInstall> | null>(null)

const enterpriseImportRef = ref<InstanceType<typeof EnterpriseImport> | null>(null)
const lookengInstallRef = ref<InstanceType<typeof LookengInstall> | null>(null)

const customFunction = () => {
  console.log('')
}

const onPrev = () => {
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
    if (installType.value === 'install') {
      simpleInstallRef.value?.saveStore()
    } else {
      simpleImportRef.value?.saveStore()
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.LITE) {
    if (installType.value === 'install') {
      liteInstallRef.value?.saveStore()
    } else {
      liteImportRef.value?.saveStore()
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE) {
    if (installType.value === 'install') {
      enterpriseInstallRef.value?.saveStore()
    } else {
      enterpriseImportRef.value?.saveStore()
    }
  }
  currStep.value = Math.max(1, currStep.value - 1)
}
const onNext = async () => {
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
    let res: any = false
    if (installType.value === 'install') {
      res = await simpleInstallRef.value?.beforeConfirm()
    } else {
      res = await simpleImportRef.value?.beforeConfirm()
    }
    if (res) {
      currStep.value = Math.min(5, currStep.value + 1)
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.LITE) {
    let res: any = false
    if (installType.value === 'install') {
      res = await liteInstallRef.value?.beforeConfirm()
    } else {
      res = await liteImportRef.value?.beforeConfirm()
    }
    if (res) {
      currStep.value = Math.min(5, currStep.value + 1)
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE) {
    let res: any = false
    if (installType.value === 'install') {
      res = await enterpriseInstallRef.value?.beforeConfirm()
    } else {
      res = await enterpriseImportRef.value?.beforeConfirm()
    }
    if (res) {
      currStep.value = Math.min(5, currStep.value + 1)
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.OPENlOOKENG) {
    const res = await lookengInstallRef.value?.beforeConfirm()
    if (res) {
      currStep.value = Math.min(3, currStep.value + 1)
    }
  }
}

const installStore = computed(() => opsStore.getInstallConfig)
const installType = computed(() => opsStore.getInstallConfig.installType)

defineExpose({
  currStep,
  onPrev,
  onNext
})

</script>
<style lang="less" scoped>@import url('~@/assets/style/ops/ops.less');</style>
