/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * ISysLogService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ISysLogService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service;

import org.opengauss.admin.common.core.dto.SysLogConfigDto;
import org.opengauss.admin.common.core.vo.SysLogConfigVo;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * System log interface
 */
public interface ISysLogService {

    public SysLogConfigVo getAllLogConfig();

    //    @PostConstruct
    void init();

    public void saveAllLogConfig(SysLogConfigDto sysLogConfigDto);

    public void saveLogConfig(String key, String value);

    public String getConfigByKey(String key);

    public List<Map<String,Object>> listAllLogFile();

    public File getLogFileByName(String filename);
}
