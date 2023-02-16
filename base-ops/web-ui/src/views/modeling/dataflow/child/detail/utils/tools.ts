import { Graph } from '@antv/x6'

export const getCheckedNodes = (graph: Graph) => {
  let nodes = graph.getNodes()
  nodes = nodes.filter(item => {
    return Boolean(item.data && item.data.antvSelected)
  })
  return nodes
}
