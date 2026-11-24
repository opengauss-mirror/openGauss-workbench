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

package com.nctigba.alert.monitor.model.query;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/23 18:30
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertClusterNodeAndTemplateQuery {
    @NotBlank
    private String clusterNodeIds;
    @NotBlank
    private String templateName;
    @NotBlank
    private String type;

    @NotNull
    @Valid
    private List<AlertTemplateRuleQuery> templateRuleReqList;
}
