<script setup lang="ts">
import moment from 'moment';
import { LineData } from '../../components/MyLine.vue';
import { useMonitorStore } from '../../store/monitor';
import { storeToRefs } from 'pinia';
import { toFixed } from '../../shared';
import { useWindowStore } from '../../store/window';
import { i18n } from '../../i18n';

const props = withDefaults(defineProps<{
    querys?: string[],
    names?: string[],
    nameFix?: string,
    nameIndexs?: number[],
    dataKey?: string,
    // chart colors
    color?: string[],
    hasScatterData?: boolean,
    formatter?: (data: string) => string
    legendName?: string
    scatterOpts?: Record<string, any>
    lineWidth?: number,
    defaultBrushArea?: string[],
    countByDataTimePicker?: boolean
}>(), {
    querys: () => [],
    names: () => [],
    nameIndexs: () => [],
    color: () => [],
    scatterOpts: () => ({}),
    lineWidth: 2,
    dataKey: 'gauss_wait_events_value',
    countByDataTimePicker: true
})

const xData = ref<string[]>([])

const data = ref<LineData[]>([])
const scatterData = ref<LineData>()

const myEmit = defineEmits<{
    (event: 'load-data', data: any[]): void
}>()

const inView = ref(false)
const load = (checkInView?: boolean, checkTab?: boolean) => {
    if (checkInView && !inView.value) {
        return
    }
    if (checkTab && tab.value !== 0) {
        return
    }
    if (rangeTime.value > 0 || (time.value && time.value.length === 2)) {
        inView.value = true
        handleData()
    }
}

const { theme } = storeToRefs(useWindowStore())
const { rangeTime, time, tab, databaseData, serverData, promethuesStart, promethuesEnd, promethuesStep } = storeToRefs(useMonitorStore())

const getXDataRange = (curData: Array<string>, t: Array<string>) => {
    const newData = [];
    const newXLabel: Array<string> = [];
    if (Array.isArray(curData) && curData.length > 0 && Array.isArray(t)) {
        let start = promethuesStart.value;
        let end = promethuesEnd.value;
        let step = promethuesStep.value;
        for (let i = start; i <= end; i += step) {
            const key = moment(i * 1000).format("YYYY-MM-DD HH:mm:ss");
            const index = t.indexOf(key);
            newXLabel.push(key);
            if (index > -1) {
                newData.push(curData[index])
            } else {
                newData.push(null)
            }
        }
    }
    return [newXLabel, newData];
}

const handleData = () => {
    data.value = [];
    scatterData.value = undefined
    const baseData = tab.value === 0 ? databaseData : serverData
    if (!baseData) {
        return
    }
    const names = props.names.length > 0 ? props.names : [props.dataKey];
    names.forEach((name, i) => {
        const _data = baseData.value[props.nameIndexs.includes(i) ? `${name}${props.nameFix}` : name]
        if (_data) {
            if (props.hasScatterData && (props.names.length - 1 === i)) {
                let curScatterData = _data[0]?.data.map(d => toFixed(d));
                const [, newData] = getXDataRange(curScatterData, _data[0].time) as [string[], string[]];
                scatterData.value = {
                    data: Array.isArray(newData) && newData.length > 0 ? newData : curScatterData,
                    name,
                    itemStyle: { color: props.color[i] },
                    ...props.scatterOpts
                }
            } else {
                _data.forEach((item, j) => {
                    const tempData = item.data.map(d => {
                        let n = d.includes('.') ? toFixed(d) : d
                        if (props.formatter) {
                            n = props.formatter(n)
                        }
                        return n;
                    });
                    let curData = tempData;
                    if (props.countByDataTimePicker) {
                        const [newXLabel, newData] = getXDataRange(tempData, _data[0].time) as [string[], string[]];
                        xData.value = newXLabel;
                        curData = newData;
                    }
                    data.value.push({
                        data: curData,
                        name: props.names.length > 0 ? name : item.name,
                        lineStyle: { color: props.color[props.legendName ? j : i], width: props.lineWidth },
                        itemStyle: { color: props.color[props.legendName ? j : i] },
                    })
                })
                if (_data.length > 0) {
                    if (!props.countByDataTimePicker) {
                        xData.value = _data[0].time
                    }
                }
            }
        }
    })
    myEmit('load-data', data.value)
}
watch(databaseData, () => { load(true, true) }, { deep: true })
watch(serverData, () => { load(true, true) }, { deep: true })
</script>

<template>
<my-line
    ref="lineRef"
    :data="data"
    :xData="xData"
    :scatterData="scatterData"
    @load="load"
    v-bind="$attrs"
    :theme="theme"
    :defaultBrushArea="defaultBrushArea"
    :countByDataTimePicker="countByDataTimePicker"
    :key="`${i18n.global.locale.value}`"
/>
</template>
