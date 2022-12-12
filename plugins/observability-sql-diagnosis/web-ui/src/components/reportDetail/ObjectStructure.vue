<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { tableStructureType } from './common';

const { t } = useI18n();

const props = withDefaults(
    defineProps<{
        structureData: Array<tableStructureType>
    }>(),
    {}
);

const formatter = (value: boolean, type: string = '') => {
    if (type === 'description') {
        return value || t('sql.objStructureOther.none');
    }
    return value ? t('sql.yes') : t('sql.no');
}

</script>

<template>
    <div class="objectStructure-container">
        <div class="objectStructure-header">{{ $t('sql.objectStructure') }}</div>
        <div class="objectStructure-body">
            <el-table max-height="240" :data="props.structureData" border>
                <el-table-column :label="$t('sql.objStructure.attnum')" prop="attnum" width="100" />
                <el-table-column :label="$t('sql.objStructure.attname')" prop="attname" width="200" />
                <el-table-column :label="$t('sql.objStructure.typname')" prop="typname" width="150" />
                <el-table-column :label="$t('sql.objStructure.attlen')" prop="attlen" width="150" />
                <el-table-column :label="$t('sql.objStructure.attnotnull')" :formatter="(r: any) => formatter(r.attnotnull)" width="150" />
                <el-table-column :label="$t('sql.objStructure.description')" :formatter="(r: any) => formatter(r.description, 'description')" />
    </el-table>
        </div>
    </div>
</template>

<style scoped lang="scss">
.objectStructure-container {
    font-size: 14px;
    color: var(--el-text-color-og);
    margin: 12px 0;
}

.objectStructure-header {
    font-weight: 700;
    margin-bottom: 10px;
}

.objectStructure-body {

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
