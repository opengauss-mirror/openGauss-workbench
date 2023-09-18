/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.constants.EbpfType;
import com.nctigba.ebpf.handler.EbpfMonitorHandler;
import com.nctigba.ebpf.handler.EbpfSendFileHandler;
import com.nctigba.ebpf.service.EbpfMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * ebpf service impl
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Service
@Slf4j
@Async("ebpfPool")
public class EbpfMonitorServiceImpl implements EbpfMonitorService {

    @Autowired
    private EbpfMonitorHandler monitorHandler;

    @Autowired
    private EbpfSendFileHandler sendFileHandler;

    @Override
    public void ebpfMonitor(String tid, String taskid, String monitorType) {
        try {
            String monitorPid = monitorHandler.startMonitor(tid, taskid, monitorType);
            boolean isTrue = monitorHandler.monitor(tid);
            if (isTrue) {
                monitorHandler.stopMonitor(monitorType, monitorPid);
            }
            TimeUnit.SECONDS.sleep(1);
            if (EbpfType.PROFILE.equals(monitorType) || EbpfType.OFFCPUTIME.equals(monitorType) || EbpfType.MEMLEAK.equals(monitorType)) {
                sendFileHandler.createSvg(taskid, monitorType);
            }
            sendFileHandler.sendFile(taskid, monitorType);
        }  catch (InterruptedException e) {
            log.info(e.getMessage());
            log.error("Interrupted!", e);
            Thread.currentThread().interrupt();
        }catch (IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }

}
