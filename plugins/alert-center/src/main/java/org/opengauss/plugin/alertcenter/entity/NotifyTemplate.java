/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author wuyuebin
 * @date 2023/5/6 14:27
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("notify_template")
public class NotifyTemplate {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @NotBlank
    private String notifyTemplateName;

    private String notifyTemplateDesc;

    @NotBlank
    private String notifyTitle;

    @NotBlank
    private String notifyContent;

    @NotBlank
    private String notifyTemplateType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer isDeleted = 0;
}
