<template>
  <div class="upgrade-container" id="upgrade">
    <a-steps :current="data.currStep">
      <a-step>{{ $t('upgrade.index.5uunxx6pdzg0') }}</a-step>
      <a-step>{{ $t('upgrade.index.5uunxx6pf5w0') }}</a-step>
      <a-step>{{ $t('upgrade.index.5uunxx6pfic0') }}</a-step>
    </a-steps>
    <a-divider />
    <div class="center-c">
      <upgrade-conifg
        ref="upgradeConifgRef"
        v-if="data.currStep === 1"
      ></upgrade-conifg>
      <env-check
        ref="envCheckRef"
        v-if="data.currStep === 2"
      ></env-check>
      <exe-upgrade
        ref="exeUpgradeRef"
        v-if="data.currStep === 3"
      ></exe-upgrade>
    </div>
    <div class="bottom-btn">
      <a-button
        v-if="data.currStep > 1 && data.backBtnShow"
        :loading="data.loading"
        class="mr-lg"
        @click="handleBack"
      >{{ $t('upgrade.index.5uunxx6pftc0') }}</a-button>
      <a-button
        v-if="data.currStep < 3 && data.nextBtnShow"
        :loading="data.loading"
        @click="handleNext"
      >{{ $t('upgrade.index.5uunxx6pg380') }}</a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import UpgradeConifg from './UpgradeConifg.vue'
import EnvCheck from './EnvCheck.vue'
import ExeUpgrade from './ExeUpgrade.vue'
import { KeyValue } from '@/types/global'
import { reactive, ref, provide } from 'vue'

const upgradeConifgRef = ref<null | InstanceType<typeof UpgradeConifg>>(null)
const envCheckRef = ref<null | InstanceType<typeof EnvCheck>>(null)
const exeUpgradeRef = ref<null | InstanceType<typeof ExeUpgrade>>(null)

const data = reactive<KeyValue>({
  currStep: 1,
  loading: false,
  backBtnShow: true,
  nextBtnShow: true
})

const handleBack = () => {
  data.currStep--
}

const handleNext = async () => {
  if (data.currStep < 3) {
    if (data.currStep === 1) {
      const flag = await upgradeConifgRef.value?.beforeConfirm()
      if (flag) {
        upgradeConifgRef.value?.saveStore()
      } else {
        return
      }
    } else if (data.currStep === 2) {
      const flag = envCheckRef.value?.beforeConfirm()
      if (flag) {
        envCheckRef.value?.saveStore()
      } else {
        return
      }
    }
    data.currStep++
  }
}

const setBackBtnShow = (val: boolean) => {
  data.backBtnShow = val
}

const setNextBtnShow = (val: boolean) => {
  data.nextBtnShow = val
}

const startLoading = () => {
  data.loading = true
}

const cancelLoading = () => {
  data.loading = false
}

provide('loading', {
  startLoading,
  cancelLoading,
  setBackBtnShow,
  setNextBtnShow
})

</script>

<style lang="less" scoped>
.upgrade-container {
  padding: 16px;
  box-sizing: border-box;
  height: calc(100vh - 120px);
  position: relative;

  .center-c {
    height: calc(100% - 89px);
  }

  .bottom-btn {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
