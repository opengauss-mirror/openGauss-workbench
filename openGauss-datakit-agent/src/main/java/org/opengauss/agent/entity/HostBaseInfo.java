/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.entity;

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
    private String hostName;
    private String cpuModel;
    private String cpuArchitecture;
    private long cpuFreq;
    private int physicalCores;
    private int logicalCores;
    private String osName;
    private String osVersion;
    private String osBuild;
}