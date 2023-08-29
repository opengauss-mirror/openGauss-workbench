
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.combination.GroupOperator.5me6gg5ogk00')">
      <div class="tab-content d-a-form">
        <div class="d-form-item-label mb-s">
          <div class="label-text label-color">{{$t('modeling.combination.GroupOperator.5me6gg5oh8c0')}}</div>
          <div class="d-control-add" @click="operateGroup('add')">+</div>
        </div>
        <a-row align="center" class="mb-s" v-for="(item, key) in iData.groups" :key="`item${key}`">
          <a-col :span="21" class="mr-xs">
            <a-select v-model="item.field" :placeholder="$t('modeling.combination.GroupOperator.5me6gg5ohjk0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
              showSearch style="width: 100%;" @change="save">
                <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                  <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                  <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                    <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`" :disabled="checkDisabled(iData.groups, item.name, 'field')" :label="item.name"></a-option>
                  </overflow-tooltip>
                </a-optgroup>
            </a-select>
          </a-col>
          <a-col :span="1">
            <div class="d-control-remove" @click="operateGroup('delete', key)">-</div>
          </a-col>
        </a-row>
      </div>
    </a-tab-pane>
  </a-tabs>
</template>
<script setup lang="ts">
import { reactive, computed } from 'vue'
import {
  Tabs as ATabs, TabPane as ATabPane, Select as ASelect, Row as ARow, Col as ACol
  } from '@arco-design/web-vue'
import { saveData, checkDisabled } from '../../utils/tool'
import { Cell } from '@antv/x6'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { KeyValue } from '@antv/x6/lib/types'
const AOption = ASelect.Option
const AOptgroup = ASelect.OptGroup
const dFStore = useDataFlowStore()
const fieldsList = computed(() => dFStore.getFieldsSelectList)
const save = () => saveData('groups', iData.groups, iData.cell)
const iData: KeyValue = reactive({
  activeKey: `1`, cell: null,
  groups: []
})
const init = (cell: Cell) => {
  iData.cell = cell
  let { data } = cell
  iData.groups = data.groups ? data.groups : []
}
const operateGroup = (type: string, key?: number) => {
  if (type === 'add') iData.groups.push({ fields: null })
  else iData.groups.splice(key, 1)
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
