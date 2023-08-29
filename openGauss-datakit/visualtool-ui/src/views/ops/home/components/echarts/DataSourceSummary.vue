<template>
  <v-chart class="chart" :option="option" />
</template>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const option = ref({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    left: 'left',
    textStyle: {
      color: '#5470c6'
    }
  },
  series: [
    {
      name: t('echarts.DataSourceSummary.5mpiwzpxnsg0'),
      type: 'pie',
      radius: ['40%', '80%'],
      center: ['50%', '60%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '15',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 1, name: 'openGauss' }
      ]
    }
  ]
})

</script>

<style lang="scss" scoped>

</style>
