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