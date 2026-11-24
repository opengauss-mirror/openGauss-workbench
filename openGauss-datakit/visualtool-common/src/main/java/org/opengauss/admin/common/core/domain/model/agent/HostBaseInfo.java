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

package org.opengauss.admin.common.core.domain.model.agent;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * HostBaseInfo
 *
 * @author: wangchao
 * @Date: 2025/4/8 17:18
 * @Description: HostBaseInfo
 * @since 7.0.0-RC2
 **/
@Data
@Builder
@Accessors(chain = true)
public class HostBaseInfo {
    private String agentId;
    private String hostName;
    private String cpuModel;
    private String cpuArchitecture;
    private String cpuFreq;
    private int physicalCores;
    private int logicalCores;
    private String osName;
    private String osVersion;
    private String osBuild;
}