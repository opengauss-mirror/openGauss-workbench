///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import axios from 'axios'
import ogRequest from '@/request'
import { useMonitorStore } from '@/store/monitor'

export type OpenGaussNode = {
  dbName: string
  dbType: string
  dbUser: string
  dbUserPassword: string
  id: string
  instanceId: string
  ip: string
  port: number
  serverId: string
}

export type ServerInfo = {
  id: string
  ip: string
  os: string
  port: number
  userName: string
  userPassword: string
}

export type OpenGaussInstance = {
  dbVersion: string
  id: string
  name: string
  nodeInfo: Partial<OpenGaussNode>[]
  remark: string
  serverInfoReq: Partial<ServerInfo>
  serverInfoResp?: Partial<ServerInfo>
  type: string
  vip: string
  port: number
}

export async function getList(keyword?: string, page?: number, limit?: number): Promise<Partial<OpenGaussInstance>[]> {
  let resp = await axios.post('/sql-diagnosis/api/v1/instance/list', { keyword, page, limit })
  return resp.data.data
}

export async function getDetail(id: string): Promise<Partial<OpenGaussInstance>> {
  let resp = await axios.get(`/sql-diagnosis/api/v1/instance/detail/${id}`)
  return resp.data.data
}

export async function deleteInstance(id: string): Promise<boolean> {
  let resp = await axios.post('/sql-diagnosis/api/v1/instance/delete', [id])
  return resp.data.data
}

export async function addInstance(entry: Partial<OpenGaussInstance>): Promise<Partial<OpenGaussInstance>> {
  let resp = await axios.post('/sql-diagnosis/api/v1/instance/add', entry)
  return resp.data.data
}

export async function updateInstance(entry: Partial<OpenGaussInstance>): Promise<Partial<OpenGaussInstance>> {
  let resp = await axios.post(`/sql-diagnosis/api/v1/instance/update/${entry.id}`, entry)
  return resp.data.data
}

export async function testConnection(dbNode: Partial<OpenGaussNode>): Promise<boolean> {
  let resp = await axios.post('/sql-diagnosis/api/v1/instance/connect', dbNode)
  return resp.data.code === '200' || resp.data.code === 200
}

export async function getIndexMetrics(tabId: string): Promise<void | {
  max_conn: number
  active: number
  waiting: number
  max_runtime: number
  CPU: number[]
  IO: number[]
  MEMORY: number[]
  NETWORK_IN_TOTAL: number[]
  NETWORK_OUT_TOTAL: number[]
  SWAP: number[]
  DB_THREAD_POOL: number[]
  DB_ACTIVE_SESSION: Record<string, number[]>
  time: string[]
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/mainMetrics', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export async function getCPUMetrics(tabId: string): Promise<void | {
  CPU_IOWAIT: number[]
  CPU_SYSTEM: number[]
  CPU_TOTAL: number[]
  CPU_TOTAL_5M_LOAD: number[]
  CPU_TOTAL_AVERAGE_UTILIZATION: number[]
  CPU_TOTAL_CORE_NUM: number[]
  CPU_USER: number[]
  CPU_DB: number[]
  CPU_NICE: number[]
  CPU_IRQ: number[]
  CPU_SOFTIRQ: number[]
  CPU_STEAL: number[]
  CPU_IDLE: number[]
  time: string[]
  name: string
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/cpu', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export async function getMemoryMetrics(tabId: string): Promise<void | {
  GLOBAL_CONFIG_SETTINGS: any
  GS_TOTAL_MEMORY_DETAIL: any
  MEMORY_USED: number[]
  MEMORY_DB_USED: number[]
  MEMORY_SWAP: number[]
  MEM_CACHE: number
  MEM_FREE: number
  MEM_TOTAL: number
  MEM_USED: number
  MEMORY_DB_USED_CURR: number
  SWAP_FREE: number
  SWAP_TOTAL: number
  SWAP_USED: number
  memoryConfig: any[]
  memoryNodeDetail: any[]
  time: string[]
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/memory', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export async function getNetworkMetrics(tabId: string): Promise<void | {
  NETWORK_IN: Record<string, number[]>
  NETWORK_OUT: Record<string, number[]>
  NETWORK_LOST_PACKAGE: Record<string, number[]>
  NETWORK_TCP_ALLOC: number[]
  NETWORK_CURRESTAB: number[]
  NETWORK_TCP_INSEGS: number[]
  NETWORK_TCP_OUTSEGS: number[]
  NETWORK_TCP_SOCKET: Record<string, number[]>
  NETWORK_UDP_SOCKET: number[]
  table: any[]
  time: string[]
  name: string
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/network', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export async function getIOMetrics(tabId: string): Promise<void | {
  table: any[]
  IOPS_R: Record<string, number[]>
  IOPS_W: Record<string, number[]>
  IO_AVG_REPONSE_TIME_READ: Record<string, number[]>
  IO_AVG_REPONSE_TIME_RW: Record<string, number[]>
  IO_AVG_REPONSE_TIME_WRITE: Record<string, number[]>
  IO_DISK_READ_BYTES_PER_SECOND: Record<string, number[]>
  IO_DISK_WRITE_BYTES_PER_SECOND: Record<string, number[]>
  IO_QUEUE_LENGTH: Record<string, number[]>
  IO_UTIL: Record<string, number[]>
  time: string[]
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/io', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export type TopSQLNow = {
  id: string
  ip: string
  os: string
  port: number
  userName: string
  userPassword: string
  duration: string
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
export type TransTable = {
  application_name: string
  client_addr: string
  datname: string
  pid: string
  query: string
  query_duration: string
  query_start: string
  sessionid: string
  state: string
  usename: string
  xact_duration: string
  xact_start: string
}
export type WaitEvent = {
  block_sessionid: string
  db_name: string
  lockmode: string
  locktag: string
  node_name: string
  query_id: string
  sessionid: string
  tag: string
  thread_name: string
  tid: string
  wait_event: string
  wait_status: string
}
export type MainNowTable = {
  blockTree: BlockTable[]
  longTxc: TransTable[]
  topSQLNow: TopSQLNow[]
  waitEvents: WaitEvent[]
}
export async function getTOPSQLNow(tabId: string): Promise<void | MainNowTable> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId } = monitorStore
  return ogRequest.get('/instanceMonitoring/api/v1/topSQLNow', {
    id: instanceId,
  })
}

export type TopCPUProcessNow = {
  '%CPU': string
  '%MEM': string
  COMMAND: string
  publicIp: string
  NI: number
  PID: string
  PR: string
  RES: string
  S: string
  SHR: string
  'TIME+': string
  USER: string
  VIRT: string
  port: string
}
export async function getTOPCPUProcessNow(tabId: string): Promise<void | TopCPUProcessNow[][]> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId } = monitorStore
  return ogRequest.get('/instanceMonitoring/api/v1/topOSProcessAndDBThread', {
    id: instanceId,
  })
}

export type TopMemoryProcessNow = {
  '%CPU': string
  '%MEM': string
  COMMAND: string
  publicIp: string
  NI: number
  PID: string
  PR: string
  RES: string
  S: string
  SHR: string
  'TIME+': string
  USER: string
  VIRT: string
  port: string
}
export async function getTOPMemoryProcessNow(tabId: string): Promise<void | TopMemoryProcessNow[][]> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId } = monitorStore
  return ogRequest.get('/instanceMonitoring/api/v1/topOSProcessAndDBThread', {
    id: instanceId,
    sort: '%MEM',
  })
}

export async function getInstanceInfo(tabId: string): Promise<void | {
  INSTANCE_DB_CONNECTION_ACTIVE: number[]
  INSTANCE_DB_CONNECTION_CURR: number[]
  INSTANCE_DB_CONNECTION_IDLE: number[]
  INSTANCE_DB_CONNECTION_TOTAL: number[]
  INSTANCE_DB_SLOWSQL: number[]
  INSTANCE_DB_RESPONSETIME_P80: number[]
  INSTANCE_DB_RESPONSETIME_P95: number[]
  INSTANCE_DB_DATABASE_BLK: Record<string, number[]>
  cacheHit: any[],
  time: string[]
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/instance', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export async function getInstanceOverload(tabId: string): Promise<void | {
  INSTANCE_DB_DATABASE_INS: Record<string, number[]>
  INSTANCE_DB_DATABASE_UPD: Record<string, number[]>
  INSTANCE_DB_DATABASE_DEL: Record<string, number[]>
  INSTANCE_DB_DATABASE_RETURN: Record<string, number[]>
  INSTANCE_DB_DATABASE_FECTH: Record<string, number[]>
  INSTANCE_DB_BGWRITER_CHECKPOINT: number[]
  INSTANCE_DB_BGWRITER_CLEAN: number[]
  INSTANCE_DB_BGWRITER_BACKEND: number[]
  INSTANCE_QPS: number[]
  INSTANCE_TPS_COMMIT: number[]
  INSTANCE_TPS_CR: number[]
  INSTANCE_TPS_ROLLBACK: number[]
  time: string[]
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/instanceOverload', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export async function getInstanceTablespace(tabId: string): Promise<void | {
  PG_TABLESPACE_SIZE: Record<string, number[]>
  tablespaceInfo: any[],
  tablesTop10: any[],
  indexsTop10: any[],
  deadTableTop10: any[],
  vacuumTop10: any[],
  time: string[]
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/instanceTablespace', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export async function getSessionMetrics(tabId: string): Promise<void | {
  max_conn: number
  active: number
  waiting: number
  max_runtime: number
  SESSION_ACTIVE_CONNECTION: number[]
  SESSION_IDLE_CONNECTION: number[]
  SESSION_MAX_CONNECTION: number[]
  SESSION_WAITING_CONNECTION: number[]
  gauss_wait_events_value: any[]
  time: string[]
  name: string
}> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId, culRangeTimeAndStep } = monitorStore
  let timeRange = culRangeTimeAndStep()
  return ogRequest.get('/instanceMonitoring/api/v1/session/sessionStatistic', {
    id: instanceId,
    start: timeRange[0],
    end: timeRange[1],
    step: timeRange[2],
    type: 'LINE',
  })
}

export type SessionTables = {
  blockTree: BlockTable[]
  longTxc: TransTable[]
}
export async function getSessionTables(tabId: string): Promise<void | SessionTables> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId } = monitorStore
  return ogRequest.get('/instanceMonitoring/api/v1/session/blockAndLongTxc', {
    id: instanceId,
  })
}

export type NodeInfo = {
  dbDataPath: string
  CPUmodel: string
  CPUmanufacturer: string
  osVersion: string
  dbLogPath: string
  CPUcores: string
  TotalMemory: string
  time: string
  version: string
  archiveMode: string
}
export async function getNodeInfo(tabId: string): Promise<void | NodeInfo> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId } = monitorStore
  return ogRequest.get('/instanceMonitoring/api/v1/nodeInfo', {
    id: instanceId,
  })
}

export async function isExistAgentForInstance(tabId: string): Promise<void | boolean> {
  const monitorStore = useMonitorStore(tabId)
  const { instanceId } = monitorStore
  return ogRequest.get('/instanceMonitoring/api/v1/isExistAgentForInstance', {
    id: instanceId,
  })
}