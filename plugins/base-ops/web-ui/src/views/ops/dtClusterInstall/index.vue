<template>
  <div id="dt-cluster-install">
    <a-steps :current="currStep">
      <a-step>{{ $t("dtCluster.index") }}</a-step>
      <a-step>{{ $t("dtCluster.exeInstall") }}</a-step>
    </a-steps>
    <a-divider />
    <div v-if="currStep === 1">
      <Prepare :installParams="installParams" @goNextStep="goStep(STEP.STEP2)" />
    </div>
    <div v-if="currStep === 2">
      <Install :installParams="installParams" @goLastStep="goStep(STEP.STEP1)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

import Prepare from "./Prepare.vue";
import Install from "./Install.vue";
import { STEP } from "./constant";

let currStep = ref<number>(STEP.STEP1);
const installParams = ref({});

const goStep = (step: number) => {
  currStep.value = step;
};
</script>

<style lang="less" scoped>
#dt-cluster-install {
  padding: 20px;
}
</style>
