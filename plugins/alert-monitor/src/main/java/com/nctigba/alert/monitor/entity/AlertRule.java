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
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
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
public class AlertRule {
    @TableId
    @JsonProperty("ruleId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String alertDesc;
    private String notifyWayIds;
    private String alertNotify;
    private Timestamp silenceEndTime;
    private Timestamp silenceStartTime;
    private Integer isSilence;
    private Integer isRepeat;
    private String notifyDurationUnit;
    private Integer notifyDuration;
    private String ruleContent;
    private String ruleExpComb;
    private String ruleType;
    private String level;
    private String ruleName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer isDeleted;
    @TableField(exist = false)
    private List<AlertRuleItem> alertRuleItemList;
}
