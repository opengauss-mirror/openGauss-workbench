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
