/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.CpuConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constants.CommonConstants;
import com.nctigba.ebpf.constants.CpuType;
import com.nctigba.ebpf.constants.FileType;
import com.nctigba.ebpf.util.OSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * os action
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/12/06 14:15
 */
@Component
public class OsMonitorHandler {

    @Autowired
    CpuConfig cpuConfig;
    @Autowired
    UrlConfig urlConfig;

    /**
     * os startMonitor
     *
     * @param taskid monitorType
     */
    public String startMonitor(String taskid, String monitorType) {
        OSUtil toolUtil = new OSUtil();
        String outputUrl = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileUrl = " > " + outputUrl + taskid + monitorType;
        String execcmd = null;
        if (CpuType.MPSTAT.equals(monitorType)) {
            execcmd = cpuConfig.getMpstat() + fileUrl + FileType.DEFAULT;
        } else if (CpuType.PIDSTAT.equals(monitorType)) {
            execcmd = cpuConfig.getPidstat() + fileUrl + FileType.DEFAULT;
        } else if (CpuType.SAR.equals(monitorType)) {
            execcmd = cpuConfig.getSar() + fileUrl + FileType.DEFAULT;
        }
        return toolUtil.execCmd(execcmd);

    }

    /**
     * os stopMonitor
     *
     * @param monitorType stop
     */
    public void stopMonitor(String monitorType, String pid) {
        OSUtil toolUtil = new OSUtil();
        String type = monitorType.substring(0, monitorType.length() - 1);
        String param = monitorType.substring(monitorType.length() - 1);
        String execcmd = "ps -ef|grep " + type + " | grep -v grep | grep " + param + " | awk '{print $2,$3}'";
        String pids = toolUtil.exec(execcmd).toString();
        String[] pidss = pids.split(System.lineSeparator());
        String tid = null;
        for (int i = 0; i < pidss.length; i++) {
            if (pidss[i].contains(pid) && pid != null && !pid.equals(pidss[i].substring(0, pidss[i].indexOf(CommonConstants.BLANK)))) {
                tid = pidss[i].substring(0, pidss[i].indexOf(CommonConstants.BLANK));
            }
        }
        String killcmd = "kill -2 " + tid;
        toolUtil.exec(killcmd);
    }

    /**
     * os stopMonitor
     *
     * @param tid monitor tid
     */
    public boolean monitor(String tid) {
        OSUtil toolUtil = new OSUtil();
        String monitorCmd = "ps -eLf |grep gaussdb|grep -v grep|awk '{print $4}'";
        long startTime = System.currentTimeMillis();
        while (true) {
            String monitorpid = toolUtil.exec(monitorCmd).toString();
            long endTime = System.currentTimeMillis();
            if ((endTime - startTime) / 1000 / 60 > 60) {
                break;
            }
            if (!monitorpid.contains(System.lineSeparator() + tid + System.lineSeparator())) {
                break;
            }
        }
        return true;
    }
}
