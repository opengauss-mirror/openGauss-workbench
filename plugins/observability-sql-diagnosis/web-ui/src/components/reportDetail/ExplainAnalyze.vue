<script setup lang="ts">

const props = withDefaults(defineProps<{
    planTextArr: Array<string>;
    curChooseRow: string
}>(), {})

const data = reactive<{
    planArr: Array<string>
}>({
    planArr: []
});

watch(() => props.planTextArr, planArr => {
    data.planArr = planArr;
})

watch(() => props.curChooseRow, (res: string) => {
    console.log(res, props.planTextArr);
    if (Array.isArray(props.planTextArr)) {
        const newPlanArr = props.planTextArr.map(query => {
            if (query.includes(res)) {
                return `<p style="margin: 0; padding: 10px 0; background-color: #424242">${query}</p>`
            }
            return query;
        })
        data.planArr = newPlanArr;
    }
});

</script>

<template>
    <div class="analyze-container">
        <div class="analyze-header">{{ $t('report.analyzeExcuationPlan') }}</div>
        <div class="analyze-body">
            <pre class="analyze-pre" v-for="(item) in data.planArr" :key="item" v-html="item"></pre>
        </div>
    </div>
</template>
 
<style scoped lang="scss">
.analyze-container {
    font-size: 14px;
    color: var(--el-text-color-og);
    margin-bottom: 12px;
}

.analyze-header {
    font-weight: 700;
    margin-bottom: 10px;
}

.analyze-body {
    box-sizing: border-box;
    width: 100%;
    height: 350px;
    padding: 10px;
    border: 1px solid $og-border-color;
    overflow: scroll;

    :deep(.el-table) {
        background-color: $og-background-color;
    }

    :deep(.el-table__header-wrapper th) {
        background-color: $og-background-color;
    }

    :deep(.el-table__row) {
        background-color: $og-background-color;
    }
}

.analyze-pre {
    color: var(--el-text-color-og);
    margin: 0;
    padding: 5px 0;
    font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

</style>
