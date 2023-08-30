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
 * OlkPageVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/olk/OlkPageVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.olk;

import lombok.Data;

@Data
public class OlkPageVO {
    private String id;
    private String name;
    private String dadPkgName;
    private String dadInstallIp;
    private String dadInstallUsername;
    private String dadInstallPath;
    private String dadPort;
    private String zkPkgName;
    private String zkPort;
    private String ssPkgName;
    private String ssInstallIp;
    private String ssInstallUsername;
    private String ssInstallPath;
    private String ssUploadPath;
    private String ssPort;
    private String olkPkgName;
    private String olkInstallIp;
    private String olkInstallUsername;
    private String olkInstallPath;
    private String olkUploadPath;
    private String olkPort;
    private String tableName;
    private String columns;
    private String ruleYaml;
    private String remark;
    private String updateTime;
}
