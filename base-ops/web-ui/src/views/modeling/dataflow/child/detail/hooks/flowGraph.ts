import { useModelCommonStore } from '@/store/modules/modeling/common'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { saveJsonData } from '@/api/modeling'
import { Cell, Edge, FunctionExt, Graph, Node, Shape } from '@antv/x6'
import { Options } from '@antv/x6/lib/graph/options'
import { RouteLocationNormalizedLoaded } from 'vue-router'
import { KeyValue } from '@antv/x6/lib/types'
import { PropsOptions } from '../types'
import { lineConnect } from '../utils/validate'
import { getCheckedNodes } from '../utils/tools'
const dFStore = useDataFlowStore()
const mCStore = useModelCommonStore()
const edgeConfig = {
  attrs: {
    line: {
      stroke: '#ECC300', strokeWidth: 2,
      targetMarker: { name: 'classic', size: 8 }
    }
  },
  zIndex: 0,
  connector: { name: 'rounded' },
  router: { name: 'manhattan' }
}

let gridType = 1
const gridModal = {
  size: 10, visible: true, type: 'doubleMesh',
  args: [
    { color: 'rgba(136, 136, 136, .2)', thickness: 1 },
    { color: 'rgba(136, 136, 136, .2)', thickness: 1, factor: 4 }
  ]
}
const t = localStorage.getItem('dianayako_antvx6_grid_type')
if (t) gridType = t ? Number(t) : 1

export default class FlowGraph {
  public static graph: Graph
  private static option: Partial<Options.Manual> ={
    width: 1920, height: 1080, autoResize: true,
    background: { color: 'var(--color-bg-1)' },
    grid: gridType === 1 ? gridModal : undefined,
		interacting: function (cellView) {
      const data = cellView.cell.getData()
      if (data && data.disableMove) return false
      return true
    },
    scroller: {
      enabled: true, pageVisible: false, pageBreak: true, pannable: false, padding: 0
    },
    mousewheel: {
      enabled: true, modifiers: ['ctrl', 'meta'], minScale: 0.5, maxScale: 2
    },
    selecting: {
      enabled: true, multiple: true, rubberband: true, movable: true
    },
    connecting: {
      anchor: 'center', connectionPoint: 'anchor', allowBlank: false, allowMulti: true, highlight: true,
      snap: { radius: 30 },
      createEdge () {
        return new Shape.Edge(edgeConfig)
      },
      validateConnection: ({
        sourceView,
        targetView,
        sourceMagnet,
        targetMagnet
      }: KeyValue) => {
        if (sourceView === targetView)
          return false
        if (!sourceMagnet)
          return false
        if (!targetMagnet)
          return false
        return true
      }
    },
    highlighting: {
      magnetAvailable: {
        name: 'stroke',
        args: {
          padding: 4,
          attrs: { strokeWidth: 4, stroke: 'rgba(200,200,200)' }
        }
      }
    },
    snapline: true,
    history: {
      enabled: true,
      ignoreAdd: false,
      ignoreRemove: false,
      ignoreChange: true
    },
    clipboard: true, keyboard: true,
    embedding: {
      enabled: true,
      findParent (this: { getNodes: any }, { node }: { node: Node }): any {
        const bbox = node.getBBox()
        return this.getNodes().filter((node: Node) => {
          const data = node.getData()
          if (data && data.parent) {
            const targetBBox = node.getBBox()
            return bbox.isIntersectWithRect(targetBBox)
          }
          return false
        })
      }
    }
  }
  public static init (options: PropsOptions, route: RouteLocationNormalizedLoaded, containerInfo: HTMLElement | undefined, callbacks?: KeyValue) {
    if (containerInfo && containerInfo.offsetWidth && containerInfo.offsetHeight) {
    }
    this.option.container = document.getElementById(options.containerId) as HTMLElement
    if (this.option.connecting) {
      this.option.connecting!.validateEdge = ({ edge }: any) => {
        if (callbacks && callbacks.edgeConnect) callbacks.edgeConnect()
        return lineConnect(this.graph.getCellById(edge.source.cell) as Edge, this.graph.getCellById(edge.target.cell) as Edge, this.graph)
      }
    }
    this.graph = new Graph(options.canvasOption ? { ...this.option, ...options.canvasOption } : this.option)
    this.initEvent(options, route, callbacks)
    return this.graph
  }
  private static initEvent (options: PropsOptions, route: RouteLocationNormalizedLoaded, callbacks?: KeyValue) {
    const { graph } = this
    const container = document.getElementById(options.containerId) as HTMLElement
    const checkDisabled = ({ cell }: { cell: Cell }) => {
      const ports = container.querySelectorAll('.x6-port-body') as NodeListOf<SVGAElement>
      if (cell.data && cell.data.disabled) this.togglePorts(ports, false)
      else this.togglePorts(ports, true)
    }
    graph.on('node:mouseenter', FunctionExt.debounce(({ node }: { node: Node }) => {
      node.setData({ showDisabledCheckbox: true })
      const ports = container.querySelectorAll('.x6-port-body') as NodeListOf<SVGAElement>
      if (!node.data.disabled) this.togglePorts(ports, true)
      node.on(`change:data`, checkDisabled)
    }), 500)
    graph.on('node:mouseleave', ({ node }: { node: Node }) => {
      node.setData({ showDisabledCheckbox: false })
      const ports = container.querySelectorAll(
        '.x6-port-body'
      ) as NodeListOf<SVGAElement>
      this.togglePorts(ports, false)
      node.off(`change:data`, checkDisabled)
    })
    graph.on('node:collapse', ({ node, e }: any) => {
      e.stopPropagation()
      node.toggleCollapse()
      const collapsed = node.isCollapsed()
      const cells = node.getDescendants()
      cells.forEach((n: any) => collapsed ? n.hide() : n.show)
    })
    graph.on(`cell:selected`, ({ cell }: { cell: Cell }) => {
      cell.setData({ antvSelected: true })
    })
    graph.on(`cell:unselected`, ({ cell }: { cell: Cell }) => {
      cell.setData({ antvSelected: false })
    })
    graph.on('blank:contextmenu', () => {
      if (callbacks && callbacks.blankContextmenu) callbacks.blankContextmenu()
    })
    graph.on('blank:click', () => {
      const nodes = graph.getNodes()
      nodes.forEach(item => {
        if (item.data.antvSelected) {
          item.setData({ antvSelected: false })
        }
      })
      mCStore.setSelectNode(null, false)
      graph.unselect(getCheckedNodes(graph))
    })
    graph.on('edge:mouseenter', ({ edge }) => {
      if (!edge.data || !edge.data.operate || (edge.data.operate && Array.isArray(edge.data.operate) && edge.data.operate.includes('showChangeArrow'))) {
        edge.addTools([
          'source-arrowhead',
          'target-arrowhead'
        ])
      }
    })
    graph.on('edge:mouseleave', ({ edge }) => {
      if (!edge.data || !edge.data.operate || (edge.data.operate && Array.isArray(edge.data.operate) && edge.data.operate.includes('showChangeArrow'))) {
        edge.removeTools()
      }
    })
    graph.bindKey('delete', () => {
      const cells = graph.getSelectedCells()
      if (cells.length) {
        const deleteArr = cells.filter((item: Cell) => (!item.data.operate || item.data.operate.includes('delete')))
        graph.removeCells(deleteArr)
      }
    })
    graph.bindKey([`ctrl+s`], () => {
      const jsonData: { cells: Array<KeyValue> } = graph.toJSON()
      jsonData.cells.forEach((item: KeyValue) => {
        if (item.data && item.data.cells_type) item.cells_type = item.data.cells_type
        else if (item.shape === 'edge') item.cells_type = 'line'
      })
      saveJsonData(dFStore.getFlowDataInfo.id as string, jsonData)
    })
    graph.bindKey([`ctrl+c`], () => {
      graph.copy(getCheckedNodes(graph), { useLocalStorage: true })
    })
    graph.bindKey([`ctrl+x`], () => {
      graph.cut(getCheckedNodes(graph), { useLocalStorage: true })
    })
    graph.bindKey([`ctrl+v`], () => {
      graph.paste({ useLocalStorage: true })
    })
    graph.bindKey([`ctrl+z`], () => {
      graph?.undo()
    })
    graph.bindKey([`ctrl+y`], () => {
      graph?.redo()
    })
    graph.on(`cell:removed`, (cell: KeyValue) => {
      if (cell.cell && cell.cell.cells_type === 'join') {
        dFStore.removeDatabaseTable(cell.cell.table)
      }
      if (callbacks && callbacks.deleteCell) callbacks.deleteCell()
    })
  }
  private static togglePorts (ports: NodeListOf<SVGAElement>, show: boolean) {
    for (let i = 0, len = ports.length; i < len; i = i + 1) {
      ports[i].style.visibility = show ? 'visible' : 'hidden'
    }
  }
  private static createEdge (source: any, target: any, data?: KeyValue) {
    this.graph.addEdge({
      shape: 'edge',
      source,
      target,
      ...edgeConfig,
      data: data ? data : {}
    })
  }
  public static renderInitJson = () => {
    const item = dFStore.getFlowDataInfo
    const json = JSON.parse(item.operatorContent)
    if (json && json.cells && json.cells.length > 0) {
      json.cells.forEach((item: KeyValue) => {
        if ((item.cells_type in mCStore.getRules) && typeof mCStore.getRules[item.cells_type] === 'function') {
          item.data.rule = mCStore.getRules[item.cells_type]
        }
      })
      this.graph.fromJSON(json)
    } else {
    }
  }
  private static createNode = (options: PropsOptions, configName: string, x?: number, y?: number) => {
    if (configName) {
      let tNode = null
      options.stencil.nodes.forEach(group => {
        group.child.forEach(node => {
          if (node.data.configName === configName) {
            tNode = this.graph.createNode({
              shape: 'BaseNode',
              x: (this.option.width ? this.option.width : 1920) / 2 + (x ? x : 0), y: (this.option.height ? this.option.height / 2 : 1080) / 2 + 60 + (y ? y : 0),
              width: 220, height: 60,
              ports: node.ports ? node.ports : {},
              data: node.data ? node.data : {}
            })
          }
        })
      })
      tNode && this.graph.addNode(tNode)
      return tNode
    }
  }

  public static toggleGridType = () => {
    gridType = gridType === 1 ? 2 : 1
    localStorage.setItem('dianayako_antvx6_grid_type', String(gridType))
    if (gridType === 1) {
      this.graph.drawGrid(gridModal)
    } else {
      this.graph.drawGrid(undefined)
    }
  }
}
