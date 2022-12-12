import { defineStore } from "pinia";

interface State {
    instanceId: string,
    databaseData: Record<string, {data: string[], time: string[], name: string}[]>,
    /**
     * current tab index
     */
    tab: number,
    filters: {
         /**
         * auto refresh interval
         */
        refreshTime: number,
        /**
         * range time
         */
        rangeTime: number,
        /**
         * custom range time
         */
        time: [Date, Date] | null
    }[],
    autoRefresh: boolean,
    instanceTimeRange: [Date, Date] | null,
    /**
     * brush select
     */
    brushRange: string[],
    timeRange: string[],
    /**
     * topsql server
     */
    fixedRangeTime: Array<string>,
    serverData: Record<string, {data: string[], time: string[], name: string}[]>,
}
export const useMonitorStore = defineStore("monitor", {
    state: (): State => ({
        instanceId: '',
        databaseData: {},
        tab: 0,
        filters: [
            {
                refreshTime: 30,
                rangeTime: 1,
                time: null
            },
            {
                refreshTime: 30,
                rangeTime: 1,
                time: null
            },
        ],
        autoRefresh: false,
        instanceTimeRange: null,
        brushRange: [],
        timeRange: [],
        fixedRangeTime: [],
        serverData: {},
    }),
    getters: {
        refreshTime: (state: State) => {
            return state.filters[state.tab].refreshTime
        },
        rangeTime: (state: State) => {
            return state.filters[state.tab].rangeTime
        },
        time: (state: State) => {
            return state.filters[state.tab].time
        },
    },
})
