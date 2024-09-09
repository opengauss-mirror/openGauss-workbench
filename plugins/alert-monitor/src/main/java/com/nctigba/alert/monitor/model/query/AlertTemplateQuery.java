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
 *  AlertTemplateQuery.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/query/AlertTemplateQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.query;

import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.validator.annotation.EnumString;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
