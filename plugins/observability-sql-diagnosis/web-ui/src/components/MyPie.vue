<script setup lang="ts">
import * as echarts from 'echarts/core';
import {
    TooltipComponent,
    TooltipComponentOption,
    LegendComponent,
    LegendComponentOption,
    GridComponentOption,
    TitleComponent,
    TitleComponentOption
} from 'echarts/components';
import { PieChart, PieSeriesOption } from 'echarts/charts';
import { LabelLayout } from 'echarts/features';
import { CanvasRenderer } from 'echarts/renderers';
import { uuid } from '../shared';
import { useDebounceFn, useEventListener, useIntersectionObserver } from '@vueuse/core';

echarts.use([
    TooltipComponent,
    LegendComponent,
    PieChart,
    CanvasRenderer,
    LabelLayout,
    TitleComponent
]);

const colorArray: Array<string> = [
    '#37D4D1',
    '#00C7F9',
    '#0D86E2',
    '#425ADD',
    '#8B00E1',
    '#9CCC65',
    '#A97526',
    '#2830FF',
    '#E64A19',
    '#0F866A',
    '#5CDF73',
    '#FEEC21'
]

type EChartsOption = echarts.ComposeOption<
  TooltipComponentOption | LegendComponentOption | PieSeriesOption | GridComponentOption | TitleComponentOption
>;

const props = withDefaults(
    defineProps<{
        smooth?: boolean
        areaColor?: echarts.graphic.LinearGradient,
        radius?: [string, string]
        center?: [string, string]
        data: {value: number, name: string}[],
        text?: string,
        height?: string,
        showLegend?: boolean,
        color?: Array<string>
    }>(),
    {
        smooth: true,
        radius: () => ['50%', '70%'],
        center: () => ['30%', '50%'],
        data: () => [],
        text: '总数',
        height: '100px',
        showLegend: true
    }
);

const domId = uuid()
const percent2num = (percent: string) => Number.parseFloat(percent) / 100
const position = reactive({ left: '0', top: '0', lineLength: '0' })
const calcPosition = (chartDom: HTMLElement) => {
    const cs = getComputedStyle(chartDom.parentNode as Element)
    const paddingLeft = Number.parseFloat(cs.getPropertyValue('padding-left'))
    const paddingTop = Number.parseFloat(cs.getPropertyValue('padding-top'))
    const base = chartDom.offsetWidth > chartDom.offsetHeight ? chartDom.offsetHeight : chartDom.offsetWidth
    const radius = base * percent2num(props.radius[1]) * 0.5
    position.left = chartDom.offsetWidth * percent2num(props.center[0]) - radius + paddingLeft + 'px'
    position.top = chartDom.offsetHeight * percent2num(props.center[1]) - radius + paddingTop + 'px'
    position.lineLength = radius * 2 + 'px'
}
const renderChart = () => {
    const chartDom = document.getElementById(domId);
    if (chartDom) {
        const instance = echarts.getInstanceByDom(chartDom)
        instance && instance.dispose()
    }
    const myChart = echarts.init(chartDom!);
    const option: EChartsOption = {
        legend: {
            show: props.showLegend,
            icon: "rect",
            type: "scroll",
            pageIconSize: 10,
            pageTextStyle: {
                fontSize: 10,
            },
            textStyle: {
                color: '#D4D4D4'
            },
            itemHeight: 10,
            itemGap: 10,
            orient: 'vertical',
            selectedMode: false,
            right: '10%',
            bottom: '5%',
        },
        color: props.color ? props.color : colorArray,
        series: [
            {
                name: 'dbtypes',
                type: 'pie',
                radius: props.radius,
                center: props.center,
                avoidLabelOverlap: true,
                label: {
                    show: false
                },
                itemStyle: {
                    borderColor: '#4A4A4A',
                    borderWidth: 1
                },
                minAngle: 10,
                data: props.data
            }
        ]
    };
    myChart.setOption(option);
}
// lazy load
const loadRef = ref<HTMLDivElement>()
const { stop } = useIntersectionObserver(
    loadRef,
    ([{ isIntersecting }]) => {
        if (isIntersecting) {
            nextTick(renderChart)
            stop()
        }
    }
)
watch(() => props.data, () => {
    nextTick(renderChart)
}, { deep: true })

useEventListener(window, 'resize', useDebounceFn(() => {
    const chartDom = document.getElementById(domId);
    if (chartDom) {
        const instance = echarts.getInstanceByDom(chartDom)
        instance && instance.resize()
        calcPosition(chartDom)
    }
}, 500))
</script>

<template>
<div ref="loadRef" :id="domId" style="width: 100%; height: 100%;position: relative;"></div>
<!-- <div class="text">
    <div style="font-size: 20px;">{{ props.data.reduce((acc, cur) => acc + cur.value, 0) }}</div>
    <div style="font-size: 12px;">{{ props.text }}</div>
</div> -->
</template>

<style scoped>
.text {
    position: relative;
    left: v-bind('position.left');
    top: v-bind('position.top');
    width: v-bind('position.lineLength');
    height: v-bind('position.lineLength');
    text-align: center;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
</style>
