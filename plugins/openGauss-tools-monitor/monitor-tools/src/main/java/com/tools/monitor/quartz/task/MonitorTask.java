/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.mapper.SysConfigMapper;
import com.tools.monitor.mapper.SysJobMapper;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.service.impl.MeterServiceImpl;
import com.tools.monitor.service.impl.SysJobServiceImpl;
import com.tools.monitor.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

/**
 * MonitorTask
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Component("monitorTask")
public class MonitorTask {
    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private SysJobMapper sysJobMapper;

    @Autowired
    private SysSourceTargetMapper sourceTargetMapper;

    @Autowired
    private SysJobServiceImpl jobService;

    @Autowired
    private MeterServiceImpl meterService;

    private JdbcTemplate jdbcTemplate;

    /**
     * targetParams
     *
     * @param params params
     * @param name name
     * @param jobId jobId
     */
    public void targetParams(String params, String name, Long jobId) {
        log.info(name);
        List<Long> configs = sourceTargetMapper.getSourceIdByJobId(jobId);
        List<SysConfig> sysConfigs = configMapper.getBatchById(configs);
        SysJob sysJob = sysJobMapper.selectJobById(jobId);
        if (CollectionUtil.isNotEmpty(sysConfigs) && ObjectUtil.isNotEmpty(sysJob)) {
            for (SysConfig sysConfig : sysConfigs) {
                SysConfig entity = sysConfig;
                entity.setPassword(Base64.decode(entity.getPassword()));
                DriverManagerDataSource dataSource = jobService.getDataSource(entity);
                jdbcTemplate = new JdbcTemplate(dataSource);
                List<Map<String, Object>> list = jobService.executeSql(jdbcTemplate, params);
                if (CollectionUtil.isEmpty(list)) {
                    continue;
                }
                meterService.publish(list, sysConfig, name, sysJob);
            }
        }
    }
}
