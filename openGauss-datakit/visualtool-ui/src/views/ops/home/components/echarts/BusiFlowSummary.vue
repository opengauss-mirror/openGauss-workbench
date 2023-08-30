<template>
  <v-chart class="chart" :option="option" />
</template>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const option = ref<any>({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    left: 'left'
  },
  series: [
    {
      name: t('echarts.BusiFlowSummary.5mpiw64lv300'),
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
      data: []
    }
  ]
})

onMounted(() => {
  option.value.series[0].data = []
})

</script>

<style lang="scss" scoped>

</style>
