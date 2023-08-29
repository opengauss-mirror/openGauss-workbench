
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.combination.ConditionOperator.5m82aq4s7qo0')">
      <div class="tab-content d-a-form">
        <div class="or-frame" v-show="iData.or.length > 0">
          <div class="or-item" v-for="(orList, orKey) in iData.or" :key="`or${orKey}`">
            <div class="or-item-ortext label-color" v-if="orKey !== 0">{{$t('modeling.combination.ConditionOperator.5m82aq4s8cw0')}}</div>
            <div class="d-frame">
              <div class="d-form-item-label mb-s">
                <div class="label-text label-color">{{$t('modeling.combination.ConditionOperator.5m82aq4s8gw0')}}</div>
                <div class="d-control-add" @click="orOperate('addAnd', orKey)">+</div>
                <div class="d-control-remove" @click="orOperate('delete', orKey)">-</div>
              </div>
              <a-row align="center" class="mb-s" v-for="(item, key) in orList" :key="`item${key}`">
                <a-col :span="8" class="mr-xs">
                  <a-select v-model="item.field" :placeholder="$t('modeling.combination.ConditionOperator.5m82aq4s8js0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                    showSearch style="width: 100%;" @change="saveData('or', iData.or, iData.cell)">
                    <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                    <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                      <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                      <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                        <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`" :label="item.name"></a-option>
                      </overflow-tooltip>
                    </a-optgroup>
                  </a-select>
                </a-col>
                <a-col :span="6" class="mr-xs">
                  <a-select
                    v-model="item.condition" :options="select.condition" @change="changeCondition(item)"
                    :placeholder="$t('modeling.combination.ConditionOperator.5m82aq4s8m40')" style="width: 100%" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                  />
                </a-col>
                <a-col :span="8" class="mr-xs">
                  <a-input
                    :max-length="140"
                    show-word-limit
                    v-model="item.value"
                    :placeholder="$t('modeling.combination.ConditionOperator.5m82aq4s8ow0')"
                    @blur="saveData('or', iData.or, iData.cell)"
                    v-show="(item.condition !== 'notNull') && (item.condition !== 'isNull')"
                  />
                </a-col>
                <a-col :span="1">
                  <div class="d-control-remove" @click="orOperate('deleteAnd', orKey, key)">-</div>
                </a-col>
              </a-row>
            </div>
          </div>
        </div>
        <div class="flex-right" style="margin-top: 10px;">
          <a-button type="primary" @click="orOperate('add')">{{$t('modeling.combination.ConditionOperator.5m82aq4s8rc0')}}</a-button>
        </div>
      </div>
    </a-tab-pane>
  </a-tabs>
</template>
<script setup lang="ts">
import { reactive, computed } from 'vue'
import {
  Tabs as ATabs, TabPane as ATabPane, Select as ASelect,
  Input as AInput, Row as ARow, Col as ACol, Button as AButton
  } from '@arco-design/web-vue'
import { saveData } from '../../utils/tool'
import { Cell } from '@antv/x6'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { KeyValue } from '@antv/x6/lib/types'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const AOption = ASelect.Option
const AOptgroup = ASelect.OptGroup
const dFStore = useDataFlowStore()
const select: KeyValue = computed(() => ({
  connectType: [{ value: t('modeling.combination.ConditionOperator.5m82aq4s8ts0'), label: t('modeling.combination.ConditionOperator.5m82aq4s8ts0') }],
  condition: [
    { value: 'equal', label: t('modeling.combination.ConditionOperator.5m82aq4s8wc0') },
    { value: 'notEqual', label: t('modeling.combination.ConditionOperator.5m82aq4s8yo0') },
    { value: 'lessThan', label: t('modeling.combination.ConditionOperator.5m82aq4s9180') },
    { value: 'equalLessThan', label: t('modeling.combination.ConditionOperator.5m82aq4s93k0') },
    { value: 'greaterThan', label: t('modeling.combination.ConditionOperator.5m82aq4s95s0') },
    { value: 'equalGreaterThan', label: t('modeling.combination.ConditionOperator.5m82aq4s98c0') },
    { value: 'include', label: t('modeling.combination.ConditionOperator.5m82aq4s9ao0') },
    { value: 'notInclude', label: t('modeling.combination.ConditionOperator.5m82aq4s9cw0') },
    { value: 'isNull', label: t('modeling.combination.ConditionOperator.5m82aq4s9fk0') },
    { value: 'notNull', label: t('modeling.combination.ConditionOperator.5m82aq4s9i00') },
    { value: 'in', label: t('modeling.combination.ConditionOperator.5m82aq4s9fa1') },
    { value: 'notIn', label: t('modeling.combination.ConditionOperator.5m82aq4s9fa2') },
    { value: 'lengthLessThan', label: t('modeling.combination.ConditionOperator.5m82aq4s9fk1') },
    { value: 'lengthLongerThan', label: t('modeling.combination.ConditionOperator.5m82aq4s9fk2') }
  ]
}))
const fieldsList = computed(() => dFStore.getFieldsSelectList)


const iData: KeyValue = reactive({
  activeKey: `1`, cell: null,
  or: []
})
const init = (cell: Cell) => {
  iData.cell = cell
  let { data } = cell
  iData.or = data.or && data.or.length !== 0 ? data.or : [[]]
}
const orOperate = (type: string, key1?: number, key2?: number) => {
  if (type === 'add') iData.or.push([])
  else if (type === 'delete') iData.or.splice(key1, 1)
  else if (type === 'addAnd' && (key1 === 0 || key1)) iData.or[key1].push({ field: null, condition: null, value: '' })
  else if (type === 'deleteAnd' && (key1 === 0 || key1) && (key2 === 0 || key2)) iData.or[key1].splice(key2, 1)
  saveData('or', iData.or, iData.cell)
}
const changeCondition = (item: KeyValue) => {
  if (item.condition === t('modeling.combination.ConditionOperator.5m82aq4s9i00')) item.value = t('modeling.combination.ConditionOperator.5m82aq4s9i00')
  else item.value = ''
  saveData('or', iData.or, iData.cell)
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
  .d-frame {
    border: 1px solid #f0f0f0;
    box-sizing: border-box;
    padding: 5px;
    border-radius: 4px;
  }
  .or-frame {
    .or-item {
      .or-item-ortext {
        margin-top: 5px;
        margin-bottom: 5px;
      }
    }
    .d-frame {
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
  .d-control-remove {
    margin-left: auto;
  }
</style>
