<template>
  <div ref="main" style="width: 100%; height: 100%;"></div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import * as echarts from "echarts"
const main = ref()
const props = withDefaults(
    defineProps<{
        datas?: any[],
        title: string,
        unit: string,
    }>(),
    {
        title: '',
        unit: ''
    }
);
watch(() => props.datas, (datas) => {
    if (datas && datas.length > 0) {
        init()
    }
}, { deep: true })

watch(() => props.title, (title) => {
    
})
watch(() => props.unit, (unit) => {
    
})

const init = () => {
    let series = props.datas.map(item => {
        if (item.metric) {
            item.metric.instance = undefined
            item.metric.instanceId = undefined
            item.metric.host = undefined
            item.metric.type = undefined
            item.metric.job = undefined
            item.metric.minor = undefined
            item.metric.major = undefined
        }
        let name = JSON.stringify(item.metric)
        let dataArr = item.values
        return {
            name: name,
            type: 'line',
            showSymbol: true,
            symbol: 'circle',
            symbolSize: 4,
            hoverAnimation: false,
            labelLine: {
                show: true,
                smooth: true
            },
            data: dataArr
        }
    });
    const theme = localStorage.getItem('theme');
    let chart = echarts.init(main.value)
    const option = {
        title: {
            show: true,
            text: props.title,
            left: '40%',
            bottom: 'bottom',
            textStyle: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a'
            }
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            left: 'left',
            type: 'scroll',
            textStyle: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a'
            }
        },
        xAxis: {
            type: 'time',
            splitLine: {
                show: true
            }
        },
        yAxis: {
            name: props.unit,
            type: 'value',
            splitLine: {
                show: true
            }
        },
        series: series
    }
    chart.setOption(option)
}

onMounted(() => {
    init()
})
</script>
<style scoped lang='scss'>
</style>