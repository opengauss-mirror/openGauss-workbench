
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.combination.PolymerizationOperator.5m82b2nqsa00')">
      <div class="tab-content d-a-form">
        <div class="d-form-item-label mb-s">
          <div class="label-text label-color">{{$t('modeling.combination.PolymerizationOperator.5m82b2nqt0k0')}}</div>
          <div class="d-control-add" @click="operatePolymerization('add')">+</div>
        </div>
        <a-row align="center" class="mb-s" v-for="(item, key) in iData.polymerization" :key="`item${key}`">
          <a-col :span="7" class="mr-xs">
            <a-select v-model="item.inputType" :options="inputTypeOptions" @change="save" style="width: 100%" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"></a-select>
          </a-col>
          <a-col :span="17" style="margin-bottom: 3px;"></a-col>
          <a-col :span="10" style="margin-bottom: 3px;" class="mr-xs">
            <a-select v-if="item.inputType === 'queryField'" v-model="item.field" :placeholder="$t('modeling.combination.PolymerizationOperator.5m82b2nqt4k0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                      showSearch style="width: 100%;" @change="save">
              <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                  <a-option class="dianayako_select-option-disabled" :value="`${group.group}.${item.name}`" :disabled="checkDisabled(iData.polymerization, `${group.group}.${item.name}`, 'field')">{{ item.name }}</a-option>
                </overflow-tooltip>
              </a-optgroup>
            </a-select>
            <a-input v-else-if="item.inputType === 'customSql'" v-model="item.field" :placeholder="$t('modeling.combination.PolymerizationOperator.5m82b2nqt4k0')" @blur="save" style="width: 100%;"></a-input>
          </a-col>
          <a-col :span="4" class="mr-xs">
            <a-select
              v-model="item.way" :options="select.type" @change="save"
              :placeholder="$t('modeling.combination.PolymerizationOperator.5m82b2nqt780')" style="width: 100%" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
            />
          </a-col>
          <a-col :span="7" class="mr-xs">
            <a-input :max-length="140" show-word-limit  v-model="item.alias" :placeholder="$t('modeling.combination.PolymerizationOperator.5m82b2nqt9s0')" @blur="save('pd1', key)" />
          </a-col>
          <a-col :span="1">
            <div class="d-control-remove" @click="operatePolymerization('delete', key)">-</div>
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
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { KeyValue } from '@antv/x6/lib/types'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const AOption = ASelect.Option
const AOptgroup = ASelect.OptGroup
const dFStore = useDataFlowStore()
const useTable = computed(() => dFStore.getUseTable)
const select: KeyValue = computed(() => ({
  type: [
    { value: 'default', label: t('modeling.combination.PolymerizationOperator.5m82b2nqtcl0') },
    { value: 'max', label: t('modeling.combination.PolymerizationOperator.5m82b2nqtck0') },
    { value: 'min', label: t('modeling.combination.PolymerizationOperator.5m82b2nqtck1') },
    { value: 'sum', label: t('modeling.combination.PolymerizationOperator.5m82b2nqtf40') },
    { value: 'avg', label: t('modeling.combination.PolymerizationOperator.5m82b2nqths0') },
    { value: 'count', label: t('modeling.combination.PolymerizationOperator.5m82b2nqths1') },
    { value: 'year', label: t('modeling.combination.PolymerizationOperator.5m82b2nqths2') },
    { value: 'month', label: t('modeling.combination.PolymerizationOperator.5m82b2nqths3') }
  ],
  inputType: inputTypeOptions,
}))

const inputTypeOptions = [
  { value: 'queryField', label: '查询字段' },
  { value: 'customSql', label: '自定义语句' }
];

const fieldsList = computed(() => dFStore.getFieldsSelectList)
const save: any = (type?: string, key?: number) => {
  if (type && type === 'pd1' && key !== undefined) {
    let item = iData.polymerization[key]
    if (iData.polymerization.findIndex((item2: any, key2: any) => (key !== key2 && item.alias === item2.alias)) !== -1) {
      item.alias = `${item.alias}${key}`
    }
  }
  saveData('polymerization', iData.polymerization, iData.cell)
}
const iData: KeyValue = reactive({
  activeKey: `1`, cell: null,
  polymerization: []
})
const init = (cell: Cell) => {
  iData.cell = cell
  let { data } = cell
  iData.polymerization = data.polymerization ? data.polymerization.map((item: any) => ({ ...item, inputType: item.inputType || 'queryField' })) : [];
}
const operatePolymerization = (type: string, key?: number) => {
  if (type === 'add') iData.polymerization.push({ field: null, way: null, alias: '', inputType: 'queryField' })
  else iData.polymerization.splice(key, 1)
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
