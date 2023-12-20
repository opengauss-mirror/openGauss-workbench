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
 *  OsMonitorServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/service/impl/OsMonitorServiceImpl.java
 *
 *  -------------------------------------------------------------------------
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
    public void getCpuMonitorData(String tid, String taskId, String monitorType) {
        log.info("OsMonitorServiceImpl" + tid + ":" + taskId + ":" + monitorType);
        try {
            String monitorPid = monitorHandler.startMonitor(taskId, monitorType);
            boolean isTrue = monitorHandler.monitor(tid);
            if (isTrue) {
                monitorHandler.stopMonitor(monitorType, monitorPid);
            }
            sendFileHandler.sendFile(taskId, monitorType);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
