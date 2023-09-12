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

import java.time.LocalDateTime;

/**
 * @author wuyuebin
 * @date 2023/4/14 10:52
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("alert_record")
public class AlertRecord {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String clusterNodeId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
    private String templateName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateRuleId;
    private String templateRuleName;
    private String templateRuleType;
    private String level;
    private String notifyWayIds;
    private String notifyWayNames;
    private Integer alertStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private Long duration;
    private String alertContent;
    private Integer recordStatus;
    private Integer sendCount;
    private LocalDateTime sendTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
