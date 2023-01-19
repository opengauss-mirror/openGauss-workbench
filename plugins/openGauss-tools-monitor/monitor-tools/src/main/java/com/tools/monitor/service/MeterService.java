package com.tools.monitor.service;

import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.quartz.domain.SysJob;

import java.util.List;
import java.util.Map;

/**
 * MeterService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface MeterService {
    void publish(List<Map<String, Object>> list, SysConfig sysConfig, String task, SysJob sysJob);

}

