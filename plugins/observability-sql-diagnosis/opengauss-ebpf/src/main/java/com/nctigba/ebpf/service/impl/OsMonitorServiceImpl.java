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

import com.nctigba.ebpf.config.OsConfig;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.constant.OsTypeConstants;
import com.nctigba.ebpf.enums.OsTypeEnum;
import com.nctigba.ebpf.handler.EbpfSendFileHandler;
import com.nctigba.ebpf.service.OsMonitorService;
import com.nctigba.ebpf.util.OSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;

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
    private EbpfSendFileHandler sendFileHandler;
    @Autowired
    private OsConfig osConfig;
    @Autowired
    private OSUtils osUtils;

    @Override
    public void getCpuMonitorData(String tid, String taskId, String monitorType) {
        log.info("OsMonitorServiceImpl" + tid + ":" + taskId + ":" + monitorType);
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
        String writeType = " > ";
        String fileName = outputPath + taskId + monitorType;
        String fileUrl = writeType + fileName + FileTypeConstants.DEFAULT;
        String endTime =
                "set -m; echo time:$(date '+%Y-%m-%d %H:%M:%S') > " + fileName + FileTypeConstants.DEFAULT;
        String pidPath = System.getProperty("user.dir") + "/pid/" + taskId + ".pid";
        String pidFile = " 2>&1 &  echo $!," + monitorType + "," + tid + " >> " + pidPath;
        HashMap<String, String> commandMap = new HashMap<>();
        for (OsTypeEnum type : OsTypeEnum.values()) {
            if (!type.getType().equals(monitorType)) {
                continue;
            }
            switch (type.getType()) {
                case OsTypeConstants.MPSTAT:
                case OsTypeConstants.PIDSTAT:
                case OsTypeConstants.SAR:
                case OsTypeConstants.SAR_D:
                    commandMap.put(
                            type.getType(), type.getCommand() + fileUrl + pidFile);
                    break;
                default:
                    commandMap.put(
                            type.getType(),
                            endTime + " ; " + type.getCommand() + " >> " + fileName + FileTypeConstants.DEFAULT
                                    + pidFile);
            }
        }
        log.info(commandMap.get(monitorType));
        osUtils.execCmd(commandMap.get(monitorType));
    }
}
