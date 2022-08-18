<template>
  <div class="panel-c">
    <div class="panel-header">
      <div class="mb ft-xlg">{{ $t('components.ChooseInstallWay.5mpmwx7b1eo0') }}</div>
      <div class="flex-row-start">
        <div class="mr">{{ $t('components.ChooseInstallWay.5mpmwx7b2580') }}:</div>
        <a-tag size="large" color="green">{{ currWay === InstallModeEnum.OFF_LINE ?
            $t('components.ChooseInstallWay.5mpmwx7b2gg0') : $t('components.ChooseInstallWay.5mpmwx7b2p00')
        }}</a-tag>
      </div>
    </div>
    <div class="flex-row-center panel-body">
      <div
        :class="'install-way-size card-item-c mr-lg ' + (currWay === InstallModeEnum.OFF_LINE ? 'center-item-active' : '')"
        @click="
        chooseWay(InstallModeEnum.OFF_LINE)">
        <svg-icon icon-class="ops-offline-install" class="icon-size image mb"></svg-icon>
        <div class="ft-main">{{ $t('components.ChooseInstallWay.5mpmwx7b2gg0') }}</div>
      </div>
      <div :class="'install-way-size card-item-c ' + (currWay === InstallModeEnum.ON_LINE ? 'center-item-active' : '')"
        @click="
        chooseWay(InstallModeEnum.ON_LINE)">
        <svg-icon icon-class="ops-online-install" class="icon-size image mb"></svg-icon>
        <div class="ft-main">{{ $t('components.ChooseInstallWay.5mpmwx7b2p00') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useOpsStore } from '@/store'
import { computed, ComputedRef, onMounted, ref } from 'vue'
import { InstallModeEnum } from '@/types/ops/install'

const currWay = ref(InstallModeEnum.OFF_LINE)
const installWayStore = useOpsStore()

onMounted(() => {
  if (installModeStoreVal.value) {
    currWay.value = installModeStoreVal.value
  } else {
    installWayStore.setInstallContext({
      installMode: InstallModeEnum.OFF_LINE
    })
  }
})

const chooseWay = (way: InstallModeEnum) => {
  currWay.value = way
  installWayStore.setInstallContext({
    installMode: way
  })
}

const installModeStoreVal: ComputedRef<InstallModeEnum> = computed(() => installWayStore.getInstallConfig.installMode)

</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');

.install-way-size {
  width: 250px;
  height: 250px;
}
</style>
