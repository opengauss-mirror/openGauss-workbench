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
