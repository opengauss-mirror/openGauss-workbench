
<template>
  <div class="line-config-container">
    <div class="d-title-1 mb-s">{{$t('modeling.dy_common.xConfig')}}</div>
    <a-radio-group direction="vertical" v-model="config.showType.key" style="width: 100%;">
      <a-radio :value="1" class="d-data-flow-radio">
        <a-row>
          <a-col :span="7"><span style="color: #f53f3f; font-weight: bold;" v-if="config.showType.key === 1">* </span>{{$t('modeling.components.LineConfig.5m7ijyex0ts0')}}</a-col>
          <a-col :span="11" class="mr-s">
            <a-select v-model="config.showType.dimension" :disabled="config.showType.key !== 1">
              <overflow-tooltip :text="item.label" v-for="(item, key) in allOption" :key="key" :content="item.label">
                <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </a-col>
          <a-col :span="3">{{$t('modeling.components.LineConfig.5m7ijyex1h80')}}</a-col>
        </a-row>
      </a-radio>
      <a-radio :value="2">
        <a-row>{{$t('modeling.components.LineConfig.5m7ijyex1m00')}}</a-row>
      </a-radio>
    </a-radio-group>
    <div class="mb" :style="{ 'padding-left': '28px', 'box-sizing': 'border-box', opacity: config.showType.key === 2 ? 1 : 0.5 }">
      <a-row align="center" class="mb-s">
        <a-col :span="7"><span style="color: #f53f3f; font-weight: bold;" v-if="config.showType.key === 2">* </span>{{$t('modeling.components.LineConfig.5m7ijyex1oo0')}}</a-col>
        <a-col :span="14">
          <a-select v-model="config.showType.dateField" :disabled="config.showType.key !== 2">
            <overflow-tooltip :text="item.label" v-for="(item, key) in datetimeOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb-s">
        <a-col :span="7"><span style="color: #f53f3f; font-weight: bold;" v-if="config.showType.key === 2">* </span>{{$t('modeling.components.LineConfig.5m7ijyex1r80')}}</a-col>
        <a-col :span="14">
          <a-select v-model="config.showType.particle" :disabled="config.showType.key !== 2">
            <a-option v-for="(item, key) in particles" :key="`particles${key}`" :value="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-col>
      </a-row>

    </div>
    <div class="serie-config">
      <div class="content-header mb-s">
        <div class="ch-title">{{$t('modeling.dy_common.yConfig')}}</div>
        <div></div>
      </div>
      <div class="mb">
        <a-row align="center" class="mb-s" v-for="(item, key) in config.indicator" :key="`indicator${key}`">
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
              <a-input :max-length="140"  v-model="item.unit">
                <template #prepend>{{$t('modeling.components.LineConfig.5m7ijyex2040')}}</template>
              </a-input>
            </div>
          </a-col>
        </a-row>
      </div>
      <div class="content-header mb-s modeling-ve-config-h">
        <div class="ch-title">{{$t('modeling.components.LineConfig.5m7ijyex22w0')}}</div>
        <a-button type="primary" shape="circle" size="mini" @click="operate('add-dimension')"><icon-plus /></a-button>
        <a-button class="ml" size="mini" type="outline" @click="openCdd"><template #icon><icon-tags /></template>{{$t('modeling.components.LineConfig.5m7ijyex2540')}}</a-button>
        <a-tooltip content-class="modeling-ve-config-h-tooltip" :content="$t('modeling.dy_common.chartTips1')">
          <div class="qs"><icon-question-circle-fill /></div>
        </a-tooltip>
      </div>
      <div>
        <a-row align="center" class="mb-s" v-for="(item, key) in config.dimension" :key="`dimension${key}`">
          <a-col class="mr-xs" :span="12" style="display: flex">
            <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
            <a-select v-model="item.field" :placeholder="$t('modeling.components.LineConfig.5mpu292dkhc0')">
              <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
                <a-option :value="item.value" :disabled="checkDisabled([item], config.showType.dimension)"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </a-col>
          <a-col class="mr-xs" :span="10">
            <a-input-number v-model="item.num">
              <template #prepend>{{$t('modeling.components.LineConfig.5m7ijyex29k0')}}</template>
            </a-input-number>
          </a-col>
          <a-col :span="1">
            <div class="d-control-remove" @click="operate('delete-dimension', key)">-</div>
          </a-col>
        </a-row>
      </div>
    </div>
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
defineProps<{
  stringOption: KeyValue[],
  allOption: KeyValue[],
  numberOption: KeyValue[],
  datetimeOption: KeyValue[]
}>()
const emits = defineEmits(['openCDD'])
const openCdd = () => emits('openCDD')
const config = reactive({
  indicator: [] as TypeIndicator[], dimension: [] as TypeDimension[],
  showType: { key: 1, dimension: '', dateField: '', particle: '', compareWay: '' }
})
const init = (data?: KeyValue) => {
  if (data && data.indicator && data.dimension) {
    config.indicator = data.indicator
    config.dimension = data.dimension
    if (data.showType) config.showType = data.showType
  } else {
    config.indicator = [{ field: '', type: '', unit: '' }]
    config.dimension = []
  }
}
const operate = (type: string, key1?: number) => {
  if (type === 'add-indicator') config.indicator.push({ field: '', type: '', unit: '' })
  else if (type === 'delete-indicator' && (key1 || key1 === 0)) config.indicator.splice(key1, 1)
  else if (type === 'add-dimension') config.dimension.push({ field: '', num: 5 })
  else if (type === 'delete-dimension' && (key1 || key1 === 0)) {
    config.dimension.splice(key1, 1)
  }
}
const noticeArr: string[] = []
const validate = () => {
  let flag = true
  let message = ''
  if ((config.showType.key === 1 && !config.showType.dimension) || (config.showType.key === 2 && (!config.showType.dateField || !config.showType.particle))) {
    flag = false
    message += t('modeling.components.LineConfig.5m7ijyex2c00')
  }
  if (!config.indicator[0] || !config.indicator[0].field) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.LineConfig.5m7ijyex2e80')
  }
  if (!config.indicator[0] || !config.indicator[0].unit) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.LineConfig.5m7ijyex2jc0')
  }
  config.dimension.forEach((item: any, key: number) => {
    if (!item.field) {
      flag = false
      message += (message ? '；\n ' : '') + `${t('modeling.dy_common.chartNotice1')}${key + 1}${t('modeling.dy_common.chartNotice2')}`
    }
  })
  if (!flag) {
    if (noticeArr.includes(message)) return flag
    let isDelete = false
    Notification.error({
      title: t('modeling.components.LineConfig.5m7ijyex2lk0'),
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
  .line-config-container {
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
