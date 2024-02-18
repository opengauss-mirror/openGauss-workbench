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
 *  CollectTemplateMetricsServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/impl/CollectTemplateMetricsServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.model.dto.TemplateDetailDTO;
import com.nctigba.observability.instance.model.dto.TemplateDetailMetricDTO;
import com.nctigba.observability.instance.model.entity.CollectTemplateDO;
import com.nctigba.observability.instance.model.entity.CollectTemplateMetricsDO;
import com.nctigba.observability.instance.model.entity.CollectTemplateNodeDO;
import com.nctigba.observability.instance.exception.TipsException;
import com.nctigba.observability.instance.mapper.CollectTemplateMapper;
import com.nctigba.observability.instance.mapper.CollectTemplateMetricsMapper;
import com.nctigba.observability.instance.service.CollectTemplateMetricsService;
import com.nctigba.observability.instance.service.CollectTemplateNodeService;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @since 2023-11-02
 */
@Service
public class CollectTemplateMetricsServiceImpl
        extends ServiceImpl<CollectTemplateMetricsMapper, CollectTemplateMetricsDO>
        implements CollectTemplateMetricsService {
    @Autowired
    CollectTemplateNodeService collectTemplateNodeService;
    @Autowired
    CollectTemplateMapper collectTemplateMapper;
    @Autowired
    MetricsService metricsService;


    @Override
    public TemplateDetailDTO getTemplateDetailsByNodeId(String nodeId) {
        CollectTemplateNodeDO nodeTemplate = collectTemplateNodeService.getOne(
                new LambdaQueryWrapper<CollectTemplateNodeDO>()
                        .eq(CollectTemplateNodeDO::getNodeId, nodeId), false
        );
        Integer templateId = 1; // default template
        if (nodeTemplate != null) {
            templateId = nodeTemplate.getTemplateId();
        }

        return getTemplateDetails(templateId);
    }

    @Override
    public TemplateDetailDTO getTemplateDetails(Integer templateId) {
        TemplateDetailDTO result = new TemplateDetailDTO();
        List<String> metricGroupKeys = new ArrayList<>();

        // get all metrics group
        try {
            metricGroupKeys = metricsService.getAllMetricKeys();
        } catch (IOException e) {
            throw new TipsException("getMetricGroupKeyError", e);
        }

        // get template info
        CollectTemplateDO template = collectTemplateMapper.selectById(templateId);
        if (template != null) {
            result.setTemplateId(template.getId());
            result.setTemplateName(template.getName());
        }

        List<CollectTemplateMetricsDO> templateConfigs = baseMapper.selectList(
                new LambdaQueryWrapper<CollectTemplateMetricsDO>()
                        .eq(CollectTemplateMetricsDO::getTemplateId,
                                templateId)
        );

        List<TemplateDetailMetricDTO> details = new ArrayList<>();
        details = metricGroupKeys.stream()
                .map(a -> {
                    TemplateDetailMetricDTO temp = new TemplateDetailMetricDTO();
                    temp.setMetricGroupKey(a);
                    temp.setMetricGroupName(MessageSourceUtils.get("metrics." + a + ".name"));
                    temp.setMetricGroupDescription(MessageSourceUtils.get("metrics." + a + ".desc"));
                    Optional<CollectTemplateMetricsDO> match =
                            templateConfigs.stream().filter(z -> z.getMetricsKey().equals(a)).findFirst();
                    if (match.isPresent()) {
                        temp.setInterval(match.get().getInterval());
                        temp.setIsEnable(match.get().getIsEnable());
                    } else {
                        temp.setInterval(CommonConstants.DEFAULT_SCRAPE_TIME);
                        temp.setIsEnable(true);
                    }
                    return temp;
                })
                .collect(Collectors.toList());
        result.setDetails(details);

        return result;
    }
}
