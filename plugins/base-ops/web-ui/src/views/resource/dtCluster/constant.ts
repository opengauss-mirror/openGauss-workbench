export enum CLUSER_ROLE {
  PRIMARY = "PRIMARY",
  STANDBY = "STANDBY",
}

// 单集群状态参考 https://docs-opengauss.osinfra.cn/zh/docs/latest/docs/DatabaseOMGuide/%E6%9F%A5%E7%9C%8B%E7%8A%B6%E6%80%81.html
export enum CLUSTER_STATE {
  NORMAL = "NORMAL",
  UNAVAILABLE = "UNAVAILABLE",
  DEGRADED = "DEGRADED",
  UNKWON = "UNKWON",
}

// 容灾集群状态参考 https://docs-opengauss.osinfra.cn/zh/docs/latest/docs/ToolandCommandReference/gs_sdr.html
export enum DT_CLUSTER_STATE {
  ARCHIVE = "ARCHIVE",
  FULL_BACKUP = "FULL_BACKUP",
  BACKUP_FAIL = "BACKUP_FAIL",
  ARCHIVE_FAIL = "ARCHIVE_FAIL",
  SWITCHOVER = "SWITCHOVER",
  RESTORE = "RESTORE",
  RESTORE_FAIL = "RESTORE_FAIL",
  RECOVERY = "RECOVERY",
  RECOVERY_FAIL = "RECOVERY_FAIL",
  PROMOTE = "PROMOTE",
  PROMOTE_FAIL = "PROMOTE_FAIL",
  UNKWON = "UNKWON",
}
