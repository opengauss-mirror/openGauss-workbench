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

export function sourceClusterDbsData (query) {
  return axios.get('/plugins/data-migration/resource/getSourceClusterDbs', { params: query })
}

export function targetClusterDbsData (query) {
  return axios.get('/plugins/data-migration/resource/getTargetClusterDbs', { params: query })
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

export function installPortal (hostId, query) {
  return axios.get(`/plugins/data-migration/resource/installPortal/${hostId}`, { params: query })
}

export function reInstallPortal (hostId, query) {
  return axios.get(`/plugins/data-migration/resource/retryInstallPortal/${hostId}`, { params: query })
}

export function deletePortal (hostId) {
  return axios.get(`/plugins/data-migration/resource/deletePortal/${hostId}`)
}

