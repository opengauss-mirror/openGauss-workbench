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

import ogRequest, { ApiResponse } from '@/request'

export type SessionGeneralDetail = {
  state: string
  sessionid: string
  lwtid: string
  usename: string
  backend_start: string
  backend_runtime: string

  resource_pool: string

  client_addr: string
  client_hostname: string
  client_port: string
  application_name: string
  datname: string
  xact_start: string
  query_start: string
  query_id: string

  block_sessionid: string
  filepath: string
  page: string
  tuple: string
  bucket: string

  wait_status: string
  wait_event: string
  lockmode: string
  namespace_relation: string

  query: string
}
export type BlockTable = {
  depth: string
  application_name: string
  tree_id: string
  backend_start: string
  hasChildren?: boolean
  children?: undefined | BlockTable[]
  client_addr: string
  datname: string
  pathid: string
  id: string
  usename: string
  state: string
  parentid: string
}
export type Statistic = {
  type: string
  name: string
  value: string
}
export type WaitingRecord = {
  event: string
  lockmode: string
  locktag: string
  sample_time: string
  wait_status: string
}
export type SessionDetail = {
  general: SessionGeneralDetail
  blockTree: BlockTable[]
  statistic: Statistic[]
  waiting: WaitingRecord[]
}

export async function getSessionDetail(
  instanceId: string,
  sessionId: string
): Promise<void | ApiResponse<SessionDetail>> {
  return ogRequest.get('/instanceMonitoring/api/v1/session/detail', {
    id: instanceId,
    sessionid: sessionId,
  })
}
