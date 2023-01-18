<script setup lang="ts">
import { LineData } from '../../components/MyLine.vue';
import { useMonitorStore } from '../../store/monitor';
import { storeToRefs } from 'pinia';
import { toFixed } from '../../shared';
import { useWindowStore } from '../../store/window';

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
    defaultBrushArea?: string[]
}>(), {
    querys: () => [],
    names: () => [],
    nameIndexs: () => [],
    color: () => [],
    scatterOpts: () => ({}),
    lineWidth: 2,
    dataKey: 'gauss_wait_events_value',
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
const { rangeTime, time, tab, databaseData, serverData } = storeToRefs(useMonitorStore())

const handleData = () => {
    data.value = []
    scatterData.value = undefined
    const baseData = tab.value === 0 ? databaseData : serverData
    if (!baseData) {
        return
    }
    const names = props.names.length > 0 ? props.names : [props.dataKey]
    names.forEach((name, i) => {
        const _data = baseData.value[props.nameIndexs.includes(i) ? `${name}${props.nameFix}` : name]
        if (_data) {
            if (props.hasScatterData && (props.names.length - 1 === i)) {
                scatterData.value = {
                    data: _data[0]?.data.map(d => toFixed(d)),
                    name,
                    itemStyle: { color: props.color[i] },
                    ...props.scatterOpts
                }
            } else {
                _data.forEach((item, j) => {
                    data.value.push({
                        data: item.data.map(d => {
                            let n = d.includes('.') ? toFixed(d) : d
                            if (props.formatter) {
                                n = props.formatter(n)
                            }
                            return n;
                        }),
                        name: props.names.length > 0 ? name : item.name,
                        lineStyle: { color: props.color[props.legendName ? j : i], width: props.lineWidth },
                        itemStyle: { color: props.color[props.legendName ? j : i] },
                    })
                })
                if (_data.length > 0) {
                    xData.value = _data[0].time
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
/>
</template>
