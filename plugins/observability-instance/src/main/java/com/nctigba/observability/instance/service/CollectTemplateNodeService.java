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
 *  CollectTemplateNodeService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/CollectTemplateNodeService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.observability.instance.model.dto.PrometheusConfigNodeDTO;
import com.nctigba.observability.instance.model.dto.SetNodeTemplateDTO;
import com.nctigba.observability.instance.model.dto.SetNodeTemplateDirectDTO;
import com.nctigba.observability.instance.model.entity.CollectTemplateNodeDO;

import java.util.List;

/**
 * Service to manager node templated setting
 *
 * @since 2023-11-02
 */
public interface CollectTemplateNodeService extends IService<CollectTemplateNodeDO> {
    /**
     * Set node template directly
     *
     * @param setNodeTemplateDirectDTO DTO
     * @since 2023/12/1
     */
    void setNodeTemplateDirect(SetNodeTemplateDirectDTO setNodeTemplateDirectDTO);

    /**
     * Set template for node
     *
     * @param templateNodeDTO DTO
     * @since 2023/12/1
     */
    void setNodeTemplate(SetNodeTemplateDTO templateNodeDTO);

    /**
     * Convert node prometheus config param
     *
     * @param templateId Template id
     * @param nodeIds    Node ids
     * @return Convert result
     * @since 2023/12/1
     */
    List<PrometheusConfigNodeDTO> getNodePrometheusConfigParam(Integer templateId, List<String> nodeIds);

    /**
     * To update prometheus configs
     *
     * @param configNodes Configs DTO
     * @since 2023/12/1
     */
    @DS("private")
    void setPrometheusConfig(List<PrometheusConfigNodeDTO> configNodes);
}
