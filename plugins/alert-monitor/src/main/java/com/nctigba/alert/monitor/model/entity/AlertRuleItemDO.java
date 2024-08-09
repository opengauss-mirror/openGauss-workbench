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
 *  AlertRuleItemDO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/entity/AlertRuleItemDO.java
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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author wuyuebin
 * @date 2023/5/9 11:19
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("alert_rule_item")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertRuleItemDO {
    @TableId
    @JsonProperty("ruleItemId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ruleId;
    @NotBlank
    private String ruleMark;
    @NotBlank(groups = AlertRuleDO.IndexRuleGroup.class)
    private String ruleExpName;
    @NotBlank(groups = AlertRuleDO.IndexRuleGroup.class)
    @EnumString(values = {"normal", "increase", "decrease"})
    private String action;
    @EnumString(values = {">", ">=", "==", "<=", "<", "!="})
    private String operate;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal limitValue;
    private String unit;
    @NotBlank(groups = AlertRuleDO.IndexRuleGroup.class)
    private String ruleExp;

    private String ruleExpParam;
    private String ruleItemDesc;
    @NotBlank(groups = AlertRuleDO.LogRuleGroup.class)
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
