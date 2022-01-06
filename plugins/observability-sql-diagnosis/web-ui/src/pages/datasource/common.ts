export type optionType = {
    label: string;
    value: string;
}

export const osLogType: Array<optionType> = [
    { label: '全日志', value: 'all' },
    { label: '错误日志', value: 'error' },
]

export const dbLogType: Array<optionType> = [
    { label: '全日志', value: 'all' },
    { label: '错误日志', value: 'error' },
    { label: '慢日志', value: 'slow' }
]
export const baseInfoOption: Array<optionType> = [
    { label: '线程号', value: 'pidname' },
    { label: '数据库名称', value: 'db_name' },
    { label: '实例名称', value: 'ip' },
    { label: '开始时间', value: 'start_time' },
    { label: '结束时间', value: 'end_time' },
];
export const taskInfoOption: Array<optionType> = [
    { label: '任务名称', value: 'name' },
    { label: '任务类型', value: 'type' },
    { label: '执行情况', value: 'execution' },
    { label: '总耗时', value: 'totalTime' },
    { label: 'SQL', value: 'sql' },
    { label: '开始时间', value: 'startTime' },
    { label: '结束时间', value: 'endTime' },
    { label: '创建时间', value: 'createTime' },
];

export const pologyOption: Array<optionType> = [
    { label: '建议项', value: 'false' },
    { label: '所有诊断项', value: 'true' }
]
export const pieColorAll:Array<string> = ['#37D4D1', '#2830FF', '#00C7F9', '#E64A19', '#0D86E2', '#0F866A', '#425ADD', '#FFAB94', '#8B00E1', '#5CDF73', '#9CCC65', '#FEEC21', '#A97526',]

export const reportNodeList = [
    'ObjectInfoCheck',
    'ObjectRecommendedToUpdateStatistics',
    'ExecPlan',
    'PlanRecommendedToCreateIndex',
    'PlanChangedToPartitionTable',
    'PlanRecommendedToQueryBasedOnPartition',
    'PlanRecommendedToDoVacuumCleaning',
    'PlanRecommendedToOptimizeStatementsOrAddWorkMemSize',
]
