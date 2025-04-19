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

// 获取插件列表扩展信息
export function extendInfoList (query?: any) {
  return axios.get('/system/plugins/extensions/list', { params: query })
}

export function unloadPluginsInfo () {
  return axios.get('/system/plugins/unloadPluginsInfo')
}

export function getUnloadpluginUrl (pluginId: string) {
  return axios.post(`/system/plugins/getUnloadPluginUrl?pluginId=${pluginId}`)
}

export function downloadPluginWs (params: any) {
  return axios.post(`/system/plugins/online_install`, params)
}

export function uploadPluginWs (params: any) {
  return axios.post(`/system/plugins/offline_install`, params)
}

