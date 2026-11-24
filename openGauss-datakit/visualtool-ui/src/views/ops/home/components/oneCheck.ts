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

export const oneCheck = {
  cluster: ['CheckClusterState', 'CheckDBParams', 'CheckDebugSwitch', 'CheckDirPermissions', 'CheckEnvProfile', 'CheckReadonlyMode', 'CheckDilateSysTab', 'CheckProStartTime', 'CheckMpprcFile'],
  db: ['CheckCurConnCount', 'CheckCursorNum', 'CheckTableSpace', 'CheckSysadminUser', 'CheckHashIndex', 'CheckPgxcRedistb', 'CheckNodeGroupName', 'CheckTDDate', 'CheckPgxcgroup'],
  os: ['CheckEncoding', 'CheckFirewall', 'CheckKernelVer', 'CheckMaxHandle', 'CheckNTPD', 'CheckOSVer', 'CheckSysParams', 'CheckTHP', 'CheckTimeZone', 'CheckCPU', 'CheckSshdService', 'CheckSshdConfig', 'CheckCrondService', 'CheckStack', 'CheckSysPortRange', 'CheckMemInfo', 'CheckHyperThread', 'CheckMaxProcMemory', 'CheckBootItems', 'CheckKeyProAdj', 'CheckFilehandle', 'CheckDropCache'],
  device: ['CheckBlockdev', 'CheckDiskFormat', 'CheckSpaceUsage', 'CheckInodeUsage', 'CheckSwapMemory', 'CheckLogicalBlock', 'CheckIOrequestqueue', 'CheckMaxAsyIOrequests', 'CheckIOConfigure'],
  network: ['CheckMTU', 'CheckPing', 'CheckRXTX', 'CheckNetWorkDrop', 'CheckMultiQueue', 'CheckRouting', 'CheckNICModel']
}
