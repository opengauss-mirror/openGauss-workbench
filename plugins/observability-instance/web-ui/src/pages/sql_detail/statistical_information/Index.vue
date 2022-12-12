<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { baseInfoOption, executeOption, consumingOption } from './common';
import { useWindowStore } from "../../../store/window";

const { theme } = storeToRefs(useWindowStore())

const props = withDefaults(defineProps<{
    data: Record<string, string>,
    loading: boolean
}>(), {
    loading: false
})
const data = reactive<{
    useTimeStatistical: Array<{ name: string, value: number, color?: string }>,
    consumingPieData: Array<{ name: string, value: number }>,
    consumingPieColor: Array<string>
}>({
    useTimeStatistical: [],
    consumingPieData: [],
    consumingPieColor: []
});

const getValue = (key: string, curData: Record<string, string>) => {
    if (curData != null) {
        return curData[key] || '-';
    }
    return '-';
}

watch(() => props.data, (res) => {
    // eslint-disable-next-line camelcase
    const { cpu_time, wait_time, db_time } = res;
    try {
        const cpuTimeDb = Number.parseInt(cpu_time) / Number.parseInt(db_time) * 100;
        const waitTimeDb = Number.parseInt(wait_time) / Number.parseInt(db_time) * 100;
        data.useTimeStatistical = [{
            name: 'waitTimeLabel',
            value: waitTimeDb,
            color: '#3DC94C'
        }, {
            name: 'cpuTimeLabel',
            value: cpuTimeDb,
            color: '#DA864C'
        }]
        consumingOption.forEach(item => {
            const value = Number.parseInt(getValue(item.value, props.data));
            if (value > 0) {
                data.consumingPieColor.push(item.color as string);
                data.consumingPieData.push({
                    name: item.label,
                    value: Number.parseInt(getValue(item.value, props.data))
                });
            }
        })
    } catch (e) {
        data.useTimeStatistical = [];
    };
}, { deep: true })

</script>

<template>
    <div class="statistical-information" v-loading="props.loading">
        <div class="s-i-row">
            <div class="s-i-col-left">
                <my-card height="280" :title="$t('sql.baseInfoTitle')" :bodyPadding="false">
                    <div class="s-i-base">
                        <p class="s-i-base-item" v-for="item in baseInfoOption" :key="item.value">
                            {{ $t(`sql.baseInfoOption.${item.label}`) }}
                            {{ `：${ getValue(item.value, props.data) }` }}
                        </p>
                    </div>
                </my-card>
            </div>
            <div class="s-i-col-right">
                <my-card height="280" :title="$t('sql.executionStatisticTitle')" :bodyPadding="false">
                    <div class="s-i-execute">
                        <el-row v-for="(item, index) in executeOption" :key="index">
                            <el-col class="s-i-el-col" :span="5">{{ $t(`sql.executeOption.${item[0].label}`) }}</el-col>
                            <el-col :span="5">{{ getValue(item[0].value, props.data) }}</el-col>
                            <el-col class="s-i-el-col" :span="8">{{ $t(`sql.executeOption.${item[1].label}`) }}</el-col>
                            <el-col :span="6">{{ getValue(item[1].value, props.data) }}</el-col>
                        </el-row>
                    </div>
                </my-card>
            </div>
        </div>
        <div class="s-i-row">
            <div class="s-i-col-left">
                <my-card height="300" :title="$t('sql.consumptionStatisticTitle')" :bodyPadding="false">
                    <div class="s-i-statistics">
                        <div class="s-i-statistics-box">
                            <div class="s-i-statistics-pie">
                                <my-pie
                                    :showLegend="false"
                                    :center="['50%', '50%']"
                                    :data="data.useTimeStatistical"
                                    :color="['#3DC94C', '#DA864C']"
                                    :theme="theme"
                                    :key="theme"
                                />
                            </div>
                            <div class="s-i-statistics-legend">
                                <div class="s-i-statistics-item" v-for="item in data.useTimeStatistical" :key="item.value">
                                    <span :style="`background-color: ${item.color}`" class="s-i-statistics-item-block"></span>
                                    <span>{{ $t(`sql.${item.name}`)  }} {{ `（${item.value.toFixed(2)}%）` }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="s-i-statistics-list">
                            <p class="s-i-statistics-text">{{ `${$t('sql.dbTimeLabel')}：${props.data['db_time']} ms` }}</p>
                            <p class="s-i-statistics-text">{{ `${$t('sql.cpuTimeLabel')}：${props.data['cpu_time']} ms` }}</p>
                            <p class="s-i-statistics-text">{{ `${$t('sql.waitTimeLabel')}：${props.data['wait_time']} ms` }}</p>
                        </div>
                    </div>
                </my-card>
            </div>
            <div class="s-i-col-right">
                <my-card height="300" :title="$t('sql.consumingBreakdownTitle')" :bodyPadding="false">
                    <div class="s-i-consuming">
                        <div class="s-i-consuming-pie">
                            <my-pie
                                :showLegend="false"
                                :center="['50%', '50%']"
                                :data="data.consumingPieData"
                                :color="data.consumingPieColor"
                                :theme="theme"
                                :key="theme"
                            />
                        </div>
                        <div class="s-i-consuming-legend">
                            <div class="s-i-consuming-item" v-for="item in consumingOption" :key="item.value">
                                <span :style="`background-color: ${item.color}`" class="s-i-consuming-item-block"></span>
                                <span class="s-i-consuming-item-label">{{ $t(`sql.consumingOption.${item.label}`) }}：</span>
                                <span class="s-i-consuming-item-value">{{ `${getValue(item.value, props.data)} ms` }}</span>
                            </div>
                        </div>
                    </div>
                </my-card>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">

.statistical-information {
    font-size: 14px;
    // border-top: 1px solid $og-border-color;

    .s-i-row {
        width: 100%;
        display: flex;
        justify-content: space-between;
        margin-bottom: 18px;
    }

    .s-i-col-left {
        height: inherit;
        width: 40%;
    }

    .s-i-col-right {
        height: inherit;
        width: 58%;
    }

    .s-i-base {
        height: inherit;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding: 18px 40px;
        overflow-y: auto;

        &-item {
            margin: 3px 0;
        }
        &:deep(.el-row) {
            line-height: 39px;
            border: 1px solid $og-border-color;
            border-top: none;
            border-right: none;
        }

        &:deep(.el-col) {
            padding-left: 5px;
            border-right: 1px solid $og-border-color;
        }
    }

    .s-i-execute {
        height: inherit;
        margin-top: 5px;
        border-top: 1px solid var(--el-color--col-border-color);
        overflow-y: scroll;
        .s-i-el-col {
            background-color: var(--el-color-card-header-color);
        }

        &:deep(.el-row) {
            line-height: 39px;
            border: 1px solid var(--el-color--col-border-color);
            border-top: none;
        }

        &:deep(.el-row:first-child) {
            border-right: 1px solid var(--el-color--col-border-color);
        }

        &:deep(.el-col) {
            padding-left: 5px;
            // border-top: 1px solid #4A4A4A;

        }
    }

    .s-i-statistics {
        height: inherit;
        display: flex;
        align-items: center;
        overflow-y: auto;

        &-box {
            display: flex;
            width: 80%;
        }

        &-pie {
            height: 180px;
            width: 55%;
        }

        &-legend {
            width: calc(100% - 120px);
            // height: 220px;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        &-item {
            font-size: 12px;
            display: flex;
            align-items: center;
            margin-bottom: 10px;

            &-block {
                display: inline-block;
                width: 10px;
                height: 10px;
                margin-right: 3px;
            }
        }

        &-list {
            font-size: 12px;
            width: 45%;
        }
    }

    .s-i-consuming {
        height: inherit;
        display: flex;
        align-items: center;
        padding: 0 0;
        overflow-y: auto;

        &-pie {
            height: 180px;
            width: 180px;
        }

        &-legend {
            width: calc(100% - 180px);
            height: 220px;
            display: flex;
            flex-wrap: wrap;
            padding-left: 10px;
        }

        &-item {
            font-size: 12px;
            display: flex;
            align-items: center;

            &:nth-child(1n) {
                width: 43%;
            }

            &:nth-child(2n) {
                width: 57%;
            }

            &-block {
                display: inline-block;
                width: 10px;
                height: 10px;
                margin-right: 3px;
            }
        }
    }

}
</style>
