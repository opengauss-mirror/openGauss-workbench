import axios from 'axios'
import { KeyValue } from '@/types/global'
import { downloadPackage, OpenLookengInstallConfig, SSHBody } from '@/types/ops/install'
import { UploadInfo } from '@/types/resource/package'
// import { hostData, hostUserData } from '@/types/resource/host'

// get Key
export const getEntryKey = () => {
  return axios.get('encryption/getKey')
}

export const download = (data: downloadPackage) => {
  return axios.post(`opsCluster/download`, data ? data : {})
}

export const getEnvMonitorData = (hostId: string, data: KeyValue) => {
  return axios.get(`opsCluster/env/${hostId}`, {
    params: data
  })
}

export const installOpenGauss = (data: KeyValue) => {
  return axios.post('opsCluster/install', data)
}

export const importOpenGauss = (data: KeyValue) => {
  return axios.post('opsCluster/import', data)
}

export const quickInstall = (data: KeyValue) => {
  return axios.post('opsCluster/quickInstall', data)
}

// upgrade

export const upgradeOsCheck = (data: KeyValue) => {
  return axios.get('opsCluster/upgradeOsCheck', {
    params: data
  })
}

export const upgrade = (data: KeyValue) => {
  return axios.post('opsCluster/upgrade', data)
}

export const upgradeCommit = (data: KeyValue) => {
  return axios.post('opsCluster/upgradeCommit', data)
}

export const upgradeRollback = (data: KeyValue) => {
  return axios.post('opsCluster/upgradeRollback', data)
}

// one check
export const clusterCheck = (data: KeyValue) => {
  return axios.get('opsCluster/check', {
    params: data
  })
}

export const uninstallOpenGauss = (data: KeyValue) => {
  return axios.post('opsCluster/uninstall', data)
}

export const hasName = (data: KeyValue) => {
  return axios.get('opsCluster/hasName', {
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

// just del platform data
export const delCluster = (clusterId: string) => {
  return axios.delete(`opsCluster/remove/${clusterId}`)
}

export const start = (data: KeyValue) => {
  return axios.post('opsCluster/start', data)
}

export const stop = (data: KeyValue) => {
  return axios.post('opsCluster/stop', data)
}

export const restart = (data: KeyValue) => {
  return axios.post('opsCluster/restart', data)
}

export const build = (data: KeyValue) => {
  return axios.get('opsCluster/build', {
    params: data
  })
}

export const switchover = (data: KeyValue) => {
  return axios.get('opsCluster/switchover', {
    params: data
  })
}

export const generateconf = (data: KeyValue) => {
  return axios.get('opsCluster/generateconf', {
    params: data
  })
}

export const getHostByClusterId = (data: KeyValue) => {
  return axios.get('opsCluster/listHost', {
    params: data
  })
}

// host interface
export const listInstallPackage = (data: any) => {
  return axios.get('opsCluster/listInstallPackage', {
    params: data
  })
}

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

export const hostPingById = (hostId: string, data: KeyValue) => {
  return axios.get(`host/ping/${hostId}`, {
    params: data
  })
}

export const delHost = (hostId: string) => {
  return axios.delete(`host/${hostId}`)
}

// Whether the directory is empty
export const pathEmpty = (hostId: string, data: KeyValue) => {
  return axios.get(`host/pathEmpty/${hostId}`, {
    params: data
  })
}

// Whether the port is used
export const portUsed = (hostId: string, data: KeyValue) => {
  return axios.get(`host/portUsed/${hostId}`, {
    params: data
  })
}

// Whether the file is exist
export const fileExist = (hostId: string, data: KeyValue) => {
  return axios.get(`host/fileExist/${hostId}`, {
    params: data
  })
}

export const multiPathQuery = (hostId: string, data: KeyValue) => {
  return axios.get(`host/multiPathQuery/${hostId}`, {
    params: data
  })
}

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

export const logPath = (data: KeyValue) => {
  return axios.get('opsCluster/logPath', {
    params: data
  })
}

export const getLs = (data: KeyValue) => {
  return axios.get('opsCluster/ls', {
    params: data
  })
}

export const slowSql = (data: KeyValue) => {
  return axios.get('opsCluster/slowSql', {
    params: data
  })
}

export const auditLog = (data: KeyValue) => {
  return axios.get('opsCluster/auditLog', {
    params: data
  })
}

export const downloadFile = (data: KeyValue) => {
  return axios.get('opsCluster/download', {
    params: data
  })
}

// WDR
export const wdrList = (data: KeyValue) => {
  return axios.get('wdr/list', {
    params: data
  })
}
export const wdrGenerate = (data: KeyValue) => {
  return axios.post('wdr/generate', data)
}

export const wdrDelete = (id: string) => {
  return axios.delete('wdr/del/' + id)
}

export const createSnapshot = (data: KeyValue) => {
  return axios.get('wdr/createSnapshot', {
    params: data
  })
}

export const downloadWdrFile = (data: KeyValue) => {
  return axios.get('wdr/downloadWdr', {
    params: data
  })
}

export const listSnapshot = (data: KeyValue) => {
  return axios.get('wdr/listSnapshot', {
    params: data
  })
}

// backup
export const clusterBackup = (data: KeyValue) => {
  return axios.post('backup/backup', data)
}
// backup page
export const backupPage = (data: KeyValue) => {
  return axios.get('backup/page', {
    params: data
  })
}
// backup recover
export const clusterRecover = (id: string, data: KeyValue) => {
  return axios.post('backup/recover/' + id, data)
}
// backup delete
export const backupDel = (id: string) => {
  return axios.delete('backup/del/' + id)
}

// package manage
export const packageListAll = (data: KeyValue) => {
  return axios.get('installPackageManager/list', {
    params: data
  })
}

export const packagePage = (data: KeyValue) => {
  return axios.get('installPackageManager/page', {
    params: data
  })
}

export const packageDetailById = (id: string) => {
  return axios.get('installPackageManager/detail/' + id)
}

export const packageDel = (id: string) => {
  return axios.delete('installPackageManager/' + id)
}

export const addPackage = (data: FormData) => {
  return axios.post('installPackageManager', data)
}

export const editPackage = (data: FormData) => {
  return axios.put(`installPackageManager`, data)
}

export const getPackageCpuArch = (data: KeyValue) => {
  return axios.get('installPackageManager/getCpuArch', {
    params: data
  })
}

export const analysisPkg = (pkgName: string, pkgType: string) => {
  return axios.get('installPackageManager/analysisPkg', {
    params: { pkgName: pkgName, pkgType: pkgType }
  })
}

export const delPkgTar = (path: string, id?: string) => {
  return axios.delete('installPackageManager/delPkgTar', { params: { path: path, id: id } })
}

export const getSysUploadPath = () => {
  return axios.get('installPackageManager/sysUploadPath')
}

export const hasPkgName = (name: string) => {
  return axios.get('/installPackageManager/hasName', { params: { name: name } })
}

export const generateRuleYaml = (data: KeyValue) => {
  return axios.post('olk/generateRuleYaml', data)
}

export const installOlk = (data: KeyValue) => {
  return axios.post('olk/install', data)
}

export const removeOlk = (id: string) => {
  return axios.delete('olk/remove/' + id)
}

export const startOlk = (id: string, bid: string) => {
  return axios.get('olk/start/' + id, {
    params: {
      bid: bid
    }
  })
}

export const stopOlk = (id: string, bid: string) => {
  return axios.get('olk/stop/' + id, {
    params: {
      bid: bid
    }
  })
}

export const destroyOlk = (id: string, bid: string) => {
  return axios.delete('olk/destroy/' + id, {
    params: {
      bid: bid
    }
  })
}

export const pageOlk = (query: any) => {
  return axios.get('olk/page', {
    params: query
  })
}

export const listGucSetting = (query: KeyValue) => {
  return axios.get('/opsCluster/listGucSetting', {
    params: query
  })
}

export const batchSetGucSetting = (data: KeyValue) => {
  return axios.post('/opsCluster/batchConfigGucSetting', data)
}

export const listEnterpriseCluster = () => {
  return axios.get('/opsCluster/listEnterpriseCluster')
}

export const listSupportOsName = () => {
  return axios.get('/opsCluster/listSupportOsName')
}

export const delPackage = (packageId: string) => {
  return axios.delete(`/installPackageManager/${packageId}`)
}

export const delPackageV2 = (data: KeyValue) => {
  return axios.post(`/installPackageManager/v2/delete/package`, data)
}

export const checkPackage = (data: KeyValue) => {
  return axios.post(`/installPackageManager/v2/check/package`, data)
}

export const checkPkg = (packageId: String) => {
  return axios.post(`/installPackageManager/v2/check/pkg?packageId=${packageId}`)
}

export const getVersionNum = () => {
  return axios.get(`/installPackageManager/v2/list/version/number`)
}

export const getPackageList = (data: KeyValue) => {
  return axios.post(`/installPackageManager/v2/list/package`, data)
}

export const getPackagePage = (data: KeyValue) => {
  return axios.post(`/installPackageManager/v2/page/package`, data)
}

export const batchPackageOnline = (data: FormData) => {
  return axios.post(`/installPackageManager/v2/save/online`, data)
}

export const batchPackageUpload = (name:KeyValue, os: KeyValue, cpuArch: KeyValue, packageVersion: KeyValue, packageVersionNum: KeyValue, packageUrl: KeyValue, file:any) => {
  return axios.post(`/installPackageManager/v2/save/upload?name=${name}&os=${os}&cpuArch=${cpuArch}&packageVersion=${packageVersion}&packageVersionNum=${packageVersionNum}&packageUrl=${packageUrl}`, file)
}

export const packageOnlineUpdate = (packageId:string, wsBusinessId:string) => {
  return axios.post(`/installPackageManager/v2/update/online?packageId=${packageId}&wsBusinessId=${wsBusinessId}`)
}

export const packageUploadUpdate = (params: KeyValue, data: KeyValue) => {
  return axios.post(`/installPackageManager/v2/update/upload${params}`, data)
}

export const checkNetStatus = () => {
  return axios.get(`/installPackageManager/v2/check/online`)
}

export const checkVersionNumber = (params: KeyValue) => {
  return axios.get(`/installPackageManager/v2/check/version/number`, {params:params})
}

export const getHostIp = (params: KeyValue) => {
  return axios.get(`/clusterTask/host/list`, {params})
}

export const getHostUser = (hostId: string) => {
  return axios.get(`/clusterTask/hostUser/${hostId}`)
}

export const getHostInfo = (hostIp: string) => {
  return axios.get(`/clusterTask/host/${hostIp}`)
}

export const createClusterTask = (data: KeyValue) => {
  return axios.post(`/clusterTask/create`, data)
}

export const checkDiskSpace = (data: KeyValue, hostId: string) => {
  return axios.post(`/clusterTask/check/host/disk/space/${hostId}`, data)
}

export const checkPortExist = (hostId: string, hostPort: number, clusterId: string) => {
  return axios.get(`/clusterTask/hostPort/${hostId}/${hostPort}?taskId=${clusterId}`)
}

export const checkClusternameExist = (clusterName: string, clusterId: string) => {
  return axios.get(`/clusterTask/check/name?clusterName=${clusterName}&clusterId=${clusterId}`)
}

export const checkClusterExist = (hostIp: string, hostUsername: number) => {
  return axios.get(`/clusterTask/check/host?hostIp=${hostIp}&hostUsername=${hostUsername}`)
}

export const batchClusterNodes = (clusterId: string) => {
  return axios.get(`/clusterTask/detail/${clusterId}`)
}

export const updateClusterTask = (data: KeyValue) => {
  return axios.post(`/clusterTask/update`, data)
}

export const clusterEnvCheck = (clusterId: string) => {
  return axios.post(`/clusterTask/check/environment?taskId=${clusterId}`)
}

export const envCheckResult = (clusterId: string) => {
  return axios.get(`/clusterTask/get/environment?taskId=${clusterId}`)
}

export const fetchAZList = () => {
  return axios.get(`/clusterTask/list/az`)
}

export const createClustertaskNode = (data: KeyValue) => {
  return axios.post(`/cluster/task/node/create`, data)
}

export const updateClustertaskNode = (data: KeyValue) => {
  return axios.post(`/cluster/task/node/update`, data)
}

export const deleteClustertaskNode = (clusterId: string, clusterNodeId: string, data: KeyValue) => {
  return axios.post(`/cluster/task/node/delete?clusterId=${clusterId}&nodeId=${clusterNodeId}`, data)
}

export const checkCluster = (clusterId: string) => {
  return axios.post(`/clusterTask/check?clusterId=${clusterId}`)
}

export const clusterTaskPage = (mutiSearchData: KeyValue, data: KeyValue) => {
  return axios.post("/clusterTask/page", mutiSearchData, {
    params: data
  })
}

export const copyTask = (clusterId: KeyValue) => {
  return axios.post(`/clusterTask/copy?taskId=${clusterId}`)
}

export const batchDeleteTask = (clusterIds: any) => {
  return axios.post(`/clusterTask/delete`, clusterIds)
}

export const clusterLogDownload = (clusterId: string) => {
  return axios.get(`/opsClusterLog/download/log?clusterId=${clusterId}`,{responseType: 'blob',headers: {'Content-Type':'application/json;application/octet-stream'}})
}

export const submitCluster = (clusterId: string) => {
  return axios.post(`/clusterTask/confirm?taskId=${clusterId}`)
}

export const executeTask = (clusterIds: any) => {
  return axios.post(`/clusterTask/batch/install`, clusterIds)
}

export const checkTaskStatus = (clusterIds: any) => {
  return axios.post(`/clusterTask/batch/install/status`, clusterIds)
}

export const reExecuteTask = (clusterId: number) => {
  return axios.post(`/clusterTask/re/install?taskId=${clusterId}`)
}

export const hostSSHByHostId = (hostId: string, data: KeyValue) => {
  return axios.post('host/ssh/' + hostId, data)
}
