
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.combination.BridgingOperator.5m82a7hpfow0')">
      <div class="tab-content d-a-form">
        <a-row class="mb-s">
          <a-form-item :label="$t('modeling.combination.BridgingOperator.5m82a7hpivk0')" style="margin-bottom: 0">
            <a-select
              v-model="iData.table" :placeholder="$t('modeling.combination.BridgingOperator.5m82a7i01wc0')" allowSearch :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
              @change="tableChange"
            >
              <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
              <overflow-tooltip :text="item.tablename" v-for="(item, key) in tableList" :key="key" :content="item.tablename">
                <a-option  class="dianayako_select-option-disabled"   :value="item.tablename" :disabled="checkDisabled(useTable, item.tablename)">{{ item.tablename }}</a-option>
              </overflow-tooltip>
            </a-select>
          </a-form-item>
        </a-row>
        <a-row class="mb-s">
          <a-form-item :label="$t('modeling.combination.BridgingOperator.5m82a7i02a80')" style="margin-bottom: 0">
            <a-select
              v-model="iData.connectType" :options="select.connectType" :placeholder="$t('modeling.combination.BridgingOperator.5m82a7i02a80')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
              @change="saveData('connectType', iData.connectType, iData.cell)"
            />
          </a-form-item>
        </a-row>
        <div class="d-form-item-label mb-s">
          <div class="label-text label-color">{{$t('modeling.combination.BridgingOperator.5m82a7i02f40')}}</div>
          <div class="d-control-add" @click="orOperate('add')">+</div>
        </div>
        <div class="or-frame">
          <div class="or-item" v-for="(orList, orKey) in iData.condition" :key="`or${orKey}`">
            <div class="or-item-ortext label-color" v-if="orKey !== 0">{{$t('modeling.combination.BridgingOperator.5m82a7i02kw0')}}</div>
            <div class="d-frame">
              <div class="d-form-item-label mb-s">
                <div class="label-text label-color">{{$t('modeling.combination.BridgingOperator.5m82a7i02p80')}}</div>
                <div class="d-control-add" @click="orOperate('addAnd', orKey)">+</div>
                <div class="d-control-remove" @click="orOperate('delete', orKey)">-</div>
              </div>
              <a-row align="center" class="mb-s" v-for="(item, key) in orList" :key="`item${key}`">
                <a-col :span="8" class="mr-xs">
                  <a-select v-model="item.field" :placeholder="$t('modeling.combination.BridgingOperator.5m82a7i02t40')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                    showSearch style="width: 100%;" @change="saveData('condition', iData.condition, iData.cell)">
                    <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                    <a-optgroup v-for="(group, groupKey) in mainTableFieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
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
                    :placeholder="$t('modeling.combination.BridgingOperator.5m82a7i02xc0')" style="width: 100%" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                  />
                </a-col>
                <a-col :span="8" class="mr-xs">
                  <a-select
                    v-model="item.value"
                    allow-create
                    :placeholder="$t('modeling.combination.BridgingOperator.5m82a7i039g0')"
                    :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                    showSearch style="width: 100%;"
                    @change="conditionValueChange(item)"
                    @input-value-change="onInputOption"
                    v-if="(item.condition !== 'notNull') && (item.condition !== 'isNull')"
                  >
                    <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                    <overflow-tooltip :text="item.name" v-for="(item, key) in joinTableFieldsList" :key="`field${key}`" :content="`${item.name}`">
                      <a-option  class="dianayako_select-option-disabled"   :value="`${iData.table}.${item.name}`" :label="item.name"></a-option>
                    </overflow-tooltip>
                  </a-select>
                </a-col>
                <a-col :span="1">
                  <div class="d-control-remove" @click="orOperate('deleteAnd', orKey, key)">-</div>
                </a-col>
              </a-row>
            </div>
          </div>
        </div>
      </div>
    </a-tab-pane>
  </a-tabs>
</template>
<script setup lang="ts">
import { reactive, computed } from 'vue'
import {
  Tabs as ATabs, TabPane as ATabPane, FormItem as AFormItem, Select as ASelect, Row as ARow, Col as ACol
  } from '@arco-design/web-vue'
import { saveData, checkDisabled } from '../../utils/tool'
import { KeyValue } from '@/api/modeling/request'
import { Cell } from '@antv/x6'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const AOption = ASelect.Option
const AOptgroup = ASelect.OptGroup
const dFStore = useDataFlowStore()
const useTable = computed(() => dFStore.getUseTable)
const select: KeyValue = computed(() => ({
  connectType: [
    { operate: 'join', value: 'innerJoin', label: t('modeling.combination.BridgingOperator.5m82a7i03lk0'), desc: t('modeling.combination.BridgingOperator.5m82a7i03qs0') },
    { operate: 'left join', value: 'leftJoin', label: t('modeling.combination.BridgingOperator.5m82a7i03w00'), desc: t('modeling.combination.BridgingOperator.5m82a7i041g0') },
    { operate: 'right join', value: 'rightJoin', label: t('modeling.combination.BridgingOperator.5m82a7i044w0'), desc: t('modeling.combination.BridgingOperator.5m82a7i047k0') },
    { operate: 'full join', value: 'fullJoin', label: t('modeling.combination.BridgingOperator.5m82a7i04bk0'), desc: t('modeling.combination.BridgingOperator.5m82a7i04ec0') }
  ],
  condition: [
    { value: 'equal', label: t('modeling.combination.BridgingOperator.5m82a7i04k00') },
    { value: 'notEqual', label: t('modeling.combination.BridgingOperator.5m82a7i04nk0') },
    { value: 'lessThan', label: t('modeling.combination.BridgingOperator.5m82a7i04ro0') },
    { value: 'equalLessThan', label: t('modeling.combination.BridgingOperator.5m82a7i04ug0') },
    { value: 'greaterThan', label: t('modeling.combination.BridgingOperator.5m82a7i04x80') },
    { value: 'equalGreaterThan', label: t('modeling.combination.BridgingOperator.5m82a7i04z80') },
    { value: 'include', label: t('modeling.combination.BridgingOperator.5m82a7i052o0') },
    { value: 'notInclude', label: t('modeling.combination.BridgingOperator.5m82a7i054o0') },
    { value: 'isNull', label: t('modeling.combination.BridgingOperator.5m82a7i056w0') },
    { value: 'notNull', label: t('modeling.combination.BridgingOperator.5m82a7i058w0') }
  ]
}))
const tableList: any = computed(() => dFStore.getTableSelectList)
const mainTableFieldsList = computed(() => dFStore.getFieldsSelectList)
const joinTableFieldsList = computed(() => dFStore.getFieldsByTable(iData.table))
const iData: KeyValue = reactive({
  activeKey: `1`, cell: null, prevTable: '',
  table: '', connectType: '', condition: []
})
const init = (cell: Cell) => {
  iData.cell = cell
  let { data } = cell
  iData.table = data.table ? data.table : ''
  iData.connectType = data.connectType ? data.connectType : ''
  iData.condition = data.condition ? data.condition : ''
  iData.prevTable = data.table ? data.table : ''
}
const tableChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[]) => {
  let index = -1
  if (iData.prevTable && iData.prevTable !== value) index = useTable.value.indexOf(iData.prevTable)
  iData.prevTable = value
  index === -1 ? dFStore.setDatabaseTableInfo(value) : dFStore.setDatabaseTableInfo(value, { tableKey: index })
  iData.condition = []
  saveData('table', iData.table, iData.cell)
}
let isInputOption = false
const conditionValueChange = (item: KeyValue) => {
  item.valueType = isInputOption ? 'string' : 'field'
  saveData('condition', iData.condition, iData.cell)
}
const onInputOption = (e: string) => isInputOption = e ? true : false
const changeCondition = (item: KeyValue) => {
  if (item.condition === t('modeling.combination.BridgingOperator.5m82a7i058w0') || item.condition === t('modeling.combination.BridgingOperator.5m82a7i056w0')) item.value = 'null/empty'
  else item.value = ''
  saveData('condition', iData.condition, iData.cell)
}
const orOperate = (type: string, key1?: number, key2?: number) => {
  if (type === 'add') iData.condition.push([])
  else if (type === 'delete') iData.condition.splice(key1, 1)
  else if (type === 'addAnd' && (key1 === 0 || key1)) iData.condition[key1].push({ field: null, condition: null, value: '', valueType: 'string' })
  else if (type === 'deleteAnd' && (key1 === 0 || key1) && (key2 === 0 || key2)) iData.condition[key1].splice(key2, 1)
  saveData('condition', iData.condition, iData.cell)
}
defineExpose({ init })
</script>
<style scoped lang="less">
  .tab-content {
    padding: 0 10px;
    .table {
      position: relative;
      box-sizing: border-box;
      padding: 15px 0 5px;
      margin-bottom: 15px;
      width: 100%;
      border-radius: 4px;
      border-bottom: 1px solid red;
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
