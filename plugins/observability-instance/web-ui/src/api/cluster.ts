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
export async function getClusterNodes(clusterId: string): Promise<void | ClusterNode[]> {
  return ogRequest.get('/instanceMonitoring/api/v1/clusters/' + clusterId + '/nodes')
}
