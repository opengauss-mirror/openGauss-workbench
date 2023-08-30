import axios from 'axios'

export function listAllLogFiles () {
  return axios.get('/system/log/files')
}

export function getAllLogConfig () {
  return axios.get('/system/log')
}

export function saveLogConfig (params: any) {
  return axios.post('/system/log', params)
}

export function download (filename: string, onDownloadProgress?: (event: any)=> void) {
  return axios.get(`/system/log/download?filename=${filename}`, { onDownloadProgress: onDownloadProgress })
}

