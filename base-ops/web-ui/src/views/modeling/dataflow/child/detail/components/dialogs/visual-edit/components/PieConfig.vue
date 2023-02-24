
<template>
  <div class="pie-config-container">
    <div class="serie-config">
      <div class="content-header mb-s">
        <div class="ch-title">{{$t('modeling.components.PieConfig.5m7ij2mkd0w0')}}</div>
        <div></div>
      </div>
      <div class="mb">
        <a-row align="center" class="mb-s">
          <a-col class="mr-xs" :span="9">
            <div style="display: flex">
              <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
              <a-select v-model="config.indicator.field" :placeholder="$t('modeling.components.PieConfig.5mpu2f4wcy80')">
                <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
                  <a-option :value="item.value">{{ item.label }}</a-option>
                </overflow-tooltip>
              </a-select>
            </div>
          </a-col>
          <a-col class="mr-xs" :span="6">
            <div style="display: flex">
              <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
              <a-select v-model="config.indicator.type" :placeholder="$t('modeling.components.PieConfig.5mpu2f4wdd40')">
                <a-option v-for="(item, key) in indicatorTypeSum" :key="`a${key}`" :value="item.value">{{ item.label }}</a-option>
              </a-select>
            </div>
          </a-col>
          <a-col class="mr-xs" :span="8">
            <a-input :max-length="140" v-model="config.indicator.unit">
              <template #prepend>{{$t('modeling.components.PieConfig.5m7ij2mkdyw0')}}</template>
            </a-input>
          </a-col>
        </a-row>
      </div>
      <div class="content-header mb-s">
        <div class="ch-title">{{$t('modeling.components.PieConfig.5m7ij2mke7w0')}}</div>
        <a-button size="mini" type="outline" @click="openCdd"><template #icon><icon-tags /></template>{{$t('modeling.components.PieConfig.5m7ij2mkeas0')}}</a-button>
      </div>
      <div>
        <a-row align="center" class="mb-s">
          <a-col class="mr-xs" :span="12">
            <a-select v-model="config.dimension.field" :placeholder="$t('modeling.components.PieConfig.5mpu2f4wcy80')">
              <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
                <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </a-col>
          <a-col class="mr-xs" :span="11">
            <a-input-number v-model="config.dimension.num">
              <template #prepend>{{$t('modeling.components.PieConfig.5m7ij2mkeek0')}}</template>
            </a-input-number>
          </a-col>
        </a-row>
      </div>
      <div class="d-title-1 mb-s">{{$t('modeling.components.PieConfig.5m7ij2mkejs0')}}</div>
      <a-row align="center" class="mb-s">
        <a-col :span="7">{{$t('modeling.components.PieConfig.5m7ij2mkemg0')}}</a-col>
        <a-col :span="11" class="mr-s">
          <a-select v-model="config.showType.field">
            <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
              <a-option :value="item.value" v-if="!item.isCustom">{{ item.label }}</a-option>
              <a-option :value="item.value" v-else><icon-tag class="mr-s" /><span>{{ item.label }}</span></a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
        <a-col :span="3">{{$t('modeling.components.PieConfig.5m7ij2mkepc0')}}</a-col>
      </a-row>
      <a-row align="center" class="mb-s">
        <a-col :span="7">{{$t('modeling.components.PieConfig.5m7ij2mkes80')}}</a-col>
        <a-col :span="11" class="mr-s">
          <a-radio-group v-model="config.showType.percentage">
            <a-radio :value="1">{{$t('modeling.components.PieConfig.5m7ij2mkeuo0')}}</a-radio>
            <a-radio :value="2">{{$t('modeling.components.PieConfig.5m7ij2mkex40')}}</a-radio>
          </a-radio-group>
        </a-col>
      </a-row>
    </div>
  </div>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { KeyValue } from '@antv/x6/lib/types'
import { indicatorType, indicatorTypeSum } from '../hooks/options'
import { Notification } from '@arco-design/web-vue'
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
  indicator: { field: '', type: '', unit: '' } as TypeIndicator, dimension: { field: '', num: 5 } as TypeDimension,
  showType: { field: '', percentage: '' }
})
const init = (data?: KeyValue) => {
  if (data && data.indicator && data.dimension) {
    config.indicator = data.indicator
    config.dimension = data.dimension
    config.showType = data.showType
  } else {
    config.indicator = { field: '', type: '', unit: '' }
    config.dimension = { field: '', num: 5 }
    config.showType = { field: '', percentage: '' }
  }
}
const noticeArr: string[] = []
const validate = () => {
  let flag = true
  let message = ''
  if (!config.indicator || !config.indicator.field) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.BarConfig.5m7insim3o80')
  }
  if (!config.indicator || !config.indicator.type) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.BarConfig.5m7insim3qo0')
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
  .pie-config-container {
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
