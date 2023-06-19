export const INSTALL_TYPE = {
  ONLINE: 0,
  OFFLINE: 1
}

export const PORTAL_INSTALL_STATUS = {
  NOT_INSTALL: 0,
  INSTALLING: 1,
  INSTALLED: 2,
  FAILED: 10
}

export const PORTAL_PARAM_TYPE = {
  STRING: 1,
  NUMBER: 2,
  BOOLEAN: 3,
  SELECT: 4,
  REGEX: 5,
  VAR: 6,
  OBJECT_ARRAY: 9
}

export const TASK_STATUS = {
  NOT_RUN: 0,
  FULL_START: 1,
  FULL_RUNNING: 2,
  FULL_FINISH: 3,
  FULL_CHECK_START: 4,
  FULL_CHECKING: 5,
  FULL_CHECK_FINISH: 6,
  INCREMENTAL_START: 7,
  INCREMENTAL_RUNNING: 8,
  // when click stop incremental
  INCREMENTAL_FINISHED: 9,
  // stop incremental complete
  INCREMENTAL_STOPPED: 10,
  REVERSE_START: 11,
  REVERSE_RUNNING: 12,
  REVERSE_STOP: 13,
  MIGRATION_FINISH: 100,
  MIGRATION_ERROR: 500,
  WAIT_RESOURCE: 1000,
  INSTALL_PORTAL: 2000
}
