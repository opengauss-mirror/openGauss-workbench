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
 * OlkConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/olk/OlkConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.olk;

import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsOlkEntity;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.DadReqPath;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.OlkParamDto;
import org.springframework.beans.BeanUtils;

import java.nio.file.Path;
import java.util.List;

@Data
public class OlkConfig {
    private String id;
    private String name;
    private String olkTarId;
    private String ssTarId;
    private String dadTarId;
    private String dadPort;
    private String dadInstallPath;
    private String dadInstallHostId;
    private String dadInstallUsername;
    private String dadInstallPassword;
    private String zkTarId;
    private String ssPort;
    private String olkPort;
    private String zkPort;
    private String ssInstallPath;
    private String ssUploadPath;
    private String olkInstallPath;
    private String olkUploadPath;
    private String ssInstallHostId;
    private String olkInstallHostId;
    private String ssInstallUsername;
    private String olkInstallUsername;
    private String ssInstallPassword;
    private String olkInstallPassword;
    private OlkParamDto olkParamConfig;
    private String remark;
    // sharding datasource config
    private List<ShardingDatasourceConfig> dsConfig;
    private String tableName;
    private String columns;
    private String ruleYaml;

    public String getDadLogFileName () {
        // return Path.of(dadInstallPath, DadReqPath.LOG_FILE_NAME + id).toString();
        return Path.of(dadInstallPath, DadReqPath.OUTPUT_LOG).toString().replace("\\", "/");
    }

    public OpsOlkEntity toEntity () {
        OpsOlkEntity entity = new OpsOlkEntity();
        BeanUtils.copyProperties(this, entity);
        return entity;
    }
}
