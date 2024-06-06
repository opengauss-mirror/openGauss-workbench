import axios from 'axios'
import ogRequest from '@/request'
import { useMonitorStore } from '@/store/monitor'
import moment from "moment"



export async function getInstanceInfo(tabId: string): Promise<void | {
  INSTANCE_DB_SLOWSQL: number[]
  time: string[]
}> {
  const monitorStore = useMonitorStore()
  const { rangeTime, time, instanceId, fixedRangeTime, tab } = monitorStore
  let start = 0
  let end = 0
  let step = 60
  if (Array.isArray(fixedRangeTime) && fixedRangeTime.length === 2) {
    end = Math.floor(moment(fixedRangeTime[1]).valueOf() / 1000);
    start = Math.floor(moment(fixedRangeTime[0]).valueOf() / 1000);
    if (end - start < 2 * 60) {
      end = end - start > 60 ? start + 120 : start + 60
      start -= 60
    }
  } else {
    if (rangeTime > 0) {
      const _time = moment()
      end = Number.parseInt(`${_time.subtract(60 * rangeTime, 'second').toDate().getTime() / 1000}`)
      start = Number.parseInt(`${_time.subtract(24 * 8 * rangeTime, 'hour').toDate().getTime() / 1000}`)
      step = Math.max(14, Number.parseInt(`${Math.round((end - start) / 260)}`))
    } else {
      start = Number.parseInt(`${time![0].getTime() / 1000}`)
      end = Number.parseInt(`${time![1].getTime() / 1000}`)
      step = Number.parseInt(`${(end - start) / 120}`)
    }
  }
  return ogRequest.get('/sqlDiagnosis/api/v1/slowSqls/chart', {
    id: instanceId,
    start: start,
    end: end,
    step: step,
    type: 'LINE',
  })
}
