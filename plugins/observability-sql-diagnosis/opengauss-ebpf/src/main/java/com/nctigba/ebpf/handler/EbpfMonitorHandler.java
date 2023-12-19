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
 *  EbpfMonitorHandler.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/handler/EbpfMonitorHandler.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.EbpfConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constant.CommonConstants;
import com.nctigba.ebpf.constant.EbpfTypeConstants;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.util.OSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;

/**
 * ebpf action
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Component
public class EbpfMonitorHandler {
    private static final long TIMEOUT = 60L;

    @Autowired
    UrlConfig urlConfig;

    @Autowired
    EbpfConfig ebpfConfig;

    /**
     * Ebpf start monitor
     *
     * @param tid Sql tid
     * @param taskId Diagnosis task id
     * @param monitorType Diagnosis type
     * @return String
     */
    public String startMonitor(String tid, String taskId, String monitorType) {
        OSUtils toolUtil = new OSUtils();
        String bccUrl = "cd " + urlConfig.getBccUrl() + " &&";
        String outputUrl = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileUrl = " > " + outputUrl + taskId + monitorType;
        HashMap<String, String> commandMap = new HashMap<>();
        commandMap.put(
                EbpfTypeConstants.PROFILE,
                bccUrl + ebpfConfig.getProfile() + CommonConstants.BLANK + tid + fileUrl + FileTypeConstants.STACKS);
        commandMap.put(
                EbpfTypeConstants.OFFCPUTIME,
                bccUrl + ebpfConfig.getOffcputime() + CommonConstants.BLANK + tid + fileUrl + FileTypeConstants.STACKS);
        commandMap.put(
                EbpfTypeConstants.RUNQLEN, bccUrl + ebpfConfig.getRunqlen() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.RUNQLAT, bccUrl + ebpfConfig.getRunqlat() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.DEFAULT,
                bccUrl + ebpfConfig.getStackcount() + CommonConstants.BLANK + tid + " c:malloc" + fileUrl
                        + FileTypeConstants.STACKS);
        commandMap.put(
                EbpfTypeConstants.CACHESTAT, bccUrl + ebpfConfig.getCachestat() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.EXT4SLOWER,
                bccUrl + ebpfConfig.getExt4slower() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.EXT4DIST, bccUrl + ebpfConfig.getExt4dist() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.BIOLATENCY,
                bccUrl + ebpfConfig.getBiolatency() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.BIOSNOOP, bccUrl + ebpfConfig.getBiosnoop() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.FILETOP, bccUrl + ebpfConfig.getFiletop() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.TCPLIFE, bccUrl + ebpfConfig.getTcplife() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(EbpfTypeConstants.TCPTOP, bccUrl + ebpfConfig.getTcptop() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.XFSDIST, bccUrl + ebpfConfig.getXfsdist() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.XFSSLOWER, bccUrl + ebpfConfig.getXfsslower() + fileUrl + FileTypeConstants.DEFAULT);
        commandMap.put(
                EbpfTypeConstants.MEMLEAK,
                bccUrl + ebpfConfig.getMemleak() + CommonConstants.BLANK + " $(pidof allocs)" + fileUrl
                        + FileTypeConstants.STACKS);
        String execCmd = commandMap.get(monitorType);
        return toolUtil.execCmd(execCmd);
    }

    /**
     * Ebpf stop monitor
     *
     * @param pid Sql tid
     * @param monitorType Diagnosis type
     */
    public void stopMonitor(String monitorType, String pid) {
        OSUtils toolUtil = new OSUtils();
        String execCmd = String.format(CommonConstants.STOP_EBPF, monitorType);
        String pidData = toolUtil.exec(execCmd).toString();
        String[] pidList = pidData.split(System.lineSeparator());
        String tid = null;
        for (String s : pidList) {
            if (s.contains(pid)) {
                tid = s.substring(0, s.indexOf(CommonConstants.BLANK));
            }
        }
        toolUtil.exec(String.format(CommonConstants.KILL, tid));
    }


    /**
     * Database tid monitor
     *
     * @param tid monitor tid
     * @return boolean
     */
    public boolean monitor(String tid) {
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
}
