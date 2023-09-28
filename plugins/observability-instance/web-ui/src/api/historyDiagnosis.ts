import restRequest from '@/request/diagnosis'

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