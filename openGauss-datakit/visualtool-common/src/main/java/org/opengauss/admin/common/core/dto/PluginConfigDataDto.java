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
 * PluginConfigDataDto.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/dto/PluginConfigDataDto.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * @className: PluginConfigDataDto
 * @description: PluginConfigDataDto
 * @author: xielibo
 * @date: 2022-09-25 3:14 PM
 **/
@Data
public class PluginConfigDataDto {

    @NotNull
    private String pluginId;
    @NotNull
    private String configData;
}
