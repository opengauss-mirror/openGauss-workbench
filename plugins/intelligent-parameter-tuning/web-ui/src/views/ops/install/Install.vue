<template>
  <div class="install" id="tuningInstall">
    <div class="install-body">
      <transition appear :enter-active-class="'install-enter-active animate__animated ' + enterTransition"
        :leave-active-class="'install-leave-active animate__animated ' + leaveTransition"
        leave-to-class="transition-display">
        <component :key="currCompName" :is="currComp" ref="stepComp"></component>
      </transition>
    </div>
    <div class="install-footer">
      <a-button size="large" v-if="backBtnShow" :loading="loading" class="mr-lg" @click="handleBack">{{
        $t('install.Install.5mpn60ejqkg0')
      }}</a-button>
      <a-button size="large" v-if="nextBtnShow" :loading="loading" type="primary" @click="handleNext">{{
        currCompName === 'InstallStep' ? $t('install.Install.5mpn60ejriw1') : $t('install.Install.5mpn60ejriw0')
      }}</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import 'animate.css'
import { firstStep, chooseStep, copyStep } from './components/installConfig'
import { ref, computed, provide, onMounted, onBeforeUnmount } from 'vue'
import { useOpsStore } from '@/store'
import InstallStep from './components/InstallStep.vue'
import { Message } from '@arco-design/web-vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'

const installStore = useOpsStore()
const currComp = ref(firstStep)
const currCompName = ref<string>('ChooseVersion')

const loading = ref(false)

const backBtnShow = ref(true)

const nextBtnShow = ref(true)

const stepComp = ref<InstanceType<typeof InstallStep> | null>(null)

const toLoading = () => {
  loading.value = true
}

const cancelLoading = () => {
  loading.value = false
}

const initData = () => {
  installStore.resetInstall()
  currComp.value = firstStep
  currCompName.value = 'ChooseVersion'
  stepComp.value = null
  backBtnShow.value = true
  nextBtnShow.value = true
  loading.value = false
}
const copyData = () => {
  currComp.value = copyStep
  currCompName.value = 'InstallStep'
  backBtnShow.value = true
  nextBtnShow.value = true
  loading.value = false
}

const setBackBtnShow = (val: boolean) => {
  backBtnShow.value = val
}

const setNextBtnShow = (val: boolean) => {
  nextBtnShow.value = val
}

provide('loading', {
  toLoading,
  cancelLoading,
  setBackBtnShow,
  setNextBtnShow,
  initData
})

onMounted(() => {
  if(installWayStore.value.isCopy){
    copyData()
  } else {
    installStore.resetInstall()
  }
})

onBeforeUnmount(() => {
  installStore.resetInstall()
})

const handleBack = () => {
  let nextComp
  nextComp = chooseStep[currCompName.value].parent
  currComp.value = nextComp.comp
  currCompName.value = nextComp.name
}
const handleNext = () => {
  getNextComp()
}

const getNextComp = () => {
  debugger
    let nextComp
    if(currCompName.value === 'ChooseVersion'){
      nextComp = chooseStep[currCompName.value].children
    }
      else if (currCompName.value === 'ChooseInstallWay') {
        nextComp = chooseStep[currCompName.value].children
    } else if (currCompName.value === 'InstallStep') {
      stepComp.value?.onPrev()
      return
    }
    currComp.value = nextComp.comp
    currCompName.value = nextComp.name
}

const installWayStore = computed(() => installStore.getInstallConfig)
</script>

<style lang="less" scoped>
.install {
  padding: 16px;
  width: 100%;
  position: relative;
  height: calc(100vh - 180px);

  &-body {
    // height: calc(100vh - 180px);
    height: calc(100% - 36px);
  }

  &-footer {
    // display: flex;
    // justify-content: center;
    position: absolute;
    left: 50%;
    transform: translate(-50%, 0);
    bottom: 0;
  }

  .transition-display {
    display: none;
  }

}
</style>
