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
import org.opengauss.admin.plugin.enums.PortalType;
import org.opengauss.admin.plugin.enums.PortalVersion;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Optional;

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
    private PortalType portalType;
    @TableField(exist = false)
    private PortalVersion portalVersion;

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

    /**
     * get portal log path
     *
     * @return String
     */
    public String getPortalLogPath() {
        if (PortalType.MULTI_DB.equals(portalType)) {
            return installPath + "portal/logs/portal.log";
        }
        return installPath + "portal/logs/portal_.log";
    }

    public String getDatakitLogPath() {
        return installPath + "datakit_install_portal.log";
    }

    /**
     * get shell information
     *
     * @return ShellInfoVo
     */
    public ShellInfoVo getShellInfoVo() {
        return new ShellInfoVo(this.getHost(), this.getPort(), this.getRunUser(), this.getRunPassword());
    }

    /**
     * Judge whether the version is ok
     *
     * @param version the version
     * @return Boolean
     */
    public Boolean isVersionGreaterThan(Integer version) {
        Optional<String[]> partsOptional = Optional.ofNullable(this.jarName).map(name -> name.split("-"));
        return partsOptional.map(parts -> {
            if (parts.length > 1) {
                String parseVersion = parts[1];
                String[] versionParts = parseVersion.split("\\.");
                return Arrays.stream(versionParts).mapToInt(Integer::parseInt).findFirst().orElse(-1);
            } else {
                return -1;
            }
        }).map(mainVersion -> mainVersion > version).orElse(false);
    }

    /**
     * get portal install root path
     *
     * @return String
     */
    public String getInstallRootPath() {
        return installPath + "portal/";
    }
}
