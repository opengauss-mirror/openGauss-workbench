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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsPackageDownloadDTO {
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
    @NotBlank(message = "package support osVersion can not be empty")
    private String osVersion;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpsPackageDownloadDTO)) {
            return false;
        }
        OpsPackageDownloadDTO that = (OpsPackageDownloadDTO) o;
        return Objects.equals(name, that.name)
                && Objects.equals(os, that.os)
                && Objects.equals(cpuArch, that.cpuArch)
                && openGaussVersion == that.openGaussVersion
                && Objects.equals(openGaussVersionNum, that.openGaussVersionNum)
                && Objects.equals(downloadUrl, that.downloadUrl)
                && Objects.equals(wsBusinessId, that.wsBusinessId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, os, cpuArch, openGaussVersion, openGaussVersionNum, downloadUrl, wsBusinessId);
    }
}
