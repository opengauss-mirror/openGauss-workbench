<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="dData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.base.InsertOperator.5m7hqzngdeg0')">
      <div class="tab-content d-a-form">
        <a-form-item :label="$t('modeling.base.InsertOperator.5m7hqzngebw0')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <div class="select-comp-container dy-select-container">
          <a-select popup-container=".select-comp-container"
            v-model="config.table" :placeholder="$t('modeling.base.InsertOperator.5m7hqzngeho0')" allowSearch
            @change="(value: any) => selectChange(value, 'table')"
          >
            <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
            <overflow-tooltip :text="item.tablename" v-for="(item, key) in tableList" :key="key" :content="item.tablename">
              <a-option  class="dianayako_select-option-disabled"   :value="item.tablename" :disabled="checkDisabled(useTable, item.tablename)">{{ item.tablename }}</a-option>
            </overflow-tooltip>
          </a-select>
          </div>
        </a-form-item>
        <div class="list-type-1">
          <div class="list-frame mb-s" :class="{'list-frame-close':list1Item.toggleClose?true:false}" v-for="(list1Item, key) in config.list1" :key="`list1${key}`">
            <div class="d-form-item-label mb-s">
              <div class="toggle" @click="toggleList(list1Item)"><icon-down class="group-arrow" /></div>
              <div class="label-text label-color">{{$t('modeling.base.InsertOperator.5m7hqzngewg0')}}</div>
              <div class="d-control-add" @click="operateConfigList1('add-list1-row', key)">+</div>
              <a-button class="copy-btn" size="mini" type="primary" @click="copyRow(list1Item)">{{ $t('modeling.dy_common.detail.copy') }}</a-button>
              <div class="d-control-remove" @click="operateConfigList1('delete-list1', key)">-</div>
            </div>
            <a-row class="mb-s" v-for="(row, rowKey) in list1Item" :key="`list1Row${rowKey}`" align="center">
              <a-col :span="11" class="mr-xs">
                <div class="select-field-container dy-select-container">
                  <a-select popup-container=".select-field-container" v-model="row.field" :placeholder="$t('modeling.base.InsertOperator.5m7hqzngez40')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                  allowSearch style="width: 100%" @change="e => listRowChange(e, row)">
                  <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                  <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                      <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                    <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                      <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`" :disabled="checkDisabled(list1Item, `${group.group}.${item.name}`, 'field')">{{ item.name }}</a-option>
                    </overflow-tooltip>
                  </a-optgroup>
                </a-select>
                </div>
              </a-col>
              <a-col :span="11" class="mr-xs">
                <a-date-picker
                  v-if="isDatePicker(row)"
                  :placeholder="$t('modeling.dy_common.panel.datePlaceholder')"
                  v-model="row.value"
                  style="width: 220px;"
                  allow-clear
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  @change="save('list1', config.list1)"
                />
                <a-input v-else v-model="row.value" :placeholder="$t('modeling.base.InsertOperator.5m7hqzngf200')" @blur="save('list1', config.list1)"></a-input>
              </a-col>
              <a-col :span="1">
                <div class="d-control-remove" @click="operateConfigList1('delete-list1-row', key, rowKey)">-</div>
              </a-col>
            </a-row>
          </div>
          <div class="flex-right" style="margin-top: 10px;">
            <a-button type="primary" @click="operateConfigList1('add-list1')">{{$t('modeling.base.InsertOperator.5m7hqzngf4k0')}}</a-button>
          </div>
        </div>
      </div>
    </a-tab-pane>
  </a-tabs>
  <a-modal
    :visible="warning.show"
    :ok-loading="warning.loading"
    :closable="false"
    simple
    :ok-text="$t('modeling.base.InsertOperator.5m7hqzngfgw0')"
    :cancel-text="$t('modeling.base.InsertOperator.5m7hqzngfjg0')"
    @ok="warningOk"
    @cancel="warningClose"
  >
    <div class="warning">
      <div class="title"><icon-exclamation-circle-fill color="#ff7d00" />{{$t('modeling.base.InsertOperator.5m7hqzngfls0')}}</div>
      <div class="content">{{$t('modeling.base.InsertOperator.5m7hqzngfoo0')}}</div>
    </div>
  </a-modal>
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
  table: '',
  list1: [] as { field: string, value: string, type: string, toggleClose?: any }[][],
  modal1: { field: '', value: '', toggleClose: false, type: '' }
})
const tableList = computed<KeyValue[]>(() => dFStore.getTableSelectList)
const fieldsList = computed(() => dFStore.getFieldsSelectList)
const init = (pCell: Cell) => {
  cell = pCell
  let { data } = cell
  config.table = data.table ? data.table : ''
  warning.oldValue = data.table ? data.table : ''
  config.list1 = data.list1 ? data.list1 : []
}
const save = (type: string, data: any) => {
  cell && saveData(type, data, cell)
}
const selectChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[], prop: any, other?: any) => {
  if (prop === 'table') {
    warningOpen()
  }
}
const operateConfigList1 = (type: string, key1?: number, key2?: number) => {
  if (type === 'add-list1') config.list1.push([])
  else if (type === 'delete-list1' && (key1 || key1 === 0)) config.list1.splice(key1, 1)
  else if (type === 'add-list1-row' && (key1 || key1 === 0))config.list1[key1].push(JSON.parse(JSON.stringify(config.modal1)))
  else if (type === 'delete-list1-row' && (key1 || key1 === 0) && (key2 || key2 === 0)) config.list1[key1].splice(key2, 1)
  save('list1', config.list1)
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
    save('table', config.table)
  }).catch(() => {
    warning.loading = false
    warning.oldValue = JSON.parse(JSON.stringify(config.table))
    warningClose(true)
    save('table', config.table)
  })
}
const toggleList = (row: any) => {
  row.toggleClose = row.toggleClose ? false : true
}
const copyRow = (row: any) => {
  config.list1.push(JSON.parse(JSON.stringify(row)))
  save('list1', config.list1)
}
const isDatePicker = (row: any) => {
  if (row.type && typeof row.type === 'string' && (row.type.includes('time') || row.type.includes('date'))) {
    return true
  } else {
    return false
  }
}
const listRowChange: any = (e: any, row: any) => {
  fieldsList.value.forEach((group: any) => {
    if (group && group.fields) {
      group.fields.forEach((item: any) => {
        if (`${group.group}.${item.name}` === e) {
          if (item.type) row.type = item.type
        }
      })
    }
  })
  save('list1', config.list1)
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
  .list-frame-close {
    .toggle {
      transform: rotate(-90deg);
    }
    height: 35px !important;
    overflow: hidden;
  }
  .toggle {
    margin-right: 10px;
    cursor: pointer;
    font-size: 16px;
    transition: all .3s;
  }
  .copy-btn {
    margin-left: auto;
    margin-right: 10px;
  }
  .d-control-remove {
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
