/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.dto;

import lombok.Data;
import org.opengauss.admin.plugin.enums.PortalType;

/**
 * Portal install host dto
 *
 * @since 2025/7/4
 */
@Data
public class PortalInstallHostDto {
    private String ip;
    private String hostname;
    private Integer cpu;
    private Double memory;
    private Double disk;
    private PortalType portalType;
}
