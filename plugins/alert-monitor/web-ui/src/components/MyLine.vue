<template>
    <div :id="domId" ref="loadRef" style="width: 100%; height: 100%;"></div>
</template>

<script setup lang="ts">
import * as echarts from "echarts/core";
import {
    GridComponent,
    GridComponentOption,
    TooltipComponent,
    TooltipComponentOption,
    ToolboxComponent,
    ToolboxComponentOption,
    BrushComponent,
    BrushComponentOption,
    TitleComponent,
    TitleComponentOption,
    DataZoomComponent,
    DataZoomComponentOption,
} from "echarts/components";
import { LineChart, LineSeriesOption, ScatterChart, ScatterSeriesOption, BarChart, BarSeriesOption } from "echarts/charts";
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from "echarts/renderers";
import { uuid } from "@/shared";
import { useEventListener, useIntersectionObserver } from "@vueuse/core";
import moment from "moment";
import { useI18n } from 'vue-i18n';
import { useDataZoom } from "@/hooks/echarts";
import { storeToRefs } from "pinia";
import { useMonitorStore } from "@/store/monitor";

export interface LineData {
    name: string,
    data: any[],
    [other: string]: any,
}

type EchartsOption = echarts.ComposeOption<
    GridComponentOption | TooltipComponentOption | LineSeriesOption | ScatterSeriesOption |
    ToolboxComponentOption | BrushComponentOption | TitleComponentOption | BarSeriesOption |
    DataZoomComponentOption
>
echarts.use([
    CanvasRenderer,
    LineChart,
    UniversalTransition,
    GridComponent,
    TooltipComponent,
    ScatterChart,
    ToolboxComponent,
    BrushComponent,
    TitleComponent,
    BarChart,
    DataZoomComponent,
]);

const { t } = useI18n();

const props = withDefaults(defineProps<{
    // xAxis Data
    xData?: string[],
    // yAxis Line Data
    data?: LineData[],
    unit?: string,
    scatterUnit?: string,
    // yAxis Scatter Data
    scatterData?: LineData,
    // LineChart use areaStyle
    areaStyle?: boolean,
    // chart colors
    color?: string[],
    theme?: 'dark' | 'light',
    /**
     * enable brush for select range time, the value is uuid
     */
    brush?: string,
    /**
     * brush default select range
     */
    defaultBrushArea?: string[],
    /**
     * bar chart
     */
    bar?: boolean,
    stack?: boolean,
    enterable?: boolean,
    translate?: boolean,
    countByDataTimePicker: boolean,

}>(), {
    xData: () => [],
    data: () => [],
    unit: '',
    scatterUnit: '',
    areaStyle: false,
    color: () => ['#37D4D1', '#00C7F9', '#0D86E2', '#425ADD', '#E64A19', '#9CCC65', '#A97526', '#2830FF', '#8B00E1', '#0F866A'],
    theme: 'dark',
    translate: true,
    countByDataTimePicker: true,
})
const { brushRange, tab } = storeToRefs(useMonitorStore())
const domId = uuid();
let myChart: echarts.ECharts
const renderChart = () => {
    let data: Array<LineSeriesOption | ScatterSeriesOption | BarSeriesOption> = [];
    props.data.forEach(d => {
        const o: Record<string, any> = {
            type: 'line',
            symbol: "none",
            yAxisIndex: 0,
            zlevel: 1,
            areaStyle: props.areaStyle ? {} : undefined,
            ...d,
        }
        if (props.bar) {
            o.type = 'bar'
            o['stack'] = 'total'
            o['barMaxWidth'] = 12
        }
        if (props.stack) {
            o['stack'] = 'total'
        }
        data.push(o)
    })
    if (props.scatterData) {
        data.push({
            type: 'scatter',
            symbol: "circle",
            symbolSize: 6,
            yAxisIndex: 1,
            zlevel: 1,
            ...props.scatterData,
        })
    }
    const chartDom = document.getElementById(domId);
    if (!myChart || myChart.isDisposed()) {
        myChart = echarts.init(chartDom!);
    }
    const option: EchartsOption = {
        title: {
            show: props.data?.length === 0 && (!props.scatterData || props.scatterData?.data?.length === 0),
            text: t('sql.isHasData'),
            left: 'center',
            top: 'center',
            textStyle: {
                color: props.theme === 'dark' ? '#ddd' : '#AEB3BC',
            }
        },
        color: props.color,
        grid: {
            left: 15,
            right: 15,
            top: 25,
            bottom: 10,
            containLabel: true,
        },
        tooltip: {
            trigger: 'axis',
            confine: true,
            enterable: props.enterable,
            backgroundColor: '#212121',
            borderColor: '#4a4a4a',
            formatter: params => {
                let str = `<div style="${props.enterable ? 'max-height: 155px;' : ''}overflow: auto;color: #D4D4D4">`;
                if (Array.isArray(params)) {
                    params.forEach((p, i) => {
                        const seriesName = props.translate ? t(`metric.${p.seriesName}`) : p.seriesName;
                        if (i === 0) {
                            str += p.name
                        }
                        str += `<div style="display: flex;justify-content: space-between;"><div style="margin-right: 24px;">${p.marker} ${seriesName} </div>${p.data}</div>`
                    })
                } else {
                    const seriesName = props.translate ? t(`metric.${params.seriesName}`) : params.seriesName;
                    str += `${params.name}<div style="display: flex;justify-content: space-between;"><div style="margin-right: 24px;">${params.marker} ${seriesName}</div>${params.data}</div>`
                }
                str += '</div>'
                return str
            }
        },
        toolbox: {
            right: -1000,
            feature: {
                dataZoom: { yAxisIndex: false },
                brush: { show: false }
            }
        },
        brush: {
            brushStyle: {
                borderWidth: 0,
                color: props.theme === 'dark' ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.2)',
            },
            transformable: !props.defaultBrushArea,
            outOfBrush: props.scatterData && props.defaultBrushArea && props.xData.length > 0
                ? {
                    color: props.scatterData.itemStyle.color
                }
                : undefined
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            axisTick: { show: false },
            axisLine: {
                lineStyle: {
                    color: props.theme === 'dark' ? '#D4D4D4' : '#e0e0e0'
                }
            },
            axisLabel: {
                color: props.theme === 'dark' ? '#D4D4D4' : '#868F9C',
                formatter: v => moment(new Date(v)).format("HH:mm")
            },
            data: props.xData,
        },
        yAxis: props.scatterData
            ? [
                {
                    type: 'value',
                    axisLabel: {
                        color: props.theme === 'dark' ? '#D4D4D4' : '#868F9C',
                        formatter: `{value}${props.unit}`
                    },
                    splitLine: {
                        lineStyle: { color: props.theme === 'dark' ? '#4a4a4a' : '#e0e0e0' }
                    }
                },
                {
                    type: 'value',
                    axisLabel: {
                        color: props.theme === 'dark' ? '#D4D4D4' : '#868F9C',
                        formatter: `{value}${props.scatterUnit}`
                    },
                    splitLine: { show: false }
                }
            ]
            : {
                type: 'value',
                axisLabel: {
                    color: props.theme === 'dark' ? '#D4D4D4' : '#868F9C',
                    formatter: `{value}${props.unit}`
                },
                splitLine: {
                    lineStyle: { color: props.theme === 'dark' ? '#4a4a4a' : '#e0e0e0' }
                }
            },
        series: data.length > 0 ? data : []
    }
    myChart.setOption(option, true)
    if (!props.countByDataTimePicker && Array.isArray(props.defaultBrushArea) && props.defaultBrushArea.length === 2 && props.xData.length > 0) {
        console.log("defaultBrushArea", props.defaultBrushArea);
        nextTick(() => {
            let interval = (moment(props.defaultBrushArea![1]).valueOf() - moment(props.defaultBrushArea![0]).valueOf()) / 1000;
            const startIndexPosi = myChart.convertToPixel({ xAxisIndex: 0 }, 0);
            const endIndexPosi = myChart.convertToPixel({ xAxisIndex: 0 }, props.xData.length - 1);
            let range: any[] = [];
            if (interval <= 120) {
                const startIndex = props.xData?.length === 3 ? 1 : props.xData.indexOf(props.defaultBrushArea![0])
                const r = myChart.convertToPixel({ xAxisIndex: 0 }, startIndex)
                const allWidth = endIndexPosi - startIndexPosi;
                const width = interval / 120 * allWidth;
                const start = Math.round(r - width / 2);
                const end = Math.round(r + width / 2);
                range = [start, end]
            } else {
                range = [startIndexPosi, endIndexPosi]
            }
            myChart.dispatchAction({
                type: 'brush',
                areas: [
                    {
                        brushType: 'lineX',
                        range,
                    }
                ]
            })
        })
    }
    if (props.brush) {
        useDataZoom(myChart)
    }
    if (brushRange.value.length > 0 && tab.value === 0) {
        myChart.dispatchAction({ type: 'dataZoom', start: 0, end: 100 })
    }
}
// lazy load
const myEmit = defineEmits<{
    (event: 'load'): void,
}>()
const loadRef = ref<HTMLDivElement>()
const { stop } = useIntersectionObserver(
    loadRef,
    ([{ isIntersecting }]) => {
        if (isIntersecting) {
            myEmit('load')
            nextTick(renderChart)
            stop()
        }
    }
)
watch(() => props.data, () => {
    nextTick(renderChart)
}, { deep: true })
useEventListener(window, "resize", () => {
    myChart?.resize();
});
</script>