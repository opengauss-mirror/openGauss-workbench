/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.constants.EbpfType;
import com.nctigba.ebpf.handler.EbpfMonitorHandler;
import com.nctigba.ebpf.handler.EbpfSendFileHandler;
import com.nctigba.ebpf.service.EbpfMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ebpf service impl
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Service
@Slf4j
public class EbpfMonitorServiceImpl implements EbpfMonitorService {

    @Autowired
    private EbpfMonitorHandler monitorHandler;

    @Autowired
    private EbpfSendFileHandler sendFileHandler;

    @Override
    public void ebpfMonitor(String tid, String taskid, String monitorType) {
        try {
            Runnable oneRunnable = () -> monitorHandler.startMonitor(tid, taskid, monitorType);
            Thread oneThread = new Thread(oneRunnable);
            oneThread.setUncaughtExceptionHandler((tr, ex) -> System.out.println(tr.getName() + " : " + ex.getMessage()));
            oneThread.start();
            Runnable twoRunnable = () -> {
                boolean isTrue = monitorHandler.monitor(tid);
                if (isTrue) {
                    monitorHandler.stopMonitor(monitorType);
                }
            };
            Thread twoThread = new Thread(twoRunnable);
            twoThread.setUncaughtExceptionHandler((tr, ex) -> System.out.println(tr.getName() + " : " + ex.getMessage()));
            twoThread.start();
            if (oneThread.isAlive() || twoThread.isAlive()) {
                oneThread.join();
                twoThread.join();
            }
            if (EbpfType.PROFILE.equals(monitorType) || EbpfType.OFFCPUTIME.equals(monitorType) || EbpfType.MEMLEAK.equals(monitorType)) {
                sendFileHandler.createSvg(taskid, monitorType);
            }
            sendFileHandler.sendFile(taskid, monitorType);
        } catch (IllegalArgumentException | InterruptedException e) {
            log.info(e.getMessage());
        }
    }

}
