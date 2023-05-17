<script setup lang="ts">
import * as echarts from 'echarts/core'
import { TitleComponent, ToolboxComponent, TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import { LineChart } from 'echarts/charts'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { useEventListener } from '@vueuse/core'
import { uuid } from '../../shared'

// type EchartsOption = echarts.ComposeOption<GridComponentOption | TooltipComponentOption | HeatmapSeriesOption>
echarts.use([TitleComponent, ToolboxComponent, TooltipComponent, GridComponent, LegendComponent, LineChart, CanvasRenderer, UniversalTransition])
const domId = uuid()
let myChart: echarts.ECharts
const props = withDefaults(
    defineProps<{
        // xAxis Data
        xData?: string[]
        series?: []
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
        series: () => [],

        yData: () => [],
        data: () => [],
        maxData: 10,
        unit: '',
        scatterUnit: '',
        areaStyle: false,
        theme: 'dark',
    }
)
const renderChart = () => {
    const theme = localStorage.getItem('theme')
    const chartDom = document.getElementById(domId)
    if (!myChart) {
        myChart = echarts.init(chartDom!, theme === 'dark' ? 'dark' : 'auto')
    }

    let seriesCul: any[] = []
    let legends: any[] = []
    props.series.forEach((element) => {
        Object.assign(element, {
            type: 'line',
            stack: 'Total',
            areaStyle: {},
            emphasis: {
                focus: 'series',
            },
        })
        seriesCul.push(element)
    })
    seriesCul.forEach((element) => {
        legends.push(element.name)
    })

    let option = {
        backgroundColor: theme === 'dark' ? '#232324' : '#ffffff',
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
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985',
                },
            },
            position:  function (pos, params, dom, rect, size) {
                // 鼠标在左侧时 tooltip 显示到右侧，鼠标在右侧时 tooltip 显示到左侧。
                let obj = {top: 10};
                obj[['left', 'right'][+(pos[0] < size.viewSize[0] / 2)]] = 10;
                return obj;
            },
           
            
        },
        legend: {
            type: 'scroll',
            top: 30,
            data: legends,
        },
        toolbox: {
            feature: {
                saveAsImage: {},
            },
        },
        grid: {
            top: 60,
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true,
        },
        xAxis: [
            {
                type: 'category',
                boundaryGap: false,
                axisLabel: {
                    color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
                },
                data: props.xData,
            },
        ],
        yAxis: [
            {
                type: 'value',
                axisLabel: {
                    color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
                },
            },
        ],
        series: seriesCul,
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
