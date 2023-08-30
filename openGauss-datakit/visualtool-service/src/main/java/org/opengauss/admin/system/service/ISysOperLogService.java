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
 * ISysOperLogService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ISysOperLogService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.system.domain.SysOperLog;

/**
 * OperLog Service Interface
 *
 * @author xielibo
 */
public interface ISysOperLogService {

    /**
     * Save Operlog
     *
     */
    public void insertOperlog(SysOperLog operLog);

    /**
     * Query the collection of system operation logs
     *
     * @param operLog operLog
     */
    public IPage<SysOperLog> selectOperLogList(IPage<SysOperLog> page, SysOperLog operLog);

    /**
     * Delete System Operation Logs in Batches
     *
     * @param operIds operIds
     */
    public int deleteOperLogByIds(String[] operIds);

    /**
     * Query operation log details
     *
     * @param operId operId
     */
    public SysOperLog selectOperLogById(String operId);

    /**
     * clear operation log
     */
    public void cleanOperLog();
}
