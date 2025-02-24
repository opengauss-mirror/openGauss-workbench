<template>
  <div class="echartContainer">
    <v-chart :option="sqlOptions"></v-chart>
  </div>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue';
import * as echarts from 'echarts';
import VChart from 'vue-echarts'
import { getSqlDuration } from '@/api/playback.js'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  id: {
    type: Number,
    require: true
  }
})
let source = []
let target = []
const sqlOptions = ref({})
onMounted(async () => {
  const { data } = await getSqlDuration(props.id)
  source = JSON.parse(data.source).map(v => [...v, 'source'])
  target = JSON.parse(data.target).map(v => [...v, 'target'])
  const demoData = [[
    "sqlId",
    "duration",
    "sqlType",
    'sqlName'
  ], ...source, ...target]
  initOptions(demoData)
})

const title = computed(() => {
  return t('transcribe.detail.compareTitle')
})

const yAxis = computed(()=>{
  return t('transcribe.detail.duration')
})

const initOptions = (demoData) => {
  sqlOptions.value = {
    dataset: [
      {
        id: 'dataset_raw',
        source: demoData
      },
      {
        id: 'dataset_of_source',
        fromDatasetId: 'dataset_raw',
        transform: {
          type: 'filter',
          config: {
            and: [
              { dimension: 'sqlId', gte: 0 },
              { dimension: 'sqlType', '=': 'source' }
            ]
          }
        }
      },
      {
        id: 'dataset_of_target',
        fromDatasetId: 'dataset_raw',
        transform: {
          type: 'filter',
          config: {
            and: [
              { dimension: 'sqlId', gte: 0 },
              { dimension: 'sqlType', '=': 'target' }
            ]
          }
        }
      }
    ],
    legend: {
      show: true,
      right: '10%',
      data: ['target', 'source'],
    },
    title: {
      text: title,
      left: '45%'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        return `sqlId: ${params[0].data[0]}<br/><span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:#EC4F83;"></span>source: ${params[0].data[1]}<br/><span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:#2CB57C;"></span>target: ${params[1].data[1]}`
      }
    },
    xAxis: {
      name: 'SqlId',
      type: 'category',
      nameLocation: 'end',
      interval: 'auto',
      nameTextStyle: {
        fontSize: 20
      }
    },
    yAxis: {
      name: yAxis,
      type: 'value',
      show: true,
      nameLocation: 'end',
      nameTextStyle: {
        fontSize: 20
      },
      axisLine: {
        show: true
      },
      axisTick: {
        show: true,
        interval: 'auto'
      }

    },
    series: [
      {
        type: 'line',
        datasetId: 'dataset_of_source',
        showSymbol: false,
        color: '#EC4F83',
        name: 'source',
        encode: {
          x: 'sqlId',
          y: 'duration',
          itemName: 'sqlId',
          tooltip: ['duration']
        }
      },
      {
        type: 'line',
        datasetId: 'dataset_of_target',
        showSymbol: false,
        name: 'target',
        color: '#2CB57C',
        encode: {
          x: 'sqlId',
          y: 'duration',
          itemName: 'sqlId',
          tooltip: ['duration']
        }
      }
    ]
  }
}
</script>
<style lang="less">
.echartContainer {
  width: 100%;
  height: 100%
}
</style>