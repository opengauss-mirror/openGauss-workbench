/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.envType;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.MetricsService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/")
public class ResourceCPUController extends ControllerConfig {
    @Autowired
    private MetricsService metricsService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    private NctigbaEnvMapper envMapper;

    private static final MetricsLine[] CPU = {
            MetricsLine.CPU_TOTAL,
            MetricsLine.CPU_USER,
            MetricsLine.CPU_SYSTEM,
            MetricsLine.CPU_IOWAIT,
            MetricsLine.CPU_IRQ,
            MetricsLine.CPU_SOFTIRQ,
            MetricsLine.CPU_NICE,
            MetricsLine.CPU_STEAL,
            MetricsLine.CPU_IDLE,
            MetricsLine.CPU_DB,
            MetricsLine.CPU_TOTAL_5M_LOAD,
            MetricsLine.CPU_TOTAL_CORE_NUM,
    };

    @SuppressWarnings("unchecked")
    @GetMapping("cpu")
    public AjaxResult cpu(String id, Long start, Long end, Integer step) {
        Map<String, Object> cpu = metricsService.listBatch(CPU, id, start, end, step);
        int core = 1;
        for (Number n : (List<Number>) cpu.get(MetricsLine.CPU_TOTAL_CORE_NUM.name())) {
            if (n.intValue() != 0) {
                core = n.intValue();
                break;
            }
        }
        List<Number> db = new ArrayList<>();
        List<Number> list = (List<Number>) cpu.get(MetricsLine.CPU_DB.name());
        if (list != null) {
            for (Number n : list) {
                db.add(n.doubleValue() / core);
            }
            cpu.put(MetricsLine.CPU_DB.name(), db);
        }
        return AjaxResult.success(cpu);
    }

    @GetMapping("topOSProcessAndDBThread")
    public AjaxResult topOSProcessAndDBThread(String id, String sort) throws IOException, InterruptedException {
        var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getNodeid, id)
                .eq(NctigbaEnv::getType, envType.EXPORTER));
        if (env == null) {
            throw new CustomException("agent not installed");
        }
        var param = new HashMap<String, Object>();
        if (StrUtil.isNotBlank(sort)) {
            param.put("sort", sort);
        }
        var host = hostFacade.getById(env.getHostid());
        var str = HttpUtil.get("http://" + host.getPublicIp() + ":" + env.getPort() + "/cmd/top", param);
        var top = JSONUtil.parseArray(str);
        var result = new ArrayList<>();
        for (Object object : top) {
            if (object instanceof JSONArray) {
                JSONArray arr = (JSONArray) object;
                result.add(arr.subList(0, Math.min(10, arr.size())));
            }
        }
        return AjaxResult.success(result);
    }
}