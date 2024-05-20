///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import ogRequest from '@/request/diagnosis'

export async function hasSQLDiagnosisModule(): Promise<void | any> {
    return ogRequest.get('/historyDiagnosis/api/v2/options', {})
}
