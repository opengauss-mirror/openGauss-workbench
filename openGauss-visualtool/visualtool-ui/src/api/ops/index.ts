import axios from 'axios'
import { KeyValue } from '@/types/global'
import { SSHBody } from '@/types/ops/install'

// get Key
export const getEntryKey = () => {
  return axios.get('encryption/getKey')
}

// one check
export const clusterCheck = (data: KeyValue) => {
  return axios.get('opsCluster/check', {
    params: data
  })
}

export const clusterList = () => {
  return axios.get('opsCluster/listCluster')
}

export const getSummary = () => {
  return axios.get('opsCluster/summary')
}

export const openSSH = (data: SSHBody) => {
  return axios.post('opsCluster/ssh', data)
}

export const clusterMonitor = (data: KeyValue) => {
  return axios.get('opsCluster/monitor', {
    params: data
  })
}

// Home Statistics Interface
// Get the number of streams
export const getDataFlowCount = () => {
  return axios.get('modeling/dataflow/count')
}

// Get the number of business flows
export const getBusiFlowCount = () => {
  return axios.get('modeling/workflow/count')
}

// Get the number of data models
export const getDataModelCount = () => {
  return axios.get('modeling/databaseDesign/count')
}

// Get business flow type distribution data
export const getBusiFlowType = () => {
  return axios.get('modeling/dataflow/distribution')
}

// Get the list of business flow running instances
export const getBusiFlowList = () => {
  return axios.get('modeling/dataflow/processInfo')
}

// Get the number of plugins
export const getPluginCount = () => {
  return axios.get('system/plugins/count')
}

export const getHostByClusterId = (data: KeyValue) => {
  return axios.get('opsCluster/listHost', {
    params: data
  })
}

// Host management related

// page
export const hostPage = (query: any) => {
  return axios.get('host/page', {
    params: query
  })
}

export const hostListAll = () => {
  return axios.get('host/listAll')
}

export const addHost = (data: KeyValue) => {
  return axios.post('host', data)
}

export const editHost = (hostId: string, data: KeyValue) => {
  return axios.put(`host/${hostId}`, data)
}

export const hostPing = (data: KeyValue) => {
  return axios.post('host/ping', data)
}

export const hostSSH = (data: KeyValue) => {
  return axios.post('host/ssh', data)
}

export const hostPingById = (hostId: string, data: KeyValue) => {
  return axios.get(`host/ping/${hostId}`, {
    params: data
  })
}

export const delHost = (hostId: string) => {
  return axios.delete(`host/${hostId}`)
}

// host user
export const hostUserPage = (hostId: string) => {
  return axios.get(`hostUser/page/${hostId}`)
}

export const hostUserListAll = (hostId: string) => {
  return axios.get(`hostUser/listAll/${hostId}`)
}

export const hostUserListWithoutRoot = (hostId: string) => {
  return axios.get(`hostUser/listAllWithoutRoot/${hostId}`)
}

export const addHostUser = (data: KeyValue) => {
  return axios.post('hostUser', data)
}

export const editHostUser = (hostUserId: string, data: KeyValue) => {
  return axios.put(`hostUser/${hostUserId}`, data)
}

export const delHostUser = (hostUserId: string) => {
  return axios.delete(`hostUser/${hostUserId}`)
}

// az management
export const azListAll = () => {
  return axios.get('az/listAll')
}

export const addAz = (data: KeyValue) => {
  return axios.post('az', data)
}

export const editAz = (azId: string, data: KeyValue) => {
  return axios.put(`az/${azId}`, data)
}

// page
export const azPage = (query: any) => {
  return axios.get('az/page', {
    params: query
  })
}

export const delAz = (azId: string) => {
  return axios.delete(`az/${azId}`)
}

export const hasNameAZ = (data: KeyValue) => {
  return axios.get('az/hasName', {
    params: data
  })
}

// jdbc 

export const downloadTemplate = () => {
  return axios.get('jdbcDbCluster/downloadTemplate')
}

export const jdbcPage = (query: any) => {
  return axios.get('jdbcDbCluster/page', {
    params: query
  })
}

export const addJdbc = (data: KeyValue) => {
  return axios.post('jdbcDbCluster/add', data)
}

export const editJdbc = (id: string, data: KeyValue) => {
  return axios.put(`jdbcDbCluster/${id}`, data)
}

export const jdbcPing = (data: KeyValue) => {
  return axios.post('jdbc/ping', data)
}

export const delJdbc = (jdbcId: string) => {
  return axios.delete(`jdbcDbCluster/${jdbcId}`)
}

export const uploadFileJdbc = (data: KeyValue) => {
  return axios.post('jdbcDbCluster/importAnalysis', data)
}

export const uploadRealJdbc = (data: KeyValue) => {
  return axios.post('jdbcDbCluster/importCluster', data)
}

// cluster node ping
export const jdbcNodePing = (data: KeyValue) => {
  return axios.post('jdbcDbClusterNode/ping', data)
}

export const jdbcNodeMonitor = (clusterNodeId: any, data: KeyValue) => {
  return axios.get(`jdbcDbClusterNode/monitor/${clusterNodeId}`, {
    params: data
  })
}