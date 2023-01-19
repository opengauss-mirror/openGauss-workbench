package com.tools.monitor.manager.factory;

import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.task.MonitorTask;
import com.tools.monitor.quartz.util.spring.SpringUtils;
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
     * @param zabbix
     * @param sysConfig
     * @return
     */
    public static TimerTask recordZabbix(final List<SysJob> zabbix, final SysConfig sysConfig, final SysConfig zabbixConfig) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(SysJobServiceImpl.class).executeZabbix(zabbix, sysConfig, zabbixConfig);
            }
        };
    }

    /**
     * removeRegistry
     *
     * @param gaugeName
     * @return TimerTask
     */
    public static TimerTask removeRegistry(final List<String> gaugeName) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(MeterServiceImpl.class).removeRegister(gaugeName);
            }
        };
    }

    /**
     * removeJobId
     *
     * @param jobid
     * @return TimerTask
     */
    public static TimerTask removeJobId(final Long jobid) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(SysSourceTargetMapper.class).removeJobids(jobid);
            }
        };
    }

    /**
     * executeOne
     *
     * @param params
     * @param name
     * @param jobId
     * @return TimerTask
     */
    public static TimerTask executeOne(final String params, final String name, final Long jobId) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(MonitorTask.class).targetParams(params, name, jobId);
            }
        };
    }

    /**
     * reportNagios
     *
     * @param nagiosMap
     * @return TimerTask
     */
    public static TimerTask reportNagios(final Map<String, Object> nagiosMap) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(MeterServiceImpl.class).reportNagios(nagiosMap);
            }
        };
    }

    /**
     * recordNagios
     *
     * @param nagios
     * @param sysConfig
     * @param nagiosConfig
     * @return TimerTask
     */
    public static TimerTask recordNagios(final List<SysJob> nagios, final SysConfig sysConfig, final SysConfig nagiosConfig) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(SysJobServiceImpl.class).publishNagios(nagios, sysConfig, nagiosConfig);
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
            @Override
            public void run() {
                SpringUtils.getBean(SysJobServiceImpl.class).startNagios(nagios);
            }
        };
    }
}
