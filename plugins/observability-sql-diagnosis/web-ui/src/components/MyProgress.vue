<script setup lang="ts">

type optionType = { label: string, value: number, total?: boolean, color?: string, hide?: boolean }

const props = withDefaults(defineProps<{
    data: Array<optionType>;
    width?: string;
    height?: string;
    text?: string;
    onlyOne?: boolean
    fixTotal?: number
    fixColor?: string[]
}>(), {
    width: '70%',
    height: '15px',
    onlyOne: false,
});

const progressData = reactive<{
    line: Array<{ percentage: string, backgroundColor: string }>
    isHasTotle: boolean,
    totalBackgroundColor: string,
    contentData: Array<optionType>
}>({
    line: [],
    isHasTotle: false,
    totalBackgroundColor: '',
    contentData: []
});

onMounted(() => {
    let curTotal = 0;
    let isHasTotle = false;
    props.data.forEach(item => {
        if (item.total) {
            isHasTotle = true;
            progressData.isHasTotle = true;
            curTotal = item.value
        }
    });

    let tempProgressData = props.data;
    if (!isHasTotle) {
        curTotal = props.data.reduce((sum: number, item: optionType) => sum + item.value, 0);
    } else {
        tempProgressData = props.data.filter((item) => {
            if (item.total) {
                progressData.isHasTotle = true;
                progressData.totalBackgroundColor = item.color || '';
                return false;
            }
            return true
        });
    }

    progressData.line = tempProgressData.map((item) => {
        const percentage = `${(item.value / curTotal * 100).toFixed(2)}%`;
        return {
            percentage,
            backgroundColor: item.color || ''
        }
    })

    progressData.contentData = props.onlyOne ? [props.data[0]] : props.data;
});

</script>

<template>
    <div class="my-progress" :style="{ height: props.height, width: props.width }">
        <el-tooltip
                effect="light"
                placement="right"
            >
                <template #content>
                    <div class="my-progress-content" v-for="(item, index) in progressData.contentData" :key="index">
                        <div class="my-progress-content-item" v-show="!item.hide">
                            <div
                                class="my-progress-content-legend"
                                :style="{
                                    backgroundColor: props.fixColor ? props.fixColor[index] : item.color
                                }"
                            />
                            <div class="my-progress-content-label">{{ item.label }}</div>
                        </div>
                        <p class="my-progress-content-label" v-show="!item.hide">{{ index === 1 ? props.fixTotal || item.value : item.value }}</p>
                    </div>
                </template>
                <div
                    class="my-progress-line"
                    :style="{
                        backgroundColor: progressData.isHasTotle ? progressData.totalBackgroundColor : 'transparent'
                    }"
                >
                    <div
                        v-for="(item, index) in progressData.line" 
                        :key="index"
                        :style="{
                            height: props.height, 
                            width: item.percentage,
                            backgroundColor: item.backgroundColor
                        }"
                    />
                </div>
        </el-tooltip>
        <div class="my-progress-text" v-if="props.text !== undefined">{{ props.text }}</div>
    </div>
</template>

<style scoped lang="scss">
.my-progress {
    width: 100%;

    &-line {
        width: 80%;
        display: flex;
    }

    &-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 160px;

        &-item {
            display: flex;
            align-items: center;
        }
       
        &-legend {
            width: 16px;
            height: 4px;
            margin-right: 6px;
        }

        &-label {
            font-size: 12px;
            color: #D4D4D4;
        }
    }

    &-text {
        font-size: 12px;
    }
}
</style>
