/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  EbpfMonitorServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/service/impl/EbpfMonitorServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.constant.EbpfTypeConstants;
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
    public void ebpfMonitor(String tid, String taskId, String monitorType) {
        try {
            String monitorPid = monitorHandler.startMonitor(tid, taskId, monitorType);
            boolean isTrue = monitorHandler.monitor(tid);
            if (isTrue) {
                monitorHandler.stopMonitor(monitorType, monitorPid);
            }
            TimeUnit.SECONDS.sleep(1);
            if (EbpfTypeConstants.PROFILE.equals(monitorType) || EbpfTypeConstants.OFFCPUTIME.equals(monitorType)
                    || EbpfTypeConstants.MEMLEAK.equals(monitorType)) {
                sendFileHandler.createSvg(taskId, monitorType);
            }
            sendFileHandler.sendFile(taskId, monitorType);
        } catch (InterruptedException e) {
            log.info(e.getMessage());
            log.error("Interrupted!", e);
            Thread.currentThread().interrupt();
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
        }
    }
}
