/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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