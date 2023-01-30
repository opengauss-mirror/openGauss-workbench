import request, { KeyValue } from './request'
import { Message } from '@arco-design/web-vue'
// import axios from 'axios'

// const baseUrl = `http://47.95.39.132:7777/`

export const selectDealDataFlowTest = (data?: any) => request({
  url: 'select/dealDataFlowTest', method: 'post', data
})

export const getTableList = (params: string, data?: any) => request({
  url: 'modeling/dataSourceDb/getTablesByScheme/' + params, method: 'get', data
})

// get fields by table
export const getTableFields = (params: string, data?: any) => request({
  url: 'modeling/dataSourceDb/getFieldsByTable/' + params, method: 'get', data
})

export const getList = (data?: any) => request({
  url: 'modeling/dataFlow/list', method: 'get',
  params: data ? data : {}
})

export const dataFlowAdd = (data?: KeyValue) => request({
  url: 'modeling/dataFlow/add', method: 'post',
  data: data ? data : {}
})

export const dataFlowDelete = (id: string) => request({
  url: `modeling/dataFlow/delete/${id}`, method: 'delete'
})

export const dataFlowEdit = (data?: KeyValue) => request({
  url: `modeling/dataFlow/edit`, method: 'put',
  data: data ? data : {}
})

export const getSQL = (data?: KeyValue) => request({
  url: `modeling/getSql`, method: 'post',
  data: data ? data : {}
})

export const runSql = (data?: KeyValue) => request({
  url: `modeling/runSql`, method: 'post',
  data: data ? data : {}
})

let canSave = true
export const saveJsonData = (id: string, jsonData: KeyValue) => {
  if (!canSave) return
  canSave = false
  request({
    url: `modeling/dataFlow/edit`, method: 'put',
    data: { id, operatorContent: JSON.stringify(jsonData) }
  }).then((res: KeyValue) => {
    canSave = true
    if (Number(res.code) === 200) {
      Message.success({ content: 'save success！' })
    }
  })
}

export const getJsonData = (id: string) => request({
  url: 'modeling/dataFlow/list', method: 'get',
  params: { id }
})

export const dataSourceDbAdd = (data?: KeyValue) => request({
  url: `modeling/dataSourceDb/add`, method: 'post',
  data: data ? data : {}
})

export const dataSourceDbList = (data?: KeyValue) => request({
  url: `modeling/dataSourceDb/list`, method: 'get',
  params: data ? data : {}
})

export const dataSourceDbDelete = (id: string) => request({
  url: `modeling/dataFlow/delete${id}`, method: 'delete'
})

export const dataSourceDbEdit = (data?: KeyValue) => request({
  url: `modeling/dataSourceDb/edit`, method: 'put',
  data: data ? data : {}
})

/** visual */
// Get the selection options of the visual editing area
export const modelingGetResultFieldsByOperator = (data?: KeyValue) => request({
  url: `modeling/getResultFieldsByOperator`, method: 'post',
  data: data ? data : {}
})
// New configuration
export const modelingVPAdd = (data?: KeyValue) => request({
  url: `modeling/visualization/params/add`, method: 'post',
  data: data ? data : {}
})
// configuration modify
export const modelingVPEdit = (data?: KeyValue) => request({
  url: `modeling/visualization/params/edit`, method: 'put',
  data: data ? data : {}
})
// get echarts config
export const modelingVPGenerateChart = (data?: KeyValue) => request({
  url: `modeling/visualization/params/generateChart`, method: 'post',
  data: data ? data : {}
})
// Obtain the visual chart configuration list of the operator
export const modelingVPGetListByOperatorId = (id: number | string, data?: KeyValue) => request({
  url: `modeling/visualization/params/getListByOperatorId/${id}`, method: 'get',
  data: data ? data : {}
})

/** snapshot */
// add
export const modelingVSAdd = (data?: KeyValue) => request({
  url: `modeling/visualization/snapshot/add`, method: 'post',
  data: data ? data : {}
})
// list
export const modelingVSGetListByDataFlowId = (id: number | string, data?: KeyValue) => request({
  url: `modeling/visualization/snapshot/getListByDataFlowId/${id}`, method: 'get',
  data: data ? data : {}
})
// delete
export const modelingVSDelete = (id: string) => request({
  url: `modeling/visualization/snapshot/delete/${id}`, method: 'delete'
})
/** report */
// list
export const modelingVRGetListByDataFlowId = (id: number | string, data?: KeyValue) => request({
  url: `modeling/visualization/report/getListByDataFlowId/${id}`, method: 'get',
  data: data ? data : {}
})
// add
export const modelingVRAdd = (data?: KeyValue) => request({
  url: `modeling/visualization/report/add`, method: 'post',
  data: data ? data : {}
})
// modify
export const modelingVRUpdate = (data?: KeyValue) => request({
  url: `modeling/visualization/report/update`, method: 'put',
  data: data ? data : {}
})
// delete
export const modelingVRDelete = (id: string) => request({
  url: `modeling/visualization/report/delete/${id}`, method: 'delete'
})
