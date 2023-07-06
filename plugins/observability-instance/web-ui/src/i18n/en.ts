///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

export default {
  app: {
    name: 'WorkBench',
    menuName: 'Monitor',
    refresh: 'refresh',
    autoRefresh: 'refresh',
    custom: 'Custom',
    startDate: 'Start Date',
    endDate: 'End Date',
    to: 'To',
    running: 'running',
    disconnect: 'disconnect',
    unknown: 'unknown',
    query: 'query',
    delete: 'delete',
    cancel: 'cancel',
    confirm: 'confirm',
    edit: 'edit',
    operate: 'operate',
    view: 'view',
    download: 'download',
    reset: 'Reset',
    back: 'Back',
    startTime: 'Start time',
    endTime: 'End time',
    lastOneHour: 'Last 1 hour',
    lastThreeHour: 'Last 3 hour',
    lastSixHour: 'Last 6 hour',
    refreshOn: 'Page refreshed on:',
    autoRefreshFor: 'Auto Refresh:',
    needSQLDiagnosis: 'Please install the smart diagnosis plug-in first',
    diagnosis: 'Diagnosis',
  },
  instanceMonitor: {
    instanceMonitor: 'Instance Monitor',
    clusterTitle: 'Cluster/Instances:',
    index: 'Home',
    resourceMonitor: 'Resource Monitor',
    instance: {
      connectionQty: 'Connections',
      slowSQL3s: 'Number Of Slow Sqls(>3s)',
      rollbackQty: 'Transaction Rollback Count',
      commitQty: 'Transaction Commits',
      transTotalQty: 'Total Number Of Transaction Commits And Rollbacks',
      queryQty: 'Query Transaction Count',
      idleConnectionQty: 'Current Number Of Idle Connections',
      activeConnectionQty: 'Current Active Connections',
      connectionQtyNow: 'Current Connections',
      maxConnectionQty: 'Total Connections',
      slowSQLQty: 'Slow SQL Number',
    },
  },
  resourceMonitor: {
    memoryTab: 'Memory',
    ioTab: 'IO',
    networkTab: 'Network',
    cpu: {
      cpuUse: 'CPU Usage',
      cpuLoad: 'Average CPU Load',
      topProcess: 'TOP OS Process',
      topThread: 'TOP DB Thread',
      total5mLoad: '5m load',
      coreNum: 'Total cores',
      dbThread: 'Database Thread',
    },
    memory: {
      memoryUse: 'Memory Usage',
      memoryDBUse: 'DB Process Memory Usage',
      physicalmemory: 'Total Physical Memory',
      usedMemory: 'Used Memory',
      freeMemory: 'Free Memory',
      cachedMemory: 'Cached Memory',
      interactiveAreaUsage: 'Interactive Area Usage',
      totalExchangeArea: 'Total Exchange Area',
      ysedSwapArea: 'Used Swap Area',
      topProcess: 'TOP OS Process',
      topThread: 'TOP DB Thread',
      instanceMemoryUsage: 'Instance Memory Usage',
      memoryName: 'Memory Name',
      description: 'Description',
      sizeOfMemoryUsed: 'Size Of Memory Used(MB)',
      parameterConfiguration: 'Instance Memory Parameter Configuration',
      parameterName: 'Parameter Name',
      settings: 'Settings',
    },
    io: {
      deviceIO: 'Disk Partition IO Statistics',
      rwSecond: 'Read/Write Bytes Per Second',
      queueLength: 'I/O Queue Length',
      ioUsage: 'I/O Usage',
      ioTime: 'Average I/O Response Time',
    },
    network: {
      in: 'Network Traffic (Incoming)',
      out: 'Network Traffic (Outgoing)',
      lost: 'NIC Packet Loss Quantity',
      connection: 'Network Socket Connection Information',
      tcpQty: 'Number of TCP Sockets',
      UDPQty: 'Number of UDP Sockets',
      card: 'NIC Transmission Statistics',
    },
  },
  instanceIndex: {
    instanceMetrics: 'Instance Metrics',
    memory: 'Memory',
    networkInOut: 'Network',
    activeSessionQty: 'Number of Active Sessions',
    activeSessionQtyTips: 'You can select an interval in the graph to drill down for analysis',
    threadPoolUsed: 'Thread Pool Usage',
    nowTOPSQL: 'Real-time TOPSQL',
    dbName: 'DB Name',
    userName: 'User Name',
    appName: 'App Name',
    startTime: 'Start Time',
    costTime: 'Cost Time(s)',
    sessionId: 'Session ID',
    detail: 'detail',
  },
  trans: {
    longTransaction: 'LongTransaction',
    sessionId: 'SessionID',
    username: 'UserName',
    databaseName: 'DatabaseName',
    applicationName: 'ApplicationName',
    clientAddress: 'ClientAddress',
    transactionStartTime: 'TransactionStartTime',
    transactionDuration: 'TransactionDuration',
    sessionStatus: 'SessionStatus',
  },
  session: {
    tabTitle: 'Session Details',
    maxSessionCount: 'Max Session Count:',
    activeSessionCount: 'Active Session Count:',
    blockedSessionCount: 'Blocked Session Count:',
    longestSessionRuntime: 'Longest Session Runtime:',
    sessionCount: 'Session Count',
    waitEvents: 'Wait Events',
    blockSessions: {
      tabTitle: 'Blocked Sessions',
      collapseAll: 'Collapse All',
      sessionID: 'Session ID',
      blockedSessionID: 'Blocked Session ID',
      sessionStartTime: 'Session Start Time',
      waitState: 'Wait State',
      waitEvent: 'Wait Event',
      waitLockMode: 'Wait Lock Mode',
      dbName: 'Database Name',
      userName: 'User Name',
      clientIP: 'Client IP',
      appName: 'Application Name',
    },
    trans: {
      tabTitle: 'Long Transactions',
      longTransaction: 'Long Transaction',
      sessionID: 'Session ID',
      userName: 'User Name',
      dbName: 'Database Name',
      appName: 'Application Name',
      clientAddr: 'Client Address',
      txStart: 'Transaction Start Time',
      txDuration: 'Transaction Duration',
      sessionState: 'Session Status',
    },
    sessionActive: {
      activeSessionCount: 'Active Session Count',
      idleConnectionCount: 'Idle Connection Count',
      maxConnectionCount: 'Max Connection Count',
      waitingConnectionCount: 'Waiting Connection Count',
    },
    detail: {
      tabTitle: 'Session Details',
      sessionID: 'Session ID:',
      info: {
        tabTitle: 'General Information',
        server: 'Server',
        sessionStatus: 'Session Current Status:',
        sessionID: 'Session ID:',
        osThreadID: 'Operating System Thread ID:',
        dbUserName: 'Database User Name:',
        loginTime: 'Login Time:',
        loginDuration: 'Login Duration:',
        resourcePool: 'Resource Pool:',
      },
      client: {
        tabTitle: 'Client Machine',
        clientIP: 'Client IP Address:',
        clientHostName: 'Client Host Name:',
        clientTCPPort: 'Client TCP Port:',
        appName: 'Application Name:',
        connectDBName: 'Connected Database Name:',
        txStartTime: 'Transaction Start Time:',
        queryStartTime: 'Query Start Time:',
        queryID: 'Query ID:',
      },
      block: {
        tabTitle: 'Contention',
        blockedSessionID: 'Blocked Session ID:',
        file: 'File:',
        pageNumber: 'Page Number:',
        lineNumber: 'Line Number:',
        bucketNumber: 'Bucket Number:',
      },
      wait: {
        tabTitle: 'Wait',
        waitState: 'Wait State:',
        waitEventType: 'Wait Event Type:',
        waitLockMode: 'Wait Lock Mode:',
        waitObject: 'Wait Object:',
      },
      currentQuerySQL: 'Current Query SQL',
      statistic: {
        tabTitle: 'Statistic Information',
        sessionStatusStatistics: 'Session Status Statistics',
        sessionRuntimeInformation: 'Session Runtime Information',
        name: 'Name',
        value: 'Value',
      },
      blockTree: {
        tabTitle: 'Block Tree',
        sessionID: 'Session ID',
        blockedSessionID: 'Blocked Session ID',
        sessionStartTime: 'Session Start Time',
        waitState: 'Wait State',
        waitEvent: 'Wait Event',
        waitLockMode: 'Wait Lock Mode',
        dbName: 'Database Name',
        userName: 'User Name',
        clientIP: 'Client IP',
        appName: 'Application Name',
      },
      waitRecord: {
        tabTitle: 'Wait Event Records',
        sampleTime: 'Sample Time',
        waitState: 'Wait State',
        waitEvent: 'Wait Event',
        waitLockMode: 'Wait Lock Mode',
        lockInfo: 'Lock Information',
      },
    },
  },
  install: {
    nodepkg: 'node exporter',
    nodesrc: 'Uploaded Package',
    gausspkg: 'openGauss exporter',
    gausssrc: 'Uploaded Package',
    uploadPkg: 'Upload Package',
    uploadSucceed: 'Upload Succeed',
    uploadFail: 'Upload Fail',
    pleaseUpload: 'Please upload',
    uploadInfo: 'Drag the file here, or click to upload the file',
    downloadSuggest: 'Recommended download address',
    installMode: 'Installation method',
    installPath: 'Installation path',
    online: 'Online',
    offline: 'Offline',
    install: 'Install',
    installAgent: 'Install proxy',
    installProxy: 'Install server',
    installedAgent: 'Installed proxy',
    installedProxy: 'Installed server',
    uninstall: 'uninstall',
    uninstallProxy: 'uninstall server',
    uninstallAgent: 'uninstall proxy',
    uninstallQuick: 'uninstall quick',
    machine: 'machine',
    installUser: 'Installation User',
    rootPWD: 'root password',
    proxyPort: 'server port',
    serverCollectPort: 'server collection port',
    datasourceCollectPort: 'datasource collection port',
    collectInstance: 'collect instance',
    collectProxy: 'collect proxy',
    uploadPath: 'Upload path',
    continueUpload: 'continue to upload',
    installServerAlert: 'please install the server first!',
    installedServerAlert: 'it is only allow to install one server!',
    proxyRules: [
      'choose the machine please',
      'input the password of root please',
      'input the proxy port please',
      'choose an installer please',
    ],
    collectorRules: [
      'choose the instance please',
      'input the password of root please',
      'input the server collection port please',
      'input the datasource collection port please',
    ],
  },
  configParam: {
    tabTitle: 'system && database configuration',
    systemConfig: 'system configuration ',
    databaseConfig: 'database configuration',
    paramDesc: 'parameter description',
    paramTuning: 'parameter optimization',
    suggestValue: 'suggestion value:',
    suggestReason: 'suggestion reason:',
    rootPWDTitle: 'please input the password of the root',
    rootPWD: 'the password of the root',
    queryValidInfo: 'please choose the instance',
  },
  dashboard: {
    name: 'Dashboard',
    addDsBtn: 'Add Instance',
    searchPlaceholder: 'filter by instance name or IP',
    table: [
      'Instance Name',
      'Server Status',
      'Database Status',
      'Connection Info',
      'Database Version',
      'CPU Usage Of Server',
      'Memory Usage Of Server',
      'Number Of Database Avtivity Session',
      'Number Of Database Block Session',
    ],
    instance: 'Instance Monitoring',
    load: 'Performance Load',
    systemConfig: {
      tabName: 'System Configuration',
      osTabName: 'System Parameter',
      dbTabName: 'DB Parameter',
    },
    wdrReports: {
      tabName: 'WDR Reports',
      clusterName: 'Cluster Name',
      hostId: 'Host IP',
      reportRange: 'Report Scope',
      reportRangeSelect: ['CLUSTER', 'NODE'],
      reportType: 'Report Type',
      reportTypeSelect: ['DETAIL', 'SUMMARY', 'ALL'],
      buildTime: 'Production time',
      snapshotManage: 'Snapshot Manage',
      buildWDR: 'Generate WDR',
      confirmDel: 'Are you sure to delete this?',
      list: {
        buildTime: 'Report Generation Time',
        reportName: 'Report Name',
      },
      snapshotManageDialog: {
        dialogName: 'Snapshot Manage',
        createSnapshot: 'Create Snapshot',
        snapshotID: 'Snapshot ID',
        captureTime: 'Capture Time',
        buildSuccess:
          'Created successfully! There may be a lag in the asynchronous writing of the snapshot list, please refresh the list manually!',
      },
      buildWDRDialog: {
        startSnapshot: 'Start Snapshot',
        endSnapshot: 'End Snapshot',
        build: 'Generate',
        buildSuccess: 'Generate suceed',
        buildFail: 'Generate fail',
      },
    },
    session: 'Session Management',
    slow: 'Slow SQL',
    space: 'Spatial Analysis',
    day: 'day',
    hour: 'hour',
    range: 'Range',
    last1H: 'Last Hour',
    last3H: 'Last 3 Hours',
    last6H: 'Last 6 Hours',
    last12H: 'Last 12 Hours',
    last1D: 'Last 1 Day',
    last2D: 'Last 2 Days',
    last7D: 'Last 7 Days',
    cpuUsage: 'CPU Usage',
    cpuUseSituation: 'CPU Use Situation',
    networkTransmissionRate: 'Network Transmission Rate',
    load5m: '5m load',
    memoryUsage: 'Memory Usage',
    diskReadRate: 'Disk Read Rate',
    diskWriteRate: 'Disk Write Rate',
    uploadRate: 'Upload Rate',
    downloadRate: 'Download Rate',
    serverInfo: {
      title: 'Server Basic Information',
      ip: 'IP',
      host: 'Host Name',
      systemVersion: 'System Version',
      runningTime: 'Running time',
      memory: 'Memory',
      coreNumber: 'Number of CPU Cores',
      cpuProcessor: 'CPU Processor',
      totalProcesses: 'Total Processes',
      totalDisk: 'Total Disk',
    },
    runnning: 'runnning',
    memory: 'memory',
    disk: 'disk',
    timeConsumption: 'Time Consumption',
    runningInAnalysis: 'Running In Analysis',
    runningInAnalysisTip:
      'You can select an interval in the figure to drill down and analyze the SQL execution in the corresponding time period',
    uncheckRegion: 'Uncheck Region',
    waitEvent: 'Wait Event',
    serverResources: 'Server Resources',
    loadAndCpu: 'Overall total load and overall average CPU utilization',
    memoryAndAverageMemory: 'Overall total memory and overall average memory usage',
    diskAndAverageDisk: 'Overall total disk and overall average disk utilization',
    diskReadAndWriteRate: 'Disk read/write rate (IOPS)',
    diskReadAndWriteRate2: 'Disk read/write rate',
    diskReadAndWritCapacity: 'Disk read/write capacity per second',
    networkBandwidthUsage: 'Network bandwidth usage per second',
    networkSocketConnection: 'Network Socket Connection Information',
    databaseLoad: 'Database Load',
    tps: 'TPS',
    qps: 'QPS',
    connectionNum: 'Number of connections',
    slowSqlMoreThan3Seconds: 'Slow SQL (more than 3 seconds)',
    longTransactionNumGreaterThan30Seconds: 'Number of long transactions (greater than 30 seconds)',
    sqlResponseTime: 'SQL response time',
    transactionLockInfo: 'Transaction lock information statistics',
    cacheHitRate: 'Cache Hit Rate',
    ioBlockNumberTrend: 'Database file IO block number trend',
    ScrubDirtyPageInfo: 'Scrub dirty page information',
    currentRate: 'Current Rate',
    minimumRate: 'Minimum Rate',
    averageRate: 'Average Rate',
    maxRate: 'Max Rate',
    currentCapacity: 'Current Capacity',
    minimumCapacity: 'Minimum Capacity',
    averageCapacity: 'Average Capacity',
    maxCapacity: 'Max Capacity',
    totalLoad: 'Total Load',
    allProtocolSocketsUsed: 'Total number of all protocol sockets used',
    serverDetail: 'Server Detail',
    sessionStatistics: 'Session Statistics',
    sessionList: 'Session List',
    userStatistics: 'User Statistics',
    accessSourceStatistics: 'Access Source Statistics',
    databaseStatistics: 'Database Statistics',
    rangeTimeTip: 'The box selection area is the SQL execution time range',
    capacity: 'capacity',
    rate: 'rate',
    connectStatus: {
      success: 'success',
      error: 'error',
    },
    topsqlListTip:
      "To perform TopSQL monitoring, you need to set 'enable_stmt_track'、'enable_resource_track' parameter to 'on'，'track_stmt_stat_level' parameter Full SQL level to at least 'L0'",
    pleaseChooseinstanceId: 'Please select an instance',
  },
  metric: {
    totalCoreNum: 'Total number of cores',
    total5mLoad: 'total 5m Load',
    totalAverageUtilization: 'Total Average Utilization',
    diskIOUsage: 'Disk IO usage',
    systemUsage: 'System Usage',
    userUsage: 'User Usage',
    totalUsage: 'total Usage',
    totalMemory: 'total Memory',
    usedMemory: 'Used Memory',
    totalDisks: 'Total Disks',
    totalNumber: 'Total Usage',
    read: 'Read',
    write: 'Write',
    upload: 'Upload',
    download: 'Download',
    transactionRollbackNum: 'Number of transaction rollback',
    transactionCommitments: 'Transaction Commitments',
    transactionAndRollbackTotal: 'Total number of transaction committed and rolled back',
    queryTransactions: 'Number of query transactions',
    currentIdleConnections: 'Current idle connections',
    currentActiveConnections: 'Current active connections',
    currentConnections: 'Current connections',
    totalConnections: 'Total connections',
    slowSqlNum: 'Slow SQL Number',
    longTransactions: 'Number of long transactions',
    sqlResponseTime80: '80% SQL response time',
    sqlResponseTime95: '95% SQL response time',
    queryCacheHitRate: 'Query cache hit rate',
    databaseCacheHitRate: 'Database cache hit rate',
    readPhysicalFileBlockNum: 'Number of read physical file blocks',
    writePhysicalFileBlockNum: 'Number of physical file blocks written',
    lastBatchDirtyPageNum: 'Number of dirty pages in the last batch',
    currentRemainingDirtyPages: 'Current estimated remaining dirty pages',
  },
  sql: {
    dbName: 'Database Name',
    schemaName: 'Schema Name',
    userName: 'User Name',
    applicationName: 'Application Name',
    startTime: 'Start Time',
    finishTime: 'Finish Time',
    dbTime: 'DB_TIME(ms)',
    cpuTime: 'CPU_TIME(ms)',
    excutionTime: 'EXEC_TIME(ms)',
    sqlDetail: 'SQL Detail',
    analysis: 'Analysis',
    baseInfo: 'Base Info',
    sqlText: 'Sql Text',
    statisticalInformation: 'Statistical Information',
    implementationPlan: 'Implementation plan',
    systemSource: 'System Source',
    objectInformation: 'Object information',
    indexSuggestions: 'Index suggestions',
    waitEvent: 'Wait Event Statistics',
    objectStructure: 'Object Structure',
    indexInformation: 'Index Information',
    reportDetail: 'Report Detail',
    sqlDiagnose: 'sql sqlDiagnose',
    sqlDiagnoseCreateTask: 'Create diagnose task',
    baseInfoTitle: 'Base Infomation',
    executionStatisticTitle: 'Execution Statistic',
    consumptionStatisticTitle: 'Consumption Statistic',
    consumingBreakdownTitle: 'Consuming Breakdown',
    waitTimeLabel: 'waiting time',
    cpuTimeLabel: 'cpu time',
    dbTimeLabel: 'db time',
    baseInfoOption: {
      debug_query_id: 'SQL ID',
      db_name: 'Database Name',
      schema_name: 'Schema Name',
      start_time: 'Start Time',
      finish_time: 'Finish Time',
      user_name: 'User Name',
      application_name: 'Application Name',
      socket: 'Client Connection Information',
    },
    executeOption: {
      n_returned_rows: 'Number of returned rows',
      n_soft_parse: 'Soft resolution times',
      n_tuples_fetched: 'Number of random scan lines',
      n_hard_parse: 'Hard resolution times',
      n_tuples_returned: 'Number of sequential scan lines',
      net_send_info_size: 'Network throughput of physical connection sending messages',
      n_tuples_inserted: 'Number of inserted rows',
      net_recv_info_size: 'Network throughput of physical connection receiving messages',
      n_tuples_updated: 'Update rows',
      net_stream_send_info_size: 'Network throughput of messages sent by logical connection',
      n_tuples_deleted: 'Number of deleted rows',
      net_stream_recv_info_size: 'Network throughput of logical connection receiving messages',
      blocks_hit_rate: 'cache hit rate',
      net_send_info_calls: 'Number of network calls for physical connection to send messages',
      lock_count: 'Locking times',
      net_recv_info_calls: 'Number of network calls for physical connection to receive messages',
      lock_wait_count: 'Number of lock waiting times',
      net_stream_send_info_calls: 'Number of network calls for sending messages via logical connection',
      lock_max_count: 'Maximum number of locks',
      net_stream_recv_info_calls: 'Number of logical connection receiving message network calls',
    },
    consumingOption: {
      execution_time: 'Execution time in actuator',
      net_send_info_time: 'Time consuming for physical connection to send messages',
      parse_time: 'SQL parsing time',
      net_recv_info_time: 'Time consuming for physical connection to receive messages',
      plan_time: 'SQL generation plan time',
      net_stream_send_info_time: 'Network time consumption of sending messages through logical connection',
      rewrite_time: 'SQL rewrite time',
      net_stream_recv_info_time: 'Time consumption of logical connection receiving message network',
      pl_execution_time: 'Execution time on PLPGSQL',
      lock_time: 'Time consuming for locking',
      pl_compilation_time: 'Compilation time on PLPGSQL',
      lock_wait_time: 'Time consuming for lock waiting',
      data_io_time: 'Time spent on IO',
    },
    mostWidth: 'most width',
    mostRows: 'most rows',
    mostCost: 'most cost',
    mostWidthPosi: 'Locate to the maximum width',
    mostRowsPosi: 'Locate to the maximum rows',
    mostCostPosi: 'Locate to the maximum single step operation cost',
    objBaseInfo: {
      schemaname: 'schema name',
      relname: 'object name',
      objectType: 'object type',
      objectSize: 'object size',
      nLiveTup: 'Number of live tuples',
      nDeadTup: 'Number of dead tuples',
      deadTupRatio: 'Dead tuple ratio',
      lastVacuum: 'Last vacuum time',
      lastVacuumAutovacuum: 'Last autovacuum time',
      lastAnalyze: 'Last analyze time',
      lastAutoanalyze: 'Last autoanalyze time',
    },
    objStructure: {
      attnum: 'Field order',
      attname: 'Field Name',
      typname: 'data type',
      attlen: 'Type Length',
      attnotnull: 'Allow to be empty',
      description: 'Field Remarks',
    },
    indexInfo: {
      relname: 'Index Name',
      indisprimary: 'primary key',
      indisunique: 'unique index',
      indisclustered: 'Cluster index',
      indisvalid: 'Available for query',
      indisreplident: 'Usage',
      def: 'Index definition',
    },
    noOjcInfoTip: 'No object information',
    placeholderTip:
      'The SQL statement has a placeholder, unable to obtain index suggestions. It is recommended that the tracks in the database_ stmt_ The parameter parameter is set to on to obtain new SQL without placeholder',
    objStructureOther: {
      commonTable: 'common Table',
      indexes: 'indexes',
      sequence: 'sequence',
      toastTable: 'TOAST table',
      view: 'view',
      materializedView: 'materialized view',
      combinationType: 'combination type',
      externalTable: 'external table',
      partitionTable: 'partition table',
      partitionIndex: 'partition index',
      none: 'none',
      isUsed: 'used',
    },
    waitEventTable: {
      startTime: 'Start Time',
      waitEventName: 'Wait Event Name',
      waitLockTime: 'Wait Lock Time (us)',
    },
    yes: 'yes',
    no: 'no',
    isHasData: 'No Data',
    failGetExecutionPlan: 'Failed to get the execution plan',
    failResolveExecutionPlan: 'The execution plan could not be resolved',
    executionParamTip:
      "To perform Execution Plan, you need to set 'track_stmt_stat_level' parameter Full SQL level to at least 'L1'",
  },
  datasource: {
    diagnosisAddTaskSuccess: 'Diagnostic task created successfully',
    confirmToDeleteTask: 'Confirm to delete this task?',
    name: 'Datasource',
    slow: 'Slow Log Analysis',
    os: 'OS',
    database: 'Database',
    statistics: 'Slow Log Statistics',
    detail: 'Slow Log Detail',
    ebpf: 'EBPF',
    track: 'SQL track',
    addTrBtn: 'Add task',
    searchPlaceholder: 'Task name or task type or SQL',
    addTaTitle: 'Add track task',
    trackDetail: 'Task detail',
    goBack: 'Go back',
    detailTitle: 'Track detail',
    diagnosticResult: 'Diagnostic results',
    taskInfo: 'Task info',
    taskName: 'Task name',
    reportDetail: 'Report detail',
    noResult: 'No result',
    analysisReport: 'Analysis report',
    example: 'Living example',
    carryOut: 'Carry out',
    flameDiagram: 'Flame diagram',
    logType: 'Log type',
    selectLogType: 'Please select the log type',
    selectDatabaseType: 'Please select the database type',
    selectTaskName: 'Please write the task name',
    selectSql: 'Please write the sql',
    executeTime: 'Execute time',
    logContent: 'Log content',
    cluterTitle: 'Clusters/instances',
    createTime: 'Create time',
    option: 'Option',
    ebpfOnLable: 'OnCPU analysis',
    ebpfOffLable: 'OffCPU analysis',
    paramAnalysis: 'Parameter diagnosis',
    createTask: 'Create task',
    cancel: 'Cancel',
    trackListTip:
      'Click the SQL diagnosis button in the upper left corner to trigger the explain analyze to obtain the new execution plan of the SQL and conduct the SQL tracking',
    InstanceConfig: 'Instance config',
    configPlaceholder: 'Cluster ID or Instance ID or property value',
    editConfigTitle: 'Edit Instance Config',
    slowChartTitle: 'Slow log trend chart',
    slowLogTable: [
      'Start time',
      'Finish time',
      'Sql text',
      'Db name',
      'Ip address',
      'User',
      'Db time',
      'Cpu time',
      'Data io time',
      'Parse time',
      'Pl execution time',
      'Lock wait time',
      'Tuples returned',
      'Returned rows',
    ],
    slowStaticTable: [
      'Sql template',
      'Db name',
      'Execution num',
      'Execution time avg',
      'Execution time max',
      'Lockwait time avg',
      'Lockwait time max',
      'Scan row avg',
      'Scan row max',
      'Return row avg',
      'Return row max',
    ],
    ebpfFormRules: ['Please select the instance', 'Please select the database'],
    trackFormRules: [
      'Please write the task of name',
      'Please select the database',
      'Please write the sql',
      'Please select the Cluster/Instance',
      'Please choose a snapshot',
      'The start of snapshot id must be less than the end of snapshot id',
      'The end of snapshot id must be greater than the start of snapshot id',
    ],
    trackTable: [
      'Track name',
      'Task type',
      'State',
      'Start time',
      'End time',
      'Cost time(s)',
      'Create time',
      'Cluster ID',
      'Instance ID',
      'Operate',
    ],
    configTable: ['Time', 'Type', 'Grade', 'log', 'Cluster ID', 'Instance ID'],
  },
  report: {
    rowsDiffTitle: 'Difference between estimated rows and actual rows',
    rowsDiffStep: 'Operation steps',
    estimateRows: 'Estimated rows',
    actualRows: 'Actual rows',
    analyzeExcuationPlan: 'Explain analyze execution plan',
    originalExecutionPlan: 'Original execution plan',
    baseInfoTitle: 'essential information',
    baseInfoObj: {
      schemaname: 'Mode Name',
      deadTupRatio: 'Dead tuple ratio',
      objectType: 'object type',
      lastVacuum: 'Last vacuum time',
      objectSize: 'Object Size',
      lastVacuumAutovacuum: 'Last autovacuum time',
      nLiveTup: 'Number of live tuples',
      lastAnalyze: 'Last analyze time',
      nDeadTup: 'Number of dead tuples',
      lastAutoanalyze: 'Last autoanalyze time',
    },
    partitionTitle: 'Partition information',
    partitionObj: {
      partStrategy: 'Partition policy',
      partKey: 'Partition key',
      interval: 'Interval value',
      rangePartition: 'Range partition',
      numericalPartition: 'Numerical partition',
      intervalPartition: 'Interval partition',
      listPartition: 'List partition',
      hashPartition: 'Hash partition',
      invalidPartition: 'Invalid partition',
    },
    workMemTitle: 'External sorting consumes memory and work_ Mem difference',
    workMemObj: {
      consumesMemory: 'External sorting consumes memory',
    },
    none: 'none',
    table: 'table',
    noIndexSuggestions: 'No index suggestions',
    ImproveQueryEfficiency: 'improve query efficiency',
    size: 'size',
    maximumConsumption: 'Maximum consumption of full table scanning',
    indexTemplate: 'It is recommended to create an index for column %c of table %t',
    multiColumnIndexTemplate: 'It is recommended to create a composite index for column %c of table %t',
    suggestion: {
      ObjectInfoCheck: {
        id: 'ObjectInfoCheck',
        name: 'Analysis ideas',
        title: 'Object statistics check',
        advise:
          'There is a large gap between the estimated rows and the actual rows in the execution plan calculation. It is recommended to execute analyze to update the statistics of related objects to generate the optimal execution plan and improve the execution efficiency',
        analysisIdea: `Analyze the objects with large gap between actual rows and estimated rows in the execution plan generated by explain analyze. Please check the report details of the analysis results. It is recommended to update the statistics for the objects with gaps between the actual rows and the estimated rows in the analysis results, so as to generate the optimal execution plan and improve the execution efficiency. <br />
                Object statistics can be updated with the analyze tablename command. `,
      },
      ObjectRecommendedToUpdateStatistics: {
        id: 'ObjectRecommendedToUpdateStatistics',
        name: 'Diagnostic results',
        title: 'It is recommended to execute analyze to update object statistics',
        advise:
          'There is a large gap between the estimated rows and the actual rows in the execution plan calculation. It is recommended to execute analyze to update the statistics of related objects to generate the optimal execution plan and improve the execution efficiency',
      },
      ExecPlan: {
        id: 'ExecPlan',
        name: 'Analysis ideas',
        title: 'Execution plan',
        advise:
          'There is a large gap between the estimated rows and the actual rows in the execution plan calculation. It is recommended to execute analyze to update the statistics of related objects to generate the optimal execution plan and improve the execution efficiency',
        analysisIdea: `Analyze the original SQL execution plan, locate the calculation step that consumes the most in the execution plan, and generate the corresponding analysis results according to the analysis path, which includes <br />
                    1、Index suggestion analysis <br />
                    2、Object structure analysis <br />
                    3、Object data analysis <br />
                    4、Memory consumption analysis <br />
                    It is recommended to optimize and process SQL or related objects according to the generated analysis results to improve SQL execution efficiency.`,
      },
      PlanRecommendedToCreateIndex: {
        id: 'PlanRecommendedToCreateIndex',
        name: 'Diagnostic results',
        title: '',
        advise: '',
      },

      PlanChangedToPartitionTable: {
        id: 'PlanChangedToPartitionTable',
        name: 'Diagnostic results',
        title: 'It is recommended to change TABLE to partition table',
        advise:
          'The number of live tuples in the TABLE exceeds 20 million. It is recommended to change it to a partitioned table to query a single partition as far as possible to improve the query efficiency',
      },
      PlanRecommendedToQueryBasedOnPartition: {
        id: 'PlanRecommendedToQueryBasedOnPartition',
        name: 'Diagnostic results',
        title: 'It is recommended to query based on partitions to avoid scanning all partitions',
        advise:
          'TABLE is a partitioned table, but it is not queried based on partitions. It is recommended to add partition column conditions or query based on partition keys to query partitioned tables to avoid scanning all partitions',
      },
      PlanRecommendedToDoVacuumCleaning: {
        id: 'PlanRecommendedToDoVacuumCleaning',
        name: 'Diagnostic results',
        title: 'It is recommended to clean the TABLE vacuum',
        advise:
          'The number of deleted or updated records in the TABLE exceeds the total number of table rows * 0.2+50. It is recommended to clean the table vacuum to avoid consuming a lot of IO and affecting query efficiency',
      },
      PlanRecommendedToOptimizeStatementsOrAddWorkMemSize: {
        id: 'PlanRecommendedToOptimizeStatementsOrAddWorkMemSize',
        name: 'Diagnostic results',
        title: 'It is recommended to optimize statements or add work_ Mem size',
        advise:
          'Memory required for sorting exceeds work_ The size of the mem parameter. It is recommended to optimize the statement or add work_ The size of mem to avoid using disk space and affect query efficiency',
      },
    },
    singleStepOperationCost: '单步运算cost',
    totalCost: '总cost',
  },
}
