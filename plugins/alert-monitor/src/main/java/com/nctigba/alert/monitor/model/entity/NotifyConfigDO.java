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
 *  NotifyConfigDO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/entity/NotifyConfigDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankConditional;
import com.nctigba.alert.monitor.model.validator.annotation.NotBlankSummary;
import com.nctigba.alert.monitor.model.validator.annotation.NotNullConditional;
import com.nctigba.alert.monitor.model.validator.annotation.NotNullSummary;
import com.nctigba.alert.monitor.constant.CommonConstants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author wuyuebin
 * @date 2023/5/7 14:01
 * @description
 */
@Data
@Accessors(chain = true)
@NotNullSummary
@NotBlankSummary
@TableName("notify_config")
public class NotifyConfigDO {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @NotBlank
    private String type;
    @NotBlankConditional(conditionalField = "type", conditionalValues = {CommonConstants.EMAIL})
    private String email;
    @NotBlankConditional(conditionalField = "type", conditionalValues = {CommonConstants.EMAIL})
    private String sender;
    @NotBlankConditional(conditionalField = "type", conditionalValues = {CommonConstants.EMAIL})
    private String sever;
    @NotNullConditional(conditionalField = "type", conditionalValues = {CommonConstants.EMAIL})
    private Integer port;
    @NotBlankConditional(conditionalField = "type", conditionalValues = {CommonConstants.EMAIL})
    private String account;
    @NotBlankConditional(conditionalField = "type", conditionalValues = {CommonConstants.EMAIL})
    private String passwd;
    @NotBlankConditional(conditionalField = "type",
        conditionalValues = {CommonConstants.WE_COM, CommonConstants.DING_TALK})
    private String agentId;
    @NotBlankConditional(conditionalField = "type",
        conditionalValues = {CommonConstants.WE_COM, CommonConstants.DING_TALK})
    private String appKey;
    @NotBlankConditional(conditionalField = "type",
        conditionalValues = {CommonConstants.WE_COM, CommonConstants.DING_TALK})
    private String secret;
    @NotNull
    private Integer enable;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
