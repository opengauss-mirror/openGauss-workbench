type ProcessContentType = {
  empty: boolean,
  execResultDetail: string,
}

type CountContentType = {
  errorCount: number,
  runningCount: number,
  successCount: number,
  totalCount: number,
  uncounted: boolean,
  waitCount: number,
}

export interface TaskDetailStoreType {
  dataCheckProcess: {
    empty: boolean,
    execResultDetail: string,
  };
  exceptionAlertTotalCount: number;
  execStatus: number;
  executedTime: number;
  fullProcess: ProcessContentType,
  funcCounts: CountContentType,

  incrementalProcess: ProcessContentType,
  produceCounts: CountContentType,
  statusRecords: any [],
  tableCounts: CountContentType,
  totalErrorCount: number,
  totalRunningCount: number,
  totalSuccessCount: number,
  totalWaitCount: number,
  triggerCounts: CountContentType,
  viewCounts: CountContentType,
  [key: string]: unknown;
}

const  initObj = {
  dataCheckProcess: {
    empty: false,
    execResultDetail: '',
  },
  exceptionAlertTotalCount: 0,
  execStatus: 1,
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
}
