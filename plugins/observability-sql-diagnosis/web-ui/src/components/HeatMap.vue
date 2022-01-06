<script setup lang="ts">
import * as echarts from 'echarts/core'
import { GridComponent, GridComponentOption, TooltipComponent, TooltipComponentOption, LegendComponent, TitleComponent, ToolboxComponent, VisualMapComponent } from 'echarts/components'
import { HeatmapChart, HeatmapSeriesOption } from 'echarts/charts'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { useEventListener } from '@vueuse/core'
import { uuid } from '../shared'

type EchartsOption = echarts.ComposeOption<GridComponentOption | TooltipComponentOption | HeatmapSeriesOption>
echarts.use([CanvasRenderer, UniversalTransition, GridComponent, TooltipComponent, ToolboxComponent, TitleComponent, HeatmapChart, VisualMapComponent, LegendComponent])
const domId = uuid()
let myChart: echarts.ECharts
const props = withDefaults(
    defineProps<{
        // xAxis Data
        xData?: string[]
        yData?: string[]
        // yAxis Line Data
        data?: []
        maxData?: number
        showNum?: boolean
        title?: string
        unit?: string
        scatterUnit?: string
        // yAxis Scatter Data
        // LineChart use areaStyle
        areaStyle?: boolean
        // chart colors
        color?: string[]
        theme?: 'dark' | 'light'
    }>(),
    {
        xData: () => [],
        yData: () => [],
        data: () => [],
        maxData: 10,
        unit: '',
        scatterUnit: '',
        areaStyle: false,
    }
)
const renderChart = () => {
    const chartDom = document.getElementById(domId)
    if (!myChart) {
        myChart = echarts.init(chartDom!)
    }

    const theme = localStorage.getItem('theme')
    let option: EchartsOption = {}
    option = {
        tooltip: { position: 'top' },
        title: {
            top: 0,
            left: 'center',
            text: props.title,
            textStyle: {
                fontSize: 14,
                fontWeight: 'bold',
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a', // font color
            },
        },
        grid: {
            top: '50px',
            bottom: '60px',
            containLabel: true, // label on the left
        },
        xAxis: {
            type: 'category',
            data: props.xData,
            splitArea: { show: true },
            axisTick: { show: false }, // add
            boundaryGap: true, // add
            axisLabel: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
            },
        },
        yAxis: {
            type: 'category',
            data: props.yData,
            splitArea: { show: true },
            name: props.unit,
            axisLabel: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
            },
            splitLine: {
                lineStyle: { color: theme === 'dark' ? '#d4d4d4' : '#1d212a' },
            },
        },
        visualMap: {
            min: 0,
            max: props.maxData,
            calculable: true,
            orient: 'horizontal',
            left: 'center',
            bottom: '0%',
            textStyle: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a', // visualMap scroll color
            },
        },
        series: [
            {
                name: 'Punch Card',
                type: 'heatmap',
                data: props.data,
                label: {
                    show: props.showNum,
                },
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowColor: 'rgba(0, 0, 0, 0.5)',
                    },
                },
            },
        ],
    }
    if (theme === 'dark2') {
        option = {
            title: {
                top: 0,
                left: 'center',
                text: props.title,
                textStyle: {
                    color: '#1d212a',
                },
            },
            grid: {
                height: '50%',
                top: '10%',
                containLabel: true,
            },
            tooltip: {
                position: 'top',
            },
            visualMap: {
                min: 0,
                max: props.maxData,
                calculable: true,
                orient: 'horizontal',
                left: 'center',
                bottom: '15%',
                textStyle: {
                    color: '#1d212a',
                },
            },
            xAxis: {
                type: 'category',
                boundaryGap: true,
                axisTick: { show: false },
                splitArea: { show: true },
                axisLine: {
                    lineStyle: {
                        color: props.theme === 'dark' ? '#D4D4D4' : '#1d212a',
                    },
                },
                data: props.xData,
            },
            yAxis: {
                type: 'category',
                name: props.unit,
                splitArea: { show: true },
                axisLabel: {
                    color: props.theme === 'dark' ? '#d4d4d4' : '#1d212a',
                },
                splitLine: {
                    lineStyle: { color: props.theme === 'dark' ? '#4a4a4a' : '#1d212a' },
                },
                data: props.yData,
            },
            series: [
                {
                    name: props.title,
                    type: 'heatmap',
                    data: props.data,
                    label: {
                        color: props.theme === 'dark' ? '#d4d4d4' : '#1d212a',
                        show: props.showNum,
                    },
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowColor: 'rgba(0, 0, 0, 0.5)',
                        },
                    },
                },
            ],
        }
    }
    if (theme === 'dark3') {
        option = {
            title: {
                top: 0,
                left: 'center',
                text: props.title,
                textStyle: {
                    color: '#ffffff',
                },
            },
            color: props.color,
            grid: {
                height: '50%',
                top: '10%',
                containLabel: true,
            },
            tooltip: {
                position: 'top',
            },
            visualMap: {
                min: 0,
                max: props.maxData,
                calculable: true,
                orient: 'horizontal',
                left: 'center',
                bottom: '15%',
                textStyle: {
                    color: '#d4d4d4',
                },
            },
            xAxis: {
                type: 'category',
                boundaryGap: true,
                axisTick: { show: false },
                splitArea: { show: true },
                axisLine: {
                    lineStyle: {
                        color: props.theme === 'dark' ? '#D4D4D4' : '#e0e0e0',
                    },
                },
                data: props.xData,
            },
            yAxis: {
                type: 'category',
                name: props.unit,
                splitArea: { show: true },
                axisLabel: {
                    color: props.theme === 'dark' ? '#d4d4d4' : '#333',
                },
                splitLine: {
                    lineStyle: { color: props.theme === 'dark' ? '#4a4a4a' : '#333' },
                },
                data: props.yData,
            },
            series: [
                {
                    name: props.title,
                    type: 'heatmap',
                    data: props.data,
                    label: {
                        show: props.showNum,
                    },
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowColor: 'rgba(0, 0, 0, 0.5)',
                        },
                    },
                },
            ],
        }
    }
    myChart.setOption(option)
}
onMounted(() => {
    renderChart()
})

watch(
    () => props.data,
    () => {
        nextTick(renderChart)
    },
    { deep: true }
)

useEventListener(window, 'resize', () => {
    myChart?.resize()
})
</script>

<template>
    <div :id="domId" ref="loadRef" style="width: 100%; height: 100%"></div>
</template>
