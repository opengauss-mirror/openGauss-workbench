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
 * SysPlugin.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/domain/SysPlugin.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * SysPlugin model
 *
 * @author xielibo
 */
@Data
@Builder
@TableName("sys_plugins")
public class SysPlugin {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * pluginId
     */
    private String pluginId;

    /**
     * bootstrapClass
     */
    private String bootstrapClass;

    /**
     * pluginDesc
     */
    private String pluginDesc;

    private String pluginDescEn;

    /**
     * logoPath
     */
    private String logoPath;

    /**
     * pluginType
     */
    private Integer pluginType;

    /**
     * pluginVersion
     */
    private String pluginVersion;

    /**
     * pluginProvider
     */
    private String pluginProvider;

    /**
     * status；1：started；2：stoped
     */
    private Integer pluginStatus;

    /**
     * isNeedConfigured
     */
    private Integer isNeedConfigured;

    /**
     * theme; dark light,default light
     */
    private String theme;


    @Tolerate
    public SysPlugin() {

    }

}
