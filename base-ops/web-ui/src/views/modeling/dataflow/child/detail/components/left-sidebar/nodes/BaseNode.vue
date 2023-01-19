
<template>
  <a-dropdown trigger="contextMenu" alignPoint position="bl">
    <div
      class="d-antv-node base-node-container" :class="{ 'd-antv-node-selected': antvSelected, 'd-antv-node-disabled': disabled }"
    >
      <div class="base-node-disabled" :class="{ 'open-base-node-disabled': nodeDisabledShow }">
        <div class="disabled-frame" @click="toggleDisbled"><icon-check v-show="disabled" /></div>
      </div>
      <div class="base-node-icon" v-if="nodeConfig.icon">
        <svg-icon v-if="nodeConfig.icon" class="bs-i" :icon-class="nodeConfig.icon"></svg-icon>
      </div>
      <div class="name">{{ nodeConfig.text }}</div>
      <icon-bar-chart class="base-node-play" v-show="showPlay" @click.stop="openVisual" />
      <icon-settings class="base-node-settings" @click.stop="openConfig" />
    </div>
    <template #content>
      <a-doption key="1" @click="operateNode('cut')" v-if="!nodeConfig.operate || nodeConfig.operate.includes('cut')">{{$t('modeling.nodes.BaseNode.5m78v3ubttc0')}}</a-doption>
      <a-doption key="2" @click="operateNode('copy')" v-if="!nodeConfig.operate || nodeConfig.operate.includes('copy')">{{$t('modeling.nodes.BaseNode.5m78v3ubuac0')}}</a-doption>
      <a-doption key="3" @click="operateNode('delete')" v-if="!nodeConfig.operate || nodeConfig.operate.includes('delete')">{{$t('modeling.nodes.BaseNode.5m78v3ubuek0')}}</a-doption>
    </template>
  </a-dropdown>
</template>
<script setup lang="ts">
import { inject, onMounted, ref } from 'vue'
import { IconSettings } from '@arco-design/web-vue/es/icon'
import { Dropdown as ADropdown, Doption as ADoption } from '@arco-design/web-vue'
import { IconCheck, IconBarChart } from '@arco-design/web-vue/es/icon'
import { Cell, Graph } from '@antv/x6'
import { useModelCommonStore } from '@/store/modules/modeling/common'
import { KeyValue } from '@antv/x6/lib/types'
import svgIcon from '@/components/svg-icon/index.vue'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
const mCStore = useModelCommonStore()
const dFStore = useDataFlowStore()
const $t = mCStore.getI18n
const getNode: any | undefined = inject(`getNode`)
const getGraph: any | undefined = inject(`getGraph`)
const nodeEvent: any | undefined = inject(`node-event`)
let nodeConfig = ref<KeyValue>({})
const antvSelected = ref<boolean>(false)
const disabled = ref<boolean>(false)
const nodeDisabledShow = ref<boolean>(false)
const showPlay = ref<boolean>(false)
onMounted(() => {
  if (getNode) {
    let n: Cell = getNode()
    if (n && n.data && n.data.cells_type && n.data.cells_type !== 'dataSource') showPlay.value = true
    n.on(`change:data`, ({ current }) => {
      antvSelected.value = current.antvSelected ? current.antvSelected : false
      nodeDisabledShow.value = current.showDisabledCheckbox ? current.showDisabledCheckbox : false
    })
  }
})
const getName = () => getNode ? nodeConfig = getNode().getData() : setTimeout(() => { getName() }, 100)
getName()
const openConfig = () => {
  if (!disabled.value) {
    let g: Graph = getGraph()
    let n: Cell = getNode()
    let nodes: Cell[] = g.getPredecessors(n, { deep: true })
    const item = dFStore.getFlowDataInfo
    dFStore.setFlowDataInfo(item)
    let source1 = item.clusterNodeId
    let source2 = item.schema
    let source3 = item.dbName
    let index = nodes.findIndex((item: any) => {
      return item.shape === 'BaseNode' && item.data && item.data.cells_type === 'dataSource'
    })
    if (index !== -1 || (n && n.data && n.data.cells_type === 'dataSource')) {

      let dataSourceNode = index !== -1 ? nodes[index] : n
      if (dataSourceNode && dataSourceNode.data && dataSourceNode.data.source && dataSourceNode.data.source.length > 0) {
        source3 = dataSourceNode.data.source[2]
        source1 = dataSourceNode.data.source[1]
        source2 = dataSourceNode.data.source[3]
      }
    }

    dFStore.setDatabaseInfo(source3, source1, source2).then(() => {
      let index2 = nodes.findIndex((item: any) => {
        return item.shape === 'BaseNode' && item.data && ['query', 'insert', 'delete', 'update'].includes(item.data.cells_type)
      })
      if (index2 !== -1 && nodes[index2].data.table) dFStore.setDatabaseTableInfo(nodes[index2].data.table)
      if (n.shape === 'BaseNode' && n.data && ['query', 'insert', 'delete', 'update'].includes(n.data.cells_type)) dFStore.setDatabaseTableInfo(n.data.table)
      let index3 = nodes.findIndex((item: any) => {
        return item.shape === 'BaseNode' && item.data && item.data.cells_type === 'join'
      })
      if (index3 !== -1 && nodes[index3].data.table) dFStore.setDatabaseTableInfo(nodes[index3].data.table)
      if (n.shape === 'BaseNode' && n.data && n.data.cells_type === 'join') dFStore.setDatabaseTableInfo(n.data.table)
      getNode && mCStore.setSelectNode(n, true)
    })
  }
}
const openVisual = () => {
  let n: Cell = getNode()
  if (n) {
    nodeEvent('visual-edit', n)
  }
}
const operateNode = (type: string) => {
  if (getGraph && getNode) {
    let g: Graph = getGraph()
    let n: Cell = getNode()
    if (type === 'delete') {
      g.removeNode(n.id)
    } else if (type === 'copy') {
      g.copy([n])
      g.paste()
    } else if (type === 'cut') g.cut([n])
  }
}
const toggleDisbled = () => {
  disabled.value = disabled.value ? !disabled.value : true
  if (getNode) {
    let n: Cell = getNode()
    n.setData({ disabled: disabled.value })
  }
}
</script>
<style scoped lang="less">
  .base-node-container {
    width: 100%;
    height: 100%;
    background: rgb(255, 255, 255);
    box-shadow: 0px 2px 8px rgba(165, 165, 165, 0.33);
    border-radius: 4px;
    transition: opacity .3s;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    padding: 0 10px;
    position: relative;
    .base-node-disabled {
      position: absolute;
      left: 0;
      top: 50%;
      transform: translate(-100%, -50%);
      width: 26px;
      height: 100%;
      display: none;
      .disabled-frame {
        position: absolute;
        left: 0;
        top: 50%;
        transform: translate(0, -50%);
        width: 16px;
        height: 16px;
        border: 1px solid rgb(var(--primary-6));
        cursor: pointer;
        color: rgb(var(--primary-6));
        &::before {
          content: "";
          position: absolute;
          top: 50%;
          right: 0;
          width: 10px;
          height: 1px;
          transform: translate(100%, -50%);
          background-color: rgb(var(--primary-6));
        }
      }
    }
    .open-base-node-disabled {
      display: block;
    }
    .name {
      color: rgb(36, 36, 36);
      font-size: 14px;
    }
    .base-node-icon {
      width: 42px;
      height: 42px;
      background-color: rgba(var(--primary-6), .1);
      border-radius: 50%;
      border: 1px solid rgba(var(--primary-6), .4);
      display: flex;
      justify-content: center;
      align-items: center;
      margin-right: 15px;
      overflow: hidden;
      > .bs-i {
        width: 18px;
        height: 18px;
        font-size: 18px;
        color: transparent;
        fill: rgb(var(--primary-6));
      }
    }
    .base-node-settings {
      position: absolute;
      font-size: 14px;
      right: 5px;
      top: 5px;
      cursor: pointer;
    }
    .base-node-play {
      position: absolute;
      font-size: 14px;
      right: 25px;
      top: 5px;
      cursor: pointer;
    }
    > * {
      &:last-child {
        margin-left: auto;
      }
    }
  }
  .d-antv-node-disabled {
    opacity: 0.2;
    .base-node-settings {
      cursor: default !important;
    }
  }
</style>
