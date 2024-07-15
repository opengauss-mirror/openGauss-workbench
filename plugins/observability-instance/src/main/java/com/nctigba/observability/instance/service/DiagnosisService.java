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
 *  DiagnosisService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/DiagnosisService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.enums.MetricsLine;
import com.nctigba.observability.instance.mapper.AgentNodeRelationMapper;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.model.dto.MetricQueryDTO;
import com.nctigba.observability.instance.model.dto.TemplateDetailMetricDTO;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestService
 *
 * @author luomeng
 * @since 2024/4/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiagnosisService {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    private AgentNodeRelationMapper relationMapper;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private CollectTemplateMetricsService templateMetricsService;

    /**
     * get Metric Data
     *
     * @param nodeId nodeId
     * @return AjaxResult
     */
    public AjaxResult getMetricData(String nodeId) {
        if (nodeId == null || nodeId.split(";").length < 5) {
            return AjaxResult.success("param is null!");
        }
        Map<String, String> result = new HashMap<>();
        String[] parts = nodeId.split(";");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length > 0) {
                result.put(keyValue[0], keyValue.length != 2 ? null : keyValue[1]);
            }
        }
        String instanceId = result.get("nodeId");
        String start = result.get("start");
        String end = result.get("end");
        String step = result.get("step");
        String dbName = result.get("dbName");
        List<AgentNodeRelationDO> relationEntity = relationMapper.selectList(
                Wrappers.<AgentNodeRelationDO>lambdaQuery().eq(
                        AgentNodeRelationDO::getNodeId, instanceId));
        if (CollectionUtils.isEmpty(relationEntity)) {
            return AjaxResult.success("node is not exists relation!");
        }
        boolean isExists = false;
        for (AgentNodeRelationDO relationDO : relationEntity) {
            NctigbaEnvDO envDO = envMapper.selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getId, relationDO.getEnvId()).eq(
                            NctigbaEnvDO::getType, NctigbaEnvDO.envType.EXPORTER));
            if (envDO != null) {
                isExists = true;
            }
        }
        if (!isExists) {
            return AjaxResult.success("export is not exists!");
        }
        List<TemplateDetailMetricDTO> metricDTOList = templateMetricsService.getTemplateDetailsByNodeId(
                instanceId).getDetails();
        if (CollectionUtils.isEmpty(metricDTOList)) {
            return AjaxResult.success("template is not exists!");
        }
        for (TemplateDetailMetricDTO metricDTO : metricDTOList) {
            if ("pg_stat_activity_slow".equals(metricDTO.getMetricGroupKey()) && !metricDTO.getIsEnable()) {
                return AjaxResult.success("metric is not enable!");
            }
        }
        MetricsLine[] INSTANCE = new MetricsLine[]{MetricsLine.INSTANCE_DB_SLOWSQL};
        MetricQueryDTO dto = new MetricQueryDTO(INSTANCE, instanceId, Long.valueOf(start), Long.valueOf(end),
                Integer.valueOf(step), dbName);
        return AjaxResult.success(
                metricsService.slowSqlList(dto));
    }
}
