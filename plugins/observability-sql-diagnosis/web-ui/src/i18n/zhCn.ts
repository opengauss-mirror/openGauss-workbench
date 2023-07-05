export default {
  app: {
    name: '一体化平台',
    menuName: '可观测可诊断',
    refresh: '刷新',
    query: '查询',
    reset: '重置',
    autoRefresh: '自动刷新',
    custom: '自定义',
    startDate: '开始日期',
    endDate: '结束日期',
    to: '至',
    running: '运行中',
    disconnect: '连接异常',
    unknown: '未知',
    delete: '删除',
    confirmDel: '确认删除',
    cancel: '取消',
    confirm: '确定',
    edit: '编辑',
    operate: '操作',
    startTime: '开始时间',
    endTime: '结束时间',
    lastOneHour: '最近一小时',
    lastThreeHour: '最近三小时',
    lastSixHour: '最近六小时',
    back: '返回',
    cluterTitle: '集群/实例：',
  },
  instanceIndex: {
    memory: '内存',
    networkInOut: '网络（In/Out）',
    threadPoolUsed: '线程池使用率',
    activeSessionQty: '活跃会话数量',
    activeSessionQtyTips: '可以在图中选取区间下钻分析',
    nowTOPSQL: '实时TOPSQL',
    dbName: '数据库名称',
    userName: '用户名称',
    appName: '应用名称',
    startTime: '开始时间',
    costTime: '耗时(s)',
    sessionId: '会话ID',
    detail: '详情',
  },
  historyDiagnosis: {
    selectTime: '请选择诊断时间：',
    nowTime: '当前时间',
    option: '可选项：',
    thresholdsThisTime: '本次诊断阈值配置：',
    kind: '类别',
    thresholdsName: '阈值名称',
    thresholdsConfig: '阈值配置',
    thresholdsRemark: '阈值说明',
    diagnosis: '诊断',
    diagnosing: '诊断中...',
    pleaseSelectInstance: '请选择实例',
    pleaseSelectStartAndEndTime: '请输入开始时间和结束时间',
    pleaseSelectStartTime: '请输入开始时间',
    pleaseSelectThreshold: '请输入阈值',
    result: '诊断结果',
    explanation: '诊断说明',
    notAnalyze: '未满足诊断条件，所以此诊断点未执行诊断操作。',
    taskInfo: {
      result: '诊断结果',
      diagnosisRange: '诊断区间：',
      taskState: '任务状态：',
      cost: '诊断耗时：',
      createTime: '创建时间：',
      option: '可选项：',
      thresholdsRemark: '诊断阈值',
    },
    topCPUTimeSQL: {
      uniqueSQLID: '归一化SQLID',
      calls: '调用次数',
      cpuTime: 'CPU时间(s)',
      dbTime: 'DB时间(s)',
      lastUpdated: '更新时间',
    },
    waitEvent: {
      eventName: '等待事件名称',
      sampleNum: '时间段内采样数量',
      description: '等待事件介绍',
      troubleshooting: '排查建议',
    },
    currentCpuUsage: {
      for: '当前CPU使用率：',
    },
    lock: {
      logTime: '日志时间',
      processId: '线程ID',
      transactionId: '事务ID',
      log: '日志内容',
    },
    businessConnCount: {
      title: 'CPU秒级抖动与业务连接数关系',
      nowStartTime: '抖动起始时间',
      nowEndTime: '抖动结束时间',
      nowSessionCount: '抖动区间平均每秒连接数',
      beforeSessionCount: '抖动前平均每秒连接数',
    },
  },
  install: {
    uninstall: '卸载',
    uninstallProxy: '卸载服务端',
    uninstallAgent: '卸载代理',
    uninstallQuick: '一键卸载',
    install: '一键部署',
    installAgent: '安装代理',
    installProxy: '安装服务端',
    installedAgent: '已安装代理',
    installedProxy: '已安装服务端',
    machine: '物理机',
    rootPWD: 'Root用户密码',
    proxyPort: '代理端口',
    installPath: '安装目录',
    collectInstance: '采集实例',
    collectProxy: '采集代理',
    callbackPath: '回调路径',
    proxyRules: ['请选择物理机', '请输入Root用户密码', '请输入代理端口号'],
    collectorRules: ['请选择实例', '请输入Root用户密码'],
  },
  dashboard: {
    name: '实例概览',
    addDsBtn: '快速接入实例',
    searchPlaceholder: '搜索实例名称或IP地址',
    table: [
      '实例名称',
      '服务器状态',
      '数据库状态',
      '连接信息',
      '数据库版本',
      '服务器CPU使用率',
      '服务器内存使用率',
      '数据库活动会话数',
      '数据库阻塞会话数',
    ],
    instance: '实例监控',
    load: '系统负载',
    session: '会话管理',
    slow: '慢SQL',
    top: 'TOPSQL',
    space: '空间分析',
    day: '天',
    hour: '小时',
    range: '范围',
    last1H: '近1小时',
    last12H: '近12小时',
    last1D: '近1天',
    last2D: '近2天',
    last7D: '近7天',
    cpuUsage: 'CPU使用率',
    cpuUseSituation: 'CPU使用情况',
    networkTransmissionRate: '网络传输速率',
    load5m: '5m load',
    memoryUsage: '内存使用率',
    diskReadRate: '磁盘读速率',
    diskWriteRate: '磁盘写速率',
    uploadRate: '上传速率',
    downloadRate: '下载速率',
    serverInfo: {
      title: '服务器基本信息',
      ip: 'IP',
      host: '主机名',
      systemVersion: '系统版本',
      runningTime: '运行时间',
      memory: '内存',
      coreNumber: 'CPU核数',
      cpuProcessor: 'CPU处理器',
      totalProcesses: '总进程数',
    },
    timeConsumption: '时间消耗',
    runningInAnalysis: '下钻分析',
    runningInAnalysisTip: '可以在图中选取区间下钻分析对应时间段SQL执行情况',
    uncheckRegion: '取消区域框选',
    waitEvent: '等待事件',
    serverResources: '服务器资源',
    loadAndCpu: '整体总负载与整体平均CPU使用率',
    memoryAndAverageMemory: '整体总内存与整体平均内存使用率',
    diskAndAverageDisk: '整体总磁盘与整体平均磁盘使用率',
    diskReadAndWriteRate: '磁盘读写速率(IOPS)',
    diskReadAndWritCapacity: '每秒磁盘读写容量',
    networkBandwidthUsage: '每秒网络带宽使用情况',
    networkSocketConnection: '网络Socket连接信息',
    databaseLoad: '数据库负载',
    tps: 'TPS',
    qps: 'QPS',
    connectionNum: '连接数',
    slowSqlMoreThan3Seconds: '慢SQL数(大于3秒)',
    longTransactionNumGreaterThan30Seconds: '长事务数(大于30秒)',
    sqlResponseTime: 'SQL响应时间',
    transactionLockInfo: '事务锁信息统计',
    cacheHitRate: '缓存命中率',
    ioBlockNumberTrend: '数据库文件IO块数趋势',
    ScrubDirtyPageInfo: '刷脏页信息',
    currentRate: '当前速率',
    minimumRate: '最小速率',
    averageRate: '平均速率',
    maxRate: '最大速率',
    currentCapacity: '当前容量',
    minimumCapacity: '最小容量',
    averageCapacity: '平均容量',
    maxCapacity: '最大容量',
    totalLoad: '总负载',
    allProtocolSocketsUsed: '已使用的所有协议套接字总量',
    serverDetail: '服务器资源详情',
    sessionStatistics: '会话统计',
    sessionList: '会话列表',
    userStatistics: '用户统计',
    accessSourceStatistics: '访问来源统计',
    databaseStatistics: '数据库统计',
    rangeTimeTip: '框选区域为SQL执行时间范围',
    capacity: '容量',
    rate: '速率',
  },
  metric: {
    totalCoreNum: '总核数',
    total5mLoad: '总5m负载',
    totalAverageUtilization: '总平均使用率',
    diskIOUsage: '磁盘IO使用率',
    systemUsage: '系统使用率',
    userUsage: '用户使用率',
    totalUsage: '总使用率',
    totalMemory: '总内存',
    usedMemory: '已用内存',
    totalDisks: '总磁盘',
    totalNumber: '总使用量',
    read: '读取',
    write: '写入',
    upload: '上传',
    download: '下载',
    transactionRollbackNum: '事务回滚数',
    transactionCommitments: '事务提交数',
    transactionAndRollbackTotal: '事务提交和回滚总数',
    queryTransactions: '查询事务数',
    currentIdleConnections: '当前空闲连接数',
    currentActiveConnections: '当前活跃连接数',
    currentConnections: '当前连接数',
    totalConnections: '总连接数',
    slowSqlNum: '慢SQL数',
    longTransactions: '长事务数',
    sqlResponseTime80: '80% SQL响应时间',
    sqlResponseTime95: '95% SQL响应时间',
    queryCacheHitRate: '查询缓存命中率',
    databaseCacheHitRate: '数据库缓存命中率',
    readPhysicalFileBlockNum: '读物理文件块数',
    writePhysicalFileBlockNum: '写物理文件块数',
    lastBatchDirtyPageNum: '上一批刷脏页数量',
    currentRemainingDirtyPages: '当前预计剩余脏页',
  },
  session: {
    sessionDetail: '会话详情',
  },
  sql: {
    dbName: '数据库名称',
    schemaName: '模式名称',
    userName: '用户名称',
    applicationName: '应用名称',
    startTime: '开始时间',
    finishTime: '结束时间',
    dbTime: 'db_time(ms)',
    cpuTime: 'cpu_time(ms)',
    excutionTime: '执行时长(ms)',
    sqlDetail: 'SQL详情',
    analysis: '分析诊断',
    baseInfo: '基本信息',
    sqlText: 'SQL 语句',
    statisticalInformation: '统计信息',
    implementationPlan: '执行计划',
    systemSource: '系统资源',
    objectInformation: '对象信息',
    indexSuggestions: '索引建议',
    objectStructure: '对象结构',
    indexInformation: '索引信息',
    reportDetail: '报告详情',
    sqlDiagnose: 'SQL诊断',
    baseInfoTitle: '基本信息',
    executionStatisticTitle: '执行统计信息',
    consumptionStatisticTitle: '耗时统计',
    consumingBreakdownTitle: '耗时细分',
    waitTimeLabel: '等待时间',
    cpuTimeLabel: 'CPU 时间',
    dbTimeLabel: 'DB时间',
    baseInfoOption: {
      debug_query_id: 'SQL ID',
      db_name: '数据库名称',
      schema_name: '模式名称',
      start_time: '开始时间',
      finish_time: '结束时间',
      user_name: '用户名称',
      application_name: '应用名称',
      socket: '客户端连接信息',
    },
    executeOption: {
      n_returned_rows: '返回行数',
      n_soft_parse: '软解析次数',
      n_tuples_fetched: '随机扫描行数',
      n_hard_parse: '硬解析次数',
      n_tuples_returned: '顺序扫描行数',
      net_send_info_size: '物理连接发送消息网络吞吐量',
      n_tuples_inserted: '插入行数',
      net_recv_info_size: '物理连接接收消息网络吞吐量',
      n_tuples_updated: '更新行数',
      net_stream_send_info_size: '逻辑连接发送消息网络吞吐量',
      n_tuples_deleted: '删除行数',
      net_stream_recv_info_size: '逻辑连接接收消息网络吞吐量',
      blocks_hit_rate: '缓存命中率',
      net_send_info_calls: '物理连接发送消息网络调用次数',
      lock_count: '加锁次数',
      net_recv_info_calls: '物理连接接收消息网络调用次数',
      lock_wait_count: '加锁等待次数',
      net_stream_send_info_calls: '逻辑连接发送消息网络调用次数',
      lock_max_count: '最大持锁数量',
      net_stream_recv_info_calls: '逻辑连接接收消息网络调用次数',
    },
    consumingOption: {
      execution_time: '执行器内执行时间',
      net_send_info_time: '物理连接发送消息网络耗时',
      parse_time: 'SQL解析时间',
      net_recv_info_time: '物理连接接收消息网络耗时',
      plan_time: 'SQL生成计划时间',
      net_stream_send_info_time: '逻辑连接发送消息网络耗时',
      rewrite_time: 'SQL重写时间',
      net_stream_recv_info_time: '逻辑连接接收消息网络耗时',
      pl_execution_time: 'plpgsql上的执行时间',
      lock_time: '加锁耗时',
      pl_compilation_time: 'plpgsql上的编译时间',
      lock_wait_time: '加锁等待耗时',
      data_io_time: 'IO上的时间花费',
    },
    mostWidth: '最大width',
    mostRows: '最大rows',
    mostCost: '最大cost',
    mostWidthPosi: '定位到最大width',
    mostRowsPosi: '定位到最大rows',
    mostCostPosi: '定位至最大单步运算cost',
    objBaseInfo: {
      schemaname: '模式名称',
      relname: '对象名称',
      objectType: '对象类型',
      objectSize: '对象大小',
      nLiveTup: '活元组数',
      nDeadTup: '死亡元组数',
      deadTupRatio: '死亡元组比例',
      lastVacuum: '上次vacuum时间',
      lastVacuumAutovacuum: '上次autovacuum时间',
      lastAnalyze: '上次analyze时间',
      lastAutoanalyze: '上次autoanalyze时间',
    },
    objStructure: {
      attnum: '字段顺序',
      attname: '字段名称',
      typname: '数据类型',
      attlen: '类型长度',
      attnotnull: '允许为空',
      description: '字段备注',
    },
    indexInfo: {
      relname: '索引名称',
      indisprimary: '主键索引',
      indisunique: '唯一索引',
      indisclustered: '簇索引',
      indisvalid: '可用于查询',
      indisreplident: '本次使用情况',
      def: '索引定义',
    },
    noOjcInfoTip: '暂无对象信息',
    placeholderTip:
      'SQL 语句有占位符，无法获取索引建议，建议将数据库中的 track_stmt_parameter 参数设置为 on，以获取新的无占位符的 SQL',
    objStructureOther: {
      commonTable: '普通表',
      indexes: '索引',
      sequence: '序列',
      toastTable: 'TOAST表',
      view: '视图',
      materializedView: '物化视图',
      combinationType: '组合类型',
      externalTable: '外部表',
      partitionTable: '分区表',
      partitionIndex: '分区索引',
      none: '无',
      isUsed: '被使用',
    },
    yes: '是',
    no: '否',
    isHasData: '暂无数据',
    failGetExecutionPlan: '获取不到执行计划',
    failResolveExecutionPlan: '无法解析该执行计划',
  },
  datasource: {
    paramAnalysis: '参数诊断',
    paramName: '参数名称',
    currentValue: '当前值',
    paramDescription: '参数说明',
    suggestValue: '推荐取值',
    suggestReason: '推荐原因',
    pleaseSelectInstance: '请先选择集群与实例',
    confirmToDeleteTask: '确定要删除该任务吗?',
    showNum: '显示数值',
    reportResultSelect: ['建议项', '所有诊断项'],
    diagnosisAddTaskSuccess: '诊断任务创建成功',
    diagnosis: '诊断',
    notMeetingConditions: '不满足建议条件',
    sqlID: 'SQL ID',
    slowSQL: '慢SQL',
    sqlTrackTask: 'SQL诊断任务',
    sqlTrackTaskStartTimeSelect: '执行开始时间',
    slowSQLSelectNode: '请选择集群和实例进行查询',
    name: '日志检索',
    slow: '慢日志分析',
    os: 'OS',
    database: '数据库',
    statistics: '慢日志统计',
    detail: '慢日志明细',
    ebpf: 'EBPF',
    statisInfo: '统计信息',
    sqlText: 'SQL语句',
    track: 'SQL诊断',
    addTrBtn: '新建任务',
    searchPlaceholder: '任务名称或SQL',
    addTaTitle: '新建跟踪任务',
    trackDetail: '任务详情',
    goBack: '返回上一级',
    detailTitle: '诊断详情',
    diagnosticResult: '诊断结果',
    taskInfo: '任务信息',
    taskName: '任务名称',
    reportDetail: '报告详情',
    noResult: '暂无数据',
    analysisReport: '分析报告',
    example: '实例',
    carryOut: '执行',
    flameDiagram: '火焰图',
    logType: '日志类型',
    selectLogType: '请选择日志类型',
    selectDatabaseType: '请选择数据库类型',
    selectTaskName: '请输入任务名称',
    selectSql: '请输入SQL',
    executeTime: '执行时间',
    logContent: '日志内容',
    cluterTitle: '集群/实例',
    createTime: '创建时间',
    option: '可选项',
    ebpfOnLable: 'onCPU分析',
    ebpfOffLable: 'offCPU分析',
    createTask: '创建任务',
    trackListTip: '点击左上角SQL诊断按钮，可触发explain analyze获取SQL新的执行计划，进行SQL跟踪',
    InstanceConfig: '实例配置',
    configPlaceholder: '集群ID或实例ID或属性值',
    editConfigTitle: '编辑实例配置',
    slowChartTitle: '本日慢SQL趋势图',
    taskInfoRemarkKey: '备注',
    slowLogTable: [
      '执行开始时间',
      '执行结束时间',
      'SQL语句',
      '库名',
      '客户端IP',
      '用户',
      '有效的DB时间',
      'CPU时间',
      'IO时间',
      'SQL解析时间',
      '存储过程执行时间',
      '加锁等待时间',
      '顺序扫描行数',
      'SELECT返回的结果集行数',
    ],
    slowStaticTable: [
      'Sql模板',
      '库名',
      '执行次数',
      '平均执行时间',
      '最大执行时间',
      '平均锁等待时间',
      '最大锁等待时间',
      '平均扫描行',
      '最大扫描行',
      '平均返回行',
      '最大返回行',
    ],
    ebpfFormRules: ['请选择实例', '请选择数据库'],
    trackFormRules: ['请填写任务名称', '请选择数据库', '请填写SQL语句', '请选择集群/实例'],
    trackTable: [
      '任务名称',
      '任务类型',
      '执行情况',
      '开始时间',
      '结束时间',
      '总耗时（秒）',
      '创建时间',
      '集群ID',
      '实例ID',
      '操作',
    ],
    logSearchTable: ['时间', '类型', '级别', '日志', '集群ID', '实例ID'],
  },
  spatial: {
    nodeId: '节点id',
    diskUsage: '磁盘使用率',
    tablespaceUsage: '表空间使用趋势',
    diskInfo: '磁盘信息',
    diskInfoTable: ['设备名称', '文件系统', '分区', '总空间', '可用空间', '使用率'],
    tablespaceInfo: '表空间信息',
    tablespaceInfoTable: ['节点名称', '表空间名称', '最大空间限制', '已用空间', '表空间路径'],
    vacuumDeadNum: '建议进行Vacuum的对象-根据死亡元组数排序',
    vacuumDeadNumTable: ['数据库名称', '模式名称', '表名称', '表大小', '活元组数', '死亡元组数', '死亡元组比例'],
    vacuumDeadPro: '建议进行Vacuum的对象-按死元组比例排序',
  },
  instance: {
    instanceManagement: '实例管理',
    addInstance: '添加实例',
    editInstance: '编辑实例',
  },
  report: {
    rowsDiffTitle: '估算rows和实际rows差异',
    rowsDiffStep: '运算步骤',
    estimateRows: '估计rows',
    actualRows: '实际rows',
    analyzeExcuationPlan: 'explain analyze执行计划',
    originalExecutionPlan: '原始执行计划',
    baseInfoTitle: '基本信息',
    baseInfoObj: {
      schemaname: '模式名称',
      deadTupRatio: '死亡元组比例',
      objectType: '对象类型',
      lastVacuum: '上次vacuum时间',
      objectSize: '对象大小',
      lastVacuumAutovacuum: '上次autovacuum时间',
      nLiveTup: '活元组数',
      lastAnalyze: '上次analyze时间',
      nDeadTup: '死亡元组数',
      lastAutoanalyze: '上次autoanalyze时间',
    },
    partitionTitle: '分区信息',
    partitionObj: {
      partStrategy: '分区策略',
      partKey: '分区键',
      interval: '间隔值',
      rangePartition: '范围分区',
      numericalPartition: '数值分区',
      intervalPartition: '间隔分区',
      listPartition: 'list分区',
      hashPartition: 'hash分区',
      invalidPartition: '无效分区',
    },
    workMemTitle: '外部排序消耗内存与work_mem差异',
    workMemObj: {
      consumesMemory: '外部排序消耗内存',
    },
    none: '无',
    table: '表',
    noIndexSuggestions: '暂无索引建议',
    ImproveQueryEfficiency: '提高查询效率',
    size: '大小',
    maximumConsumption: '全表扫描消耗最大',
    indexTemplate: '建议为 %t 表的 %c 列创建索引',
    multiColumnIndexTemplate: '建议为 %t 表的 %c 创建复合索引',
    suggestion: {
      ObjectInfoCheck: {
        id: 'ObjectInfoCheck',
        name: '分析思路',
        title: '对象统计信息检查',
        advise:
          '执行计划运算存在估算rows和实际rows差距较大，建议执行analyze更新相关对象统计信息，以生成最优的执行计划，提高执行效率',
        analysisIdea: `分析explain analyze生成的执行计划中，实际rows和估算rows差距较大的对象。请查看分析结果的报告详情，建议针对分析结果中的实际rows和估算rows存在差距的对象，进行统计信息更新操作，以生成最优的执行计划，提高执行效率。<br />
                可通过analyze tablename 命令更新对象统计信息。`,
      },
      ObjectRecommendedToUpdateStatistics: {
        id: 'ObjectRecommendedToUpdateStatistics',
        name: '诊断结果',
        title: '建议执行analyze更新对象统计信息',
        advise:
          '执行计划运算存在估算rows和实际rows差距较大，建议执行analyze更新相关对象统计信息，以生成最优的执行计划，提高执行效率',
      },
      ExecPlan: {
        id: 'ExecPlan',
        name: '分析思路',
        title: '执行计划',
        advise:
          '执行计划运算存在估算rows和实际rows差距较大，建议执行analyze更新相关对象统计信息，以生成最优的执行计划，提高执行效率',
        analysisIdea: `分析SQL原始执行计划，定位执行计划中消耗最大的运算步骤，根据分析路径生成对应的分析结果，分析路径包括 <br />
                    1、索引建议分析 <br />
                    2、对象结构分析 <br />
                    3、对象数据分析 <br />
                    4、占用内存消耗分析 <br />
                建议根据生成的分析结果，对SQL或相关相关对象进行优化和处理，以提高SQL执行效率。`,
      },
      PlanRecommendedToCreateIndex: {
        id: 'PlanRecommendedToCreateIndex',
        name: '诊断结果',
        title: '',
        advise: '',
      },

      PlanChangedToPartitionTable: {
        id: 'PlanChangedToPartitionTable',
        name: '诊断结果',
        title: '建议将 TABLE 表改成分区表',
        advise: 'TABLE 表活元组数超过两千万，建议改为分区表，尽可能针对单一分区查询，提高查询效率',
      },
      PlanRecommendedToQueryBasedOnPartition: {
        id: 'PlanRecommendedToQueryBasedOnPartition',
        name: '诊断结果',
        title: '建议基于分区查询，避免扫描所有分区',
        advise:
          'TABLE 表为分区表，但没有基于分区查询，建议查询分区表尽量要加上分区列的条件或基于分区键查询，避免扫描所有分区',
      },
      PlanRecommendedToDoVacuumCleaning: {
        id: 'PlanRecommendedToDoVacuumCleaning',
        name: '诊断结果',
        title: '建议对 TABLE 表做vacuum清理',
        advise: 'TABLE 表被删除或更新的记录数超过表总行数*0.2+50，建议对表做vacuum清理，避免消耗大量IO，影响查询效率',
      },
      PlanRecommendedToOptimizeStatementsOrAddWorkMemSize: {
        id: 'PlanRecommendedToOptimizeStatementsOrAddWorkMemSize',
        name: '诊断结果',
        title: '建议优化语句或增加work_mem大小',
        advise:
          '排序所需的内存超过work_mem参数的大小，建议优化语句或增加work_mem大小，以避免使用磁盘空间，影响查询效率',
      },
    },
    singleStepOperationCost: '单步运算cost',
    totalCost: '总cost',
  },
  histroyDiagnosis: {
    menuName: '历史数据诊断',
  },
}
