/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.handler.EbpfSendFileHandler;
import com.nctigba.ebpf.handler.OsMonitorHandler;
import com.nctigba.ebpf.service.OsMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * os service impl
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/12/06 11:31
 */
@Service
@Slf4j
@Async("ebpfPool")
public class OsMonitorServiceImpl implements OsMonitorService {

    @Autowired
    private OsMonitorHandler monitorHandler;

    @Autowired
    private EbpfSendFileHandler sendFileHandler;

    @Override
    public void getCpuMonitorData(String tid, String taskid, String monitorType) {
        try {
            String monitorPid = monitorHandler.startMonitor(taskid, monitorType);
            boolean isTrue = monitorHandler.monitor(tid);
            if (isTrue) {
                monitorHandler.stopMonitor(monitorType, monitorPid);
            }
            sendFileHandler.sendFile(taskid, monitorType);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
