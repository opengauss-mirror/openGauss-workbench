
<template>
  <a-tabs class="d-a-tabs" v-model:activeKey="iData.activeKey" centered size="small" :tabBarGutter="40" >
    <a-tab-pane key="1" :title="$t('modeling.combination.MappingOperator.5me6giyzpck0')">
      <div class="tab-content d-a-form">
        <div class="d-form-item-label mb-s">
          <div class="label-text label-color">{{$t('modeling.combination.MappingOperator.5me6giyzq4s0')}}</div>
          <div class="d-control-add" @click="operateMapping('add')">+</div>
        </div>
        <a-row align="center" class="mb-s" v-for="(item, key) in iData.mappings" :key="`item${key}`">
          <a-col :span="8" class="mr-xs">
            <a-select v-model="item.field" :placeholder="$t('modeling.combination.MappingOperator.5me6giyzqds0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
              showSearch style="width: 100%;" @change="save">
              <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
              <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                  <a-option class="dianayako_select-option-disabled" :value="`${group.group}.${item.name}`" :disabled="checkDisabled(iData.polymerization, `${group.group}.${item.name}`, 'field')">{{ item.name }}</a-option>
                </overflow-tooltip>
              </a-optgroup>
            </a-select>
          </a-col>
          <a-col :span="5" class="mr-xs">
            <a-select
              v-model="item.condition" :options="select.condition" @change="save"
              :placeholder="$t('modeling.combination.MappingOperator.5me6giyzqmo0')" style="width: 100%" dropdownClassName="d-a-select-dropdown"
            />
          </a-col>
          <a-col :span="7" class="mr-xs">
            <a-input :max-length="140" show-word-limit  v-model="item.value" :placeholder="$t('modeling.combination.PolymerizationOperator.5m82b2nqt9s0')" @blur="save('pd1', key)" />
          </a-col>
          <a-col :span="1">
            <div class="d-control-remove" @click="operateMapping('delete', key)">-</div>
          </a-col>
        </a-row>
      </div>
    </a-tab-pane>
  </a-tabs>
</template>
<script setup lang="ts">
import { reactive, computed } from 'vue'
import {
  Tabs as ATabs, TabPane as ATabPane, Select as ASelect, Row as ARow, Col as ACol, Input as AInput
  } from '@arco-design/web-vue'
import { checkDisabled, saveData } from '../../utils/tool'
import { Cell } from '@antv/x6'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { KeyValue } from '@antv/x6/lib/types'
const AOption = ASelect.Option
const AOptgroup = ASelect.OptGroup
const dFStore = useDataFlowStore()
const select: KeyValue = {
  condition: [
    { value: 'as', label: 'as' }
  ]
}
const fieldsList = computed(() => dFStore.getFieldsSelectList)
const save: any = (type?: string, key?: number) => {
  if (type && type === 'pd1' && key !== undefined) {
    let item = iData.mappings[key]
    if (iData.mappings.findIndex((item2: any, key2: any) => (key !== key2 && item.value === item2.value)) !== -1) {
      item.value = `${item.value}${key}`
    }
  }
  saveData('mappings', iData.mappings, iData.cell)
}
const iData: KeyValue = reactive({
  activeKey: `1`, cell: null,
  mappings: []
})
const init = (cell: Cell) => {
  iData.cell = cell
  let { data } = cell
  iData.mappings = data.mappings ? data.mappings : []
}
const operateMapping = (type: string, key?: number) => {
  if (type === 'add') iData.mappings.push({ field: null, condition: null, value: '' })
  else iData.mappings.splice(key, 1)
  save()
}
defineExpose({ init })
</script>
<style scoped lang="less">
  .tab-content {
    padding: 0 10px;
    .table {
      border: 1px solid #a2a8b3;
      position: relative;
      box-sizing: border-box;
      padding: 15px 10px;
      margin-bottom: 10px;
      width: 100%;
      border-radius: 4px;
      > .d-control-remove {
        position: absolute;
        right: 10px;
        top: 0;
        background-color: #0a1936;
        transform: translateY(-50%);
      }
    }
  }
</style>
