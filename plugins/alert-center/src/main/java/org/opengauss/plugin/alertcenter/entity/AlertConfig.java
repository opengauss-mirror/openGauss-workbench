/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author wuyuebin
 * @date 2023/6/6 16:13
 * @description
 */
@Data
@Accessors(chain = true)
@TableName("alert_config")
public class AlertConfig {
    private Long id;
    @NotBlank
    private String alertIp;
    @NotBlank
    private String alertPort;
}
