import { defineStore } from 'pinia'
import { TaskDetailStoreType } from './type'
import i18n from '@/locale/index'
const useSubTaskStore = defineStore('subTaskStore',  {
  state: () => ({
    subTaskData: {
      dataCheckProcess: {
        empty: false,
        execResultDetail: '',
      },
      exceptionAlertTotalCount: 0,
      execStatus: 0,
      executedTime: 0,
      fullProcess: {
        empty: false,
        execResultDetail: '',
      },
      funcCounts: {
        errorCount: 0,
        runningCount: 0,
        successCount: 0,
        totalCount: 0,
        uncounted: true,
        waitCount: 0,
      },
      incrementalProcess: {
        empty: false,
        execResultDetail: '',
      },
      reverseProcess: {

      },
      produceCounts: {
        errorCount: 0,
        runningCount: 0,
        successCount: 0,
        totalCount: 0,
        uncounted: true,
        waitCount: 0,
      },
      statusRecords: [],
      tableCounts: {
        errorCount: 0,
        runningCount: 0,
        successCount: 0,
        totalCount: 0,
        uncounted: true,
        waitCount: 0,
      },
      totalErrorCount: 0,
      totalRunningCount: 0,
      totalSuccessCount: 0,
      totalWaitCount: 0,
      triggerCounts: {
        errorCount: 0,
        runningCount: 0,
        successCount: 0,
        totalCount: 0,
        uncounted: true,
        waitCount: 0,
      },
      viewCounts: {
        errorCount: 0,
        runningCount: 0,
        successCount: 0,
        totalCount: 0,
        uncounted: true,
        waitCount: 0,
      },
    },
  }),

  getters: {
    getSubTaskData (): TaskDetailStoreType {
      return this.subTaskData
    },
  },

  actions: {
    updateSubTaskData(info: any) {
      this.subTaskData = { ...info }
    }
  }
})

export default useSubTaskStore
