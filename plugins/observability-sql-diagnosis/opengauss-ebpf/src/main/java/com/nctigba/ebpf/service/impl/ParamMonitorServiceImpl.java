/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.ebpf.service.impl;

import com.nctigba.ebpf.config.OsConfig;
import com.nctigba.ebpf.config.UrlConfig;
import com.nctigba.ebpf.constant.FileTypeConstants;
import com.nctigba.ebpf.service.ParamMonitorService;
import com.nctigba.ebpf.util.OSUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
        String outputUrl = System.getProperty("user.dir") + "/output/";
        File dir = new File(outputUrl);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileUrl = " > " + outputUrl + taskId + monitorType;
        String cmd;
        if ("osParam".equals(monitorType)) {
            cmd = "sysctl -a " + fileUrl + FileTypeConstants.DEFAULT;
        } else {
            cmd = osConfig.getCpuCoreNum() + fileUrl + FileTypeConstants.DEFAULT;
        }
        OSUtils osUtils = new OSUtils();
        osUtils.exec(cmd);
    }
}
