
<template>
  <a-spin class="left-sidebar-container" :spinning="iData.loading" :wrapperClassName="iData.open ? 'open' : 'close'">
    <div class="left-sidebar" :class="[iData.open ? 'open' : 'close']">
      <div class="search">
        <div class="search-frame">
          <!-- <input v-model="iData.search" type="text" :placeholder="$t('modeling.left-sidebar.index.5m78udtsry80')" @keydown.enter="onSearch" /> -->
          <a-input-search
            v-model="iData.search"
            :style="{width:'100%'}"
            :placeholder="$t('modeling.left-sidebar.index.5m78udtsry80')"
            search-button
            allow-clear
            @clear="onSearch"
            @search="onSearch"
            @press-enter="onSearch"
          />
          <!-- <div class="search-icon" @click="onSearch">
            <icon-search />
          </div> -->
        </div>
      </div>
      <div class="content">
        <div class="group" :class="group.open || !group.name ? 'group-open' : ''" v-for="(group, groupKey) in showNodes" :key="`groupKey${groupKey}`">
          <div class="group-name" @click="toggleGroup(group)" v-if="group.name">
            <div class="name">
              <component v-if="group.icon && group.icon.startsWith(`icon-`)" :is="group.icon"></component>
              <div class="text">{{ group.name }}</div>
              <icon-down class="group-arrow" />
            </div>
          </div>
          <div class="nodes" :style="group.name ? { height: group.childHeight } : { height: 'auto', paddingTop: 0 }">
            <div class="node" v-for="(node, nodeKey) in group.child" :key="`nodeKey${nodeKey}`">
              <div class="node-title" @mousedown="(e: MouseEvent) => onMousedown(e, node)" @mouseup="(e: MouseEvent) => onMouseup(e, node)">
                <div class="node-title-icon">
                  <svg-icon v-if="node.data.icon" class="bs-i" :icon-class="node.data.icon"></svg-icon>
                  <div class="active-bg"></div>
                </div>
                <div class="title-text">{{ node.data.text }}</div>
                <a-tooltip :content-style="{ whiteSpace: 'pre-wrap', maxWidth: '680px' }" :content="$t('modeling.dy_common.detail.'+node.data.cells_type+'Desc')">
                  <div class="help">
                    <IconQuestionCircleFill />
                  </div>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-spin>
</template>
<script setup lang="tsx">
import { computed, nextTick, reactive } from 'vue'
import { Dnd } from '@antv/x6/lib/addon'
import { Graph, Addon, Node } from '@antv/x6'
import '@antv/x6-vue-shape'
import { Spin as ASpin } from '@arco-design/web-vue'
import { IconQuestionCircleFill } from '@arco-design/web-vue/es/icon'
import Nodes from './nodes/index.vue'
import { useModelCommonStore } from '@/store/modules/modeling/common'
import { PropsOptions } from '../../types'
import { KeyValue } from '@/api/modeling/request'
import svgIcon from '@/components/svg-icon/index.vue'
const mCStore = useModelCommonStore()
const operate = (type: string, data: any) => {
  mCStore.nodeEvent({ event: 'operate', type, data })
}
type Group = { name?: string, child: Array<Node.Metadata>, open?: boolean, childHeight?: string, groupType: string }
interface leftIData { loading: boolean, graph: Graph | null, dnd: Dnd | null, search: string, nodes: Array<Group>, open: boolean }
const iData: leftIData = reactive({
  loading: true, graph: null, dnd: null, nodes: [], open: true,
  search: ''
})
const init = (graph: Graph, options: PropsOptions) => {
  iData.dnd = new Addon.Dnd({
    target: graph,
    getDragNode: (node) => node.clone(),
    getDropNode: (node) => node.clone()
  })
  iData.graph = graph
  if (options && options.stencil && options.stencil.nodes) {
    let menuArr: Group[] = JSON.parse(JSON.stringify(options.stencil.nodes))
    iData.nodes = menuArr
    registerVueComponent(options)
    iData.nodes = menuArr.filter(item => item.groupType !== 'hidden')
    nextTick(() => { iData.nodes.forEach(item => toggleGroup(item)) })
  }
  iData.loading = false
}
const showNodes = computed(() => {
  let arr: any = [{ child: [] }]
  if (iData.search) {
    iData.nodes.forEach((group: KeyValue) => {
      group.child.forEach((node: Node.Metadata) => {
        if (node.data.text.includes(iData.search)) arr[0].child.push(node)
      })
    })
  } else arr = iData.nodes
  return arr
})
const toggleGroup = (group: Group) => {
  group.childHeight = group.open ? '0px' : `${group.child.length * (50 + 10) + 50}px`
  group.open = !group.open
}
const onMousedown = (e: MouseEvent, node: Node.Metadata) => {
  let iNode: Node | undefined = undefined
  iNode = iData.graph?.createNode({
    shape: node.data.type,
    width: 220, height: 60,
    ports: node.ports ? node.ports : {},
    data: node.data ? node.data : {}
  })
  iData.graph && iNode && iData.dnd?.start(iNode, e)
}
const onMouseup = (e: Event, node: Node.Metadata) => {
  console.log(e, node)
}
const onSearch = () => {
  console.log('search component')
}
const registerVueComponent = (options: PropsOptions) => {
  options.stencil.nodes.forEach(group => {
    group.child.forEach((node) => {
      if (node.data.rule && typeof node.data.rule === 'function') mCStore.setRule(node.data.cells_type, node.data.rule)
      if (node.shape === 'vue-shape') {
        if (!mCStore.isRegisterNodes.includes(node.data.type)) {
          mCStore.isRegisterNodes.push(node.data.type)
          Graph.registerNode(node.data.type, {
            inherit: 'vue-shape',
            component: { render: () => <Nodes component={node.data.type} operate={operate} /> }
          })
        }
      }
    })
  })
}
const toggleMenu = () => {
  iData.open = !iData.open
}
defineExpose({ init, toggleMenu })
</script>
<style scoped lang="less">
  .left-sidebar-container {
    .left-sidebar {
      display: flex;
      flex-direction: column;
      padding-top: 15px;
      box-sizing: border-box;
      position: relative;
      width: 100%;
      height: 100%;
      transition: all .3s;
      overflow: hidden;
      box-shadow: 0px 0px 18px rgba(240, 240, 240, 0.16);
      .search {
        width: 100%;
        margin: 0 auto;
        box-sizing: border-box;
        padding: 0 10px;
        margin-bottom: 15px;
        .search-frame {
          display: flex;
          align-items: center;
          width: 100%;
          height: 30px;
          line-height: 30px;
          box-sizing: border-box;
          border: 1px solid transparent;
          background-color: var(--color-fill-2);
          border-radius: 2px;
          transition: border .3s;
        }
      }
      .content {
        border-top: 1px solid var(--color-border-2);
        box-sizing: border-box;
        width: 100%;
        min-width: 260px;
        padding: 15px 0 0;
        flex: 1;
        overflow-y: auto;
        .group {
          min-height: 40px;
          position: relative;
          .group-name {
            cursor: pointer;
            height: 40px;
            position: absolute;
            width: 100%;
            z-index: 1;
            .name {
              position: absolute;
              left: 0;
              top: 0;
              width: 100%;
              height: 40px;
              display: flex;
              align-items: center;
              padding: 0 10px 0 8px;
              color: var(--color-text-1);
              .group-arrow {
                transform: rotate(-90deg);
                margin-right: 10px;
                font-size: 14px;
                transition: transform .3s;
                margin-left: auto;
              }
              .text {
                font-size: 14px;
                margin-left: 10px;
              }
            }
          }
          .nodes {
            box-sizing: border-box;
            height: 0;
            overflow: hidden;
            transition: all .3s;
            opacity: 0;
            .node {
              height: 30px;
              cursor: pointer;
              position: relative;
              width: 230px;
              height: 50px;
              line-height: 40px;
              border: 1px solid var(--color-border-2);
              border-color: transparent;
              border-radius: 2px;
              margin: 0 auto 10px;
              box-sizing: border-box;
              padding-left: 20px;
              transition: all .5s;
              .node-title {
                display: flex;
                align-items: center;
                width: 100%;
                height: 100%;
                font-size: 14px;
                transition: all .1s;
                transform: translateX(0);
                color: var(--color-text-1);
                .node-title-icon {
                  width: 30px;
                  height: 30px;
                  background-color: var(--color-fill-2);
                  margin-right: 15px;
                  border: 1px solid #d7dae4;
                  border-radius: 4px;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                  position: relative;
                  .bs-i {
                    z-index: 1;
                    transition: all .5s;
                    font-size: 18px;
                    color: transparent;
                    fill: var(--color-text-2);
                  }
                  .active-bg {
                    position: absolute;
                    left: 0;
                    top: 0;
                    width: 100%;
                    background: linear-gradient(180deg,rgb(var(--primary-6)),rgba(var(--primary-6),.2));
                    height: 100%;
                    opacity: 0;
                    transition: all .5s;
                    border-radius: 3px;
                  }
                }
                .help {
                  margin-left: auto;
                  margin-right: 10px;
                  font-size: 20px;
                }
              }
              &:hover {
                box-shadow: 0 2px 15px var(--color-fill-4);
                border-radius: 2px;
                .node-title {
                  .node-title-icon {
                    .bs-i {
                      fill: #fff;
                    }
                    .active-bg {
                      opacity: 1;
                    }
                  }
                }
              }
            }
          }
        }
        .group-open {
          .group-name {
            .name {
              .group-arrow {
                transform: rotate(0);
              }
            }
          }
          .nodes {
            padding-top: 50px;
            opacity: 1;
          }
        }
      }
    }
    .open {
      width: 260px;
      height: 100%;
    }
    .close {
      width: 0;
      height: 100%;
    }
  }
</style>
