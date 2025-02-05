/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ResourceCPUController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/controller/ResourceCPUController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.enums.MetricsLine;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO.envType;
import com.nctigba.observability.instance.mapper.AgentNodeRelationMapper;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.ip.IpUtils;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/")
@Slf4j
public class ResourceCPUController extends ControllerConfig {
    @Autowired
    private MetricsService metricsService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    private AgentNodeRelationMapper agentMapper;

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
            MetricsLine.CPU_TOTAL_1M_LOAD,
            MetricsLine.CPU_TOTAL_5M_LOAD,
            MetricsLine.CPU_TOTAL_15M_LOAD,
            MetricsLine.CPU_TOTAL_CORE_NUM,
    };

    @SuppressWarnings("unchecked")
    @GetMapping("cpu")
    public AjaxResult cpu(String id, Long start, Long end, Integer step) {
        Map<String, Object> cpu = metricsService.listBatch(CPU, id, start, end, step);
        int core = 1;
        if (cpu.get(MetricsLine.CPU_TOTAL_CORE_NUM.name()) != null) {
            for (Number n : (List<Number>) cpu.get(MetricsLine.CPU_TOTAL_CORE_NUM.name())) {
                if (n.intValue() != 0) {
                    core = n.intValue();
                    break;
                }
            }
        }
        if (cpu.get(MetricsLine.CPU_DB.name()) != null) {
            List<Number> list = (List<Number>) cpu.get(MetricsLine.CPU_DB.name());
            if (list != null) {
                List<Number> db = new ArrayList<>();
                for (Number n : list) {
                    db.add(n.doubleValue() / core);
                }
                cpu.put(MetricsLine.CPU_DB.name(), db);
            }
        }
        return AjaxResult.success(cpu);
    }

    @GetMapping("topOSProcessAndDBThread")
    public AjaxResult topOSProcessAndDBThread(String id, String sort) throws IOException, InterruptedException {
        List<AgentNodeRelationDO> relationList = agentMapper.selectList(
                Wrappers.<AgentNodeRelationDO>lambdaQuery().eq(AgentNodeRelationDO::getNodeId, id));
        if (CollectionUtil.isEmpty(relationList)) {
            return AjaxResult.success("agent not installed");
        }
        NctigbaEnvDO env = null;
        for (AgentNodeRelationDO agentNodeRel : relationList) {
            env = envMapper.selectOne(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getId, agentNodeRel.getEnvId())
                    .eq(NctigbaEnvDO::getType, envType.EXPORTER));
            if (env == null) {
                continue;
            }
            break;
        }
        if (env == null) {
            return AjaxResult.success("agent not installed");
        }
        var param = new HashMap<String, Object>();
        param.put("nodeId", id);
        if (StrUtil.isNotBlank(sort)) {
            param.put("sort", sort);
        }
        var host = hostFacade.getById(env.getHostid());
        var str = HttpUtil.get("http://"
            + IpUtils.formatIp(host.getPublicIp()) + ":" + env.getPort() + "/cmd/top", param);
        ArrayList<Object> result = new ArrayList<>();
        try {
            var top = JSONUtil.parseArray(str);
            for (Object object : top) {
                if (object instanceof JSONArray) {
                    JSONArray arr = (JSONArray) object;
                    result.add(arr.subList(0, Math.min(10, arr.size())));
                }
            }
        } catch (Exception e) {
           log.error("parse to Array error:" + e.getMessage());
        }
        return AjaxResult.success(result);
    }
}