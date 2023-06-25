import restRequest from '@/request/restful'

export type Option = {
    option: string
    orderId: string
    value: string
}
export async function getOptions(): Promise<Option[]> {
    return restRequest.get('/historyDiagnosis/api/v1/options')
}

export type Threshold = {
    threshold: string
    thresholdName: string
    thresholdValue: string
    thresholdType: string
}
export async function getThresholds(): Promise<Threshold[]> {
    return restRequest.get('/historyDiagnosis/api/v1/thresholds')
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
    hisDataStartTime: string
    hisDataEndTime: string | null
    configs: DiagnosisParamConfig[]
    thresholds: DiagnosisParamThreshold[]
}
export async function addTask(param: DiagnosisParam): Promise<Threshold[]> {
    return restRequest.post('/historyDiagnosis/api/v1/tasks', param)
}

export type Threshold2 = {
    thresholdName: string
    thresholdValue: string
    thresholdType: string
}
export async function getTask(id: string): Promise<Threshold2> {
    return restRequest.get('/historyDiagnosis/api/v1/tasks/' + id + '/suggestPoints/all=false')
}

export type PointInfo = {
    thresholdName: string
    thresholdValue: string
    thresholdType: string
}
export async function getPointData(id: string, pointKey: string): Promise<PointInfo> {
    return restRequest.get('/historyDiagnosis/api/v1/tasks/' + id + '/points/' + pointKey)
}
