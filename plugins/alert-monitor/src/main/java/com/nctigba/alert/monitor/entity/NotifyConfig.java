/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.nctigba.alert.monitor.config.annotation.NotBlankConditional;
import com.nctigba.alert.monitor.config.annotation.NotBlankSummary;
import com.nctigba.alert.monitor.config.annotation.NotNullConditional;
import com.nctigba.alert.monitor.config.annotation.NotNullSummary;
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
public class NotifyConfig {
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
