import axios from 'axios'
import { KeyValue } from '@/types/global'
import { downloadPackage, OpenLookengInstallConfig, SSHBody } from '@/types/ops/install'
import { UploadInfo } from '@/types/resource/package'
import { appRoutes } from '@/router/routes'

export const downloadFile = (host: string,filePath: string) => {
  return axios.get('operate/download', {
    params: { host: host,filePath:filePath},
    responseType: 'blob',
    headers: {'Content-Type':'application/json;application/octet-stream'}
  })
}

export const downloadReportFile = (data: string) => {
  return axios.get(`operate/assess/download?reportFileName=${data}`, {
    responseType: 'blob',
    headers: {'Content-Type':'application/json;application/octet-stream'}
  })
}

export const packagePage = (data: KeyValue) => {
  return axios.post('operate/list', data)
}

export const getAllIps = () => {
  return axios.get('operate/all/ip')
}

export const getLeftIps = () => {
  return axios.get('assess/mysql/ip')
}

export const getRightIps = () => {
  return axios.get('assess/opengauss/ip')
}

export const getAllPids = (host:String) => {
  return axios.get('operate/all/pids',{
    params: { host: host },
  })
}

export const assessInitData = () => {
  return axios.get('operate/assess/init')
}

export const packageDetailById = (id: string) => {
  return axios.get('operate/detail/' + id)
}

export const packageDel = (id: string) => {
  return axios.post('operate/delete', [id])
}

export const addPackage = (data: FormData) => {
  const config = {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }
  return axios.post('/operate/save', data, config)
}

export const editPackage = (data: FormData) => {
  return axios.post(`/operate/update`, data)
}

export const startTask = (data: FormData) => {
  return axios.post(`/operate/collect/start`, data)
}

export const getCompletePids = () => {
  return axios.get('/operate/complete/process')
}

export const getUploadPath = () => {
  return axios.get('/operate/assess/systemPath')
}

export const sysUploadPath = () => {
  return axios.get('/operate/sysUploadPath')
}

export const startAssess = (data: FormData, sqlInputType: string) => {
  return axios.post(`/operate/assess/start?sqlInputType=${sqlInputType}`, data);
}

export const hasPkgName = (name: string) => {
  return axios.get('/operate/hasName', { params: { name: name }})
}

export const getMysql = (ip: string) => {
  return axios.get('/assess/mysql', { params: { ip: ip }})
}

export const getOpenGauss = (ip: string) => {
  return axios.get('/assess/openGauss', { params: { ip: ip }})
}

export const getListResult = (data: KeyValue) => {
  return axios.post(`/assess/all/result/${data.pageNum}/${data.pageSize}`)
}

export const deleteAssess = (assessmentId: string) => {
  return axios.get(`/assess/delete/${assessmentId}`)
}
