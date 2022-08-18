<template>
  <div class="panel-c">
    <simple-install v-if="installStore.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST" :curr-step="currStep"
      :custom-func="customFunction" ref="simpleInstallRef" />
    <light-weight-install v-if="installStore.openGaussVersion === OpenGaussVersionEnum.LITE" :curr-step="currStep"
      ref="liteInstallRef"></light-weight-install>
    <enterprise-install v-if="installStore.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE" :curr-step="currStep"
      ref="enterpriseInstallRef"></enterprise-install>
    <lookeng-install v-if="installStore.openGaussVersion === OpenGaussVersionEnum.OPENlOOKENG" :curr-step="currStep"
      ref="lookengInstallRef"></lookeng-install>
  </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue'
import SimpleInstall from './simple/SimpleInstall.vue'
import { useOpsStore } from '@/store'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import LightWeightInstall from './lightweight/LightWeightInstall.vue'
import EnterpriseInstall from './enterprise/EnterpriseInstall.vue'
import LookengInstall from './openLookeng/LookengInstall.vue'
const currStep = ref<number>(1)

const opsStore = useOpsStore()

const simpleInstallRef = ref<InstanceType<typeof SimpleInstall> | null>(null)
const liteInstallRef = ref<InstanceType<typeof LightWeightInstall> | null>(null)
const enterpriseInstallRef = ref<InstanceType<typeof EnterpriseInstall> | null>(null)
const lookengInstallRef = ref<InstanceType<typeof LookengInstall> | null>(null)

const customFunction = () => {
  console.log('')
}

const onPrev = () => {
  currStep.value = Math.max(1, currStep.value - 1)
}
const onNext = async () => {
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
    console.log('ql beforeConfirm', liteInstallRef.value)
    const res = await simpleInstallRef.value?.beforeConfirm()
    if (res) {
      currStep.value = Math.min(5, currStep.value + 1)
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.LITE) {
    console.log('ql beforeConfirm', liteInstallRef.value)
    const res = await liteInstallRef.value?.beforeConfirm()
    if (res) {
      currStep.value = Math.min(5, currStep.value + 1)
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.ENTERPRISE) {
    const res = await enterpriseInstallRef.value?.beforeConfirm()
    if (res) {
      currStep.value = Math.min(5, currStep.value + 1)
    }
  }
  if (installStore.value.openGaussVersion === OpenGaussVersionEnum.OPENlOOKENG) {
    const res = await lookengInstallRef.value?.beforeConfirm()
    if (res) {
      currStep.value = Math.min(2, currStep.value + 1)
    }
  }
}

const installStore = computed(() => opsStore.getInstallConfig)

defineExpose({
  currStep,
  onPrev,
  onNext
})

</script>
<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');
</style>
