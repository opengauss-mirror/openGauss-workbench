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
 *  AlertTemplateRuleItemDO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/entity/AlertTemplateRuleItemDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.model.validator.annotation.EnumString;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author wuyuebin
 * @date 2023/4/28 02:36
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("alert_template_rule_item")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertTemplateRuleItemDO {
    @TableId
    @JsonProperty("templateRuleItemId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateRuleId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ruleItemId;
    @NotBlank
    private String ruleMark;
    @NotBlank(groups = AlertTemplateRuleDO.IndexRuleGroup.class)
    private String ruleExpName;
    @NotBlank(groups = AlertTemplateRuleDO.IndexRuleGroup.class)
    @EnumString(values = {"normal", "increase", "decrease"})
    private String action;
    @EnumString(values = {">", ">=", "=", "<=", "<", "!="})
    private String operate;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal limitValue;
    private String unit;
    @NotBlank(groups = AlertTemplateRuleDO.IndexRuleGroup.class)
    private String ruleExp;
    private String ruleExpParam;
    private String ruleItemDesc;
    @NotBlank(groups = AlertTemplateRuleDO.LogRuleGroup.class)
    private String keyword;
    private String blockWord;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer isDeleted;
}
