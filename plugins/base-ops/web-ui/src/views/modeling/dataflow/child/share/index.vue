
<template>
  <div class="app-container" id="childShare">
    <div class="main-bd">
      <div class="navbar">
        <div class="left-side" @click="toHome">
          <a-space>
            <img src="@/assets/logo.png" alt="logo" />
            <a-typography-title
              :style="{ margin: 0, fontSize: '14px', color: '#fff' }"
              :heading="5"
            >
            </a-typography-title>
          </a-space>
        </div>
      </div>
      <div class="preview-content" v-if="route.params.id">
        <grid-layout ref="gridlayoutRef" v-model:layout="layout"
          :col-num="colNum"
          :row-height="10"
          :is-draggable="false"
          :is-resizable="false"
          :vertical-compact="true"
          :use-css-transforms="true"
          class="grad-layout"
        >
          <grid-item :key="item.i" v-for="(item) in layout"
            :x="item.x"
            :y="item.y"
            :w="item.w"
            :h="item.h"
            :i="item.i"
            style="border: 1px solid blue;"
          >
            <div class="grid-item-content">
              <div :id="`previewShareId_${item.i}`"></div>
            </div>
          </grid-item>
        </grid-layout>
      </div>
      <div v-else>
        {{$t('modeling.share.index.5m78k584o7w0')}}
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { getMapGeo, modelingVRGetByReportId } from '@/api/modeling'
import { KeyValue } from '@/types/global'
import { Message } from '@arco-design/web-vue'
import { nextTick, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Base64 from '../../../utils/base64'
import * as echarts from 'echarts'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const router = useRouter()
const route = useRoute()
interface ItemType { x: number, y: number, w: number, h: number, i: number | string, imgBase64: string, chartDataJson: any }
const layout = ref<Array<ItemType>>([])
const colNum = 120
const getContent = () => {
  if (route.params.id) {
    const b = new Base64()
    const id = b.decode(`${route.params.id}`).split('|d1a2ay1k0|')[0]
    modelingVRGetByReportId(id).then((res: KeyValue) => {
      layout.value = JSON.parse(res.data.paramsJson)
      layout.value.forEach((item: ItemType) => {
        item.chartDataJson = JSON.parse(item.chartDataJson)
      })
      nextTick(() => {
        layout.value.forEach((item: ItemType) => {
          renderChart(item)
        })
      })
    })
  } else {
    Message.error({ content: t('modeling.share.index.5m78k584ock0') })
  }
}
getContent()
 /** chart */
const charts: echarts.ECharts[] = []
const renderChart = (item: ItemType) => {
  let chart: echarts.ECharts
  let dom = document.getElementById(`previewShareId_${item.i}`)
  if (dom) {
    chart = echarts.init(dom)
    let option = item.chartDataJson
    chart.showLoading()
    if (option.series && Array.isArray(option.series) && option.series.length > 0 && option.series[0] && option.series[0].type === 'map' && option.series[0].map) {
      getMapGeo(option.series[0].map).then((res: KeyValue) => {
        echarts.registerMap(res.mapData.name, res.mapData.data)
        chart.hideLoading()
        chart && chart.setOption(option, true)
      })
    } else {
      chart.hideLoading()
      chart && chart.setOption(option, true)
    }
    charts.push(chart)
  }
}
const toHome = () => {
  router.push('/')
}
</script>
<style scoped lang="less">
.app-container {
  width: 100vw;
  height: 100vh;
  background-color: var(--color-neutral-2);
  padding: 0;
  overflow-x: hidden;
  .main-bd {
    overflow-x: hidden;
    .navbar {
      display: flex;
      justify-content: space-between;
      height: 50px;
      background-color: #282B33;
      border-bottom: 1px solid var(--color-border);
    }
    .left-side {
      display: flex;
      align-items: center;
      padding-left: 20px;
      cursor: pointer;
    }
    .preview-content {
      width: 100%;
      height: calc(100vh - 55px);
      box-sizing: border-box;
      overflow-x: hidden;
      .grid-item-content {
        width: 100%;
        height: 100%;
        border-radius: 2px;
        box-sizing: border-box;
        padding: 10px;
        border-radius: 4px;
        > div {
          width: 100%;
          height: 100%;
        }
      }
    }
    .grad-layout {
      background-color: var(--color-neutral-2);
    }
  }
}
</style>
<style lang="less">
  .preview-content {
    .vue-grid-item {
      border: none !important;
    }
  }
</style>
