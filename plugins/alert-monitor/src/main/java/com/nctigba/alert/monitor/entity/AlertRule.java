/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.config.annotation.EnumInteger;
import com.nctigba.alert.monitor.config.annotation.EnumString;
import com.nctigba.alert.monitor.constant.CommonConstants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:04
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("alert_rule")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertRule {
    @TableId
    @JsonProperty("ruleId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String alertDesc;
    @NotBlank
    private String notifyWayIds;
    @NotBlank
    private String alertNotify;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(groups = SilenceGroup.class)
    private LocalDateTime silenceEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(groups = SilenceGroup.class)
    private LocalDateTime silenceStartTime;
    @NotNull
    @EnumInteger(values = {0, 1})
    private Integer isSilence;
    @NotNull(groups = IndexRuleGroup.class)
    @EnumInteger(values = {0, 1})
    private Integer isRepeat;
    @NotBlank
    @EnumString(values = {CommonConstants.SECOND, CommonConstants.MINUTE, CommonConstants.HOUR, CommonConstants.DAY})
    private String notifyDurationUnit;
    @NotNull
    private Integer notifyDuration;
    @NotBlank
    private String ruleContent;
    @NotBlank
    private String ruleExpComb;
    @NotBlank
    @EnumString(values = {CommonConstants.LOG_RULE, CommonConstants.INDEX_RULE})
    private String ruleType;
    @NotBlank
    @EnumString(values = {CommonConstants.SERIOUS, CommonConstants.WARN, CommonConstants.INFO})
    private String level;
    @NotBlank
    private String ruleName;
    private Integer nextRepeat;
    @EnumString(values = {CommonConstants.SECOND, CommonConstants.MINUTE, CommonConstants.HOUR, CommonConstants.DAY})
    private String nextRepeatUnit;
    private Integer maxRepeatCount;
    @NotNull(groups = LogRuleGroup.class)
    private Integer checkFrequency;
    @NotBlank(groups = LogRuleGroup.class)
    @EnumString(values = {CommonConstants.SECOND, CommonConstants.MINUTE, CommonConstants.HOUR, CommonConstants.DAY})
    private String checkFrequencyUnit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer isDeleted;
    @Valid
    @NotEmpty
    @TableField(exist = false)
    private List<AlertRuleItem> alertRuleItemList;

    /**
     * validate LogRule fields
     */
    public interface LogRuleGroup {
    }

    /**
     * validate IndexRule fields
     */
    public interface IndexRuleGroup {
    }

    /**
     * validate Silence fields
     */
    public interface SilenceGroup {
    }
}
