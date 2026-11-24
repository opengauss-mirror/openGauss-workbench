/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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
  isMigrationObject: boolean,
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
