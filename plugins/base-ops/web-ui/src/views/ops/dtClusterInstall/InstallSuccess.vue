<template>
  <div class="flex-col full-w full-h" id="dtInstallSuccess">
    <svg-icon icon-class="ops-install-success" class="icon-size mb"></svg-icon>
    <div class="label-color mb-lg">
      {{ $t("dtCluster.installSuccess") }}
    </div>
    <div class="install-connect-c flex-col mb-xlg">
      <div class="flex-row mb-s">
        <div class="label-w">
          {{ $t("dtCluster.dtClusterName") }}
        </div>
        <div class="label-value">
          {{ installParams.clusterId }}
        </div>
      </div>
      <div class="flex-row mb-s">
        <div class="label-w">
          {{ $t("dtCluster.dtClusterType") }}
        </div>
        <div class="label-value">{{ $t("dtCluster.resourcePoolingDt") }}</div>
      </div>
      <div class="flex-row">
        <div class="label-w">
          {{ $t("dtCluster.replicationType") }}
        </div>
        <div class="label-value">{{ $t("dtCluster.storageReplication") }}</div>
      </div>
    </div>
    <div class="flex-row">
      <a-button type="outline" class="mr" @click="goHome">{{ $t("dtCluster.goHome") }}</a-button>
      <a-button type="primary" class="mr" @click="downloadLog">{{
        $t("dtCluster.downloadLog")
      }}</a-button>
      <a-button type="primary" @click="goDtClusterManagement">{{
        $t("dtCluster.dtClusterManagement")
      }}</a-button>
    </div>
  </div>
</template>
<script setup lang="ts">

const props = defineProps({
  installParams: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["downloadLog"]);

const goHome = () => {
  window.$wujie?.props.methods.jump({
    name: "Dashboard",
  });
};

const goDtClusterManagement = () => {
  window.$wujie?.props.methods.jump({
    name: "Static-pluginBase-opsResourceDisasterClusterManager",
  });
};

const downloadLog = () => {
  emit("downloadLog");
};
</script>
<style lang="less" scoped>
.icon-size {
  width: 100px;
  height: 100px;
}
.install-connect-c {
  padding: 20px 60px;
  background-color: #f2f3f5;
  border-radius: 8px;

  .label-w {
    width: 80px;
    text-align: left;
    margin-right: 15px;
    white-space: nowrap;
  }

  .label-value {
    width: 120px;
    text-align: left;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    height: 18px;
    line-height: 18px;
  }
}
</style>
