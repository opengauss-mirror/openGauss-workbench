<template>
    <div :id="domId" style="width: 100%;height: 100%;"></div>
</template>

<script lang="ts" setup>
import { uuid } from "@/shared";
import * as echarts from "echarts/core";
import { GridComponent, GridComponentOption, TooltipComponent, TooltipComponentOption, LegendComponent, TitleComponent, ToolboxComponent } from "echarts/components";
import { useEventListener } from "@vueuse/core";
import { LineChart, LineSeriesOption, BarSeriesOption, BarChart } from "echarts/charts";
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from "echarts/renderers";
import { OptionDataValue } from 'echarts/types/src/util/types'
import { YAXisOption } from "echarts/types/dist/shared";
    
export type LineData = {
        name: string,
        nameGap?: number,
        data: any[],
        unit?: string, // unit
        type: 'line' | 'bar',
        min?: number,
        max?: number,
        interval?: number,
        barWidth?:number,
        yAxisIndex?:number,
    }
    
    type EchartsOption = echarts.ComposeOption<
        GridComponentOption | TooltipComponentOption | LineSeriesOption | BarSeriesOption
    >
    
echarts.use([
    CanvasRenderer,
    LineChart,
    BarChart,
    UniversalTransition,
    GridComponent,
    TooltipComponent,
    LegendComponent,
    TitleComponent,
    ToolboxComponent
]);
    
const props = withDefaults(defineProps<{
        // xAxis Data
        xData?: string[],
        // yAxis Bar Data | Line Data
        data: LineData[],
        // yAxis Line Data
        lineData?: LineData[],
        // yAxis Bar Data
        barData?: LineData[],
        // LineChart use areaStyle
        areaStyle?: boolean,
        // chart colors
        color?: string[],
        // yAxis Line Name
        lineName?: string,
        // yAxis Line NameGap
        lineNameGap?: number,
    }>(), {
    xData: () => ['10:00', '11:00', '12:00', '13:00'],
    data: () => [],
    areaStyle: false,
    color: () => ['#37D4D1', '#00C7F9', '#0D86E2', '#425ADD', '#E64A19', '#9CCC65', '#A97526', '#2830FF', '#8B00E1', '#0F866A'],
})
    
const domId = uuid();
let myChart: echarts.ECharts;
const renderChart = () => {
    let seriesData: Array<LineSeriesOption | BarSeriesOption> = [];
    let yAxisData: Array<YAXisOption> = [];
    props.data.forEach(({ name, type, data, unit, min, max, interval, ...item }) => {
        let obj: LineSeriesOption | BarSeriesOption = {
            name,
            data,
            type,
            tooltip: {
                valueFormatter: function (value: OptionDataValue | OptionDataValue[]) {
                    return value + ` ${unit}`;
                }
            },
            ...item
        }
        if (type === 'line') obj.yAxisIndex = 1
        seriesData.push(obj)
        yAxisData.push({
            type: 'value',
            name,
            min,
            max,
            interval,
            axisLabel: {
                formatter: `{value} ${unit}`,
                color: '#d4d4d4'
            },
            nameTextStyle: {
                color: '#d4d4d4'
            },
            splitLine: {
                lineStyle: {
                    color: '#888888'
                }
            },
        })
    })
    const chartDom = document.getElementById(domId);
    myChart = echarts.init(chartDom!);
    const option: EchartsOption = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                crossStyle: {
                    color: '#999'
                }
            }
        },
        toolbox: {
            left: '55%',
            feature: {
                dataView: { show: true, readOnly: false, title: '数据视图' },
                magicType: { show: true, type: ['line', 'bar'], title: { line: '切换为折线图', bar: '切换为柱状图' } },
                restore: { show: true, title: '还原' },
                saveAsImage: { show: true, title: '下载图片' }
            },
            iconStyle: {
                borderColor: '#d4d4d4',
                shadowColor: 'orange'
            }
        },
        legend: {
            left: '30%',
            data: [{
                name: 'Slow SQL',
                textStyle: {
                    color: '#d4d4d4'
                }
            }, {
                name: 'cpu',
                textStyle: {
                    color: '#d4d4d4'
                }
            }]
        },
        xAxis: {
            type: 'category',
            axisTick: { show: false },
            axisLine: {
                lineStyle: {
                    color: '#d4d4d4'
                }
            },
            axisPointer: {
                type: 'shadow'
            },
            data: props.xData,
        },   
        yAxis: yAxisData,
        // series: props.data
        series: seriesData
    }
    myChart.setOption(option);
}
watch(() => props.data, () => {
    nextTick(renderChart)
}, { deep: true })
useEventListener(window, "resize", () => {
    myChart?.resize();
});
    
</script>