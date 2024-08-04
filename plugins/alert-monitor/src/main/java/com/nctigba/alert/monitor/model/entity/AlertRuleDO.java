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
 *  AlertRuleDO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/entity/AlertRuleDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.model.validator.annotation.EnumInteger;
import com.nctigba.alert.monitor.model.validator.annotation.EnumString;
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
public class AlertRuleDO {
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
    @NotBlank(groups = {IndexRuleGroup.class, LogRuleGroup.class})
    @EnumString(values = {CommonConstants.SECOND, CommonConstants.MINUTE, CommonConstants.HOUR, CommonConstants.DAY})
    private String notifyDurationUnit;
    @NotNull(groups = {IndexRuleGroup.class, LogRuleGroup.class})
    private Integer notifyDuration;
    @NotBlank
    private String ruleContent;
    @NotBlank(groups = {IndexRuleGroup.class, LogRuleGroup.class})
    private String ruleExpComb;
    @NotBlank
    @EnumString(values = {CommonConstants.LOG_RULE, CommonConstants.INDEX_RULE, CommonConstants.PLUGIN_RULE})
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
    @NotEmpty(groups = {IndexRuleGroup.class, LogRuleGroup.class})
    @TableField(exist = false)
    private List<AlertRuleItemDO> alertRuleItemList;

    @NotBlank(groups = PluginRuleGroup.class)
    private String pluginCode;
    @NotBlank(groups = PluginRuleGroup.class)
    private String ruleCode;

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

    public interface PluginRuleGroup {
    }

    /**
     * validate Silence fields
     */
    public interface SilenceGroup {
    }
}
