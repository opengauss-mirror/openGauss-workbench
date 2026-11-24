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

import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.validator.annotation.EnumString;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/17 00:07
 * @description
 */
@Data
public class AlertTemplateQuery {
    private Long id;
    @NotBlank
    private String templateName;
    @NotBlank
    @EnumString(values = {CommonConstants.INSTANCE, CommonConstants.PLUGIN})
    private String type;

    @Valid
    @NotNull
    @Size(min = 1)
    private List<AlertTemplateRuleQuery> templateRuleReqList;

    private List<String> excludedTemplateRuleIds;
}
