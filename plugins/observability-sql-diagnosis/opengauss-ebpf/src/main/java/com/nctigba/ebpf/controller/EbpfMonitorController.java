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

import com.nctigba.ebpf.enums.OsTypeEnum;
import com.nctigba.ebpf.service.EbpfMonitorService;
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
    @Autowired
    ParamMonitorService paramMonitorService;

    @Value(value = "${project.version}")
    private String version;

    @PostMapping("/ebpfMonitor")
    public void ebpfMonitor(String tid, String taskId, String monitorType) {
        log.info(tid + ":" + taskId + ":" + monitorType);
        boolean isEmptyParam = tid == null || taskId == null || monitorType == null;
        if (isEmptyParam) {
            log.info("Parameter cannot be empty");
        }
        boolean isExist = false;
        for (OsTypeEnum type : OsTypeEnum.values()) {
            if (type.getType().equals(monitorType)) {
                isExist = true;
                break;
            }
        }
        if (isExist) {
            osMonitorService.getCpuMonitorData(tid, taskId, monitorType);
        } else if ("osParam".equals(monitorType) || "cpuCoreNum".equals(monitorType)) {
            paramMonitorService.getOsParamData(tid, taskId, monitorType);
        } else {
            ebpfMonitorService.ebpfMonitor(tid, taskId, monitorType);
        }
    }

    @GetMapping("/version")
    public String getVersion() {
        return version;
    }
}
