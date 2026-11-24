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

package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * SysPluginRepository model
 *
 * @author: zhaochen
 * @since 2025-03-20
 * @version 7.0.0-RC1
 */
@Data
@Builder
@TableName("sys_plugins_repository")
public class SysPluginRepository {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * pluginId
     */
    private String pluginId;

    /**
     * isDownloaded
     */
    private Integer isDownloaded = 0;

    /**
     * pluginVersion
     */
    private String pluginVersion;

    /**
     * downloadUrl
     */
    private String downloadUrl;

    /**
     * pluginDesc
     */
    private String pluginDesc;

    /**
     * pluginDescEn
     */
    private String pluginDescEn;

    @Tolerate
    public SysPluginRepository() {
    }
}
