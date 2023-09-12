/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.config.annotation.EnumString;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
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
public class AlertTemplateRuleItem {
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
    @NotBlank(groups = AlertTemplateRule.IndexRuleGroup.class)
    private String ruleExpName;
    @NotBlank(groups = AlertTemplateRule.IndexRuleGroup.class)
    @EnumString(values = {"normal", "increase", "decrease"})
    private String action;
    @NotBlank
    @EnumString(values = {">", ">=", "=", "<=", "<", "!="})
    private String operate;
    @NotBlank
    @Pattern(regexp = "^-?\\d+(\\.\\d+)?$")
    private String limitValue;
    private String unit;
    @NotBlank(groups = AlertTemplateRule.IndexRuleGroup.class)
    private String ruleExp;
    private String ruleExpParam;
    private String ruleItemDesc;
    @NotBlank(groups = AlertTemplateRule.LogRuleGroup.class)
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
