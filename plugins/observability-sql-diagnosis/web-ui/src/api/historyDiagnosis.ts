import restRequest from '@/request/restful'

export type Option = {
  option: string
  orderId: string
  value: string
}
export async function getOptions(diagnosisType: string): Promise<Option[]> {
  return restRequest.get('/historyDiagnosis/api/v2/options', {
    diagnosisType: diagnosisType !== undefined ? diagnosisType : 'history',
  })
}

export type Threshold = {
  threshold: string
  thresholdName: string
  thresholdValue: string
  thresholdType: string
}
export async function getThresholds(diagnosisType: string): Promise<Threshold[]> {
  return restRequest.get('/thresholds/api/v2/thresholds', {
    diagnosisType: diagnosisType !== undefined ? diagnosisType : 'history',
  })
}

export type ThresholdItem = {
  thresholdKey: string
  thresholdValue: string
}
export type ThresholdGlobalSetting = {
  diagnosisType: string
  thresholds: ThresholdItem[]
}
export async function setThresholds(data: ThresholdGlobalSetting): Promise<Boolean> {
  return restRequest.post('/thresholds/api/v1/thresholds/action?action=setGlobalThresholds', data)
}

export interface DiagnosisParamConfig {
  option: string
  isCheck: boolean
}
export interface DiagnosisParamThreshold {
  threshold: string
  thresholdValue: string
}
export interface DiagnosisParam {
  nodeId: string
  diagnosisType: string
  hisDataStartTime: string | null
  hisDataEndTime: string | null
  dbName: string | null
  clusterId: string | null
  taskName: string | null
  sql: string | null
  sqlId: string | null
  configs: DiagnosisParamConfig[]
  thresholds: DiagnosisParamThreshold[]
}
export async function addTask(param: DiagnosisParam): Promise<any> {
  if (param.diagnosisType === undefined) param.diagnosisType = 'history'
  return restRequest.post('/historyDiagnosis/api/v2/tasks', param)
}

export type Threshold2 = {
  thresholdName: string
  thresholdValue: string
  thresholdType: string
}
export async function getTask(id: string, diagnosisType?: string): Promise<Threshold2> {
  return restRequest.get('/historyDiagnosis/api/v2/tasks/' + id + '/suggestPoints/all=false', {
    diagnosisType: diagnosisType !== undefined ? diagnosisType : 'history',
  })
}

export type PointInfo = {
  pointDetail: string
  pointName: string
  pointState: string
  pointSuggestion: string
  isHint: string
  pointType: string
  pointData: any
}
export async function getPointData(id: string, pointKey: string, diagnosisType?: string): Promise<PointInfo> {
  return restRequest.get('/historyDiagnosis/api/v2/tasks/' + id + '/points/' + pointKey, {
    diagnosisType: diagnosisType !== undefined ? diagnosisType : 'history',
  })
}
