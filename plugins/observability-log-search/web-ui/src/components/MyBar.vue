<script setup lang="ts">
import * as echarts from 'echarts/core';
import { GridComponent, GridComponentOption, TooltipComponent, TooltipComponentOption, LegendComponent, TitleComponent, ToolboxComponent, VisualMapComponent } from 'echarts/components';
import { BarSeriesOption, BarChart } from 'echarts/charts';
import { UniversalTransition } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { useEventListener } from '@vueuse/core';
import { uuid } from '../shared';
import { YAXisOption } from 'echarts/types/dist/shared';
import { OptionDataValue } from 'echarts/types/src/util/types';

export type LineData = {
    name: string;
    nameGap?: number;
    data: any[];
    unit?: string; // 单位
    min?: number;
    max?: number;
    interval?: number;
    stack: string;
};
type EchartsOption = echarts.ComposeOption<GridComponentOption | TooltipComponentOption | BarSeriesOption>;
echarts.use([CanvasRenderer, UniversalTransition, GridComponent, TooltipComponent, ToolboxComponent, TitleComponent, BarChart, VisualMapComponent, LegendComponent]);
const domId = uuid();
let myChart: echarts.ECharts;
const props = withDefaults(
    defineProps<{
        // xAxis Data
        xData?: string[];
        yData?: string[];
        // yAxis Line Data
        data?: LineData[];
        maxData?: number;
        showNum?: boolean;
        title?: string;
        unit?: string;
        yname?: string;
        scatterUnit?: string;
        // yAxis Scatter Data
        // LineChart use areaStyle
        areaStyle?: boolean;
        // chart colors
        color?: string[];
        theme?: 'dark' | 'light';
    }>(),
    {
        xData: () => [],
        yData: () => [],
        data: () => [],
        maxData: 10,
        unit: '',
        yname: '',
        scatterUnit: '',
        areaStyle: false,
        color: () => ['#37D4D1', '#00C7F9', '#0D86E2', '#425ADD', '#E64A19', '#9CCC65', '#A97526', '#2830FF', '#8B00E1', '#0F866A'],
        theme: 'dark',
    }
);
const renderChart = () => {
    const theme = localStorage.getItem('theme');
    let seriesData: Array<BarSeriesOption> = [];
    let yAxisData: Array<YAXisOption> = [];
    props.data.forEach(({ name, data, unit, interval, stack, ...item }) => {
        let obj: BarSeriesOption = {
            type: 'bar',
            stack,
            name,
            data,
            // barWidth:60,
            emphasis: {
                focus: 'series',
            },
            tooltip: {},
            ...item,
        };
        seriesData.push(obj);
        yAxisData.push({
            type: 'value',
            name: props.yname,
            axisLabel: {
                formatter: `{value}`,
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
            },
            nameTextStyle: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
            },
            splitLine: {
                lineStyle: {
                    color: theme === 'dark' ? '#888888' : '#1d212a',
                },
            },
        });
    });
    const chartDom = document.getElementById(domId);
    if (!myChart) {
        myChart = echarts.init(chartDom!);
    }

    const option: EchartsOption = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow',
            },
        },
        legend: {
            textStyle: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a', // font color
            },
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true,
        },
        xAxis: {
            type: 'category',
            axisLine: {
                lineStyle: {
                    color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
                },
            },
            data: props.xData,
        },
        yAxis: yAxisData,
        series: seriesData,
    };
    console.log('option', JSON.stringify(option));
    myChart.setOption(option);
};
onMounted(() => {
    renderChart();
});

watch(
    () => props.data,
    () => {
        nextTick(renderChart);
    },
    { deep: true }
);

useEventListener(window, 'resize', () => {
    myChart?.resize();
});
</script>

<template>
    <div :id="domId" ref="loadRef" style="width: 100%; height: 100%"></div>
</template>
