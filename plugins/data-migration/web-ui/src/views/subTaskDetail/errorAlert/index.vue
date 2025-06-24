<template>
  <div class="error-alert">
    <el-tabs type="card" style="padding-top: 16px;" @tab-change="alarmPhaseChange" v-model="phaseTab">
      <el-tab-pane :label="$t('components.SubTaskDetail.fullMigration')" :name="item.key" v-for="item in phaseList"
        :key="item.key">
        <template #label>
          <div v-if="phaseNums[item.key] && phaseNums[item.key] > 0">
            {{ item.label }}<el-tag type="round" size="small">{{ phaseNums[item.key] }}</el-tag>
          </div>
          <span v-else>{{ item.label }}</span>
        </template>
      </el-tab-pane>
    </el-tabs>
    <div class="loading-area" v-loading="tableLoading">
      <el-table :data="alarmPhaseList">
        <template #empty>
          <empty-page></empty-page>
        </template>
        <el-table-column prop="sourceName" width="350" :label="$t('components.SubTaskDetail.alarmLocation')">
          <template #default="{ row }">
            <span style="line-height: 1.3">
              {{ locationMap[row.logSource] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="causeCn" :label="$t('components.SubTaskDetail.alarmReason')">
          <template #default="{ row }">
            <div>
              {{ currentLocale === 'en-US' ? row.causeEn : row.causeCn }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="firstDateTime" :label="$t('components.SubTaskDetail.firstAlarmTime')"></el-table-column>
        <el-table-column prop="latestDateTime" :label="$t('components.SubTaskDetail.latestAlarmTime')"></el-table-column>
        <el-table-column prop="operation" :label="$t('components.SubTaskDetail.operate')">
          <template #default="{ row }">
            <el-button text :icon="IconVisible" @click="showErrorDetail(row)">
              {{ $t('components.SubTaskDetail.showDetail') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageNum" v-model:pageSize="pageSize" :page-sizes="pageSizes"
        :total="phaseTotal" :teleported="false" :layout="paginationLayout"
        @change="fetchPhaseAlarmList"></el-pagination>
    </div>

  </div>
  <error-detail v-model:error-visible="errorVisible" :detail-info="detailInfo"
    :area-loading="areaLoading"></error-detail>
</template>
<script setup>
import { ref, watch, onMounted, onBeforeUnmount, computed, inject } from 'vue'
import { IconVisible } from '@computing/opendesign-icons'
import ErrorDetail from './ErrorDetail.vue'
import EmptyPage from '@/components/emptyPage'
import { useI18n } from 'vue-i18n'
import { MIGRATION_MODE, SUB_TASK_STATUS } from '@/utils/constants'
import { getAlarmDetail, getPhaseAlarmList, getTotalAlarmNum } from '@/api/detail'

const { t } = useI18n()
const subTaskId = inject('subTaskId')
const autoRefresh = inject('autoRefresh')
const props = defineProps({
  phaseNums: {
    type: Object,
    default: {
      '1': 0,
      '2': 0,
      '3': 0,
      '4': 0,
      total: 0,
    }
  }
})
watch(() => autoRefresh.value, () => {
  //   The logic for automatic refreshing is determined here.
  if (autoRefresh.value) {
    queryAndSetInterInfo()
  } else {
    clearTimeInterval()
  }
}, { deep: true })

const subTaskInfo = ref({
  migrationModelId: 2
})
const phaseList = computed(() => {
  if (subTaskInfo.value.migrationModelId === MIGRATION_MODE.OFFLINE) {
    return [{
      key: '1',
      label: t('components.SubTaskDetail.fullMigration')
    },
    {
      key: '2',
      label: t('components.SubTaskDetail.fullCheck'),
    },]
  } else {
    return [
      {
        key: '1',
        label: t('components.SubTaskDetail.fullMigration')
      },
      {
        key: '2',
        label: t('components.SubTaskDetail.fullCheck'),
      },
      {
        key: '3',
        label: t('components.SubTaskDetail.incrementMigration'),
      },
      {
        key: '4',
        label: t('components.SubTaskDetail.reverseMigration'),
      }
    ]
  }

})

const locationMap = computed(() => {
  return {
    0: t('components.SubTaskDetail.portal', { id: subTaskId.value }),
    10: t('components.SubTaskDetail.fullMigrationLog'),
    20: t('components.SubTaskDetail.checkCheck'),
    21: t('components.SubTaskDetail.checkSource'),
    22: t('components.SubTaskDetail.checkSink'),
    31: t('components.SubTaskDetail.connectSource'),
    32: t('components.SubTaskDetail.connectSink'),
    41: t('components.SubTaskDetail.reverseConnectSource'),
    42: t('components.SubTaskDetail.reverseConnectSink'),
  }
})

const pageSize = ref(10)
const pageSizes = ref([10, 20, 30, 40])
const paginationLayout = ref('total,sizes,prev,pager,next,jumper')
const pageNum = ref(1)
const alarmPhaseList = ref([])
const phaseTotal = ref(0)
const tableLoading = ref(false)
const intervalTime = ref(null);
const fetchPhaseAlarmList = async () => {
  try {
    tableLoading.value = true
    const { code, rows, total } = await getPhaseAlarmList(subTaskId.value, phaseTab.value, pageSize.value, pageNum.value)
    tableLoading.value = false
    if (code === 200) {
      alarmPhaseList.value = rows || []
      phaseTotal.value = total
    }
  } catch (error) {
    tableLoading.value = false
  }
}

const errorVisible = ref(false)
const areaLoading = ref(false)
const detailInfo = ref({})
const showErrorDetail = async (rows) => {
  try {
    detailInfo.value = {}
    areaLoading.value = true
    errorVisible.value = true
    const { firstDateTime, latestDateTime, sourceName, logSource, id, taskId } = rows
    const { code, data } = await getAlarmDetail(id)
    areaLoading.value = false
    detailInfo.value = {
      firstDateTime, latestDateTime, sourceName, logSource, taskId
    }
    if (code === 200) {
      detailInfo.value.detail = data.detail
    }
  } catch (error) {
    areaLoading.value = false
  }
}

const phaseTab = ref('1')
const alarmPhaseChange = () => {
  // Reset the pagination when switching tabs.
  pageSize.value = 10
  pageNum.value = 1
  fetchPhaseAlarmList()
}

onMounted(() => {
  // Set the timer here -- polling table data
  queryAndSetInterInfo()
})

// Set timer
const queryAndSetInterInfo = () => {
  if (intervalTime.value) {
    clearTimeInterval();
  }

  fetchPhaseAlarmList()
  intervalTime.value = setInterval(() => {
    fetchPhaseAlarmList()
  }, 6000)
}

// Clear the timer
const clearTimeInterval = () => {
  clearInterval(intervalTime.value)
  intervalTime.value = null
}

onBeforeUnmount(() => {
  clearTimeInterval();
})

</script>
<style lang="less" scoped>
.error-alert {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.el-tabs) {
    border-radius: 4px;
    height: 56px !important;

  }

  :deep(.el-tabs--card>.el-tabs__header .el-tabs__item) {
    background-color: var(--o-bg-color-base);
    z-index: 1;
    position: relative;

    &:not(.is-active)::after {
      content: '';
      position: absolute;
      left: 0;
      // bottom: 0;
      top: 88%;
      width: 100%;
      height: 4px;
      background-color: var(--o-bg-color-light);
      z-index: 999;
    }

  }

  .loading-area {
    flex: 1;
    background-color: var(--o-bg-color-base);
    height: 100%;
    padding: 24px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
}
</style>
