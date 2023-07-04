/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;
import com.nctigba.alert.monitor.constant.CommonConstants;
import org.springframework.format.annotation.DateTimeFormat;

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
public class NotifyWay {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String notifyType;
    private String phone;
    private String email;
    private String personId;
    private String deptId;
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
