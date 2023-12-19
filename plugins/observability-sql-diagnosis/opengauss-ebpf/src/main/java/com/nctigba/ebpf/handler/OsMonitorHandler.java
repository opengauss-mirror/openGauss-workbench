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
 *  OsMonitorHandler.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/handler/OsMonitorHandler.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.handler;

import com.nctigba.ebpf.config.OsConfig;
import com.nctigba.ebpf.constant.CommonConstants;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.constant.OsTypeConstants;
import com.nctigba.ebpf.util.OSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * os action
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/12/06 14:15
 */
@Slf4j
@Component
public class OsMonitorHandler {
    private static final long TIMEOUT = 60L;

    @Autowired
    OsConfig osConfig;

    /**
     * Os startMonitor
     *
     * @param taskId Diagnosis task id
     * @param monitorType Diagnosis type
     * @return String
     */
    public String startMonitor(String taskId, String monitorType) {
        OSUtils toolUtil = new OSUtils();
        String outputUrl = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String writeType = " > ";
        String fileName = outputUrl + taskId + monitorType;
        String fileUrl = writeType + fileName + FileTypeConstants.DEFAULT;
        String endTime =
                "echo time:$(date '+%Y-%m-%d %H:%M:%S') > " + fileName + FileTypeConstants.DEFAULT;
        String execcmd = null;
        if (OsTypeConstants.MPSTAT.equals(monitorType)) {
            execcmd = osConfig.getMpstat() + fileUrl;
        } else if (OsTypeConstants.PIDSTAT.equals(monitorType)) {
            execcmd = osConfig.getPidstat() + fileUrl;
        } else if (OsTypeConstants.SAR.equals(monitorType)) {
            execcmd = osConfig.getSar() + fileUrl;
        } else if (OsTypeConstants.VM_STAT.equals(monitorType)) {
            execcmd = endTime + " && " + osConfig.getVmstat1() + " >> " + fileName + FileTypeConstants.DEFAULT;
        } else if (OsTypeConstants.CPU_AVG_LOAD.equals(monitorType)) {
            execcmd = endTime + " && " + osConfig.getCpuAvgLoad() + " >> " + fileName + FileTypeConstants.DEFAULT;
        } else if (OsTypeConstants.IO_STAT.equals(monitorType)) {
            execcmd = endTime + " && " + osConfig.getIostatX1() + " >> " + fileName + FileTypeConstants.DEFAULT;
        } else if (OsTypeConstants.SAR_D.equals(monitorType)) {
            execcmd = osConfig.getSarD() + fileUrl;
        }
        return toolUtil.execCmd(execcmd);
    }

    /**
     * Os stop monitor
     *
     * @param pid Sql tid
     * @param monitorType Diagnosis type
     */
    public void stopMonitor(String monitorType, String pid) {
        log.info("OsMonitorHandler" + pid + ":" + monitorType);
        OSUtils toolUtil = new OSUtils();
        String type = monitorType.substring(0, monitorType.length() - 1);
        String param = monitorType.substring(monitorType.length() - 1);
        log.info("1:" + type + param);
        String execCmd = String.format(CommonConstants.STOP_OS, type, param);
        log.info("execCmd" + execCmd);
        String pidData = toolUtil.exec(execCmd).toString();
        log.info("2:" + pidData);
        String[] pidList = pidData.split(System.lineSeparator());
        String tid = null;
        for (String s : pidList) {
            String substring = s.substring(0, s.indexOf(CommonConstants.BLANK));
            if (pid != null && s.contains(pid) && !pid.equals(
                    substring)) {
                tid = substring;
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
