<template>
  <div class="full-migration" id="full-migration">
    <div class="statistics">
      <statistic-card v-for="(item, index) in cardConfigList" :key="index" :count="item.count" :type="item.type"
        :description="item.desc"></statistic-card>
    </div>
    <div class="sub-tabs">
      <el-tabs type="card" v-model="tabName" @tab-change="tabTypeChange">
        <el-tab-pane :label="t('components.SubTaskDetail.dataTable')" name="table"></el-tab-pane>
        <el-tab-pane :label="t('components.BigDataList.5q09jzwfqiw0')" name="view"></el-tab-pane>
        <el-tab-pane :label="t('components.BigDataList.5q09jzwfqm80')" name="function"></el-tab-pane>
        <el-tab-pane :label="t('components.BigDataList.5q09jzwfqp80')" name="trigger"></el-tab-pane>
        <el-tab-pane :label="t('components.BigDataList.5q09jzwfqs40')" name="procedure"></el-tab-pane>
      </el-tabs>
      <div class="main-list">
        <full-migration-table ref="fullMigrationTableRef"></full-migration-table>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import StatisticCard from '@/components/statisticCard'
import FullMigrationTable from './FullMigrationTable.vue';
import { onMounted, ref, inject, computed } from 'vue';
import { useSubTaskStore } from '@/store'
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const subTaskStore = useSubTaskStore()
// The tabular form of each tab corresponding to each page
type tabType = 'table' | 'view' | 'function' | 'trigger' | 'wait'
const tabName = ref<tabType>('table')
const tabTypeVal = computed(() => tabName.value)
const fullMigrationTableRef = ref<any>();
onMounted(async () => {
  fullMigrationTableRef.value?.getTableType(tabTypeVal.value)
})

const tabTypeChange = () => {
  fullMigrationTableRef.value?.getTableType(tabTypeVal.value)
}

const cardConfigList = computed(() => {
  return [{
    desc: t('components.SubTaskDetail.totalMigrationObj'),
    count: subTaskStore.subTaskData.totalWaitCount + subTaskStore.subTaskData.totalErrorCount
      + subTaskStore.subTaskData.totalSuccessCount + subTaskStore.subTaskData.totalRunningCount,
    type: ''
  }, {
    desc: t('components.SubTaskDetail.migrationSuccess'),
    count: subTaskStore.subTaskData.totalSuccessCount,
    type: 'success'
  }, {
    desc: t('components.SubTaskDetail.migrationFail'),
    count: subTaskStore.subTaskData.totalErrorCount,
    type: 'danger'
  }, {
    desc: t('components.SubTaskDetail.migrationRunning'),
    count: subTaskStore.subTaskData.totalRunningCount,
    type: 'primary'
  }, {
    desc: t('components.SubTaskDetail.migrationNotStart'),
    count: subTaskStore.subTaskData.totalWaitCount,
    type: 'wait'
  }]
})

</script>
<style lang="less" scoped>
.full-migration {
  display: flex;
  flex-direction: column;
  height: 100%;

  .statistics {
    display: flex;
    height: 80px;
    margin-bottom: 16px;
    gap: 8px;
  }

  .sub-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;
    border-radius: 0px 4px 4px 4px;

    :deep(.el-tabs) {
      height: 40px !important;

      .el-tab-pane {
        height: 40px !important;
      }
    }

    .el-tabs__content {
      display: none;
    }

    .main-list {
      flex: auto
    }

  }
}
</style>
