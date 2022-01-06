/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.CpuConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constants.CpuType;
import com.nctigba.ebpf.constants.FileType;
import com.nctigba.ebpf.util.OSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public void startMonitor(String taskid, String monitorType) {
        OSUtil toolUtil = new OSUtil();
        String outputUrl = urlConfig.getOutputUrl();
        String fileUrl = " > " + outputUrl + taskid + monitorType;
        String execcmd = null;
        if (CpuType.MPSTAT.equals(monitorType)) {
            execcmd = cpuConfig.getMpstat() + fileUrl + FileType.DEFAULT;
        } else if (CpuType.PIDSTAT.equals(monitorType)) {
            execcmd = cpuConfig.getPidstat() + fileUrl + FileType.DEFAULT;
        } else if (CpuType.SAR.equals(monitorType)) {
            execcmd = cpuConfig.getSar() + fileUrl + FileType.DEFAULT;
        }
        toolUtil.exec(execcmd);

    }

    /**
     * os stopMonitor
     *
     * @param monitorType stop
     */
    public void stopMonitor(String monitorType) {
        OSUtil toolUtil = new OSUtil();
        String type = monitorType.substring(0, monitorType.length() - 1);
        String param = monitorType.substring(monitorType.length() - 1);
        String execcmd = "ps -ef|grep " + type + " | grep -v grep | grep " + param + " | awk '{print $2}' | xargs kill -2";
        String pid = toolUtil.exec(execcmd).toString();
        System.out.printf(pid);
        String killcmd = "kill -2 " + pid;
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
        while (true) {
            String monitorpid = toolUtil.exec(monitorCmd).toString();
            if (!monitorpid.contains("\n" + tid + "\n")) {
                break;
            }
        }
        return true;
    }
}
