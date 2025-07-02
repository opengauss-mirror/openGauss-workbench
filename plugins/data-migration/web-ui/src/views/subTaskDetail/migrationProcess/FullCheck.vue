<template >
    <div class="full-check">
        <div class="statistics">
            <statistic-card v-for="(item, index) in cardConfigList" :key="index" :count="item.count"
                            :description="item.desc" :type="item.type"></statistic-card>
        </div>
        <div class="fullCheckTable" id="fullCheckTable">
            <el-table :data="fullCheckTableData" @filter-change="filterChange" v-loading="fullCheckPage.loading">
              <template #empty>
                <div>
                  <empty-page></empty-page>
                </div>
              </template>
              <el-table-column prop="sourceName" :label="$t('components.SubTaskDetail.sourceTable')"></el-table-column>
                <el-table-column prop="sinkName" :label="$t('components.SubTaskDetail.targetTable')"></el-table-column>
                <el-table-column prop="status" :label="$t('components.SubTaskDetail.checkStatus')" :filters="filterDataList"
                    column-key="status">
                    <template #default="{ row }">
                        <div v-if="row.status === 'success'" class="status-icon">
                            <el-icon class="mr-s">
                                <IconSuccess />
                            </el-icon>
                            <span class="success-color">{{ $t('components.SubTaskDetail.success') }}</span>
                        </div>
                        <div v-else class="status-icon">
                            <el-icon class="mr-s">
                                <IconCloseRed />
                            </el-icon>
                            <span class="danger-color">{{ $t('components.SubTaskDetail.fail') }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="failedRows" :label="$t('components.SubTaskDetail.failedRows')">
                    <template #default="{ row }">
                        {{ row.failedRows ?? 0 }}
                    </template>
                </el-table-column>
                <el-table-column prop="message" :label="$t('components.SubTaskDetail.failReason')"
                    :show-overflow-tooltip="{ 'append-to': '#detailDrawer', trigger: 'hover' }">
                    <template #default="{ row }">
                        {{ row.status === 'failed' ? row.message : '--' }}
                    </template>
                </el-table-column>
                <el-table-column prop="operations" :label="$t('components.SubTaskDetail.operate')">
                    <template #default="{ row }">
                        <el-button text @click="downloadFixShell(row)"
                            :disabled="!(row.status === 'failed' && row.failedRows > 0)">
                            {{ $t('components.SubTaskDetail.downloadFix') }}
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="fullCheckPage.pageNum" class="table-page"
                v-model:pageSize="fullCheckPage.pageSize" :page-sizes="pageSizes" :total="fullCheckPage.total"
                :teleported="false" :layout="paginationLayout" @sizeChange="getFullCheckData"
                @currentChange="getFullCheckData"></el-pagination>
        </div>
    </div>
</template>
<script setup>
import {
    queryFullCheckSummary,
    queryFullCheckDetail,
    downloadRepairFile
} from '@/api/detail';
import { IconSuccess, IconCloseRed, } from '@computing/opendesign-icons';
import { toRefs, computed, onMounted, ref, inject, onBeforeUnmount, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useSubTaskStore } from '@/store';
import EmptyPage from '@/components/emptyPage';
import StatisticCard from '@/components/statisticCard';
const subTaskStore = useSubTaskStore();

const subTaskId = inject('subTaskId');
const autoRefresh = inject('autoRefresh');
const intervalTimer = ref(null)
const { t } = useI18n();
const fullCheckTimer = ref(null)
const cardConfigList = computed(() => {
  const fullCheckProcessObj = subTaskStore.subTaskData.dataCheckProcess?.execResultDetail ? JSON.parse(subTaskStore.subTaskData.dataCheckProcess?.execResultDetail) : {};

  return [{
    desc: t('components.SubTaskDetail.dataTablesTotal'),
    count: fullCheckProcessObj.tableCount || 0,
    type: ''
  },{
    desc: t('components.SubTaskDetail.completedTablesTotal'),
    count: fullCheckProcessObj.completeCount || 0,
    type: 'success'
  },{
    desc: t('components.SubTaskDetail.failTablesTotal'),
    count: (fullCheckProcessObj.tableCount - fullCheckProcessObj.completeCount) || 0,
    type: 'danger'
  }]
})

onMounted(() => {
    getFullCheckData();
});


const interQueryTable = () => {
  cancelInterval()
  intervalTimer.value = setInterval(() => {
    getFullCheckData('loopQuery');
  }, 6000)
}

const cancelInterval = () => {
  if (intervalTimer.value) {
    clearInterval(intervalTimer.value)
    intervalTimer.value = null
  }
}

watch(() => autoRefresh.value, (newVal, oldValue) => {
  if (autoRefresh.value) {
    interQueryTable();
  } else {
    cancelInterval();
  }
}, { immediate: true })


onBeforeUnmount(() => {
  // Cancel polling
  cancelInterval()
})

const downloadFixShell = async (row) => {
    const res = await downloadRepairFile(subTaskId.value, row.repairFileName);
    if (res) {
        const blob = new Blob([res], {
            type: 'text/plain'
        });
        const a = document.createElement('a');
        const URL = window.URL || window.webkitURL;
        const href = URL.createObjectURL(blob);
        a.href = href;
        a.download = row.repairFileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(href);
    }
};

const filterChange = (e) => {
    const status = e.status.length === 2 ? '' : e.status[0];
    fullCheckPage.value.status = status;
    getFullCheckData();
};
const filterDataList = computed(() => [
    { text: t('components.SubTaskDetail.success'), value: "success" },
    { text: t('components.SubTaskDetail.fail'), value: "failed" }
]);

const pageSizes = [10, 20, 30, 40];
const paginationLayout = 'total,sizes,prev,pager,next,jumper';
const fullCheckTableData = ref([]);
const fullCheckPage = ref({
    status: '',
    pageSize: 10,
    pageNum: 1,
    loading: false,
    total: 0,
});
const getFullCheckData = async (loopQuery) => {
    if (loopQuery === 'loopQuery' && !autoRefresh.value) {
        return;
    }
    fullCheckPage.value.loading = true;
    try {
        const { pageNum, pageSize, status } = fullCheckPage.value;
        const res = await queryFullCheckDetail({
            id: subTaskId.value,
            status,
            pageSize,
            pageNum,
        });
        if (res.code === 200) {
            fullCheckTableData.value = res.data.records;
            fullCheckPage.value.total = res.data.total;
        }
        fullCheckPage.value.loading = false;
    } catch (error) {
        cancelInterval()
        fullCheckPage.value.loading = false;
    }
}

</script>
<style lang="less" scoped>
.full-check {
    display: flex;
    flex-direction: column;
    height: 100%;

    .statistics {
        // flex: 1;
        height: 80px;
        margin-bottom: 16px;
        display: flex;
        gap: 4px;
    }

    .fullCheckTable {
        padding: 24px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        flex: 1;
        border-radius: 0px 4px 4px 4px;
        background-color: var(--o-bg-color-base);
    }
}
</style>
