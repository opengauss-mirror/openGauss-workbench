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
        barWidth: string,
        barHeight: string,
        finallyTime: string
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
        barWidth: '100%',
        barHeight: '100%',
        finallyTime: ''
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
            tooltip: {confine: true},
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

    const time = props.finallyTime
    const xData2 = props.xData.concat(time)

    const option: EchartsOption = {
        tooltip: {
            formatter(item) {
              let htmlStr = '<div style="height: auto;max-height: 300px;overflow-y: scroll;border-radius: 4px;">'
              htmlStr += '<div style="display: flex;flex-direction: row;align-items:center;font-size:12px">' +
                xData2[item[0].dataIndex] + '~' + xData2[item[0].dataIndex + 1] + '</div>';
              for (let i = 0; i < item.length; i++) {
                if(item[i].value){
                  htmlStr +=
                    '<div style="display: flex;flex-direction: row;align-items:center;font-size:12px">' +
                    '<div style="display: inline-block; width: 14px; height: 4px;border-radius: 1px;margin-right:8px;background-color: ' +
                    item[i].color +
                    ';"></div>' +
                    '<div style="flex-grow:1;padding-right:12px">' +
                    item[i].seriesName +
                    '</div>' +
                    '<div style="min-width: 50px; text-align: right;">' +
                    item[i].value +
                    (props.unit ? props.unit : '') +
                    '</div>' +
                    '</div>'
                }
              }
              htmlStr += '</div>'
              return htmlStr;
            },
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
        xAxis: [{
            data: props.xData, show: false
        },{
          type: 'category',
          axisLine: {
            lineStyle: {
              color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
            },
          },
          data: xData2,
          position: 'bottom',
          boundaryGap: false,
          axisPointer: { show: false },
        }],
        yAxis: yAxisData,
        series: seriesData,
    };
    myChart.setOption(option);
};
const themeChange = ref<any>();
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
    <div :id="domId" ref="loadRef" :style="`width: ${barWidth}; height: ${barHeight}`"></div>
</template>
