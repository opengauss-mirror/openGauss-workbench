///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import { defineStore } from 'pinia'

const sourceType = {
  INSTANCE: 'INSTANCE',
  AUTOREFRESHTIME: 'AUTOREFRESHTIME',
  MANUALREFRESH: 'MANUALREFRESH',
  TABCHANGE: 'TABCHANGE',
  TIMETYPE: 'TIMETYPE',
  TIMERANGE: 'TIMERANGE',
}
const timeTypeSelection = {
  MIN15: '15m',
  MIN30: '30m',
  HOUR1: '1h',
  HOUR3: '3h',
  HOUR6: '6h',
  HOUR12: '12h',
  DAY1: '1d',
  DAY2: '2d',
  DAY7: '7d',
  CUSTOM: 'CUSTOM',
}
interface State {
  // use to refresh data
  updateCounter: {
    count: number
    source: string
  }
  clusterId: string
  instanceId: string
  node: any
  tabNow: string
  autoRefreshTime: number
  timeType: string
  timeRange: string[]
  isManualRangeSelected: boolean

  databaseData: Record<string, { data: string[]; time: string[]; name: string }[]>
  /**
   * current tab index
   */
  tab: number
  filters: {
    /**
     * auto refresh interval
     */
    refreshTime: number
    /**
     * range time
     */
    rangeTime: number
    /**
     * custom range time
     */
    time: [Date, Date] | null
  }[]
  autoRefresh: boolean
  instanceTimeRange: [Date, Date] | null
  /**
   * brush select
   */
  brushRange: string[]
  /**
   * topsql server
   */
  fixedRangeTime: Array<string>
  serverData: Record<string, { data: string[]; time: string[]; name: string }[]>
  promethuesStart: number
  promethuesEnd: number
  promethuesStep: number
}
export const useMonitorStore = (tabId: string) => {
  return defineStore('monitor-' + tabId, {
    state: (): State => ({
      updateCounter: {
        count: 0,
        source: '',
      },
      clusterId: '',
      instanceId: '',
      node: {},
      tabNow: '',
      autoRefreshTime: 30,
      timeType: timeTypeSelection.HOUR1,
      timeRange: [],
      isManualRangeSelected: false,

      // below may not be use anymore 0525
      databaseData: {},
      tab: 0,
      filters: [
        {
          refreshTime: 30,
          rangeTime: 1,
          time: null,
        },
        {
          refreshTime: 30,
          rangeTime: 1,
          time: null,
        },
        {
          refreshTime: 30,
          rangeTime: 1,
          time: null,
        },
        {
          refreshTime: 30,
          rangeTime: 1,
          time: null,
        },
      ],
      autoRefresh: false,
      instanceTimeRange: null,
      brushRange: [],
      fixedRangeTime: [],
      serverData: {},
      promethuesStart: 0,
      promethuesEnd: 0,
      promethuesStep: 60,
    }),
    getters: {
      refreshTimeForBind: (state: State) => {
        return state.autoRefreshTime
      },

      // below may not be use anymore 0525
      refreshTime: (state: State) => {
        return state.filters[state.tab].refreshTime
      },
      sourceType: (state: State) => {
        return sourceType
      },
      timeTypeSelected: (state: State) => {
        return state.timeType
      },
      timeTypeSelection: (state: State) => {
        return timeTypeSelection
      },
      rangeTime: (state: State) => {
        return state.filters[state.tab].rangeTime
      },
      time: (state: State) => {
        return state.filters[state.tab].time
      },
      selectedNode: (state: State) => {
        return state.node
      },
    },
    actions: {
      updateInstanceAndClusterId(instanceId: string, clusterId: string, obj: any) {
        this.instanceId = instanceId
        this.clusterId = clusterId
        this.node = obj
        this.updateCounter = {
          count: this.updateCounter.count + 1,
          source: sourceType.INSTANCE,
        }
      },
      culRangeTimeAndStep() {
        let start = 0
        let end = 0
        if (this.timeType === timeTypeSelection.CUSTOM) {
          start = Number.parseInt(`${new Date(this.timeRange![0]).getTime() / 1000}`)
          end = Number.parseInt(`${new Date(this.timeRange![1]).getTime() / 1000}`)
        } else {
          let min = 0
          let now = new Date()
          if (this.timeType === timeTypeSelection.MIN15) min = 15
          else if (this.timeType === timeTypeSelection.MIN30) min = 30
          else if (this.timeType === timeTypeSelection.HOUR1) min = 1 * 60
          else if (this.timeType === timeTypeSelection.HOUR3) min = 3 * 60
          else if (this.timeType === timeTypeSelection.HOUR6) min = 6 * 60
          else if (this.timeType === timeTypeSelection.HOUR12) min = 12 * 60
          else if (this.timeType === timeTypeSelection.DAY1) min = 1 * 24 * 60
          else if (this.timeType === timeTypeSelection.DAY2) min = 2 * 24 * 60
          else if (this.timeType === timeTypeSelection.DAY7) min = 7 * 24 * 60
          start = Number.parseInt(`${(now.getTime() - 1000 * min * 60) / 1000}`)
          end = Number.parseInt(`${now.getTime() / 1000}`)
        }
        return [start, end, Math.max(14, Number.parseInt(`${Math.round((end - start) / 260)}`))]
      },
      updateTabNow(tabNow: string) {
        this.tabNow = tabNow
        this.updateCounter = {
          count: this.updateCounter.count + 1,
          source: sourceType.TABCHANGE,
        }
      },
      increaseCounter(soucre: string) {
        if (soucre === sourceType.TIMERANGE || soucre === sourceType.TIMETYPE) {
          this.isManualRangeSelected = false
        }
        this.updateCounter = {
          count: this.updateCounter.count + 1,
          source: soucre,
        }
      },
      manualRangeSelection(timeRange: string[]) {
        this.isManualRangeSelected = true
        this.timeType = timeTypeSelection.CUSTOM
        this.timeRange = timeRange
        this.updateCounter = {
          count: this.updateCounter.count + 1,
          source: sourceType.TIMERANGE,
        }
      },
    },
  })()
}
