<template>
  <div :id="domId" ref="loadRef" style="width: 100%; height: 100%"></div>
</template>

<!-- eslint-disable indent -->
<script setup lang="ts">
import * as echarts from 'echarts/core'
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
import { i18n } from '@/i18n'
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
    // xAxis Data
    xData?: string[]
    // yAxis Line Data
    data?: LineData[]
    max?: number // yAxis max
    min?: number // yAxis min
    interval?: number // yAxis interval

    unit?: string
    scatterUnit?: string
    // yAxis Scatter Data
    scatterData?: LineData
    // LineChart use areaStyle
    areaStyle?: boolean
    // chart colors
    color?: string[]
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
    color: () => [
      '#37D4D1',
      '#00C7F9',
      '#0D86E2',
      '#425ADD',
      '#E64A19',
      '#9CCC65',
      '#A97526',
      '#2830FF',
      '#8B00E1',
      '#0F866A',
    ],
    translate: true,
    countByDataTimePicker: true,
  }
)
let myChart: echarts.ECharts
const timer = ref<number>()
const lastWidth = ref<number>(0)
const notMatch = ref<boolean>()

const show = ref<boolean>(true)
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

  // @ts-ignore
  const wujie = window.$wujie
  // Judge whether it is a plug-in environment or a local environment through wujie
  if (wujie) {
    // Monitoring platform language change
    wujie?.bus.$on('opengauss-locale-change', (val: string) => {
      nextTick(() => {
      })
    })
  }
})
const domId = uuid()
const renderChart = () => {
  let data: Array<LineSeriesOption | ScatterSeriesOption | BarSeriesOption> = []
  props.data.forEach((d) => {
    const o: Record<string, any> = {
      type: 'line',
      symbol: 'none',
      yAxisIndex: 0,
      zlevel: 1,
      ...d,
      lineStyle: d.lineStyle ? d.lineStyle : { color: '#83CBFF' },
      areaStyle: { color: '#FFFFFF', opacity: 0.4 },
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
        color: '#FFFFFF',
        opacity: 0.5,
        fontSize: '10px',
        fontWeight: 400,
      },
    },
    color: props.color,
    grid: {
      left: 15,
      right: 15,
      top: 25,
      bottom: 10,
      containLabel: true,
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
        color: 'rgba(0, 0, 0, 0.2)',
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
          color: '#EAEBEE',
        },
      },
      axisLabel: {
        color: '#FFFFFF',
        fontSize: 7.3,
        opacity: 0.7,
        formatter: (v) => moment(new Date(v)).format('HH:mm'),
      },
      data: props.xData,
    },
    yAxis: props.scatterData
      ? [
          {
            type: 'value',
            axisLabel: {
              color: '#FFFFFF',
              opacity: 0.7,
              fontSize: 8,
              formatter: `{value}${props.unit}`,
            },
            splitLine: {
              lineStyle: { color: '#EAEBEE', opacity: 0.7 },
            },
          },
          {
            type: 'value',
            axisLabel: {
              color: '#FFFFFF',
              fontSize: 8,
              formatter: `{value}${props.scatterUnit}`,
              opacity: 0.7,
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
            color: '#FFFFFF',
            fontSize: 8,
            formatter: `{value}${props.unit}`,
            opacity: 0.7,
          },
          splitLine: {
            lineStyle: { color: '#EAEBEE', opacity: 0.7 },
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
