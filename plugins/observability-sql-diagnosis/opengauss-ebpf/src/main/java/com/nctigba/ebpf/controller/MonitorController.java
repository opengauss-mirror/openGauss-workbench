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
 *  EbpfMonitorController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/controller/EbpfMonitorController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.controller;

import com.nctigba.ebpf.service.EbpfMonitorService;
import com.nctigba.ebpf.service.MonitorService;
import com.nctigba.ebpf.service.OsMonitorService;
import com.nctigba.ebpf.service.ParamMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Monitor controller
 *
 * @author luomeng
 * @since 2022/10/17 09:00
 */
@RestController
@RequestMapping("/monitor/v1")
@Slf4j
public class MonitorController {
    @Autowired
    MonitorService monitorService;
    @Autowired
    EbpfMonitorService ebpfMonitorService;
    @Autowired
    OsMonitorService osMonitorService;
    @Autowired
    ParamMonitorService paramMonitorService;

    @Value(value = "${project.version}")
    private String version;

    /**
     * Start monitor
     *
     * @param tid         String
     * @param taskId      String
     * @param monitorType String
     */
    @PostMapping("/startMonitor")
    public void startMonitor(String tid, String taskId, String monitorType) {
        boolean isEmptyParam = tid == null || taskId == null || monitorType == null;
        if (isEmptyParam) {
            log.error("Parameter cannot be empty");
        }
        monitorService.startMonitor(tid, taskId, monitorType);
    }

    /**
     * Stop monitor
     *
     * @param taskId String
     * @return boolean
     */
    @PostMapping("/stopMonitor")
    public boolean stopMonitor(String taskId) {
        return monitorService.stopMonitor(taskId);
    }

    /**
     * Agent status monitor
     *
     * @param taskId String
     * @return boolean
     */
    @PostMapping("/statusMonitor")
    public boolean statusMonitor(String taskId) {
        return monitorService.statusMonitor(taskId);
    }

    /**
     * Get agent version
     *
     * @return String
     */
    @GetMapping("/version")
    public String getVersion() {
        return version;
    }

    /**
     * Get agent status
     *
     * @return AjaxResult
     */
    @GetMapping("/status")
    public String getStatus() {
        return "success";
    }

    /**
     * Create a record
     *
     * @param taskId String
     */
    @PostMapping("/record")
    public void record(String taskId) {
        monitorService.record(taskId);
    }
}
