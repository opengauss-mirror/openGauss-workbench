<template>
  <div class="panel-c">
    <div class="flex-row-center panel-body">
      <div
        :class="'deploy-size card-item-c mr-xlg ' + (deployType === 'SINGLE_NODE' ? 'center-item-active' : 'center-item')"
        @click="chooseType(DeployTypeEnum.SINGLE_NODE)"
      >
        <div class="flex-row mb">
          <svg-icon
            icon-class="ops-host"
            class="icon-size-s mr"
          ></svg-icon>
          <div class="instance-c">
            <svg-icon
              icon-class="ops-instance"
              class="icon-size-s"
            ></svg-icon>
          </div>
        </div>
        <div class="label-color ft-main">{{ $t('simple.DeployWay.5mpmphlozp00') }}</div>
      </div>
      <div
        :class="'deploy-cluster-size card-item-c ' + (deployType === 'CLUSTER' ? 'center-item-active' : 'center-item')"
        @click="chooseType(DeployTypeEnum.CLUSTER)"
      >
        <div class="flex-row mb">
          <svg-icon
            icon-class="ops-host"
            class="icon-size-s mr"
          ></svg-icon>
          <div class="instance-type-c mr">
            <a-tag class="type">{{ $t('simple.DeployWay.5mpmphlp0eo0') }}</a-tag>
            <svg-icon
              icon-class="ops-instance"
              class="icon-size-s"
            ></svg-icon>
          </div>
          <div class="instance-type-c">
            <a-tag class="type">{{ $t('simple.DeployWay.5mpmphlp0k00') }}</a-tag>
            <svg-icon
              icon-class="ops-instance"
              class="icon-size-s"
            ></svg-icon>
          </div>
        </div>
        <div class="label-color  ft-main">{{ $t('simple.DeployWay.5mpmphlp0nw0') }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { DeployTypeEnum, LiteInstallConfig } from '@/types/ops/install'
import { useOpsStore } from '@/store'

const deployType = ref(DeployTypeEnum.SINGLE_NODE)

const chooseType = (type: DeployTypeEnum) => {
  deployType.value = type
}

const deployStore = useOpsStore()

onMounted(() => {
  if (deployStoreVal.value) {
    deployType.value = deployStoreVal.value
  }
})

onBeforeUnmount(() => {
  console.log('deploy select', deployStore.getInstallConfig.deployType, deployType.value)
  if (deployStore.getInstallConfig.deployType !== deployType.value) {
    const liteConfig = {
      clusterName: '',
      installPackagePath: '',
      port: Number(5432),
      databaseUsername: '',
      databasePassword: '',
      nodeConfigList: []
    }
    deployStore.setLiteConfig(liteConfig as LiteInstallConfig)
  }
  deployStore.setInstallContext({
    deployType: deployType.value
  })
})

const deployStoreVal = computed(() => deployStore.getInstallConfig.deployType)

</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');

.deploy-size {
  width: 250px;
  height: 250px;

  .instance-c {
    padding: 20px;
    border-radius: 8px;
    border: 1px dashed #4E5969;
  }
}

.deploy-cluster-size {
  width: 330px;
  height: 250px;

  .instance-type-c {
    padding: 40px 20px 10px;
    border-radius: 8px;
    border: 1px dashed #4E5969;
    position: relative;

    .type {
      position: absolute;
      top: 4px;
      left: 4px;
    }
  }
}
</style>
