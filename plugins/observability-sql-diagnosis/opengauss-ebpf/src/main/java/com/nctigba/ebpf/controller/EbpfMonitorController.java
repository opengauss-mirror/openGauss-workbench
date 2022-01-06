/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.controller;

import com.nctigba.ebpf.constants.CpuType;
import com.nctigba.ebpf.service.EbpfMonitorService;
import com.nctigba.ebpf.service.OsMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ebpf controller
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@RestController
@RequestMapping("/ebpf/v1")
@Slf4j
public class EbpfMonitorController {

    @Autowired
    EbpfMonitorService ebpfMonitorService;
    @Autowired
    OsMonitorService osMonitorService;

    @PostMapping("/ebpfMonitor")
    @Async("ebpfPool")
    public void ebpfMonitor(String tid, String taskid, String monitorType) {
        log.info(tid + ":" + taskid + ":" + monitorType);
        boolean isEmptyParam = tid == null || taskid == null || monitorType == null;
        if (isEmptyParam) {
            log.info("Parameter cannot be empty");
        }
        if (CpuType.SAR.equals(monitorType) || CpuType.PIDSTAT.equals(monitorType) || CpuType.MPSTAT.equals(monitorType)) {
            osMonitorService.getCpuMonitorData(tid, taskid, monitorType);
        } else {
            ebpfMonitorService.ebpfMonitor(tid, taskid, monitorType);
        }

    }

}
