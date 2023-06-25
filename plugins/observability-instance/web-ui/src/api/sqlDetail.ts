///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import ogRequest from '@/request'

export type SQLEvent = {
    event: string
    id: string
    lockType: string
    time: string
    unknown: string
}

export async function getSQLEvent(instanceId: string, sqlId: string): Promise<void | SQLEvent[]> {
    return ogRequest.get('/observability/v1/topsql/waitevent', {
        id: instanceId,
        sqlId,
    })
}
