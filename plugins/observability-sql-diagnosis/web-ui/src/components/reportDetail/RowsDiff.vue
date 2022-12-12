<script setup lang="ts">
import { rowsDiffType } from './common';

const props = withDefaults(
    defineProps<{
        rowdDiffList: Array<rowsDiffType>
        changeCurChooseRow: (rowId: string) => void
    }>(),
    {}
);

const changeRow = (index: number) => {
    const { changeCurChooseRow } = props;
    changeCurChooseRow(`row-${index}`);
}

</script>

<template>
    <div class="rows-container">
        <div class="rows-header">{{ $t('report.rowsDiffTitle') }}</div>
        <div class="rows-body">
            <el-table :data="props.rowdDiffList" border>
                <el-table-column :label="$t('report.rowsDiffStep')">
                    <template #default="{row, $index}">
                       <div class="rows-body-step" @click="changeRow($index)">
                            {{ row.stepName }}
                       </div>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('report.estimateRows')" prop="estimateRows" />
                <el-table-column :label="$t('report.actualRows')" prop="actualRows" />
            </el-table>
        </div>
    </div>
</template>
 
<style scoped lang="scss">
.rows-container {
    font-size: 14px;
    color: var(--el-text-color-og);
    margin-bottom: 12px;
}

.rows-header {
    font-weight: 700;
    margin-bottom: 10px;
}

.rows-body {

    :deep(.el-table) {
        background-color: var(--el-bg-color-og);
    }

    :deep(.el-table__header-wrapper th) {
        background-color: var(--el-bg-color-og);
        color: var(--el-text-color-og);
    }

    :deep(.el-table__row) {
        background-color: var(--el-bg-color-og);
    }
    :deep(.el-table__row th) {
        color: var(--el-text-color-og);
    }
}

.rows-body-step {
    color: #177DDC;
    cursor: pointer;
}

</style>
