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
 *  AlertRuleParamDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/AlertRuleParamDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemDO;
import com.nctigba.alert.monitor.model.validator.annotation.EnumInteger;
import com.nctigba.alert.monitor.model.validator.annotation.EnumString;
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankConditional;
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankSummary;
import com.nctigba.alert.monitor.model.validator.annotation.NotNullConditional;
import com.nctigba.alert.monitor.model.validator.annotation.NotNullSummary;
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
 * AlertRuleParamDTO
 *
 * @since 2023/12/15 11:45
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NotNullSummary
@NotBlankSummary
public class AlertRuleParamDTO {
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
    @NotNull(groups = AlertRuleDO.SilenceGroup.class)
    private LocalDateTime silenceEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(groups = AlertRuleDO.SilenceGroup.class)
    private LocalDateTime silenceStartTime;
    @NotNull
    @EnumInteger(values = {0, 1})
    private Integer isSilence;
    @NotNull(groups = AlertRuleDO.IndexRuleGroup.class)
    @EnumInteger(values = {0, 1})
    private Integer isRepeat;
    @NotBlank(groups = {AlertRuleDO.IndexRuleGroup.class, AlertRuleDO.LogRuleGroup.class})
    @EnumString(values = {CommonConstants.SECOND, CommonConstants.MINUTE, CommonConstants.HOUR, CommonConstants.DAY})
    private String notifyDurationUnit;
    @NotNull(groups = {AlertRuleDO.IndexRuleGroup.class, AlertRuleDO.LogRuleGroup.class})
    private Integer notifyDuration;
    @NotBlank
    private String ruleContent;
    @NotBlank(groups = {AlertRuleDO.IndexRuleGroup.class, AlertRuleDO.LogRuleGroup.class})
    private String ruleExpComb;
    @NotBlank
    @EnumString(values = {CommonConstants.LOG_RULE, CommonConstants.INDEX_RULE, CommonConstants.PLUGIN_RULE})
    private String ruleType;
    @NotBlank
    @EnumString(values = {CommonConstants.SERIOUS, CommonConstants.WARN, CommonConstants.INFO})
    private String level;
    @NotBlank
    private String ruleName;
    @NotNullConditional(conditionalField = "isRepeat", conditionalValues = {"1"})
    private Integer nextRepeat;
    @NotBlankConditional(conditionalField = "isRepeat", conditionalValues = {"1"})
    @EnumString(values = {CommonConstants.SECOND, CommonConstants.MINUTE, CommonConstants.HOUR, CommonConstants.DAY})
    private String nextRepeatUnit;
    @NotNullConditional(conditionalField = "isRepeat", conditionalValues = {"1"})
    private Integer maxRepeatCount;
    @NotNull(groups = AlertRuleDO.LogRuleGroup.class)
    private Integer checkFrequency;
    @NotBlank(groups = AlertRuleDO.LogRuleGroup.class)
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
    @NotEmpty(groups = {AlertRuleDO.IndexRuleGroup.class, AlertRuleDO.LogRuleGroup.class})
    private List<AlertRuleItemDO> alertRuleItemList;

    @NotBlank(groups = AlertRuleDO.PluginRuleGroup.class)
    private String pluginCode;
    @NotBlank(groups = AlertRuleDO.PluginRuleGroup.class)
    private String ruleCode;

    private List<Long> templateRuleIds;
}
