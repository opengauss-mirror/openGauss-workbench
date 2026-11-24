/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
