import axios from 'axios'

export function sourceClusters (payload) {
  return axios.get('/plugins/data-migration/resource/sourceClusters', payload)
}

export function targetClusters (payload) {
  return axios.get('/plugins/data-migration/resource/targetClusters', payload)
}

export function hostsData () {
  return axios.get('/plugins/data-migration/resource/getHosts')
}

export function sourceClusterDbsData (formData) {
  return axios.post('/plugins/data-migration/resource/getSourceClusterDbs', formData)
}

export function targetClusterDbsData (data) {
  return axios.post('/plugins/data-migration/resource/getTargetClusterDbs', data)
}

export function migrationSave (payload) {
  return axios.post('/plugins/data-migration/migration/save', payload)
}

export function migrationUpdate (payload) {
  return axios.post('/plugins/data-migration/migration/update', payload)
}

export function defaultParams () {
  return axios.get('/plugins/data-migration/param/default')
}

export function jdbcNodePing (data) {
  return axios.post('/jdbcDbClusterNode/ping', data)
}

export function addJdbc (data) {
  return axios.post('/jdbcDbCluster/add', data)
}

export function hostListAll () {
  return axios.get('/host/listAll')
}

export function downloadEnvLog (hostId) {
  return axios.get(`/plugins/data-migration/resource/log/downloadEnv/${hostId}`)
}

export function hostUsers (hostId) {
  return axios.get(`/plugins/data-migration/resource/hostUsers/${hostId}`)
}

export function installPortal (hostId, params) {
  return axios.post(`/plugins/data-migration/resource/installPortal/${hostId}`, params)
}

export function installPortalFromDatakit (hostId, params) {
  return axios.post(`/plugins/data-migration/resource/installPortalFromDatakit/${hostId}`, params)
}

export function reInstallPortal (hostId, params) {
  return axios.post(`/plugins/data-migration/resource/retryInstallPortal/${hostId}`, params)
}

export function deletePortal (hostId, onlyPkg = false) {
  return axios.delete(`/plugins/data-migration/resource/deletePortal/${hostId}`, { params: { onlyPkg: onlyPkg }})
}

export function listHostUserByHostIds (hostIds) {
  return axios.get('/plugins/data-migration/resource/listAllHostUser', { params: { hostIds: hostIds }})
}

export function listKafkaInstance (data) {
  return axios.post('/plugins/data-migration/MQConfig/list', data)
}

export function listToolsParams (hostId) {
  return axios.get(`/plugins/data-migration/toolsParam/list/${hostId}`)
}

export function modToolsParams (globalToolsParams) {
  return axios.post('/plugins/data-migration/toolsParam/modify', globalToolsParams)
}

export function removeToolsParams (toolsParmaId) {
  return axios.get(`/plugins/data-migration/toolsParam/delete/${toolsParmaId}`)
}

export function saveToolsParams (globalToolsParam) {
  return axios.post('/plugins/data-migration/toolsParam/save', globalToolsParam)
}

export function hasParamKey (paramKey,configId,portalHostID) {
  return axios.get(`/plugins/data-migration/toolsParam/hasParamKey/?paramKey=${paramKey}&configId=${configId}&portalHostID=${portalHostID}`)
}

export function isAdmin (data) {
  return axios.post('/plugins/data-migration/resource/isAdmin', data)
}

