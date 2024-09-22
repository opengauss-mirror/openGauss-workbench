/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * PackageDto.java
 *
 * IDENTIFICATION
 * visualtool-common/src/main/java/org/opengauss/admin/common/core/dto/ops/PackageDto.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.core.dto.ops;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class PackageDto {
    /**
     * package name
     */
    private String name;
    /**
     * package support os
     */
    private String os;
    private String osVersion;
    /**
     * package support cpu arch
     */
    private String cpuArch;
    /**
     * package support openGauss version
     */
    private OpenGaussVersionEnum openGaussVersion;
    /**
     * package support openGauss version num
     */
    private String openGaussVersionNum;

    /**
     * get simple info
     *
     * @return simple package info
     */
    public String simple() {
        return os + osVersion + " " + cpuArch + " " + openGaussVersion + openGaussVersionNum;
    }
}
