/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.EbpfConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constants.CommonConstants;
import com.nctigba.ebpf.constants.EbpfType;
import com.nctigba.ebpf.constants.FileType;
import com.nctigba.ebpf.util.OSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

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
    public String startMonitor(String tid, String taskid, String monitorType) {
        OSUtil toolUtil = new OSUtil();
        String bccUrl = "cd " + urlConfig.getBccUrl() + " &&";
        String outputUrl = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileUrl = " > " + outputUrl + taskid + monitorType;
        String execcmd = null;
        if (EbpfType.PROFILE.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getProfile() + CommonConstants.BLANK + tid + fileUrl + FileType.STACKS;
        } else if (EbpfType.OFFCPUTIME.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getOffcputime() + CommonConstants.BLANK + tid + fileUrl + FileType.STACKS;
        } else if (EbpfType.RUNQLEN.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getRunqlen() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.RUNQLAT.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getRunqlat() + fileUrl + FileType.DEFAULT;
        } else if (EbpfType.DEFAULT.equals(monitorType)) {
            execcmd = bccUrl + ebpfConfig.getStackcount() + CommonConstants.BLANK + tid + " c:malloc" + fileUrl + FileType.STACKS;
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
            execcmd = bccUrl + ebpfConfig.getMemleak() + CommonConstants.BLANK + " $(pidof allocs)" + fileUrl + FileType.STACKS;
        }
        return toolUtil.execCmd(execcmd);
    }


    /**
     * ebpf stopMonitor
     *
     * @param monitorType monitor ebpf type
     */
    public void stopMonitor(String monitorType, String pid) {
        OSUtil toolUtil = new OSUtil();
        String execcmd = "ps -ef | grep ./" + monitorType + " | grep -v grep | grep python3 | awk '{print $2,$3}';";
        String pids = toolUtil.exec(execcmd).toString();
        String[] pidss = pids.split(System.lineSeparator());
        String tid = null;
        for (int i = 0; i < pidss.length; i++) {
            if (pidss[i].contains(pid)) {
                tid = pidss[i].substring(0, pidss[i].indexOf(CommonConstants.BLANK));
            }
        }
        String killcmd = "kill -2 " + tid;
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
        long startTime = System.currentTimeMillis();
        while (true) {
            String monitorpid = toolUtil.exec(monitorCmd).toString();
            long endTime = System.currentTimeMillis();
            if((endTime-startTime)/1000/60>60){
                break;
            }
            if (!monitorpid.contains(System.lineSeparator() + tid + System.lineSeparator())) {
                break;
            }
        }
        return true;
    }


}
