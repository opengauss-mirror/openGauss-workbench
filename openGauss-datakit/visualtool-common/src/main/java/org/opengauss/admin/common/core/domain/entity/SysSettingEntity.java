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
 * SysSettingEntity.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/entity/SysSettingEntity.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * System Setting Model
 *
 * @author wangyl
 */
@Data
@TableName("sys_setting")
public class SysSettingEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotBlank(message = "uploadPath cannot be empty")
    private String uploadPath;
    @NotNull(message = "userId cannot be empty")
    private Integer userId;
    @NotBlank(message = "portalPkgDownloadUrl cannot be empty")
    private String portalPkgDownloadUrl;
    @NotBlank(message = "portalPkgName cannot be empty")
    private String portalPkgName;
    @NotBlank(message = "portalJarName cannot be empty")
    private String portalJarName;
    @TableField(exist = false)
    private String serverHost;
}
