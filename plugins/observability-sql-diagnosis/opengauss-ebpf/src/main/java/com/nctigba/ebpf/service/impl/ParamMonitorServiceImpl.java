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
 *  ParamMonitorServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/service/impl/ParamMonitorServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.config.OsConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constant.CommonConstants;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.service.ParamMonitorService;
import com.nctigba.ebpf.util.HTTPUtils;
import com.nctigba.ebpf.util.OSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Async("ebpfPool")
public class ParamMonitorServiceImpl implements ParamMonitorService {
    @Autowired
    UrlConfig urlConfig;
    @Autowired
    OsConfig osConfig;

    @Override
    public void getOsParamData(String tid, String taskId, String monitorType) {
        HTTPUtils httpUtils = new HTTPUtils();
        String httpUrl = urlConfig.getHttpUrl();
        String outputUrl = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileUrl = " > " + outputUrl + taskId + monitorType;
        String url = httpUrl.substring(0, httpUrl.lastIndexOf(CommonConstants.SLASH) + 1) + taskId + httpUrl.substring(
                httpUrl.lastIndexOf(CommonConstants.SLASH));
        String cmd;
        if ("osParam".equals(monitorType)) {
            cmd = "sysctl -a " + fileUrl + FileTypeConstants.DEFAULT;
        } else {
            cmd = osConfig.getCpuCoreNum() + fileUrl + FileTypeConstants.DEFAULT;
        }
        OSUtils osUtils = new OSUtils();
        osUtils.exec(cmd);
        FileSystemResource file = new FileSystemResource(outputUrl + taskId + monitorType + FileTypeConstants.DEFAULT);
        httpUtils.httpUrlPost(url, file, monitorType);
    }
}
