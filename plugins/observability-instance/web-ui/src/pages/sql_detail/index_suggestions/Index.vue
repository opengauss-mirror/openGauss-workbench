<script setup lang="ts">
import { useRequest } from "vue-request";
import ogRequest from "../../../request";
import { i18n } from '../../../i18n';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = withDefaults(defineProps<{
    dbid: string | string[];
    sqlId: string | string[];
    sqlText: string;
}>(), {
    dbid: '',
    sqlId: '',
    sqlText: '',
})

const isHasSuggestion = ref<boolean>(false);

const placeholders = ref<string>("")

const errorInfo = ref<Error | undefined>();

const data = reactive<{
    indexSuggestionData: Array<string>
}>({
    indexSuggestionData: [],
});

const { data: indexSuggRes, run: requestIndexSuggRes, error, loading } = useRequest((sqlId: string | string[], dbid: string | string[]) =>
    ogRequest.get(`/observability/v1/topsql/index?id=${dbid}&sqlId=${sqlId}`), { manual: true }
)

onMounted(() => {
    if (typeof props.sqlText === 'string' && props.sqlText.includes("?")) {
        placeholders.value = `The SQL statement has a placeholder, unable to obtain index suggestions. It is recommended that the tracks in the database_ stmt_ The parameter parameter is set to on to obtain new SQL without placeholder`;
        return;
    }
    requestIndexSuggRes(props.sqlId, props.dbid);
});

watch(indexSuggRes, (res) => {
    if (Array.isArray(res)) {
        if (res.includes("No index suggestions") || res.length === 0) {
            isHasSuggestion.value = false
        } else {
            isHasSuggestion.value = true
            data.indexSuggestionData = res;
        }
    }
})

watch(error, () => {
    if (error.value != null) {
        errorInfo.value = error.value;
    }
});

</script>

<template>
    <div class="index-suggestions"  v-if="error == null && placeholders === ''" v-loading="loading">
        <div v-if="isHasSuggestion">
            <el-row v-for="(item, index) in data.indexSuggestionData" :key="index">
                <el-col :span="1" class="i-s-el-col-index">{{ index + 1 }}</el-col>
                <el-col :span="23" class="i-s-el-col-content">{{ item }}</el-col>
            </el-row>
        </div>
        <div v-else>
            <el-row>
                <el-col :span="1" class="i-s-el-col-index">1</el-col>
                <el-col :span="23" class="i-s-el-col-content">{{ t('report.noIndexSuggestions') }}</el-col>
            </el-row>
        </div>
    </div>
    <my-message
        v-if="error != null && placeholders === ''"
        type="error"
        :tip="errorInfo"
        defaultTip=""
        :key="i18n.global.locale.value"
    />
    <my-message
        v-if="placeholders !== ''"
        type="error"
        :tip="$t('sql.placeholderTip')"
        defaultTip=""
        :key="i18n.global.locale.value"
    />
</template>

<style scoped lang="scss">

.index-suggestions {
    font-size: 14px; 
    border-top: 1px solid var(--el-color--col-border-color);
    min-height: 100px;

    .i-s-el-col-index {
        border-right: 1px solid var(--el-color--col-border-color);
        text-align: center;
    }

    .i-s-el-col-content {
        text-align: left;
        padding-left: 10px;
    }

    &:deep(.el-row) {
        line-height: 39px;
        border: 1px solid var(--el-color--col-border-color);
        border-top: none;
    }
}

</style>
