<template>
  <a-row :gutter="20" justify="center" class="mb" id="dtInstall">
    <a-col :span="16" v-if="status === STATUS.INSTALLING || status === STATUS.FAILURE">
      <Installing
        :status="status"
        :installParams="installParams"
        :logs="logs"
        @updateStatus="updateStatus"
        ref="installingRef"
      />
    </a-col>
    <a-col :span="8" v-if="status === STATUS.FAILURE">
      <InstallFailure :installParams="installParams" @retry="retry" @downloadLog="downloadLog" />
    </a-col>
  </a-row>
  <a-row justify="center" v-if="status === STATUS.FAILURE">
    <a-button type="outline" @click="goLastStep">{{ $t("dtCluster.lastStep") }}</a-button>
  </a-row>
  <div v-if="status === STATUS.SUCCESS">
    <InstallSuccess :installParams="installParams" @downloadLog="downloadLog" />
  </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import dayjs from "dayjs";
import Installing from "./Installing.vue";
import InstallSuccess from "./InstallSuccess.vue";
import InstallFailure from "./InstallFailure.vue";
import { STATUS } from "./constant";

const props = defineProps({
  installParams: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["goLastStep"]);

const status = ref<any>(STATUS.INSTALLING);
const installingRef = ref<any>(null);
const logs = ref<any>({});

const goLastStep = () => {
  emit("goLastStep");
};

const updateStatus = (s: string) => {
  status.value = s;
};

const retry = () => {
  status.value = STATUS.INSTALLING;
  installingRef.value.install();
};

const downloadLog = () => {
  for (let key in logs.value) {
    const time = dayjs().format("YYYY-MM-DD_HH:mm:ss");
    const filename = `${key}_${time}.log`;

    const blob = new Blob([logs.value[key]], { type: "text/plain" });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
};
</script>
<style lang="less" scoped></style>
