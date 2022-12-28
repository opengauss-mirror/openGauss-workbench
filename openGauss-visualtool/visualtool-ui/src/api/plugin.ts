import axios from 'axios'

export function list (query?: any) {
  return axios.get('/system/plugins/list', { params: query })
}

export function start (id: string | number) {
  return axios.post(`/system/plugins/start/${id}`)
}

export function stop (id: string | number) {
  return axios.post(`/system/plugins/stop/${id}`)
}

export function uninstall (id: string | number) {
  return axios.post(`/system/plugins/uninstall/${id}`)
}

export function pluginConfigData (payload: any) {
  return axios.post('/system/plugins/pluginConfigData', payload)
}
