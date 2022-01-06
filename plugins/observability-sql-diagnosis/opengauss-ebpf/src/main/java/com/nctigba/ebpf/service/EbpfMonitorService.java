/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.service;

/**
 * ebpf service
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
public interface EbpfMonitorService {

    void ebpfMonitor(String tid, String taskid, String monitorType);
}
