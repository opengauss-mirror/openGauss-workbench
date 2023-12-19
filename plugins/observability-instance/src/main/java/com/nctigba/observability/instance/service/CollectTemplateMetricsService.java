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
 *  CollectTemplateMetricsService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/CollectTemplateMetricsService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.observability.instance.model.dto.TemplateDetailDTO;
import com.nctigba.observability.instance.model.entity.CollectTemplateMetricsDO;

/**
 * Server to set template metrics data
 *
 * @since 2023-11-02
 */
public interface CollectTemplateMetricsService extends IService<CollectTemplateMetricsDO> {
    /**
     * Get template detail by node id
     *
     * @param nodeId Node id
     * @return Template Detail
     * @since 2023/12/1
     */
    TemplateDetailDTO getTemplateDetailsByNodeId(String nodeId);

    /**
     * Get template details by template id
     *
     * @param templateId Template Id
     * @return Template Detail
     * @since 2023/12/1
     */
    TemplateDetailDTO getTemplateDetails(Integer templateId);
}
