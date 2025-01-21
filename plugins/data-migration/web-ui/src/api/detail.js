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
  return axios.post('/host/ssh', data)
}

export function getEntryKey () {
  return axios.get('/encryption/getKey')
}

export function getdataTbl (formData, dbName, pageSize, pageNum) {
  return axios.post(`/plugins/data-migration/resource/tables/${dbName}?pageSize=${pageSize}&pageNum=${pageNum}`, formData)
}

export function getTotalAlarmNum (taskId) {
  return axios.get(`/plugins/data-migration/migration/alert/count/${taskId}`)
}

export function getPhaseAlarmList (taskId, migrationPhase, pageSize, pageNum) {
  return axios.get(`/plugins/data-migration/migration/alert/list/${taskId}/${migrationPhase}?pageSize=${pageSize}&pageNum=${pageNum}`)
}

export function getAlarmDetail (taskId) {
  return axios.get(`/plugins/data-migration/migration/alert/detail/${taskId}`)
}

export function queryFullCheckSummary (taskId) {
  return axios.get(`/plugins/data-migration/migration/query/full/check/summary/${taskId}`)
}

export function queryFullCheckDetail (fullCheckParam) {
  return axios.post(`/plugins/data-migration/migration/query/full/check/detail`, fullCheckParam)
}

export function getOnlineReverseStatus (taskId) {
  return axios.get(`/plugins/data-migration/migration/check/incremental/reverse/status/${taskId}`)
}

export function stopOnlineReverseProcess (taskId, name) {
  return axios.post(`/plugins/data-migration/migration/stop/online/reverse/task/process/${taskId}`, name)
}

export function startOnlineReverseProcess (taskId, name) {
  return axios.post(`/plugins/data-migration/migration/start/incremental/reverse/task/process/${taskId}?name=${name}`, name)
}

export function downloadRepairFile (id, repairFileName) {
  return axios.get(`/plugins/data-migration/migration/download/repair/file/${id}/${repairFileName}`)
}