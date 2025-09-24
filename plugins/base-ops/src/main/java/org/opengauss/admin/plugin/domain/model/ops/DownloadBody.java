/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * DownloadBody.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/DownloadBody.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author lhf
 * @date 2022/8/15 22:20
 **/
@Data
public class DownloadBody {
    @NotBlank(message = "The resource path cannot be empty")
    private String resourceUrl;
    @NotBlank(message = "The target path cannot be empty")
    private String targetPath;
    @NotBlank(message = "The file name cannot be empty")
    private String fileName;
    @NotBlank(message = "The service ID cannot be empty")
    private String businessId;
}
