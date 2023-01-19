/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
