///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

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
