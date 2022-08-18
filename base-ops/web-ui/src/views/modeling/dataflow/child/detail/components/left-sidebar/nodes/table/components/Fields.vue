
<template>
  <a-modal class="fields-container" v-model:visible="dData.show" :confirm-loading="dData.loading" style="min-width: 1080px;" @cancel="close" @ok="submit">
    <div class="edit">
      <div class="header">
        <a-button type="primary" class="mr-xs" @click="operate('add')">{{$t('modeling.components.Fields.5mpu393tc580')}}</a-button>
        <a-button type="primary" class="mr-xs" @click="operate('insert')">{{$t('modeling.components.Fields.5mpu393tcr00')}}</a-button>
        <a-button type="primary" class="mr-xs" @click="operate('delete')">{{$t('modeling.components.Fields.5mpu393td080')}}</a-button>
        <a-button type="primary" class="mr-xs" @click="operate('key')">{{$t('modeling.components.Fields.5mpu393td3o0')}}</a-button>
        <a-button type="primary" class="mr-xs" @click="operate('up')">{{$t('modeling.components.Fields.5mpu393tdcs0')}}</a-button>
        <a-button type="primary" class="mr-xs" @click="operate('down')">{{$t('modeling.components.Fields.5mpu393tdfo0')}}</a-button>
      </div>
      <div class="table">
        <div class="d-row">
          <div class="d-col"></div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393tdi40')}}</div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393tdk80')}}</div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393tdpo0')}}</div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393tdsk0')}}</div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393tdus0')}}</div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393tdyc0')}}</div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393td3o0')}}</div>
          <div class="d-col">{{$t('modeling.components.Fields.5mpu393te100')}}</div>
        </div>
        <div class="d-row d-table-content" v-for="(item, key) in dData.list" :key="`item${key}`" @click="clickItem(item)">
          <div class="d-col"><div class="fill" v-show="item.select"></div></div>
          <div class="d-col"><input v-model="item.name" /></div>
          <div class="d-col">
            <select v-model="item.type">
              <option v-for="(item, key) in dData.types" :key="`type${key}`" :value="item">{{ item }}</option>
            </select>
          </div>
          <div class="d-col"><input v-model="item.length" type="number" /></div>
          <div class="d-col"><input v-model="item.decimal" type="number" /></div>
          <div class="d-col"><a-checkbox v-model:checked="item.isNull" /></div>
          <div class="d-col"><a-checkbox v-model:checked="item.virtual" /></div>
          <div class="d-col"><input v-model="item.key" /></div>
          <div class="d-col"><input v-model="item.note" /></div>
        </div>
      </div>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { inject, reactive } from 'vue'
import {
  Modal as AModal, Button as AButton, Checkbox as ACheckbox
} from '@arco-design/web-vue'
import { Graph } from '@antv/x6'
import { KeyValue } from '@antv/x6/lib/types'
const emits = defineEmits([`finish`])
const getGraph: any | undefined = inject(`getGraph`)
const dData: KeyValue = reactive({
  show: false, loading: false, list: [],
  types: [
    'bigint', 'binary', 'bit', 'blob', 'char', 'date', 'datetime', 'decimal', 'double', 'enum', 'float', 'geometry',
    'getmetrycollection', 'int', 'integer', 'json', 'linestring', 'longblob', 'longtext', 'mediumblob', 'mediumint',
    'mediumtext', 'multilinestring', 'multipoint', 'multipolygon', 'numeric', 'point', 'polygon', 'real', 'set',
    'smallint', 'text', 'time', 'timestamp', 'tinyblob', 'tinyint', 'tinytext', 'varbinary', 'varchar'
  ]
})
const open = (fields: Array<KeyValue>) => {
  dData.show = true
  dData.list = JSON.parse(JSON.stringify(fields))
}
const close = () => {
  dData.show = false
  if (getGraph) {
    let g: Graph = getGraph()
    g.resetSelection()
  }
}
const submit = () => {
  dData.loading = true
  emits(`finish`, 'fields', dData.list)
  close()
  dData.loading = false
}
const operate = (type: string) => {
  let model = { name: '', type: 'varchar', length: 255, decimal: '', isNull: false, virtual: false, key: '', note: '', select: false }
  let index = dData.list.findIndex((item: KeyValue) => item.select)
  if (type === 'add') dData.list.push(JSON.parse(JSON.stringify(model)))
  else if (type === 'insert') index !== -1 && dData.list.splice(index + 1, 0, JSON.parse(JSON.stringify(model)))
  else if (type === 'delete') index !== -1 && dData.list.splice(index, 1)
  else if (type === 'up') {
    if (index !== -1 && index !== 0) {
      let temp = dData.list[index]
      dData.list[index] = dData.list[index - 1]
      dData.list[index - 1] = temp
    }
  } else if (type === 'down') {
    if (index !== -1 && index !== dData.list.length - 1) {
      let temp = dData.list[index]
      dData.list[index] = dData.list[index + 1]
      dData.list[index + 1] = temp
    }
  }
}
const clickItem = (item: KeyValue) => {
  dData.list.forEach((item: KeyValue) => item.select = false)
  item.select = true
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .fields-container {
    .edit {
      .header {
        display: flex;
      }
      .table {
        border-top: 1px solid #dcdcdc;
        border-left: 1px solid #dcdcdc;
        margin-top: 10px;
        height: 500px;
        box-sizing: border-box;
        overflow: auto;
        border-right: 1px solid #dcdcdc;
        border-bottom: 1px solid #dcdcdc;
        .d-row {
          display: flex;
          .d-col {
            border-right: 1px solid #dcdcdc;
            border-bottom: 1px solid #dcdcdc;
            box-sizing: border-box;
            padding: 2px 4px;
            &:nth-child(1) { width: 18px }
            &:nth-child(2) { width: 200px }
            &:nth-child(3) { width: 150px }
            &:nth-child(4) { width: 120px }
            &:nth-child(5) { width: 80px }
            &:nth-child(6) { width: 60px }
            &:nth-child(7) { width: 38px }
            &:nth-child(8) { width: 60px }
            &:nth-child(9) {
              flex: 1;
              border-right: none;
            }
            .fill {
              width: 10px;
              height: 20px;
              overflow: hidden;
              position: relative;
              &::before {
                content: "";
                position: absolute;
                left: -5px;
                top: 50%;
                transform: translateY(-50%) rotate(45deg);
                width: 10px;
                height: 10px;
                background-color: #000;
              }
            }
            input {
              width: 100%;
              height: 100%;
              border: none;
              outline: none;
            }
            select {
              width: 100%;
              height: 100%;
              border: none;
              outline: none;
            }
          }
        }
        .d-table-content {
          .d-col {
            display: flex;
          }
        }
      }
    }
  }
</style>
