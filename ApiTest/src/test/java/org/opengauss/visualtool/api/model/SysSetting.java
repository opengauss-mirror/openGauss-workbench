/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * system setting
 *
 * @since 2024/10/29
 */
@Data
@NoArgsConstructor
public class SysSetting {
    private Integer id;
    private String uploadPath;
    private Integer userId;
    private String portalPkgDownloadUrl;
    private String portalPkgName;
    private String portalJarName;
}
