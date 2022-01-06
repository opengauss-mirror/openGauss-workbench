/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.handler.EbpfSendFileHandler;
import com.nctigba.ebpf.handler.OsMonitorHandler;
import com.nctigba.ebpf.service.OsMonitorService;
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
public class OsMonitorServiceImpl implements OsMonitorService {

    @Autowired
    private OsMonitorHandler monitorHandler;

    @Autowired
    private EbpfSendFileHandler sendFileHandler;

    @Override
    public void getCpuMonitorData(String tid, String taskid, String monitorType) {
        try {
            Runnable oneRunnable = () -> monitorHandler.startMonitor(taskid, monitorType);
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
            sendFileHandler.sendFile(taskid, monitorType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
