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
 * OpsOlkEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsOlkEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.domain.model.ops.olk.ShardingDatasourceConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.OlkParamDto;

import java.util.List;

@Data
@TableName("ops_olk")
public class OpsOlkEntity extends BaseEntity {
    @TableId
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
    @TableField(exist = false)
    private String ssInstallPassword;
    @TableField(exist = false)
    private String olkInstallPassword;
    private String tableName;
    private String columns;
    private String ruleYaml;
}
