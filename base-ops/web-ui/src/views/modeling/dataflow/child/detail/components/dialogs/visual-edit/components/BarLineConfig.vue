
<template>
  <div class="bar-line-config">
    <div class="d-title-1 mb-s">{{$t('modeling.dy_common.xConfig')}}</div>
    <a-radio-group direction="vertical" v-model="config.showType.key" style="width: 100%;">
      <a-radio :value="1" class="d-data-flow-radio">
        <a-row>
          <a-col :span="7"><span style="color: #f53f3f; font-weight: bold;" v-if="config.showType.key === 1">* </span>{{$t('modeling.components.BarLineConfig.5m7in04pdbs0')}}</a-col>
          <a-col :span="11" class="mr-s">
            <a-select v-model="config.showType.dimension">
              <overflow-tooltip :text="item.label" v-for="(item, key) in allOption" :key="key" :content="item.label">
                <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </a-col>
          <a-col :span="3">{{$t('modeling.components.BarLineConfig.5m7in04pe240')}}</a-col>
        </a-row>
      </a-radio>
      <a-radio :value="2">
        <a-row>{{$t('modeling.components.BarLineConfig.5m7in04pe6c0')}}</a-row>
      </a-radio>
    </a-radio-group>
    <div class="mb" :style="{ 'padding-left': '28px', 'box-sizing': 'border-box', opacity: config.showType.key === 2 ? 1 : 0.5 }">
      <a-row align="center" class="mb-s">
        <a-col :span="7"><span style="color: #f53f3f; font-weight: bold;" v-if="config.showType.key === 2">* </span>{{$t('modeling.components.BarLineConfig.5m7in04pe8s0')}}</a-col>
        <a-col :span="14">
          <a-select v-model="config.showType.dateField" :disabled="config.showType.key !== 2">
            <overflow-tooltip :text="item.label" v-for="(item, key) in datetimeOption" :key="key" :content="item.label">
              <a-option :value="item.value">{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb-s">
        <a-col :span="7"><span style="color: #f53f3f; font-weight: bold;" v-if="config.showType.key === 2">* </span>{{$t('modeling.components.BarLineConfig.5m7in04pebk0')}}</a-col>
        <a-col :span="14">
          <a-select v-model="config.showType.particle" :disabled="config.showType.key !== 2">
            <a-option v-for="(item, key) in particles" :key="`particles${key}`" :value="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-col>
      </a-row>

    </div>
    <div class="subCharts-item mb" v-for="(serie, serieKey) in config.subCharts" :key="`serie${serieKey}`">
      <a-row class="serie-chart-type" align="center">
        <a-col :span="6"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.BarLineConfig.5m7in04pegw0')}}</a-col>
        <a-col :span="17">
          <a-radio-group v-model="serie.chartType">
            <a-radio :value="`line`">{{$t('modeling.components.BarLineConfig.5m7in04pej80')}}</a-radio>
            <a-radio :value="`bar`">{{$t('modeling.components.BarLineConfig.5m7in04pemg0')}}</a-radio>
          </a-radio-group>
        </a-col>
        <a-col :span="2" v-if="serieKey !== 0">
          <a-button size="mini" type="primary" shape="circle" @click="operate('delete-serie', serieKey)"><template #icon><icon-minus /></template></a-button>
        </a-col>
      </a-row>
      <div class="serie-config">
        <div class="content-header mb-s">
          <div class="ch-title">{{$t('modeling.dy_common.yConfig')}}</div>
          <div></div>
        </div>
        <div class="mb">
          <a-row align="center" class="mb-s" v-for="(item, key) in serie.indicator" :key="`indicator${key}`">
            <a-col class="mr-xs" :span="9">
              <div style="display: flex">
                <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
                <a-select v-model="item.field" :placeholder="$t('modeling.components.LineConfig.5mpu292dkhc0')">
                  <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
                    <a-option :value="item.value">{{ item.label }}</a-option>
                  </overflow-tooltip>
                </a-select>
              </div>
            </a-col>
            <a-col class="mr-xs" :span="6">
              <a-select v-model="item.type" :placeholder="$t('modeling.components.LineConfig.5mpu292dky40')">
                <a-option v-for="(item, key) in indicatorType" :key="`a${key}`" :value="item.value">{{ item.label }}</a-option>
              </a-select>
            </a-col>
            <a-col class="mr-xs" :span="8">
              <div style="display: flex">
                <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
                <a-input :max-length="140" v-model="item.unit">
                  <template #prepend>{{$t('modeling.components.BarLineConfig.5m7in04per40')}}</template>
                </a-input>
              </div>
            </a-col>
          </a-row>
        </div>
        <div class="content-header mb-s modeling-ve-config-h">
          <div class="ch-title">{{$t('modeling.components.BarLineConfig.5m7in04peu00')}}</div>
          <a-button type="primary" shape="circle" size="mini" @click="operate('add-dimension', serieKey)"><icon-plus /></a-button>
          <a-button class="ml" size="mini" type="outline" @click="openCdd"><template #icon><icon-tags /></template>{{$t('modeling.components.BarLineConfig.5m7in04pewg0')}}</a-button>
          <a-tooltip content-class="modeling-ve-config-h-tooltip" :content="$t('modeling.dy_common.chartTips1')">
            <div class="qs"><icon-question-circle-fill /></div>
          </a-tooltip>
        </div>
        <div>
          <a-row align="center" class="mb-s" v-for="(item, key) in serie.dimension" :key="`dimension${key}`">
            <a-col class="mr-xs" :span="12" style="display: flex">
              <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
              <a-select v-model="item.field" :placeholder="$t('modeling.components.LineConfig.5mpu292dky40')">
                <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
                  <a-option :value="item.value" :disabled="checkDisabled([item], config.showType.dimension)"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
                </overflow-tooltip>
              </a-select>
            </a-col>
            <a-col class="mr-xs" :span="10">
              <a-input-number v-model="item.num">
                <template #prepend>{{$t('modeling.components.BarLineConfig.5m7in04pez40')}}</template>
              </a-input-number>
            </a-col>
            <a-col :span="1">
              <div class="d-control-remove" @click="operate('delete-dimension', serieKey, key)">-</div>
            </a-col>
          </a-row>
        </div>
      </div>
    </div>
    <a-button type="outline" @click="operate(`add-serie`)"><template #icon><icon-plus /></template>{{$t('modeling.components.BarLineConfig.5m7in04pf1c0')}}</a-button>
  </div>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { IconPlus, IconQuestionCircleFill } from '@arco-design/web-vue/es/icon'
import { KeyValue } from '@antv/x6/lib/types'
import { indicatorType, compareWays, particles } from '../hooks/options'
import { Notification } from '@arco-design/web-vue'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { checkDisabled } from '../../../config-panel/utils/tool'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
interface TypeIndicator { field: string, type: string, unit: string }
interface TypeDimension { field: string, num: number }
interface configModal {
  chartType?: string,
  indicator: Array<TypeIndicator>,
  dimension: Array<TypeDimension>
}
defineProps<{
  stringOption: KeyValue[],
  allOption: KeyValue[],
  numberOption: KeyValue[],
  datetimeOption: KeyValue[]
}>()
const emits = defineEmits(['openCDD'])
const openCdd = () => emits('openCDD')
const getModal = () => {
  let modal: configModal = {
    indicator: [{ field: '', type: '', unit: '' }],
    dimension: []
  }
  return modal
}
const config = reactive({
  subCharts: [] as configModal[],
  showType: { key: 1, dimension: '', dateField: '', particle: '', compareWay: '' }
})
const init = (data?: KeyValue) => {
  if (data && data.subCharts && Array.isArray(data.subCharts) && data.subCharts.length > 0) {
    config.subCharts = data.subCharts
    if (data.showType) config.showType = data.showType
  } else {
    config.subCharts = []
    config.subCharts.push(getModal())
    config.showType = { key: 1, dimension: '', dateField: '', particle: '', compareWay: '' }
  }
}
const operate = (type: string, serieKey?: number, key1?: number) => {
  if (type === 'add-serie') config.subCharts.push(getModal())
  if (serieKey || serieKey === 0) {
    if (type === 'delete-serie') config.subCharts.splice(serieKey, 1)
    else if (type === 'add-indicator') config.subCharts[serieKey].indicator.push({ field: '', type: '', unit: '' })
    else if (type === 'delete-indicator' && (key1 || key1 === 0)) config.subCharts[serieKey].indicator.splice(key1, 1)
    else if (type === 'add-dimension') config.subCharts[serieKey].dimension.push({ field: '', num: 5 })
    else if (type === 'delete-dimension' && (key1 || key1 === 0)) {
      config.subCharts[serieKey].dimension.splice(key1, 1)
    }
  }
}
const noticeArr: string[] = []
const validate = () => {
  let flag = true
  let message = ''
  if ((config.showType.key === 1 && !config.showType.dimension) || (config.showType.key === 2 && (!config.showType.dateField || !config.showType.particle))) {
    flag = false
    message += t('modeling.dy_common.xConfigMessage')
  }
  if (config.subCharts.length === 0) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.BarLineConfig.5m7in04pf3o0')
  } else {
    config.subCharts.forEach((serie, key1: number) => {
      if (!serie.chartType) {
        flag = false
        message += (message ? '；\n ' : '') + t('modeling.components.BarLineConfig.5m7in04pf600') + (key1 + 1) + t('modeling.components.BarLineConfig.5m7in04pf8o0')
      }
      serie.indicator.forEach((item: KeyValue) => {
        if (!item.field) {
          flag = false
          message += (message ? '；\n ' : '') + t('modeling.components.BarLineConfig.5m7in04pf600') + (key1 + 1) + t('modeling.components.BarLineConfig.5m7in04pfas0')
        }
        if (!item.unit) {
          flag = false
          message += (message ? '；\n ' : '') + t('modeling.components.BarLineConfig.5m7in04pf600') + (key1 + 1) + t('modeling.components.BarLineConfig.5m7in04pfgs0')
        }
      })
      serie.dimension.forEach((item22: any, key22: number) => {
        if (!item22.field) {
          flag = false
          message += (message ? '；\n ' : '') + `${t('modeling.dy_common.chartNotice3')}${key1 + 1}${t('modeling.dy_common.chartNotice4')}${key22 + 1}${t('modeling.dy_common.chartNotice2')}`
        }
      })
    })
  }
  if (!flag) {
    if (noticeArr.includes(message)) return flag
    let isDelete = false
    Notification.error({
      title: t('modeling.components.BarLineConfig.5m7in04pfis0'),
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
  .bar-line-config {
    .content-header {
      display: flex;
      align-items: center;
      .ch-title {
        margin-right: 8px;
        color: rgb(var(--primary-6));
        font-size: 16px;
        font-weight: bold;
      }
      .ml {
        margin-left: auto;
      }
      .qs {
        width: 20px;
        height: 20px;
        // background-color: rgb(var(--primary-6));
        display: flex;
        align-items: center;
        justify-content: center;
        margin-left: 5px;
        border-radius: 50%;
        color: #fff;
        cursor: pointer;
        svg {
          color: rgb(var(--primary-6));
          font-size: 20px;
        }
      }
    }
    .d-title-1 {
      color: rgb(var(--primary-6));
      font-size: 16px;
      font-weight: bold;
    }
    .subCharts-item {
      border: 1px solid var(--color-neutral-3);
      border-radius: 4px;
      .serie-chart-type {
        padding: 12px;
        border-bottom: 1px solid var(--color-neutral-4);
      }
      .serie-config {
        padding: 12px;
      }
    }
  }
</style>
<style lang="less">
  .d-data-flow-radio {
    .arco-radio-label {
      width: 100%;
    }
  }
  .modeling-ve-config-h-tooltip {
    white-space: pre-wrap;
  }
</style>
