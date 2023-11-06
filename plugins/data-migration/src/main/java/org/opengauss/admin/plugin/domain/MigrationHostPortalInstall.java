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
 * MigrationHostPortalInstall.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/domain/MigrationHostPortalInstall.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * Host portal install model
 *
 * @author xielibo
 */
@TableName(value = "tb_migration_host_portal_install", autoResultMap = true)
@Data
public class MigrationHostPortalInstall {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String runHostId;

    // 0 ：not install  1：installing；2：Installed；10：install error
    private Integer installStatus;

    private String installPath;

    private String hostUserId;
    // ip
    private String host;
    private Integer port;
    private String runUser;
    private String runPassword;
    // 0: online 1: offline 2:import install
    private Integer installType;
    private String pkgDownloadUrl;
    private String pkgName;
    private String jarName;

    @TableField(exist = false)
    @JSONField(name = "thirdPartySoftwareConfig", serialize = false, deserialize = false)
    private MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig;
    @TableField(exist = false)
    private Integer kafkaBindId;


    @TableField(typeHandler = FastjsonTypeHandler.class)
    private UploadInfo pkgUploadPath;
    @TableField(exist = false)
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    private MultipartFile file;

    public String getPortalLogPath() {
        return installPath + "portal/logs/portal_.log";
    }

    public String getDatakitLogPath() {
        return installPath + "datakit_install_portal.log";
    }
}
