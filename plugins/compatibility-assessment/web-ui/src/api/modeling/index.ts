import { Message } from '@arco-design/web-vue'
import axios from 'axios'
import { KeyValue } from '@antv/x6/lib/types'
import i18n from '@/locale/index'

export const selectDealDataFlowTest = (data?: any) => axios.post(`/select/dealDataFlowTest`, data ? data : {})

export const getTableList = (dbName: string, clusterNodeId: string, schema: string, data?: any) => axios.get(`/modeling/dataflow/dataSourceDb/getTablesBySchema/${dbName}/${clusterNodeId}/${schema}`, data ? data : {})

export const getTableFields = (params: string, data?: any) => axios.get(`/modeling/dataflow/dataSourceDb/getFieldsByTable/${params}`, data ? data : {})

export const getList = (data?: any) => axios.get(`/modeling/dataflow/list`, {
  params: data
})
export const dataFlowAdd = (data?: KeyValue) => axios.post(`/modeling/dataflow/add`, data ? data : {})
export const dataFlowDelete = (id: string) => axios.delete(`/modeling/dataflow/delete/${id}`)
export const dataFlowEdit = (data?: KeyValue) => axios.put(`/modeling/dataflow/edit`, data ? data : {})
export const dataFlowGetById = (dataFlowId: any) => axios.get(`/modeling/dataflow/getById/${dataFlowId}`)

export const getSchemeByClusterNodeId = (data: any, data2:any) => axios.get(`/modeling/dataflow/dataSourceDb/getSchemaByClusterNodeId/${data}/${data2}`, data ? data : {})
export const getSQL = (data?: KeyValue) => axios.post(`/modeling/dataflow/getSql`, data ? data : {})
export const runSql = (data?: KeyValue) => axios.post(`/modeling/dataflow/runSql`, data ? data : {})

let canSave = true
export const saveJsonData = (id: string, jsonData: KeyValue, notShowMessage?: boolean) => new Promise(resolve => {
  if (!canSave) return
  canSave = false
  axios.put(`/modeling/dataflow/edit`, { id, operatorContent: JSON.stringify(jsonData) }).then((res: KeyValue) => {
    resolve(true)
    canSave = true
    if (Number(res.code) === 200) {
      if (!notShowMessage) Message.success({ content: i18n.global.t('modeling.dy_common.saveSuccess') })
    }
  }).catch(() => {
    resolve(false)
    if (!notShowMessage) Message.error({ content: i18n.global.t('modeling.dy_common.saveFailed') })
    canSave = true
  })
})
// get full json
export const getJsonData = (id: string) => axios.get(`/modeling/dataflow/list`, { params: { id }})

export const dataSourceDbAdd = (data?: KeyValue) => axios.post(`/modeling/dataflow/dataSourceDb/add`, data ? data : {})

export const dataSourceDbList = (data?: KeyValue) => axios.get(`/modeling/dataflow/dataSourceDb/list`, data ? data : {})

export const dataSourceDbDelete = (id: string) => axios.delete(`/modeling/dataflow/delete${id}`)

export const dataSourceDbEdit = (data?: KeyValue) => axios.put(`/modeling/dataflow/dataSourceDb/edit`, data ? data : {})

export const modelingGetResultFieldsByOperator = (data?: KeyValue) => axios.post(`/modeling/dataflow/getResultFieldsByOperator`, data ? data : {})
export const modelingVPAdd = (data?: KeyValue) => axios.post(`/modeling/dataflow/visualization/params/add`, data ? data : {})
export const modelingVPEdit = (data?: KeyValue) => axios.put(`/modeling/dataflow/visualization/params/edit`, data ? data : {})
export const modelingVPDelete = (id: string) => axios.delete(`/modeling/dataflow/visualization/params/delete/${id}`)
export const modelingVPGenerateChart = (data?: KeyValue) => axios.post(`/modeling/dataflow/visualization/params/generateChart`, data ? data : {})
export const modelingVPGetListByOperatorId = (id: number | string, data?: KeyValue) =>
  axios.get(`/modeling/dataflow/visualization/params/getListByOperatorId/${id}`, data ? data : {})

export const modelingVSAdd = (data?: KeyValue) => axios.post(`/modeling/dataflow/visualization/snapshot/add`, data ? data : {})
export const modelingVSGetListByDataFlowId = (id: number | string, data?: KeyValue) =>
  axios.get(`/modeling/dataflow/visualization/snapshot/getListByDataFlowId/${id}`, data ? data : {})
export const modelingVSDelete = (id: string) => axios.delete(`/modeling/dataflow/visualization/snapshot/delete/${id}`)

export const modelingVRGetListByDataFlowId = (id: number | string, params?: KeyValue) =>
  axios.get(`/modeling/dataflow/visualization/report/getListByDataFlowId/${id}`, { params: params ? params : {}})
export const modelingVRAdd = (data?: KeyValue) => axios.post(`/modeling/dataflow/visualization/report/add`, data ? data : {})
export const modelingVRUpdate = (data?: KeyValue) => axios.put(`/modeling/dataflow/visualization/report/update`, data ? data : {})
export const modelingVRDelete = (id: string) => axios.delete(`/modeling/dataflow/visualization/report/delete/${id}`)
export const modelingVRGetByReportId = (id: string) => axios.get(`/modeling/dataflow/visualization/report/share/getByReportId/${id}`)

export const modelingVCGetListByOperatorId = (id: number | string, params?: KeyValue) =>
axios.get(`/modeling/dataflow/visualization/customDimension/getListByOperatorId/${id}`, { params: params ? params : {}})
export const modelingVCAdd = (data?: KeyValue) => axios.post(`/modeling/dataflow/visualization/customDimension/add`, data ? data : {})
export const modelingVCEdit = (data?: KeyValue) => axios.put(`/modeling/dataflow/visualization/customDimension/edit`, data ? data : {})
export const modelingVCDelete = (id: string) => axios.delete(`/modeling/dataflow/visualization/customDimension/delete/${id}`)

export const getMapGeo = (name: string, data?: any) => axios.get(`/modeling/visualization/report/share/getMapGeo/${name}`, data ? data : {})
export const uploadGeo = (data?: any) => axios.post(`/modeling/dataflow/visualization/params/uploadGeo`, data ? data : {})
export const modelingDVGetGeo = (id: number | string, params?: KeyValue) =>
axios.get(`/modeling/dataflow/visualization/params/getGeo/${id}`, { params: params ? params : {}})
export const modelingDVdeleteGeo = (id: string) => axios.delete(`/modeling/dataflow/visualization/params/deleteGeo/${id}`)
