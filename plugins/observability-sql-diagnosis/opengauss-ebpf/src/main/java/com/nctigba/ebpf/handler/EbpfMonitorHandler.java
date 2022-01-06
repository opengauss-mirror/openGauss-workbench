/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.EbpfConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constants.EbpfType;
import com.nctigba.ebpf.constants.FileType;
import com.nctigba.ebpf.util.OSUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ebpf action
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Component
public class EbpfMonitorHandler {

    @Autowired
    EbpfConfig ebpfConfig;
    @Autowired
    UrlConfig urlConfig;

    /**
     * ebpf startMonitor
     *
     * @param tid taskid monitorType
     */
    public void startMonitor(String tid, String taskid, String monitorType) {
        OSUtil toolUtil = new OSUtil();
        String bccUrl = "cd " + urlConfig.getBccUrl() + " &&";
        String outputUrl = urlConfig.getOutputUrl();
        String fileUrl = " > " + outputUrl + taskid + monitorType;
        String execcmd = null;
        if (EbpfType.PROFILE.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getProfile() + " " + tid + fileUrl + FileType.STACKS;
        } else if (EbpfType.OFFCPUTIME.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getOffcputime() + " " + tid + fileUrl + FileType.STACKS;
        } else if (EbpfType.RUNQLEN.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getRunqlen() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.RUNQLAT.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getRunqlat() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.DEFAULT.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getStackcount() + " " + tid + " c:malloc" + fileUrl + FileType.STACKS;
        } else if (EbpfType.CACHESTAT.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getCachestat() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.EXT4SLOWER.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getExt4slower() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.EXT4DIST.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getExt4dist() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.BIOLATENCY.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getBiolatency() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.BIOSNOOP.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getBiosnoop() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.FILETOP.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getFiletop() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.TCPLIFE.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getTcplife() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.TCPTOP.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getTcptop() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.XFSDIST.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getXfsdist() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.XFSSLOWER.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getXfsslower() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.MEMLEAK.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getMemleak() + " " + " $(pidof allocs)" + fileUrl + FileType.DEFAULT;
        }
        toolUtil.exec(execcmd);

    }


    /**
     * ebpf stopMonitor
     *
     * @param monitorType monitor ebpf type
     */
    public void stopMonitor(String monitorType) {
        OSUtil toolUtil = new OSUtil();
        String execcmd = "ps -ef | grep ./" + monitorType + " | grep -v grep | grep python3 | awk '{print $2}';";
        String pid = toolUtil.exec(execcmd).toString();
        String killcmd = "kill -2 " + pid;
        toolUtil.exec(killcmd);
    }


    /**
     * ebpf monitor
     *
     * @param tid monitor tid
     */
    public boolean monitor(String tid) {
        OSUtil toolUtil = new OSUtil();
        String monitorCmd = "ps -eLf |grep gaussdb|grep -v grep|awk '{print $4}'";
        while (true) {
            String monitorpid = toolUtil.exec(monitorCmd).toString();
            if (!monitorpid.contains(System.lineSeparator() + tid + System.lineSeparator())) {
                break;
            }
        }
        return true;
    }


}
