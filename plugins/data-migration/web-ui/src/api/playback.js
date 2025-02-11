import axios from 'axios'

export const addHost = (data) => {
  return axios.post('host', data)
}

export const hostPing = (data) => {
  return axios.post('host/ping', data)
}

export const hostTagListAll = () => {
  return axios.get(`hostTag/listAll`)
}

export const hostUserListAll = (hostId) => {
  return axios.get(`hostUser/listAll/${hostId}`)
}
export const addHostUser = (data) => {
  return axios.post('hostUser', data)
}

export const editHostUser = (hostUserId, data) => {
  return axios.put(`hostUser/${hostUserId}`, data)
}

export const delHostUser = (hostUserId) => {
  return axios.delete(`hostUser/${hostUserId}`)
}

export function hostListAll () {
  return axios.get('/host/listAll')
}
export function hostUsers (hostId) {
  return axios.get(`/hostUser/page/${hostId}`)
}
export function sourceClusterDbsData (formData) {
  return axios.post('/plugins/data-migration/resource/getSourceClusterDbs', formData)
}
export function sourceClusters (payload) {
  return axios.get('/plugins/data-migration/resource/sourceClusters', payload)
}
export function targetClusterDbsData (data) {
  return axios.post('/plugins/data-migration/resource/getTargetClusterDbs', data)
}
export function targetClusters (payload) {
  return axios.get('/plugins/data-migration/resource/targetClusters', payload)
}

export function transcribeReplaySave (data) {
  return axios.post('/plugins/data-migration/transcribeReplay/save', data)
}

export function transcribeReplaytoolsVersion () {
  return axios.get('/plugins/data-migration/transcribeReplay/toolsVersion')
}

export function transcribeReplaydownloadAndConfig (id, data) {
  return axios.post(`/plugins/data-migration/transcribeReplay/downloadAndConfig?id=${id}`, data)
}

export function transcribeReplayList (query) {
  return axios.get('/plugins/data-migration/transcribeReplay/list', { params: query })
}

export function transcribeReplayStart (id) {
  return axios.post(`/plugins/data-migration/transcribeReplay/start/${id}`)
}

export function transcribeReplayFinish (id) {
  return axios.post(`/plugins/data-migration/transcribeReplay/finish/${id}`)
}

export function transcribeReplayDelete (id) {
  return axios.post(`/plugins/data-migration/transcribeReplay/delete/${id}`)
}

export function getHostInfo (id) {
  return axios.get(`/plugins/data-migration/transcribeReplay/getHostInfo/${id}`)
}
export function getSlowSql (id, query) {
  return axios.get(`/plugins/data-migration/transcribeReplay/list/slowSql/${id}/`, { params: query })
}
export function getFailSql (id, query) {
  return axios.get(`/plugins/data-migration/transcribeReplay/list/failSql/${id}/`,{ params: query })
}
export function downloadFailSql (id) {
  return axios.get(`/plugins/data-migration/transcribeReplay/download/failSql/${id}`)
}

export function getSqlDuration (id) {
  return axios.get(`/plugins/data-migration/transcribeReplay/getSqlDuration/${id}`)
}
