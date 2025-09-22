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
 * OpsClusterTaskDTO.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/dto/OpsClusterTaskDTO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.springframework.beans.BeanUtils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsClusterTaskDTO {
    private String clusterId;
    @NotBlank(message = "hostId can not be empty")
    private String hostId;
    @NotBlank(message = "hostUserId can not be empty")
    private String hostUserId;
    @NotBlank(message = "os can not be empty")
    private String os;
    @NotBlank(message = "cpuArch can not be empty")
    private String cpuArch;
    @NotNull(message = "version can not be empty")
    private OpenGaussVersionEnum version;
    @NotBlank(message = "versionNum can not be empty")
    private String versionNum;
    @NotBlank(message = "packageName can not be empty")
    private String packageName;
    @NotBlank(message = "packageId can not be empty")
    private String packageId;
    @NotBlank(message = "clusterName can not be empty")
    private String clusterName;
    @NotBlank(message = "databaseUsername can not be empty")
    private String databaseUsername;
    @NotBlank(message = "databasePassword can not be empty")
    private String databasePassword;
    @Range(min = 1024, max = 65535, message = "host port should be between 1024 and 65535")
    private long port = 5432;
    private String installPackagePath;
    private String installPath;
    private String logPath;
    private String tmpPath;
    private String omToolsPath;
    private String corePath;
    private String envPath;
    private Boolean enableCmTool;
    private Boolean enableGenerateEnvironmentVariableFile;
    private String xmlConfigPath;
    @NotNull(message = "deployType can not be empty")
    private DeployTypeEnum deployType;

    /**
     * to entity
     *
     * @return OpsClusterTaskEntity
     */
    public OpsClusterTaskEntity toEntity() {
        OpsClusterTaskEntity target = new OpsClusterTaskEntity();
        BeanUtils.copyProperties(this, target);
        target.setDatabasePort(getDatabasePort());
        return target;
    }

    /**
     * get database port
     *
     * @return port
     */
    private Integer getDatabasePort() {
        return Optional.of(getPort()).orElse(5432L).intValue();
    }
}
