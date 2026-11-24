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

package com.tools.monitor.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.tools.monitor.entity.MonitorResult;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.service.ISysSourceTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * SysSourceTargetServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Service
@DependsOn("generatorFile")
public class SysSourceTargetServiceImpl implements ISysSourceTargetService {
    @Autowired
    private SysSourceTargetMapper sysSourceTargetMapper;

    @Override
    public MonitorResult selectAll() {
        return MonitorResult.success("succ");
    }

    @Override
    public MonitorResult save(SysSourceTarget sysSourceTarget) {
        sysSourceTargetMapper.save(sysSourceTarget);
        return MonitorResult.success("Save successfully");
    }
}
