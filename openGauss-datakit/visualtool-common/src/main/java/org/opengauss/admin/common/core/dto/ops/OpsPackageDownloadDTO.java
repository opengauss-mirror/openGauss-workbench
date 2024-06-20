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
 * OpsPackageDownloadDTO.java
 *
 * IDENTIFICATION
 * visualtool-common/src/main/java/org/opengauss/admin/common/core/dto/ops/OpsPackageDownloadDTO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.core.dto.ops;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsPackageDownloadDTO {
    private String packageId;
    /**
     * package name
     */
    @NotBlank(message = "package name can not be empty")
    private String name;
    /**
     * package support os
     */
    @NotBlank(message = "package support os can not be empty")
    private String os;
    /**
     * package support cpu arch
     */
    @NotBlank(message = "package support cpu arch can not be empty")
    private String cpuArch;
    /**
     * package support openGauss version
     */
    @NotNull(message = "package support openGauss version can not be empty")
    private OpenGaussVersionEnum openGaussVersion;
    /**
     * package support openGauss version num
     */
    @NotBlank(message = "package support openGauss version num can not be empty")
    private String openGaussVersionNum;
    /**
     * package download url
     */
    @NotBlank(message = "package download url can not be empty")
    private String downloadUrl;
    /**
     * package download ws business id
     */
    @NotBlank(message = "package download ws business id can not be empty")
    private String wsBusinessId;
}
