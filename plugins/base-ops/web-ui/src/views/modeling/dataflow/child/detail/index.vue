<template>
  <div class="app-container" id="childDetail">
    <div class="main-bd" style="height: calc(100vh - 114px);">
      <div class="process data-flow data-flow-scroller">
        <a-spin style="width: 100%; height: 100%;" :loading="loading" :tip="$t('modeling.detail.index.5m7apwiz75o0')" >
          <div class="antv-panel d-antv-component-panel" v-if="isReady">
            <div class="antv-header">
              <Header ref="headerRef" @operate="operate" />
              <div class="auto-save-tip" :class="{ 'auto-save-tip-hidden': !autosave.visible }">{{ autosave.tip }}</div>
            </div>
            <div class="antv-main">
              <left-sidebar ref="leftSidebarRef" />
              <a-dropdown :popup-visible="rightMenu.visible" trigger="contextMenu" alignPoint :style="{display:'block'}" @popup-visible-change="rightMenuVisibleChange">
                <div class="antv-main-center" ref="antvMainCenterRef">
                  <div :id="optionR ? optionR.containerId : 'database_container_id'" class="antv-container" />
                </div>
                <template #content>
                  <a-doption key="1" @click="operateNode('cut')">{{$t('modeling.nodes.BaseNode.5m78v3ubttc0')}}</a-doption>
                  <a-doption key="2" @click="operateNode('copy')">{{$t('modeling.nodes.BaseNode.5m78v3ubuac0')}}</a-doption>
                  <a-doption key="3" @click="operateNode('delete')">{{$t('modeling.nodes.BaseNode.5m78v3ubuek0')}}</a-doption>
                  <a-doption key="4" @click="operateNode('paste')">{{$t('modeling.dy_common.paste')}}</a-doption>
                </template>
              </a-dropdown>
              <ConfigPanel ref="configPanelRef" @config-open="configPancelOpen" />
            </div>
            <Run ref="runRef" />
            <VisualEdit ref="visualEditRef" @operate="operate" />
            <VisualList ref="visualListRef" />
          </div>
        </a-spin>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import LeftSidebar from './components/left-sidebar/index.vue'
import ConfigPanel from './components/config-panel/index.vue'
import FlowGraph from './hooks/flowGraph'
import Header from './components/Header.vue'
import Run from './components/dialogs/Run.vue'
import VisualList from './components/dialogs/visual-list/index.vue'
import VisualEdit from './components/dialogs/visual-edit/index.vue'
import './style/common.less'
import { Graph } from '@antv/x6'
import { Spin as ASpin } from '@arco-design/web-vue'
import { useRoute, useRouter } from 'vue-router'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { options } from './hooks/options'
import { useModelCommonStore } from '@/store/modules/modeling/common'
import { checkData, jsonFormat } from './utils/operateJson'
import { dataFlowGetById, saveJsonData } from '@/api/modeling'
import { useI18n } from 'vue-i18n'
import { PropsOptions } from './types'
import { KeyValue } from '@/types/global'
import { getCheckedNodes } from './utils/tools'

const { t } = useI18n()
const route = useRoute()

const dFStore = useDataFlowStore()
const mCStore = useModelCommonStore()
mCStore.setI18n(t)
const router = useRouter()
let graph: null | Graph = null
let autoSaveTimer: number | null
const autosave = reactive({
  tip: t('modeling.dy_common.AutomaticallySaved'), visible: false
})
const clearAutoSaveTimer = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}
const autoSaveFunc = () => {
  autosave.tip = t('modeling.dy_common.AutomaticallySaving')
  autosave.visible = true
  if (graph) {
    let jsonData = jsonFormat(graph) as KeyValue
    saveJsonData(String(window.$wujie?.props.data.id), jsonData, true).then(() => {
      autosave.tip = t('modeling.dy_common.AutomaticallySaved')
      setTimeout(() => {
        autosave.visible = false
      }, 2 * 1000)
    })
  }
}
const setAutoSave = () => {
  autoSaveTimer = setInterval(autoSaveFunc, 10 * 1000)
}
const optionR = ref<PropsOptions | null>()
const loading = ref<boolean>(false)
const isReady = ref<boolean>(false)
const leftSidebarRef = ref<InstanceType<typeof LeftSidebar> | null>(null)
const configPanelRef = ref<InstanceType<typeof ConfigPanel> | null>(null)
const headerRef = ref<InstanceType<typeof Header> | null>(null)
const antvMainCenterRef = ref<HTMLElement>()
const initAntv = async () => {
  let optionR = options()
  if (optionR && optionR.containerId) {
    loading.value = true
    dataFlowGetById(window.$wujie?.props.data.id as string).then((res: any) => {
      if (Number(res.code) === 200 && res.data) {
        const item = res.data
        dFStore.setFlowDataInfo(item)
        dFStore.setDatabaseInfo(item.dbName, item.clusterNodeId, item.schema).then(() => {
          isReady.value = true
          nextTick(async () => {
            graph = FlowGraph.init(optionR, route, antvMainCenterRef.value, {
              edgeConnect: () => {
                closeConfigPancel()
              },
              edgeDelete: () => {
                closeConfigPancel()
              },
              deleteCell: () => {
                closeConfigPancel()
              },
              blankContextmenu: () => {
                openRightMenu()
              }
            })
            dFStore.setGraph(graph)
            graph && leftSidebarRef.value?.init(graph, optionR)
            configPanelRef.value?.init()
            graph && headerRef.value?.init(graph, optionR)
            FlowGraph.renderInitJson()
            loading.value = false
            setAutoSave()
          })
        }).catch((e) => {
            console.log(e)
            router.back()
        })
      } else {
        router.back()
      }
    }).catch(() => router.back())
  } else {
    console.error({ content: t('modeling.detail.index.5m7apwiz8b00') })
  }
}
const ctrlSEvent = (e: any) => {
  if (e.key === 's' && (navigator.platform.match(`Mac`) ? e.metaKey : e.ctrlKey)) {
    e.preventDefault()
  }
}
onMounted(() => {
  dFStore.initState()
  initAntv()
  document.addEventListener(`keydown`, ctrlSEvent, false)
})
onUnmounted(() => {
  document.removeEventListener(`keydown`, ctrlSEvent)
  clearAutoSaveTimer()
})
const runRef = ref<InstanceType<typeof Run>>()
const visualListRef = ref<InstanceType<typeof VisualList>>()
const visualEditRef = ref<InstanceType<typeof VisualEdit>>()
mCStore.$onAction(({ name, args }) => {
  if (name === 'nodeEvent' && args && args[0] && args[0].event && args[0].event === 'operate') {
    if (args[0].type) operate(args[0].type, args[0].data ? args[0].data : null)
  }
})

const operate = (type: string, data?: any) => {
  if (type === 'run') {
    graph && runRef.value?.open(graph, data, type)
  } else if (type === 'code') {
    graph && runRef.value?.open(graph, data, type)
  } else if (type === 'antvLeft') leftSidebarRef.value?.toggleMenu()
  else if (type === 'close-config-panel') {
    configPanelRef.value?.closeConfigPanel()
  } else if (type === `visual`) {
    visualListRef.value?.open(data)
    operate('close-config-panel')
  } else if (type === `visual-edit`) {
    if (graph) {
      let jsonData = jsonFormat(graph)
      let check = checkData(jsonData)
      if (jsonData && check) {
        visualEditRef.value?.open(graph, data)
      }
    }
  } else if (type === `toggleGridType`) {
    if (graph) {
      FlowGraph.toggleGridType()
    }
  }
}
onUnmounted(() => {
  dFStore.initState()
})
const configPancelShow = ref<boolean>(false)
const configPancelOpen = () => {
  configPancelShow.value = true
}
const closeConfigPancel = () => {
  configPanelRef.value?.closeConfigPanel()
}

const rightMenu = reactive({
  visible: false
})
let rightMenuShow = false
const openRightMenu = () => {
  rightMenu.visible = true
}
const closeRightMenu = () => {
  rightMenuShow = false
  rightMenu.visible = false
}
const rightMenuVisibleChange = () => {
  if (rightMenuShow && rightMenu.visible) {
    closeRightMenu()
  } else {
    if (rightMenu.visible) {
      rightMenuShow = true
    }
  }
}
const operateNode = (type: string) => {
  if (graph) {
    if (type === 'delete') {
      const cells = graph.getSelectedCells()
      if (cells.length) {
        const deleteArr = cells.filter((item: any) => (!item.data.operate || item.data.operate.includes('delete')))
        graph.removeCells(deleteArr)
      }
    } else if (type === 'copy') {
      graph.copy(getCheckedNodes(graph), { useLocalStorage: true })
    } else if (type === 'cut') {
      graph.cut(getCheckedNodes(graph), { useLocalStorage: true })
    } else if (type === 'paste') {
      graph.paste({ useLocalStorage: true })
    }
  }
}

</script>
<style scoped lang="less">
.app-container {
  height: 100%;
  .main-bd {
    height: 100%;
    height: calc(100vh - 60px - 50px - 48px - 16px - 16px);
    min-height: 0 !important;
    .data-flow {
      width: 100%;
      height: 100%;
    }
    .antv-panel {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      .antv-header {
        width: 100%;
        height: 40px;
        position: relative;
        .auto-save-tip {
          position: absolute;
          top: 0;
          z-index: 499;
          right: 50px;
          height: 40px;
          line-height: 40px;
          font-size: 12px;
          color: var(--color-neutral-6);
          opacity: 1;
          transition: all .5s;
          transform: translateX(0);
        }
        .auto-save-tip-hidden {
          opacity: 0;
          transform: translateX(-20PX);
        }
      }
      .antv-main {
        display: flex;
        flex: 1;
        height: 0;
        width: 100%;
        position: relative;
        overflow: hidden;
        .mask {
          width: 100%;
          height: 100%;
          position: absolute;
          left: 0;
          top: 0;
          z-index: 10;
        }
        .left-sidebar {
          width: 260px;
          height: 100%;
          .stencil {
            width: 100%;
            height: 100%;
            position: relative;
          }
        }
        .antv-main-center {
          width: 0;
          flex: 1;
        }
      }
      .command {
        background-color: #232f46;
        box-sizing: border-box;
        padding: 6px 10px;
        position: relative;
        overflow: auto;
        .toggle {
          position: absolute;
          right: 10px;
          top: 5px;
          color: #fff;
          cursor: pointer;
          transition: all .3s;
        }
        .content {
          .info-1 {
            color: #fff;
            height: 20px;
            line-height: 20px;
            font-size: 14px;
            margin-bottom: 5px;
          }
        }
      }
    }
  }
}
</style>
<style lang="less">
.d-antv-component-panel {
  .x6-graph-scroller {
    width: 100% !important;
    height: 100% !important;
    &::-webkit-scrollbar {
      width: 10px;
      height: 10px;
      position: absolute;
    }
    &::-webkit-scrollbar-track {
      border-radius: 5px;
      background-color: transparent;
    }
    &::-webkit-scrollbar-thumb {
      border-radius: 10px;
      background-color: #c3c4c5;
    }
  }
}
.data-flow-scroller {
  * {
    &::-webkit-scrollbar {
      width: 10px;
      height: 10px;
      position: absolute;
    }
    &::-webkit-scrollbar-track {
      border-radius: 5px;
      background-color: transparent;
    }
    &::-webkit-scrollbar-thumb {
      border-radius: 10px;
      background-color: #c3c4c5;
    }
  }
  &::-webkit-scrollbar {
    width: 10px;
    height: 10px;
    position: absolute;
  }
  &::-webkit-scrollbar-track {
    border-radius: 5px;
    background-color: transparent;
  }
  &::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background-color: #c3c4c5;
  }
}
.dianayako_select-option-disabled.arco-select-option-disabled {
  color: var(--color-text-1) !important;
  opacity: .65;
}
</style>
