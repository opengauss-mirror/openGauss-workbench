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
 * AsyncFactory.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/manager/factory/AsyncFactory.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.framework.manager.factory;

import org.opengauss.admin.common.utils.ip.AddressUtils;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.admin.system.domain.SysOperLog;
import org.opengauss.admin.system.service.ISysOperLogService;

import java.util.Date;
import java.util.TimerTask;

/**
 * Async factory
 *
 * @author xielibo
 */
public class AsyncFactory {

    /**
     * Record Operation Log
     *
     * @param operLog operLog
     * @return task
     */
    public static TimerTask recordOper(final SysOperLog operLog) {
        return new TimerTask() {
            @Override
            public void run() {
            operLog.setOperLocation(AddressUtils.getRealAddressByIp(operLog.getOperIp()));
            operLog.setOperTime(new Date());
            SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }
}
