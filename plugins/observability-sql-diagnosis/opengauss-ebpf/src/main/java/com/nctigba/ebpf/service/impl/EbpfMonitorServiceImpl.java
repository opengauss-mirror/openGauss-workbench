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

import com.nctigba.ebpf.config.EbpfConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constant.EbpfTypeConstants;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.enums.EbpfTypeEnum;
import com.nctigba.ebpf.handler.EbpfSendFileHandler;
import com.nctigba.ebpf.service.EbpfMonitorService;
import com.nctigba.ebpf.util.OSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;

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
    private EbpfSendFileHandler sendFileHandler;
    @Autowired
    private UrlConfig urlConfig;
    @Autowired
    private EbpfConfig ebpfConfig;
    @Autowired
    private OSUtils osUtils;

    @Override
    public void ebpfMonitor(String tid, String taskId, String monitorType) {
        String fileName = System.getProperty("user.dir") + "/pid/" + taskId + ".pid";
        File file = new File(fileName);
        if (!file.exists()) {
            log.error("pid file is not exists!");
            return;
        }
        startMonitor(tid, taskId, monitorType);
    }

    private void startMonitor(String tid, String taskId, String monitorType) {
        String outputPath = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputPath);
        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            log.info("mkdir file:" + isSuccess);
        }
        String filePath = " > " + outputPath + taskId + monitorType;
        String bccPath = "set -m; cd " + urlConfig.getBccUrl() + ";";
        String pidPath = System.getProperty("user.dir") + "/pid/" + taskId + ".pid";
        String pidFile = " 2>&1 &  echo $!," + monitorType + "," + tid + " >> " + pidPath;
        HashMap<String, String> commandMap = new HashMap<>();
        for (EbpfTypeEnum type : EbpfTypeEnum.values()) {
            if (!type.getType().equals(monitorType)) {
                continue;
            }
            switch (type.getType()) {
                case EbpfTypeConstants.PROFILE:
                case EbpfTypeConstants.OFFCPUTIME:
                case EbpfTypeConstants.STACKCOUNT:
                case EbpfTypeConstants.MEMLEAK:
                    commandMap.put(
                            type.getType(),
                            bccPath + String.format(type.getCommand(), tid) + filePath + FileTypeConstants.STACKS
                                    + pidFile);
                    break;
                default:
                    commandMap.put(
                            type.getType(),
                            bccPath + String.format(type.getCommand(), tid) + filePath + FileTypeConstants.DEFAULT
                                    + pidFile);
                    break;
            }
        }
        String execCmd = commandMap.get(monitorType);
        log.info(execCmd);
        osUtils.execCmd(execCmd);
    }
}
