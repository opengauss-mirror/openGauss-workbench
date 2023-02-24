
<template>
  <div class="graph-config-container">
    <div class="d-title-1 mb-s">{{$t('modeling.components.GraphConfig.5m7ikjwuoig0')}}</div>
    <div class="mb">
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwuphs0')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.node.id" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwuphs0')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwupmw0')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.node.name" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwupmw0')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwupsg0')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.node.x" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwupsg0')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwupv40')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.node.y" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwupv40')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwupxw0')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.node.size" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwupxw0')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
    </div>
    <div class="d-title-1 mb-s">{{$t('modeling.components.GraphConfig.5m7ikjwuq0w0')}}</div>
    <div class="mb">
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwuq3c0')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.relation.out" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwuq3c0')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwuq600')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.relation.in" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwuq600')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb">
        <a-col class="mr-xs" :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.GraphConfig.5m7ikjwuq8k0')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.relation.value" :placeholder="$t('modeling.components.GraphConfig.5m7ikjwuq8k0')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
    </div>
  </div>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { KeyValue } from '@antv/x6/lib/types'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { Notification } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
interface TypeIndicator { field: string, type: string, unit: string }
defineProps<{
  stringOption: KeyValue[],
  numberOption: KeyValue[],
  datetimeOption: KeyValue[]
}>()
const config = reactive({
  indicator: { field: '', type: '', unit: '' } as TypeIndicator,
  node: { id: '', name: '', x: '', y: '', size: '' },
  relation: { out: '', in: '', value: '' }
})
const init = (data?: KeyValue) => {
  if (data) {
    config.indicator = data.indicator
    config.node = data.node
    config.relation = data.relation
  } else {
    config.indicator = { field: '', type: '', unit: '' }
    config.node = { id: '', name: '', x: '', y: '', size: '' }
    config.relation = { out: '', in: '', value: '' }
  }
}
const noticeArr: string[] = []
const validate = () => {
  let flag = true
  let message = ''
  if (!config.node || !config.node.id) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.5mjckw7can80')
  }
  if (!config.node || !config.node.name) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.5mjckw850is0')
  }
  if (!config.relation || !config.relation.out) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.5mjckw8511c0')
  }
  if (!config.relation || !config.relation.in) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.5mjckw8516w0')
  }
  if (!config.relation || !config.relation.value) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.5mjckw851bo0')
  }
  if (!config.node || !config.node.x) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.notice2')
  }
  if (!config.node || !config.node.y) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.notice3')
  }
  if (!config.node || !config.node.size) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.GraphConfig.notice1')
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
  .graph-config-container {
    .content-header {
      display: flex;
      align-items: center;
      .ch-title {
        margin-right: 8px;
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
