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

import ogRequest from '@/request'
import { useMonitorStore } from '@/store/monitor'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

export type AspCountData = {
  sampleTime: string[]
  sessionCount: []
}
export async function getAspCount(tabId: string): Promise<void | AspCountData> {
  dayjs.extend(utc)
  dayjs.extend(timezone)
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/asp/count', {
    id: instanceId,
    startTime: dayjs(new Date(timeRange[0] * 1000))
      .utc()
      .format('YYYY-MM-DDTHH:mm:ss[Z]'),
    finishTime: dayjs(new Date(timeRange[1] * 1000))
      .utc()
      .format('YYYY-MM-DDTHH:mm:ss[Z]'),
  })
}
export async function getAspAnalysis(tabId: string): Promise<void | any[]> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/asp/analysis', {
    id: instanceId,
    startTime: dayjs(new Date(timeRange[0] * 1000))
      .utc()
      .format('YYYY-MM-DDTHH:mm:ss[Z]'),
    finishTime: dayjs(new Date(timeRange[1] * 1000))
      .utc()
      .format('YYYY-MM-DDTHH:mm:ss[Z]'),
  })
}
