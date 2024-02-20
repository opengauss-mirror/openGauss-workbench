<template>
  <div id="dt-cluster">
    <div v-if="dtClusterList?.length">
      <DtClusterItem
        v-for="dtCluster in dtClusterList"
        :key="dtCluster.disasterCluterName"
        :data="dtCluster"
        @refresh="refresh"
      ></DtClusterItem>
    </div>
    <div class="empty full-w full-h flex-col" v-else>
      <svg-icon icon-class="ops-empty" class="icon-size icon-size mb"></svg-icon>
      <a-row class="mb">{{ $t("dtCluster.empty") }}</a-row>
      <a-row align="center">
        <a-button class="mr-s" type="outline" @click="jumpDtClusterInstallationPage">{{
          $t("dtCluster.index")
        }}</a-button>
        <a-button type="outline" :loading="isRefreshLoading" @click="refresh">{{
          $t("dtCluster.refresh")
        }}</a-button>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeMount, ref } from "vue";
import { apiGetDtCluster } from "@/api/resource/dtCluster";
import DtClusterItem from "./DtClusterItem.vue";
import { CLUSER_ROLE } from "./constant";

const dtClusterList = ref<any>([]);
const isRefreshLoading = ref(false);

onBeforeMount(() => {
  getList();
});

const getList = () => {
  return apiGetDtCluster().then(
    (res: any) => {
      if (res?.code == 200 && res?.data) {
        res.data.forEach((item: any) => {
          item.subClusters.sort((a: any, b: any) =>
            a.clusterRole?.toUpperCase() === CLUSER_ROLE.PRIMARY ? -1 : 1
          );
        });
        dtClusterList.value = res.data;
      }
    },
    () => {}
  );
};

const jumpDtClusterInstallationPage = () => {
  window.$wujie?.props.methods.jump({
    name: "Static-pluginBase-opsOpsDisasterClusterInstall",
  });
};

const refresh = async () => {
  isRefreshLoading.value = true;
  await getList().catch(() => {});
  isRefreshLoading.value = false;
};
</script>

<style lang="less" scoped>
#dt-cluster {
  height: calc(100vh - 130px);
  overflow: auto;
  padding: 0 20px;
  color: var(--color-text-1);

  .empty {
    color: var(--color-neutral-4);
  }

  .icon-size {
    width: 100px;
    height: 100px;
  }
}
</style>
