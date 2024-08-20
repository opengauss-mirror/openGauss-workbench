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
 *  CollectConfigController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/controller/CollectConfigController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.mapper.PromAgentRelationMapper;
import com.nctigba.observability.instance.model.AjaxResult;
import com.nctigba.observability.instance.model.dto.SetNodeTemplateDirectDTO;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.model.entity.PromAgentRelationDO;
import com.nctigba.observability.instance.model.vo.CollectTemplateListVO;
import com.nctigba.observability.instance.service.AgentNodeRelationService;
import com.nctigba.observability.instance.service.CollectTemplateMetricsService;
import com.nctigba.observability.instance.service.CollectTemplateNodeService;
import com.nctigba.observability.instance.service.CollectTemplateService;
import com.nctigba.observability.instance.service.PrometheusService;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API for collect config
 *
 * @since 2023/12/1
 */
@RestController
@RequestMapping("/collectConfig/api")
@Api(tags = "Collect Config API")
public class CollectConfigController {
    @Autowired
    CollectTemplateNodeService collectTemplateNodeService;
    @Autowired
    CollectTemplateService collectTemplateService;
    @Autowired
    CollectTemplateMetricsService collectTemplateMetricsService;

    @Autowired
    AgentNodeRelationService agentNodeRelationService;
    @Autowired
    private PromAgentRelationMapper promAgentRelationMapper;
    @Autowired
    private NctigbaEnvMapper envMapper;
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    @Autowired
    private HostFacade hostFacade;
    @Autowired
    private PrometheusService prometheusService;

    /**
     * Set node template direct
     *
     * @param setNodeTemplateDirectDTO DTO
     * @return Normal result
     * @since 2023/12/1
     */
    @ApiOperation(value = "Set metrics scrape interval for a database node",
            notes = "Set metrics scrape interval for only a database node")
    @PostMapping(value = "/v1/templates/action", params = "action=setNodeTemplateDirect")
    public AjaxResult setNodeTemplateDirect(
            @RequestBody SetNodeTemplateDirectDTO setNodeTemplateDirectDTO) {
        List<AgentNodeRelationDO> agentNodeRelList = agentNodeRelationService.list(
            Wrappers.<AgentNodeRelationDO>lambdaQuery().eq(AgentNodeRelationDO::getNodeId,
                setNodeTemplateDirectDTO.getNodeId()));
        if (CollectionUtil.isEmpty(agentNodeRelList)) {
            collectTemplateNodeService.setNodeTemplateDirect(setNodeTemplateDirectDTO);
            return AjaxResult.success();
        }
        String envAgentId = agentNodeRelList.get(0).getEnvId();
        List<PromAgentRelationDO> promAgentRelList = promAgentRelationMapper.selectList(
            Wrappers.<PromAgentRelationDO>lambdaQuery().eq(PromAgentRelationDO::getEnvAgentId, envAgentId));
        if (CollectionUtil.isEmpty(promAgentRelList)) {
            NctigbaEnvDO agentEnv = envMapper.selectById(envAgentId);
            OpsHostEntity agentHost = hostFacade.getById(agentEnv.getHostid());
            Object obj = agentHost.getPublicIp() + ":" + agentEnv.getPort();
            throw new CustomException(MessageSourceUtils.get("collect.prom_not_found1", obj));
        }
        String envPromId = promAgentRelList.get(0).getEnvPromId();
        NctigbaEnvDO promEnv = envMapper.selectById(envPromId);
        if (promEnv == null) {
            throw new CustomException(MessageSourceUtils.get("collect.prom_not_found2"));
        }
        OpsHostEntity promHost = hostFacade.getById(promEnv.getHostid());
        promEnv.setHost(promHost);
        boolean isHealth = prometheusService.getHealthStatus(promEnv);
        if (!isHealth) {
            Object obj = promHost.getPublicIp() + ":" + promEnv.getPort();
            throw new CustomException(MessageSourceUtils.get("collect.prom_status_exception", obj));
        }
        Integer templateId = collectTemplateNodeService.setNodeTemplateDirect(setNodeTemplateDirectDTO);
        collectTemplateNodeService.setNodePrometheusConfig(setNodeTemplateDirectDTO.getNodeId(), templateId, envPromId);
        return AjaxResult.success();
    }

    /**
     * Api to get all templates
     *
     * @return List of templates
     * @since 2023/12/1
     */
    @GetMapping("/v1/templates")
    public AjaxResult getTemplates() {
        List<CollectTemplateListVO> result =
                collectTemplateService.list().stream()
                        .map(z -> {
                            CollectTemplateListVO vo = new CollectTemplateListVO();
                            BeanUtil.copyProperties(z, vo);
                            return vo;
                        }).collect(Collectors.toList());
        return AjaxResult.success(result);
    }

    /**
     * Get template detail
     *
     * @param templateId ID of template
     * @return Template detail
     * @since 2023/12/1
     */
    @GetMapping("/v1/templates/{templateId}")
    public AjaxResult getTemplateDetail(@PathVariable Integer templateId) {
        return AjaxResult.success(collectTemplateMetricsService.getTemplateDetails(templateId));
    }

    /**
     * Get node template detail
     *
     * @param nodeId Node Id
     * @return Template detail
     * @since 2023/12/1
     */
    @GetMapping("/v1/templates/getDetailByNodeId/{nodeId}")
    public AjaxResult getTemplateDetailByNodeId(@PathVariable String nodeId) {
        return AjaxResult.success(collectTemplateMetricsService.getTemplateDetailsByNodeId(nodeId));
    }
}