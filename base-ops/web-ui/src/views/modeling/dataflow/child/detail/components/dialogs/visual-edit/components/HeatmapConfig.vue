
<template>
  <div class="heatmap-config-container">
    <div class="content-header mb-s">
      <div class="ch-title">{{$t('modeling.components.HeatmapConfig.5m7ikcw34ic0')}}</div>
      <div></div>
    </div>
    <div class="mb">
      <a-row align="center" class="mb-s">
        <a-col class="mr-xs" :span="9">
          <div style="display: flex">
            <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
            <a-select v-model="config.indicator.field" :placeholder="$t('modeling.components.LineConfig.5mpu292dkhc0')">
              <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
                <a-option :value="item.value">{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </div>
        </a-col>
        <a-col class="mr-xs" :span="6">
          <a-select v-model="config.indicator.type" :placeholder="$t('modeling.components.LineConfig.5mpu292dky40')">
            <a-option v-for="(item, key) in indicatorTypeSum" :key="`a${key}`" :value="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-col>
        <a-col class="mr-xs" :span="8">
          <a-input :max-length="140" model="config.indicator.unit">
            <template #prepend>{{$t('modeling.components.HeatmapConfig.5m7ikcw35840')}}</template>
          </a-input>
        </a-col>
      </a-row>
    </div>
    <div class="content-header mb-s">
      <div class="ch-title">{{$t('modeling.dy_common.xFB')}}</div>
      <a-button size="mini" type="outline" @click="openCdd"><template #icon><icon-tags /></template>{{$t('modeling.components.HeatmapConfig.5m7ikcw35e80')}}</a-button>
    </div>
    <div class="mb">
      <a-row align="center" class="mb-s">
        <a-col class="mr-xs" :span="12">
          <div style="display: flex">
            <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
            <a-select v-model="config.x.field" :placeholder="$t('modeling.components.LineConfig.5mpu292dkhc0')">
              <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
                <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </div>
        </a-col>
        <a-col class="mr-xs" :span="11">
          <a-input-number v-model="config.x.num">
            <template #prepend>{{$t('modeling.components.HeatmapConfig.5m7ikcw35hc0')}}</template>
          </a-input-number>
        </a-col>
      </a-row>
    </div>
    <div class="content-header mb-s">
      <div class="ch-title">{{$t('modeling.dy_common.yConfig')}}</div>
      <a-button size="mini" type="outline" @click="openCdd"><template #icon><icon-tags /></template>{{$t('modeling.components.HeatmapConfig.5m7ikcw35e80')}}</a-button>
    </div>
    <div class="mb">
      <a-row align="center" class="mb-s">
        <a-col class="mr-xs" :span="12">
          <div style="display: flex">
            <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
            <a-select v-model="config.y.field" :placeholder="$t('modeling.components.LineConfig.5mpu292dkhc0')">
              <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
                <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </div>
        </a-col>
        <a-col class="mr-xs" :span="11">
          <a-input-number v-model="config.y.num">
            <template #prepend>{{$t('modeling.components.HeatmapConfig.5m7ikcw35hc0')}}</template>
          </a-input-number>
        </a-col>
      </a-row>
    </div>
    <div>
      <a-row align="center">
        <a-col class="mr-xs" :span="5">{{$t('modeling.components.HeatmapConfig.5m7ikcw35kc0')}}</a-col>
        <a-col class="mr-xs" :span="8">
          <a-input-number v-model="config.range[0]" :style="{width:'100%'}" class="input-demo" :max="config.range[1]"/>
        </a-col>
        <a-col class="mr-xs" :span="1" style="text-align: center;">-</a-col>
        <a-col class="mr-xs" :span="8">
          <a-input-number v-model="config.range[1]" :style="{width:'100%'}" class="input-demo" :min="config.range[0]"/>
        </a-col>
      </a-row>
    </div>
  </div>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import { Notification } from '@arco-design/web-vue'
import { KeyValue } from '@antv/x6/lib/types'
import { indicatorType, indicatorTypeSum } from '../hooks/options'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
interface TypeIndicator { field: string, type: string, unit: string }
interface TypeDimension { field: string, num: number }
defineProps<{
  stringOption: KeyValue[],
  numberOption: KeyValue[],
  datetimeOption: KeyValue[]
}>()
const emits = defineEmits(['openCDD'])
const openCdd = () => emits('openCDD')
const config = reactive({
  indicator: { field: '', type: 'sum', unit: '' } as TypeIndicator,
  x: { field: '', num: 5 } as TypeDimension,
  y: { field: '', num: 5 } as TypeDimension,
  range: [0, 100] as [number, number]
})
const init = (data?: KeyValue) => {
  if (data) {
    config.indicator = data.indicator
    config.x = data.x ? data.x : { field: '', num: 5 }
    config.y = data.y ? data.y : { field: '', num: 5 }
  } else {
    config.indicator = { field: '', type: 'sum', unit: '' }
    config.x = { field: '', num: 5 }
    config.y = { field: '', num: 5 }
  }
}
const noticeArr: string[] = []
const validate = () => {
  let flag = true
  let message = ''
  if (!config.indicator || !config.indicator.field) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.dy_common.heatmapConfig.notice1')
  }
  if (!config.x || !config.x.field) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.dy_common.heatmapConfig.notice2')
  }
  if (!config.y || !config.y.field) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.dy_common.heatmapConfig.notice2')
  }
  if (!flag) {
    if (noticeArr.includes(message)) return flag
    let isDelete = false
    Notification.error({
      title: t('modeling.components.BarConfig.5m7insim3vc0'),
      content: message,
      closable: true,
      duration: 5 * 1000,
      onClose: () => {
        let index = noticeArr.indexOf(message)
        if (index != -1) noticeArr.splice(index, 1)
        isDelete = true
      }
    })
    noticeArr.push(message)
    setTimeout(() => {
      if (!isDelete) {
        let index = noticeArr.indexOf(message)
        if (index != -1) noticeArr.splice(index, 1)
      }
    }, 5 * 1000)
  }
  return flag
}
defineExpose({ config, validate, init })
</script>
<style scoped lang="less">
  .heatmap-config-container {
    .content-header {
      display: flex;
      align-items: center;
      .ch-title {
        margin-right: 8px;
        color: rgb(var(--primary-6));
        font-size: 16px;
        font-weight: bold;
      }
      > * {
        &:last-child {
          margin-left: auto;
        }
      }
    }
    .d-title-1 {
      color: rgb(var(--primary-6));
      font-size: 16px;
      font-weight: bold;
    }
  }
</style>
<style lang="less">
  .d-data-flow-radio {
    .arco-radio-label {
      width: 100%;
    }
  }
</style>
