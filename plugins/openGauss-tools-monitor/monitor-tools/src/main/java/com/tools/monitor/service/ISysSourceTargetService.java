/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
