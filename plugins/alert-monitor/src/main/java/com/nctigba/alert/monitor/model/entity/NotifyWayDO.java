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
 *  NotifyWayDO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/entity/NotifyWayDO.java
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
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankConditional;
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankSummary;
import com.nctigba.alert.monitor.model.validator.annotation.NotNullConditional;
import com.nctigba.alert.monitor.model.validator.annotation.NotNullSummary;
import com.nctigba.alert.monitor.model.validator.annotation.OneNotNull;
import com.nctigba.alert.monitor.constant.CommonConstants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
public class NotifyWayDO {
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
    @Pattern(regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|)"
        + "(,\\s*([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|))*$")
    private String email;
    private String personId;
    private String deptId;
    private String header;
    private String params;
    @TableField("\"body\"")
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String snmpAuthPasswd;
    @NotBlankConditional(conditionalField = "notifyType", conditionalValues = {SnmpConstants.version3 + ""})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
