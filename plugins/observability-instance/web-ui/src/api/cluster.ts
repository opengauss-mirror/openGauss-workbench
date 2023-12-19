import ogRequest from '@/request'

export type ClusterListItem = {
  clusterId: string
  versionNum: string
  databaseUsername: string
  port: string
  clusterState: string
  envPath: string
  arch: string
  desc: string
  color: string
  nodeId: string
}
export async function getAllClusters(): Promise<void | ClusterListItem[]> {
  return ogRequest.get('/instanceMonitoring/api/v1/clusters/list')
}

export type ClusterStateItem = {
  clusterId: string
  versionNum: string
  databaseUsername: string
  port: string
  clusterState: string
  envPath: string
  desc: string
  primaryNodeId: string
}
export async function getAllClustersStates(): Promise<void | ClusterStateItem[]> {
  return ogRequest.get('/instanceMonitoring/api/v1/clusters/allClusterState')
}

export type NodeStateItem = {
  clusterId: string
  sync: string
  nodeName: string
  syncState: any
}
export async function getAllNodes(): Promise<void | NodeStateItem[]> {
  return ogRequest.get('/instanceMonitoring/api/v1/clusters/nodes')
}

export type ClusterDetail = {}
export async function getClusterDetails(clusterId: string): Promise<void | ClusterDetail[]> {
  let start = 0
  let end = 0
  let min = 0
  let now = new Date()
  min = 1 * 60
  start = Number.parseInt(`${(now.getTime() - 1000 * min * 60) / 1000}`)
  end = Number.parseInt(`${now.getTime() / 1000}`)
  let step = Math.max(14, Number.parseInt(`${Math.round((end - start) / 260)}`))
  return ogRequest.get(
    '/instanceMonitoring/api/v1/clusters/' + clusterId + '/metrics?start=' + start + '&end=' + end + '&step=' + step
  )
}

export type ClusterNode = {}
export type ClusterRelation = {}
export type ClusterNodesResult = {
  nodeList: ClusterNode[]
  relation: ClusterRelation[]
}
export async function getClusterNodes(clusterId: string): Promise<void | ClusterNodesResult> {
  return ogRequest.get('/instanceMonitoring/api/v1/clusters/' + clusterId + '/nodes')
}

export type ClusterProportion = {
  value: string
  color: string
  num: number
}
export type Top5 = {
  nodeName: string
  value: string
  ratio: number
}
export type Statistics = {
  cluster: {
    total: number
    proportion: ClusterProportion[]
  }
  nodeStat: {
    total: number
    proportion: ClusterProportion[]
  }
  nodeSyncStat: {
    total: number
    proportion: ClusterProportion[]
  }
  top5: Top5[]
}
export async function getStatistics(): Promise<void | Statistics> {
  return ogRequest.get('/instanceMonitoring/api/v1/clusters/statistics')
}

export type switchRecord = {}
export async function getSwitchRecords(clusterId: String, pageSize: number, pageNum: number, start: String, end: String): Promise<void | switchRecord[]> {
  return ogRequest.get('/instanceMonitoring/api/v1/clusters/' + clusterId + '/switchRecord', {
    start,
    end,
    pageSize,
    pageNum
  })
}
