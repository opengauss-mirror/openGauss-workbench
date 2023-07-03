/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wuyuebin
 * @date 2023/5/22 10:36
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertClusterNodeConfDto {
    private String clusterNodeId;
    private String nodeName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
    private String templateName;
}
