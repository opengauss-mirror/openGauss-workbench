/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.service;

/**
 * os service
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/12/06 11:30
 */
public interface OsMonitorService {
    void getCpuMonitorData(String tid, String taskid, String monitorType);
}
