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
  return restRequest.get('/historyDiagnosis/api/v2/thresholds', {
    diagnosisType: diagnosisType !== undefined ? diagnosisType : 'history',
  })
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
  hisDataStartTime: string
  hisDataEndTime: string | null
  configs: DiagnosisParamConfig[]
  thresholds: DiagnosisParamThreshold[]
}
export async function addTask(param: DiagnosisParam): Promise<Threshold[]> {
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
