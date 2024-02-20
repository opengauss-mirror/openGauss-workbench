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
 *  MonitroServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/service/impl/MonitroServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.constant.CommonConstants;
import com.nctigba.ebpf.constant.EbpfTypeConstants;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.constant.OsTypeConstants;
import com.nctigba.ebpf.enums.OsTypeEnum;
import com.nctigba.ebpf.handler.EbpfSendFileHandler;
import com.nctigba.ebpf.service.EbpfMonitorService;
import com.nctigba.ebpf.service.MonitorService;
import com.nctigba.ebpf.service.OsMonitorService;
import com.nctigba.ebpf.service.ParamMonitorService;
import com.nctigba.ebpf.util.OSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * MonitorServiceImpl
 *
 * @author luomeng
 * @since 2024/2/22
 */
@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {
    private static final long MONITOR_CYCLE = 1L;
    private static final long TIMEOUT = 60L;

    @Autowired
    OSUtils osUtils;
    @Autowired
    EbpfSendFileHandler sendFileHandler;
    @Autowired
    EbpfMonitorService ebpfMonitorService;
    @Autowired
    OsMonitorService osMonitorService;
    @Autowired
    ParamMonitorService paramMonitorService;

    @Override
    public void startMonitor(String tid, String taskId, String monitorType) {
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

    @Override
    public boolean stopMonitor(String taskId) {
        log.info("Stop monitor start:");
        String pidPath = System.getProperty("user.dir") + "/pid/" + taskId + FileTypeConstants.PID;
        File file = new File(pidPath);
        if (!file.exists()) {
            return true;
        }
        return stopMonitorAndSendData(file);
    }

    @Override
    public boolean statusMonitor(String taskId) {
        String pidPath = System.getProperty("user.dir") + "/pid/";
        File folder = new File(pidPath);
        if (!folder.exists()) {
            return true;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(FileTypeConstants.PID));
        if (files == null) {
            return true;
        }
        for (File file : files) {
            boolean isDelete = killPid(file, taskId);
            if (!isDelete) {
                return false;
            }
        }
        return true;
    }

    private boolean killPid(File file, String taskId) {
        if (!file.getName().contains(taskId) || file.length() == 0) {
            return true;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                String checkPid = osUtils.exec(String.format(CommonConstants.CHECK_PID, split[0])).toString();
                if (checkPid != null && checkPid.contains("true")) {
                    osUtils.execCmd(String.format(CommonConstants.KILL_9, split[0]));
                }
            }
        } catch (IOException e) {
            return false;
        }
        if (file.exists()) {
            boolean isSuccess = file.delete();
            log.info("delete file:" + isSuccess);
        }
        return true;
    }

    @Override
    public void record(String taskId) {
        String pidPath = System.getProperty("user.dir") + "/pid/";
        File dir = new File(pidPath);
        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            log.info("mkdir file:" + isSuccess);
        }
        String filePath = pidPath + taskId + FileTypeConstants.PID;
        File file = new File(filePath);
        try {
            boolean isSuccess = file.createNewFile();
            log.info("create file:" + isSuccess);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Monitoring, Interrupting, and Sending
     */
    @Scheduled(fixedDelay = MONITOR_CYCLE, timeUnit = TimeUnit.SECONDS)
    public void monitorPid() {
        String pidPath = System.getProperty("user.dir") + "/pid/";
        File folder = new File(pidPath);
        if (!folder.exists()) {
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(FileTypeConstants.PID));
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains("delete") || file.length() == 0) {
                    return;
                }
                boolean isAllStop = stopMonitorAndSendData(file);
                log.info("pid is kill:" + isAllStop);
                if (isAllStop) {
                    try {
                        log.info(file.getCanonicalPath());
                        osUtils.execCmd("rm -f " + file.getCanonicalPath());
                    } catch (IOException e) {
                        log.error("Delete file failed:" + e.getMessage());
                    }
                }
            }
        }
    }

    private boolean monitor(String tid) {
        OSUtils toolUtil = new OSUtils();
        long startTime = System.currentTimeMillis();
        while (true) {
            String monitorPid = toolUtil.exec(CommonConstants.DB_MONITOR).toString();
            long endTime = System.currentTimeMillis();
            if ((endTime - startTime) / 1000 / 60 > TIMEOUT) {
                break;
            }
            if (!monitorPid.contains(System.lineSeparator() + tid + System.lineSeparator())) {
                break;
            }
        }
        return true;
    }

    private boolean stopMonitorAndSendData(File file) {
        String fileName = file.getName();
        String taskId = fileName.substring(0, fileName.indexOf("."));
        int totalCount = 0;
        int stopCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFinish = false;
            while ((line = reader.readLine()) != null) {
                totalCount++;
                String[] split = line.split(",");
                if (!isFinish && monitor(split[2])) {
                    isFinish = true;
                    String newFileName = file.getAbsolutePath().replace(".pid", "_delete.pid");
                    File newFile = new File(newFileName);
                    boolean isSuccess = file.renameTo(newFile);
                    log.info("modify file name:" + isSuccess);
                }
                String checkPid = osUtils.exec(String.format(CommonConstants.CHECK_PID, split[0])).toString();
                if (checkPid != null && checkPid.contains("false")) {
                    stopCount++;
                    continue;
                }
                osUtils.execCmd(String.format(CommonConstants.KILL, split[0]));
                if (EbpfTypeConstants.PROFILE.equals(split[1]) || EbpfTypeConstants.OFFCPUTIME.equals(
                        split[1])
                        || EbpfTypeConstants.MEMLEAK.equals(split[1])) {
                    TimeUnit.SECONDS.sleep(2);
                    sendFileHandler.createSvg(taskId, split[1]);
                }
                if (OsTypeConstants.SAR.equals(split[1])) {
                    sendFileHandler.sendFile(taskId, "cpuCoreNum");
                }
                sendFileHandler.sendFile(taskId, split[1]);
                checkPid = osUtils.exec(String.format(CommonConstants.CHECK_PID, split[0])).toString();
                if (checkPid != null && checkPid.contains("false")) {
                    stopCount++;
                }
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        checkOutput(taskId);
        log.info("totalCount:" + totalCount + "/" + "stopCount:" + stopCount);
        return totalCount == stopCount;
    }

    private void checkOutput(String taskId) {
        String outputPath = System.getProperty("user.dir") + "/output/";
        File outputFolder = new File(outputPath);
        File[] outputFiles = outputFolder.listFiles((dir, name) -> name.startsWith(taskId));
        if (outputFiles != null) {
            for (File file : outputFiles) {
                String name = file.getName().replace(taskId, "");
                String diagnosisType = name.substring(0, name.indexOf("."));
                sendFileHandler.sendFile(taskId, diagnosisType);
            }
        }
    }
}
