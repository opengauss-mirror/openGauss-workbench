import axios from 'axios'
import { KeyValue } from '@/types/global'
import { downloadPackage, SSHBody } from '@/types/ops/install'

// get Key
export const getEntryKey = () => {
  return axios.get('encryption/getKey')
}

// get Loginkey
export const getLoginKey = () => {
  return axios.get('pubKey')
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
  return axios.get('plugins/base-ops/modeling/dataflow/count')
}

// Get the number of business flows
export const getBusiFlowCount = () => {
  return axios.get('plugins/base-ops/modeling/workflow/count')
}

// Get the number of data models
export const getDataModelCount = () => {
  return axios.get('plugins/base-ops/modeling/databaseDesign/count')
}

// Get business flow type distribution data
export const getBusiFlowType = () => {
  return axios.get('plugins/base-ops/modeling/dataflow/distribution')
}

// Get the list of business flow running instances
export const getBusiFlowList = () => {
  return axios.get('plugins/base-ops/modeling/dataflow/processInfo')
}
// Get the status of Base-ops
export const isBaseOpsStart = () => {
  return axios.get('system/plugins/isBaseOpsStart')
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

export const hostSSHByHostId = (hostId: string, data: KeyValue) => {
  return axios.post('host/ssh/' + hostId, data)
}

export const hostMonitor = (data: KeyValue) => {
  return axios.get('host/monitor', {
    params: data
  })
}

export const pageMonitor = (hostIds: string [], businessId: string) => {
  return axios.post(`host/monitor?businessId=${businessId}`, hostIds)
}

export const hostPingById = (hostId: string, data: KeyValue) => {
  return axios.get(`host/ping/${hostId}`, {
    params: data
  })
}

export const delHost = (hostId: string) => {
  return axios.delete(`host/${hostId}`)
}

export const stopHostAgent = (agentId: string) => {
  return axios.post(`agent/stop?agentId=${agentId}`)
}

export const startHostAgent = (agentId: string) => {
  return axios.post(`agent/start?agentId=${agentId}`)
}

export const uninstallAgent = (agentId: string) => {
  return axios.post(`agent/uninstall?agentId=${agentId}`)
}

export const listAgent = (data: KeyValue) => {
  return axios.get(`agent/list`, {
    params: data
  })
}

// host tag
export const hostBatchAddTag = (data: KeyValue) => {
  return axios.put('hostTag/addTag', data)
}

export const hostBatchDelTag = (data: KeyValue) => {
  return axios.put('hostTag/delTag', data)
}

export const hostTagListAll = () => {
  return axios.get(`hostTag/listAll`)
}

export const hostTagPage = (data: KeyValue) => {
  return axios.get('hostTag/page', {
    params: data
  })
}

export const hostTagAdd = (data: KeyValue) => {
  return axios.post('hostTag/add', data)
}

export const hostTagUpdate = (id: string, data: KeyValue) => {
  return axios.put('hostTag/update/' + id, data)
}

export const hostTagDel = (id: string) => {
  return axios.delete('hostTag/del/' + id)
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

export const bulkuploadPhy = (data: KeyValue) => {
  return axios.post(`/host/upload`, data)
}

export const bulkImportany = (data: KeyValue) => {
  return axios.post(`host/invokeFile`, data)
}

export const bulkImporErrPlan = (data: KeyValue) => {
  return axios.get(`/host/get_import_plan`, data)
}

export const delPackage = (packageId: string) => {
  return axios.delete(`/plugins/base-ops/installPackageManager/${packageId}`)
}

export const delPackageV2 = (data: KeyValue) => {
  return axios.post(`/plugins/base-ops/installPackageManager/v2/delete/package`, data)
}

export const checkPackage = (data: KeyValue) => {
  return axios.post(`/plugins/base-ops/installPackageManager/v2/check/package`, data)
}

export const getVersionNum = () => {
  return axios.get(`/plugins/base-ops/installPackageManager/v2/list/version/number`)
}

export const getPackageList = (data: FormData) => {
  return axios.get(`/plugins/base-ops/installPackageManager/v2/list/package`, { data })
}

export const getPackagePage = (pageSize: number, pageNum:number, data: KeyValue) => {
  return axios.post(`/plugins/base-ops/installPackageManager/v2/page/package?pageSize=${pageSize}&pageNum=${pageNum}`, data)
}

export const batchPackageOnline = (data: FormData) => {
  return axios.post(`/plugins/base-ops/installPackageManager/v2/save/online`, data)
}

export const batchPackageUpload = (uploadFileParam:any) => {
  return axios.post(`/plugins/base-ops/installPackageManager/v2/save/upload`, uploadFileParam)
}

export const packageOnlineUpdate = (params: URLSearchParams, config: KeyValue) => {
  return axios.post(`/plugins/base-ops/installPackageManager/v2/update/online`, params, config)
}

export const packageUploadUpdate = (params: KeyValue, data: KeyValue) => {
  return axios.post(`/plugins/base-ops/installPackageManager/v2/update/upload${params}`, data)
}

export const checkNetStatus = () => {
  return axios.get(`/plugins/base-ops/installPackageManager/v2/check/online`)
}

export const checkVersionNumber = (params: KeyValue) => {
  return axios.get(`/plugins/base-ops/installPackageManager/v2/check/version/number`, { params: params })
}

export const getSysUploadPath = () => {
  return axios.get('/plugins/base-ops/installPackageManager/sysUploadPath')
}

export const hasPkgName = (name: string) => {
  return axios.get('/plugins/base-ops/installPackageManager/hasName', { params: { name: name }})
}

export const download = (data: downloadPackage) => {
  return axios.post(`/plugins/base-ops/opsCluster/download`, data ? data : {})
}

export const delPkgTar = (path: string, id?: string) => {
  return axios.delete('/plugins/base-ops/installPackageManager/delPkgTar', { params: { path: path, id: id }})
}

export const addAgent = (data: KeyValue) => {
  return axios.post('/agent/install', data)
}
