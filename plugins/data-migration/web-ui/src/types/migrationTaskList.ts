interface subTaskList {
  sourceDbType: string,
  sourceNodeName: string,
  sourceIpPort: string,
  sourceNodeId: string,
  sourceDBName: string,
  sourceSchema: string[],
  sourceTables: string[],

  targetNodeName: string,
  targetIpPort: string,
  targetNodeId: string,
  targetDBName: string,

  configType: number,
  isAdjustKernelParam: boolean,
  isSystemAdmin: boolean,
  taskParamsObject: {
    basic:ParamItem[],
    more: ParamItem[],
  },
  id: string,
  curretTab: number,
  subTaskName: string,
  selectHost: string
  mode: number,
  isDefaultConfig: boolean,
  isSelectAlltables: boolean

  sourceIp: string,
  targetIp: string,
  sourcePort: number,
  targetPort: number,
}

interface migrationTaskList {
  taskId: number
  taskName: string
  subTaskData: subTaskList[]
  selectedHosts: string[]
}

interface ParamItem {
  paramKey: string
  paramValue: string
  paramDesc: string
}
