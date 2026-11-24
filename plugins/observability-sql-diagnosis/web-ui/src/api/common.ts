// Copyright (c) 2023 Huawei Technologies Co., Ltd.
// openGauss-workbench is licensed under Mulan PSL v2.
// You can use this software according to the terms and conditions of the Mulan PSL v2.
// You may obtain a copy of Mulan PSL v2 at:
//          http://license.coscl.org.cn/MulanPSL2
// THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
// EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
// MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
// See the Mulan PSL v2 for more details.

export type keyObject = {
  Home: string
  WDR: string
  ASP: string
  SystemConfig: string
  ResourceMonitor: string
  ResourceMonitorCPU: string
  ResourceMonitorMemory: string
  ResourceMonitorIO: string
  ResourceMonitorNetwork: string
  InstanceMonitor: string
  InstanceMonitorInstance: string
  InstanceMonitorSession: string
  InstanceMonitorTOPSQL: string
  InstanceMonitorTOPSQLDBTime: string
  InstanceMonitorTOPSQLCPUTime: string
  InstanceMonitorTOPSQLEXECTime: string
  InstanceMonitorTOPSQLIOTime: string
  CollectConfig: string
  InstanceMonitorInstanceOverload: string
  InstanceMonitorInstanceTablespace: string
  InstanceMonitorInstanceInfo: string
}

export const tabKeys: keyObject = {
  Home: 'Home',
  WDR: 'WDR',
  ASP: 'ASP',
  SystemConfig: 'SystemConfig',
  ResourceMonitor: 'ResourceMonitor',
  ResourceMonitorCPU: 'ResourceMonitorCPU',
  ResourceMonitorMemory: 'ResourceMonitorMemory',
  ResourceMonitorIO: 'ResourceMonitorIO',
  ResourceMonitorNetwork: 'ResourceMonitorNetwork',
  InstanceMonitor: 'InstanceMonitor',
  InstanceMonitorInstance: 'InstanceMonitorInstance',
  InstanceMonitorSession: 'InstanceMonitorSession',
  InstanceMonitorTOPSQL: 'InstanceMonitorTOPSQL',
  InstanceMonitorTOPSQLDBTime: 'InstanceMonitorTOPSQLDBTime',
  InstanceMonitorTOPSQLCPUTime: 'InstanceMonitorTOPSQLCPUTime',
  InstanceMonitorTOPSQLEXECTime: 'InstanceMonitorTOPSQLEXECTime',
  InstanceMonitorTOPSQLIOTime: 'InstanceMonitorTOPSQLIOTime',
  CollectConfig: 'CollectConfig',
  InstanceMonitorInstanceOverload: 'InstanceMonitorInstanceOverload',
  InstanceMonitorInstanceInfo: 'InstanceMonitorInstanceInfo',
  InstanceMonitorInstanceTablespace: 'InstanceMonitorInstanceTablespace',
}
