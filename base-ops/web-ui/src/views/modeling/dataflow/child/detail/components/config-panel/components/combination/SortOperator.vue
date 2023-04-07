
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.combination.SortOperator.5m82bdo2r0w0')">
      <div class="tab-content d-a-form">
        <a-row>
          <a-col :span="24">
            <div class="d-tips">
              <icon-exclamation-circle-fill />
              {{$t('modeling.dy_common.panel.sortTip1')}}
            </div>
          </a-col>
        </a-row>
        <div class="d-form-item-label mb-s">
          <div class="label-text label-color">{{$t('modeling.combination.SortOperator.5m82bdo2rns0')}}</div>
          <div class="d-control-add" @click="operateSorts('add')">+</div>
        </div>
        <a-row align="center" class="mb-s" v-for="(item, key) in iData.sorts" :key="`item${key}`">
          <a-col :span="10" class="mr-xs">
            <a-select v-model="item.field" :placeholder="$t('modeling.combination.SortOperator.5m82bdo2rs00')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
              showSearch style="width: 100%;" @change="save">
              <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
              <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                  <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`" :label="item.name"></a-option>
                </overflow-tooltip>
              </a-optgroup>
            </a-select>
          </a-col>
          <a-col :span="10" class="mr-xs">
            <a-select
              v-model="item.value" :options="select.sorts" @change="save"
              :placeholder="$t('modeling.combination.SortOperator.5m82bdo2rus0')" style="width: 100%" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
            />
          </a-col>
          <a-col :span="1">
            <div class="d-control-remove" @click="operateSorts('delete', key)">-</div>
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
import { saveData } from '../../utils/tool'
import { Cell } from '@antv/x6'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { KeyValue } from '@antv/x6/lib/types'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const AOption = ASelect.Option
const AOptgroup = ASelect.OptGroup
const dFStore = useDataFlowStore()
const fieldsList = computed(() => dFStore.getFieldsSelectList)
const select: KeyValue = computed(() => ({
  sorts: [
    { value: 'asc', label: t('modeling.combination.SortOperator.5m82bdo2rx80') },
    { value: 'des', label: t('modeling.combination.SortOperator.5m82bdo2rzo0') },
    { value: 'phoneticize', label: t('modeling.combination.SortOperator.5m82bdo2s2c0') }
  ]
}))
const save = () => saveData('sorts', iData.sorts, iData.cell)
const iData: KeyValue = reactive({
  activeKey: `1`, cell: null,
  sorts: []
})
const init = (cell: Cell) => {
  iData.cell = cell
  let { data } = cell
  iData.sorts = data.sorts ? data.sorts : []
}
const operateSorts = (type: string, key?: number) => {
  if (type === 'add') iData.sorts.push({ field: null, value: null })
  else iData.sorts.splice(key, 1)
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
  .d-tips {
    color: var(--color-neutral-6);
    margin-bottom: 10px;
  }
</style>
