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
import lombok.Generated;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/28 02:29
 * @description
 */
@Data
@Generated
@Accessors(chain = true)
@TableName("alert_template_rule")
public class AlertTemplateRule {
    @TableId
    @JsonProperty("templateRuleId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
    private String alertDesc;
    @NotBlank
    private String notifyWayIds;
    @NotBlank
    private String alertNotify;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime silenceEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime silenceStartTime;
    @NotNull
    private Integer isSilence;
    @NotNull
    private Integer isRepeat;
    @NotBlank
    private String notifyDurationUnit;
    @NotNull
    private Integer notifyDuration;
    @NotBlank
    private String ruleContent;
    @NotBlank
    private String ruleExpComb;
    @NotBlank
    private String ruleType;
    @NotBlank
    private String level;
    @NotBlank
    private String ruleName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ruleId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer isDeleted;
    @TableField(exist = false)
    @NotNull
    @Valid
    @Size(min = 1)
    private List<AlertTemplateRuleItem> alertRuleItemList;
}
