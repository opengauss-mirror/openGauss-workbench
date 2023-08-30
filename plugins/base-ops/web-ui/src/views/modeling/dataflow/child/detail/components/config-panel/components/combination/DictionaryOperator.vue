
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="dData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.combination.DictionaryOperator.5me6g9wp5p40')">
      <div class="tab-content d-a-form">
        <a-form-item :label="$t('modeling.combination.DictionaryOperator.5me6g9wp6xk0')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <a-select
            v-model="config.table" :placeholder="$t('modeling.combination.DictionaryOperator.5me6g9wp74g0')" allowSearch
            @change="(value: any) => selectChange(value, 'table')"
          >
            <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
            <overflow-tooltip :text="item.tablename" v-for="(item, key) in tableList" :key="key" :content="item.tablename">
              <a-option  class="dianayako_select-option-disabled"   :value="item.tablename" :disabled="checkDisabled(useTable, item.tablename)">{{ item.tablename }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('modeling.combination.DictionaryOperator.5me6g9wp78o0')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <a-select v-model="config.field" :placeholder="$t('modeling.combination.DictionaryOperator.5me6g9wp7cg0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
            allowSearch style="width: 100%" @change="save('field', config.field)">
            <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
            <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
              <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
              <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`" :disabled="checkDisabled([config.field], `${group.group}.${item.name}`)" :label="item.name"></a-option>
              </overflow-tooltip>
            </a-optgroup>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('modeling.combination.DictionaryOperator.5me6g9wp7gw0')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <a-select v-model="config.matchField" :placeholder="$t('modeling.combination.DictionaryOperator.5me6g9wp7cg0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
            allowSearch style="width: 100%" @change="save('matchField', config.matchField)">
            <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
            <overflow-tooltip :text="field.name" v-for="(field, key) in tableFields(config.table)" :key="`field${key}`" :content="`${field.name}`">
              <a-option  class="dianayako_select-option-disabled"   :value="`${config.table}.${field.name}`" :disabled="checkDisabled([config.matchField], `${config.table}.${field.name}`)" :label="field.name"></a-option>
            </overflow-tooltip>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('modeling.combination.DictionaryOperator.5me6g9wp7lc0')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <a-select v-model="config.rigidField" :placeholder="$t('modeling.combination.DictionaryOperator.5me6g9wp7cg0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
            allowSearch style="width: 100%" @change="save('rigidField', config.rigidField)">
            <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
            <overflow-tooltip :text="field.name" v-for="(field, key) in tableFields(config.table)" :key="`field${key}`" :content="`${field.name}`">
              <a-option  class="dianayako_select-option-disabled"   :value="`${config.table}.${field.name}`" :disabled="checkDisabled([config.rigidField], `${config.table}.${field.name}`)" :label="field.name"></a-option>
            </overflow-tooltip>
          </a-select>
        </a-form-item>
      </div>
    </a-tab-pane>
  </a-tabs>
</template>
<script setup lang="ts">
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { Cell } from '@antv/x6'
import { KeyValue } from '@antv/x6/lib/types'
import { computed, reactive } from 'vue'
import { saveData, checkDisabled } from '../../utils/tool'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
const dFStore = useDataFlowStore()
const useTable = computed(() => dFStore.getUseTable)
let cell: Cell | null = null
const dData = reactive({
  activeKey: `1`
})
const config = reactive({
  table: '', field: '', matchField: '', rigidField: ''
})
const tableList = computed<KeyValue[]>(() => dFStore.getTableSelectList)
const fieldsList = computed(() => dFStore.getFieldsSelectList)
const tableFields = (table: string) => {
  return dFStore.getFieldsByTable(table)
}
const init = (pCell: Cell) => {
  cell = pCell
  let { data } = cell
  config.table = data.table ? data.table : ''
  config.field = data.field ? data.field : ''
  config.matchField = data.matchField ? data.matchField : ''
  config.rigidField = data.rigidField ? data.rigidField : ''
}
const save = (type: string, data: any) => {
  cell && saveData(type, data, cell)
}
const selectChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[], prop: any) => {
  if (prop === 'table') {
    dFStore.setDatabaseTableInfo(value, { noUse: true })
    config.matchField = ''
    config.rigidField = ''
    save('table', value)
  }
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
  .update-field-frame {
    border: 1px solid #f0f0f0;
    box-sizing: border-box;
    padding: 5px;
  }
}
.list-frame {
  border: 1px solid #f0f0f0;
  box-sizing: border-box;
  padding: 5px;
  border-radius: 4px;
}
.d-control-remove {
  margin-left: auto;
}
</style>
