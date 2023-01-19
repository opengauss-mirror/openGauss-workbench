/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.manager.factory;

import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.task.MonitorTask;
import com.tools.monitor.quartz.util.spring.MonitSpringUtils;
import com.tools.monitor.service.impl.MeterServiceImpl;
import com.tools.monitor.service.impl.SysJobServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * AsyncFactory
 *
 * @author liu
 * @since 2022-10-01
 */
public class AsyncFactory {
    /**
     * recordZabbix
     *
     * @param zabbix       zabbix
     * @param sysConfig    sysConfig
     * @param zabbixConfig zabbixConfig
     * @return TimerTask
     */
    public static TimerTask recordZabbix(final List<SysJob> zabbix,
        final SysConfig sysConfig,
        final SysConfig zabbixConfig) {
        return new TimerTask() {
            @Override
            public void run() {
                MonitSpringUtils.getClass(SysJobServiceImpl.class).executeZabbix(zabbix, sysConfig, zabbixConfig);
            }
        };
    }

    /**
     * removeRegistry
     *
     * @param gaugeName gaugeName
     * @return TimerTask
     */
    public static TimerTask removeRegistry(final List<String> gaugeName) {
        return new TimerTask() {
            /**
             * run
             */
            @Override
            public void run() {
                MonitSpringUtils.getClass(MeterServiceImpl.class).removeRegister(gaugeName);
            }
        };
    }

    /**
     * removeJobId
     *
     * @param jobid jobid
     * @return TimerTask
     */
    public static TimerTask removeJobId(final Long jobid) {
        return new TimerTask() {
            @Override
            public void run() {
                MonitSpringUtils.getClass(SysSourceTargetMapper.class).removeJobids(jobid);
            }
        };
    }

    /**
     * executeOne
     *
     * @param params params
     * @param name   name
     * @param jobId  jobId
     * @return TimerTask
     */
    public static TimerTask executeOne(final String params, final String name, final Long jobId) {
        return new TimerTask() {
            /**
             * run
             */
            @Override
            public void run() {
                MonitSpringUtils.getClass(MonitorTask.class).targetParams(params, name, jobId);
            }
        };
    }

    /**
     * reportNagios
     *
     * @param nagiosMap nagiosMap
     * @return TimerTask
     */
    public static TimerTask reportNagios(final Map<String, Object> nagiosMap) {
        return new TimerTask() {
            /**
             * run
             */
            @Override
            public void run() {
                MonitSpringUtils.getClass(MeterServiceImpl.class).reportNagios(nagiosMap);
            }
        };
    }

    /**
     * recordNagios
     *
     * @param nagios       nagios
     * @param sysConfig    sysConfig
     * @param nagiosConfig nagiosConfig
     * @return TimerTask
     */
    public static TimerTask recordNagios(final List<SysJob> nagios,
        final SysConfig sysConfig,
        final SysConfig nagiosConfig) {
        return new TimerTask() {
            /**
             * run
             */
            @Override
            public void run() {
                MonitSpringUtils.getClass(SysJobServiceImpl.class).publishNagios(nagios, sysConfig, nagiosConfig);
            }
        };
    }

    /**
     * executeNagios
     *
     * @param nagios nagios
     * @return TimerTask
     */
    public static TimerTask executeNagios(final List<SysJob> nagios) {
        return new TimerTask() {
            /**
             * run
             */
            @Override
            public void run() {
                MonitSpringUtils.getClass(SysJobServiceImpl.class).startNagios(nagios);
            }
        };
    }
}
