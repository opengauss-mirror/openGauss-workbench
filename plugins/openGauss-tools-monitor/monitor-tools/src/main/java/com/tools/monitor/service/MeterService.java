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

import java.util.List;
import java.util.Map;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.quartz.domain.SysJob;

/**
 * MeterService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface MeterService {
    /**
     * publish
     *
     * @param list list
     * @param sysConfig sysConfig
     * @param task task
     * @param sysJob sysJob
     */
    void publish(List<Map<String, Object>> list, SysConfig sysConfig, String task, SysJob sysJob);
}

