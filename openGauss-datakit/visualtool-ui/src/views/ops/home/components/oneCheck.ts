export const oneCheck = {
  cluster: ['CheckClusterState', 'CheckDBParams', 'CheckDebugSwitch', 'CheckDirPermissions', 'CheckEnvProfile', 'CheckReadonlyMode', 'CheckDilateSysTab', 'CheckProStartTime', 'CheckMpprcFile'],
  db: ['CheckCurConnCount', 'CheckCursorNum', 'CheckTableSpace', 'CheckSysadminUser', 'CheckHashIndex', 'CheckPgxcRedistb', 'CheckNodeGroupName', 'CheckTDDate', 'CheckPgxcgroup'],
  os: ['CheckEncoding', 'CheckFirewall', 'CheckKernelVer', 'CheckMaxHandle', 'CheckNTPD', 'CheckOSVer', 'CheckSysParams', 'CheckTHP', 'CheckTimeZone', 'CheckCPU', 'CheckSshdService', 'CheckSshdConfig', 'CheckCrondService', 'CheckStack', 'CheckSysPortRange', 'CheckMemInfo', 'CheckHyperThread', 'CheckMaxProcMemory', 'CheckBootItems', 'CheckKeyProAdj', 'CheckFilehandle', 'CheckDropCache'],
  device: ['CheckBlockdev', 'CheckDiskFormat', 'CheckSpaceUsage', 'CheckInodeUsage', 'CheckSwapMemory', 'CheckLogicalBlock', 'CheckIOrequestqueue', 'CheckMaxAsyIOrequests', 'CheckIOConfigure'],
  network: ['CheckMTU', 'CheckPing', 'CheckRXTX', 'CheckNetWorkDrop', 'CheckMultiQueue', 'CheckRouting', 'CheckNICModel']
}
