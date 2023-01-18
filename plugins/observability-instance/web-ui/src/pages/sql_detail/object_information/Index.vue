<script setup lang="ts">
import {
    baseInfoOption,
    tableStructureType,
    tableIndexType
} from './common';
import { useRequest } from "vue-request";
import ogRequest from "../../../request";
import { toFixed } from '../../../shared';
import { useI18n } from 'vue-i18n';

const { t } = useI18n()

const props = withDefaults(defineProps<{
    dbid: string | string[];
    sqlId: string | string[];
}>(), {
    dbid: '',
    sqlId: '',
})

const data = reactive<{
    objNameList: string[],
    tableStructureData: Record<string, Array<tableStructureType>>,
    tableIndexData: Record<string, Array<tableIndexType>>,
    tableMetaData: Record<string, Record<string, string>>,
}>({
    objNameList: [],
    tableStructureData: {},
    tableIndexData: {},
    tableMetaData: {}
});

const activeItemTab = ref('');
const activeInfoTab = ref('baseInfo');

const errorInfo = ref<string | undefined>();

const onChangeTab = (curTab: string) => {
    activeItemTab.value = curTab;
}

const getValue = (key: string, curData: Record<string, string>) => {
    if (curData != null && curData[key] != null) {
        if (key === 'object_type') {
            const map: Record<string, string> = {
                r: t('sql.objStructureOther.commonTable'),
                i: t('sql.objStructureOther.indexes'),
                S: t('sql.objStructureOther.sequence'),
                t: t('sql.objStructureOther.toastTable'),
                v: t('sql.objStructureOther.view'),
                m: t('sql.objStructureOther.materializedView'),
                c: t('sql.objStructureOther.combinationType'),
                f: t('sql.objStructureOther.externalTable'),
                p: t('sql.objStructureOther.partitionTable'),
                I: t('sql.objStructureOther.partitionIndex')
            }
            return `${map[curData[key]] || curData[key]}` || t('sql.objStructureOther.none');
        }
        if (key === 'object_size') {
            return `${toFixed(Number.parseFloat(curData[key]) / 1048576)}MB` || t('sql.objStructureOther.none');
        }
        if (key === 'dead_tup_ratio') {
            return curData[key].indexOf(".") === 0 ? `0${curData[key]}` : curData[key];
        }
        return `${curData[key]}` || t('sql.objStructureOther.none');
    }
    return t('sql.objStructureOther.none');
}

const {
    data: objectInfoRes,
    run: requestObjectInfoRes,
    error,
    loading,
} = useRequest((sqlId: string | string[], dbid: string | string[]) =>
    ogRequest.get(`/observability/v1/topsql/object?id=${dbid}&sqlId=${sqlId}`), { manual: true }
)

onMounted(() => {
    requestObjectInfoRes(props.sqlId, props.dbid);
});

const isInfoTip = (value: string | undefined) => {
    return value === 'failGetExecutionPlan' || value === 'failResolveExecutionPlan'
}

watch(error, () => {
    if (error.value != null) {
        errorInfo.value = error.value as unknown as string || 'failGetExecutionPlan';
    }
});

const formatter = (value: boolean, type: string = '') => {
    if (type === 'indisreplident') {
        return value ? t('sql.objStructureOther.partitionIndex') : t('sql.objStructureOther.none');
    }
    if (type === 'description') {
        return value || t('sql.objStructureOther.none');
    }
    return value ? t('sql.yes') : t('sql.no');
}

watch(objectInfoRes, (res) => {
    const objNameList = res.object_name_list;
    if (Array.isArray(objNameList) && objNameList.length > 0) {
        data.objNameList = objNameList;
        activeItemTab.value = objNameList[0];
        data.tableStructureData = res.table_structure;
        data.tableIndexData = res.table_index;
        data.tableMetaData = res.table_metadata;
    }
})

</script>

<template>
    <div class="object-information" v-if="error == null" v-loading="loading">
        <div
            style="text-align:center;width: 100%;padding-top: 16px;"
            v-show="!loading && data.objNameList && data.objNameList.length == 0"
        >{{ $t('sql.noOjcInfoTip') }}</div>
        <div class="o-i-left" v-show="data.objNameList && data.objNameList.length > 0">
            <div
                v-for="item in data.objNameList"
                :key="item"
                :class="`o-i-left-item ${activeItemTab === item ? 'o-i-left-actived' : ''}`"
                @click="onChangeTab(item)"
            >
                {{ data.tableMetaData[item] ? data.tableMetaData[item].schemaname + '.' + item : item }}
            </div>
        </div>
        <div class="o-i-right" v-show="data.objNameList && data.objNameList.length > 0">
            <el-tabs v-model="activeInfoTab">
                <el-tab-pane :label="$t('sql.baseInfo')" name="baseInfo">
                    <el-row class="s-i-el-row" v-for="(item) in baseInfoOption" :key="item.value">
                        <el-col class="s-i-el-col" :span="10">{{ $t(`sql.objBaseInfo.${item.label}`) }}</el-col>
                        <el-col :span="14">{{ getValue(item.value, data.tableMetaData[activeItemTab]) }}</el-col>
                    </el-row>
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.objectStructure')" name="objStructure">
                    <el-table :data="data.tableStructureData[activeItemTab]" border>
                        <el-table-column :label="$t('sql.objStructure.attnum')" prop="attnum" width="100" />
                        <el-table-column :label="$t('sql.objStructure.attname')" prop="attname" width="200" />
                        <el-table-column :label="$t('sql.objStructure.typname')" prop="typname" width="150" />
                        <el-table-column :label="$t('sql.objStructure.attlen')" prop="attlen" width="150" />
                        <el-table-column :label="$t('sql.objStructure.attnotnull')" :formatter="(r: any) => formatter(r.attnotnull)" width="150" />
                        <el-table-column :label="$t('sql.objStructure.description')" :formatter="(r: any) => formatter(r.description, 'description')" />
                    </el-table>
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.indexInformation')" name="indexInfo">
                    <el-table :data="data.tableIndexData[activeItemTab]" border>
                        <el-table-column :label="$t('sql.indexInfo.relname')" prop="relname" width="150" />
                        <el-table-column :label="$t('sql.indexInfo.indisprimary')" :formatter="(r: any) => formatter(r.indisprimary)" width="100" />
                        <el-table-column :label="$t('sql.indexInfo.indisunique')" :formatter="(r: any) => formatter(r.indisunique)" width="100" />
                        <el-table-column :label="$t('sql.indexInfo.indisclustered')" :formatter="(r: any) => formatter(r.indisclustered)" width="100" />
                        <el-table-column :label="$t('sql.indexInfo.indisvalid')" :formatter="(r: any) => formatter(r.indisvalid)" width="100" />
                        <el-table-column :label="$t('sql.indexInfo.indisreplident')" :formatter="(r: any) => formatter(r.indisreplident, 'indisreplident')" width="120" />
                        <el-table-column :label="$t('sql.indexInfo.def')" prop="def" />
                    </el-table>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
    <my-message
        v-if="error != null"
        :type="isInfoTip(errorInfo) ? 'info' : 'error'"
        :tip="$t(`sql.${errorInfo}`)"
        :key="errorInfo"
    />
</template>

<style scoped lang="scss">

.object-information {
    display: flex;
    min-height: 520px;
    background-color: var(--el-bg-color-og);
    overflow-y: auto;

    .o-i-right {
        box-sizing: border-box;
        width: calc(100% - 160px);

        &:deep(.el-tabs__header) {
            background-color: transparent;
            // padding: 0 10px ;
        }

        &:deep(.el-tabs__content) {
            padding: 0 10px 20px 10px ;
        }

        .s-i-el-row {
            width: 500px;
            line-height: 39px;

            &:last-child {
                border-bottom: 1px solid var(--el-color--col-border-color);
            }
        }

        .s-i-el-col {
            background-color: var(--el-color-card-header-color);
        }

        &:deep(.el-col) {
            padding-left: 5px;
            border: 1px solid var(--el-color--col-border-color);
            border-bottom: none;
        }
    }

    .o-i-left {
        box-sizing: border-box;
        border-right: 2px solid var(--el-color--col-border-color);

        &-item {
            box-sizing: border-box;
            width: 100%;
            font-size: 14px;
            color: var(--el-text-color-og);
            padding: 12px;
            cursor: pointer;

            &:hover {
                background-color: var(--el-bg-color-og-hover); 
            }
        }

        &-actived {
            background-color: var(--el-color-card-header-color);
            border-right: 2px solid var(--el-color-tabbar-active);
        }
    }

}

</style>
