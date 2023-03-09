import axios from 'axios'

export function taskDetail (id) {
  return axios.get(`/plugins/data-migration/migration/${id}`)
}

export function refreshStatus (id) {
  return axios.get(`/plugins/data-migration/migration/refreshStatus/${id}`)
}

export function subTaskList (id, query) {
  return axios.get(`/plugins/data-migration/migration/subTasks/${id}`, { params: query })
}

export function subTaskFinish (id) {
  return axios.post(`/plugins/data-migration/migration/subTask/finish/${id}`)
}

export function subTaskStartReverse (id) {
  return axios.post(`/plugins/data-migration/migration/subTask/start/reverse/${id}`)
}

export function subTaskStopIncremental (id) {
  return axios.post(`/plugins/data-migration/migration/subTask/stop/incremental/${id}`)
}

export function subTaskDetail (id) {
  return axios.get(`/plugins/data-migration/migration/subTaskInfo/${id}`)
}

export function downloadLog (id, query) {
  return axios.get(`/plugins/data-migration/migration/subTask/log/download/${id}`, { params: query })
}

export function taskEditInfo (id) {
  return axios.get(`/plugins/data-migration/migration/editInfo/${id}`)
}

export function openSSH (data) {
  return axios.post('/plugins/base-ops/opsCluster/ssh', data)
}

export function getEntryKey () {
  return axios.get('/encryption/getKey')
}
