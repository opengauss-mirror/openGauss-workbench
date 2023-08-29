
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="dData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.base.UpdateOperator.5m7hr2f43280')">
      <div class="tab-content d-a-form">
        <a-form-item :label="$t('modeling.base.UpdateOperator.5m7hr2f43ro0')" :labelCol="{ span: 6, offset: 0 }" labelAlign="left" :colon="false">
          <div class="select-comp-container dy-select-container">
          <a-select popup-container=".select-comp-container"
            v-model="config.table" :placeholder="$t('modeling.base.UpdateOperator.5m7hr2f43ws0')" allowSearch
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
          <a-col :span="2"></a-col>
          <a-col :span="20">
            <div class="d-tips">
              <icon-exclamation-circle-fill />
              {{$t('modeling.dy_common.panel.updateTip')}}
            </div>
          </a-col>
        </a-row>
        <div class="d-form-item-label mb-s">
          <div class="label-text label-color">{{$t('modeling.base.UpdateOperator.5m7hr2f43zo0')}}</div>
          <div class="d-control-add" @click="operateList('list-add')">+</div>
        </div>
        <div class="update-field-frame mb-s" v-for="(item, listKey) in config.list" :key="`configlist${listKey}`">
          <a-row align="center" class="mb-s">
            <a-col :span="22" class="mr-s">
              <div class="select-field-container dy-select-container">
                <a-select popup-container=".select-field-container" v-model="item.field" :placeholder="$t('modeling.base.UpdateOperator.5m7hr2f442c0')"
                allowSearch style="width: 100%;"
                @change="save('list', config.list)">
                <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                      <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                  <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                    <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`" :disabled="checkDisabled(config.list, `${group.group}.${item.name}`, 'field')">{{ item.name }}</a-option>
                  </overflow-tooltip>
                </a-optgroup>
              </a-select>
              </div>
            </a-col>
            <a-col :span="1">
              <div class="d-control-remove" @click="operateList('list-delete', listKey)">-</div>
            </a-col>
          </a-row>
          <a-row>
            <a-col class="label-color" :span="2" style="height: 30px; line-height: 30px; text-align: center;">=</a-col>
            <a-col :span="22">
              <a-row>
                <a-col :span="24" class="mb-s">
                  <div class="select-field-container dy-select-container">
                    <a-select popup-container=".select-field-container" v-model="item.valueType" :placeholder="$t('modeling.base.UpdateOperator.5m7hr2f44580')" @change="save('list', config.list)">
                    <a-option  class="dianayako_select-option-disabled"   v-for="(opt, optKey) in valueType" :key="`valueTypeKey${optKey}`" :value="opt.value">{{ opt.label }}</a-option>
                  </a-select>
                  </div>
                </a-col>
                <a-col :span="24" v-show="item.valueType === 'customSql'">
                  <a-input :max-length="140" show-word-limit  v-model="item.value" :placeholder="$t('modeling.base.UpdateOperator.5m7hr2f44a40')" @blur="save('list', config.list)"></a-input>
                </a-col>
                <a-col :span="item.fieldType === $t('modeling.base.UpdateOperator.5mav1wd54g00') ? 18 : 9" v-show="item.valueType === 'field'">
                  <div class="select-field-container dy-select-container">
                    <a-select popup-container=".select-field-container" v-model="item.value" :placeholder="$t('modeling.base.UpdateOperator.5m7hr2f44h40')"
                    allowSearch style="width: 100%;"
                    @change="save('list', config.list)">
                    <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                    <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                      <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                      <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                        <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`"  :disabled="checkDisabled(fieldsList, item.name, 'field')">{{ item.name }}</a-option>
                      </overflow-tooltip>
                    </a-optgroup>
                  </a-select>
                  </div>
                </a-col>
                <a-col :span="6" v-show="item.valueType === 'field'">
                  <div class="select-type-container dy-select-container">
                    <a-select popup-container=".select-type-container" v-model="item.fieldType" :placeholder="$t('modeling.base.UpdateOperator.5m7hr2f44580')" @change="save('list', config.list)">
                    <a-option  class="dianayako_select-option-disabled"   v-for="(opt, optKey) in fieldUpdateType" :key="`fieldTypeKey${optKey}`" :value="opt.value">{{ opt.label }}</a-option>
                  </a-select>
                  </div>
                </a-col>
                <a-col :span="9" v-show="item.valueType === 'field' && item.fieldType === 'concat'">
                  <div class="select-value-container dy-select-container">
                    <a-select popup-container=".select-value-container" v-model="item.splitValue" :placeholder="$t('modeling.base.UpdateOperator.5m7hr2f44h40')"
                    allowSearch style="width: 100%;"
                    @change="save('list', config.list)">
                    <template #label="{ data }"><overflow-tooltip :text="data?.label" :content="data?.label" :other-width="0">{{ data?.label }}</overflow-tooltip></template>
                    <a-optgroup v-for="(group, groupKey) in fieldsList" :key="`fieldsGroup${groupKey}`"  :label="group.group">
                      <template #label><overflow-tooltip :text="group.group" :content="group.group">{{ group.group }}</overflow-tooltip></template>
                      <overflow-tooltip :text="item.name" v-for="(item, key) in group.fields" :key="`field${key}`" :content="`${group.group} . ${item.name}`">
                        <a-option  class="dianayako_select-option-disabled"   :value="`${group.group}.${item.name}`"  :disabled="checkDisabled(fieldsList, item.name, 'field')">{{ item.name }}</a-option>
                      </overflow-tooltip>
                    </a-optgroup>
                  </a-select>
                  </div>
                </a-col>
              </a-row>
            </a-col>
          </a-row>
        </div>
      </div>
    </a-tab-pane>
  </a-tabs>
  <a-modal
    :visible="warning.show"
    :ok-loading="warning.loading"
    :closable="false"
    simple
    :ok-text="$t('modeling.base.UpdateOperator.5m7hr2f44o80')"
    :cancel-text="$t('modeling.base.UpdateOperator.5m7hr2f44qo0')"
    @ok="warningOk"
    @cancel="warningClose"
  >
    <div class="warning">
      <div class="title"><icon-exclamation-circle-fill color="#ff7d00" />{{$t('modeling.base.UpdateOperator.5m7hr2f44tg0')}}</div>
      <div class="content">{{$t('modeling.base.UpdateOperator.5m7hr2f44vk0')}}！</div>
    </div>
  </a-modal>
  <div></div>
</template>
<script setup lang="ts">
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { Cell } from '@antv/x6'
import { computed, reactive } from 'vue'
import { saveData, checkDisabled } from '../../utils/tool'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { KeyValue } from '@antv/x6/lib/types'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const dFStore = useDataFlowStore()
const useTable = computed(() => dFStore.getUseTable)
const valueType = computed(() => [{ value: 'customSql', label: t('modeling.base.UpdateOperator.5mav1wd9z9o0') }, { value: 'field', label: t('modeling.base.UpdateOperator.5mav1wd9zos0') }])
const fieldUpdateType = computed(() => [{ value: 'originalValue', label: t('modeling.base.UpdateOperator.5mav1wd54g00') }, { value: 'concat', label: t('modeling.base.UpdateOperator.5mav1wd9ztw0') }])
const tableList: any = computed(() => dFStore.getTableSelectList)
const fieldsList = computed(() => dFStore.getFieldsSelectList)
let cell: Cell | null = null
const dData = reactive({
  activeKey: `1`
})
const config: { table: undefined | string, list: KeyValue[], listItem: KeyValue } = reactive({
  table: undefined, list: [],
  listItem: { field: undefined, valueType: 'customSql', value: '', fieldType: 'originalValue', splitValue: undefined }
})
const save = (type: string, data: any) => {
  cell && saveData(type, data, cell)
}
const init = (pCell: Cell) => {
  cell = pCell
  let { data } = cell
  config.list = data.list ? data.list : []
  config.table = data.table ? data.table : ''
  warning.oldValue = data.table ? data.table : ''
}
const selectChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[], prop: any) => {
  if (prop === 'table') {
    warningOpen()
  }
}
const operateList = (type: string, key1?: number) => {
  if (type === 'list-add') config.list.push(JSON.parse(JSON.stringify(config.listItem)))
  else if (type === 'list-delete' && (key1 || key1 === 0)) config.list.splice(key1, 1)
  save('list', config.list)
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
  .select-value-container {
    position: relative;
    width: 100%;
    :deep(.arco-trigger-popup) {
      left: 0 !important;
    }
  }

  .select-type-container {
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
