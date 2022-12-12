<script setup lang="ts">
import moment from 'moment';
import LazyLine from '../../dashboard/LazyLine.vue';
import { toFixed } from '../../../shared';
import { i18n } from '../../../i18n';
import { getDatabaseMetrics } from '../../../api/prometheus';

const props = withDefaults(defineProps<{
    fixedRangeTime?: string[],
}>(), {
    fixedRangeTime: () => [],
})

const dealTime = (value: string) => {
    return moment(value).format('HH:mm:ss')
}

const getRangeTime = () => {
    return `${dealTime(props.fixedRangeTime[0])} ~ ${dealTime(props.fixedRangeTime[1])}`
}

onMounted(() => {
    getDatabaseMetrics()
});

</script>

<template>
    <div class="system-source">
        <my-message
            type="info"
            :tip="`${$t('dashboard.rangeTimeTip')}（${getRangeTime()}）`"
            style="margin-bottom: 8px;"
            :key="i18n.global.locale.value"
        />
        <div class="s-i-row">
            <div class="s-i-col">
                <my-card :title="$t('dashboard.cpuUseSituation')" height="255" :legend="[
                    {color: '#9CCC65', name: $t('metric.totalCoreNum')},
                    {color: '#00C7F9', name: $t('metric.totalAverageUtilization')}
                ]" :bodyPadding="false">
                    <div class="linename">
                        <div>{{ $t('metric.totalCoreNum') }}</div>
                        <div id="system_source_0" style="height: 100%;">
                            <LazyLine
                                :color="['#9CCC65', '#00C7F9']"
                                :names="['totalCoreNum', 'totalAverageUtilization']"
                                :name-indexs="[1]"
                                name-fix="1"
                                hasScatterData
                                scatterUnit="%"
                                :scatterOpts="{type: 'line', symbol: 'none'}"
                                :defaultBrushArea="props.fixedRangeTime"
                            />
                        </div>
                        <div>{{ $t('metric.totalAverageUtilization') }}</div>
                    </div>
                </my-card>
            </div>
            <div class="s-i-col">
                <my-card :title="$t('dashboard.memoryUsage')" height="255" :legend="[
                    {color: '#00C7F9', name: $t('metric.totalAverageUtilization')}
                ]" :bodyPadding="false">
                    <div id="system_source_1" style="height: 100%;">
                        <LazyLine
                            :color="['#00C7F9']"
                            :names="['totalAverageUtilization']"
                            :name-indexs="[0]"
                            name-fix="2"
                            unit="%"
                            :defaultBrushArea="props.fixedRangeTime"
                        />
                    </div>
                </my-card>
            </div>
        </div>
        <div class="s-i-row">
            <div class="s-i-col">
                <my-card :title="$t('dashboard.networkTransmissionRate')" height="255" :legend="[
                    {color: '#00C7F9', name: $t('metric.upload')},
                    {color: '#37D4D1', name: $t('metric.download')}
                ]" :bodyPadding="false">
                    <div id="system_source_2" style="height: 100%;">
                        <LazyLine
                            :color="['#00C7F9', '#37D4D1']"
                            :names="['upload', 'download']"
                            unit="MB/s"
                            :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1048576))"
                            :defaultBrushArea="props.fixedRangeTime"
                        />
                    </div>
                </my-card>
            </div>
            <div class="s-i-col">
                <my-card :title="$t('dashboard.diskReadAndWriteRate2')" height="255" :legend="[
                    {color: '#00C7F9', name: $t('metric.read')},
                    {color: '#37D4D1', name: $t('metric.write')}
                ]" :bodyPadding="false">
                    <div id="system_source_3" style="height: 100%;">
                        <LazyLine
                            :color="['#00C7F9', '#37D4D1']"
                            :names="['read', 'write']"
                            :name-indexs="[0, 1]"
                            name-fix="2"
                            :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1024))"
                            unit="KB/s"
                            :defaultBrushArea="props.fixedRangeTime"
                        />
                    </div>
                </my-card>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.linename {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    position: relative;
    justify-content: center;
    > div:nth-of-type(2) {
        width: calc(100% - 60px);
        height: 100%;
        margin: 0 10px;
    }
    > div:nth-of-type(1), > div:nth-of-type(3) {
        color: var(--el-color-line-text-color);
        font-size: 12px;
        text-align: center;
        position: absolute;
        width: 200px;
        height: 15px;
    }
    > div:nth-of-type(1) {
        transform: rotate(-90deg);
        left: -80px;
    }
    > div:nth-of-type(3) {
        transform: rotate(90deg);
        right: -80px;
    }
}
.system-source {

    .s-i-row {
        width: 100%;
        display: flex;
        justify-content: space-between;
        margin-bottom: 18px;
    }

    .s-i-col {
        height: inherit;
        width: 49%;
        position: relative;
    }
}
</style>
