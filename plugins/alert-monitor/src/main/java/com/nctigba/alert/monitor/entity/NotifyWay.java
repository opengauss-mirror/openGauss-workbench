/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.config.annotation.EnumInteger;
import com.nctigba.alert.monitor.config.annotation.EnumString;
import com.nctigba.alert.monitor.config.annotation.NotBlankConditional;
import com.nctigba.alert.monitor.config.annotation.NotBlankSummary;
import com.nctigba.alert.monitor.config.annotation.NotNullConditional;
import com.nctigba.alert.monitor.config.annotation.NotNullSummary;
import com.nctigba.alert.monitor.config.annotation.OneNotNull;
import com.nctigba.alert.monitor.constant.CommonConstants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author wuyuebin
 * @date 2023/5/9 11:24
 * @description
 */
@Accessors(chain = true)
@Data
@TableName("notify_way")
@JsonIgnoreProperties(ignoreUnknown = true)
@NotNullSummary
@NotBlankSummary
@OneNotNull(checkFields = {"personId", "deptId"}, conditionalField = "sendWay", conditionalValues = "0")
public class NotifyWay {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @EnumString(values = {CommonConstants.EMAIL, CommonConstants.WE_COM, CommonConstants.DING_TALK,
        CommonConstants.WEBHOOK, CommonConstants.SNMP})
    private String notifyType;
    @NotNullConditional(conditionalField = "notifyType",
        conditionalValues = {CommonConstants.WE_COM, CommonConstants.DING_TALK})
    @EnumInteger(values = {0, 1})
    private Integer sendWay;
    @NotBlankConditional.List({
        @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {CommonConstants.WEBHOOK}),
        @NotBlankConditional(conditionalField = "sendWay", conditionalValues = {"1"})
    })
    private String webhook;
    private String sign;
    private String phone;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {CommonConstants.EMAIL})
    @Email
    private String email;
    private String personId;
    private String deptId;
    private String header;
    private String params;
    private String body;
    private String resultCode;

    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {CommonConstants.SNMP})
    private String snmpIp;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {CommonConstants.SNMP})
    private String snmpPort;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {CommonConstants.SNMP})
    private String snmpCommunity;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {CommonConstants.SNMP})
    private String snmpOid;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {CommonConstants.SNMP})
    private Integer snmpVersion;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {SnmpConstants.version3 + ""})
    private String snmpUsername;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {SnmpConstants.version3 + ""})
    private String snmpAuthPasswd;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {SnmpConstants.version3 + ""})
    private String snmpPrivPasswd;
    @NotNull
    @JsonSerialize(using = ToStringSerializer.class)
    private Long notifyTemplateId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer isDeleted = CommonConstants.IS_NOT_DELETE;
}
