/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.dto;

import lombok.Data;
import org.opengauss.admin.plugin.enums.PortalType;

import java.util.List;

/**
 * Portal install host dto
 *
 * @since 2025/7/4
 */
@Data
public class PortalInstallHostDto {
    private String ip;
    private String hostname;
    private PortalType portalType;

    /**
     * 0： not install, 1: installing, 2: Installed, 10: install error
     */
    private List<Integer> installStatusList;
}
