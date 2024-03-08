<template>
    <div ref="main" style="width: 100%; height: 125px;"></div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import * as echarts from "echarts"
const main = ref()
const props = withDefaults(
    defineProps<{
        datas?: any[],
        title: string
    }>(),
    {
        title: ''
    }
);
watch(() => props.datas, (datas) => {
    if (datas && datas.length > 0) {
        init()
    }
}, { deep: true })

watch(() => props.title, (title) => {
    
})

const init = () => {
    const theme = localStorage.getItem('theme');
    let chart = echarts.init(main.value)
    const option = {
        title: {
            show: true,
            text: props.title,
            left: '34%',
            bottom: 'bottom',
            padding: 0,
            textStyle: {
                fontWeight: 400,
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
                fontSize: 14
            }
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            right: '18%',
            top: 'middle',
            orient: 'vertical',
            icon: 'roundRect',
            itemWidth: 10,
            itemHeight: 10,
            textStyle: {
                color: theme === 'dark' ? '#d4d4d4' : '#1d212a',
                fontSize: 10
            },
            lineStyle: {
                width: 10,
                height: 10
            },
            formatter: function (name: string) {
                let datas = option.series[0].data
                if (datas.length === 0) {
                    return `${name}: 0`
                }
                let val = ''
                for (let data of datas) {
                    if (data.name === name) {
                        val = data.value
                        break;
                    }
                }
                return `${name}: ${val}`
            }
        },
        series: [
            {
                name: props.title,
                top: '10px',
                type: 'pie',
                radius: ['40%', '60%'],
                center: ['45%', '50%'],
                avoidLabelOverlap: false,
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: '14',
                        fontWeight: 'bold'
                    }
                },
                labelLine: {
                    show: true,
                    smooth: true
                },
                data: props.datas || []
            }
        ]
    }
    chart.setOption(option)
}

onMounted(() => {
    const wujie = window.$wujie;
    // Judge whether it is a plug-in environment or a local environment through wujie
    if (wujie) {
        // Monitoring platform language change
        wujie?.bus.$on('opengauss-locale-change', (val: string) => {
            console.log('log-search catch locale change');
            nextTick(() => {
                init()
            });
        });
        wujie?.bus.$on('opengauss-theme-change', (val: string) => {
            nextTick(() => {
                init()
            });
        });
    }
})

</script>
<style scoped lang='scss'></style>