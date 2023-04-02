
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
    @change="tabChange"
  >
    <a-tab-pane key="1" :title="$t('modeling.base.QueryOperator.5m7hhed6am80')">
      <div class="tab-content d-a-form">
        <a-form-item :label="$t('modeling.base.QueryOperator.5m7hhed94ik0')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <div class="select-comp-container dy-select-container">
          <a-select popup-container=".select-comp-container"
            v-model="config.table" :placeholder="$t('modeling.base.QueryOperator.5m7hhed94qc0')" allowSearch :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
            @change="(value) => selectChange(value, 'table')"
          >
            <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
            <overflow-tooltip :text="item.tablename" v-for="(item, key) in tableList" :key="key" :content="item.tablename">
              <a-option  class="dianayako_select-option-disabled"   :value="item.tablename" :disabled="checkDisabled(useTable, item.tablename)">{{ item.tablename }}</a-option>
            </overflow-tooltip>
          </a-select>
          </div>
        </a-form-item>
        <a-row>
          <a-col :span="2"></a-col>
          <a-col :span="22">
            <div class="d-tips">
              <icon-exclamation-circle-fill />
              {{$t('modeling.dy_common.panel.queryTip1')}}
            </div>
          </a-col>
        </a-row>
          <div class="d-form-item-label mb-s">
            <div class="label-text label-color">{{$t('modeling.base.QueryOperator.5m7hhed94tg0')}}</div>
            <div class="d-control-add" @click="configOperateFields('add')">+</div>
          </div>
          <a-row align="center" v-for="(field, fieldKey) in config.fields" :key="`configFields${fieldKey}`" class="mb-s">
            <a-col :span="21" class="mr-s">
            <div class="select-field-container dy-select-container">
              <a-select popup-container=".select-field-container" v-model="field.value" :placeholder="$t('modeling.base.QueryOperator.5m7hhed94w80')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                allowSearch style="width: 100%;"
                @change="saveData('fields', config.fields)">
                <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                  <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                  <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                    <a-option  class="dianayako_select-option-disabled" :value="`${group.group}.${item.name}`" :disabled="checkDisabled(config.fields, `${group.group}.${item.name}`, 'value')">{{ item.name }}</a-option>
                  </overflow-tooltip>
                </a-optgroup>
              </a-select>
            </div>
            </a-col>
            <a-col :span="2">
              <div class="d-control-remove" @click="configOperateFields('delete', fieldKey)">-</div>
            </a-col>
          </a-row>
      </div>
    </a-tab-pane>
  </a-tabs>
  <a-modal
    :visible="warning.show"
    :ok-loading="warning.loading"
    :closable="false"
    simple
    :ok-text="$t('modeling.base.QueryOperator.5m7hr5m93c80')"
    :cancel-text="$t('modeling.base.QueryOperator.5m7hr5m943c0')"
    @ok="warningOk"
    @cancel="warningClose"
  >
    <div class="warning">
      <div class="title"><icon-exclamation-circle-fill color="#ff7d00" />{{$t('modeling.base.QueryOperator.5m7hr5m948k0')}}</div>
      <div class="content">{{$t('modeling.base.QueryOperator.5m7hr5m94c00')}}</div>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { reactive, computed, watch, ref, nextTick } from 'vue'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import {
  Tabs as ATabs, TabPane as ATabPane, FormItem as AFormItem,
  Row as ARow, Col as ACol, Select as ASelect
} from '@arco-design/web-vue'
import { Cell } from '@antv/x6'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { KeyValue } from '@/api/modeling/request'
import VisualConfig from '../VisualConfig.vue'
import { checkDisabled } from '../../utils/tool'
const AOption = ASelect.Option
const AOptgroup = ASelect.OptGroup
const dFStore = useDataFlowStore()
const useTable = computed(() => dFStore.getUseTable)
watch(() => dFStore.useTable, () => {
  config.fields.forEach((item: KeyValue) => item.value = null)
  output.fields.forEach((item: KeyValue) => item.key = null)
}, { deep: true })
let cell: Cell | null = null
const visualConfigRef = ref<InstanceType<typeof VisualConfig>>()
const init = (pCell: Cell) => {
  cell = pCell
  let { data } = pCell
  config.fields = data.fields ? data.fields : []
  config.table = data.table
  input.source = data.source ? data.source : []
  output.alias = data.outputAlias ? data.outputAlias : ''
  output.fields = data.outputFields ? data.outputFields : []
  warning.oldValue = data.table ? data.table : ''
}
let f = false
const tabChange = (e: string | number) => {
  if (e === '4') {
    if (f) return
    nextTick(() => {
      visualConfigRef.value && cell?.data.visualConfig && visualConfigRef.value.init(cell?.data.visualConfig)
    })
  }
}
const tableList: any = computed(() => dFStore.getTableSelectList)
const iData = reactive({
  activeKey: `1`
})
const selectChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[], prop: any) => {
  if (prop === 'table') {
    warningOpen()
  }
}
const config: KeyValue = reactive({
  table: undefined, tableInfo: {},
  fields: []
})
const fieldsList = computed(() => dFStore.getFieldsSelectList)
const configOperateFields = (type: string, key?: number) => {
  if (type === 'add') config.fields.push({ value: undefined })
  else if (type === 'delete') config.fields.splice(key, 1)
  saveData('fields', config.fields)
}
const input: KeyValue = reactive({
  source: []
})
const output: KeyValue = reactive({
  fields: [], alias: ''
})
const saveData = (key: string, value: any) => {
  if (cell) {
    let cellData = cell.getData()
    cell?.setData({ ...cellData, [key]: JSON.parse(JSON.stringify(value)) }, { overwrite: true })
  }
}
const warning = reactive({
  show: false, loading: false, oldValue: ''
})
const warningOpen = () => {
  if (warning.oldValue) {
    warning.show = true
  } else {
    warningOk()
  }
}
const warningClose = (isFunc: boolean) => {
  warning.show = false
  if (!isFunc) config.table = warning.oldValue
}
const warningOk = () => {
  warning.loading = true
  dFStore.setDatabaseTableInfo(config.table, { tableKey: 0 }).then(() => {
    warning.loading = false
    warning.oldValue = JSON.parse(JSON.stringify(config.table))
    warningClose(true)
    saveData('table', config.table)
  }).catch(() => {
    warning.loading = false
    warning.oldValue = JSON.parse(JSON.stringify(config.table))
    warningClose(true)
    saveData('table', config.table)
  })
}
defineExpose({ init })
</script>
<style scoped lang="less">
  .tab-content {
    padding: 0 10px;
    .d-title-1 {
      font-size: 14px;
      margin-bottom: 20px;
    }
  }
  .warning {
    .title {
      text-align: center;
      font-size: 20px;
      color: #ff7d00;
      margin-bottom: 10px;
      > svg {
        margin-right: 10px;
      }
    }
    .content {
      font-size: 14px;
    }
  }
  .d-tips {
    color: var(--color-neutral-6);
    margin-bottom: 20px;
  }
  .select-comp-container {
    position: relative;
    width: 100%;
    :deep(.arco-trigger-popup) {
      left: 0 !important;
    }
  }
  .select-field-container {
    position: relative;
    width: 100%;
    :deep(.arco-trigger-popup) {
      left: 0 !important;
    }
  }
  .arco-tabs {
    overflow: visible;
  }
  .d-a-tabs {
    :deep(.arco-tabs-content) {
      overflow: visible !important;
      .arco-tabs-content-item  {
        overflow: visible;
      }
    }
  }
</style>
