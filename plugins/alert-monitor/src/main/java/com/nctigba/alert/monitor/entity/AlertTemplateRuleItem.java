/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/28 02:36
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("alert_template_rule_item")
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
    @NotBlank
    private String ruleExpName;
    @NotBlank
    private String action;
    @NotBlank
    private String operate;
    @NotBlank
    private String limitValue;
    private String unit;
    private String ruleExp;
    private String ruleExpParam;
    private String ruleItemDesc;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer isDeleted;
    @TableField(exist = false)
    private List<AlertTemplateRuleItemParam> itemParamList;
}
