import axios from 'axios'
import { SysSetting } from '@/types/sysSetting'

export function updateSysSetting (params: SysSetting) {
  return axios.put('/system/setting', params)
}

export function listSysSetting () {
  return axios.get('/system/setting')
}

export const checkUploadPath = (path: string) => {
  return axios.get('/system/setting/checkUploadPath', {
    params: {
      path: path
    }
  })
}
