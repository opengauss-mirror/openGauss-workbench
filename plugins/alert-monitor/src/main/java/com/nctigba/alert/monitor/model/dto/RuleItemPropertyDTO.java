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
 *  RuleItemPropertyDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/RuleItemPropertyDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

import com.nctigba.alert.monitor.config.property.RuleItemProperty;
import lombok.Data;

/**
 * @author wuyuebin
 * @date 2023/5/29 11:07
 * @description
 */
@Data
public class RuleItemPropertyDTO extends RuleItemProperty {
    private String i18nName;
}
