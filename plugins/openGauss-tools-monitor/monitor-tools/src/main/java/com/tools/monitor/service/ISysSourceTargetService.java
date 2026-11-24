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

package com.tools.monitor.service;

import com.tools.monitor.entity.MonitorResult;
import com.tools.monitor.entity.SysSourceTarget;

/**
 * ISysSourceTargetService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface ISysSourceTargetService {
    /**
     * selectAll
     *
     * @return MonitorResult
     */
    MonitorResult selectAll();

    /**
     * save
     *
     * @param sysSourceTarget sysSourceTarget
     * @return MonitorResult
     */
    MonitorResult save(SysSourceTarget sysSourceTarget);
}
