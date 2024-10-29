<template>
  <div class="install" id="clusterInstall">
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
        $t('install.Install.5mpn60ejriw0')
      }}</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import 'animate.css'
import { firstStep, chooseStep } from './components/installConfig'
import { ref, computed, provide, onMounted, onBeforeUnmount } from 'vue'
import { useOpsStore } from '@/store'
import InstallStep from './components/InstallStep.vue'
import { Message } from '@arco-design/web-vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'

const installStore = useOpsStore()
const currComp = ref(firstStep)
const currCompName = ref<string>('ChooseVersion')
const enterTransition = ref('animate__fadeInRight')
const leaveTransition = ref('animate__fadeOutLeft')

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
  localStorage.removeItem('Static-pluginBase-opsOpsInstall')
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
  installStore.resetInstall()
})

onBeforeUnmount(() => {
  installStore.resetInstall()
})

const handleBack = () => {
  enterTransition.value = 'animate__fadeInLeft'
  leaveTransition.value = 'animate__fadeOutRightBig'
  let nextComp
  if (currCompName.value === 'InstallStep') {
    if (stepComp.value?.currStep === 1) {
      if (installStore.getInstallConfig.installType === 'import' || installStore.getInstallConfig.openGaussVersion === OpenGaussVersionEnum.OPENlOOKENG) {
        // import
        nextComp = chooseStep[currCompName.value].parentImport
      } else {
        if (installWayStore.value.installMode) {
          nextComp = chooseStep[currCompName.value].parent[installWayStore.value.installMode]
        } else {
          nextComp = chooseStep[currCompName.value].parent.offline
        }
      }
      currComp.value = nextComp.comp
      currCompName.value = nextComp.name
    } else {
      stepComp.value?.onPrev()
    }
  } else {
    nextComp = chooseStep[currCompName.value].parent
    currComp.value = nextComp.comp
    currCompName.value = nextComp.name
  }
}
const handleNext = () => {

  changeTransition()
  getNextComp()

}

const changeTransition = () => {
  enterTransition.value = 'animate__fadeInRight'
  leaveTransition.value = 'animate__fadeOutLeft'
}

const getNextComp = () => {
  if (currCompName.value === 'ChooseVersion' && installStore.getInstallConfig.openGaussVersion === OpenGaussVersionEnum.OPENlOOKENG) {
    const nextComp = chooseStep[currCompName.value].childrenImport
    currComp.value = nextComp.comp
    currCompName.value = nextComp.name
    return
  }
  if (currCompName.value === 'InstallStep') {
    stepComp.value?.onNext()
  } else if (currCompName.value === 'ChooseVersion' && installStore.getInstallConfig.installType === 'import') {
    // import
    const nextComp = chooseStep[currCompName.value].childrenImport
    currComp.value = nextComp.comp
    currCompName.value = nextComp.name
  } else {
    let nextComp
    if (currCompName.value === 'ChooseInstallWay') {
      if (installWayStore.value.installMode) {
        nextComp = chooseStep[currCompName.value].children[installWayStore.value.installMode]
      } else {
        nextComp = chooseStep[currCompName.value].parent.offline
      }
    } else if (currCompName.value === 'OfflineInstall') {
      if (!installWayStore.value.installPackagePath) {
        Message.warning('Select the offline installation package to be installed')
        return
      } else {
        nextComp = chooseStep[currCompName.value].children
      }
    } else if (currCompName.value === 'OnlineInstall') {
      if (!installWayStore.value.installPackagePath) {
        Message.warning('Please download the corresponding installation package')
        return
      } else {
        nextComp = chooseStep[currCompName.value].children
      }
    } else {
      nextComp = chooseStep[currCompName.value].children
    }
    currComp.value = nextComp.comp
    currCompName.value = nextComp.name
  }
}

const installWayStore = computed(() => installStore.getInstallConfig)
</script>

<style lang="less" scoped>
.install {
  padding: 16px;
  width: 100%;
  position: relative;
  height: calc(100vh - 130px);

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
