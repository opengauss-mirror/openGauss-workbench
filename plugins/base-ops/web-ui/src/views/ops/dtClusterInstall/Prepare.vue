<template>
  <div id="dt-cluster-install__step1">
    <a-collapse style="margin-bottom: 20px" :default-active-key="['1']">
      <a-collapse-item :header="$t('dtCluster.installConstraints')" key="1">
        <a-row class="grid-demo">
          <a-col :span="1">
            <p style="text-align: center; font-size: 20px">
              <icon-exclamation-circle-fill />
            </p>
          </a-col>
          <a-col :span="23">
            <p>{{ $t("dtCluster.installConstraint1") }}</p>
            <p>{{ $t("dtCluster.installConstraint2") }}</p>
            <p>{{ $t("dtCluster.installConstraint3") }}</p>
            <p>{{ $t("dtCluster.installConstraint4") }}</p>
          </a-col>
        </a-row>
      </a-collapse-item>
    </a-collapse>
    <a-form auto-label-width :model="dtCluster" :rules="formRules" ref="formRef">
      <a-row :gutter="20">
        <a-col :span="9">
          <a-form-item field="name" :label="$t('dtCluster.dtClusterName')" validate-trigger="blur">
            <a-input
              v-model.trim="dtCluster.name"
              :placeholder="$t('dtCluster.inputDtClusterName')"
            ></a-input> </a-form-item
        ></a-col>
        <a-col :span="9"
          ><a-form-item field="type" :label="$t('dtCluster.dtClusterType')" validate-trigger="blur">
            <a-select v-model="dtCluster.type" :placeholder="$t('dtCluster.selectDtClusterType')">
              <a-option
                v-for="option in dtClusterTypes"
                :label="option.label"
                :value="option.value"
              />
            </a-select> </a-form-item
        ></a-col>
        <a-col :span="6" v-show="Number(dtCluster.type) === dtClusterTypes[0].value"
          ><a-form-item
            field="replicationType"
            :label="$t('dtCluster.replicationType')"
            validate-trigger="blur"
          >
            <a-select
              v-model="dtCluster.replicationType"
              :placeholder="$t('dtCluster.selectReplicationType')"
            >
              <a-option
                v-for="option in replicationTypes"
                :label="option.label"
                :value="option.value"
              />
            </a-select> </a-form-item
        ></a-col>
      </a-row>
      <a-row justify="space-between" :gutter="20">
        <a-col :span="12" v-for="(cluster, index) in clusters" :key="index">
          <ClusterItem
            :data="cluster"
            :role="index === 0 ? $t('dtCluster.primary') : $t('dtCluster.standby')"
            :clusterOptions="clusterOptions"
            :diskArrayOptions="diskArrayOptions"
            :ref="setClusterItemRef"
          />
        </a-col>
      </a-row>
      <a-row class="btns" justify="center">
        <a-button type="outline" @click="jumpClusterInstallationPage">{{
          $t("dtCluster.clusterInstallation")
        }}</a-button>
        <a-button type="outline" @click="jumpDiskArrayManagementPage">{{
          $t("dtCluster.diskArrayManagement")
        }}</a-button>
        <a-button type="outline" :loading="isRefreshLoading" @click="refresh">{{
          $t("dtCluster.refresh")
        }}</a-button>
        <a-button type="outline" @click="goNextStep">{{ $t("dtCluster.nextStep") }}</a-button>
      </a-row>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, onBeforeMount } from "vue";
import { useI18n } from "vue-i18n";
import { Message } from "@arco-design/web-vue";
import ClusterItem from "./ClusterItem.vue";
import { apiGetClusterAndDeviceManagerList } from "@/api/resource/dtCluster";

const { t } = useI18n();

const props = defineProps(["installParams"]);
const emit = defineEmits(["goNextStep"]);

const dtCluster = ref({
  name: props.installParams.clusterId || "",
  type: "",
  replicationType: "",
});

const clusters = ref<any>([
  {
    cluster: props.installParams.primaryClusterName || "",
    diskArray: props.installParams.primaryDeviceManager || "",
  },
  {
    cluster: props.installParams.standbyClusterName || "",
    diskArray: props.installParams.standbyDeviceManager || "",
  },
]);

const clusterOptions = ref<any>([]);
const diskArrayOptions = ref<any>([]);
const formRef = ref<any>();
const clusterItemRefs = ref<any>([]);
const isRefreshLoading = ref(false);

const dtClusterTypes = computed(() => [
  { value: 1, label: t("dtCluster.resourcePoolingDt") },
  { value: 2, label: t("dtCluster.tranditionalDt") },
]);

const replicationTypes = computed(() => [
  { value: 1, label: t("dtCluster.storageReplication") },
  { value: 2, label: t("dtCluster.networkReplication") },
]);

onBeforeMount(() => {
  initData();
});

const initData = async () => {
  const res: any = await apiGetClusterAndDeviceManagerList().catch(() => {});
  if (res?.code == 200) {
    clusterOptions.value = res.data.cluster;
    diskArrayOptions.value = res.data.deviceManager;
  }
};

const goNextStep = async () => {
  let formErrors = await Promise.all([
    formRef.value?.validate(),
    ...clusterItemRefs.value.map((x: any) => x?.validate()),
  ]).catch(() => {
    formErrors = ["error"];
  });
  for (let e of formErrors) {
    if (e) {
      return;
    }
  }

  if (clusters.value[0].cluster === clusters.value[1].cluster) {
    Message.error(t("dtCluster.noSameCluster"));
    return;
  }

  if (clusters.value[0].diskArray === clusters.value[1].diskArray) {
    Message.error(t("dtCluster.noSameDiskArray"));
    return;
  }

  props.installParams.clusterId = dtCluster.value.name;
  props.installParams.primaryClusterName = clusters.value[0].cluster;
  props.installParams.primaryDeviceManager = clusters.value[0].diskArray;
  props.installParams.standbyClusterName = clusters.value[1].cluster;
  props.installParams.standbyDeviceManager = clusters.value[1].diskArray;
  props.installParams.businessId = `disasterCluster_install_${Date.now()}`;
  props.installParams.primaryBusinessId = `disasterCluster_install_primary_${Date.now()}`;
  props.installParams.standbyBusinessId = `disasterCluster_install_standby_${Date.now()}`;
  emit("goNextStep");
};

const formRules = computed(() => {
  return {
    name: [
      { required: true, message: t("dtCluster.inputDtClusterName") },
      {
        validator: (value: any, cb: any) => {
          return new Promise((resolve) => {
            const regExp = /[|;&$><`\\!\n]/;
            if (regExp.test(value)) {
              resolve(false);
              cb(t("dtCluster.illegalCharacters"));
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    type: [{ message: t("dtCluster.selectDtClusterType") }],
    replicationType: [{ message: t("dtCluster.selectReplicationType") }],
  };
});

const jumpClusterInstallationPage = () => {
  window.$wujie?.props.methods.jump({
    name: "Static-pluginBase-opsOpsInstall",
  });
};

const jumpDiskArrayManagementPage = () => {
  window.$wujie?.props.methods.jump({
    name: "Static-pluginBase-opsResourceDeviceManager",
  });
};

const refresh = async () => {
  isRefreshLoading.value = true;
  await initData().catch(() => {});
  isRefreshLoading.value = false;
};

const setClusterItemRef = (ref: any) => {
  clusterItemRefs.value.push(ref);
};
</script>
<style lang="less" scoped>
.btns {
  margin-top: 40px;
  button {
    margin-right: 20px;
  }
}
</style>
