
<template>
  <a-tabs
    class="d-a-tabs"
    v-model:activeKey="iData.activeKey" centered size="mini" :tabBarGutter="40"
    :tabBarStyle="{ color: '#fff' }"
  >
    <a-tab-pane key="1" :title="$t('modeling.combination.RestrictionOperator.5me6gnj6b8s0')">
      <div class="tab-content d-a-form">
        <a-row class="mb mt-s" align="center">
          <a-col :span="4" class="mr-xs label-color">{{$t('modeling.combination.RestrictionOperator.5me6gnj6bpc0')}}</a-col>
          <a-col :span="15" class="mr-xs">
            <a-input-number v-model="iData.restriction.skip" @blur="save" />
          </a-col>
          <a-col :span="3" class="label-color">{{$t('modeling.combination.RestrictionOperator.5me6gnj6bts0')}}</a-col>
        </a-row>
        <a-row align="center">
          <a-col :span="4" class="mr-xs label-color">{{$t('modeling.combination.RestrictionOperator.5me6gnj6c3w0')}}</a-col>
          <a-col :span="15" class="mr-xs">
            <a-input-number v-model="iData.restriction.limitCount" @blur="save" />
          </a-col>
          <a-col :span="3" class="label-color">{{$t('modeling.combination.RestrictionOperator.5me6gnj6bts0')}}</a-col>
        </a-row>
      </div>
    </a-tab-pane>
  </a-tabs>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import {
  Tabs as ATabs, TabPane as ATabPane, Row as ARow, Col as ACol, InputNumber as AInputNumber
  } from '@arco-design/web-vue'
import { saveData } from '../../utils/tool'
import { Cell } from '@antv/x6'
import { KeyValue } from '@antv/x6/lib/types'
const save = () => saveData('restriction', iData.restriction, iData.cell)
const iData: KeyValue = reactive({
  activeKey: `1`, cell: null,
  restriction: { skip: null, limitCount: null }
})
const init = (cell: Cell) => {
  iData.cell = cell
  let { data } = cell
  iData.restriction.skip = data.restriction && data.restriction.skip ? data.restriction.skip : null
  iData.restriction.limitCount = data.restriction && data.restriction.limitCount ? data.restriction.limitCount : null
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
