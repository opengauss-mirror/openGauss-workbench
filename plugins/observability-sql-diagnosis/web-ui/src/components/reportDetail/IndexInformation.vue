<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { tableIndexType } from './common';

const { t } = useI18n();

const props = withDefaults(
    defineProps<{
        indexData: Array<tableIndexType>
    }>(),
    {}
);

const formatter = (value: boolean, type: string = '') => {
    if (type === 'indisreplident') {
        return value ? t('sql.objStructureOther.partitionIndex') : t('sql.objStructureOther.none');
    }
    return value ? t('sql.yes') : t('sql.no');
}

</script>

<template>
    <div class="indexInformation-container">
        <div class="indexInformation-header">{{ $t('sql.indexInformation') }}</div>
        <div class="indexInformation-body">
            <el-table max-height="240" :data="props.indexData" border>
                <el-table-column :label="$t('sql.indexInfo.relname')" prop="relname" width="150" />
                <el-table-column :label="$t('sql.indexInfo.indisprimary')" :formatter="(r: any) => formatter(r.indisprimary)" width="100" />
                <el-table-column :label="$t('sql.indexInfo.indisunique')" :formatter="(r: any) => formatter(r.indisunique)" width="100" />
                <el-table-column :label="$t('sql.indexInfo.indisclustered')" :formatter="(r: any) => formatter(r.indisclustered)" width="100" />
                <el-table-column :label="$t('sql.indexInfo.indisvalid')" :formatter="(r: any) => formatter(r.indisvalid)" width="100" />
                <el-table-column :label="$t('sql.indexInfo.indisreplident')" :formatter="(r: any) => formatter(r.indisreplident, 'indisreplident')" width="120" />
                <el-table-column :label="$t('sql.indexInfo.def')" prop="def" />
            </el-table>
        </div>
    </div>
</template>

<style scoped lang="scss">
.indexInformation-container {
    font-size: 14px;
    color: var(--el-text-color-og);
    margin: 12px 0;
}

.indexInformation-header {
    font-weight: 700;
    margin-bottom: 10px;
}

.indexInformation-body {

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

</style>
