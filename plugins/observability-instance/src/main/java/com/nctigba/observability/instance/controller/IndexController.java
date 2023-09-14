/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.MyBatisSystemException;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.instance.aop.Ds;
import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.SessionService;
import com.nctigba.observability.instance.service.TopSQLService;
import com.nctigba.observability.instance.util.SshSession;

import cn.hutool.core.util.StrUtil;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/")
@Log4j2
public class IndexController extends ControllerConfig {
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private TopSQLService topSQLService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private DbConfigMapper configMapper;
    @AutowiredType(Type.MAIN_PLUGIN)
    @Autowired
    private IOpsClusterService opsClusterService;
    @AutowiredType(Type.MAIN_PLUGIN)
    @Autowired
    private HostFacade hostFacade;
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    @Autowired
    private HostUserFacade hostUserFacade;
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    @Autowired
    private EncryptionUtils encryptionUtils;

    private static final MetricsLine[] MAIN = {
            MetricsLine.CPU,
            MetricsLine.MEMORY,
            MetricsLine.IO,
            MetricsLine.NETWORK_IN_TOTAL,
            MetricsLine.NETWORK_OUT_TOTAL,
            MetricsLine.SWAP,
            MetricsLine.DB_THREAD_POOL,
            MetricsLine.DB_ACTIVE_SESSION
    };

    /**
     * nodeInfo
     *
     * @param id id
     * @return AjaxResult
     */
    @GetMapping("nodeInfo")
    @Ds
    public AjaxResult nodeInfo(String id) {
        Map<String, Object> result = new HashMap<>();
        // cluster info
        var node = clusterManager.getOpsNodeById(id);
        result.put("version", configMapper.version());
        result.put("time", configMapper.starttime());
        var env = configMapper.env();
        result.put("dbDataPath", env.get("datapath"));
        result.put("dbLogPath", env.get("datapath") + StrUtil.SLASH + env.get("log_directory"));
        result.put("archiveMode", configMapper.archiveMode());
        OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
        var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
            return node.getInstallUserName().equals(e.getUsername());
        }).findFirst().orElse(null);
        try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), node.getInstallUserName(),
                encryptionUtils.decrypt(user.getPassword()));) {
            result.put("osVersion", session.execute("cat /etc/system-release"));
            result.put("CPUmanufacturer", session.execute("cat /proc/cpuinfo | grep 'vendor_id' | head -n 1 | "
                    + "awk -F: '{print $2}' | sed 's/^[ \\t]*//'"));
            result.put("CPUmodel", session.execute("cat /proc/cpuinfo | grep 'model name' | head -n 1 | "
                    + "awk -F: '{print $2}' | sed 's/^[ \\t]*//'"));
            result.put("CPUcores", session.execute("nproc"));
            result.put("TotalMemory", session.execute("free -h | awk '/^Mem:/ {print $2}'"));
        } catch (IOException | CustomException e) {
            log.error("", e);
        }
        return AjaxResult.success(result);
    }

    /**
     * mainMetrics
     *
     * @param id    id
     * @param start start
     * @param end   end
     * @param step  step
     * @return AjaxResult
     */
    @GetMapping("mainMetrics")
    public AjaxResult mainMetrics(String id, Long start, Long end, Integer step) {
        Map<String, Object> metrics = metricsService.listBatch(MAIN, id, start, end, step);
        var simpleStatistic = sessionService.simpleStatistic(id);
        metrics.putAll(simpleStatistic);
        return AjaxResult.success(metrics);
    }

    /**
     * topSQLNow
     *
     * @param id id
     * @return AjaxResult
     */
    @GetMapping(value = "topSQLNow")
    @Ds
    public AjaxResult topSQLNow(String id) {
        if (StrUtil.isBlank(id)) {
            return AjaxResult.success();
        }
        try {
            var blockAndLongTxc = sessionService.blockAndLongTxc(id);
            blockAndLongTxc.put("topSQLNow", topSQLService.topSQLNow(id));
            blockAndLongTxc.put("waitEvents", configMapper.waitEvents());
            return AjaxResult.success(blockAndLongTxc);
        } catch (MyBatisSystemException e) {
            log.error("connection fail");
            return AjaxResult.error();
        }
    }
}