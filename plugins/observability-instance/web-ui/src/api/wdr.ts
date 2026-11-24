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

export type WDRSnapshots = {
  start: string
  end: string
  wdrId: string[]
}
export async function getWDRSnapshot(tabId: string): Promise<void | WDRSnapshots> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, timeRange } = monitorStore
  return ogRequest.get('/wdr/findSnapshot', {
    id: instanceId,
    start: timeRange == null ? '' : new Date(timeRange[0]).toISOString().replace(/\.\d+/, ''),
    end: timeRange == null ? '' : new Date(timeRange[1]).toISOString().replace(/\.\d+/, ''),
  })
}
