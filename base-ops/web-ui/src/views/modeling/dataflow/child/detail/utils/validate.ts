import { Edge, Graph } from '@antv/x6'
import { Message } from '@arco-design/web-vue'
const baseNodes = ['query', 'update', 'delete', 'insert']
import i18n from '@/locale/index'
export const lineConnect = (source: Edge, target: Edge, graph: Graph): boolean => {
  const showMessage = (content: string) => {
    Message.warning({ content: content, duration: 5 * 1000 })
  }
  if (target.data.cells_type === 'dataSource') {
    showMessage(i18n.global.t('modeling.utils.index.5m835tc0m9o0'))
    return false
  }
  if (source.data.cells_type === 'dataSource') {
    if (!baseNodes.includes(target.data.cells_type)) {
      showMessage(i18n.global.t('modeling.utils.index.5m835tc0n8k0'))
      return false
    }
  }
  if (baseNodes.includes(source.data.cells_type) && baseNodes.includes(target.data.cells_type)) {
    showMessage(i18n.global.t('modeling.utils.index.5m835tc0ndo0'))
    return false
  }
  const edges: Edge[] = graph.getEdges()
  if (source.data.cells_type !== 'dataSource') {
    const sourceList: Edge[] = edges.filter((item: any) => item.source.cell === source.id)
    if (sourceList.length > 1) {
      showMessage(i18n.global.t('modeling.utils.index.5m835tc0nh00'))
      return false
    }
  }
  const targetList: Edge[] = edges.filter((item: any) => item.target.cell === target.id)
  if (targetList.length > 1) {
    showMessage(i18n.global.t('modeling.utils.index.5m835tc0njw0'))
    return false
  }
  return true
}
