
<template>
  <a-dropdown trigger="contextMenu" alignPoint position="bl">
    <div
      class="d-antv-node base-node-container"
      :class="{
        'd-antv-node-selected1': antvSelected,
        'd-antv-node-disabled': disabled,
        'd-antv-node-dark': dAntvNodeDarkTheme,
        'd-antv-node-dark-selected': dAntvNodeDarkTheme && antvSelected
      }"
    >
      <div class="base-node-icon" v-if="nodeConfig.icon">
        <svg-icon v-if="nodeConfig.icon" class="bs-i" :icon-class="nodeConfig.icon"></svg-icon>
      </div>
      <div class="name">{{ nodeConfig.text }}</div>
      <icon-stop class="disabled-frame-dy-c" v-show="showDisabled" :class="{ 'open-base-node-disabled': disabled }" @click="toggleDisbled" />
      <icon-bar-chart class="base-node-play" v-show="showPlay" @click.stop="openVisual" />
      <icon-settings class="base-node-settings" @click.stop="openConfig" />
    </div>
    <template #content>
      <a-doption key="1" @click="operateNode('cut')" v-if="!nodeConfig.operate || nodeConfig.operate.includes('cut')">{{$t('modeling.nodes.BaseNode.5m78v3ubttc0')}}</a-doption>
      <a-doption key="2" @click="operateNode('copy')" v-if="!nodeConfig.operate || nodeConfig.operate.includes('copy')">{{$t('modeling.nodes.BaseNode.5m78v3ubuac0')}}</a-doption>
      <a-doption key="3" @click="operateNode('delete')" v-if="!nodeConfig.operate || nodeConfig.operate.includes('delete')">{{$t('modeling.nodes.BaseNode.5m78v3ubuek0')}}</a-doption>
      <a-doption key="4" @click="operateNode('paste')" v-if="!nodeConfig.operate || nodeConfig.operate.includes('delete')">{{$t('modeling.dy_common.paste')}}</a-doption>
    </template>
  </a-dropdown>
</template>
<script setup lang="ts">
import { computed, inject, onMounted, ref } from 'vue'
import { IconSettings, IconStop } from '@arco-design/web-vue/es/icon'
import { Dropdown as ADropdown, Doption as ADoption } from '@arco-design/web-vue'
import { IconCheck, IconBarChart } from '@arco-design/web-vue/es/icon'
import { Cell, Graph } from '@antv/x6'
import { useModelCommonStore } from '@/store/modules/modeling/common'
import { KeyValue } from '@antv/x6/lib/types'
import svgIcon from '@/components/svg-icon/index.vue'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { getCheckedNodes } from '../../../utils/tools'
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
const showPlay = ref<boolean>(true)
const showDisabled = ref<boolean>(true)
const useTable = computed(() => dFStore.getUseTable)
const useDatabase = computed(() => dFStore.getUseDatabase)

const dAntvNodeDarkTheme = ref<boolean>(false)
onMounted(() => {
  if (getNode) {
    let n: Cell = getNode()
    if (n && n.data && n.data.cells_type && n.data.cells_type === 'dataSource') showPlay.value = false
    if (n && n.data && n.data.cells_type && n.data.cells_type === 'dataSource') showDisabled.value = false
    if (n && n.data && n.data.disabled) disabled.value = true
    else disabled.value = false
    n.on(`change:data`, ({ current }) => {
      antvSelected.value = current.antvSelected ? current.antvSelected : false
      nodeDisabledShow.value = current.showDisabledCheckbox ? current.showDisabledCheckbox : false
    })
  }

  const theme = localStorage.getItem('opengauss-theme')
  if (theme === 'dark') {
    dAntvNodeDarkTheme.value = true
  } else {
    dAntvNodeDarkTheme.value = false
  }
  // window.$wujie?.bus.$on('opengauss-theme-change', (val: string) => {
  //   if (val === 'dark') {
  //     dAntvNodeDarkTheme.value = true
  //   } else {
  //     dAntvNodeDarkTheme.value = false
  //   }
  // })
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

      // Existing mapping operator and aggregation operator
      // Must be a sequence operator
      dFStore.clearFieldsAlias && dFStore.clearFieldsAlias()
      if (n.data.cells_type === 'sort') {
        nodes.forEach(item1 => {
          if (item1.data.cells_type === 'mapping') {
            item1.data.mappings.forEach((item: any) => {
              if (item && item.field && item.field.split) {
                let tablename = item.field.split('.')[0]
                if (tablename && item.value && useDatabase.value) {
                  dFStore.setFieldsAlias({
                    database: useDatabase.value,
                    table: tablename,
                    value: item.value,
                    name: item.field
                  })
                }
              }
            })
          } else if (item1.data.cells_type === 'polymerization') {
            item1.data.polymerization.forEach((item: any) => {
              console.log(item)
              if (item && item.field && item.field.split) {
                let tablename = item.field.split('.')[0]
                if (tablename && item.alias && useDatabase.value) {
                  dFStore.setFieldsAlias({
                    database: useDatabase.value,
                    table: tablename,
                    value: item.alias,
                    name: item.field
                  })
                }
              }
            })
          }
        })
      }

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
      let cells = getCheckedNodes(g)
      if (cells && cells.length > 0) {
        const deleteArr = cells.filter((item: Cell) => (!item.data.operate || item.data.operate.includes('delete')))
        g.removeCells(deleteArr)
      } else {
        g.removeNode(n.id)
      }
    } else if (type === 'copy') {
      let cells = getCheckedNodes(g)
      if (cells && cells.length > 0) {
        g?.copy(cells, { useLocalStorage: true })
      } else {
        g.copy([n], { useLocalStorage: true })
      }
    } else if (type === 'cut') {
      let cells = getCheckedNodes(g)
      if (cells && cells.length > 0) {
        g?.cut(getCheckedNodes(g), { useLocalStorage: true })
      } else {
        g?.cut([n], { useLocalStorage: true })
      }
    } else if (type === 'paste') {
      g?.paste({ useLocalStorage: true })
    }
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
    box-shadow: 0px 2px 10px rgb(201, 205, 212);
    border-radius: 4px;
    transition: opacity .3s;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    padding: 0 10px;
    position: relative;
    .name {
      color: rgb(36, 36, 36);
      font-size: 14px;
    }
    .base-node-icon {
      width: 42px;
      height: 42px;
      background-color: rgba(227, 29, 28, .1);
      border-radius: 50%;
      border: 1px solid rgba(227, 29, 28, .4);
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
        fill: rgb(227, 29, 28);
      }
    }
    .disabled-frame-dy-c {
      position: absolute;
      font-size: 14px;
      right: 47px;
      top: 5px;
      cursor: pointer;
    }
    .open-base-node-disabled {
      color: #FF0000;
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
    opacity: 0.4;
    .base-node-settings {
      cursor: default !important;
    }
  }
  .d-antv-node-dark {
    background-color: #171719 !important;
    box-shadow: 0px 2px 10px rgb(201, 205, 212);
    .base-node-icon {
      background-color: rgba(252, 240, 147, .1) !important;
      border: 1px solid rgba(252, 240, 147, .4) !important;
      > .bs-i {
        fill: rgb(252, 240, 147) !important;
      }
    }
    .name, .base-node-play, .base-node-settings {
      color: rgba(255, 255, 255, .9) !important;
    }
    .base-node-disabled {
      .disabled-frame {
        border: 1px solid rgb(252, 240, 147) !important;
        color: rgb(252, 240, 147) !important;
        &::before {
          content: "";
          position: absolute;
          top: 50%;
          right: 0;
          width: 10px;
          height: 1px;
          transform: translate(100%, -50%);
          background-color: rgb(252, 240, 147) !important;
        }
      }
    }
  }

.d-antv-node-selected1 {
  border: 1px solid rgb(227, 29, 28) !important;
}
.d-antv-node-dark-selected {
  border: 1px solid rgb(252, 240, 147) !important;
}
</style>
