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
  .then(function (res) { return res })
  .catch(function (res) { return "error"})
}

export async function getSQLMetrics(
  instanceId: String,
  startTime: String,
  endTime: String,
  step: String
): Promise<void | {
  CPU_DB: number[]
  CPU_IDLE: number[]
  CPU_IOWAIT: number[]
  CPU_IRQ: number[]
  CPU_NICE: number[]
  CPU_SOFTIRQ: number[]
  CPU_STEAL: number[]
  CPU_SYSTEM: number[]
  CPU_TOTAL: number[]
  CPU_USER: number[]
  IO_UTIL: number[][]
  MEMORY_DB_USED: number[]
  MEMORY_USED: number[]
  NETWORK_IN_TOTAL: number[]
  NETWORK_OUT_TOTAL: number[]
  time: string[]
}> {
  return ogRequest.get('/observability/v1/topsql/sysResource', {
    id: instanceId,
    start: startTime,
    end: endTime,
    step,
  })
}
