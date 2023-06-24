///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import { useRequest } from 'vue-request'
import ogRequest from '@/request'
import { useMonitorStore } from '@/store/monitor'
import moment from 'moment'
import { tabKeys } from '@/pages/dashboardV2/common'

let tabIdTemp: string = ''

const { data, run: getDatabaseMetrics } = useRequest<Record<
    string,
    { data: string[]; time: string[]; name: string }[]
> | void>(
    (tabId: string) => {
        tabIdTemp = tabId
        const monitorStore = useMonitorStore(tabIdTemp)
        const { rangeTime, time, instanceId, fixedRangeTime, tab } = monitorStore
        let start = 0
        let end = 0
        let step = 60
        if (tab === 1 && Array.isArray(fixedRangeTime) && fixedRangeTime.length === 2) {
            end = Math.floor(moment(fixedRangeTime[1]).valueOf() / 1000)
            start = Math.floor(moment(fixedRangeTime[0]).valueOf() / 1000)
            if (end - start < 2 * 60) {
                const mid = start + Math.floor((end - start) / 2)
                end = mid + 60
                start = mid - 60
            }
            console.log('sql detail - system source', start, end)
        } else {
            if (rangeTime > 0) {
                const _time = moment()
                end = Number.parseInt(
                    `${
                        _time
                            .subtract(60 * rangeTime, 'second')
                            .toDate()
                            .getTime() / 1000
                    }`
                )
                start = Number.parseInt(`${_time.subtract(rangeTime, 'hour').toDate().getTime() / 1000}`)
                step = 60 * rangeTime
            } else {
                start = Number.parseInt(`${time![0].getTime() / 1000}`)
                end = Number.parseInt(`${time![1].getTime() / 1000}`)
                step = Number.parseInt(`${(end - start) / 120}`)
            }
        }
        monitorStore.promethuesStart = start
        monitorStore.promethuesEnd = end
        monitorStore.promethuesStep = Math.max(60, step)
        return ogRequest.get('/observability/v1/monitoring/database-metrics', {
            id: instanceId,
            start,
            end,
            step: Math.max(60, step),
            type: 'LINE',
        })
    },
    { manual: true }
)

watch(
    data,
    () => {
        console.log('prometheus:tabIdTemp2', tabIdTemp)
        const monitorStore = useMonitorStore(tabIdTemp)
        if (monitorStore.tabNow === tabKeys.Home) {
            monitorStore.databaseData = data.value || {}
            console.log('prometheus:databaseData', monitorStore.databaseData)
        } else {
            monitorStore.serverData = data.value || {}
        }
    },
    { deep: true }
)
export { getDatabaseMetrics }
