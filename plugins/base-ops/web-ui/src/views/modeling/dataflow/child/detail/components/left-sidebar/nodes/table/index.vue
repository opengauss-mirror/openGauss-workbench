
<template>
  <a-dropdown trigger="contextMenu" alignPoint :style="{display:'block'}">
    <div
      class="d-antv-node table-node-container" :class="{ 'd-antv-node-selected': dData.antvSelected }" ref="tableNodeRef"
      @dblclick="openDialog('fields')"
    >
      <div class="header">
        <div class="name">{{ dData.name }}</div>
      </div>
      <div class="fields" v-show="dData.fields.length !== 0">
        <div class="field" v-for="(item, key) in dData.fields" :key="`field${key}`">
          <div class="f-name">{{ item.name }}：</div>
          <div class="f-type">{{ item.type }}</div>
          <div class="f-length" v-if="item.length || item.length === 0">（{{item.length}}）</div>
        </div>
      </div>
      <Fields ref="fieldsRef" @finish="DialogSuccess" />
      <Rename ref="renameRef" @finish="DialogSuccess" />
    </div>
    <template #content>
      <a-doption key="1" @click="operateNode('cut')">{{$t('modeling.table.index.5mpu30zsqys0')}}</a-doption>
      <a-doption key="2" @click="operateNode('copy')">{{$t('modeling.table.index.5mpu30zsrj00')}}</a-doption>
      <a-doption key="3" @click="operateNode('delete')">{{$t('modeling.table.index.5mpu30zsroc0')}}</a-doption>
      <a-doption key="3" @click="openDialog('rename')">{{$t('modeling.table.index.5mpu30zsrrw0')}}</a-doption>
      <a-doption key="4">{{$t('modeling.table.index.5mpu30zsrvc0')}}</a-doption>
    </template>
  </a-dropdown>
</template>
<script setup lang="ts">
import { inject, nextTick, onMounted, reactive, ref } from 'vue'
import Fields from './components/Fields.vue'
import { Dropdown as ADropdown, Doption as ADoption } from '@arco-design/web-vue'
import { Cell, Graph } from '@antv/x6'
import Rename from './components/Rename.vue'
import { KeyValue } from '@antv/x6/lib/types'
const getNode: any | undefined = inject(`getNode`)
const getGraph: any | undefined = inject(`getGraph`)
const dData: KeyValue = reactive({
  name: this.$t('modeling.table.index.5mpu30zss2o0'), fields: [], antvSelected: false
})
onMounted(() => {
  if (getNode) {
    let n: Cell = getNode()
    n.on(`change:data`, ({ current }) => {
      dData.antvSelected = current.antvSelected
    })
  }
})
const fieldsRef = ref<null | InstanceType<typeof Fields>>(null)
const renameRef = ref<null | InstanceType<typeof Rename>>(null)
const openDialog = (type: string) => {
  if (type === 'fields') fieldsRef.value?.open(dData.fields)
  else if (type === 'rename') renameRef.value?.open(dData.name)
}
const tableNodeRef = ref<null | HTMLElement>(null)
const DialogSuccess = (type: string, data: Array<KeyValue> | string) => {
  if (type === 'fields') {
    dData.fields = JSON.parse(JSON.stringify(data))
    if (getNode) {
      let n = getNode()
      n.setData({ fields: dData.fields })
      nextTick(() => {
        n.setProp(`size`, { width: tableNodeRef.value?.offsetWidth, height: tableNodeRef.value?.offsetHeight })
      })
    }
  } else if (type === 'rename') {
    if (getNode) {
      let n = getNode()
      dData.name = data
      n.setData({ name: data })
    }
  }
}
const operateNode = (type: string) => {
  if (getGraph && getNode) {
    let g: Graph = getGraph()
    let n: Cell = getNode()
    if (type === 'delete') g.removeNode(n.id)
    else if (type === 'copy') {
      g.copy([n])
      g.paste()
    } else if (type === 'cut') g.cut([n])
  }
}
</script>
<style scoped lang="less">
  .table-node-container {
    width: 100%;
    min-height: 40px;
    background-color: #fff;
    box-shadow: 0px 2px 6px rgba(0, 0, 0, 0.15);
    border-radius: 1px;
    .header {
      display: flex;
      align-items: center;
      box-sizing: border-box;
      padding: 0 10px;
      height: 40px;
      line-height: 40px;
      justify-content: center;
      .name {
        font-size: 16px;
        font-weight: 600;
      }
      .icon1 {
        margin-left: auto;
        margin-right: 10px;
        font-size: 16px;
        cursor: pointer;
      }
      .icon2 {
        font-size: 16px;
        cursor: pointer;
      }
    }
    .fields {
      box-sizing: border-box;
      padding: 10px;
      border-top: 1px solid #000;
      .field {
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 5px;
        > div {
          white-space: wrap;
        }
      }
    }
  }
</style>
