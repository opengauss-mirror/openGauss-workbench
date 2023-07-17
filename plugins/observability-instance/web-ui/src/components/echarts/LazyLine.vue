<template>
  <div style="width: 100%; height: 100%">
    <div class="line-tips" v-if="tips">
      <div class=""><svg-icon name="info" />{{ tips }}</div>
    </div>
    <div :key="`${i18n.global.locale.value}`" :id="domId" ref="loadRef" style="width: 100%; height: 100%"></div>
  </div>
</template>

<!-- eslint-disable indent -->
<script setup lang="ts">
import * as echarts from 'echarts/core'
import {
  GridComponent,
  GridComponentOption,
  TooltipComponent,
  LegendComponent,
  TooltipComponentOption,
  ToolboxComponent,
  ToolboxComponentOption,
  BrushComponent,
  BrushComponentOption,
  TitleComponent,
  TitleComponentOption,
  DataZoomComponent,
  DataZoomComponentOption,
} from 'echarts/components'
import {
  LineChart,
  LineSeriesOption,
  ScatterChart,
  ScatterSeriesOption,
  BarChart,
  BarSeriesOption,
} from 'echarts/charts'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { uuid } from '@/shared'
import { useIntersectionObserver } from '@vueuse/core'
import moment from 'moment'
import { useI18n } from 'vue-i18n'
import { useDataZoom } from '@/hooks/echarts'
import { storeToRefs } from 'pinia'
import { i18n } from '@/i18n'
import { useWindowStore } from '@/store/window'
import colorCharts from '@/assets/style/color.module.scss'
import { useIntervalTime } from '@/hooks/time'

export interface LineData {
  name: string
  data: any[]
  [other: string]: any
}

type EchartsOption = echarts.ComposeOption<
  | GridComponentOption
  | TooltipComponentOption
  | LineSeriesOption
  | ScatterSeriesOption
  | ToolboxComponentOption
  | BrushComponentOption
  | TitleComponentOption
  | BarSeriesOption
  | DataZoomComponentOption
>
echarts.use([
  CanvasRenderer,
  LineChart,
  UniversalTransition,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  ScatterChart,
  ToolboxComponent,
  BrushComponent,
  TitleComponent,
  BarChart,
  DataZoomComponent,
])

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    xData?: string[] // xAxis Data
    data?: LineData[] // yAxis Line Data
    max?: number // yAxis max
    min?: number // yAxis min
    interval?: number // yAxis interval
    tabId: string
    rangeSelect: boolean
    isTooltipsFormatDate: boolean // x value is date,and format to YYYY-MM-DD HH:mm:ss

    unit?: string
    scatterUnit?: string
    tips?: string
    // yAxis Scatter Data
    scatterData?: LineData
    // LineChart use areaStyle
    areaStyle?: boolean
    // chart colors
    color?: Array<string>
    /**
     * enable brush for select range time, the value is uuid
     */
    brush?: string
    /**
     * brush default select range
     */
    defaultBrushArea?: string[]
    /**
     * bar chart
     */
    bar?: boolean
    stack?: boolean
    enterable?: boolean
    translate?: boolean
    countByDataTimePicker: boolean
  }>(),
  {
    xData: () => [],
    data: () => [],
    unit: '',
    scatterUnit: '',
    areaStyle: false,
    rangeSelect: false,
    translate: true,
    isTooltipsFormatDate: true,
    enterable: true,
    countByDataTimePicker: true,
  }
)
const timer = ref<number>()
const lastWidth = ref<number>(0)
const notMatch = ref<boolean>()
onMounted(() => {
  // to solve the problem
  // when windows resize,other chart in tab cannot get the windows size
  // then will become small ones
  timer.value = useIntervalTime(
    () => {
      const divElement = document.getElementById(domId) as HTMLElement
      const divWidth = divElement.offsetWidth
      if (divWidth === 0 || divWidth !== lastWidth.value) {
        notMatch.value = true
      }
      if (divWidth !== 0 && notMatch.value) {
        notMatch.value = false
        myChart.resize()
      }
      lastWidth.value = divWidth
    },
    computed(() => 500)
  )
})
const colorArray: Array<string> = colorCharts.chartColors.split(',')
const { theme } = storeToRefs(useWindowStore())
const domId = uuid()
let myChart: echarts.ECharts
const renderChart = () => {
  let data: Array<LineSeriesOption | ScatterSeriesOption | BarSeriesOption> = []
  props.data.forEach((d) => {
    const o: Record<string, any> = {
      type: 'line',
      symbol: 'none',
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
    if (o.lineStyle) o.lineStyle.width = 1
    else {
      o.lineStyle = {
        width: 1,
      }
    }
    data.push(o)
  })
  if (props.scatterData) {
    data.push({
      type: 'scatter',
      symbol: 'circle',
      symbolSize: 6,
      yAxisIndex: 1,
      zlevel: 1,
      ...props.scatterData,
    })
  }
  const chartDom = document.getElementById(domId)
  if (!myChart || myChart.isDisposed()) {
    myChart = echarts.init(chartDom!)
  }
  const option: EchartsOption = {
    title: {
      show: props.data?.length === 0 && (!props.scatterData || props.scatterData?.data?.length === 0),
      text: t('sql.isHasData'),
      left: 'center',
      top: 'center',
      textStyle: {
        color: theme.value === 'dark' ? '#ddd' : '#AEB3BC',
      },
    },
    color: props.color ? props.color : colorArray,
    legend: {
      show: true,
      type: 'scroll',
      left: 10,
      bottom: 0,
      textStyle: {
        color: '#86909C',
        fontWeight: 400,
        fontSize: 11,
      },
      icon: 'roundRect',
      itemWidth: 12,
      itemHeight: 4,
    },
    grid: {
      left: 15,
      right: 15,
      top: props.tips ? 50 : 25,
      bottom: 28,
      containLabel: true,
    },
    tooltip: {
      trigger: 'axis',
      confine: true,
      enterable: props.enterable,
      backgroundColor: 'rgba(0, 0, 0, 0.64)',
      borderWidth: 0,
      formatter: (params) => {
        let htmlStr = '<div style="height: auto;max-height: 180px;overflow-y: scroll;border-radius: 4px;color:#fff">'
        if (Array.isArray(params)) {
          if (props.isTooltipsFormatDate) {
            htmlStr +=
              '<div style="font-size:14px">' +
              moment(new Date(params[0].axisValue)).format('YYYY-MM-DD HH:mm:ss') +
              '</div>'
          } else {
            htmlStr += '<div style="font-size:14px">' + params[0].axisValue + '</div>'
          }

          for (let i = 0; i < params.length; i++) {
            // htmlStr += '<div ">' + params[i].marker + params[i].seriesName + ':' + params[i].value + '</div>'
            htmlStr +=
              '<div style="display: flex;flex-direction: row;align-items:center;font-size:12px">' +
              '<div style="display: inline-block; width: 14px; height: 4px;border-radius: 1px;margin-right:8px;background-color: ' +
              params[i].color +
              ';"></div>' +
              '<div style="flex-grow:1;padding-right:12px">' +
              params[i].seriesName +
              '</div>' +
              '<div style="">' +
              params[i].value +
              (props.unit ? props.unit : '') +
              '</div>' +
              '</div>'
          }
        } else {
          const seriesName = props.translate ? t(`${params.seriesName}`) : params.seriesName
          htmlStr += `${params.name}<div style="display: flex;justify-content: space-between;"><div style="margin-right: 24px;">${params.marker} ${seriesName}</div>${params.data}</div>`
        }
        htmlStr += '</div>'
        return htmlStr
      },
    },
    toolbox: {
      right: -1000,
      feature: {
        dataZoom: { yAxisIndex: false },
        brush: { show: false },
      },
    },
    brush: {
      brushStyle: {
        borderWidth: 0,
        color: theme.value === 'dark' ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.2)',
      },
      transformable: !props.defaultBrushArea,
      outOfBrush:
        props.scatterData && props.defaultBrushArea && props.xData.length > 0
          ? {
              color: props.scatterData.itemStyle.color,
            }
          : undefined,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisTick: { show: false },
      axisLine: {
        lineStyle: {
          color: theme.value === 'dark' ? '#FFFFFF' : '#EAEBEE',
        },
      },
      axisLabel: {
        color: theme.value === 'dark' ? '#FFFFFF' : '#4E5969',
        fontSize: 10,
        formatter: (v) => moment(new Date(v)).format('HH:mm'),
      },
      data: props.xData,
    },
    yAxis: props.scatterData
      ? [
          {
            type: 'value',
            axisLabel: {
              color: theme.value === 'dark' ? '#D4D4D4' : '#4E5969',
              fontSize: 11,
              formatter: `{value}${props.unit}`,
            },
            splitLine: {
              lineStyle: { color: theme.value === 'dark' ? '#4a4a4a' : '#EAEBEE' },
            },
          },
          {
            type: 'value',
            axisLabel: {
              color: theme.value === 'dark' ? '#D4D4D4' : '#4E5969',
              fontSize: 11,
              formatter: `{value}${props.scatterUnit}`,
            },
            splitLine: { show: false },
          },
        ]
      : {
          type: 'value',
          max: props.max,
          min: props.min,
          interval: props.interval,
          axisLabel: {
            color: theme.value === 'dark' ? '#FFFFFF' : '#4E5969',
            fontSize: 11,
            formatter: `{value}${props.unit}`,
          },
          splitLine: {
            lineStyle: { color: theme.value === 'dark' ? '#4a4a4a' : '#EAEBEE' },
          },
        },
    series: data.length > 0 ? data : [],
  }
  myChart.setOption(option, true)
  if (
    !props.countByDataTimePicker &&
    Array.isArray(props.defaultBrushArea) &&
    props.defaultBrushArea.length === 2 &&
    props.xData.length > 0
  ) {
    nextTick(() => {
      let interval =
        (moment(props.defaultBrushArea![1]).valueOf() - moment(props.defaultBrushArea![0]).valueOf()) / 1000
      const startIndexPosi = myChart.convertToPixel({ xAxisIndex: 0 }, 0)
      const endIndexPosi = myChart.convertToPixel({ xAxisIndex: 0 }, props.xData.length - 1)
      let range: any[] = []
      if (interval <= 120) {
        const startIndex = props.xData?.length === 3 ? 1 : props.xData.indexOf(props.defaultBrushArea![0])
        const r = myChart.convertToPixel({ xAxisIndex: 0 }, startIndex)
        const allWidth = endIndexPosi - startIndexPosi
        const width = (interval / 120) * allWidth
        const start = Math.round(r - width / 2)
        const end = Math.round(r + width / 2)
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
          },
        ],
      })
    })
  }
  if (props.rangeSelect) {
    useDataZoom(myChart, props.tabId)
  }
}
// lazy load
const myEmit = defineEmits<{
  (event: 'load'): void
}>()
const loadRef = ref<HTMLDivElement>()
const { stop } = useIntersectionObserver(loadRef, ([{ isIntersecting }]) => {
  if (isIntersecting) {
    myEmit('load')
    nextTick(renderChart)
    stop()
  }
})
watch(
  () => props.data,
  () => {
    nextTick(renderChart)
  },
  { deep: true }
)
</script>

<style scoped lang="scss"></style>
