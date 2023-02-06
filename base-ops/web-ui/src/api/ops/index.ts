import axios from 'axios'
import { KeyValue } from '@/types/global'
import { downloadPackage, SSHBody } from '@/types/ops/install'
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

export const hostPingById = (hostId: string) => {
  return axios.get(`host/ping/${hostId}`)
}

export const delHost = (hostId: string) => {
  return axios.delete(`host/${hostId}`)
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

export const addPackage = (data: KeyValue) => {
  return axios.post('installPackageManager/save', data)
}

export const editPackage = (azId: string, data: KeyValue) => {
  return axios.put(`installPackageManager/update/${azId}`, data)
}