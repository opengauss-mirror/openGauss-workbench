/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.Generated;
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
@Generated
@Accessors(chain = true)
public class NotifyConfig {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @NotBlank(groups = DefaultGroup.class)
    private String type;
    @NotBlank(groups = EmailGroup.class)
    private String email;
    @NotBlank(groups = EmailGroup.class)
    private String sender;
    @NotBlank(groups = EmailGroup.class)
    private String sever;
    @NotNull(groups = EmailGroup.class)
    private Integer port;
    @NotBlank(groups = EmailGroup.class)
    private String account;
    @NotBlank(groups = EmailGroup.class)
    private String passwd;
    @NotBlank(groups = {WeComGroup.class, DingTalkGroup.class})
    private String agentId;
    @NotBlank(groups = {WeComGroup.class, DingTalkGroup.class})
    private String appKey;
    @NotBlank(groups = {WeComGroup.class, DingTalkGroup.class})
    private String secret;
    @NotNull(groups = DefaultGroup.class)
    private Integer enable;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer isDeleted;

    public interface DefaultGroup {
    }

    public interface EmailGroup {
    }

    public interface WeComGroup {
    }

    public interface DingTalkGroup {
    }
}
