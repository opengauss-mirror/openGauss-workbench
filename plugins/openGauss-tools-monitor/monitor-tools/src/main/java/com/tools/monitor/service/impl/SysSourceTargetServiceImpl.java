package com.tools.monitor.service.impl;

import com.tools.monitor.entity.AjaxResult;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.service.ISysSourceTargetService;
import lombok.extern.slf4j.Slf4j;
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
    public AjaxResult selectAll() {
        return null;
    }

    @Override
    public AjaxResult save(SysSourceTarget sysSourceTarget) {
        sysSourceTargetMapper.save(sysSourceTarget);
        return AjaxResult.success("保存成功");
    }
}
