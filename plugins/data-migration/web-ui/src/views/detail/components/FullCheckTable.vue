<template>
    <div class="fullCheckTable" id="fullCheckTable">
        <el-table :data="props.fullCheckTableData" @filter-change="filterChange" v-loading="fullCheckPage.loading">
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
            <el-table-column prop="message" :label="$t('components.SubTaskDetail.failReason')" :show-overflow-tooltip="{'append-to':'#detailDrawer',trigger:'hover'}">
                <template #default="{ row }">
                    {{ row.status === 'failed' ? row.message : '--' }}
                </template>
            </el-table-column>
            <el-table-column prop="operations" :label="$t('components.SubTaskDetail.operate')">
                <template #default="{ row }">
                    <el-button text @click="downloadFixShell(row)" :disabled="!(row.status === 'failed' && row.failedRows > 0)">
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
</template>
<script setup>
import { IconSuccess, IconCloseRed, } from '@computing/opendesign-icons'
import { toRefs, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { downloadRepairFile } from '@/api/detail'

const { t } = useI18n()
const props = defineProps({
    id: {
        type: Number,
        require: true
    },
    fullCheckPage: {
        type: Object,
        require: true
    },
    fullCheckTableData: {
        type: Array,
        require: true
    }
})

const { fullCheckPage } = toRefs(props)
const downloadFixShell = async (row) => {
    const res = await downloadRepairFile(props.id, row.repairFileName)
    if (res) {
        const blob = new Blob([res], {
            type: 'text/plain'
        })
        const a = document.createElement('a')
        const URL = window.URL || window.webkitURL
        const herf = URL.createObjectURL(blob)
        a.href = herf
        a.download = row.repairFileName
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        window.URL.revokeObjectURL(herf)
    }
}

const filterChange = (e) => {
    const status = e.status.length === 2 ? '' : e.status[0]
    fullCheckPage.value.status = status
    getFullCheckData()
}
const filterDataList = computed(()=>[
    { text: t('components.SubTaskDetail.success'), value: "success" },
    { text: t('components.SubTaskDetail.fail'), value: "failed" }
])

const pageSizes = [10, 20, 30, 40]
const paginationLayout = 'total,sizes,prev,pager,next,jumper'

const emits = defineEmits(['update:fullCheckPage','refreshTableDate'])

const getFullCheckData = () => {
    emits('update:fullCheckPage', fullCheckPage.value)
    emits('refreshTableDate')
}
</script>
<style lang="less">
.el-table .cell{
    line-height: 18px;
}
.fullCheckTable {
    margin-top: 16px;
    .table-page {
        margin-top: 8px;
    }
}
    
</style>