import axios from 'axios'

export function getSysSetting () {
    return axios.get('/system/setting')
}

export function getPortalDownloadInfoList(hostId){
  return axios.get(`/plugins/data-migration/portalDownloadInfo/list/${hostId}`)
}
