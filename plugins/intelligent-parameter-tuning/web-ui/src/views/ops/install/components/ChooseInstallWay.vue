<template>
  <div class="panel-c">
    <div class="panel-header">
      <div class="label-color mb ft-xlg">{{ $t('components.ChooseInstallWay.5mpmwx7b1eo0') }}</div>
      <div class="flex-row-start">
        <div class="label-color mr">{{ $t('components.ChooseInstallWay.5mpmwx7b2580') }}:</div>
        <a-tag size="large" color="green">{{ currWay === BenchMarkEnum.SYSBENCH ?
          $t('components.ChooseInstallWay.5mpmwx7b2gg0') : $t('components.ChooseInstallWay.5mpmwx7b2p00')
        }}</a-tag>
      </div>
    </div>
    <div class="flex-row-center panel-body">
      <div
        :class="'install-way-size card-item-c mr-xlg ' + (currWay === BenchMarkEnum.SYSBENCH ? 'center-item-active' : '')"
        @click="
        chooseWay(BenchMarkEnum.SYSBENCH)">
        <svg-icon icon-class="ops-offline-install" class="icon-size image mb"></svg-icon>
        <div class="label-color ft-main">{{ $t('components.ChooseInstallWay.5mpmwx7b2gg0') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseInstallWay.5mpmwx7b2gg1') }}</div>
      </div>
      <div :class="'install-way-size card-item-c ' + (currWay === BenchMarkEnum.DWG ? 'center-item-active' : '')"
        @click="
        chooseWay(BenchMarkEnum.DWG)">
        <svg-icon icon-class="ops-online-install" class="icon-size image mb"></svg-icon>
        <div class="label-color ft-main">{{ $t('components.ChooseInstallWay.5mpmwx7b2p00') }}</div>
        <div class="label-color remark">{{ $t('components.ChooseInstallWay.5mpmwx7b2p01') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useOpsStore } from '@/store'
import { computed, ComputedRef, onMounted, ref, inject } from 'vue'
import { BenchMarkEnum } from '@/types/ops/install'

const currWay = ref(BenchMarkEnum.SYSBENCH)
const installWayStore = useOpsStore()

const loadingFunc = inject<any>('loading')

onMounted(() => {
  loadingFunc.setBackBtnShow(true)
  if (installModeStoreVal.value) {
    currWay.value = installModeStoreVal.value
  } else {
    installWayStore.setInstallContext({
      benchMark: BenchMarkEnum.SYSBENCH
    })
  }
})

const chooseWay = (way: BenchMarkEnum) => {
  currWay.value = way
  installWayStore.setInstallContext({
    benchMark: way
  })
}

const installModeStoreVal: ComputedRef<BenchMarkEnum> = computed(() => installWayStore.getInstallConfig.benchMark)

</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');

.install-way-size {
  width: 300px;
  height: 300px;
  .remark {
    display: none;
    line-height: 1.5em;
    padding: 10px;
    overflow: auto;
    white-space: pre-line;
  }

  &:hover .remark {
    display: initial;
  }

  &:hover .image {
    display: none;
  }
}
</style>
