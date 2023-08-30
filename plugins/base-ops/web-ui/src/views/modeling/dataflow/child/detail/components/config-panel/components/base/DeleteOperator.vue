<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="dData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.base.DeleteOperator.5m7hqy836z40')">
      <div class="tab-content d-a-form">
        <a-form-item :label="$t('modeling.base.DeleteOperator.5m7hqy838600')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <div class="select-comp-container dy-select-container">
          <a-select popup-container=".select-comp-container"
            v-model="config.table" :placeholder="$t('modeling.base.DeleteOperator.5m7hqy838bg0')" allowSearch
            @change="(value: any) => selectChange(value, 'table')"
          >
            <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
            <overflow-tooltip :text="item.tablename" v-for="(item, key) in tableList" :key="key" :content="item.tablename">
              <a-option  class="dianayako_select-option-disabled"   :value="item.tablename" :disabled="checkDisabled(useTable, item.tablename)">{{ item.tablename }}</a-option>
            </overflow-tooltip>
          </a-select>
          </div>
        </a-form-item>
        <a-row>
          <a-col :span="3"></a-col>
          <a-col :span="18">
            <div class="d-tips">
              <icon-exclamation-circle-fill />
              {{$t('modeling.base.DeleteOperator.5maolasd5hk0')}}
            </div>
          </a-col>
        </a-row>
        <a-row class="mb-s" v-for="(row, rowKey) in config.fields" :key="`list2Row${rowKey}`" align="center">
          <a-col :span="22" class="mr-xs">
            <div class="select-comp-container dy-select-container">
              <a-select popup-container=".select-comp-container"
                v-model="config.fields[rowKey]" :placeholder="$t('modeling.base.DeleteOperator.5m7hqy838hc0')" :trigger-props="{ contentClass: 'd-a-select-dropdown' }"
                allowSearch style="width: 100%;"
                @change="save('fields', config.fields)">
                <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                  <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                  <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                    <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`" :disabled="checkDisabled(config.fields, `${group.group}.${item.name}`, 'value')">{{ item.name }}</a-option>
                  </overflow-tooltip>
                </a-optgroup>
              </a-select>
            </div>
          </a-col>
          <a-col :span="1">
            <div class="d-control-remove" @click="operateList('delete-list', rowKey)">-</div>
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
    :ok-text="$t('modeling.base.DeleteOperator.5m7hqy838kw0')"
    :cancel-text="$t('modeling.base.DeleteOperator.5m7hqy838no0')"
    @ok="warningOk"
    @cancel="warningClose"
  >
    <div class="warning">
      <div class="title"><icon-exclamation-circle-fill color="#ff7d00" />{{$t('modeling.base.DeleteOperator.5m7hqy838q40')}}</div>
      <div class="content">{{$t('modeling.base.DeleteOperator.5m7hqy838t40')}}</div>
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
  fields: [] as string[]
})
const tableList = computed<KeyValue[]>(() => dFStore.getTableSelectList)
const fieldsList = computed(() => dFStore.getFieldsSelectList)
const init = (pCell: Cell) => {
  cell = pCell
  let { data } = cell
  warning.oldValue = data.table ? data.table : ''
  config.table = data.table ? data.table : ''
  config.fields = data.fields ? data.fields : []
}
const save = (type: string, data: any) => {
  cell && saveData(type, data, cell)
}
const selectChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[], prop: any) => {
  if (prop === 'table') {
    warningOpen()
  }
}
const operateList = (type: string, key1?: number) => {
  if (type === 'add-list') config.fields.push('')
  else if (type === 'delete-list' && (key1 || key1 === 0)) config.fields.splice(key1, 1)
  save('fields', config.fields)
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
