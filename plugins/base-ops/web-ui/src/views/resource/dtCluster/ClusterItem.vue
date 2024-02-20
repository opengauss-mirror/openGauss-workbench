<template>
  <div class="cluster-item">
    <div class="cluster-role-triangle">
      <div class="cluster-role">{{ getClusterRole() }}</div>
    </div>
    <a-row class="mb title">
      <div class="mr-s">{{ $t("dtCluster.clusterName") }}:</div>
      <div class="mr-s name">
        {{ data.clusterName || "--" }}
      </div>
      <div class="mr-s">
        {{ data.version ? getVersionName() : "--" }}
      </div>
      <div>
        {{ data.versionNum || "--" }}
      </div>
    </a-row>
    <a-row>
      <a-col flex="2">
        <svg-icon icon-class="ops-mini-version" class="icon-size"></svg-icon>
      </a-col>
      <a-col flex="8" style="max-width:80%;">
        <a-row class="mb-s info-row" align="center">
          <a-col flex="6">
            <a-row align="center">
              <div class="mr-s label-color">{{ $t("dtCluster.currentStatus") }}:</div>
              <a-tag :color="getClusterStateColor()">{{ getClusterState() }}</a-tag></a-row
            >
          </a-col>
          <a-col flex="6">
            <a-row align="center" style="height: 100%">
              <div class="mr-s label-color">{{ $t("dtCluster.nodeNum") }}:</div>
              <a-row v-if="data.slaveIps.length > 0">
                <div class="mr-s">1 {{ $t("dtCluster.primary") }}</div>
                <div>
                  {{ data.slaveIps.length }}
                  {{ $t("dtCluster.standby") }}
                </div>
              </a-row>
              <div v-else>
                {{ $t("dtCluster.singleNode") }}
              </div></a-row
            >
          </a-col>
        </a-row>
        <a-row class="mb-s info-row" align="center">
          <a-col flex="6">
            <a-row align="center">
              <div class="mr-s label-color">{{ $t("dtCluster.dbUser") }}:</div>
              <div class="info-row-value">
                {{ data.dbUser }}
              </div>
            </a-row>
          </a-col>
          <a-col flex="6">
            <a-row align="center">
              <div class="mr-s label-color">{{ $t("dtCluster.dbPort") }}:</div>
              <div>
                {{ data.dbPort }}
              </div>
            </a-row>
          </a-col>
        </a-row>
        <a-row class="mb-s info-row" align="center">
          <a-col flex="6">
            <a-row align="center">
              <div class="mr-s label-color">{{ $t("dtCluster.primaryNodeIp") }}:</div>
              <div>
                {{ data.masterIp }}
              </div>
            </a-row>
          </a-col>
          <a-col flex="6" v-if="data.slaveIps?.length > 0">
            <a-row align="center"
              ><div class="mr-s label-color">{{ $t("dtCluster.standbyNodeIp") }}:</div>
              <div v-if="data.slaveIps?.length === 1">
                {{ data.slaveIps[0] }}
              </div>
              <div v-else>
                <a-trigger trigger="click" @popup-visible-change="onSlaveIpsPopupChange">
                  <a-button type="text" size="small" class="dtcluster-slave-ips__btn"
                    >{{ data.slaveIps[0] }}
                    <icon-caret-up v-if="isSlaveIpsPopupShow" /><icon-caret-down v-else />
                  </a-button>
                  <template #content>
                    <div class="dtcluster-slave-ips">
                      <div class="slave-ip mb-s" v-for="option in data.slaveIps">
                        {{ option }}
                      </div>
                    </div>
                  </template>
                </a-trigger>
              </div>
            </a-row>
          </a-col>
        </a-row>
        <a-row class="mb-s info-row" align="center" v-if="data.envPath">
          <div class="mr-s label-color">{{ $t("dtCluster.envVarPath") }}:</div>
          <div class="info-row-value__full">{{ data.envPath }}</div>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { CLUSER_ROLE, CLUSTER_STATE } from "./constant";

const { t } = useI18n();

const props = defineProps({
  data: {
    type: Object,
    default: () => {},
  },
});

const isSlaveIpsPopupShow = ref(false);

const getClusterRole = () => {
  switch (props.data.clusterRole?.toUpperCase()) {
    case CLUSER_ROLE.PRIMARY:
      return t("dtCluster.primary");
    case CLUSER_ROLE.STANDBY:
      return t("dtCluster.standby");
    default:
      return t("dtCluster.unknown");
  }
};

const getClusterState = () => {
  switch (props.data.state?.toUpperCase()) {
    case CLUSTER_STATE.NORMAL:
      return t("dtCluster.available");
    case CLUSTER_STATE.UNAVAILABLE:
      return t("dtCluster.unavailable");
    case CLUSTER_STATE.DEGRADED:
      return t("dtCluster.degraded");
    default:
      return t("dtCluster.unknown");
  }
};

const getClusterStateColor = () => {
  switch (props.data.state?.toUpperCase()) {
    case CLUSTER_STATE.NORMAL:
      return "green";
    case CLUSTER_STATE.UNAVAILABLE:
    case CLUSTER_STATE.DEGRADED:
      return "red";
    default:
      return "gray";
  }
};

const getVersionName = () => {
  switch (props.data.version?.toUpperCase()) {
    case "MINIMAL_LIST":
      return t("operation.DailyOps.5mplp1xc46o0");
    case "LITE":
      return t("operation.DailyOps.5mplp1xc4b40");
    default:
      return t("operation.DailyOps.5mplp1xc4fg0");
  }
};

const onSlaveIpsPopupChange = (visible: boolean) => {
  isSlaveIpsPopupShow.value = visible;
};
</script>
<style lang="less" scoped>
.cluster-item {
  border: 1px dashed #00b42a;
  padding: 25px 40px;
  position: relative;

  .title {
    max-width: 100%;
    display: flex;
    flex-wrap: nowrap;
    white-space: nowrap;
    height: 18px;
    line-height: 18px;
    .name {
      max-width: 80%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .cluster-role-triangle {
    width: 50px;
    height: 50px;
    position: absolute;
    top: 0;
    left: 0;
    clip-path: polygon(0 0, 50px 0, 0 50px);
    background: #00b42a;
    .cluster-role {
      position: absolute;
      top: 8px;
      left: 8px;
      color: #ffffff;
    }
  }

  .icon-size {
    height: 100px;
    width: 100px;
  }
  .info-row {
    height: 28px;
    width: 100%;
    white-space: nowrap;
    display: flex;
    flex-wrap: nowrap;
  }
  .info-row-value {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    max-width: 160px;
    height: 18px;
    line-height: 18px;
  }
  .info-row-value__full {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    height: 18px;
    line-height: 18px;
  }
}
</style>
<style>
.dtcluster-slave-ips {
  padding: 10px 10px 5px 10px;
  background-color: var(--color-bg-popup);
  border-radius: 4px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.15);
  color: var(--color-text-1);
}
.dtcluster-slave-ips__btn {
  padding: 0;
  color: var(--color-text-1) !important;
}
</style>
