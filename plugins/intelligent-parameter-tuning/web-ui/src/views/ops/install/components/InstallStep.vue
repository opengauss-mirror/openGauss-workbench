<template>
  <div class="panel-c" id="tuningInstallStep">
    <offline-install
      v-if="installStore.benchMark === BenchMarkEnum.SYSBENCH "
      ref="offlineInstallRef"
      @selectdbList="getAlldbName"
      @selectschemaNameList="getAllschemaName"
      :dbList="dbList"
      :schemaNameList="schemaNameList"
    />
    <online-install
      v-if="installStore.benchMark === BenchMarkEnum.DWG "
      ref="onlineInstallRef"
      @selectdbList="getAlldbName"
      @selectschemaNameList="getAllschemaName"
      :dbList="dbList"
      :schemaNameList="schemaNameList"
    />
  </div>
</template>
<script lang="ts" setup>
import { onMounted, computed, ref, provide } from 'vue'
import { trainingModel, getAllCluster, getAlldbList, getSchemaNameList } from '@/api/ops' // eslint-disable-line
import OfflineInstall from './OfflineInstall.vue'
import OnlineInstall from './OnlineInstall.vue'
import { useOpsStore } from '@/store'
import { BenchMarkEnum } from '@/types/ops/install'

const opsStore = useOpsStore()
const selectParams = ref([]);
const dbList = ref([]);
const schemaNameList = ref([]);
const offlineInstallRef = ref<InstanceType<typeof OfflineInstall> | null>(null)
const onlineInstallRef = ref<InstanceType<typeof OnlineInstall> | null>(null)
provide('selectParams',selectParams)
onMounted(async () => {
   await getAllClusterName()
})

const getAllClusterName = () => {
  getAllCluster().then((res: KeyValue) => {
    let clusterNameList = []
    if (Number(res.code) === 200) {
      res.obj.forEach((item: KeyValue) => {
        const temp = {
          label: item,
          value: item
        }
        clusterNameList.push(temp)
      })
      selectParams.value = clusterNameList
    }
  }).finally(() => {
  })
}
const getAlldbName = (id) => {
  getAlldbList({clusterName:id}).then((res: KeyValue) => {
    let data = []
    if (Number(res.code) === 200) {
      res.obj.forEach((item: KeyValue) => {
        const temp = {
          label: item,
          value: item
        }
        data.push(temp)
      })
      dbList.value = data
    }
  }).finally(() => {
  })
}
const getAllschemaName = (id) => {
  getSchemaNameList({clusterName:id}).then((res: KeyValue) => {
    let data = []
    if (Number(res.code) === 200) {
      res.obj.forEach((item: KeyValue) => {
        const temp = {
          label: item,
          value: item
        }
        data.push(temp)
      })
      schemaNameList.value = data
    }
  }).finally(() => {
  })
}
const onPrev = () => {
  if (installStore.value.benchMark === BenchMarkEnum.SYSBENCH) {
    offlineInstallRef.value?.submit()
  }
  if (installStore.value.benchMark === BenchMarkEnum.DWG) {
    onlineInstallRef.value?.submit()
  }
}
const onNext = async () => {
  if (installStore.value.installMode === InstallModeEnum.OFF_LINE) {
    let res: any = false
    
  }
  if (installStore.value.installMode === InstallModeEnum.ON_LINE) {
    let res: any = false
    
  }
}

const installStore = computed(() => opsStore.getInstallConfig)
const installType = computed(() => opsStore.getInstallConfig.installType)

defineExpose({
  onPrev,
  onNext
})

</script>
<style lang="less" scoped>@import url('~@/assets/style/ops/ops.less');</style>
