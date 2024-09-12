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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if (file.exists()) {
            stopMonitorAndSendData(file);
        } else {
            pidPath = System.getProperty("user.dir") + "/pid/" + taskId + "_delete" + FileTypeConstants.PID;
            file = new File(pidPath);
            checkStop(file, taskId);
        }
        return true;
    }

    private void checkStop(File file, String taskId) {
        List<String> typeList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                if (split.length < 3) {
                    continue;
                }
                String pid = split[0];
                String checkPid = osUtils.exec(String.format(CommonConstants.CHECK_PID, pid)).toString();
                if (checkPid != null && checkPid.contains("true")) {
                    osUtils.execCmd(String.format(CommonConstants.KILL, pid));
                    typeList.add(split[1]);
                }
            }
        } catch (IOException e) {
            log.error("error:" + e);
        }
        boolean isSend = sendFile(typeList, taskId);
        log.info("send files:" + isSend);
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
                    continue;
                }
                boolean isAllStop = stopMonitorAndSendData(file);
                if (isAllStop) {
                    boolean isDelete = file.delete();
                    log.info("start delete file:" + isDelete);
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

    @SneakyThrows
    private boolean stopMonitorAndSendData(File file) {
        HashMap<String, Object> map = getPidInfo(file);
        Object sqlTidObj = map.get("sqlTid");
        String sqlTid = "";
        if (sqlTidObj instanceof String) {
            sqlTid = (String) sqlTidObj;
        }
        boolean isStop = monitor(sqlTid);
        if (!isStop) {
            return false;
        }
        String newFileName = file.getAbsolutePath().replace(".pid", "_delete.pid");
        File newFile = new File(newFileName);
        boolean isSuccess = file.renameTo(newFile);
        log.info("modify file name:" + isSuccess);
        List<String> pidList = objToList(map.get("pidList"));
        boolean isKill = killPid(pidList);
        log.info("kill pid:" + isKill);
        String fileName = file.getName();
        String taskId = fileName.substring(0, fileName.indexOf("."));
        List<String> typeList = objToList(map.get("typeList"));
        boolean isSend = sendFile(typeList, taskId);
        log.info("send file:" + isSend);
        checkOutput(taskId);
        return true;
    }

    private HashMap<String, Object> getPidInfo(File file) {
        List<String> pidList = new ArrayList<>();
        List<String> typeList = new ArrayList<>();
        String sqlTid = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                if (split.length < 3) {
                    continue;
                }
                pidList.add(split[0]);
                typeList.add(split[1]);
                sqlTid = split[2];
            }
        } catch (IOException e) {
            log.error("error:" + e);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("pidList", pidList);
        map.put("typeList", typeList);
        map.put("sqlTid", sqlTid);
        return map;
    }

    private List<String> objToList(Object object) {
        List<String> list = new ArrayList<>();
        List<?> listObj = (List<?>) object;
        for (Object obj : listObj) {
            if (obj instanceof String) {
                list.add((String) obj);
            }
        }
        return list;
    }

    @SneakyThrows
    private boolean killPid(List<String> pidList) {
        for (String pid : pidList) {
            osUtils.execCmd(String.format(CommonConstants.KILL, pid));
        }
        TimeUnit.SECONDS.sleep(2);
        for (String pid : pidList) {
            String checkPid = osUtils.exec(String.format(CommonConstants.CHECK_PID, pid)).toString();
            if (checkPid != null && checkPid.contains("true")) {
                osUtils.execCmd(String.format(CommonConstants.KILL_9, pid));
            }
        }
        return true;
    }

    @SneakyThrows
    private boolean sendFile(List<String> typeList, String taskId) {
        for (String monitorType : typeList) {
            if (EbpfTypeConstants.PROFILE.equals(monitorType) || EbpfTypeConstants.OFFCPUTIME.equals(monitorType)
                    || EbpfTypeConstants.MEMLEAK.equals(monitorType)) {
                TimeUnit.SECONDS.sleep(2);
                sendFileHandler.createSvg(taskId, monitorType);
            }
            if (OsTypeConstants.SAR.equals(monitorType)) {
                sendFileHandler.sendFile(taskId, "cpuCoreNum");
            }
            sendFileHandler.sendFile(taskId, monitorType);
        }
        return true;
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
